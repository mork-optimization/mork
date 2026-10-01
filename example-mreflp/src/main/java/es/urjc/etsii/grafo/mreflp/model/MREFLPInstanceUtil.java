package es.urjc.etsii.grafo.mreflp.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class MREFLPInstanceUtil {
    private MREFLPInstanceUtil() {}

    public static String canonicalName(String filename) {
        String name = filename.replaceFirst("\\.txt$", "");
        if (name.endsWith("_t")) name = name.substring(0, name.length() - 2);
        if (name.startsWith("QAP_")) name = name.substring(4).replaceFirst("_n$", "");
        if (name.startsWith("AnKeVa_2005_")) name = name.replace("AnKeVa_2005_", "AKV").replace("dept", "");
        return name;
    }

    /** Anjos et al. (2018), Theorem 3 and Table 1. The even-row bound need not be tight. */
    public static int groupCount(int n, int r) {
        if (n < 1 || r < 2 || r > 5) throw new IllegalArgumentException("Expected n > 0 and capacity 2..5");
        if (n <= 16 && r <= 4) {
            int[][] table = {
                    {0, 1, 1, 2, 2, 3, 4, 4, 5, 5, 6, 7, 7, 8, 9, 9, 10},
                    {0, 1, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7, 8},
                    {0, 1, 1, 1, 1, 2, 2, 2, 3, 3, 4, 4, 4, 5, 5, 5, 6}
            };
            return table[r - 2][n];
        }
        if (n <= r) return 1;
        if (2 * n < 3 * r + 3) return 2;
        if (r == 2) return (2 * n + 2) / 3 - 1;
        if (r % 2 == 1) return 2 * n / (r + 1);
        int l = (n - r / 2 - 1 + r) / (r + 1);
        return 2 * l + 1;
    }

    public static String sha256(Path path) throws IOException {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            try (var in = Files.newInputStream(path)) {
                byte[] buffer = new byte[65536];
                int count;
                while ((count = in.read(buffer)) != -1) digest.update(buffer, 0, count);
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }

    public static MREFLPInstance read(Path source, int capacity) throws IOException {
        try (var reader = Files.newBufferedReader(source)) {
            int n = Integer.parseInt(nextLine(reader));
            if (n < 1) throw new IllegalArgumentException("Invalid facility count");
            String[] widths = nextLine(reader).split("\\s+");
            for (String width : widths) if (!width.equals("1")) throw new IllegalArgumentException("Non-unit width");
            int missing = n - widths.length;
            boolean knownShortHeader = source.getFileName().toString().startsWith("QAP_sko") && missing == 5;
            if (missing != 0 && !knownShortHeader) throw new IllegalArgumentException("Invalid unit-width header in " + source);
            long[][] flows = new long[n][n];
            boolean upper = true, symmetric = true;
            for (int i = 0; i < n; i++) {
                String[] row = nextLine(reader).split("\\s+");
                if (row.length != n) throw new IllegalArgumentException("Invalid matrix row " + i + " in " + source);
                for (int j = 0; j < n; j++) {
                    flows[i][j] = Long.parseLong(row[j]);
                    if (i > j && flows[i][j] != 0) upper = false;
                }
            }
            String trailing;
            while ((trailing = reader.readLine()) != null) if (!trailing.isBlank()) throw new IllegalArgumentException("Trailing data");
            for (int i = 0; i < n; i++) for (int j = i + 1; j < n; j++) {
                if (flows[i][j] != flows[j][i]) symmetric = false;
            }
            if (!symmetric && !upper) throw new IllegalArgumentException("Unsupported asymmetric flow matrix");
            if (upper) for (int i = 0; i < n; i++) for (int j = i + 1; j < n; j++) flows[j][i] = flows[i][j];
            return new MREFLPInstance(canonicalName(source.getFileName().toString()), source.getParent().getFileName().toString(),
                    capacity, groupCount(n, capacity), flows, missing, sha256(source));
        }
    }

    private static String nextLine(BufferedReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) if (!line.isBlank()) return line.trim();
        throw new IllegalArgumentException("Unexpected end of instance");
    }
}
