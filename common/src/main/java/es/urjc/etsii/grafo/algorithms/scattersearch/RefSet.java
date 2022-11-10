package es.urjc.etsii.grafo.algorithms.scattersearch;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class RefSet<S extends Solution<S, I>, I extends Instance> {
    S[] solutions;
    Set<S> currentRefset;
    final int bestSize;
    final int diversitySize;

    public RefSet(S[] solutions, int bestSize, int diversitySize) {
        if(solutions.length != bestSize + diversitySize){
            throw new IllegalArgumentException("Size mismatch: expeted %s == %s + %s".formatted(solutions.length, bestSize, diversitySize));
        }

        this.solutions = solutions;
        this.currentRefset = new HashSet<>(List.of(solutions));
        this.bestSize = bestSize;
        this.diversitySize = diversitySize;
    }

    public boolean isInRefset(S solution){
        return this.currentRefset.contains(solution);
    }

    @Override
    public String toString() {
        return "RefSet{" +
                "solutions=" + Arrays.toString(solutions) +
                '}';
    }
}

