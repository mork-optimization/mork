package es.urjc.etsii.grafo.MST.model;

public record Edge(int from, int to, double weight) implements Comparable<Edge>{

    @Override
    public int compareTo(Edge o) {
        return Double.compare(this.weight, o.weight);
    }
}

