package es.urjc.etsii.grafo.util;

import java.io.File;

public class IOUtil {
    public static void createIfNotExists(String path) {
        File dir = new File(path);
        dir.mkdir();
        errorIfNotExists(path);
    }

    public static void errorIfNotExists(String path){
        File dir = new File(path);
        if(!dir.isDirectory()){
            throw new IllegalArgumentException("Path does not exist or not a folder: " + dir.getAbsolutePath());
        }
    }
}
