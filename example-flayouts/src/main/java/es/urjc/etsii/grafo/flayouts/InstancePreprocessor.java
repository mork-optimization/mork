//package es.urjc.etsii.grafo.CAP;
//
//import java.io.File;
//import java.io.IOException;
//import java.nio.file.Files;
//
//import static java.util.Objects.requireNonNull;
//
//public class InstancePreprocessor {
//    public static void main(String[] args) throws IOException {
//        if(args.length !=2){
//            System.err.println("Usage: java -jar preprocessor.jar <inputFolder> <outputFolder>");
//            System.exit(1);
//        }
//        var inputFolder = new File(args[0]);
//        var outputFolder = new File(args[1]);
//        if(!inputFolder.isDirectory()){
//            System.err.println("Input folder does not exist or not a directory");
//            System.exit(1);
//        }
//        if(!outputFolder.exists()){
//            outputFolder.mkdirs();
//        }
//        if(!outputFolder.isDirectory()){
//            System.err.println("Failed to create output folder or not a directory");
//            System.exit(1);
//        }
//        for(var f: requireNonNull(inputFolder.listFiles())){
//            var fileContent = Files.readAllLines(f.toPath());
//            fileContent.add(0, "");
//            for (int i = 2; i <= 5; i++) {
//                String newFilename = f.getName().replace(".txt", "_%s.txt".formatted(i));
//                System.out.printf("Writing %s%n", newFilename);
//                var newFile = new File(outputFolder, newFilename);
//                fileContent.set(0, String.valueOf(i));
//                Files.write(newFile.toPath(), fileContent);
//            }
//        }
//    }
//}
