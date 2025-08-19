package es.urjc.etsii.grafo.BMSSC.model;

import es.urjc.etsii.grafo.io.Instance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class BMSSCInstance extends Instance {

    /**
     * Order instances by increasing number of points
     */
    public static final Comparator<BMSSCInstance> INSTANCE_COMPARATOR = Comparator.comparing(BMSSCInstance::getNPoints).reversed();

    /**
     * Number of points
     */
    public final int n;

    /**
     * Number of dimensions
     */
    public final int d;

    /**
     * Number of clusters
     */
    public final int k;

    /**
     * Points matrix, each row represents a point, each column a dimension
     */
    private double[][] points;

    /**
     * Symmetric matrix, stores the cost between any two points
     */
    protected double[][] distances;

    /**
     * Minimum points per cluster (minPointsPerCluster = n / k)
     */
    public final int minPointsPerCluster;

    /**
     * Assigned Size for each cluster
     */
    private int[] clusterSizes;

    public BMSSCInstance(String name, int n, int d, int k, double[][] pointData){
        super(name);
        this.n = n;
        this.d = d;
        this.k = k;

        this.minPointsPerCluster = n / k;
        this.clusterSizes = new int[k];
        Arrays.fill(clusterSizes, minPointsPerCluster);

        // If the integer division is not exact, assign remaining points to clusters in order
        for (int i = 0; i < n % k; i++) {
            clusterSizes[i]++;
        }

        this.points = pointData;
        // Precalculate distances between each pair of points
        calculateDistances();
        checkDataIsValid();
    }

    private void checkDataIsValid() {
        if (points.length != n){
            throw new RuntimeException("Declared number of points does not correspond with the datafile: " + this.getId());
        }

        for (double[] f : points) {
            if (f.length != d)
                throw new RuntimeException(String.format("Point length %s: %s does not match declared dimension %d, instance %s", f.length, Arrays.toString(f), this.d, this.getId()));
        }
    }

    private void calculateDistances() {
        assert (this.distances == null) : "Distances already calculated";
        this.distances = new double[this.n][this.n];
        for (int i = 0; i < this.n - 1; i++) {
            for (int j = i + 1; j < this.n; j++) {
                double distance = distanceBetween(this.points[i], this.points[j]);
                this.distances[i][j] = distance;
                this.distances[j][i] = distance;
            }
        }
    }

    public double distanceBetween(double[] a, double[] b) {
        assert (a != null && b != null && a.length == b.length) : "Points are null, or have different number of dimensions";
        double distance = 0;
        for (int i = 0; i < a.length; i++) {
            double t = (a[i] - b[i]);
            distance += t * t;
        }
        return distance;
    }

    public double[] getPoint(int a) {
        return this.points[a];
    }

    public int getClusterSize(int i) {
        return clusterSizes[i];
    }

    public int[] getClusterSizes() {
        return clusterSizes;
    }

    public double distance(int a, int b) {
        return this.distances[a][b];
    }

    public int getNPoints() {
        return n;
    }

    public int getNDimensions() {
        return d;
    }

    public int getNClusters() {
        return k;
    }

    @Override
    public int compareTo(Instance other) {
        return INSTANCE_COMPARATOR.compare(this, (BMSSCInstance) other);
    }

    public List<Integer> getPoints() {
        var list = new ArrayList<Integer>(this.n);
        for (int i = 0; i < this.n; i++) {
            list.add(i);
        }
        return list;
    }
}
