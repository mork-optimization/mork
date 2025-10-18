package es.urjc.etsii.grafo.io.serializers.excel;

import static org.apache.poi.ss.util.CellReference.convertNumToColString;

/**
 * Column types in raw sheet
 */
public enum RawSheetCol {
    /**
     * Instance name
     */
    INSTANCE_NAME(0, "Instance Name", ExcelSerializer.CType.VALUE),

    /**
     * Algorithm name
     */
    ALG_NAME(1, "Algorithm Name", ExcelSerializer.CType.VALUE),

    /**
     * Iteration
     */
    ITERATION(2, "Iteration", ExcelSerializer.CType.VALUE),

    /**
     * Score
     */
    SCORE(3, "Score", ExcelSerializer.CType.VALUE),

    /**
     * Total time in seconds
     */
    TOTAL_TIME(4, "Total Time (s)", ExcelSerializer.CType.VALUE),

    /**
     * Time to best in seconds
     */
    TTB(5, "Time to Best (s)", ExcelSerializer.CType.VALUE),

    /**
     * True if score is best known for instance
     */
    IS_BEST_KNOWN(6, "Is Best Known?", ExcelSerializer.CType.FORMULA),

    /**
     * %Dev to best value. Should be 0 if is best known.
     */
    DEV_TO_BEST(7, "% Dev. to best known", ExcelSerializer.CType.FORMULA),

    /**
     * Best known value for instance name in same row
     */
    BEST_KNOWN_FOR_INSTANCE(8, "Best value known", ExcelSerializer.CType.FORMULA);

    private final int index;
    private final String name;
    private final ExcelSerializer.CType type;

    /**
     * Fields for RawSheetCol
     * @param index column index
     * @param name column name
     * @param type column type hint
     */
    RawSheetCol(int index, String name, ExcelSerializer.CType type) {
        this.index = index;
        this.name = name;
        this.type = type;
    }

    /**
     * Get column index for ordering
     *
     * @return column index as int
     */
    public int getIndex() {
        return index;
    }

    /**
     * Get column index as Excel letters
     *
     * @return column index as string (i.e AB)
     */
    public String getExcelColIdx(){
        return convertNumToColString(this.getIndex());
    }

    /**
     * Get column name
     *
     * @return column name
     */
    public String getName() {
        return name;
    }

    /**
     * Get column type hint
     *
     * @return column type hint
     */
    public ExcelSerializer.CType getCType(){
        return type;
    }

    /**
     * Get enum value for a given column index
     *
     * @param index index to search
     * @return enum value for given index
     */
    public static ExcelSerializer.CType getCTypeForIndex(int index){
        var values = RawSheetCol.values();
        if(index >= values.length){
            return ExcelSerializer.CType.VALUE;
        }
        for(var i: values){
            if(i.getIndex() == index){
                return i.getCType();
            }
        }
        throw new IllegalArgumentException(String.format("Invalid index: %s, not declared", index));
    }
}
