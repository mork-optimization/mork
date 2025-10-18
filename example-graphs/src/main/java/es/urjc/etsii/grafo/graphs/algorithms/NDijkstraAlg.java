package es.urjc.etsii.grafo.graphs.algorithms;

import es.urjc.etsii.grafo.graphs.model.MSTInstance;
import es.urjc.etsii.grafo.graphs.model.MSTSolution;
import es.urjc.etsii.grafo.algorithms.Algorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;

public class NDijkstraAlg extends Algorithm<MSTSolution, MSTInstance> {
    static final int BIG_VALUE = 1_000_000;

    public NDijkstraAlg() {
        super("NDijkstra");
    }

    @Override
    public MSTSolution algorithm(MSTInstance instance) {
        double[][] d = new double[instance.v()][instance.v()];
        List<IntPair>[] intPairs = new List[instance.v()];
        for (int i = 0; i < instance.v(); i++) {
            intPairs[i] = new ArrayList<>();
        }
        var allEdges = instance.getEdges();
        for(var edge: allEdges){
            intPairs[edge.from()].add(new IntPair(edge.to(), edge.weight()));
            intPairs[edge.to()].add(new IntPair(edge.from(), edge.weight()));
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

        @Override
        public String toString() {
            return "--(%s)--> %s".formatted(this.w, this.v);
        }
    }

    private double[] dijkstra(MSTInstance instance, List<IntPair>[] intPairs, int startingNode) {
        var queue = new PriorityQueue<IntPair>();
        queue.offer(new IntPair(startingNode,0));
        double[] dist = new double[instance.v()];
        Arrays.fill(dist,BIG_VALUE);
        dist[startingNode]=0;

        while(!queue.isEmpty()) {
            IntPair top = queue.poll();
            //if (top.w > dist[top.v]) continue;
            for (IntPair aux : intPairs[top.v]) {
                if (dist[top.v] + aux.w < dist[aux.v]){
                    dist[aux.v] = dist[top.v] + aux.w;
                    queue.offer(new IntPair(aux.v, dist[aux.v]));
                }
            }
        }
        return dist;
    }

}
