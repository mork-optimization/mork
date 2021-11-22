package es.urjc.etsii.grafo.io.serializers.excel;

import es.urjc.etsii.grafo.solver.services.events.types.SolutionGeneratedEvent;
import es.urjc.etsii.grafo.solver.services.reference.ReferenceResultProvider;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Generates excel raw sheet with all data to use in pivot table and custom charts or other custom processin
 */
public abstract class RawSheetWriter {

    /**
     * Write data to raw sheet
     *
     * @param rawSheet sheet reference where data should be written to
     * @param maximizing true if this is a maximizing problem, false otherwise
     * @param results list of results to serialize
     * @param referenceResultProviders reference result providers if available
     * @return AreaReference specifying the area used in the sheet
     */
    public abstract AreaReference fillRawSheet(XSSFSheet rawSheet, boolean maximizing, List<? extends SolutionGeneratedEvent<?, ?>> results, List<ReferenceResultProvider> referenceResultProviders);

    /**
     * Convert nanoseconds to seconds
     *
     * @param nanos nanoseconds
     * @return seconds as a double value
     */
    protected static double nanoToSecs(long nanos) {
        return nanos / (double) 1_000_000_000;
    }

    /**
     * Transform NaNs and other special double values to valid values in Excel
     *
     * @param maximizing true if this is a maximizing problem, false otherwise
     * @param value value to transform
     * @return transformed value
     */
    protected static double nanInfiniteFilter(boolean maximizing, double value){
        if(Double.isFinite(value)){
            return value;
        }
        return maximizing ? ExcelSerializer.NEGATIVE_INFINITY : ExcelSerializer.POSITIVE_INFINITY;
    }

    /**
     * Calculate %Dev to reference value
     *
     * @param score value to check
     * @param bestValueForInstance reference valu
     * @return %Dev as a doube
     */
    protected static double getPercentageDevToBest(double score, double bestValueForInstance) {
        return Math.abs(score - bestValueForInstance) / bestValueForInstance;
    }

    /**
     * Write value to sheet cell
     *
     * @param cell cell where value will be written
     * @param d value to write
     * @param type hints how the value should be interpreted. May or may not be honored.
     */
    protected void writeCell(XSSFCell cell, Object d, CType type) {
        switch (type){
            case FORMULA:
                if(! (d instanceof String)){
                    throw new IllegalArgumentException("Trying to set cell as formula but not a String: " + d);
                }
                cell.setCellFormula((String) d);
                break;
            case ARRAY_FORMULA:
                if(! (d instanceof String)){
                    throw new IllegalArgumentException("Trying to set cell as formula but not a String: " + d);
                }
                String[] parts = ((String) d).split("·");
                if(parts.length != 2){
                    throw new IllegalArgumentException("Invalid setArrayFormula: " + d);
                }
                cell.getSheet().setArrayFormula(parts[0], CellRangeAddress.valueOf(parts[1]));
                break;
            case VALUE:
                if (d instanceof Double) {
                    cell.setCellValue((double) d);
                } else if (d instanceof Integer) {
                    cell.setCellValue((int) d);
                } else if (d instanceof String) {
                    cell.setCellValue((String) d);
                } else {
                    throw new IllegalArgumentException("Invalid datatype");
                }
                break;
        }
    }


    /**
     * Get best result for a given instance
     *
     * @param results our results
     * @param providers reference values
     * @param maximizing true if this is a maximizing problem, false otherwise
     * @return best value known for a given instance
     */
    protected static Map<String, Double> bestResultPerInstance(List<? extends SolutionGeneratedEvent<?, ?>> results, List<ReferenceResultProvider> providers, boolean maximizing) {
        Map<String, Double> ourBestValuePerInstance = results
                .stream()
                .collect(Collectors.toMap(
                        SolutionGeneratedEvent::getInstanceName,
                        SolutionGeneratedEvent::getScore,
                        (a, b) -> maximizing ? Math.max(a, b) : Math.min(a, b)
                ));

        Map<String, Double> bestValuePerInstance = new HashMap<>();

        for(var instance: ourBestValuePerInstance.keySet()){
            double best = ourBestValuePerInstance.get(instance);
            for(var reference: providers){
                var optionalValue = reference.getValueFor(instance).getScore();
                if(maximizing){
                    best = Math.max(best, optionalValue.orElse(ExcelSerializer.NEGATIVE_INFINITY));
                } else {
                    best = Math.min(best, optionalValue.orElse(ExcelSerializer.POSITIVE_INFINITY));
                }
            }
            bestValuePerInstance.put(instance, best);
        }

        return bestValuePerInstance;
    }

    /**
     * Value types to use as hint when serializing to Excel cells.
     * Necessary because for example we cannot determine if a string is a formula,
     * an array formula or should be interpreted as a literal
     */
    protected enum CType {
        /**
         * Serialize as value
         */
        VALUE,

        /**
         * Serialize as a normal formula
         */
        FORMULA,

        /**
         * Serialize as an array formula
         */
        ARRAY_FORMULA
    }
}
