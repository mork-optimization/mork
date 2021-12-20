package es.urjc.etsii.grafo.solver.services;

import java.util.List;

/**
 * Base orchestrator, contains common code.
 * An Orchestrator is the entity responsible for organizing all the work to execute and dispaching it.
 */
public abstract class AbstractOrchestrator {

    /**
     * Given a list of implementations for a given class, which one should be used if we can only use one?
     * If the user has implemented a component, always prefer the user component to default or mork ones.
     *
     * @param list List of implementations for a given base class
     * @param defaultClass MORK implementation, or a default implementation
     * @param <T> Class type
     * @return Implementation to use
     */
    public static <T> T decideImplementation(List<? extends T> list, Class<? extends T> defaultClass){
        // TODO This method could be deleted if using Spring's autowiring priorities
        T defaultImpl = null;
        for(var e: list){
            if(!e.getClass().equals(defaultClass)){
                return e;
            } else {
                defaultImpl = e;
            }
        }
        if(defaultImpl == null) throw new IllegalStateException("Where is the default implementation???");
        return defaultImpl;
    }

    /**
     * Run the Mork core, depending on the work to do.
     * For example: there are two execution modes, standalone experiments and irace autoconfiguration.
     * @param args command line parameters
     */
    public abstract void run(String... args);
}
