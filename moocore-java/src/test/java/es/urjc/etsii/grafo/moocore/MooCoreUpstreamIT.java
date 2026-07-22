// SPDX-License-Identifier: MPL-2.0
package es.urjc.etsii.grafo.moocore;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static es.urjc.etsii.grafo.moocore.UpstreamSuiteSupport.UpstreamCase;

class MooCoreUpstreamIT {

    private static List<UpstreamCase> cases;

    @BeforeAll
    static void validateSuite() throws Exception {
        cases = UpstreamSuiteSupport.loadCases();
        UpstreamSuiteSupport.verifyRevisionAndCoverage(cases);
    }

    @TestFactory
    Stream<DynamicTest> upstreamRegressionSuite() {
        String selected = System.getProperty("moocore.testsuite.case", "");
        List<DynamicTest> tests = new ArrayList<>();
        for (UpstreamCase testCase : cases) {
            if (selected.isBlank() || testCase.path().contains(selected)) {
                tests.add(DynamicTest.dynamicTest(testCase.path(), () -> run(testCase)));
            }
        }
        return tests.stream();
    }

    private static void run(UpstreamCase testCase) throws Exception {
        switch (testCase.family()) {
            case "dominatedsets" -> dominatedSets(testCase);
            case "eaf" -> eaf(testCase);
            case "epsilon" -> epsilon(testCase);
            case "hv" -> hypervolume(testCase);
            case "hvapprox" -> approximateHypervolume(testCase);
            case "igd" -> distanceIndicators(testCase);
            case "ndsort" -> nondominatedSort(testCase);
            case "nondominated" -> nondominated(testCase);
            default -> throw new IllegalArgumentException("unsupported testsuite family " + testCase.family());
        }
    }

    private static void hypervolume(UpstreamCase testCase) throws Exception {
        if (testCase.name().equals("testempty")) {
            Assertions.assertThrows(IOException.class, () -> UpstreamSuiteSupport.readDataset(input(testCase)));
            Assertions.assertTrue(UpstreamSuiteSupport.expectedLines(testCase).stream()
                    .anyMatch(line -> line.toLowerCase().contains("no input data")));
            return;
        }

        Dataset dataset = UpstreamSuiteSupport.readDataset(input(testCase));
        List<Double> actual = new ArrayList<>();
        boolean contributions = testCase.path().contains("/hvc/");
        for (String command : UpstreamSuiteSupport.commands(testCase)) {
            double[] reference = UpstreamSuiteSupport.reference(command);
            Assertions.assertNotNull(reference, "missing reference in " + testCase.path());
            boolean union = hasUnion(command);
            for (double[][] set : UpstreamSuiteSupport.sets(dataset, union)) {
                if (!strictlyDominatesReference(set, reference)) {
                    continue;
                }
                if (contributions) {
                    double[] values = MooCore.hypervolumeContributions(
                            set, reference, new boolean[]{false}, true);
                    for (double value : values) {
                        actual.add(value);
                    }
                } else {
                    actual.add(MooCore.hypervolume(set, reference));
                }
            }
        }
        UpstreamSuiteSupport.assertPrintedScalars(testCase, actual, testCase.path());
    }

    private static void approximateHypervolume(UpstreamCase testCase) throws Exception {
        Dataset dataset = UpstreamSuiteSupport.readDataset(input(testCase));
        List<Double> expected = UpstreamSuiteSupport.flatten(
                UpstreamSuiteSupport.expectedNumericRows(testCase));
        List<Double> actual = new ArrayList<>();
        List<Boolean> stochastic = new ArrayList<>();
        for (String command : UpstreamSuiteSupport.commands(testCase)) {
            double[] reference = UpstreamSuiteSupport.reference(command);
            int methodNumber = integerOption(command, "--method", 3);
            long samples = integerOption(command, "--nsamples", 100_000);
            long seed = integerOption(command, "--seed", 0);
            HypervolumeApproximation method = switch (methodNumber) {
                case 1 -> HypervolumeApproximation.DZ2019_MC;
                case 2 -> HypervolumeApproximation.DZ2019_HW;
                case 3 -> HypervolumeApproximation.RPHI_FWE_PLUS;
                default -> throw new IllegalArgumentException("unknown approximation method " + methodNumber);
            };
            for (double[][] set : UpstreamSuiteSupport.sets(dataset, hasUnion(command))) {
                actual.add(MooCore.approximateHypervolume(
                        set, reference, new boolean[]{false}, samples, seed, method));
                stochastic.add(method == HypervolumeApproximation.DZ2019_MC);
            }
        }
        Assertions.assertEquals(expected.size(), actual.size(), testCase.path() + " result count");
        for (int i = 0; i < expected.size(); i++) {
            double tolerance = Math.abs(expected.get(i)) * (stochastic.get(i) ? 0.05 : 0.005) + 1e-15;
            Assertions.assertEquals(expected.get(i), actual.get(i), tolerance,
                    testCase.path() + " approximation " + i);
        }
    }

