package es.urjc.etsii.grafo.TSPTW.model;

import es.urjc.etsii.grafo.io.InstanceImporter;
import es.urjc.etsii.grafo.util.DoubleComparator;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Locale;
import java.util.Scanner;

public class TSPTWInstanceImporter extends InstanceImporter<TSPTWInstance> {

    /**
     * Load instance from file. This method is called by the framework when a new instance is being loaded.
     * Note that instance load time is never considered in the total execution time.
     * @param reader Input buffer, managed by the framework.
     * @param suggestedName Suggested filename for the instance, can be ignored.
     *                      By default, the suggested filename is built by removing the path and extension info.
     *                      For example, for the path "instances/TSP/TSP-1.txt", the suggestedName would be "TSP-1"
     * @return immutable instance
     * @throws IOException If an error is encountered while the instance is being parsed
     */
    @Override
    public TSPTWInstance importInstance(BufferedReader reader, String suggestedName) throws IOException {
        try (Scanner sc = new Scanner(reader)) {
            sc.useLocale(Locale.US);

            // Customer 0 is the depot.
            if (!sc.hasNextInt()) {
                throw new IOException("invalid number of customers (missing/invalid n)");
            }
            int n = sc.nextInt();
            if (n <= 0) {
                throw new IOException("invalid number of customers (n <= 0)");
            }

            // Distance matrix n x n
            double[][] distance = new double[n][n];
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (!sc.hasNextDouble()) {
                        throw new IOException("invalid distance matrix");
                    }
                    distance[i][j] = sc.nextDouble();
                }
            }

            boolean isSymmetric = isSymmetric(distance);

            // Time windows: for each i in [0, n) read rtime and ddate
            int[] windowStart = new int[n];
            int[] windowEnd = new int[n];
            for (int i = 0; i < n; i++) {
                if (!sc.hasNextInt()) {
                    throw new IOException("invalid time windows (missing release time)");
                }
                int rtime = sc.nextInt();
                if (!sc.hasNextInt()) {
                    throw new IOException("invalid time windows (missing due date)");
                }
                int ddate = sc.nextInt();
                windowStart[i] = rtime;
                windowEnd[i] = ddate;
            }

            // Build immutable instance (adapt constructor to your model if needed)
            return new TSPTWInstance(
                    suggestedName,
                    n,
                    distance,
                    windowStart,
                    windowEnd,
                    isSymmetric
            );
        }
    }

    private static boolean isSymmetric(double[][] m) {
        int n = m.length;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (!DoubleComparator.equals(m[i][j], m[j][i])) {
                    return false;
                }
            }
        }
        return true;
    }
}