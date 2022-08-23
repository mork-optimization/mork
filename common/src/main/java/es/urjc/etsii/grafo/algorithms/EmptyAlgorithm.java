package es.urjc.etsii.grafo.algorithms;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;

public class EmptyAlgorithm<S extends Solution<S,I>, I extends Instance> extends Algorithm<S,I>{
    private final String name;

    public EmptyAlgorithm(String name) {
        if(name == null || name.isBlank()){
            throw new IllegalArgumentException("Invalid name, must be non non blank and non null");
        }
        this.name = name;
    }

    @Override
    public String getShortName() {
        return this.name;
    }

    @Override
    public String toString() {
        return "EA{" +
                "name='" + name + '\'' +
                '}';
    }

    @Override
    public S algorithm(I instance) {
        return this.newSolution(instance);
    }
}
