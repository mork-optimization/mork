package es.urjc.etsii.grafo.autoconfig.controller;

import es.urjc.etsii.grafo.autoconfig.service.AlgorithmCandidateGenerator;
import es.urjc.etsii.grafo.autoconfig.service.AlgorithmInventoryService;
import es.urjc.etsii.grafo.config.SolverConfig;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DebugController {

    private final SolverConfig solverConfig;
    private final AlgorithmInventoryService inventory;
    private final AlgorithmCandidateGenerator generator;

    public DebugController(SolverConfig solverConfig, AlgorithmInventoryService inventory, AlgorithmCandidateGenerator generator) {
        this.solverConfig = solverConfig;
        this.inventory = inventory;
        this.generator = generator;
    }

    @GetMapping("/auto/debug/tree")
    public Object getTree(){
        return this.generator.buildTree(solverConfig.getTreeDepth());
    }

    @GetMapping("/auto/debug/params")
    public Object params(){
        return this.generator.componentParams();
    }

    @GetMapping("/auto/debug/inventory")
    public Object getInventory(){
        return this.inventory.getInventory();
    }

}
