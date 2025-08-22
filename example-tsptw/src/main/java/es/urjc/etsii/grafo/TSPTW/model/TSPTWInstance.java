package es.urjc.etsii.grafo.TSPTW.model;

import es.urjc.etsii.grafo.io.Instance;

public class TSPTWInstance extends Instance {

    private int n;
    private int[][] distance;
    private int[] windowStart;
    private int[] windowEnd;
    private boolean isSymmetric;

    public TSPTWInstance(String suggestedName, int n, int[][] distance, int[] windowStart, int[] windowEnd, boolean isSymmetric) {
        super(suggestedName);

        this.n = n;
        this.distance = distance;
        this.windowStart = windowStart;
        this.windowEnd = windowEnd;
        this.isSymmetric = isSymmetric;

        setProperty("n", n);
        setProperty("isSymmetric", isSymmetric);
    }


    /**
     * How should instances be ordered, when listing and solving them.
     * If not implemented, defaults to lexicographic sort by instance name
     * @param other the other instance to be compared against this one
     * @return comparison result
     */
    @Override
    public int compareTo(Instance other) {
        var otherInstance = (TSPTWInstance) other;
        return Integer.compare(this.n, otherInstance.n);
    }

    public int n() {
        return n;
    }

    public int dist(int a, int b) {
        return distance[a][b];
    }

    public int getWindowStart(int a) {
        return windowStart[a];
    }

    public int getWindowEnd(int a) {
        return windowEnd[a];
    }

    public boolean isSymmetric() {
        return isSymmetric;
    }

    public int[][] getDistance() {
        return distance;
    }

    public int[] getWindowStart() {
        return windowStart;
    }

    public int[] getWindowEnd() {
        return windowEnd;
    }
}
