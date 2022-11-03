package es.urjc.etsii.grafo.algorithms.scattersearch;

import es.urjc.etsii.grafo.annotations.AlgorithmComponent;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;

import java.util.function.BinaryOperator;

@AlgorithmComponent
public abstract class SolutionCombinator<S extends Solution<S, I>, I extends Instance> implements BinaryOperator<S> {
}
