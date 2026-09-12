package es.urjc.etsii.grafo.tsptw.model;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.util.ArrayUtil;

public class TSPTWInstance extends Instance {

    private final int n;
    private final double[][] distance;
    private final int[] windowStart;
    private final int[] windowEnd;
    private final boolean isSymmetric;
    private final boolean[][] timeWindowInfeasible;

    public TSPTWInstance(String suggestedName, int n, double[][] distance, int[] windowStart, int[] windowEnd) {
        super(suggestedName);

        this.n = n;
        this.distance = distance;
        this.windowStart = windowStart;
        this.windowEnd = windowEnd;
        this.timeWindowInfeasible = new boolean[n][n];
        boolean symmetric = true;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                timeWindowInfeasible[i][j] = windowStart[i] + distance[i][j] > windowEnd[j];
                if (distance[i][j] != distance[j][i]) symmetric = false;
            }
        }
        this.isSymmetric = symmetric;

        setProperties();
    }

    private void setProperties() {
        setProperty("n", n);
        setProperty("isSymmetric", isSymmetric);

        int[] windowSizes = new int[windowStart.length];
        for (int i = 0; i < windowSizes.length; i++) {
            windowSizes[i] = windowEnd[i] - windowStart[i];
        }
        var windowStats = ArrayUtil.stats(windowSizes);
        setProperty("windowSizeAvg", windowStats.avg());
        setProperty("windowSizeMin", windowStats.min());
        setProperty("windowSizeMax", windowStats.max());

        int c = 0;
        double total = 0, min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
        for (int i = 0; i < distance.length; i++) {
            for (int j = 0; j < distance[i].length; j++) {
                if(i != j){
                    c++;
                    total += distance[i][j];
                    if(distance[i][j] < min){
                        min = distance[i][j];
                    }
                    if(distance[i][j] > max){
                        max = distance[i][j];
                    }
                }
            }
        }
        setProperty("distMin", min);
        setProperty("distMax", max);
        setProperty("distAvg", total/c);
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

    public double dist(int a, int b) {
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

    public boolean isTimeWindowInfeasible(int from, int to) {
        return timeWindowInfeasible[from][to];
    }

    public double[][] getDistance() {
        return distance;
    }

    public int[] getWindowStart() {
        return windowStart;
    }

    public int[] getWindowEnd() {
        return windowEnd;
    }
}