    private static void nondominatedSort(UpstreamCase testCase) throws Exception {
        Dataset dataset = UpstreamSuiteSupport.readDataset(input(testCase));
        List<Double> expected = UpstreamSuiteSupport.flatten(
                UpstreamSuiteSupport.expectedNumericRows(testCase));
        int[] actual = MooCore.paretoRank(dataset.points());
        Assertions.assertEquals(expected.size(), actual.length, testCase.path() + " rank count");
        for (int i = 0; i < actual.length; i++) {
            Assertions.assertEquals(expected.get(i).intValue(), actual[i], testCase.path() + " rank " + i);
        }
    }

    private static void nondominated(UpstreamCase testCase) throws Exception {
        if (testCase.name().equals("nondom-count")) {
            nondominatedCounts(testCase);
            return;
        }
        String command = UpstreamSuiteSupport.commands(testCase).get(0);
        Dataset original = UpstreamSuiteSupport.readDataset(input(testCase));
        int originalObjectives = original.points()[0].length;
        Dataset dataset = UpstreamSuiteSupport.project(original, command);
        boolean[] directions = UpstreamSuiteSupport.directions(command, originalObjectives);
        List<double[][]> sets = UpstreamSuiteSupport.sets(dataset, hasUnion(command));

        if (!command.contains("--filter")) {
            List<Integer> expectedCounts = expectedNondominatedCounts(testCase);
            Assertions.assertEquals(expectedCounts.size(), sets.size(), testCase.path() + " set count");
            for (int i = 0; i < sets.size(); i++) {
                int actual = countTrue(MooCore.isNondominated(sets.get(i), directions, false));
                Assertions.assertEquals(expectedCounts.get(i), actual,
                        testCase.path() + " nondominated count in set " + (i + 1));
            }
            return;
        }

        List<double[]> actual = new ArrayList<>();
        boolean union = hasUnion(command);
        boolean[] selected = union
                ? MooCore.isNondominated(dataset.points(), directions, false)
                : MooCore.isNondominatedWithinSets(dataset.points(), dataset.sets(), directions, false);
        double[][] originalPoints = original.points();
        for (int i = 0; i < selected.length; i++) {
            if (selected[i]) {
                actual.add(originalPoints[i]);
            }
        }
        List<double[]> expected = UpstreamSuiteSupport.expectedNumericRows(testCase);
        boolean sorted = UpstreamSuiteSupport.recipe(testCase).contains("sort ");
        boolean signedZero = testCase.name().equals("signed_zeros-2d");
        UpstreamSuiteSupport.assertRows(expected, actual, sorted, signedZero, testCase.path());
    }

    private static void nondominatedCounts(UpstreamCase testCase) throws Exception {
        Matcher files = Pattern.compile("\\.\\./data/([^\\s]+)").matcher(UpstreamSuiteSupport.recipe(testCase));
        List<Integer> expected = expectedNondominatedCounts(testCase);
        List<Integer> actual = new ArrayList<>();
        while (files.find()) {
            Dataset dataset = UpstreamSuiteSupport.readDataset("data/" + files.group(1));
            actual.add(countTrue(MooCore.isNondominated(dataset.points())));
        }
        Assertions.assertEquals(expected, actual, testCase.path());
    }

    private static List<Integer> expectedNondominatedCounts(UpstreamCase testCase) throws IOException {
        List<Integer> result = new ArrayList<>();
        Pattern tableRow = Pattern.compile("^.*?\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s*$");
        for (String line : UpstreamSuiteSupport.expectedLines(testCase)) {
            Matcher matcher = tableRow.matcher(line.trim());
            if (matcher.matches()) {
                int size = Integer.parseInt(matcher.group(2));
                int nondominated = Integer.parseInt(matcher.group(3));
                int dominated = Integer.parseInt(matcher.group(4));
                if (size == nondominated + dominated) {
                    result.add(nondominated);
                }
            }
        }
        if (result.isEmpty()) {
            throw new IllegalArgumentException("no nondominated counts in " + testCase.path());
        }
        return result;
    }

