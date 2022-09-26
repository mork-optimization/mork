package es.urjc.etsii.grafo.orchestrator;

import org.springframework.context.annotation.Profile;

@Profile("util")
public class UtilOrchestrator extends AbstractOrchestrator {

    @Override
    public void run(String... args) {
        throw new UnsupportedOperationException("Util orchestrator not implemented yet");
    }
}
