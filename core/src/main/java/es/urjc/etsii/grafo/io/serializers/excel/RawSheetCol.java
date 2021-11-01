package es.urjc.etsii.grafo.io.serializers.excel;

import static org.apache.poi.ss.util.CellReference.convertNumToColString;

public enum RawSheetCol {
    INSTANCE_NAME(0, "Instance Name", RawSheetWriter.CType.VALUE),
    ALG_NAME(1, "Algorithm Name", RawSheetWriter.CType.VALUE),
    ITERATION(2, "Iteration", RawSheetWriter.CType.VALUE),
    SCORE(3, "Score", RawSheetWriter.CType.VALUE),
    TOTAL_TIME(4, "Total Time (s)", RawSheetWriter.CType.VALUE),
    TTB(5, "Time to Best (s)", RawSheetWriter.CType.VALUE),
    IS_BEST_KNOWN(6, "Is Best Known?", RawSheetWriter.CType.FORMULA),
    DEV_TO_BEST(7, "% Dev. to best known", RawSheetWriter.CType.FORMULA),
    BEST_KNOWN_FOR_INSTANCE(8, "Best value known", RawSheetWriter.CType.ARRAY_FORMULA);

    private final int index;
    private final String name;
    private final RawSheetWriter.CType type;

    RawSheetCol(int index, String name, RawSheetWriter.CType type) {
        this.index = index;
        this.name = name;
        this.type = type;
    }

    public int getIndex() {
        return index;
    }

    public String getExcelColIndex(){
        return convertNumToColString(this.getIndex());
    }

    public String getName() {
        return name;
    }

    public RawSheetWriter.CType getCType(){
        return type;
    }

    public static RawSheetCol getForIndex(int index){
        for(var i: RawSheetCol.values()){
            if(i.getIndex() == index){
                return i;
            }
        }
        throw new IllegalArgumentException(String.format("Invalid index: %s, not declared", index));
    }
}
