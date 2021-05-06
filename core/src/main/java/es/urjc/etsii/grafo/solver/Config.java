package es.urjc.etsii.grafo.solver;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component
public class Config {
    private static final Logger log = Logger.getLogger(Config.class.getName());
    private static boolean maximizing;

    protected Config(@Value("${solver.maximizing}") boolean maximizing){
        log.info("Config.class initialized");
        Config.maximizing = maximizing;
    }

    public static boolean isMaximizing() {
        return maximizing;
    }
}
