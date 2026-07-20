// SPDX-License-Identifier: MPL-2.0
package es.urjc.etsii.grafo.moocore;

import org.junit.jupiter.api.Assertions;
import org.tukaani.xz.XZInputStream;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class UpstreamSuiteSupport {

    static final String TESTSUITE_COMMIT = "9a9b80b61aff0f48c8935a927a4bf841e3f99b3c";
    private static final double ABSOLUTE_TOLERANCE = 1e-12;
    private static final double RELATIVE_TOLERANCE = 1e-10;
    private static final Pattern OBJECTIVES = Pattern.compile("(?:--obj=|-o\\s+)([+\\-0]+)");

    private UpstreamSuiteSupport() {
    }

    static Path suiteRoot() {
        String configured = System.getProperty("moocore.testsuite.path");
        if (configured == null || configured.isBlank()) {
            throw new IllegalStateException("moocore.testsuite.path is not configured; use -Pmoocore-testsuite");
        }
        Path root = Path.of(configured).toAbsolutePath().normalize();
        if (!Files.isRegularFile(root.resolve("regtest.py"))) {
            throw new IllegalStateException("moocore testsuite is missing at " + root
                    + "; initialize the testsuite submodule");
        }
        return root;
    }

    static void verifyRevisionAndCoverage(List<UpstreamCase> cases) throws Exception {
        Path root = suiteRoot();
        Process process = new ProcessBuilder("git", "-C", root.toString(), "rev-parse", "HEAD")
                .redirectErrorStream(true)
                .start();
        String revision;
        try (InputStream input = process.getInputStream()) {
            revision = new String(input.readAllBytes(), StandardCharsets.UTF_8).trim();
        }
        int exitCode = process.waitFor();
        Assertions.assertEquals(0, exitCode, "cannot determine testsuite submodule revision");
        Assertions.assertEquals(TESTSUITE_COMMIT, revision, "unexpected testsuite submodule revision");

        Set<String> discovered = new LinkedHashSet<>();
        try (var paths = Files.walk(root)) {
            paths.filter(path -> path.getFileName().toString().endsWith(".test"))
                    .map(root::relativize)
                    .map(Path::toString)
                    .map(value -> value.replace('\\', '/'))
                    .sorted()
                    .forEach(discovered::add);
        }
        Set<String> declared = new LinkedHashSet<>();
        for (UpstreamCase testCase : cases) {
            Assertions.assertTrue(declared.add(testCase.path()), "duplicate manifest entry " + testCase.path());
        }
        Assertions.assertEquals(discovered, declared,
                "every upstream recipe must have an explicit coverage-manifest entry");
        Assertions.assertEquals(109, declared.size());
        int invariantCases = 0;
        for (UpstreamCase testCase : cases) {
            if (testCase.mode() == Mode.POLYGON_INVARIANT) {
                invariantCases++;
            }
        }
        Assertions.assertEquals(2, invariantCases);
    }

    static List<UpstreamCase> loadCases() throws IOException {
        InputStream input = UpstreamSuiteSupport.class.getClassLoader()
                .getResourceAsStream("moocore-testsuite-cases.txt");
        if (input == null) {
            throw new IOException("missing moocore-testsuite-cases.txt");
        }
        List<UpstreamCase> result = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] columns = line.split("\\s+");
                if (columns.length != 2) {
                    throw new IOException("invalid testsuite manifest line: " + line);
                }
                result.add(new UpstreamCase(columns[0], Mode.valueOf(columns[1])));
            }
        }
        return List.copyOf(result);
    }

    static String recipe(UpstreamCase testCase) throws IOException {
        String recipe = Files.readString(suiteRoot().resolve(testCase.path()), StandardCharsets.UTF_8);
        return recipe.replace("${TESTNAME}", testCase.name());
    }

    static List<String> commands(UpstreamCase testCase) throws IOException {
        String text = recipe(testCase)
                .replace("${PROGRAM}", "@@PROGRAM@@")
                .replace("$PROGRAM", "@@PROGRAM@@")
                .replace('\n', ' ');
        List<String> commands = new ArrayList<>();
        int start = text.indexOf("@@PROGRAM@@");
        while (start >= 0) {
            start += "@@PROGRAM@@".length();
            int end = text.indexOf("@@PROGRAM@@", start);
            String command = text.substring(start, end < 0 ? text.length() : end).trim();
            commands.add(command);
            start = end;
        }
        return commands;
    }

    static Dataset readDataset(Path path) throws IOException {
        return MooCore.readDataset(path);
    }

    static Dataset readDataset(String relativePath) throws IOException {
        return readDataset(suiteRoot().resolve(relativePath));
    }

    static Dataset project(Dataset dataset, String command) {
        double[][] points = dataset.points();
        int objectives = points[0].length;
        Matcher matcher = OBJECTIVES.matcher(command);
        if (!matcher.find()) {
            return dataset;
        }
        String specification = matcher.group(1);
        if (specification.length() != objectives) {
            throw new IllegalArgumentException("objective specification has the wrong length: " + command);
        }
        int kept = 0;
        for (int i = 0; i < objectives; i++) {
            if (specification.charAt(i) != '0') {
                kept++;
            }
        }
        double[][] projected = new double[points.length][kept];
        for (int row = 0; row < points.length; row++) {
            int target = 0;
            for (int objective = 0; objective < objectives; objective++) {
                if (specification.charAt(objective) != '0') {
                    projected[row][target++] = points[row][objective];
                }
            }
        }
        return new Dataset(projected, dataset.sets());
    }

    static boolean[] directions(String command, int originalObjectives) {
        Matcher matcher = OBJECTIVES.matcher(command);
        if (matcher.find()) {
            String specification = matcher.group(1);
            if (specification.length() != originalObjectives) {
                throw new IllegalArgumentException("objective specification has the wrong length: " + command);
            }
            int kept = 0;
            for (int i = 0; i < specification.length(); i++) {
                if (specification.charAt(i) != '0') {
                    kept++;
                }
            }
            boolean[] directions = new boolean[kept];
            int target = 0;
            for (int i = 0; i < specification.length(); i++) {
                char value = specification.charAt(i);
                if (value != '0') {
                    directions[target++] = value == '+';
                }
            }
            return directions;
        }
        boolean maximise = command.contains("--maximise") || command.contains("--maximize");
        boolean[] directions = new boolean[originalObjectives];
        Arrays.fill(directions, maximise);
        return directions;
    }

    static List<double[][]> sets(Dataset dataset, boolean union) {
        if (union) {
            return Collections.singletonList(dataset.points());
        }
        double[][] points = dataset.points();
        int[] sets = dataset.sets();
        Map<Integer, List<double[]>> grouped = new LinkedHashMap<>();
        for (int i = 0; i < points.length; i++) {
            grouped.computeIfAbsent(sets[i], ignored -> new ArrayList<>()).add(points[i]);
        }
        List<double[][]> result = new ArrayList<>();
        for (List<double[]> group : grouped.values()) {
            result.add(group.toArray(double[][]::new));
        }
        return result;
    }

    static List<String> expectedLines(UpstreamCase testCase) throws IOException {
        Path expected = suiteRoot().resolve(testCase.path().replace(".test", ".exp"));
        if (!Files.isRegularFile(expected)) {
            expected = Path.of(expected + ".xz");
        }
        if (!Files.isRegularFile(expected)) {
            throw new IOException("missing expected output for " + testCase.path());
        }
        InputStream raw = Files.newInputStream(expected);
        InputStream input = expected.getFileName().toString().endsWith(".xz")
                ? new XZInputStream(raw) : raw;
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }

    static List<double[]> expectedNumericRows(UpstreamCase testCase) throws IOException {
        List<double[]> rows = new ArrayList<>();
        for (String line : expectedLines(testCase)) {
            String trimmed = line.trim().toLowerCase(Locale.ROOT);
            if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.contains("warning")
                    || trimmed.contains("error") || trimmed.startsWith("===")) {
                continue;
            }
            String[] tokens = trimmed.split("\\s+");
            double[] row = new double[tokens.length];
            boolean numeric = true;
            for (int i = 0; i < tokens.length; i++) {
                Double value = parseExpectedNumber(tokens[i]);
                if (value == null) {
                    numeric = false;
                    break;
                }
                row[i] = value;
            }
            if (numeric) {
                rows.add(row);
            }
        }
        return rows;
    }

    static List<Double> flatten(List<double[]> rows) {
        List<Double> result = new ArrayList<>();
        for (double[] row : rows) {
            for (double value : row) {
                result.add(value);
            }
        }
        return result;
    }

    static void assertScalars(List<Double> expected, List<Double> actual, String context) {
        Assertions.assertEquals(expected.size(), actual.size(), context + " result count");
        for (int i = 0; i < expected.size(); i++) {
            assertClose(expected.get(i), actual.get(i), context + " value " + i);
        }
    }

    static void assertPrintedScalars(UpstreamCase testCase, List<Double> actual, String context)
            throws IOException {
        List<PrintedNumber> expected = new ArrayList<>();
        for (String line : expectedLines(testCase)) {
            String trimmed = line.trim().toLowerCase(Locale.ROOT);
            if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.contains("warning")
                    || trimmed.contains("error") || trimmed.startsWith("===")) {
                continue;
            }
            String[] tokens = trimmed.split("\\s+");
            List<PrintedNumber> row = new ArrayList<>();
            boolean numeric = true;
            for (String token : tokens) {
                Double value = parseExpectedNumber(token);
                if (value == null) {
                    numeric = false;
                    break;
                }
                row.add(new PrintedNumber(value, printedTolerance(token)));
            }
            if (numeric) {
                expected.addAll(row);
            }
        }
        Assertions.assertEquals(expected.size(), actual.size(), context + " result count");
        for (int i = 0; i < expected.size(); i++) {
            PrintedNumber number = expected.get(i);
            if (number.tolerance() == 0.0) {
                assertClose(number.value(), actual.get(i), context + " value " + i);
            } else {
                double tolerance = Math.max(number.tolerance(), ABSOLUTE_TOLERANCE
                        + RELATIVE_TOLERANCE * Math.max(Math.abs(number.value()), Math.abs(actual.get(i))));
                Assertions.assertEquals(number.value(), actual.get(i), tolerance, context + " value " + i);
            }
        }
    }

    static void assertRows(List<double[]> expected, List<double[]> actual,
                           boolean sort, boolean preserveSignedZero, String context) {
        List<double[]> expectedCopy = deepCopy(expected);
        List<double[]> actualCopy = deepCopy(actual);
        if (sort) {
            expectedCopy.sort(UpstreamSuiteSupport::compareRows);
            actualCopy.sort(UpstreamSuiteSupport::compareRows);
        }
        Assertions.assertEquals(expectedCopy.size(), actualCopy.size(), context + " row count");
        for (int row = 0; row < expectedCopy.size(); row++) {
            double[] expectedRow = expectedCopy.get(row);
            double[] actualRow = actualCopy.get(row);
            Assertions.assertEquals(expectedRow.length, actualRow.length, context + " columns at row " + row);
            for (int column = 0; column < expectedRow.length; column++) {
                double expectedValue = expectedRow[column];
                double actualValue = actualRow[column];
                if (preserveSignedZero && expectedValue == 0.0 && actualValue == 0.0) {
                    Assertions.assertEquals(Double.doubleToRawLongBits(expectedValue),
                            Double.doubleToRawLongBits(actualValue),
                            context + " signed zero at row " + row + ", column " + column);
                } else {
                    assertClose(expectedValue, actualValue,
                            context + " at row " + row + ", column " + column);
                }
            }
        }
    }

    static void assertClose(double expected, double actual, String context) {
        if (Double.isInfinite(expected) || Double.isNaN(expected)) {
            Assertions.assertEquals(expected, actual, context);
            return;
        }
        double tolerance = ABSOLUTE_TOLERANCE
                + RELATIVE_TOLERANCE * Math.max(Math.abs(expected), Math.abs(actual));
        Assertions.assertEquals(expected, actual, tolerance, context);
    }

    static double[] reference(String command) {
        Matcher matcher = Pattern.compile("-r\\s+([\"'])(.*?)\\1").matcher(command);
        if (!matcher.find()) {
            return null;
        }
        String[] tokens = matcher.group(2).trim().split("\\s+");
        double[] result = new double[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            result[i] = Double.parseDouble(tokens[i]);
        }
        return result;
    }

    static String referenceFile(String command) {
        Matcher matcher = Pattern.compile("-r\\s+([^\\s]+)").matcher(command);
        if (!matcher.find()) {
            return null;
        }
        String value = matcher.group(1).replace("\"", "").replace("'", "");
        return value.contains(" ") || isNumber(value) ? null : value;
    }

    static Path resolve(UpstreamCase testCase, String value) {
        value = value.replace("${TESTNAME}", testCase.name()).replace("$TESTNAME", testCase.name());
        while (value.startsWith("./")) {
            value = value.substring(2);
        }
        return suiteRoot().resolve(testCase.path()).getParent().resolve(value).normalize();
    }

    static String objectiveSpecification(String command) {
        Matcher matcher = OBJECTIVES.matcher(command);
        return matcher.find() ? matcher.group(1) : null;
    }

    private static Double parseExpectedNumber(String token) {
        if (token.equals("inf") || token.equals("+inf") || token.equals("infinity")) {
            return Double.POSITIVE_INFINITY;
        }
        if (token.equals("-inf") || token.equals("-infinity")) {
            return Double.NEGATIVE_INFINITY;
        }
        if (token.equals("...") || token.endsWith(":")) {
            return null;
        }
        String numeric = token.replace("...", "");
        try {
            return Double.parseDouble(numeric);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private static double printedTolerance(String token) {
        if (!token.endsWith("...")) {
            return 0.0;
        }
        String numeric = token.substring(0, token.length() - 3);
        int exponentMarker = Math.max(numeric.indexOf('e'), numeric.indexOf('E'));
        int exponent = exponentMarker < 0 ? 0 : Integer.parseInt(numeric.substring(exponentMarker + 1));
        String mantissa = exponentMarker < 0 ? numeric : numeric.substring(0, exponentMarker);
        int decimal = mantissa.indexOf('.');
        int decimals = decimal < 0 ? 0 : mantissa.length() - decimal - 1;
        return Math.pow(10.0, exponent - decimals);
    }

    private static boolean isNumber(String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    private static List<double[]> deepCopy(List<double[]> rows) {
        List<double[]> result = new ArrayList<>(rows.size());
        for (double[] row : rows) {
            result.add(row.clone());
        }
        return result;
    }

    private static int compareRows(double[] left, double[] right) {
        int length = Math.min(left.length, right.length);
        for (int i = 0; i < length; i++) {
            int comparison = Double.compare(left[i], right[i]);
            if (comparison != 0) {
                return comparison;
            }
        }
        return Integer.compare(left.length, right.length);
    }

    enum Mode {
        ORACLE,
        POLYGON_INVARIANT
    }

    record UpstreamCase(String path, Mode mode) {
        String family() {
            return path.substring(0, path.indexOf('/'));
        }

        String name() {
            return Path.of(path).getFileName().toString().replace(".test", "");
        }
    }

    private record PrintedNumber(double value, double tolerance) {
    }
}
