package es.urjc.etsii.grafo.graphs.algorithms;

import es.urjc.etsii.grafo.graphs.model.Edge;
import es.urjc.etsii.grafo.graphs.model.MSTInstance;
import es.urjc.etsii.grafo.graphs.model.MSTSolution;
import es.urjc.etsii.grafo.algorithms.Algorithm;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class PrimAlg extends Algorithm<MSTSolution, MSTInstance> {

    public PrimAlg() {
        super("Prim");
    }

    @Override
    public MSTSolution algorithm(MSTInstance instance) {
        int v = instance.v();
        boolean[] inMST = new boolean[v];
        List<Edge> mstEdges = new ArrayList<>(v);
        PriorityQueue<Edge> pq = new PriorityQueue<>(instance.e(), Comparator.comparingDouble(Edge::weight));

        // Start from vertex 0
        inMST[0] = true;
        for (var e : instance.getEdges(0)) {
            if(!(e.from() == 0 || e.to() == 0)) { // Avoid adding self-loop if exists
                throw new IllegalStateException("Edge " + e + " is not connected to vertex 0");
            }
            pq.add(e);
        }

        while (!pq.isEmpty()) {
            Edge edge = pq.poll();
            int next = inMST[edge.from()] ? edge.to() : edge.from();
            if (inMST[next]) continue;
            mstEdges.add(edge);
            inMST[next] = true;
            for (var e : instance.getEdges(next)) {
                if ((e.from() == next && !inMST[e.to()]) || (e.to() == next && !inMST[e.from()])) {
                    pq.add(e);
                }
            }
        }

        MSTSolution solution = new MSTSolution(instance);
        solution.setScoreEdges(mstEdges);
        solution.notifyUpdate();
        return solution;
    }
}
