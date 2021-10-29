package es.urjc.etsii.grafo.io.serializers;

import es.urjc.etsii.grafo.solver.configuration.ExcelSerializerConfiguration;
import es.urjc.etsii.grafo.solver.services.events.types.SolutionGeneratedEvent;
import es.urjc.etsii.grafo.solver.services.reference.ReferenceResultProvider;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.DataConsolidateFunction;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.apache.poi.ss.util.CellReference.convertNumToColString;


public class ExcelSerializer extends ResultsSerializer {

    public static final String RAW_SHEET = "Raw Results";
    public static final String PIVOT_SHEET = "Pivot Table";

    public static final double POSITIVE_INFINITY = 1e99;
    public static final double NEGATIVE_INFINITY = -1e99;

    private final boolean maximizing;
    private final List<ReferenceResultProvider> referenceResultProviders;
    private final ExcelSerializerConfiguration config;

    public ExcelSerializer(
            ExcelSerializerConfiguration config,
            @Value("${solver.maximizing}") boolean maximizing,
            List<ReferenceResultProvider> referenceResultProviders
    ) {
        super(config.isEnabled(), config.getFolder(), config.getFormat());
        this.config = config;
        this.maximizing = maximizing;
        this.referenceResultProviders = referenceResultProviders;
    }

    private static double nanoToSecs(long nanos) {
        return nanos / (double) 1_000_000_000;
    }

    public void _serializeResults(List<? extends SolutionGeneratedEvent<?, ?>> results, Path p) {
        log.info("Exporting result data to XLSX...");

        File f = p.toFile();
        try (
                var outputStream = new FileOutputStream(f);
                var excelBook = new XSSFWorkbook();
        ) {
            var pivotSheet = excelBook.createSheet(PIVOT_SHEET);
            var rawSheet = excelBook.createSheet(RAW_SHEET);

            var area = fillRawSheetWithFormula(rawSheet, results);
            fillPivotSheet(pivotSheet, area, rawSheet);
            log.info("Calculating formulas...");
            excelBook.getCreationHelper().createFormulaEvaluator().evaluateAll();
            log.info("Writing to disk...");
            excelBook.write(outputStream);
        } catch (Exception e) {
            throw new RuntimeException(String.format("Exception while trying to save Excel file: %s, reason: %s", f.getAbsolutePath(), e.getClass().getSimpleName()), e.getCause());
        }
    }