    private static void epsilon(UpstreamCase testCase) throws Exception {
        List<Double> actual = new ArrayList<>();
        for (String command : UpstreamSuiteSupport.commands(testCase)) {
            Dataset input = epsilonInput(testCase, command);
            Dataset reference = epsilonReference(testCase, command);
            int objectives = input.points()[0].length;
            input = UpstreamSuiteSupport.project(input, command);
            reference = UpstreamSuiteSupport.project(reference, command);
            boolean[] directions = UpstreamSuiteSupport.directions(command, objectives);
            double[][] referencePoints = reference.points();
            for (double[][] set : UpstreamSuiteSupport.sets(input, false)) {
                actual.add(command.contains("--multiplicative")
                        ? MooCore.epsilonMultiplicative(set, referencePoints, directions)
                        : MooCore.epsilonAdditive(set, referencePoints, directions));
            }
        }
        List<Double> expected = UpstreamSuiteSupport.flatten(
                UpstreamSuiteSupport.expectedNumericRows(testCase));
        UpstreamSuiteSupport.assertScalars(expected, actual, testCase.path());
    }

    private static void distanceIndicators(UpstreamCase testCase) throws Exception {
        List<Double> actual = new ArrayList<>();
        for (String command : UpstreamSuiteSupport.commands(testCase)) {
            Dataset input = igdInput(testCase, command);
            Dataset reference = igdReference(testCase, command);
            int objectives = input.points()[0].length;
            input = UpstreamSuiteSupport.project(input, command);
            reference = UpstreamSuiteSupport.project(reference, command);
            boolean[] directions = UpstreamSuiteSupport.directions(command, objectives);
            double[][] filteredReference = MooCore.filterDominated(reference.points(), directions, false);
            for (double[][] set : UpstreamSuiteSupport.sets(input, false)) {
                double gd = MooCore.igd(filteredReference, set, directions);
                double igd = MooCore.igd(set, filteredReference, directions);
                double igdPlus = MooCore.igdPlus(set, filteredReference, directions);
                double hausdorff = Math.max(gd, igd);
                if (command.contains("--all")) {
                    actual.add(gd);
                    actual.add(igd);
                    actual.add(gd);
                    actual.add(igd);
                    actual.add(igdPlus);
                    actual.add(hausdorff);
                } else {
                    actual.add(igd);
                }
            }
        }
        List<Double> expected = UpstreamSuiteSupport.flatten(
                UpstreamSuiteSupport.expectedNumericRows(testCase));
        UpstreamSuiteSupport.assertScalars(expected, actual, testCase.path());
    }

    private static void eaf(UpstreamCase testCase) throws Exception {
        if (testCase.name().startsWith("eafdiff-")) {
            Dataset left = UpstreamSuiteSupport.readDataset("data/ALG_1_dat.xz");
            Dataset right = UpstreamSuiteSupport.readDataset("data/ALG_2_dat.xz");
            double[][] actual = MooCore.eafDifference(left, right);
            List<double[]> expected = new ArrayList<>();
            for (double[] row : UpstreamSuiteSupport.expectedNumericRows(testCase)) {
                expected.add(new double[]{row[0], row[1], row[2] - row[3]});
            }
            UpstreamSuiteSupport.assertRows(
                    uniqueRows(expected), Arrays.asList(actual),
                    false, false, testCase.path());
            return;
        }

        Dataset dataset = UpstreamSuiteSupport.readDataset(input(testCase));
        double[][] actual;
        if (testCase.name().equals("testl1")) {
            int setCount = distinctSets(dataset.sets());
            actual = MooCore.eaf(dataset.points(), dataset.sets(), new double[]{100.0 / setCount});
        } else {
            actual = MooCore.eaf(dataset.points(), dataset.sets());
        }

        if (testCase.mode() == UpstreamSuiteSupport.Mode.POLYGON_INVARIANT) {
            Assertions.assertTrue(actual.length > 0, testCase.path() + " must produce EAF surfaces");
            for (double[] row : actual) {
                for (int i = 0; i < row.length - 1; i++) {
                    Assertions.assertTrue(Double.isFinite(row[i]), testCase.path() + " finite coordinate");
                }
                Assertions.assertTrue(row[row.length - 1] >= 0.0 && row[row.length - 1] <= 100.0,
                        testCase.path() + " valid percentile");
            }
            return;
        }

        int objectives = dataset.points()[0].length;
        List<double[]> expectedCoordinates = new ArrayList<>();
        for (double[] row : UpstreamSuiteSupport.expectedNumericRows(testCase)) {
            expectedCoordinates.add(Arrays.copyOf(row, objectives));
        }
        List<double[]> actualCoordinates = new ArrayList<>();
        for (double[] row : actual) {
            actualCoordinates.add(Arrays.copyOf(row, objectives));
        }
        UpstreamSuiteSupport.assertRows(expectedCoordinates, actualCoordinates,
                true, false, testCase.path());
    }

