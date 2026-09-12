package es.urjc.etsii.grafo.bmssc.model;

import es.urjc.etsii.grafo.exception.InstanceImportException;
import es.urjc.etsii.grafo.io.InstanceImporter;

import java.io.BufferedReader;
import java.io.IOException;

public class BMSSCInstanceImporter extends InstanceImporter<BMSSCInstance> {
    @Override
    public BMSSCInstance importInstance(BufferedReader reader, String filename) throws IOException {
        int lineNumber = 1;
        try {
            String line = reader.readLine();
            if (line == null) throw new IllegalArgumentException("missing header");
            String[] header = line.split(",", -1);
            if (header.length != 3) throw new IllegalArgumentException("expected header n,d,k");
            int n = Integer.parseInt(header[0].trim());
            int d = Integer.parseInt(header[1].trim());
            int k = Integer.parseInt(header[2].trim());
            if (n <= 0 || d <= 0 || k < 1 || k > n) {
                throw new IllegalArgumentException("expected n > 0, d > 0 and 1 <= k <= n");
            }
            double[][] points = new double[n][];
            for (int p = 0; p < n; p++) {
                lineNumber = p + 2;
                line = reader.readLine();
                if (line == null) throw new IllegalArgumentException("missing point");
                String[] values = line.split(",", -1);
                if (values.length != d) throw new IllegalArgumentException("expected " + d + " dimensions");
                points[p] = new double[d];
                for (int dimension = 0; dimension < d; dimension++) {
                    double value = Double.parseDouble(values[dimension].trim());
                    if (!Double.isFinite(value)) throw new IllegalArgumentException("non-finite coordinate");
                    points[p][dimension] = value;
                }
            }
            lineNumber = n + 2;
            if (reader.readLine() != null) throw new IllegalArgumentException("unexpected extra row");
            return new BMSSCInstance(filename, n, d, k, points);
        } catch (IllegalArgumentException e) {
            throw new InstanceImportException(filename + ": line " + lineNumber + ": " + e.getMessage(), e);
        }
    }
}
