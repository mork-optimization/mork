package es.urjc.etsii.grafo.util.graph_algorithms;

// ADAPTED FROM:
// "UCF Programming Team" Hackpack Code
// Taken from Team Badlands Hackpack
// Commented and Edited by Arup Guha on 3/6/2017 for COP 4516
// Code for Dinic's Network Flow Algorithm

import java.util.*;

/**
 * Dinic's algorithm for the maxflow problem.
 * See https://www.geeksforgeeks.org/dinics-algorithm-maximum-flow/
 */
public class Dinic {

    // Queue for the top level BFS.
    private ArrayDeque<Integer> q;

    // Stores the graph.
    private HashSet<Edge>[] adj;
    private int n;

    // For BFS.
    private boolean[] blocked;
    private int[] dist;

    private static final int oo = (int)1E9;

    /**
     * New Dinic algorithm or graph of size N
     *
     * @param N graph size
     */
    public Dinic(int N) {

        // s is the source, t is the sink, add these as last two nodes.
        n = N;

        // Everything else is empty.
        blocked = new boolean[n];
        dist = new int[n];
        q = new ArrayDeque<>();
        adj = new HashSet[n];
        for(int i = 0; i < n; ++i)
            adj[i] = new HashSet<>();
    }


    /**
     * Just adds an edge and ALSO adds it going backwards.
     * EDGES ARE IGNORED IF ALREADY EXISTS FROM V1 TO V2
     *
     * @param v1 Origin
     * @param v2 Destination
     * @param cap Capacity
     */
    public void add(int v1, int v2, int cap) {
        Edge e = new Edge(v1, v2, cap, 0);
        Edge rev = new Edge(v2, v1, 0, 0);
        adj[v1].add(rev.rev = e);
        adj[v2].add(e.rev = rev);
    }

    /**
     * BFS
     *
     * @param source start point
     * @param sink end point
     * @return true if path exists, false otherwise
     */
    public boolean bfs(int source, int sink) {

        // Set up BFS
        q.clear();
        Arrays.fill(dist, -1);
        dist[sink] = 0;
        q.add(sink);

        // Go backwards from sink looking for source.
        // We just care to mark distances left to the sink.
        while(!q.isEmpty()) {
            int node = q.poll();
            if(node == source)
                return true;
            for(Edge e : adj[node]) {
                if(e.rev.cap > e.rev.flow && dist[e.v2] == -1) {
                    dist[e.v2] = dist[node] + 1;
                    q.add(e.v2);
                }
            }
        }

        // Augmenting paths exist iff we made it back to the source.
        return dist[source] != -1;
    }

    /**
     * Runs inner DFS in Dinic's, from node pos with a flow of min.
     *
     * @param source start point
     * @param sink end point
     * @param min flow
     * @return flow
     */
    public int dfs(int source, int sink, int min) {

        // Made it to the sink, we're good, return this as our max flow for the augmenting path.
        if(source == sink)
            return min;
        int flow = 0;

        // Try each edge from here.
        for(Edge e : adj[source]) {
            int cur = 0;

            // If our destination isn't blocked and it's 1 closer to the sink and there's flow, we
            // can go this way.
            if(!blocked[e.v2] && dist[e.v2] == dist[source]-1 && e.cap - e.flow > 0) {

                // Recursively run dfs from here - limiting flow based on current and what's left on this edge.
                cur = dfs(e.v2, sink, Math.min(min-flow, e.cap - e.flow));

                // Add the flow through this edge and subtract it from the reverse flow.
                e.flow += cur;
                e.rev.flow = -e.flow;

                // Add to the total flow.
                flow += cur;
            }

            // No more can go through, we're good.
            if(flow == min)
                return flow;
        }

        // mark if this node is now blocked.
        blocked[source] = flow != min;

        // This is the flow
        return flow;
    }

    /**
     * Calculate flow from a source to a destination
     *
     * @param source start point
     * @param sink end point
     * @return flow
     */
    public int flow(int source, int sink) {
        int ret = 0;

        // Run a top level BFS.
        while(bfs(source, sink)) {

            // Reset this.
            Arrays.fill(blocked, false);

            // Run multiple DFS's until there is no flow left to push through.
            ret += dfs(source, sink, oo);
        }
        return ret;
    }


    // An edge connects v1 to v2 with a capacity of cap, flow of flow.
    private static class Edge {
        int v1, v2, cap, flow;
        Edge rev;
        Edge(int V1, int V2, int Cap, int Flow) {
            v1 = V1;
            v2 = V2;
            cap = Cap;
            flow = Flow;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Edge edge = (Edge) o;
            return v1 == edge.v1 &&
                    v2 == edge.v2;
        }

        @Override
        public int hashCode() {
            return Objects.hash(v1, v2);
        }
    }
}