    private static void dominatedSets(UpstreamCase testCase) throws Exception {
        List<Dataset> datasets = List.of(
                UpstreamSuiteSupport.readDataset("data/DTLZDiscontinuousShape.3d.front.1000pts.10"),
                UpstreamSuiteSupport.readDataset("data/DTLZLinearShape.3d.front.1000pts.10"),
                UpstreamSuiteSupport.readDataset("data/DTLZSphereShape.3d.front.1000pts.10"),
                UpstreamSuiteSupport.readDataset("data/ran.1000pts.3d.10"));
        int[][] actual = new int[datasets.size()][datasets.size()];
        for (int left = 0; left < datasets.size(); left++) {
            List<double[][]> leftSets = UpstreamSuiteSupport.sets(datasets.get(left), false);
            for (int right = left + 1; right < datasets.size(); right++) {
                List<double[][]> rightSets = UpstreamSuiteSupport.sets(datasets.get(right), false);
                for (double[][] a : leftSets) {
                    for (double[][] b : rightSets) {
                        double ab = MooCore.epsilonAdditive(a, b);
                        double ba = MooCore.epsilonAdditive(b, a);
                        if (ab <= 0.0 && ba > 0.0) {
                            actual[left][right]++;
                        } else if (ab > 0.0 && ba <= 0.0) {
                            actual[right][left]++;
                        }
                    }
                }
            }
        }

        int[][] expected = parseDominatedSetMatrix(testCase, datasets.size());
        for (int row = 0; row < expected.length; row++) {
            for (int column = 0; column < expected.length; column++) {
                if (row != column) {
                    Assertions.assertEquals(expected[row][column], actual[row][column],
                            testCase.path() + " f" + (row + 1) + " versus f" + (column + 1));
                }
            }
        }
    }

    private static int[][] parseDominatedSetMatrix(UpstreamCase testCase, int size) throws IOException {
        int[][] result = new int[size][size];
        Pattern rowPattern = Pattern.compile("^f(\\d+)\\s+(.+)$");
        for (String line : UpstreamSuiteSupport.expectedLines(testCase)) {
            Matcher matcher = rowPattern.matcher(line.trim());
            if (!matcher.matches()) {
                continue;
            }
            int row = Integer.parseInt(matcher.group(1)) - 1;
            String[] values = matcher.group(2).trim().split("\\s+");
            if (values.length != size) {
                continue;
            }
            for (int column = 0; column < size; column++) {
                if (!values[column].equals("--")) {
                    result[row][column] = Integer.parseInt(values[column]);
                }
            }
        }
        return result;
    }

    private static Dataset epsilonInput(UpstreamCase testCase, String command) throws IOException {
        if (testCase.name().equals("bug-1")) {
            List<String> files = filesWithSuffix(command, ".inp");
            return UpstreamSuiteSupport.readDataset(UpstreamSuiteSupport.resolve(testCase, files.get(1)));
        }
        return UpstreamSuiteSupport.readDataset(input(testCase));
    }

    private static Dataset epsilonReference(UpstreamCase testCase, String command) throws IOException {
        if (testCase.name().equals("rmnk_0.0_10_16_1_0_random_search_1.txt")) {
            return UpstreamSuiteSupport.readDataset("data/rmnk_0.0_10_16_1_0_ref.txt.xz");
        }
        String reference = UpstreamSuiteSupport.referenceFile(command);
        Assertions.assertNotNull(reference, "missing reference file in " + command);
        return UpstreamSuiteSupport.readDataset(UpstreamSuiteSupport.resolve(testCase, reference));
    }

    private static Dataset igdInput(UpstreamCase testCase, String command) throws IOException {
        if (testCase.name().equals("ALG_1_ALG_2")) {
            return UpstreamSuiteSupport.readDataset(command.contains("\"$tmpfile2\"")
                    && command.indexOf("\"$tmpfile2\"") > command.indexOf("-r")
                    && command.indexOf("\"$tmpfile1\"") < 0
                    ? "data/ALG_2_dat.xz" : algInputAfterReference(command));
        }
        return UpstreamSuiteSupport.readDataset(input(testCase));
    }

    private static String algInputAfterReference(String command) {
        int reference = command.indexOf("-r");
        int first = command.indexOf("$tmpfile1", reference);
        int second = command.indexOf("$tmpfile2", reference);
        if (first < 0) {
            return "data/ALG_1_dat.xz";
        }
        if (second < 0) {
            return "data/ALG_2_dat.xz";
        }
        return first < second ? "data/ALG_2_dat.xz" : "data/ALG_1_dat.xz";
    }

