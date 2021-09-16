package es.urjc.etsii.grafo.util;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.logging.Logger;

public class IOUtil {
    private static final Logger log = Logger.getLogger(IOUtil.class.getName());

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

    public static boolean isJAR(Class<?> c){
        String className = c.getName().replace('.', '/');
        String protocol = c.getResource("/" + className + ".class").getProtocol();
        return protocol.equals("jar");
    }

    public static InputStream getInputStreamFor(String s, boolean isJar) throws IOException {
        if(isJar){
            return IOUtil.class.getResourceAsStream("/BOOT-INF/classes/irace/" + s);
            //return ResourceUtils.getFile("classpath:irace/" + s).toPath();
        } else {
            return new FileInputStream(new File("src/main/resources/irace/", s));
        }
    }

    public static void markAsExecutable(String s){
        var f = new File(s);
        var success = f.setExecutable(true);
        if(!success){
            log.warning("Failed marking file as executable: "+s);
        }
    }

    public static void copyWithSubstitutions(InputStream origin, Path target, Map<String, String> substitutions) throws IOException {
        String content = new String(origin.readAllBytes(), StandardCharsets.UTF_8);
        for(var e: substitutions.entrySet()){
            content = content.replace(e.getKey(), e.getValue());
        }
        Files.writeString(target, content);
    }
}
