package es.urjc.etsii.grafo.exgraph.algorithms;

import es.urjc.etsii.grafo.exgraph.model.Edge;
import es.urjc.etsii.grafo.exgraph.model.MSTInstance;
import es.urjc.etsii.grafo.exgraph.model.MSTSolution;
import es.urjc.etsii.grafo.algorithms.Algorithm;

import java.util.Arrays;
import java.util.PriorityQueue;

public class NDijkstraAlg extends Algorithm<MSTSolution, MSTInstance> {
    static final int BIG_VALUE = 1_000_000;

    public NDijkstraAlg() {
        super("NDijkstra");
    }

    @Override
    public MSTSolution algorithm(MSTInstance instance) {
        double[][] d = new double[instance.v()][instance.v()];
        IntPair[][] intPairs = new IntPair[instance.v()][instance.v()];
        for (int i = 0; i < instance.v(); i++) {
            intPairs[i] = toIntPair(instance, i);
        }
        for (int i = 0; i < instance.v(); i++) {
            d[i] = dijkstra(instance, intPairs, i);
        }
        MSTSolution solution = new MSTSolution(instance);
        solution.setScoreDist(d);
        solution.notifyUpdate();
        return solution;
    }

    record IntPair(int v, double w) implements Comparable<IntPair> {
        @Override
        public int compareTo(IntPair o) {
            return Double.compare(this.w, o.w);
        }
    }

    private double[] dijkstra(MSTInstance instance, IntPair[][] intPairs, int startingNode) {
        var queue = new PriorityQueue<IntPair>();
        queue.offer(new IntPair(startingNode,0));
        double[] dist = new double[instance.v()];
        Arrays.fill(dist,BIG_VALUE);
        dist[startingNode]=0;

        while(!queue.isEmpty()) {
            IntPair top = queue.poll();
            if (top.w > dist[top.v]) continue;
            for (IntPair aux : intPairs[top.v]) {
                if (dist[top.v] + aux.w >= dist[top.v]) continue;
                dist[aux.v] = dist[top.v] + aux.w;
                queue.offer(new IntPair(aux.v, dist[aux.v]));
            }
        }
        return dist;
    }

    private static IntPair[] toIntPair(MSTInstance instance, int node) {
        var neighbors = instance.getEdges(node);
        IntPair[] pairs = new IntPair[neighbors.size()];
        for (int i = 0; i < neighbors.size(); i++) {
            Edge edge = neighbors.get(i);
            int neighbor = edge.from() == node ? edge.to() : edge.from();
            pairs[i] = new IntPair(neighbor, edge.weight());
        }
        return pairs;
    }
}
