package es.urjc.etsii.grafo.MST.algorithms;

import es.urjc.etsii.grafo.MST.model.Edge;
import es.urjc.etsii.grafo.MST.model.MSTInstance;
import es.urjc.etsii.grafo.MST.model.MSTSolution;
import es.urjc.etsii.grafo.algorithms.Algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class KruskalAlgorithm extends Algorithm<MSTSolution, MSTInstance> {

    public KruskalAlgorithm() {
        super("Kruskal");
    }

    @Override
    public MSTSolution algorithm(MSTInstance instance) {
        int v = instance.v();
        var edges = new ArrayList<>(instance.getEdges());
        Collections.sort(edges);

        int[] parent = new int[v];
        for (int i = 0; i < v; i++) parent[i] = i;

        List<Edge> mstEdges = new ArrayList<>(instance.e());
        int edgesAdded = 0;

        for (Edge edge : edges) {
            int rootFrom = find(parent, edge.from());
            int rootTo = find(parent, edge.to());
            if (rootFrom != rootTo) {
                mstEdges.add(edge);
                parent[rootFrom] = rootTo;
                edgesAdded++;
                if (edgesAdded == v - 1) break;
            }
        }

        MSTSolution solution = new MSTSolution(instance);
        solution.setEdges(mstEdges);
        solution.notifyUpdate();
        return solution;
    }

    private int find(int[] parent, int i) {
        if (parent[i] != i) {
            parent[i] = find(parent, parent[i]);
        }
        return parent[i];
    }
}
