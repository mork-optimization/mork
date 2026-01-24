package es.urjc.etsii.grafo.flayouts.model;

import es.urjc.etsii.grafo.io.InstanceImporter;

import java.io.BufferedReader;
import java.util.Scanner;
import java.util.logging.Logger;

public class FLPInstanceImporter extends InstanceImporter<FLPInstance> {

    Logger log = Logger.getLogger(FLPInstanceImporter.class.getName());

    @Override
    public FLPInstance importInstance(BufferedReader reader, String filename) {
        Scanner sc = new Scanner(reader);
        int n = sc.nextInt();

        Facility[] facilities = new Facility[n];
        for (int i = 0; i < n; i++) {
            int width = sc.nextInt();
            facilities[i] = new Facility(i, width);
        }

        int[][] weights = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                weights[i][j] = sc.nextInt();
            }
        }

        if(sc.hasNextInt()){
            throw new IllegalStateException("Unparsed input at end of file");
        }

        if(!isSimmetric(weights)){
            log.warning(String.format("Non symmetric weight matrix in instance %s, copying data such us w[j][i] = w[i][j] to force symmetry", filename));

            for (int i = 0; i < weights.length; i++) {
                for (int j = 0; j < weights.length; j++) {
                    weights[j][i] = weights[i][j];
                }
            }
        }

        // Match instance filenames with names in previous paper and excel file
        String instanceName = filename.replace(".txt", "").replace("Am", "");
        var instance = new FLPInstance(instanceName, facilities, weights);
        return instance;
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
