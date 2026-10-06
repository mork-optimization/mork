package es.urjc.etsii.grafo.solver;

import es.urjc.etsii.grafo.orchestrator.AbstractOrchestrator;
import es.urjc.etsii.grafo.orchestrator.DefaultOrchestrator;
import es.urjc.etsii.grafo.orchestrator.InstanceProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static es.urjc.etsii.grafo.monitoring.MonitorModeState.Mode.*;

@Service
public class RunOnStart implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(RunOnStart.class);

    private final Map<String, AbstractOrchestrator> orchestrators;
    private final es.urjc.etsii.grafo.monitoring.MonitorModeState monitorMode;

    public RunOnStart(List<AbstractOrchestrator> orchestrators, es.urjc.etsii.grafo.monitoring.MonitorModeState monitorMode) {
        this.monitorMode = monitorMode;
        this.orchestrators = new HashMap<>();
        for(var o: orchestrators){
            for(var name: o.getNames()){
                name = normalizeName(name);
                log.debug("Registering orchestrator: {} as {}", o.getClass().getSimpleName(), name);
                this.orchestrators.put(name, o);
            }
        }
    }

    @Override
    public void run(String... args) throws Exception {
        var orchestrator = getOrchestrator(args);
        var mode = orchestrator instanceof DefaultOrchestrator
                ? STANDARD
                : orchestrator instanceof InstanceProperties
                ? UTILITY
                : TUNING;
        for (var arg : args) if (arg.equals("--follower")) mode = WORKER;
        monitorMode.select(mode);
        orchestrator.run(args);
    }

    public AbstractOrchestrator getOrchestrator(String[] args) {
        AbstractOrchestrator orchestrator = null;
        for(var string: args){
            var name = resolveOrchestratorName(string);
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

    private String resolveOrchestratorName(String arg) {
        var name = normalizeName(arg);
        if ("follower".equals(name) && orchestrators.containsKey("irace")) {
            return "irace";
        }
        return name;
    }

    private static String normalizeName(String value) {
        return value.replaceAll("[-_]", "").toLowerCase();
    }
}
