package es.urjc.etsii.grafo.autoconfig.irace;

import es.urjc.etsii.grafo.autoconfig.r.RLangRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static es.urjc.etsii.grafo.util.IOUtil.getInputStreamForIrace;

/**
 * <p>IraceIntegration class.</p>
 *
 */
@Service
public class IraceIntegration {
    private static final Logger log = LoggerFactory.getLogger(IraceIntegration.class.getName());

    private final RLangRunner runner;

    /**
     * <p>Constructor for IraceIntegration.</p>
     *
     * @param runner a {@link RLangRunner} object.
     */
    public IraceIntegration(RLangRunner runner) {
        log.debug("Using R runner: {}", runner.getClass().getSimpleName());
        this.runner = runner;
    }

    /**
     * <p>runIrace.</p>
     *
     * @param isJAR a boolean.
     */
    public void runIrace(boolean isJAR){
        try {
            var inputStream = getInputStreamForIrace("runner.R", isJAR);
            runner.execute(inputStream);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot load R runner", e);
        }
    }
}
