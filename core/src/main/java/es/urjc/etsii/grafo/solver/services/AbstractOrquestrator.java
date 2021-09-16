package es.urjc.etsii.grafo.solver.services;

import org.springframework.boot.CommandLineRunner;

import java.util.List;

public abstract class AbstractOrquestrator implements CommandLineRunner {

    public static <T> T decideImplementation(List<? extends T> list, Class<? extends T> defaultClass){
        //String qualifiedDefaultname = defaultClass.getName();
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
}
