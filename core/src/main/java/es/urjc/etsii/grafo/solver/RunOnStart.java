package es.urjc.etsii.grafo.solver;

import es.urjc.etsii.grafo.solver.services.AbstractOrchestrator;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("!test")
public class RunOnStart implements CommandLineRunner {

    private final AbstractOrchestrator orchestrator;

    public RunOnStart(AbstractOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @Override
    public void run(String... args) throws Exception {
        this.orchestrator.run(args);
    }
}
