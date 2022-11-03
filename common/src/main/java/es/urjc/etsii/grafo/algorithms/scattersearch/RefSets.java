package es.urjc.etsii.grafo.algorithms.scattersearch;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;

import java.util.ArrayList;
import java.util.Objects;

public final class RefSets<S extends Solution<S, I>, I extends Instance> {
    ArrayList<ScatterSearch.Pair<S, I>> diversity;
    ArrayList<ScatterSearch.Pair<S, I>> best;
    final int diversitySize;
    final int bestSize;

    public RefSets(ArrayList<ScatterSearch.Pair<S, I>> diversity, int diversitySize, ArrayList<ScatterSearch.Pair<S, I>> best, int bestSize) {
        this.diversity = diversity;
        this.best = best;
        this.diversitySize = diversitySize;
        this.bestSize = bestSize;
    }

    public ArrayList<ScatterSearch.Pair<S, I>> diversity() {
        return diversity;
    }

    public ArrayList<ScatterSearch.Pair<S, I>> best() {
        return best;
    }

    public int bestSize() {
        return this.bestSize;
    }

    public int diversitySize() {
        return this.diversitySize;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (RefSets) obj;
        return Objects.equals(this.diversity, that.diversity) &&
                Objects.equals(this.best, that.best);
    }

    @Override
    public int hashCode() {
        return Objects.hash(diversity, best);
    }

    @Override
    public String toString() {
        return "RefSets[" +
                "diversity=" + diversity + ", " +
                "best=" + best + ']';
    }
}

