package es.urjc.etsii.grafo.bmssc.model;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.util.ArrayUtil;

import java.util.Comparator;

public final class BMSSCInstance extends Instance {
    // Keep the historical solve order, largest instances first.
    public static final Comparator<BMSSCInstance> INSTANCE_COMPARATOR =
            Comparator.comparingInt(BMSSCInstance::getNPoints).reversed();

    public final int n;
    public final int d;
    public final int k;
    private final double[][] points;
    private final double[][] distances;
    private final int[] clusterSizes;

    public BMSSCInstance(String name, int n, int d, int k, double[][] pointData) {
        super(name);
        if (n <= 0 || d <= 0 || k < 1 || k > n) {
            throw new IllegalArgumentException(name + ": expected n > 0, d > 0 and 1 <= k <= n");
        }
        if (pointData == null || pointData.length != n) {
            throw new IllegalArgumentException(name + ": expected " + n + " points");
        }
        this.n = n;
        this.d = d;
        this.k = k;
        this.points = new double[n][];
        for (int p = 0; p < n; p++) {
            if (pointData[p] == null || pointData[p].length != d) {
                throw new IllegalArgumentException(name + ": line " + (p + 2) + ": expected " + d + " dimensions");
            }
            this.points[p] = pointData[p].clone();
            for (double coordinate : this.points[p]) {
                if (!Double.isFinite(coordinate)) {
                    throw new IllegalArgumentException(name + ": line " + (p + 2) + ": non-finite coordinate");
                }
            }
        }
        this.clusterSizes = new int[k];
        for (int c = 0; c < k; c++) clusterSizes[c] = n / k + (c < n % k ? 1 : 0);
        this.distances = new double[n][n];
        for (int p = 0; p < n; p++) {
            for (int q = p + 1; q < n; q++) {
                double distance = 0;
                for (int dimension = 0; dimension < d; dimension++) {
                    double difference = points[p][dimension] - points[q][dimension];
                    distance += difference * difference;
                }
                if (!Double.isFinite(distance)) {
                    throw new IllegalArgumentException(name + ": lines " + (p + 2) + " and " + (q + 2)
                            + ": non-finite squared distance");
                }
                distances[p][q] = distances[q][p] = distance;
            }
        }
        setProperty("n", n);
        setProperty("d", d);
        setProperty("k", k);
        setProperty("unbalancedK", n % k);
        var stats = ArrayUtil.statsUpperTriangle(distances);
        setProperty("distance_min", stats.min());
        setProperty("distance_max", stats.max());
        setProperty("distance_avg", stats.avg());
        setProperty("distance_std", stats.std());
    }

    public double[] getPoint(int point) { return points[point].clone(); }
    public int getClusterSize(int cluster) { return clusterSizes[cluster]; }
    public int[] getClusterSizes() { return clusterSizes.clone(); }
    public double distance(int p, int q) { return distances[p][q]; }
    public int getNPoints() { return n; }
    public int getNDimensions() { return d; }
    public int getNClusters() { return k; }

    @Override
    public int compareTo(Instance other) {
        return INSTANCE_COMPARATOR.compare(this, (BMSSCInstance) other);
    }
}
