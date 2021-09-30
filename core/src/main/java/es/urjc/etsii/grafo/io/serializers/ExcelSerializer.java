package es.urjc.etsii.grafo.io.serializers;

import es.urjc.etsii.grafo.solver.configuration.ExcelSerializerConfiguration;
import es.urjc.etsii.grafo.solver.services.ReferenceResultProvider;
import es.urjc.etsii.grafo.solver.services.events.types.SolutionGeneratedEvent;
import es.urjc.etsii.grafo.util.DoubleComparator;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.DataConsolidateFunction;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;


public class ExcelSerializer extends ResultsSerializer {

    public static final String RAW_SHEET = "Raw Results";
    public static final String PIVOT_SHEET = "Pivot Table";
    public static final String SUMMARY_SHEET = "Summary";

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

            var area = fillRawSheet(rawSheet, results);
            fillPivotSheet(pivotSheet, area, rawSheet);

            excelBook.write(outputStream);
        } catch (Exception e) {
            throw new RuntimeException(String.format("Exception while trying to save Excel file: %s, reason: %s", f.getAbsolutePath(), e.getClass().getSimpleName()), e.getCause());
        }
    }

    private void fillPivotSheet(XSSFSheet pivotSheet, AreaReference area, XSSFSheet source) {
        // Generate tables like
        /*                  ____________________________________________________________________________________
         *  ______________ |                Algorithm 1              |                Algorithm 2              |
         * | Instance name | Best Value | isBest | %Dev | Total t(s) | Best Value | isBest | %Dev | Total t(s) |
         * | Instance1     |            |        |      |            |            |        |      |            |
         * | Instance1     |            |        |      |            |            |        |      |            |
           .....................................................................................................
         *  etc
         */

        var pivotTable = pivotSheet.createPivotTable(area, new CellReference(0, 0), source);
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

    private AreaReference fillRawSheet(XSSFSheet rawSheet, List<? extends SolutionGeneratedEvent<?, ?>> results) {
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
                __.DEV_TO_BEST.getName()
        };

        int nColumns = header.length;
        int nRows = results.size() + 1;

        // Create matrix data
        Object[][] data = new Object[nRows][nColumns];
        data[0] = header;

        for (int i = 1; i < nRows; i++) {
            var r = results.get(i - 1);
            data[i][__.INSTANCE_NAME.getIndex()] = r.getInstanceName();
            data[i][__.ALG_NAME.getIndex()] = r.getAlgorithmName();
            data[i][__.ITERATION.getIndex()] = r.getIteration();
            data[i][__.SCORE.getIndex()] = r.getScore();
            data[i][__.TOTAL_TIME.getIndex()] = nanoToSecs(r.getExecutionTime());
            data[i][__.TTB.getIndex()] = nanoToSecs(r.getTimeToBest());

            //double bestValueForInstance = bestResultForInstance(bestValuesPerInstance, r.getInstanceName());
            double bestValueForInstance = bestValuesPerInstance.get(r.getInstanceName());
            boolean isBest = DoubleComparator.equals(bestValueForInstance, r.getScore());
            data[i][__.IS_BEST_KNOWN.getIndex()] = isBest ? 1 : 0;
            double percentageDevToBest = Math.abs(r.getScore() - bestValueForInstance) / bestValueForInstance;
            data[i][__.DEV_TO_BEST.getIndex()] = percentageDevToBest;
        }

        // Write matrix data to cell Excel sheet
        for (int i = 0; i < data.length; i++) {
            var row = rawSheet.createRow(i);
            for (int j = 0; j < data[0].length; j++) {
                var cell = row.createCell(j);
                // Either numeric (and parseable to double) or string
                writeCell(cell, data[i][j]);
            }
        }

        // Return total area used
        return new AreaReference(new CellReference(0, 0), new CellReference(nRows - 1, nColumns - 1), SpreadsheetVersion.EXCEL2007);
    }

    private void writeCell(XSSFCell cell, Object d) {
        if (d instanceof Double) {
            cell.setCellValue((double) d);
        } else if (d instanceof Integer) {
            cell.setCellValue((int) d);
        } else if (d instanceof String) {
            cell.setCellValue((String) d);
        } else {
            throw new IllegalArgumentException("Invalid datatype");
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
            double best = maximizing? NEGATIVE_INFINITY: POSITIVE_INFINITY;
            for(var reference: providers){
                var optionalValue = reference.getValueFor(instance);
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

    private enum __ {
        INSTANCE_NAME(0, "Instance Name"),
        ALG_NAME(1, "Algorithm Name"),
        ITERATION(2, "Iteration"),
        SCORE(3, "Score"),
        TOTAL_TIME(4, "Total Time (s)"),
        TTB(5, "Time to Best (s)"),
        IS_BEST_KNOWN(6, "Is Best Known?"),
        DEV_TO_BEST(7, "% Dev. to best known");

        private final int index;
        private final String name;

        __(int index, String name) {
            this.index = index;
            this.name = name;
        }

        public int getIndex() {
            return index;
        }

        public String getName() {
            return name;
        }
    }

}
