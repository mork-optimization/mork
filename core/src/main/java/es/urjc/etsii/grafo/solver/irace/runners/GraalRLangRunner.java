package es.urjc.etsii.grafo.solver.irace.runners;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Runs R code inside the JVM. Only works if using GraalVM, fails otherwise.
 */
@Service
@ConditionalOnExpression("!${irace.shell}")
public class GraalRLangRunner extends RLangRunner {
    private static final String R_LANG = "R";
    private static final Logger log = Logger.getLogger(GraalRLangRunner.class.getName());

    /** {@inheritDoc} */
    public void execute(InputStream inputStream){
        try {
            // Load R script
            String script = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

            // Map R outputs to input streams to use them later
            var out = new PipedOutputStream();
            var err = new PipedOutputStream();
            var out_in = new PipedInputStream(out);
            var err_in = new PipedInputStream(err);

            // Build and launch
            Context polyglot = Context.newBuilder(R_LANG)
                    .option("R.PrintErrorStacktracesToFile", "true")
                    .allowAllAccess(true)
                    .out(out)
                    .err(err)
                    .build();
            new Thread(() -> polyglot.eval(R_LANG, script)).start();

            // Send irace output to our logs
            drainStream(Level.INFO, out_in);
            log.info("IRACE Error Stream: ");
            drainStream(Level.WARNING, err_in);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
