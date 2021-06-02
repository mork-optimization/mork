package es.urjc.etsii.grafo.patches;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.logging.Logger;

@Service
public class PatchCollections {

    private static final Logger log = Logger.getLogger(PatchCollections.class.getName());

    private final boolean isEnabled;

    public PatchCollections(@Value("${advanced.block.collections-shuffle}") boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

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
