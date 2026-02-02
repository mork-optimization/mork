package es.urjc.etsii.grafo.flayouts.model;

import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.util.ArrayUtil;
import es.urjc.etsii.grafo.util.DoubleComparator;
import org.glassfish.jersey.internal.guava.Sets;

import java.util.*;

public class FLPSolution extends Solution<FLPSolution, FLPInstance> {

    public static final int FREE_SPACE = Integer.MIN_VALUE;
    public static final double UNKNOWN_CENTER = -1.0;

    protected double cachedScore;
    protected int[][] rows;
    protected double[] center;
    protected final HashSet<Integer> notAssignedFacilities;
    protected final int[] rowSize;
    protected int assignedFacilities = 0;
//
//    protected List<Piece> pendingPieces = new ArrayList<>();
//
//    public List<Piece> getPendingPieces() {
//        return pendingPieces;
//    }

    public int[][] getRows() {
        return rows;
    }

    /**
     * Initialize solution from instance
     * @param instance
     */
    public FLPSolution(FLPInstance instance) {
        super(instance);
        int nFacilities = instance.nFacilities();
        // Each row can have at most N elements
        rows = new int[instance.nRows()][nFacilities];
        center = new double[nFacilities];
        Arrays.fill(center, UNKNOWN_CENTER);
        for (int i = 0; i < instance.nRows(); i++) {
            Arrays.fill(rows[i], FREE_SPACE);
        }
        rowSize = new int[instance.nRows()]; // all initial row sizes are 0

        // No facilities are assigned at the beginning
        notAssignedFacilities = Sets.newHashSetWithExpectedSize(nFacilities);
        for (int i = 0; i < nFacilities; i++) {
            notAssignedFacilities.add(i);
        }
    }

    /**
     * Clone constructor
     * @param s Solution to clone
     */
    public FLPSolution(FLPSolution s) {
        super(s);
        // Copy solution data
        rows = new int[s.rows.length][];
        for (int i = 0; i < rows.length; i++) {
            rows[i] = new int[s.rows[i].length];
            System.arraycopy(s.rows[i], 0, rows[i], 0, rows[i].length);
        }

//        this.pendingPieces = new ArrayList<>(s.pendingPieces);
        this.rowSize = s.rowSize.clone();
        this.center = s.center.clone();
        this.notAssignedFacilities = new HashSet<>(s.notAssignedFacilities);

        this.cachedScore = s.cachedScore;
        this.assignedFacilities = s.assignedFacilities;
    }

    public int nAssigned(){
        return this.assignedFacilities;
    }

    public void deassignAll(){
        for (int rowIdx = 0; rowIdx < this.rows.length; rowIdx++) {
            for (int j = 0, rowSize = this.rowSize(rowIdx); j < rowSize; j++) {
                assert this.rows[rowIdx][j] != FREE_SPACE;

                this.assignedFacilities--;
                this.rowSize[rowIdx]--;
                this.notAssignedFacilities.add(this.rows[rowIdx][j]);
                this.rows[rowIdx][j] = FREE_SPACE;
            }
        }
        this.cachedScore = 0;
        Arrays.fill(center, UNKNOWN_CENTER);
    }

    public Set<Integer> getNotAssignedFacilities() {
        return notAssignedFacilities;
    }

    public int rowSize(int rowIdx){
        return this.rowSize[rowIdx];
    }

    public double left(int rowIdx, int pos){
        if(pos == 0){
            return 0;
        }
        int f = rows[rowIdx][pos-1];
        return center[f] - getInstance().length(f) / 2.0;
    }

    public int nRows(){
        return getInstance().nRows();
    }

    boolean verifyCorrectSizes(){
        int totalSize = 0;
        for (int i = 0; i < this.nRows(); i++) {
            int rowSize = 0;
            while (this.rows[i][rowSize] != FREE_SPACE) {
                rowSize++;
            }

            if(rowSize != this.rowSize[i]){
                throw new AssertionError(String.format("Row %s size mismatch, expected %s, got %s", i, totalSize, this.rowSize[i]));
            }
            totalSize += rowSize;
        }
        if (totalSize != this.assignedFacilities){
            throw new AssertionError(String.format("Total size mismatch, expected %s, got %s", totalSize, this.assignedFacilities));
        }

        return true;
    }

    public void remove(int rowIndex, int i) {
        var rowData = this.rows[rowIndex];
        var f = rowData[i];
        assert verifyCorrectSizes();

        assert !this.notAssignedFacilities.contains(rowData[i]);
        ArrayUtil.remove(rowData, i);
        rowSize[rowIndex]--;
        this.assignedFacilities--;
        rowData[rowSize[rowIndex]] = FREE_SPACE;
        this.notAssignedFacilities.add(f);

        assert DoubleComparator.equals(this.cachedScore, this.recalculateScore()) : String.format("Score mismatch, expected %s cached is %s", this.recalculateScore(), this.cachedScore);
        assert this.notAssignedFacilities.contains(f);
        verifyCorrectSizes();
    }


    @Override
    public FLPSolution cloneSolution() {
        // Call clone constructor
        return new FLPSolution(this);
    }

