package es.urjc.etsii.grafo.autoconfig.r;

import es.urjc.etsii.grafo.util.IOUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
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

            // Launch Rscript executable in a new process
            ProcessBuilder pb = new ProcessBuilder("Rscript", "runner.R");
            pb.redirectErrorStream(true);
            Process p = pb.start();

            // Send irace output to our logs in real time
            var containsErrors = drainStream(Level.INFO, p.getInputStream());

            // Wait until Rscript process terminates
            var exitCode = p.waitFor();
            log.info("Rscript process terminated with code: " + exitCode);
            if(exitCode != 0 || containsErrors){
                throw new RuntimeException("Error executing R code, review logs. Exit code: " + exitCode);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
