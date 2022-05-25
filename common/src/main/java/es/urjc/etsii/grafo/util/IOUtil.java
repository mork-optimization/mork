package es.urjc.etsii.grafo.util;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Util methods for managing input/output
 */
public class IOUtil {
    private static final Logger log = Logger.getLogger(IOUtil.class.getName());

    /**
     * Create folder if not exists, recursively
     *
     * @param path a {@link java.lang.String} object.
     */
    public static void createFolder(String path) {
        File dir = new File(path);
        boolean created = dir.mkdirs();
        checkIsFolder(path);
    }

    /**
     * Verify that the given path exists
     *
     * @param path path to check
     */
    public static void checkExists(String path){
        File dir = new File(path);
        if(!dir.exists()){
            throw new IllegalArgumentException("Path does not exist or not a folder: " + dir.getAbsolutePath());
        }
    }

    /**
     * Check that the given path is a folder
     *
     * @param path path to check
     */
    public static void checkIsFolder(String path){
        File dir = new File(path);
        if(!dir.isDirectory()){
            throw new IllegalArgumentException("Path does not exist or not a folder: " + dir.getAbsolutePath());
        }
    }

    /**
     * Check if the given class is in a JAR file
     *
     * @param c class to check
     * @return true if inside a JAR, false otherwise
     */
    public static boolean isJAR(Class<?> c){
        String className = c.getName().replace('.', '/');
        String protocol = c.getResource("/" + className + ".class").getProtocol();
        return protocol.equals("jar");
    }

    /**
     * Get input stream for the given path
     *
     * @param s path to resource
     * @param isJar true if the resource is inside a JAR file, false otherwise
     * @return InputStream to the resource given as a parameter
     * @throws java.io.IOException if anything goes wrong
     */
    public static InputStream getInputStreamFor(String s, boolean isJar) throws IOException {
        if(isJar){
            return IOUtil.class.getResourceAsStream("/BOOT-INF/classes/irace/" + s);
            //return ResourceUtils.getFile("classpath:irace/" + s).toPath();
        } else {
            return new FileInputStream(new File("src/main/resources/irace/", s));
        }
    }

    /**
     * Mark a file as executable
     *
     * @param s path to file as string
     */
    public static void markAsExecutable(String s){
        var f = new File(s);
        var success = f.setExecutable(true);
        if(!success){
            log.warning("Failed marking file as executable: "+s);
        }
    }

    /**
     * Replace substitutions in the given input stream
     *
     * @param origin input stream where the data is read from
     * @param target where should data be written to
     * @param substitutions list of substitutions to do while copying the adta
     * @throws java.io.IOException if anything goes wrong
     */
    public static void copyWithSubstitutions(InputStream origin, Path target, Map<String, String> substitutions) throws IOException {
        String content = new String(origin.readAllBytes(), StandardCharsets.UTF_8);
        for(var e: substitutions.entrySet()){
            content = content.replace(e.getKey(), e.getValue());
        }
        Files.writeString(target, content);
    }

    /**
     * Returns true if it is a file (not a folder) and the file is not hidden and its name does not start with a dot.
     * @param p Path to check
     * @return true if passes all checks, false otherwise
     */
    public static boolean isNormalFile(Path p){
        try {
            if(Files.isHidden(p)) {
                return false;
            }
            if(p.toFile().getName().startsWith(".")){
                // In Windows files starting with . are not considered hidden, but they are in Linux, so ignore them.
                return false;
            }
        } catch (IOException e) {
            log.warning("Error while reading file attributes, skipping instance file: " + p.toAbsolutePath());
            return false;
        }
        return Files.isRegularFile(p);
    }

    /**
     * List all normal files under a given path. Ignores hidden files. Recursively explores folders.
     * @param path Path to iterate
     * @return list of paths to normal files
     */
    public static List<Path> iterate(Path path) {
        try(var stream = Files.walk(path)) {
            return stream
                    .filter(IOUtil::isNormalFile)
                    .collect(Collectors.toList());
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    /**
     * List all normal files under a given path. Ignores hidden files. Recursively explores folders.
     * @param path Path to iterate
     * @return list of paths to normal files
     */
    public static List<Path> iterate(String path){
        return iterate(Path.of(path));
    }
}