    /**
     * Get the current solution score.
     * The difference between this method and recalculateScore is that
     * this result can be a property of the solution, or cached,
     * it does not have to be calculated each time this method is called
     * @return current solution score as double
     */
    public double getScore() {
        return this.cachedScore;
    }

    public double recalculateScore() {
        double[] centers = calculateCenters();
        return totalDistanceNew(getInstance(), this.rowSize, centers);
    }


    private static double totalDistanceNew(FLPInstance instance, int[] rowSize, int[][] data){
        double cost = 0;
        for (int first_row = 0; first_row < data.length; first_row++) {
            for (int first_pos = 0; first_pos < rowSize[first_row]; first_pos++) {
                var first_facility = data[first_row][first_pos];
                for (int second_row = 0; second_row < data.length; second_row++) {
                    for (int second_pos = 0; second_pos < rowSize[second_row]; second_pos++) {
                        var second_facility = data[second_row][second_pos];
                        cost += Math.abs(first_facility.lastCenter - second_facility.lastCenter) * instance.flow(first_facility.facility, second_facility.facility);
                    }
                }
            }
        }
        return cost / 2;
    }


    public double partialCost(final int row, final int index1, final int index2){
        var instance = getInstance();
        double total = 0;
        // todos los afectados cambia su coste, faltaria verificar que esto esta bien
        // ademas podria optimizarse mas seguramente
        for (int i = index1; i <= index2; i++) {
            for (int currentRow = 0; currentRow < rows.length; currentRow++) {
                for (int k = 0; k < this.rowSize[currentRow]; k++) {
                    final var b1 = rows[row][i];
                    final var b2 = rows[currentRow][k];
                    final double cost = Math.abs(b1.lastCenter - b2.lastCenter) * instance.flow(b1.facility, b2.facility);
                    if(currentRow == row && k >= index1 && k <= index2){
                        // Si esta dentro del rango modificado evitamos contar doble
                        total += cost / 2;
                    } else {
                        total += cost;
                    }
                }
            }
        }
        return total;
    }

    protected double[] calculateCenters() {
        var instance = getInstance();
        double[] c = new double[instance.nFacilities()];
        Arrays.fill(c, UNKNOWN_CENTER);
        for (int i = 0; i < this.rows.length; i++) {
            double left = 0;
            for (int j = 0; j < this.rowSize[i]; j++) {
                int facility = this.rows[i][j];
                int width = instance.length(facility);
                assert c[facility] == UNKNOWN_CENTER;
                c[facility] = left + width / 2.0;
                left += width;
            }
        }
        return c;
    }

    public static double evaluate(FLPInstance instance, int[][] facilitiesIds){
        double[][] centers = centers(instance, facilitiesIds);
        double cost = 0;
        for (int first_row = 0; first_row < facilitiesIds.length; first_row++) {
            for (int first_pos = 0; first_pos < facilitiesIds[first_row].length; first_pos++) {
                var first_facility = facilitiesIds[first_row][first_pos];
                for (int second_row = 0; second_row < facilitiesIds.length; second_row++) {
                    for (int second_pos = 0; second_pos < facilitiesIds[second_row].length; second_pos++) {
                        var second_facility = facilitiesIds[second_row][second_pos];
                        cost += Math.abs(centers[first_row][first_pos] - centers[second_row][second_pos]) * instance.flow(first_facility, second_facility);
                    }
                }
            }
        }
        return cost / 2;
    }

    /**
     * Generate a string representation of this solution. Used when printing progress to console,
     * show as minimal info as possible
     * @return Small string representing the current solution (Example: id + score)
     */
    @Override
    public String toString() {
        return "FLPSolution{" +
                "sc=" + cachedScore +
                '}';
    }

    /**
     * Update all facility centers from
     * @param rowIdx row to update
     * @param pos update centers from this position (included) until the end of the row
     */
    public void updateCentersFrom(int rowIdx, int pos){
        var instance = getInstance();
        var row = this.rows[rowIdx];
        double left = this.left(rowIdx, pos);
        for (int i = pos; i < this.rowSize[rowIdx]; i++) {
            var f = row[i];
            this.center[f] = left + instance.length(f) / 2.0;
            left += instance.length(f);
        }
    }

    public void insertLast(int rowIndex, int facility){
        assert DoubleComparator.equals(this.cachedScore, this.recalculateScore());
        assert this.notAssignedFacilities.contains(facility);

        int position = this.rowSize[rowIndex];
        double cost = this.insertCost(rowIndex, position, facility);

        cachedScore += cost;
        var row = rows[rowIndex];
        row[position] = facility;
        rowSize[rowIndex]++;
        this.assignedFacilities++;
        this.notAssignedFacilities.remove(facility);
        recalculateCentersInPlace(rowIndex);

        assert DoubleComparator.equals(this.cachedScore, this.recalculateScore()) : String.format("Score mismatch, expected %s cached is %s", this.recalculateScore(), this.cachedScore);
        assert !this.notAssignedFacilities.contains(facility);
    }
}