    private void fillPivotSheet(XSSFSheet pivotSheet, AreaReference sourceDataArea, XSSFSheet source) {
        // Generate tables like
        /*                  ____________________________________________________________________________________
         *  ______________ |                Algorithm 1              |                Algorithm 2              |
         * | Instance name | Best Value | isBest | %Dev | Total t(s) | Best Value | isBest | %Dev | Total t(s) |
         * | Instance1     |            |        |      |            |            |        |      |            |
         * | Instance1     |            |        |      |            |            |        |      |            |
           .....................................................................................................
         *  etc
         */

        var pivotTable = pivotSheet.createPivotTable(sourceDataArea, new CellReference(0, 0), source);

        var ctptd = pivotTable.getCTPivotTableDefinition();
        ctptd.setColGrandTotals(config.isColumnGrandTotal());
        ctptd.setRowGrandTotals(config.isRowGrandTotal());

        if (config.isAlgorithmsInColumns()) {
            pivotTable.addRowLabel(__.INSTANCE_NAME.getIndex()); // Instances label in rows
            pivotTable.addColLabel(__.ALG_NAME.getIndex());      // Algorithm labels in columns
        } else {
            pivotTable.addColLabel(__.INSTANCE_NAME.getIndex()); // Instances label in columns
            pivotTable.addRowLabel(__.ALG_NAME.getIndex());      // Algorithm labels in rows
        }

        if (maximizing) {
            pivotTable.addColumnLabel(DataConsolidateFunction.MAX, __.SCORE.getIndex(), "Max. score");
        } else {
            pivotTable.addColumnLabel(DataConsolidateFunction.MIN, __.SCORE.getIndex(), "Min. score");
        }

        // Optional fields
        if(config.isAvgScoreEnabled()){
            pivotTable.addColumnLabel(DataConsolidateFunction.AVERAGE, __.SCORE.getIndex(), "Avg. score");
        }
        if(config.isStdScoreEnabled()){
            pivotTable.addColumnLabel(DataConsolidateFunction.STD_DEVP, __.SCORE.getIndex(), "STDDEVP score");
        }

        if(config.isVarScoreEnabled()){
            pivotTable.addColumnLabel(DataConsolidateFunction.VARP, __.SCORE.getIndex(), "VARP Score");
        }

        if(config.isAvgTimeEnabled()){
            pivotTable.addColumnLabel(DataConsolidateFunction.AVERAGE, __.TOTAL_TIME.getIndex(), "Avg. Total T(s)");
        }

        if(config.isTotalTimeEnabled()){
            pivotTable.addColumnLabel(DataConsolidateFunction.SUM, __.TOTAL_TIME.getIndex(), "Sum Total T(s)");
        }

        if(config.isAvgTTBEnabled()){
            pivotTable.addColumnLabel(DataConsolidateFunction.AVERAGE, __.TTB.getIndex(), "Avg. TTB(s)");
        }

        if(config.isTotalTTBEnabled()){
            pivotTable.addColumnLabel(DataConsolidateFunction.SUM, __.TTB.getIndex(), "Sum TTB(s)");
        }

        if(config.isSumBestKnownEnabled()){
            pivotTable.addColumnLabel(DataConsolidateFunction.SUM, __.IS_BEST_KNOWN.getIndex(), "#Best");
        }

        if(config.isHasBestKnownEnabled()){
            pivotTable.addColumnLabel(DataConsolidateFunction.MAX, __.IS_BEST_KNOWN.getIndex(), "hasBest");
        }

        if(config.isMinDevToBestKnownEnabled()){
            pivotTable.addColumnLabel(DataConsolidateFunction.MIN, __.DEV_TO_BEST.getIndex(), "Min. %Dev2Best");
        }

        if(config.isAvgDevToBestKnownEnabled()){
            pivotTable.addColumnLabel(DataConsolidateFunction.AVERAGE, __.DEV_TO_BEST.getIndex(), "Avg. %Dev2Best");
        }
    }

    private static double nanInfiniteFilter(boolean maximizing, double value){
        if(Double.isFinite(value)){
            return value;
        }
        return maximizing ? NEGATIVE_INFINITY : POSITIVE_INFINITY;
    }


