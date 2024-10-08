package es.urjc.etsii.grafo.util;

import jnt.scimark2.ScimarkAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;

/**
 * Benchmark helper methods
 */
public class BenchmarkUtil {

    private static final Logger log = LoggerFactory.getLogger(BenchmarkUtil.class);
    private static final String BENCH_FILE = ".benchmark";

    /**
     * Run a small benchmark and return score. Checks if benchmark file
     *
     * @return score as a double
     */
    public static double getBenchmarkScore(long seed) {
        double score = Double.NaN;
        File f = new File(BENCH_FILE);

        if (f.exists()) {
            score = load(f);
        }

        if (Double.isNaN(score)) {
            log.info("Benchmark data not found, running CPU benchmark...");
            var result = ScimarkAPI.runBenchmark((int) seed);
            score = result.getScore();
            store(f, result);
        }

        return score;
    }

    private static void store(File f, ScimarkAPI.BenchmarkResult result) {
        var info = new SystemInfo();

        try (var bw = new BufferedWriter(new FileWriter(f))) {
            bw.write(
                    info.nProcessors() + "\n" +
                            info.vmVersion() + "\n" +
                            info.javaVersion() + "\n" +
                            result.getScore() + "\n"
            );
            Files.setAttribute(f.toPath(), "dos:hidden", true);
        } catch (IOException e) {
            log.error("Failed to create benchmark file", e);
        } catch (UnsupportedOperationException ignore){
            // due to DOS view not available, for example in Mac OS X, just ignore it
        }
    }

    private static double load(File f) {
        var current = new SystemInfo();
        var cached = parseCache(f);
        if (cached == null) {
            return Double.NaN;
        }

        if (!current.equals(cached.info())) {
            log.warn("Benchmark file may not be from this computer, removing it");
            try {
                Files.delete(f.toPath());
            } catch (IOException e){
                log.error("Failed to delete benchmark file", e);
            }
            return Double.NaN;
        }
        return cached.score();
    }

    /**
     * Tries to parse an existing benchmark file
     * @return null if file does not exist or failed to parse the file
     */
    public static BenchmarkCache parseCache() {
        return parseCache(new File(BENCH_FILE));
    }

    /**
     * Tries to parse an existing benchmark file
     * @param f file containing cached benchmark data
     * @return null if file does not exist or failed to parse the file
     */
    public static BenchmarkCache parseCache(File f) {
        if(!f.exists()) return null;

        try (var br = new BufferedReader(new FileReader(f))) {
            int nProcessors = Integer.parseInt(br.readLine());
            String vmVersion = br.readLine();
            String javaVersion = br.readLine();
            double score = Double.parseDouble(br.readLine());
            var info = new SystemInfo(nProcessors, vmVersion, javaVersion);
            return new BenchmarkCache(info, score);
        } catch (IOException e) {
            log.error("Failed to load benchmark data from cached file", e);
            return null;
        }
    }

    /**
     * Benchmark cache
     * @param info system info
     * @param score benchmark score
     */
    public record BenchmarkCache(SystemInfo info, double score) {
    }

    /**
     * System info
     * @param nProcessors number of processors
     * @param vmVersion java virtual machine version
     * @param javaVersion version of the java code, as configured in the compiler
     */
    public record SystemInfo(int nProcessors, String vmVersion, String javaVersion) {
        private SystemInfo() {
            this(Runtime.getRuntime().availableProcessors(), System.getProperty("java.vm.version"), System.getProperty("java.runtime.version"));
        }
    }

}
