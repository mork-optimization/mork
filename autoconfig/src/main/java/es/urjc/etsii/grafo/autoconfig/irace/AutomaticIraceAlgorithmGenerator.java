package es.urjc.etsii.grafo.autoconfig.irace;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.autoconfig.service.AlgorithmBuilderService;
import es.urjc.etsii.grafo.autoconfig.service.AlgorithmCandidateGenerator;
import es.urjc.etsii.grafo.config.SolverConfig;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;

import java.util.List;

@ConditionalOnExpression(value = "${solver.autoconfig}")
public class AutomaticIraceAlgorithmGenerator<S extends Solution<S,I>, I extends Instance> extends IraceAlgorithmGenerator<S,I> {

    private final List<AlgorithmCandidateGenerator.Node> tree;
    private final AlgorithmBuilderService algorithmBuilder;

    public AutomaticIraceAlgorithmGenerator(SolverConfig solverConfig, AlgorithmCandidateGenerator candidateGenerator, AlgorithmBuilderService algorithmBuilder) {
        this.algorithmBuilder = algorithmBuilder;
        this.tree = candidateGenerator.buildTree(solverConfig.getTreeDepth());
    }

    @Override
    public Algorithm<S, I> buildAlgorithm(AlgorithmConfiguration config) {
        String[] params = config.getConfig().keySet().toArray(new String[0]);
        validateParams(params);
        var algorithmAsString = algorithmString(params);
        var algorithm = this.algorithmBuilder.buildAlgorithmFromString(algorithmAsString);
        return (Algorithm<S, I>) algorithm;
    }

    private void validateParams(String[] params) {

    }

    public String algorithmString(String[] params){
        return null;
    }
}
