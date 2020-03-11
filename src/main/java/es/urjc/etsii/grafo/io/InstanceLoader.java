//package es.urjc.etsii.grafo.io;
//
//import java.io.File;
//import java.nio.file.Path;
//
//interface InstanceLoader<I extends Instance> {
//
//    I loadInstance(File f, Class<I> type);
//
//    default I loadInstance(String s, Class<I> type){
//        return loadInstance(Path.of(s), type);
//    }
//
//    default I loadInstance(Path p, Class<I> type){
//        return loadInstance(p.toFile(), type);
//    }
//}
