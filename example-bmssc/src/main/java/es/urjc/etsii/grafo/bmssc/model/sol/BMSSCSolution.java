package es.urjc.etsii.grafo.bmssc.model.sol;

import es.urjc.etsii.grafo.bmssc.model.BMSSCInstance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.util.CollectionUtil;
import es.urjc.etsii.grafo.util.DoubleComparator;

import java.util.*;

// OLD StrategicSolution
public class BMSSCSolution extends Solution<BMSSCSolution, BMSSCInstance> {

    /**
     * For use in arrays / fixed size data structures, mark a given position as an invalid / not assigned value
     */
    static final int NOT_ASSIGNED = -1;

    /**
     * Array of Sets, each set contains the points assigned to that set
     */
    final Set<Integer>[] clusters;

    /**
     * Unassigned points
     */
    final Set<Integer> notAssignedPoints;

    /**
     * Stores which point is assigned to which cluster
     */
    final int[] setOfPoint;

    /**
     * Cluster sizes. Can be different to real cluster sizes because this constraint
     * will be relaxed by the strategic oscillation method
     */
    private final int[] actualClusterSizes;

    /**
     * Value of the objective function for each cluster
     */
    double[] clusterScore;

    /**
     * Remove cost cache
     */
    double[] removeCost;

    /**
     * Assign cost cache
     */
    final double[][] assignCost;

    /**
     * Calculate the score of a set of points without using a solution, and without side effects, used for validation purposes
     * @param instance current instance
     * @param solution Solution data
     * @return Score calculated from scratch.
     */
    public static double score(BMSSCInstance instance, Set<Integer>[] solution){
        double[] costPerCluster = costPerCluster(instance, solution);

        double total = 0;
        for (int i = 0; i < costPerCluster.length; i++) {
            int cSize = solution[i].size();
            if(cSize > 0){
                total += costPerCluster[i] / cSize;
            }
        }
        return total;
    }

    public static double[] costPerCluster(BMSSCInstance instance, Set<Integer>[] solution){
        double[] costPerCluster = new double[solution.length];
        for (int i = 0; i < solution.length; i++) {
            int[] pointsInSet = CollectionUtil.toIntArray(solution[i]);

            // For each pair of points
            for (int j = 0; j < pointsInSet.length - 1; j++) {
                for (int k = j + 1; k < pointsInSet.length; k++) {
                    costPerCluster[i] += instance.distance(pointsInSet[j], pointsInSet[k]);
                }
            }
        }
        return costPerCluster;
    }


    public BMSSCSolution(BMSSCInstance instance){
        super(instance);

        //noinspection unchecked due to generic array creation
        this.clusters = new Set[instance.k];

        this.actualClusterSizes = new int[instance.getNClusters()];
        for (int i = 0; i < instance.k; i++) {
            int cSize = instance.getClusterSize(i);
            this.clusters[i] = new HashSet<>((int) (cSize/.75f)); // take into account default load factor
            this.actualClusterSizes[i] = cSize;
        }

        this.setOfPoint = new int[instance.n];
        Arrays.fill(setOfPoint, NOT_ASSIGNED);

        this.clusterScore = new double[instance.k];

        // Cache costs are all 0s because no points are assigned by default
        this.assignCost = new double[instance.n][instance.k];
        this.removeCost = new double[instance.n];

        this.notAssignedPoints = new HashSet<>(instance.getPoints());
    }

    public BMSSCSolution(BMSSCSolution solution){
        super(solution.getInstance());
        var instance = solution.getInstance();

        //noinspection unchecked due to generic array creation
        this.clusters = new Set[solution.clusters.length];

        for (int i = 0; i < this.clusters.length; i++) {
            this.clusters[i] = new HashSet<>(solution.clusters[i]);
        }

        this.clusterScore = solution.clusterScore.clone();
        this.setOfPoint = solution.setOfPoint.clone();

        this.removeCost = solution.removeCost.clone();
        this.assignCost = new double[instance.n][instance.k];

        for (int i = 0; i < instance.n; i++) {
            System.arraycopy(solution.assignCost[i], 0, this.assignCost[i], 0, instance.k);
        }
        this.actualClusterSizes = solution.actualClusterSizes.clone();
        this.notAssignedPoints = new HashSet<>(solution.notAssignedPoints);
    }

