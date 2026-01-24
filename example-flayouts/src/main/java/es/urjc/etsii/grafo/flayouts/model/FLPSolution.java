package es.urjc.etsii.grafo.flayouts.model;

import es.urjc.etsii.grafo.flayouts.constructives.tetris.Piece;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.util.ArrayUtil;
import es.urjc.etsii.grafo.util.DoubleComparator;

import java.util.*;

public class FLPSolution extends Solution<FLPSolution, FLPInstance> {

    protected double cachedScore;
    // Rows * Position
    protected Facility[][] solutionData;
    protected final HashSet<Facility> notAssignedFacilities;
    protected final int[] rowSize;
    protected int nFakeFacilities = 0;
    protected int assignedFacilities = 0;
    protected int lastFakeId = -1;

    protected Map<Double, List<Facility>> fakeFacilities;

    protected List<Piece> pendingPieces = new ArrayList<>();

    public List<Piece> getPendingPieces() {
        return pendingPieces;
    }

    public Facility[][] getSolutionData() {
        return solutionData;
    }

    /**
     * Initialize solution from instance
     * @param instance
     */
    public FLPSolution(FLPInstance instance) {
        super(instance);
        // Each row can have at most N elements
        solutionData = new Facility[instance.nRows()][instance.getNRealFacilities()];
        rowSize = new int[instance.nRows()];
        // No facilities are assigned at start
        notAssignedFacilities = new HashSet<>(instance.getFacilities());
        fakeFacilities = new HashMap<>();
    }

    /**
     * Clone constructor
     * @param s Solution to clone
     */
    public FLPSolution(FLPSolution s) {
        super(s);
        // Copy solution data
        solutionData = new Facility[s.solutionData.length][];
        for (int i = 0; i < solutionData.length; i++) {
            solutionData[i] = new Facility[s.solutionData[i].length];
            for (int j = 0; j < solutionData[i].length; j++) {
                if(s.solutionData[i][j] != null){
                    solutionData[i][j] = new Facility(s.solutionData[i][j]);
                }
            }
        }

        this.pendingPieces = new ArrayList<>(s.pendingPieces);
        this.nFakeFacilities = s.nFakeFacilities;
        this.rowSize = s.rowSize.clone();
        this.notAssignedFacilities = new HashSet<>(s.notAssignedFacilities);

        // Deep clone fake facilities
        this.fakeFacilities = new HashMap<>();
        s.fakeFacilities.forEach((k, v) -> this.fakeFacilities.put(k, new ArrayList<>(v)));

        this.cachedScore = s.cachedScore;
        this.lastFakeId = s.lastFakeId;
        this.assignedFacilities = s.assignedFacilities;
    }

    public int getNAssignedFacilities(){
        return this.assignedFacilities;
    }

    public Map<Double, List<Facility>> getFakeFacilities() {
        return fakeFacilities;
    }

    public Facility[] addFakeFacilities(double[] widths){
        assert Arrays.stream(widths).allMatch(DoubleComparator::isPositive);

        // Resize data structures to accommodate worst case (all fakes in same row)
        this.nFakeFacilities += widths.length;
        for (int i = 0; i < this.solutionData.length; i++) {
            this.solutionData[i] = Arrays.copyOf(this.solutionData[i], this.solutionData[i].length + widths.length);
        }

        var fakeFacilities = new Facility[widths.length];
        for (int i = 0; i < widths.length; i++) {
            fakeFacilities[i] = new Facility(lastFakeId--, widths[i], true);
            this.fakeFacilities.computeIfAbsent(widths[i], (k) -> new ArrayList<>()).add(fakeFacilities[i]);
        }
        this.notAssignedFacilities.addAll(Arrays.asList(fakeFacilities));
        return fakeFacilities;
    }

    public void deassignAll(){
        for (int rowId = 0; rowId < this.solutionData.length; rowId++) {
            for (int j = 0, rowSize = this.getRowSize(rowId); j < rowSize; j++) {
                this.assignedFacilities--;
                this.rowSize[rowId]--;
                this.notAssignedFacilities.add(this.solutionData[rowId][j].facility);
                this.solutionData[rowId][j] = null;
            }
        }
        this.rebuildCaches();
    }

