package es.urjc.etsii.grafo.solver;

import es.urjc.etsii.grafo.orchestrator.AbstractOrchestrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RunOnStart implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(RunOnStart.class);

    private final Map<String, AbstractOrchestrator> orchestrators;

    public RunOnStart(List<AbstractOrchestrator> orchestrators) {
        this.orchestrators = new HashMap<>();
        for(var o: orchestrators){
            for(var name: o.getNames()){
                name = name.replaceAll("[-_]", "").toLowerCase();
                log.debug("Registering orchestrator: {} as {}", o.getClass().getSimpleName(), name);
                this.orchestrators.put(name, o);
            }
        }
    }

    @Override
    public void run(String... args) throws Exception {
        var orchestrator = getOrchestrator(args);
        orchestrator.run(args);
    }

    public AbstractOrchestrator getOrchestrator(String[] args) {
        AbstractOrchestrator orchestrator = null;
        for(var string: args){
            var name = string.replaceAll("[-_]", "").toLowerCase();
            if(orchestrators.containsKey(name)){
                orchestrator = orchestrators.get(name);
            }
        }
        if(orchestrator == null){
            orchestrator = orchestrators.get("default");
            if(orchestrator == null){
                throw new IllegalArgumentException("No orchestrator found for the given arguments");
            } else {
                log.debug("Running default orchestrator");
            }
        }
        return orchestrator;
    }
}
