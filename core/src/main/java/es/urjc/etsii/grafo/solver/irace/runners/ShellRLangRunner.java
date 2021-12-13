package es.urjc.etsii.grafo.solver.irace.runners;

import es.urjc.etsii.grafo.util.IOUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Path;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Runs R code outside the JVM using the R executable. Only works if R is installed and available in the path.
 */
@Service
@ConditionalOnExpression("${irace.shell}")
public class ShellRLangRunner extends RLangRunner {

    private static final Logger log = Logger.getLogger(RLangRunner.class.getName());

    /** {@inheritDoc} */
    public void execute(InputStream rCode){
        try {
            // No substitutions for this file, only extract
            IOUtil.copyWithSubstitutions(rCode, Path.of("runner.R"), Map.of());

            ProcessBuilder pb = new ProcessBuilder("Rscript", "runner.R");
            Process p = pb.start();
            drainStream(Level.INFO, p.getInputStream());
            log.info("IRACE Error Stream: ");
            drainStream(Level.WARNING, p.getErrorStream());
            p.waitFor();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