    public void deassignAllFakeFacilities(){
        this.fakeFacilities = new HashMap<>();
        for (int rowId = 0; rowId < this.solutionData.length; rowId++) {
            for (int j = 0, j2 = 0, rowSize = this.getRowSize(rowId); j < rowSize; j++, j2++) {
                if(this.solutionData[rowId][j].facility.fake){
                    j2--;
                    this.assignedFacilities--;
                    this.rowSize[rowId]--;
                    this.notAssignedFacilities.add(this.solutionData[rowId][j].facility);
                } else {
                    this.solutionData[rowId][j2] = this.solutionData[rowId][j];
                }
            }
        }
        this.rebuildCaches();
    }

    public Set<Facility> getNotAssignedFacilities() {
        return Collections.unmodifiableSet(notAssignedFacilities);
    }

    public int getRowSize(int rowIndex){
        return this.rowSize[rowIndex];
    }

    public int[] getRowSizes(){
        return this.rowSize;
    }

    public int getNRows(){
        return getInstance().nRows();
    }

    private void assertCorrectSizes(){
        assert this.assignedFacilities == ArrayUtil.sum(this.rowSize) : String.format("Size mismatch: Ref %s, sum %s", this.assignedFacilities, ArrayUtil.sum(this.rowSize));
        assert this.assignedFacilities == Arrays.stream(this.solutionData).flatMap(Arrays::stream).filter(Objects::nonNull).count();
    }

    public void remove(int rowIndex, int i) {
        var rowData = this.solutionData[rowIndex];
        var f = rowData[i];
        assertCorrectSizes();

        // Validation removed because it is not expected to fulfill this requirement.
        //assert DoubleComparator.equals(this.cachedScore, this.recalculateScore());

        assert !this.notAssignedFacilities.contains(rowData[i].facility);
        ArrayUtil.remove(rowData, i);
        rowSize[rowIndex]--;
        this.assignedFacilities--;
        rowData[rowSize[rowIndex]] = null;
        this.notAssignedFacilities.add(f.facility);

        this.rebuildCaches();
        assert DoubleComparator.equals(this.cachedScore, this.recalculateScore()) : String.format("Score mismatch, expected %s cached is %s", this.recalculateScore(), this.cachedScore);
        assert this.notAssignedFacilities.contains(f.facility);
        assertCorrectSizes();
    }


    public static class RowIndex {
        public final int row, index;

        public RowIndex(int row, int index) {
            this.row = row;
            this.index = index;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RowIndex rowIndex = (RowIndex) o;
            return row == rowIndex.row && index == rowIndex.index;
        }

        @Override
        public int hashCode() {
            return Objects.hash(row, index);
        }

        @Override
        public String toString() {
            return "RowIndex{" +
                    "r=" + row +
                    ", i=" + index +
                    '}';
        }
    }

    public RowIndex getRowIndexForPosition(int position){
        assert position < this.allFacilitiesSize() : "Position >= nElements";
        int row = 0;
        while (position >= this.rowSize[row]){
            position -= this.rowSize[row];
            row++;
            assert row < this.getNRows(): "Calculated Row Index >= nRows";
        }
        return new RowIndex(row, position);
    }

    public int getPositionForRowIndex(int row, int index){
        int total = 0;
        for (int i = 0; i < row; i++) {
            total += this.rowSize[i];
        }
        total += index;
        return total;
    }