    private AreaReference fillRawSheetWithFormula(XSSFSheet rawSheet, List<? extends SolutionGeneratedEvent<?, ?>> results) {
        // Best values per instance
        Map<String, Double> bestValuesPerInstance = bestResultPerInstance(results, referenceResultProviders, maximizing);

        // Create headers
        String[] header = new String[]{
                __.INSTANCE_NAME.getName(),
                __.ALG_NAME.getName(),
                __.ITERATION.getName(),
                __.SCORE.getName(),
                __.TOTAL_TIME.getName(),
                __.TTB.getName(),
                __.IS_BEST_KNOWN.getName(),
                __.DEV_TO_BEST.getName(),
                __.BEST_KNOWN_FOR_INSTANCE.getName()
        };

        int nColumns = header.length;
        int cutOff = results.size() + 1;
        int rowsForProvider = this.referenceResultProviders.size() * bestValuesPerInstance.keySet().size();
        int nRows = cutOff + rowsForProvider;

        // Create matrix data
        Object[][] data = new Object[nRows][nColumns];
        data[0] = header;

        for (int i = 1; i < cutOff; i++) {
            var r = results.get(i - 1);

            data[i][__.INSTANCE_NAME.getIndex()] = r.getInstanceName();
            data[i][__.ALG_NAME.getIndex()] = r.getAlgorithmName();
            data[i][__.ITERATION.getIndex()] = r.getIteration();
            data[i][__.SCORE.getIndex()] = r.getScore();
            data[i][__.TOTAL_TIME.getIndex()] = nanoToSecs(r.getExecutionTime());
            data[i][__.TTB.getIndex()] = nanoToSecs(r.getTimeToBest());
            // Example: =MIN(FILTER(D:D, A:A=A2))
            int excelRowIndex = i + 1; // Current row +1 because Excel starts indexing rows on 1.
//            data[i][__.BEST_KNOWN_FOR_INSTANCE.getIndex()] = String.format("%s(FILTER(%2$s:%2$s, %3$s:%3$s=%3$s%4$s))", maximizing ? "MAX" : "MIN", __.SCORE.getExcelColIndex(),  __.INSTANCE_NAME.getExcelColIndex(), excelRowIndex);
//            // Example: =IF(D2=L2,1,0) with L2 best known for instance and D2 current score
//            data[i][__.IS_BEST_KNOWN.getIndex()] = String.format("IF(%s%s=%s%s,1,0)", __.BEST_KNOWN_FOR_INSTANCE.getExcelColIndex(), excelRowIndex, __.SCORE.getExcelColIndex(), excelRowIndex);
//            data[i][__.DEV_TO_BEST.getIndex()] = String.format("ABS(%s%s-%s%s)/%s%s", __.SCORE.getExcelColIndex(), excelRowIndex, __.BEST_KNOWN_FOR_INSTANCE.getExcelColIndex(), excelRowIndex, __.BEST_KNOWN_FOR_INSTANCE.getExcelColIndex(), excelRowIndex);

            data[i][__.BEST_KNOWN_FOR_INSTANCE.getIndex()] = String.format("%s(IF(%3$s:%3$s=%3$s%4$s,%2$s:%2$s))·%5$s%4$s", maximizing ? "MAX" : "MIN", __.SCORE.getExcelColIndex(),  __.INSTANCE_NAME.getExcelColIndex(), excelRowIndex, __.BEST_KNOWN_FOR_INSTANCE.getExcelColIndex());
            // Example: =IF(D2=L2,1,0) with L2 best known for instance and D2 current score
            data[i][__.IS_BEST_KNOWN.getIndex()] = String.format("IF(%s%s=%s%s,1,0)", __.BEST_KNOWN_FOR_INSTANCE.getExcelColIndex(), excelRowIndex, __.SCORE.getExcelColIndex(), excelRowIndex);
            data[i][__.DEV_TO_BEST.getIndex()] = String.format("ABS(%s%s-%s%s)/%s%s", __.SCORE.getExcelColIndex(), excelRowIndex, __.BEST_KNOWN_FOR_INSTANCE.getExcelColIndex(), excelRowIndex, __.BEST_KNOWN_FOR_INSTANCE.getExcelColIndex(), excelRowIndex);
        }

        int currentRow = cutOff;
        for(String instanceName: bestValuesPerInstance.keySet()){
            for(var provider: referenceResultProviders){
                var result = provider.getValueFor(instanceName);
                double score = result.getScoreOrNan();

                data[currentRow][__.INSTANCE_NAME.getIndex()] = instanceName;
                data[currentRow][__.ALG_NAME.getIndex()] = provider.getProviderName();
                data[currentRow][__.ITERATION.getIndex()] = 0;
                data[currentRow][__.SCORE.getIndex()] = nanInfiniteFilter(maximizing, score);
                data[currentRow][__.TOTAL_TIME.getIndex()] = nanInfiniteFilter(false, result.getTimeInSeconds());
                data[currentRow][__.TTB.getIndex()] = nanInfiniteFilter(false, result.getTimeToBestInSeconds());

                // Example: =MIN(FILTER(D:D, A:A=A2))
                int excelRowIndex = currentRow + 1; // Current row +1 because Excel starts indexing rows on 1.
                data[currentRow][__.BEST_KNOWN_FOR_INSTANCE.getIndex()] = String.format("%s(IF(%3$s:%3$s=%3$s%4$s,%2$s:%2$s))·%5$s%4$s", maximizing ? "MAX" : "MIN", __.SCORE.getExcelColIndex(),  __.INSTANCE_NAME.getExcelColIndex(), excelRowIndex, __.BEST_KNOWN_FOR_INSTANCE.getExcelColIndex());
                // Example: =IF(D2=L2,1,0) with L2 best known for instance and D2 current score
                data[currentRow][__.IS_BEST_KNOWN.getIndex()] = String.format("IF(%s%s=%s%s,1,0)", __.BEST_KNOWN_FOR_INSTANCE.getExcelColIndex(), excelRowIndex, __.SCORE.getExcelColIndex(), excelRowIndex);
                data[currentRow][__.DEV_TO_BEST.getIndex()] = String.format("ABS(%s%s-%s%s)/%s%s", __.SCORE.getExcelColIndex(), excelRowIndex, __.BEST_KNOWN_FOR_INSTANCE.getExcelColIndex(), excelRowIndex, __.BEST_KNOWN_FOR_INSTANCE.getExcelColIndex(), excelRowIndex);
//                data[currentRow][__.BEST_KNOWN_FOR_INSTANCE.getIndex()] = String.format("%s(FILTER(%2$s:%2$s, %3$s:%3$s=%3$s%4$s))", maximizing ? "MAX" : "MIN", __.SCORE.getExcelColIndex(),  __.INSTANCE_NAME.getExcelColIndex(), excelRowIndex);
//                // Example: =IF(D2=L2,1,0) with L2 best known for instance and D2 current score
//                data[currentRow][__.IS_BEST_KNOWN.getIndex()] = String.format("IF(%s%s=%s%s,1,0)", __.BEST_KNOWN_FOR_INSTANCE.getExcelColIndex(), excelRowIndex, __.SCORE.getExcelColIndex(), excelRowIndex);
//                data[currentRow][__.DEV_TO_BEST.getIndex()] = String.format("ABS(%s%s-%s%s)/%s%s", __.SCORE.getExcelColIndex(), excelRowIndex, __.BEST_KNOWN_FOR_INSTANCE.getExcelColIndex(), excelRowIndex, __.BEST_KNOWN_FOR_INSTANCE.getExcelColIndex(), excelRowIndex);
                currentRow++;
            }
        }

        // Write matrix data to cell Excel sheet
        for (int i = 0; i < data.length; i++) {
            var row = rawSheet.createRow(i);
            for (int j = 0; j < data[0].length; j++) {
                var cell = row.createCell(j);
                // Header is NEVER a formula
                CType type = i == 0? CType.VALUE: __.getForIndex(j).getCType();
                writeCell(cell, data[i][j], type);
            }
        }

        // Return total area used
        return new AreaReference(convertNumToColString(0) + ":" + convertNumToColString(nColumns-1), SpreadsheetVersion.EXCEL2007);
    }


