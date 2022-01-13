package es.urjc.etsii.grafo.patches;

import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.lang.reflect.InaccessibleObjectException;
import java.util.Collections;
import java.util.logging.Level;
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
        } catch (NoSuchFieldException | InaccessibleObjectException | IllegalAccessException e){
            // Log as warning, but do not stop application when failing to patch, it is not critical
            log.log(Level.WARNING, "Failed to patch Collections.shuffle(), internal random is not accessible. Probably missing opens, see: https://mork-optimization.readthedocs.io/en/latest/quickstart/troubleshooting/", e);
        }
        log.info("Collections.shuffle() patched successfully");
    }
}
