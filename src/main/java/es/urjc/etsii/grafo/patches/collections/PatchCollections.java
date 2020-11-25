package es.urjc.etsii.grafo.patches.collections;

import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.logging.Logger;

@Service
public class PatchCollections {

    private static final Logger log = Logger.getLogger(PatchCollections.class.getName());

    @PostConstruct
    public void patch(){
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