    private static Dataset igdReference(UpstreamCase testCase, String command) throws IOException {
        if (testCase.name().equals("ALG_1_ALG_2")) {
            int reference = command.indexOf("-r");
            int first = command.indexOf("$tmpfile1", reference);
            int second = command.indexOf("$tmpfile2", reference);
            return UpstreamSuiteSupport.readDataset(first >= 0 && (second < 0 || first < second)
                    ? "data/ALG_1_dat.xz" : "data/ALG_2_dat.xz");
        }
        if (testCase.name().equals("spherical-3d-2000pts-10")) {
            return UpstreamSuiteSupport.readDataset("data/spherical-3d-2000pts-10.dat.xz");
        }
        String reference = UpstreamSuiteSupport.referenceFile(command);
        return UpstreamSuiteSupport.readDataset(UpstreamSuiteSupport.resolve(testCase, reference));
    }

    private static Path input(UpstreamCase testCase) {
        Path root = UpstreamSuiteSupport.suiteRoot();
        Path directory = root.resolve(testCase.path()).getParent();
        String name = testCase.name();
        if (testCase.path().equals("hv/init-area-bug.test")) {
            return root.resolve("data/ran.5d.400pts_t1");
        }
        if (testCase.path().equals("nondominated/ran.1000pts.3d.10.test")) {
            return root.resolve("eaf/ran.1000pts.3d.10.exp.xz");
        }
        if (name.equals("input1")) {
            return root.resolve("data/input1.dat");
        }
        if (name.equals("spherical-3d-2000pts-10")) {
            return root.resolve("data/spherical-3d-2000pts-10.dat.xz");
        }
        if (name.equals("testl1")) {
            return directory.resolve("test.dat");
        }
        if (name.equals("00-polygon-bug")) {
            return directory.resolve("polygon-bug.dat");
        }
        if (name.equals("mayowa-bug")) {
            return directory.resolve("mayowa-bug.dat");
        }

        List<Path> candidates = List.of(
                directory.resolve(name + ".inp"),
                directory.resolve(name + ".inp.xz"),
                directory.getParent() == null ? directory.resolve(name) : directory.getParent().resolve(name + ".inp"),
                root.resolve("data/" + name),
                root.resolve("data/" + name + ".xz"),
                root.resolve("data/" + name + ".dat.xz"),
                root.resolve("data/" + name + ".inp.xz"));
        for (Path candidate : candidates) {
            if (Files.isRegularFile(candidate)) {
                return candidate;
            }
        }
        throw new IllegalArgumentException("cannot locate input for " + testCase.path());
    }

    private static boolean hasUnion(String command) {
        return command.contains("--union") || Pattern.compile("(^|\\s)-u(\\s|$)").matcher(command).find();
    }

    private static boolean strictlyDominatesReference(double[][] points, double[] reference) {
        for (double[] point : points) {
            boolean dominates = true;
            for (int objective = 0; objective < reference.length; objective++) {
                if (point[objective] >= reference[objective]) {
                    dominates = false;
                    break;
                }
            }
            if (dominates) {
                return true;
            }
        }
        return false;
    }

    private static int integerOption(String command, String option, int defaultValue) {
        Matcher matcher = Pattern.compile(Pattern.quote(option) + "(?:=|\\s+)(\\d+)").matcher(command);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : defaultValue;
    }

    private static List<String> filesWithSuffix(String command, String suffix) {
        List<String> result = new ArrayList<>();
        Matcher matcher = Pattern.compile("([^\\s]+" + Pattern.quote(suffix) + ")").matcher(command);
        while (matcher.find()) {
            result.add(matcher.group(1));
        }
        return result;
    }

    private static int countTrue(boolean[] values) {
        int result = 0;
        for (boolean value : values) {
            if (value) {
                result++;
            }
        }
        return result;
    }

    private static int distinctSets(int[] sets) {
        Map<Integer, Boolean> unique = new LinkedHashMap<>();
        for (int set : sets) {
            unique.put(set, Boolean.TRUE);
        }
        return unique.size();
    }

    private static List<double[]> uniqueRows(List<double[]> rows) {
        Map<List<Double>, double[]> unique = new LinkedHashMap<>();
        for (double[] row : rows) {
            List<Double> key = new ArrayList<>(row.length);
            for (double value : row) {
                key.add(value);
            }
            unique.putIfAbsent(key, row);
        }
        return new ArrayList<>(unique.values());
    }
}
