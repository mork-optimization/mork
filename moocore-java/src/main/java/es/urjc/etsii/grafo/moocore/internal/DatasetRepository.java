// SPDX-License-Identifier: MPL-2.0
package es.urjc.etsii.grafo.moocore.internal;

import es.urjc.etsii.grafo.moocore.Dataset;
import org.tukaani.xz.XZInputStream;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class DatasetRepository {

    private static final String RESOURCE_ROOT = "es/urjc/etsii/grafo/moocore/data/";
    private static final String BASE_URL =
            "https://github.com/multi-objective/testsuite/raw/refs/heads/main/data/";
    private static final Map<String, String> PACKAGED_CHECKSUMS = Map.of(
            "input1.dat", "dbf36ad60f01cb559153d3bb8548c0ac053e56e8da1be08bbecc79ad1f37da1d",
            "ran.10pts.9d.10", "d1af38ee6398b499b28a743e5fe4de1fe247c20ccade644b223b961d89891ed6");
    private static final Map<String, String> REMOTE_CHECKSUMS = remoteChecksums();

    private DatasetRepository() {
    }

    public static Dataset read(Path path) throws IOException {
        if (path == null || !Files.isRegularFile(path)) {
            throw new IOException("dataset file does not exist: " + path);
        }
        try (InputStream input = Files.newInputStream(path)) {
            return read(path.getFileName().toString().endsWith(".xz") ? new XZInputStream(input) : input);
        }
    }

    public static Dataset read(InputStream input) throws IOException {
        if (input == null) {
            throw new IllegalArgumentException("input cannot be null");
        }
        List<double[]> points = new ArrayList<>();
        List<Integer> sets = new ArrayList<>();
        int currentSet = 1;
        int objectives = -1;
        boolean setHasPoints = false;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                int comment = line.indexOf('#');
                if (comment >= 0) {
                    line = line.substring(0, comment);
                }
                line = line.trim();
                if (line.isEmpty()) {
                    if (setHasPoints) {
                        currentSet++;
                        setHasPoints = false;
                    }
                    continue;
                }
                String[] columns = line.split("\\s+");
                if (objectives < 0) {
                    objectives = columns.length;
                } else if (columns.length != objectives) {
                    throw new IOException("inconsistent number of objectives in dataset");
                }
                double[] point = new double[objectives];
                try {
                    for (int i = 0; i < objectives; i++) {
                        point[i] = Double.parseDouble(columns[i]);
                    }
                } catch (NumberFormatException exception) {
                    throw new IOException("invalid numeric value in dataset: " + line, exception);
                }
                points.add(point);
                sets.add(currentSet);
                setHasPoints = true;
            }
        }
        if (points.isEmpty()) {
            throw new IOException("dataset is empty");
        }
        int[] setArray = new int[sets.size()];
        for (int i = 0; i < sets.size(); i++) {
            setArray[i] = sets.get(i);
        }
        return new Dataset(points.toArray(double[][]::new), setArray);
    }

    public static Dataset get(String name) throws IOException {
        return read(getPath(name, false, 3, Duration.ofSeconds(1)));
    }

    public static Path getPath(String name, boolean force, int retries, Duration delay) throws IOException {
        if (name == null || name.isBlank() || name.contains("/") || name.contains("\\")) {
            throw new IllegalArgumentException("name must be a plain dataset filename");
        }
        if (retries < 0 || delay == null || delay.isNegative()) {
            throw new IllegalArgumentException("retries and delay must be nonnegative");
        }
        Path cache = cacheDirectory();
        Files.createDirectories(cache);
        Path target = cache.resolve(name);

        InputStream packaged = DatasetRepository.class.getClassLoader().getResourceAsStream(RESOURCE_ROOT + name);
        boolean encoded = false;
        if (packaged == null) {
            packaged = DatasetRepository.class.getClassLoader().getResourceAsStream(RESOURCE_ROOT + name + ".b64");
            encoded = packaged != null;
        }
        try (InputStream resource = packaged) {
            if (resource != null) {
                String checksum = PACKAGED_CHECKSUMS.get(name);
                if (checksum == null) {
                    throw new IllegalStateException("packaged dataset has no checksum: " + name);
                }
                if (force || !Files.isRegularFile(target) || !checksum.equals(sha256(target))) {
                    Path temporary = Files.createTempFile(cache, name + ".part-", ".tmp");
                    try {
                        InputStream source = encoded ? Base64.getMimeDecoder().wrap(resource) : resource;
                        Files.copy(source, temporary, StandardCopyOption.REPLACE_EXISTING);
                        if (!checksum.equals(sha256(temporary))) {
                            throw new IOException("packaged dataset has an unexpected SHA-256 checksum");
                        }
                        atomicReplace(temporary, target);
                    } finally {
                        Files.deleteIfExists(temporary);
                    }
                }
                return target;
            }
        }

        String checksum = REMOTE_CHECKSUMS.get(name);
        if (checksum == null) {
            throw new IllegalArgumentException("unknown dataset: " + name);
        }
        if (!force && Files.isRegularFile(target) && checksum.equals(sha256(target))) {
            return target;
        }
        download(name, target, checksum, retries, delay);
        return target;
    }

    private static void download(String name, Path target, String checksum, int retries,
                                 Duration delay) throws IOException {
        Path temporary = Files.createTempFile(target.getParent(), name + ".part-", ".tmp");
        HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE_URL + name)).GET().build();
        try {
            IOException failure = null;
            for (int attempt = 0; attempt <= retries; attempt++) {
                try {
                    HttpResponse<Path> response = client.send(request,
                            HttpResponse.BodyHandlers.ofFile(temporary));
                    if (response.statusCode() < 200 || response.statusCode() >= 300) {
                        throw new IOException("dataset download failed with HTTP " + response.statusCode());
                    }
                    if (!checksum.equals(sha256(temporary))) {
                        throw new IOException("downloaded dataset has an unexpected SHA-256 checksum");
                    }
                    atomicReplace(temporary, target);
                    return;
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    throw new IOException("interrupted while downloading dataset", exception);
                } catch (IOException exception) {
                    failure = exception;
                    if (attempt < retries && !delay.isZero()) {
                        try {
                            Thread.sleep(delay);
                        } catch (InterruptedException interrupted) {
                            Thread.currentThread().interrupt();
                            throw new IOException("interrupted while retrying dataset download", interrupted);
                        }
                    }
                }
            }
            throw failure;
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private static Path cacheDirectory() {
        String configured = System.getProperty("moocore.cache");
        if (configured != null && !configured.isBlank()) {
            return Path.of(configured);
        }
        String xdgCache = System.getenv("XDG_CACHE_HOME");
        Path base = xdgCache == null || xdgCache.isBlank()
                ? Path.of(System.getProperty("user.home"), ".cache")
                : Path.of(xdgCache);
        return base.resolve("mork-moocore").resolve("0.23");
    }

    private static String sha256(Path path) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream input = new BufferedInputStream(Files.newInputStream(path))) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = input.read(buffer)) >= 0) {
                    digest.update(buffer, 0, read);
                }
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private static void atomicReplace(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static Map<String, String> remoteChecksums() {
        Map<String, String> checksums = new LinkedHashMap<>();
        checksums.put("ALG_1_dat.xz", "a51165fe69b356c45e5bb052c747e7dcb97432975b5e7a632fc94f0b59620046");
        checksums.put("DTLZLinearShape.8d.front.60pts.10", "ad1f23a6785025d13f364b925c8371389fbc42aaa5deba2ee7325330cb3528df");
        checksums.put("ran.1000pts.3d.10", "45daca9bd1a6fd16cc4032853118713c3c05a42438642a74d2e85c074d279a62");
        checksums.put("test2D-200k.inp.xz", "5e211ea67d228512bd27521fae63856c74694d0aa61fe077d5d37181623671ad");
        checksums.put("ran.40000pts.3d.1.xz", "69afefc93eb8e14f355a48b5e2dacc4f43d7fd01dfa7b4b4b3a35366a0504eba");
        checksums.put("DTLZLinearShape.4d.front.1000pts.10", "eedfd7c9db8632133301705aaa1c68b8e5e605f219fac44abd78521cd98f5e80");
        checksums.put("rmnk_0.0_10_16_1_0_ref.txt.xz", "f9bb1f5f40b09c86d303edbcb70d26d90a17b616530b9c662a848e4095b4eac4");
        return Map.copyOf(checksums);
    }
}
