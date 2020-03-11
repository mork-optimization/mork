//package es.urjc.etsii.grafo.io;
//
//import java.io.File;
//import java.nio.file.Path;
//
//interface ResultsExporter {
//    void saveResult(Result s, File f);
//
//    default void saveResult(Result s, Path p){
//        saveResult(s, p.toFile());
//    }
//
//    default void saveResult(Result s, String path){
//        saveResult(s, Path.of(path));
//    }
//}
