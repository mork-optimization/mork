package io;

import solution.Result;

import java.io.File;
import java.nio.file.Path;

public interface ResultsExporter {
    void saveResult(Result s, File f);

    default void saveResult(Result s, Path p){
        saveResult(s, p.toFile());
    }

    default void saveResult(Result s, String path){
        saveResult(s, Path.of(path));
    }
}
