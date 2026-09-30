package es.urjc.etsii.grafo.flayouts.model;

import es.urjc.etsii.grafo.io.InstanceImporter;

import java.io.BufferedReader;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Logger;

public class FLPInstanceImporter extends InstanceImporter<FLPInstance> {

    Logger log = Logger.getLogger(FLPInstanceImporter.class.getName());

    @Override
    public FLPInstance importInstance(BufferedReader reader, String filename) {
        Scanner sc = new Scanner(reader);
        int nRows = sc.nextInt();
        if(nRows != 2){
            throw new IllegalArgumentException("This example currently expects space-free layouts with 2 rows");
        }
        int n = sc.nextInt();

        int[] lengths = new int[n];
        for (int i = 0; i < n; i++) {
            lengths[i] = sc.nextInt();
        }

        int[][] flow = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                flow[i][j] = sc.nextInt();
            }
        }

        if(sc.hasNextInt()){
            throw new IllegalStateException("Unparsed input at end of file");
        }

        if(!isSimmetric(flow)){
            log.warning(String.format("Non symmetric weight matrix in instance %s, copying data such us w[j][i] = w[i][j] to force symmetry", filename));

            for (int i = 0; i < flow.length; i++) {
                for (int j = 0; j < flow.length; j++) {
                    flow[j][i] = flow[i][j];
                }
            }
        }

        String instanceName = filename.replace(".txt", "");
        return new FLPInstance(instanceName, lengths, flow, nRows, Map.of());
    }

    private boolean isSimmetric(int[][] matrix){
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix.length; j++) {
                if(matrix[i][j] != matrix[j][i]){
                    return false;
                }
            }
        }
        return true;
    }
}
