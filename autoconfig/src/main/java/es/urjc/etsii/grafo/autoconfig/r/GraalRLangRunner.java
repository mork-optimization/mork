package es.urjc.etsii.grafo.autoconfig.r;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
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
            var out_in = new PipedInputStream(out);

            // Build and launch
            var contextBuilder = Context.newBuilder(R_LANG)
                    .option("R.PrintErrorStacktracesToFile", "true")
                    .allowAllAccess(true)
                    .out(out)
                    .err(out);

            // Execute R script inside the JVM in a new thread
            new Thread(() -> {
                try (Context polyglot = contextBuilder.build()){
                    polyglot.eval(R_LANG, script);
                } catch (PolyglotException e){
                    log.log(Level.SEVERE, "Error executing R code", e);
                }
            }).start();

            // Send irace output to our logs in real time
            var containsErrors = drainStream(Level.INFO, out_in);
            if(containsErrors){
                throw new RuntimeException("Error executing R code, review logs");
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
