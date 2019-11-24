package io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static io.JsonSerializer.ensureValid;

public class Preprocessing {

    private static final Pattern INSTANCE_FOLDER_NAME = Pattern.compile("(.*)Dataset");

    public static void main(String[] args) {
        if(args.length != 2){
            System.out.println("Dos argumentos, ruta instancias sin procesar, ruta salida");
            System.exit(-1);
        }

        var inputFolder = new File(args[0]);
        var outFolder = new File(args[1]);
        ensureValid(outFolder);

        var inputFiles = inputFolder.listFiles();
        if(inputFiles == null){
            throw new IllegalArgumentException("Invalid input path");
        }

        JsonSerializer serializer = new JsonSerializer();

        for (File inputFile : inputFiles) {
            if(inputFile.isDirectory()){
                var matcher = INSTANCE_FOLDER_NAME.matcher(inputFile.getName());
                if(matcher.matches()){
                    String instanceName = matcher.group(1);
                    String base = inputFile + File.separator + instanceName;
                    File distanceMatrix = new File(base + "DistanceMatrix.csv");
                    File entities = new File(base + "Entities.csv");
                    Instance ins = loadRawData(distanceMatrix, entities, instanceName);
                    serializer.saveInstance(ins, String.format("%s%s%s.json", outFolder.getName(), File.separator, instanceName));
                } else {
                    System.out.format("Skipping invalid folder, name '%s' inside instance folder '%s'\n", inputFile.getName(), inputFolder.getName());
                }
            } else {
                System.out.format("Ignoring non folder '%s' inside instance folder '%s'\n", inputFile.getName(), inputFolder.getName());
            }
        }
    }

    // Transform any random format to our format
    private static Instance loadRawData(File distanceMatrix, File entities, String name){
        try (
                BufferedReader matrixReader = new BufferedReader(new FileReader(distanceMatrix));
                BufferedReader entitiesReader = new BufferedReader(new FileReader(entities))
        ) {
            Instance ins = new Instance();
            ins.name = name;

            //load entities data
            String line;
            List<double[]> entitiesData = new ArrayList<>(100);
            int currentIndex = 0;
            int expectedNWeigths = -1;
            entitiesReader.readLine(); // Skip headers line
            while((line = entitiesReader.readLine()) != null){
                var parts = line.split(";");
                int id = Integer.parseInt(parts[0]);
                if(id != currentIndex){
                    throw new RuntimeException(String.format("Id mismatch at line %s, expected %s, got %s", currentIndex+1, currentIndex, id));
                }
                if(expectedNWeigths == -1){
                    expectedNWeigths = parts.length - 1;
                } else if (expectedNWeigths != parts.length -1){
                    throw new RuntimeException(String.format("Weight array size mismatch at line %s, expected %s, got %s", currentIndex + 1, expectedNWeigths, parts.length - 1));
                }
                double[] currentEntity = new double[expectedNWeigths];
                for (int i = 1; i < parts.length; i++) {
                    currentEntity[i - 1] = Double.parseDouble(parts[i]);
                }
                entitiesData.add(currentEntity);
                currentIndex++;
            }
            ins.benefits = entitiesData.toArray(new double[entitiesData.size()][expectedNWeigths]);

            //load matrix data
            int matrixSize = ins.benefits.length;
            ins.distances = new double[matrixSize][matrixSize];
            currentIndex = 0;
            while((line = matrixReader.readLine()) != null) {
                String[] parts = line.split(";");
                if(parts.length != matrixSize){
                    throw new RuntimeException(String.format("Matrix size mismatch at line %s, expected %s, got %s", currentIndex, matrixSize, parts.length));
                }
                for (int i = 0; i < parts.length; i++) {
                    ins.distances[currentIndex][i] = Double.parseDouble(parts[i]);
                }
                currentIndex++;
            }
            if(currentIndex != matrixSize){
                throw new RuntimeException(String.format("Missing data in distance matrix, expected %s lines, got %s", matrixSize, currentIndex));
            }

            return ins;
        } catch (IOException e) {
            // Que lo maneje otro
            throw new RuntimeException(e);
        }
    }
}
