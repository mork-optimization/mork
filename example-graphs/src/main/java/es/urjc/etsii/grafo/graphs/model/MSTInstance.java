package es.urjc.etsii.grafo.graphs.model;

import es.urjc.etsii.grafo.io.Instance;

import java.util.List;

public class MSTInstance extends Instance {

    private final List<Edge>[] graph;
    private final List<Edge> edges;
    private final int e;
    private final int v;


    public MSTInstance(String name, List<Edge>[] graph, List<Edge> edges, int seed){
        super(name);
        this.e = edges.size();
        this.v = graph.length;
        this.graph = graph;
        this.edges = edges;

        setProperty("v", graph.length);
        setProperty("e", this.e);

        int maxEdges = v * (v - 1) / 2;
        setProperty("density", (double)e / maxEdges);
    }

    public int v(){
        return v;
    }

    public int e(){
        return e;
    }

    public List<Edge> getEdges() {
        return this.edges;
    }

    public List<Edge> getEdges(int node) {
        return this.graph[node];
    }

    /**
     * How should instances be ordered, when listing and solving them.
     * If not implemented, defaults to lexicographic sort by instance name
     * @param other the other instance to be compared against this one
     * @return comparison result
     */
    @Override
    public int compareTo(Instance other) {
        var otherInstance = (MSTInstance) other;
        return Integer.compare(this.v, otherInstance.v);
    }
}
