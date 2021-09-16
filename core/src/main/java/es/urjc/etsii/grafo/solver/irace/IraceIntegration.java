package es.urjc.etsii.grafo.solver.irace;

import es.urjc.etsii.grafo.solver.irace.runners.RLangRunner;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

import static es.urjc.etsii.grafo.util.IOUtil.getInputStreamFor;

@Service
public class IraceIntegration {
    private static final Logger log = Logger.getLogger(IraceIntegration.class.getName());

    private final RLangRunner runner;

    public IraceIntegration(RLangRunner runner) {
        log.info("Using R runner: " + runner.getClass().getSimpleName());
        this.runner = runner;
    }

    public void runIrace(boolean isJAR){
        try {
            var inputStream = getInputStreamFor("runner.R", isJAR);
            String script = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            runner.execute(script);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot load R runner", e);
        }
    }
}
