package es.urjc.etsii.grafo.autoconfig.controller;

import es.urjc.etsii.grafo.autoconfig.irace.AutomaticIraceAlgorithmGenerator;
import es.urjc.etsii.grafo.autoconfig.irace.IraceOrchestrator;
import es.urjc.etsii.grafo.autoconfig.service.AlgorithmCandidateGenerator;
import es.urjc.etsii.grafo.autoconfig.service.AlgorithmInventoryService;
import es.urjc.etsii.grafo.config.SolverConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@ConditionalOnExpression(value = "${solver.autoconfig}")
public class AutoconfigDebugController {

    private final SolverConfig solverConfig;
    private final AlgorithmInventoryService inventory;
    private final AlgorithmCandidateGenerator candidateGenerator;
    private final AutomaticIraceAlgorithmGenerator<?, ?> algorithmGenerator;

    public AutoconfigDebugController(SolverConfig solverConfig, AlgorithmInventoryService inventory, AlgorithmCandidateGenerator candidateGenerator, AutomaticIraceAlgorithmGenerator<?, ?> algorithmGenerator) {
        this.solverConfig = solverConfig;
        this.inventory = inventory;
        this.candidateGenerator = candidateGenerator;
        this.algorithmGenerator = algorithmGenerator;
    }

    @GetMapping("/auto/debug/tree")
    public Object getTree(){
        return this.candidateGenerator.buildTree(solverConfig.getTreeDepth());
    }

    @GetMapping("/auto/debug/params")
    public Object params(){
        return this.candidateGenerator.componentParams();
    }

    @GetMapping("/auto/debug/inventory")
    public Object getInventory(){
        return this.inventory.getInventory();
    }

    @GetMapping("/auto/debug/decode/{cmdline}")
    public Object decode(@PathVariable String cmdline){
        var config = IraceOrchestrator.toIraceRuntimeConfig(cmdline);
        var algorithm = this.algorithmGenerator.buildAlgorithm(config.getAlgorithmConfig());
        return Map.of("config", config, "algorithm", algorithm);
    }
}
