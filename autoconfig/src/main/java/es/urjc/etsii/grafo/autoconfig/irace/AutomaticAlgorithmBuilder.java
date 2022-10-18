package es.urjc.etsii.grafo.autoconfig.irace;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.autoconfig.irace.params.ComponentParameter;
import es.urjc.etsii.grafo.autoconfig.service.AlgorithmBuilderService;
import es.urjc.etsii.grafo.autoconfig.service.AlgorithmCandidateGenerator;
import es.urjc.etsii.grafo.config.SolverConfig;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class AutomaticAlgorithmBuilder<S extends Solution<S,I>, I extends Instance> extends AlgorithmBuilder<S,I> {

    private final List<AlgorithmCandidateGenerator.Node> tree;
    private final AlgorithmBuilderService algorithmBuilder;

    public AutomaticAlgorithmBuilder(SolverConfig solverConfig, AlgorithmCandidateGenerator candidateGenerator, AlgorithmBuilderService algorithmBuilder) {
        this.algorithmBuilder = algorithmBuilder;
        this.tree = candidateGenerator.buildTree(solverConfig.getTreeDepth());
    }

    public String buildAlgorithmString(AlgorithmConfiguration config){
        var params = config.getConfig().keySet().toArray(new String[0]);
        Arrays.sort(params);
        var root = new Node("ROOT");
        for(var p: params){
            String[] t = p.split(Pattern.quote(ComponentParameter.PARAM_SEP));
            Node current = root;
            for (int i = 0; i < t.length - 1; i++) {
                String[] nameValue = t[i].split(Pattern.quote(ComponentParameter.NAMEVALUE_SEP));
                assert nameValue.length == 2;
                current = current.children.get(nameValue[0]);
            }

            String[] nameValue = t[t.length-1].split(Pattern.quote(ComponentParameter.NAMEVALUE_SEP));
            assert nameValue.length == 1;
            String name = nameValue[0];
            String value = config.getValue(p).orElseThrow(); // get with full parameter name, not only "name" which is a suffix of the full parameter name
            current.children.put(name, new Node(value));
        }

        return root.toAlgorithmString();
    }

    @Override
    public Algorithm<S, I> buildAlgorithm(AlgorithmConfiguration config) {
        var algorithmAsString = this.buildAlgorithmString(config);
        var algorithm = this.algorithmBuilder.buildAlgorithmFromString(algorithmAsString);
        return (Algorithm<S, I>) algorithm;
    }

    private record Node(String name, Map<String, Node> children){
        Node(String name){
            this(name, new HashMap<>());
        }

        public String toAlgorithmString(){
            if(!this.name.equals("ROOT")){
                throw new IllegalStateException("Invalid tree walk");
            }
            assert this.children.size() == 1;
            var sb = new StringBuilder();
            this.children.entrySet().iterator().next().getValue().toAlgorithmString(sb, 0);
            return sb.toString();
        }

        public void toAlgorithmString(StringBuilder sb, int depth){
            sb.append(this.name);
            sb.append('{');
            depth++;
            boolean atLeastOneIter = false;
            for (var child : this.children.entrySet()) {
                if (atLeastOneIter) {
                    sb.append(",");
                } else {
                    atLeastOneIter = true;
                }
                tabs(sb, depth);
                sb.append(child.getKey()).append('=');
                child.getValue().toAlgorithmString(sb, depth);
            }
            depth--;
            if(atLeastOneIter) tabs(sb, depth);
            sb.append('}');
        }

        public void tabs(StringBuilder sb, int depth){
            sb.append("\n");
            sb.append("\t".repeat(Math.max(0, depth)));
        }
    }
}
