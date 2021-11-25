package es.urjc.etsii.grafo.util;


/**
 * Util methods to manipulate graphs
 */
public class GraphUtil {

    /**
     * Returns the maximum number of edges for a complete graph with n nodes
     *
     * @param n number of nodes
     * @return maximum number of edges
     */
    public static long maxNEdges(int n){
        // https://en.wikipedia.org/wiki/Complete_graph#Properties
        return (long) (n - 1) * n / 2;
    }
}
