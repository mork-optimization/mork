package es.urjc.etsii.grafo.solver.irace;

import es.urjc.etsii.grafo.solver.irace.runners.RLangRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.logging.Logger;

import static es.urjc.etsii.grafo.util.IOUtil.getInputStreamFor;

/**
 * <p>IraceIntegration class.</p>
 *
 */
@Service
@ConditionalOnExpression(value = "${irace.enabled}")
public class IraceIntegration {
    private static final Logger log = Logger.getLogger(IraceIntegration.class.getName());

    private final RLangRunner runner;

    /**
     * <p>Constructor for IraceIntegration.</p>
     *
     * @param runner a {@link es.urjc.etsii.grafo.solver.irace.runners.RLangRunner} object.
     */
    public IraceIntegration(RLangRunner runner) {
        log.info("Using R runner: " + runner.getClass().getSimpleName());
        this.runner = runner;
    }

    /**
     * <p>runIrace.</p>
     *
     * @param isJAR a boolean.
     */
    public void runIrace(boolean isJAR){
        try {
            var inputStream = getInputStreamFor("runner.R", isJAR);
            runner.execute(inputStream);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot load R runner", e);
        }
    }
}
