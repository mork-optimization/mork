package es.urjc.etsii.grafo.util;


import org.apache.commons.io.FilenameUtils;

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
import java.util.stream.Stream;

import static org.apache.commons.compress.archivers.ArchiveStreamFactory.SEVEN_Z;
import static org.apache.commons.compress.archivers.ArchiveStreamFactory.ZIP;

/**
 * Util methods for managing input/output
 */
public class IOUtil {
    private static final Logger log = Logger.getLogger(IOUtil.class.getName());

    private static final Map<String, Compression.FileArchiveHandler> SUPPORTED_ARCHIVES = Map.of(
            ZIP, new Compression.ZipArchiveHandler(),
            SEVEN_Z, new Compression.SevenZHandler()
    );

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
    public static String checkExists(String path) {
        File file = new File(path);
        if (!file.exists()) {
            throw new IllegalArgumentException("Path does not exist: " + file.getAbsolutePath());
        }
        return path;
    }

    /**
     * Check that the given path is a folder
     *
     * @param path path to check
     */
    public static void checkIsFolder(String path) {
        File dir = new File(path);
        if (!dir.isDirectory()) {
            throw new IllegalArgumentException("Path does not exist or not a folder: " + dir.getAbsolutePath());
        }
    }

    /**
     * Check if the given class is in a JAR file
     *
     * @param c class to check
     * @return true if inside a JAR, false otherwise
     */
    public static boolean isJAR(Class<?> c) {
        String className = c.getName().replace('.', '/');
        String protocol = c.getResource("/" + className + ".class").getProtocol();
        return protocol.equals("jar");
    }

    /**
     * Get input stream for the given path
     *
     * @param s     path to resource
     * @param isJar true if the resource is inside a JAR file, false otherwise
     * @return InputStream to the resource given as a parameter
     * @throws java.io.IOException if anything goes wrong
     */
    public static InputStream getInputStreamForIrace(String s, boolean isJar) throws IOException {
        if (isJar) {
            return IOUtil.class.getResourceAsStream("/BOOT-INF/classes/irace/" + s);
            //return ResourceUtils.getFile("classpath:irace/" + s).toPath();
        } else {
            return new FileInputStream(new File("src/main/resources/irace/", s));
        }
    }

    /**
     * Get input stream for the given path
     *
     * @param path   path to resource
     * @param target where to copy the resource
     * @param isJar  true if the resource is inside a JAR file, false otherwise
     */
    public static void extractResource(String path, String target, boolean isJar, boolean autodelete) {
        Path pTarget;
        try (var inStream = isJar ?
                IOUtil.class.getResourceAsStream("/BOOT-INF/classes/" + path) :
                Files.newInputStream(new File("src/main/resources/", path).toPath())) {
            pTarget = Path.of(target);
            Files.write(pTarget, inStream.readAllBytes());
        } catch (IOException e){
            throw new RuntimeException(e);
        }
        if (autodelete) {
            pTarget.toFile().deleteOnExit();
        }
    }


    /**
     * Mark a file as executable
     *
     * @param s path to file as string
     */
    public static void markAsExecutable(String s) {
        var f = new File(s);
        var success = f.setExecutable(true);
        if (!success) {
            log.warning("Failed marking file as executable: " + s);
        }
    }

    /**
     * Replace substitutions in the given input stream
     *
     * @param origin        input stream where the data is read from
     * @param target        where should data be written to
     * @param substitutions list of substitutions to do while copying the adta
     * @throws java.io.IOException if anything goes wrong
     */
    public static void copyWithSubstitutions(InputStream origin, Path target, Map<String, String> substitutions) throws IOException {
        String content = new String(origin.readAllBytes(), StandardCharsets.UTF_8);
        for (var e : substitutions.entrySet()) {
            content = content.replace(e.getKey(), e.getValue());
        }
        Files.writeString(target, content);
    }

    /**
     * Returns true if it is a file (not a folder) and the file is not hidden and its name does not start with a dot.
     *
     * @param p Path to check
     * @return true if passes all checks, false otherwise
     */
    public static boolean isNormalFile(Path p) {
        try {
            if (Files.isHidden(p)) {
                return false;
            }
            if (p.toFile().getName().startsWith(".")) {
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
     *
     * @param path Path to iterate
     * @return list of paths to normal files
     */
    public static List<String> iterate(Path path) {
        try (var stream = Files.walk(path)) {
            return stream
                    .filter(IOUtil::isNormalFile)
                    .map(Path::toAbsolutePath)
                    .flatMap(IOUtil::expandIfContainer)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * List all normal files under a given path. Ignores hidden files. Recursively explores folders.
     *
     * @param path Path to iterate
     * @return list of paths to normal files
     */
    public static List<String> iterate(String path) {
        return iterate(Path.of(path));
    }

    /**
     * Check if the file represented by the given path is a compressed file, and if so, return list of files inside the archive.
     *
     * @param p Path to check
     * @return list of files inside the archive, or the path itself if it is not a compressed file
     */
    public static Stream<String> expandIfContainer(Path p) {
        String path = p.toAbsolutePath().toString();
        if (path.contains(Compression.SEP)) {
            throw new IllegalArgumentException("Path cannot not contain the compressed separator %s: %s".formatted(Compression.SEP, path));
        }

        if (Files.isDirectory(p)) {
            throw new IllegalArgumentException("expandIfContainer should only be called with files, not directories");
        }

        var fileExtension = FilenameUtils.getExtension(path);
        var handler = SUPPORTED_ARCHIVES.get(fileExtension);
        if (handler == null) {
            // Not a known compressed filetype, return file path as is
            return Stream.of(p.toString());
        }
        // else, it is a compressed file we know how to handle, return list of files inside the archive
        return handler.getFileEntries(path);
    }

    public static InputStream getInputStream(String path) throws IOException {
        if (!path.contains(Compression.SEP)) {
            // Not a compressed file, just return the input stream
            return Files.newInputStream(Path.of(path));
        }
        // Compressed file, find the entry and return the input stream
        var split = path.split(Compression.SEP);
        var archivePath = split[0];
        var entryPath = split[1];
        var fileExtension = FilenameUtils.getExtension(archivePath);
        var archiver = SUPPORTED_ARCHIVES.get(fileExtension);
        if (archiver == null) {
            throw new IllegalArgumentException("Path is compressed, but the file extension is not recognized: " + archivePath);
        }

        return archiver.getEntryInputStream(archivePath, entryPath);
    }

    public static String relativizePath(String path){
        try {
            String currentPath =  new File(".").getCanonicalPath();
            if (path.startsWith(currentPath)) {
                return path.substring(currentPath.length() + 1);
            } else {
                return path;
            }
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }
}
