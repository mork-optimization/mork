package es.urjc.etsii.grafo.util;

import java.io.File;

public class IOUtil {

    public static void createFolder(String path) {
        File dir = new File(path);
        dir.mkdir();
        checkIsFolder(path);
    }

    public static void checkExists(String path){
        File dir = new File(path);
        if(!dir.exists()){
            throw new IllegalArgumentException("Path does not exist or not a folder: " + dir.getAbsolutePath());
        }
    }

    public static void checkIsFolder(String path){
        File dir = new File(path);
        if(!dir.isDirectory()){
            throw new IllegalArgumentException("Path does not exist or not a folder: " + dir.getAbsolutePath());
        }
    }
}
