package es.urjc.etsii.grafo.autoconfig.controller;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.autoconfig.generator.AlgorithmCandidateGenerator;
import es.urjc.etsii.grafo.autoconfig.generator.TreeNode;
import es.urjc.etsii.grafo.autoconfig.inventory.AlgorithmInventoryService;
import es.urjc.etsii.grafo.autoconfig.irace.AlgorithmConfiguration;
import es.urjc.etsii.grafo.autoconfig.irace.AutomaticAlgorithmBuilder;
import es.urjc.etsii.grafo.autoconfig.irace.IraceOrchestrator;
import es.urjc.etsii.grafo.autoconfig.irace.IraceRuntimeConfiguration;
import es.urjc.etsii.grafo.config.SolverConfig;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class AutoconfigDebugController {

    private final SolverConfig solverConfig;
    private final AlgorithmInventoryService inventory;
    private final AlgorithmCandidateGenerator candidateGenerator;
    private final AutomaticAlgorithmBuilder<?, ?> algorithmGenerator;
    private final IraceOrchestrator<?, ?> orchestrator;

    public AutoconfigDebugController(SolverConfig solverConfig, AlgorithmInventoryService inventory, AlgorithmCandidateGenerator candidateGenerator, AutomaticAlgorithmBuilder<?, ?> algorithmGenerator, IraceOrchestrator<?, ?> orchestrator) {
        this.solverConfig = solverConfig;
        this.inventory = inventory;
        this.candidateGenerator = candidateGenerator;
        this.algorithmGenerator = algorithmGenerator;
        this.orchestrator = orchestrator;
    }

    @GetMapping("/auto/debug/tree")
    public Object getTree(){
        var tree = this.candidateGenerator.buildTree(solverConfig.getTreeDepth(), solverConfig.getMaxDerivationRepetition());
        var map = new HashMap<String, List<TreeNode>>();
        map.put("ROOT", tree);
        var rootNode = new TreeNode("", Algorithm.class, map);
        return TreeNodeStats.fromNode(rootNode);
    }

    record TreeNodeStats(String paramName, Class<?> clazz, int totalChildrenNodes, Map<String, List<TreeNodeStats>> children){
        public static TreeNodeStats fromNode(TreeNode node){
            int count = 1;
            var map = new HashMap<String, List<TreeNodeStats>>();
            for(var e: node.children().entrySet()){
                var list = new ArrayList<TreeNodeStats>();
                for(var n: e.getValue()){
                    var nodeStat = fromNode(n);
                    count += nodeStat.totalChildrenNodes;
                    list.add(nodeStat);
                }
                map.put(e.getKey(), list);
            }
            return new TreeNodeStats(node.paramName(), node.clazz(), count, map);
        }
    }

    @GetMapping("/auto/debug/params")
    public Object params(){
        return this.candidateGenerator.componentParams();
    }

    @GetMapping("/auto/debug/inventory")
    public Object getInventory(){
        return this.inventory.getInventory();
    }

    @GetMapping("/auto/debug/toIraceConfig/**")
    public Object toIraceConfig(HttpServletRequest request){
        String url = request.getRequestURI();
        String[] parts = url.split("/toIraceConfig/");
        if(parts.length != 2){
            return Map.of("error", "Invalid URL format. Expected format: /toIraceConfig/{commandLine}");
        }
        String cmdline = URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
        cmdline = cleanCmdLine(cmdline);
        Map<String, String> params = extractParameters(cmdline);
        var line1 = new StringBuilder();
        var line2 = new StringBuilder();
        for(var entry: params.entrySet()){
            line1.append(entry.getKey()).append("\t");
            line2.append(entry.getValue()).append("\t");
        }
        return Map.of("iraceConfig", line1.toString().trim() + "\n" + line2.toString().trim());
    }

    private static Map<String, String> extractParameters(String cmdline) {
        Map<String, String> params = new LinkedHashMap<>();
        for(var pair: cmdline.split("\\s+")) {
            if (pair.contains("=")) {
                var parts = pair.split("=");
                if (parts.length == 2) {
                    params.put(parts[0], parts[1]);
                } else {
                    throw new IllegalArgumentException("Invalid parameter format: " + pair);
                }
            } else {
                throw new IllegalArgumentException("Parameter without value: " + pair);
            }
        }
        return params;
    }

    @GetMapping("/auto/debug/decode/**")
    public Object decode(HttpServletRequest request){
        String url = request.getRequestURI();
        String[] parts = url.split("/decode/");
        if(parts.length != 2){
            return Map.of("error", "Invalid URL format. Expected format: /toIraceConfig/{commandLine}");
        }
        String cmdline = URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
        cmdline = cleanCmdLine(cmdline);

        var config = cmdline.startsWith("ROOT")?
                new IraceRuntimeConfiguration("Unknown", "Unknown", "Unknown", "Unknown", new AlgorithmConfiguration(cmdline.split("\\s+"))):
                IraceUtil.toIraceRuntimeConfig(cmdline);
        var algorithmString = this.algorithmGenerator.asParseableAlgorithm(config.getAlgorithmConfig());
        return Map.of("config", config, "algorithmString", algorithmString);
    }

    @GetMapping("/auto/debug/slow")
    public Object getSlow(){
        return this.orchestrator.getSlowRuns();
    }

    private String cleanCmdLine(String cmdline) {
        cmdline = cmdline.trim();
        return cmdline.contains("middleware.sh") ? Arrays.stream(cmdline.split("\\s+")).skip(1).collect(Collectors.joining(" ")) : cmdline;
    }

    @GetMapping("/auto/debug/history")
    public Object historic(){
        return this.orchestrator.getConfigHistoric();
    }

    @GetMapping("/auto/debug/rejected")
    public List<Object> getRejected(){
        return this.orchestrator.getRejected();
    }

}
