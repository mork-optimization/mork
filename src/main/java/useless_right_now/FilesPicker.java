package useless_right_now;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class FilesPicker {
    // Pick one of each N files
    public static void main(String[] args) throws IOException {
        if(args.length != 3){
            System.out.println("Usage: java -jar program.jar folderIn folderOut N");
            System.out.println("- folderIn is the existing folder containing all the instances");
            System.out.println("- folderOut is the folder that will be created, preeliminary instances will be copied here");
            System.out.println("- N is the proportion to be picked, for example, 5 means one of each 5, 1 means pick all");
            System.exit(-1);
        }
        File f = new File(args[0]);
        File outPre = new File(args[1]);
        int NFILES = Integer.parseInt(args[2]);
        if(!outPre.exists()) outPre.mkdirs();
        File[] files = f.listFiles();
        if(files == null){
            System.out.println("Invalid input path, check that folder exists, path is a folder, and we have read permissions");
        }
        String[] outFiles = outPre.list();
        if(outFiles == null){
            System.out.println("Invalid output path.");
        } else if(outFiles.length != 0){
            System.out.println("Non empty folder");
        } else {
            int n = 0;
            Arrays.sort(files);
            for(var file: files){
                if(n % NFILES == 0){
                    Files.copy(file.toPath(), Path.of( outPre.getAbsolutePath()+ '/' + file.getName()));
                }
                n++;
            }
        }

    }

}
