package es.urjc.etsii.grafo.io.serializers;

import es.urjc.etsii.grafo.io.Result;
import es.urjc.etsii.grafo.util.DoubleComparator;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.DataConsolidateFunction;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ExcelSerializer extends ResultsSerializer {

    public static final String RAW_SHEET = "Raw Results";
    public static final String PIVOT_SHEET = "Pivot Table";

    @Value("${serializers.xlsx.algorithmsInColumns}")
    private boolean algorithmsInColumns;

    @Value("${solver.maximizing}")
    private boolean maximizing;

    public ExcelSerializer(
            @Value("${serializers.xlsx.enabled}") boolean enabled,
            @Value("${serializers.xlsx.folder}") String folder,
            @Value("${serializers.xlsx.format}") String format
    ) {
        super(enabled, folder, format);
    }

    public void _serializeResults(List<Result> results, Path p) {
        log.info("Exporting result data to XLSX...");

        File f = p.toFile();
        try (var outputStream = new FileOutputStream(f)){
            var excelBook = new XSSFWorkbook();
            var rawSheet = excelBook.createSheet(RAW_SHEET);
            var pivotSheet = excelBook.createSheet(PIVOT_SHEET);

            var area = fillRawSheet(rawSheet, results);
            fillPivotSheet(pivotSheet, area, rawSheet);

            excelBook.write(outputStream);
        } catch (Exception e) {
            throw new RuntimeException("Exception while trying to save Excel file: "+f.getAbsolutePath(), e);
        }
    }

    private void fillPivotSheet(XSSFSheet pivotSheet, AreaReference area, XSSFSheet source) {
        var pivotTable = pivotSheet.createPivotTable(area, new CellReference(0,0), source);
        if(algorithmsInColumns){
            pivotTable.addRowLabel(__.INSTANCE_NAME.getIndex()); // Instances label in rows
            pivotTable.addColLabel(__.ALG_NAME.getIndex());      // Algorithm labels in columns
        } else {
            pivotTable.addColLabel(__.INSTANCE_NAME.getIndex()); // Instances label in columns
            pivotTable.addRowLabel(__.ALG_NAME.getIndex());      // Algorithm labels in rows
        }

        pivotTable.addColumnLabel(DataConsolidateFunction.AVERAGE, __.BEST_VALUE.getIndex(), __.BEST_VALUE.getName());
        pivotTable.addColumnLabel(DataConsolidateFunction.AVERAGE, __.AVG_VALUE.getIndex(), __.AVG_VALUE.getName());
        pivotTable.addColumnLabel(DataConsolidateFunction.AVERAGE, __.STD.getIndex(), __.STD.getName());
        pivotTable.addColumnLabel(DataConsolidateFunction.AVERAGE, __.AVG_TIME.getIndex(), __.AVG_TIME.getName());
        pivotTable.addColumnLabel(DataConsolidateFunction.AVERAGE, __.TOTAL_TIME.getIndex(), __.TOTAL_TIME.getName());
        pivotTable.addColumnLabel(DataConsolidateFunction.SUM, __.IS_BEST.getIndex(), __.IS_BEST.getName());
    }


    private AreaReference fillRawSheet(XSSFSheet rawSheet, List<Result> results) {
        // Best values per instance
        Map<String, Optional<Double>> bestValuesPerInstance = bestPerInstance(results);
        if(bestValuesPerInstance.values().stream().anyMatch(Optional::isEmpty)){
            throw new RuntimeException("Cannot export instances that have not been solved at least once");
        }
        // Create headers
        String[] header = new String[]{
                __.INSTANCE_NAME.getName(),
                __.ALG_NAME.getName(),
                __.BEST_VALUE.getName(),
                __.AVG_VALUE.getName(),
                __.STD.getName(),
                __.AVG_TIME.getName(),
                __.TOTAL_TIME.getName(),
                __.IS_BEST.getName()
        };

        int nColumns = header.length;
        int nRows = results.size() + 1;

        // Create matrix data
        String[][] data = new String[nRows][nColumns];
        data[0] = header;

        for (int i = 1; i < nRows; i++) {
            Result r = results.get(i-1);
            data[i][__.INSTANCE_NAME.getIndex()] = r.getInstanceName();
            data[i][__.ALG_NAME.getIndex()] = r.getAlgorythmName();
            data[i][__.BEST_VALUE.getIndex()] = r.getBestValue();
            data[i][__.AVG_VALUE.getIndex()] = r.getAvgValue();
            data[i][__.STD.getIndex()] = r.getStd();
            data[i][__.AVG_TIME.getIndex()] = r.getAvgTimeInMs();
            data[i][__.TOTAL_TIME.getIndex()] = r.getTotalTimeInMs();

            boolean isBest = DoubleComparator.equals(bestValuesPerInstance.get(r.getInstanceName()).get(), Double.parseDouble(r.getBestValue())); // TODO fix? parsing as double, originally was a double
            data[i][__.IS_BEST.getIndex()] = isBest? "1": "0";
        }

        // Write matrix data to cell Excel sheet
        for (int i = 0; i < data.length; i++) {
            var row = rawSheet.createRow(i);
            for (int j = 0; j < data[0].length; j++) {
                var cell = row.createCell(j);
                // Either numeric (and parseable to double) or string
                var d = data[i][j];
                try {
                    cell.setCellValue(Double.parseDouble(d));
                } catch (NumberFormatException e){
                    cell.setCellValue(d);
                }
            }
        }

        // Return total area used
        return new AreaReference(new CellReference(0,0), new CellReference(nRows-1, nColumns-1), SpreadsheetVersion.EXCEL2007);
    }

    private Map<String, Optional<Double>> bestPerInstance(List<Result> results) {                                       // Reduce by last
        return results.stream().collect(Collectors.groupingBy(Result::getInstanceName,
                Collectors.mapping(a -> Double.parseDouble(a.getBestValue()), Collectors.reducing((a, b) -> maximizing ?
                        Math.max(a, b) :
                        Math.min(a, b)
                ))
        ));
    }

    private enum __ {
        INSTANCE_NAME(0, "Instance Name"),
        ALG_NAME(1, "Algorithm Name"),
        BEST_VALUE(2, "Best Value"),
        AVG_VALUE(3,"Avg. Value"),
        STD(4, "Std"),
        AVG_TIME(5, "Avg. Time (ms)"),
        TOTAL_TIME(6, "Total Time (ms)"),
        IS_BEST(7, "Is Best Known?");

        private final int index;
        private final String name;

        __(int index, String name){
            this.index = index;
            this.name = name;
        }
        public int getIndex() {
            return index;
        }
        public String getName(){
            return name;
        }
    }

}
