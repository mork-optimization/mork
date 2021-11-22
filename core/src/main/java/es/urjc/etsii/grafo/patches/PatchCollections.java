package es.urjc.etsii.grafo.patches;

import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.logging.Logger;

/**
 * Block calls to Collections.shuffle() as it uses an internal random that breaks experiment reproducibility
 * Use CollectionUtil.shuffle() instead
 */
@Service
public class PatchCollections {

    private static final Logger log = Logger.getLogger(PatchCollections.class.getName());

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
        } catch (NoSuchFieldException | IllegalAccessException e) {
            log.warning("Failed to patch Collections.shuffle()");
            throw new RuntimeException(e);
        }
        log.info("Collections.shuffle() patched successfully");
    }
}