    public int allFacilitiesSize(){
        int count = 0;
        for (int j : rowSize) {
            count += j;
        }
        return count;
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

    /**
     * Recalculate solution score and validate current solution state
     * You must check that no constraints are broken, and that all costs are valid
     * The difference between this method and getScore is that we must recalculate the score from scratch,
     * without using any cache/shortcuts.
     * DO NOT UPDATE CACHES / MAKE SURE THIS METHOD DOES NOT HAVE SIDE EFFECTS
     * @return current solution score as double
     */
    public double recalculateScore() {
        var centers = recalculateCentersCopy();
        return totalDistanceNew(getInstance(), this.rowSize, centers);
    }

    public void rebuildCaches(){
        this.solutionData = recalculateCentersCopy();
        this.cachedScore = totalDistanceNew(getInstance(), this.rowSize, this.solutionData);
    }

    private static double totalDistanceNew(FLPInstance instance, int[] rowSize, Facility[][] data){
        double cost = 0;
        for (int first_row = 0; first_row < data.length; first_row++) {
            for (int first_pos = 0; first_pos < rowSize[first_row]; first_pos++) {
                var first_facility = data[first_row][first_pos];
                for (int second_row = 0; second_row < data.length; second_row++) {
                    for (int second_pos = 0; second_pos < rowSize[second_row]; second_pos++) {
                        var second_facility = data[second_row][second_pos];
                        cost += Math.abs(first_facility.lastCenter - second_facility.lastCenter) * instance.c(first_facility.facility, second_facility.facility);
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
            for (int currentRow = 0; currentRow < solutionData.length; currentRow++) {
                for (int k = 0; k < this.rowSize[currentRow]; k++) {
                    final var b1 = solutionData[row][i];
                    final var b2 = solutionData[currentRow][k];
                    final double cost = Math.abs(b1.lastCenter - b2.lastCenter) * instance.c(b1.facility, b2.facility);
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

    protected Facility[][] recalculateCentersCopy() {
        var positions = new Facility[this.solutionData.length][];
        for (int i = 0; i < this.solutionData.length; i++) {
            positions[i] = recalculateCentersCopy(i);
        }
        return positions;
    }

    public static double evaluate(FLPInstance instance, int[][] facilitiesIds){
        return evaluate(instance, facilitiesIds, 0);
    }

    public static double evaluate(FLPInstance instance, int[][] facilitiesIds, double fakeWidth){
        double[][] centers = centers(instance, facilitiesIds, fakeWidth);
        double cost = 0;
        for (int first_row = 0; first_row < facilitiesIds.length; first_row++) {
            for (int first_pos = 0; first_pos < facilitiesIds[first_row].length; first_pos++) {
                var first_facility = facilitiesIds[first_row][first_pos];
                for (int second_row = 0; second_row < facilitiesIds.length; second_row++) {
                    for (int second_pos = 0; second_pos < facilitiesIds[second_row].length; second_pos++) {
                        var second_facility = facilitiesIds[second_row][second_pos];
                        cost += Math.abs(centers[first_row][first_pos] - centers[second_row][second_pos]) * instance.c(first_facility, second_facility);
                    }
                }
            }
        }
        return cost / 2;
    }

    public static double[][] centers(FLPInstance instance, int[][] facilitiesIds, double fakeWidth){
        double[][] centers = new double[facilitiesIds.length][];
        for (int i = 0; i < facilitiesIds.length; i++) {
            int size = facilitiesIds[i].length;
            centers[i] = new double[size];
            double leftDistance = 0;
            for (int j = 0; j < size; j++) {
                int id = facilitiesIds[i][j];
                double width = id >=0? instance.byId(id).width: fakeWidth;
                double newCenter = leftDistance + width / (double) 2;
                centers[i][j] = newCenter;
                leftDistance += width;
            }
        }
        return centers;
    }

    private Facility[] recalculateCentersCopy(int rowIndex){
        double leftDistance = 0;
        var size = this.rowSize[rowIndex];
        var row = solutionData[rowIndex];
        var newRow = new Facility[getInstance().getNRealFacilities() + this.nFakeFacilities];

        for (int i = 0; i < size; i++) {
            var position = this.solutionData[rowIndex][i];
            double newCenter = leftDistance + position.facility.width / (double) 2;
            newRow[i] = new Facility(row[i].facility, newCenter);
            leftDistance += position.facility.width;
        }

        return newRow;
    }

    public void recalculateCentersInPlace(int rowindex){
        double leftDistance = 0;
        var row = solutionData[rowindex];
        int size = this.rowSize[rowindex];
        for (int i = 0; i < size; i++) {
            var position = row[i];
            position.lastCenter = leftDistance + position.facility.width / (double) 2;
            leftDistance += position.facility.width;
        }
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

    public double insertCost(int rowIndex, int index1, Facility f){
        int tope = rowSize[rowIndex];
        var row = this.solutionData[rowIndex];

        // Antes de hacer el movimiento
        assert equalsSolutionData(recalculateCentersCopy());
        double before = partialCost(rowIndex, index1, tope-1);

        // Do movement
        System.arraycopy(row, index1, row, index1+1, rowSize[rowIndex]-index1);
        row[index1] = f;
        rowSize[rowIndex]++;
        recalculateCentersInPlace(rowIndex);

        // Despues de hacer el movimiento
        double after = partialCost(rowIndex, index1, tope);

        // Deshacemos el movimiento
        rowSize[rowIndex]--;
        System.arraycopy(row, index1+1, row, index1, rowSize[rowIndex]-index1); // Todo, no faltaria un -1 al length?
        row[rowSize[rowIndex]] = null;
        recalculateCentersInPlace(rowIndex);

        // Al deshacer el coste deberia quedar igual
        assert DoubleComparator.equals(before, partialCost(rowIndex, index1, tope-1));

        return after - before;
    }

    public void insert(int rowIndex, int index1, double cost, Facility box){
        assert DoubleComparator.equals(this.cachedScore, this.recalculateScore());
        assert this.notAssignedFacilities.contains(box.facility);
        assertCorrectSizes();

        cachedScore += cost;
        var row = solutionData[rowIndex];
        // Desplazamos hacia la derecha 1 posicion la longitud justa y necesaria.
        // Si el array estuviera completo deberia fallar
        System.arraycopy(row, index1, row, index1+1, rowSize[rowIndex]-index1);
        row[index1] = box;
        rowSize[rowIndex]++;
        this.assignedFacilities++;
        this.notAssignedFacilities.remove(box.facility);
        recalculateCentersInPlace(rowIndex);

        assert DoubleComparator.equals(this.cachedScore, this.recalculateScore()) : String.format("Score mismatch, expected %s cached is %s", this.recalculateScore(), this.cachedScore);
        assert !this.notAssignedFacilities.contains(box.facility);
        assertCorrectSizes();
    }

    public void insertLast(int rowIndex, Facility facility){
        assert DoubleComparator.equals(this.cachedScore, this.recalculateScore());
        assert this.notAssignedFacilities.contains(facility);

        int position = this.rowSize[rowIndex];
        var facilityPosition = new Facility(facility);
        double cost = this.insertCost(rowIndex, position, facilityPosition);

        cachedScore += cost;
        var row = solutionData[rowIndex];
        row[position] = facilityPosition;
        rowSize[rowIndex]++;
        this.assignedFacilities++;
        this.notAssignedFacilities.remove(facility);
        recalculateCentersInPlace(rowIndex);

        assert DoubleComparator.equals(this.cachedScore, this.recalculateScore()) : String.format("Score mismatch, expected %s cached is %s", this.recalculateScore(), this.cachedScore);
        assert !this.notAssignedFacilities.contains(facility);
    }

    public boolean equalsSolutionData(Facility[][] data){
        if(this.solutionData.length != data.length){
            return false;
        }
        for (int i = 0; i < data.length; i++) {
            if(this.solutionData[i].length != data[i].length){
                return false;
            }
            //for (int j = 0; j < data[i].length; j++) {
            for (int j = 0; j < rowSize[i]; j++) {
                if(data[i][j] == null && this.solutionData[i][j] == null){
                    continue;
                }
                if(data[i][j] == null || this.solutionData[i][j] == null || !data[i][j].equals(this.solutionData[i][j])){
                    return false;
                }
            }
        }
        return true;
    }

}
