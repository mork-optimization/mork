package es.urjc.etsii.grafo.patches;

import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.logging.Logger;

@Service
public class PatchMathRandom {

    private static final Logger log = Logger.getLogger(PatchMathRandom.class.getName());

    private final boolean isEnabled;

    public PatchMathRandom(BlockConfig config){
        this.isEnabled = config.isBlockMathRandom();
    }

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
        } catch (NoSuchFieldException | IllegalAccessException | ClassNotFoundException e) {
            log.warning("Failed to patch Math.random()");
            throw new RuntimeException(e);
        }
        log.info("Math.random() patched successfully");
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
