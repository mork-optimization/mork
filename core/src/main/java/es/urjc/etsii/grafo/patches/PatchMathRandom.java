package es.urjc.etsii.grafo.patches;

import es.urjc.etsii.grafo.config.BlockConfig;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.objenesis.instantiator.util.UnsafeUtils;
import org.springframework.stereotype.Service;

import java.lang.reflect.InaccessibleObjectException;

/**
 * Block calls to Math.random() as it uses an internal random that breaks experiment reproducibility
 * Use RandomManager.getRandom().nextDouble() instead.
 */
@Service
public class PatchMathRandom {

    private static final Logger log = LoggerFactory.getLogger(PatchMathRandom.class);

    private final boolean isEnabled;

    /**
     * Initialize patch with config
     *
     * @param config configuration
     */
    public PatchMathRandom(BlockConfig config){
        this.isEnabled = config.isBlockMathRandom();
    }

    /**
     * Execute patch
     */
    @PostConstruct
    public void patch(){
        if(!isEnabled){
            log.debug("Skipping Math.random() patch");
            return;
        }

        try {
            var unsafe = UnsafeUtils.getUnsafe();

            var internalRandomClass = Class.forName("java.lang.Math$RandomNumberGeneratorHolder");
            var internalRandomField = internalRandomClass.getDeclaredField("randomNumberGenerator");
            var base = unsafe.staticFieldBase(internalRandomField);
            var offset = unsafe.staticFieldOffset(internalRandomField);
            unsafe.putObject(base, offset, new FailRandom());
            log.debug("Math.random() patched successfully");
        } catch (NoSuchFieldException | ClassNotFoundException | InaccessibleObjectException | UnsupportedOperationException e) {
            // Log as warning, but do not stop application when failing to patch, it is not critical
            log.warn("Failed to patch Math.random(), probably due to missing opens, see: https://mork-optimization.readthedocs.io/en/latest/quickstart/troubleshooting/. Cause: {}", e.getMessage());
        }
    }
}
