package io;

import java.io.File;
import java.nio.file.Path;

public interface InstanceLoader {

    Instance loadInstance(File f);

    default Instance loadInstance(String s){
        return loadInstance(Path.of(s));
    }

    default Instance loadInstance(Path p){
        return loadInstance(p.toFile());
    }
}
