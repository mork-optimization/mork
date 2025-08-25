package es.urjc.etsii.grafo.util;

import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.*;
import java.util.ArrayList;
import java.util.stream.Stream;

public class Compression {

    public static final String SEP = "!"; // Compressed separator, separates filename with internal file entry name

    public abstract static class FileArchiveHandler {
        public abstract InputStream getEntryInputStream(String path, String entry) throws IOException;

        public abstract Stream<String> getFileEntries(String path);
    }

    public static class SevenZHandler extends FileArchiveHandler {
        @Override
        public InputStream getEntryInputStream(String path, String entryName) throws IOException {
            var builder = new SevenZFile.Builder();
            builder.setPath(path);
            var sevenZFile = builder.get();
            for (var e : sevenZFile.getEntries()) {
                if (e.getName().equals(entryName)) {
                    return sevenZFile.getInputStream(e);
                }
            }
            throw new IllegalArgumentException("Entry not found: %s, path: %s".formatted(entryName, path));
        }

        @Override
        public Stream<String> getFileEntries(String path) {
            var builder = new SevenZFile.Builder();
            builder.setPath(path);
            try (var sevenZFile = builder.get()) {
                var entryNames = new ArrayList<String>();
                var entries = sevenZFile.getEntries();
                for (var e : entries) {
                    if (!e.isDirectory()) {
                        entryNames.add(path + SEP + e.getName());
                    }
                }
                return entryNames.stream();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class ZipArchiveHandler extends FileArchiveHandler {
        @Override
        public InputStream getEntryInputStream(String path, String entryName) throws IOException {
            var builder = new ZipFile.Builder();
            builder.setPath(path);
            var zipFile = builder.get();
            var entry = zipFile.getEntry(entryName);
            return zipFile.getInputStream(entry);
        }

        @Override
        public Stream<String> getFileEntries(String path) {
            var builder = new ZipFile.Builder();
            builder.setPath(path);
            try (var zipFile = builder.get()) {
                var entryNames = new ArrayList<String>();
                var entries = zipFile.getEntries();
                while (entries.hasMoreElements()) {
                    var e = entries.nextElement();
                    if (!e.isDirectory()) {
                        entryNames.add(path + SEP + e.getName());
                    }
                }
                return entryNames.stream();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class TarGzArchiveHandler extends FileArchiveHandler {

        @Override
        public InputStream getEntryInputStream(String path, String entryName) throws IOException {
            var tarFile = getTarInputStream(path);
            TarArchiveEntry entry;
            while ((entry = tarFile.getNextEntry()) != null){
                if (entry.getName().equals(entryName)) {
                    int size = Math.toIntExact(entry.getSize());
                    byte[] data = new byte[size];
                    int bytesRead = tarFile.read(data);
                    if (bytesRead != size) {
                        throw new IOException("Entry size does not match expected size");
                    }
                    return new ByteArrayInputStream(data);
                }
            }

            throw new IOException("Entry not found: %s, path: %s".formatted(entryName, path));
        }

        private static TarArchiveInputStream getTarInputStream(String path) throws IOException {
            InputStream input = new FileInputStream(path);
            if(path.endsWith("gz")){
                input = new GzipCompressorInputStream(input);
            }
            var tarFile = new TarArchiveInputStream(input);
            return tarFile;
        }

        public boolean isMetadata(TarArchiveEntry entry) {
            var name = entry.getName();
            if(name.contains("/")){
                name = name.substring(name.lastIndexOf('/')+1);
            }
            return name.startsWith("._") || name.equals(".DS_Store");
        }

        @Override
        public Stream<String> getFileEntries(String path) {
            try (var tarFile = getTarInputStream(path)) {
                var entryNames = new ArrayList<String>();
                TarArchiveEntry entry;
                while ((entry = tarFile.getNextEntry()) != null){
                    if (!entry.isDirectory() && !isMetadata(entry)) {
                        entryNames.add(path + SEP + entry.getName());
                    }
                }
                return entryNames.stream();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
