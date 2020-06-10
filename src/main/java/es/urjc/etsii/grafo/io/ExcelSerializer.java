package es.urjc.etsii.grafo.io;

import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.DataConsolidateFunction;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.List;

@Service
public class ExcelSerializer {

    public static final String RAW_SHEET = "Raw Results";
    public static final String PIVOT_SHEET = "Pivot Table";

    public void saveResult(List<Result> results, Path p) {
        if(results.isEmpty()){
            throw new IllegalArgumentException("Cannot save empty list of results");
        }

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
        pivotTable.addRowLabel(__.INSTANCE_NAME.getIndex()); // Instances label in rows
        pivotTable.addColLabel(__.ALG_NAME.getIndex());      // Algorithm labels in columns
        pivotTable.addColumnLabel(DataConsolidateFunction.SUM, __.BEST_VALUE.getIndex(), __.BEST_VALUE.getName());
        pivotTable.addColumnLabel(DataConsolidateFunction.SUM, __.AVG_VALUE.getIndex(), __.AVG_VALUE.getName());
        pivotTable.addColumnLabel(DataConsolidateFunction.SUM, __.STD.getIndex(), __.STD.getName());
        pivotTable.addColumnLabel(DataConsolidateFunction.SUM, __.AVG_TIME.getIndex(), __.AVG_TIME.getName());
        pivotTable.addColumnLabel(DataConsolidateFunction.SUM, __.TOTAL_TIME.getIndex(), __.TOTAL_TIME.getName());

    }


    private AreaReference fillRawSheet(XSSFSheet rawSheet, List<Result> results) {
        // Create headers
        String[] header = new String[]{
                __.INSTANCE_NAME.getName(),
                __.ALG_NAME.getName(),
                __.BEST_VALUE.getName(),
                __.AVG_VALUE.getName(),
                __.STD.getName(),
                __.AVG_TIME.getName(),
                __.TOTAL_TIME.getName()
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
        }

        // TODO improvements: isBest column, option to generate pivot table instances in either columns or rows, option to disable each serializer
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

    private enum __ {
        INSTANCE_NAME(0, "Instance Name"),
        ALG_NAME(1, "Algorithm Name"),
        BEST_VALUE(2, "Best Value"),
        AVG_VALUE(3,"Avg. Value"),
        STD(4, "Std"),
        AVG_TIME(5, "Avg. Time (ms)"),
        TOTAL_TIME(6, "Total Time (ms)");

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
