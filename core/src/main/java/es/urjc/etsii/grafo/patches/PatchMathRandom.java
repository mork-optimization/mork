package es.urjc.etsii.grafo.patches;

import es.urjc.etsii.grafo.config.BlockConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.Modifier;

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
            log.info("Skipping Math.random() patch");
            return;
        }

        try {
            var internalClass = Class.forName("java.lang.Math$RandomNumberGeneratorHolder");
            var internalRandom = internalClass.getDeclaredField("randomNumberGenerator");
            internalRandom.setAccessible(true);
            makeNonFinal(internalRandom);
            internalRandom.set(null, new FailRandom());
            log.info("Math.random() patched successfully");
        } catch (NoSuchFieldException | IllegalAccessException | ClassNotFoundException | InaccessibleObjectException e) {
            // Log as warning, but do not stop application when failing to patch, it is not critical
            log.warn("Failed to patch Math.random(), probably due to missing opens, see: https://mork-optimization.readthedocs.io/en/latest/quickstart/troubleshooting/. Cause: {}", e.getMessage());
        }
    }

    private void makeNonFinal(Field field) throws NoSuchFieldException, IllegalAccessException {
        var lookup = MethodHandles.privateLookupIn(Field.class, MethodHandles.lookup());
        VarHandle modifiers = lookup.findVarHandle(Field.class, "modifiers", int.class);
        int mods = field.getModifiers();
        if (Modifier.isFinal(mods)) {
            modifiers.set(field, mods & ~Modifier.FINAL);
        }
    }
}
