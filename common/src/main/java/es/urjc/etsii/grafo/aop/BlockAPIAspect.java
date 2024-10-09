package es.urjc.etsii.grafo.aop;

import es.urjc.etsii.grafo.exception.InvalidRandomException;
import es.urjc.etsii.grafo.util.Context;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

@Aspect
public class BlockAPIAspect {
    @Before("call(* java.lang.Math.random(..))")
    public void blockMathRandom() {
        var cfg = Context.Configurator.getBlockConfig();
        if(cfg == null || cfg.isBlockMathRandom()){
            throw new InvalidRandomException("Math.random() is forbidden. Use RandomManager.getRandom(), or Context.getRandom() instead.");
        }
    }

    @Before("call(* java.util.Collections.shuffle(..))")
    public void blockNonReproducibleShuffle() {
        var cfg = Context.Configurator.getBlockConfig();
        if(cfg == null || cfg.isBlockCollectionsShuffle()){
            throw new InvalidRandomException("Collections.shuffle(..) is forbidden. Use CollectionUtil.shuffle(..) instead.");
        }
    }
}