    private static double getPercentageDevToBest(double score, double bestValueForInstance) {
        return Math.abs(score - bestValueForInstance) / bestValueForInstance;
    }

    private void writeCell(XSSFCell cell, Object d, CType type) {
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

    private static Map<String, Double> bestResultPerInstance(List<? extends SolutionGeneratedEvent<?, ?>> results, List<ReferenceResultProvider> providers, boolean maximizing) {
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
                    best = Math.max(best, optionalValue.orElse(NEGATIVE_INFINITY));
                } else {
                    best = Math.min(best, optionalValue.orElse(POSITIVE_INFINITY));
                }
            }
            bestValuePerInstance.put(instance, best);
        }

        return bestValuePerInstance;
    }

    private enum CType {
        VALUE,
        FORMULA,
        ARRAY_FORMULA
    }

    private enum __ {
        INSTANCE_NAME(0, "Instance Name", CType.VALUE),
        ALG_NAME(1, "Algorithm Name", CType.VALUE),
        ITERATION(2, "Iteration", CType.VALUE),
        SCORE(3, "Score", CType.VALUE),
        TOTAL_TIME(4, "Total Time (s)", CType.VALUE),
        TTB(5, "Time to Best (s)", CType.VALUE),
        IS_BEST_KNOWN(6, "Is Best Known?", CType.FORMULA),
        DEV_TO_BEST(7, "% Dev. to best known", CType.FORMULA),
        BEST_KNOWN_FOR_INSTANCE(8, "Best value known", CType.ARRAY_FORMULA);

        private final int index;
        private final String name;
        private final CType type;

        __(int index, String name, CType type) {
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

        public CType getCType(){
            return type;
        }

        public static __ getForIndex(int index){
            for(var i: __.values()){
                if(i.getIndex() == index){
                    return i;
                }
            }
            throw new IllegalArgumentException(String.format("Invalid index: %s, not declared", index));
        }
    }

}