    /**
     * Relax cluster size constraint, by the given margin. New cluster sizes will be
     * clusterSize * (1 + margin)
     * Subsequent calls to this method do NOT further relax the constraint.
     * @param margin margin, must be greater or equal to zero.
     */
    public void relaxClusterSizeConstraint(float margin){
        var instance = getInstance();
        for (int i = 0; i < instance.k; i++) {
            actualClusterSizes[i] = Math.round(instance.getClusterSize(i)*(1+margin));
        }
    }

    /**
     * Restore cluster size constraint to the feasible value provided by the instance
     */
    public void restoreClusterSizeConstraint(){
        relaxClusterSizeConstraint(0);
    }

    @Override
    public BMSSCSolution cloneSolution() {
        return new BMSSCSolution(this);
    }

    public double getScore() {
        assert clusterScore.length == clusters.length;

        double temp = 0;
        for (int i = 0; i < clusterScore.length; i++) {
            int cSize = clusters[i].size();
            if(cSize != 0){
                temp += clusterScore[i] / cSize;
            }
        }
        return temp;
    }

    public double recalculateScore() {
        // Calculate f.o score from scratch, without side effects
        return BMSSCSolution.score(getInstance(), clusters);
    }

    @Override
    public String toString() {
        return "%s".formatted(this.getScore());
    }

    public int getClusterSize(int i){
        return actualClusterSizes[i];
    }

    public int[] getClusterSizes(){
        return actualClusterSizes;
    }

    public boolean feasibleClusterSizes(){
        var ins = getInstance();
        for (int i = 0; i < ins.k; i++) {
            if(this.getCluster(i).size() > ins.getClusterSize(i)) {
                return false;
            }
        }
        return true;
    }

    // Start copy paste
    public Set<Integer> getCluster(int i) {
        return this.clusters[i];
    }

    public Set<Integer> getNotAssignedPoints() {
        return Collections.unmodifiableSet(this.notAssignedPoints);
    }

    public void generateCachedScore() {
        this.clusterScore = costPerCluster(getInstance(), clusters);
    }

    /**
     * Checks if a cluster is full. Checks if the cluster size restriction is relaxed, and takes it into account
     * @param cluster cluster to check
     * @return true if the current cluster size is equals to its assigned size, false otherwise
     */
    public boolean isFullCluster(int cluster) {
        return this.clusters[cluster].size() == this.getClusterSize(cluster);
    }

    /**
     * Get the set a point is inside of
     * @param a Point to get which cluster is assigned to
     * @return the cluster ID, or -1 if the point is not assigned yet
     */
    public int clusterOf(int a) {
        return this.setOfPoint[a];
    }

    public double cachedAssignCost(int a, int k){
        return this.assignCost[a][k];
    }

    public double calculateAssignCost(int a, int k) {
        if(clusterOf(a)==k)
            return 0;
        double sum = 0.0;
        for (int i : this.clusters[k]) {
            double distanceBetween = getInstance().distance(a, i);
            sum += distanceBetween;
        }
        return sum;
    }

    /**
     * Calculate how much the optimal value would increase or decrease if we assign point a to cluster k
     *
     * @param a Point to assign
     */
    double calculateRemoveCost(int a) {
        if(!isAssigned(a)){
            return 0;
        }
//        assert isAssigned(a);
        int k = clusterOf(a);
        assert clusters[k].contains(a);

        double sum = 0.0;
        for (int i : this.clusters[k]) {
            double distanceBetween = getInstance().distance(a, i);
            sum += distanceBetween;
        }
        return -sum;
    }

    /**
     * Checks if a point is asigned to a cluster
     *
     * @param a Point to check
     * @return true if assigned to a cluster, false otherwise
     */
    public boolean isAssigned(int a) {
        return this.setOfPoint[a] != NOT_ASSIGNED;
    }

    boolean cachesValid(){
        var ins = getInstance();

        // Validate remove costs
        for (int i = 0; i < getInstance().n; i++) {
            if(!DoubleComparator.equals(this.removeCost[i], calculateRemoveCost(i))) {
                return false;
            }
        }
        // Validate insert costs
        for (int i = 0; i < ins.n; i++) {
            for (int j = 0; j < ins.k; j++) {
                if(!DoubleComparator.equals(this.assignCost[i][j], calculateAssignCost(i,j))) {
                    return false;
                }
            }
        }
        return true;
    }
}
