package es.urjc.etsii.grafo.patches;

import es.urjc.etsii.grafo.config.BlockConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.lang.reflect.InaccessibleObjectException;
import java.util.Collections;

/**
 * Block calls to Collections.shuffle() as it uses an internal random that breaks experiment reproducibility
 * Use CollectionUtil.shuffle() instead
 */
@Service
public class PatchCollections {

    private static final Logger log = LoggerFactory.getLogger(PatchCollections.class);

    private final boolean isEnabled;

    /**
     * Initialize patch with config
     *
     * @param config configuration
     */
    public PatchCollections(BlockConfig config) {
        this.isEnabled = config.isBlockCollectionsShuffle();
    }

    /**
     * Execute patch
     */
    @PostConstruct
    public void patch(){
        if(!isEnabled){
            log.info("Skipping Collections.shuffle() patch");
            return;
        }

        try {
            var internalRandom = Collections.class.getDeclaredField("r");
            internalRandom.setAccessible(true);
            internalRandom.set(null, new FailRandom());
            log.info("Collections.shuffle() patched successfully");
        } catch (NoSuchFieldException | InaccessibleObjectException | IllegalAccessException | UnsupportedOperationException e){
            // Log as warning, but do not stop application when failing to patch, it is not critical
            log.warn("Failed to patch Collections.shuffle(), probably due to missing opens, see: https://mork-optimization.readthedocs.io/en/latest/quickstart/troubleshooting/. Cause: {}", e.getMessage());
        }
    }
}
