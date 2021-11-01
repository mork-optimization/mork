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


public abstract class RawSheetWriter {

    public abstract AreaReference fillRawSheet(XSSFSheet rawSheet, boolean maximizing, List<? extends SolutionGeneratedEvent<?, ?>> results, List<ReferenceResultProvider> referenceResultProviders);

    protected static double nanoToSecs(long nanos) {
        return nanos / (double) 1_000_000_000;
    }

    protected static double nanInfiniteFilter(boolean maximizing, double value){
        if(Double.isFinite(value)){
            return value;
        }
        return maximizing ? ExcelSerializer.NEGATIVE_INFINITY : ExcelSerializer.POSITIVE_INFINITY;
    }

    protected static double getPercentageDevToBest(double score, double bestValueForInstance) {
        return Math.abs(score - bestValueForInstance) / bestValueForInstance;
    }

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

    protected enum CType {
        VALUE,
        FORMULA,
        ARRAY_FORMULA
    }
}
