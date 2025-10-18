package es.urjc.etsii.grafo.graphs.model;

public record Edge(int from, int to, double weight) implements Comparable<Edge>{

    @Override
    public int compareTo(Edge o) {
        return Double.compare(this.weight, o.weight);
    }
}

