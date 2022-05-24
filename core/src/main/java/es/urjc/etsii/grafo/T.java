package es.urjc.etsii.grafo;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

class T {

    private final static String PATH = "C:\\Users\\rmartin\\Documents\\plagiotest\\";

    public static void main(String[] args) throws IOException {

        var students = Files.list(Path.of(PATH)).map(Path::toFile).filter(File::isDirectory).map(T::parse).toList();
        for (var s1: students){
            for (var s2: students){
                if(s1 == s2) continue;
                for(var f: s1.files){
                    if (s2.files.contains(f)) {
                        String reverseCheck = reverseCheck(s1, s2);
                        System.out.format("[PLAGIO] %s :: %s con %s :: %s%n", s1.path, f, s2.path, reverseCheck);
                    }
                }
            }
        }

        System.out.println("Done");
    }

    public static String reverseCheck(Student s1, Student s2){
        for(var f: s2.files){
            if(s1.files.contains(f)){
                return f.filename;
            }
        }
        throw new IllegalStateException();
    }
    public static Mark sha1(File file) {
        try(InputStream fis = new FileInputStream(file)){
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            int n = 0;
            byte[] buffer = new byte[8192];
            while (n != -1) {
                n = fis.read(buffer);
                if (n > 0) {
                    digest.update(buffer, 0, n);
                }
            }
            var hash = digest.digest();
            return new Mark(file.getName(), hash);
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public static Student parse(File f){
        var hashes = FileUtils.listFiles(f, new String[]{"png", "jpg", "jpeg"}, true).stream().map(T::sha1).collect(Collectors.toSet());
        return new Student(f.getName(), hashes);
    }

    public record Student(String path, Set<Mark> files){
    }

    public static class Mark {
        byte[] hash;
        String filename;

        public Mark(String name, byte[] hash) {
            this.filename = name.replace("_assignsubmission_file_", "");
            this.hash = hash;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Mark mark = (Mark) o;
            return Arrays.equals(hash, mark.hash);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(hash);
        }

        @Override
        public String toString() {
            return this.filename;
        }
    }
}