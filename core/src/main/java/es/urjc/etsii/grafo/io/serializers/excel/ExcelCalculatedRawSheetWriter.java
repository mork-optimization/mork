package es.urjc.etsii.grafo.io.serializers.excel;

import es.urjc.etsii.grafo.events.types.SolutionGeneratedEvent;
import es.urjc.etsii.grafo.experiment.reference.ReferenceResultProvider;
import es.urjc.etsii.grafo.util.ArrayUtil;
import es.urjc.etsii.grafo.util.Context;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import java.util.List;
import java.util.Map;

import static es.urjc.etsii.grafo.util.TimeUtil.nanosToSecs;
import static org.apache.poi.ss.util.CellReference.convertNumToColString;

/**
 * Serialize raw sheet using Excel formulas. This method is extremely slow for big quantities of data,
 * but allows to seamlessly add or modify the raw data and recalculate all relevant fields without user intervention.
 */
public class ExcelCalculatedRawSheetWriter extends RawSheetWriter {
    
    /** {@inheritDoc} */
    @Override
    public AreaReference fillRawSheet(XSSFSheet rawSheet, boolean maximizing, List<? extends SolutionGeneratedEvent<?, ?>> results, List<ReferenceResultProvider> referenceResultProviders) {
        // Best values per instance
        Map<String, Double> bestValuesPerInstance = bestResultPerInstance(Context.getMainObjective(), results, referenceResultProviders, maximizing);

        String[] customProperties = getCustomPropertyNames(results);

        // Create headers
        String[] commonHeaders = getCommonHeaders();
        String[] headers = ArrayUtil.merge(commonHeaders, customProperties);

        int nColumns = headers.length;
        int cutOff = results.size() + 1;
        int rowsForProvider = referenceResultProviders.size() * bestValuesPerInstance.keySet().size();
        int nRows = cutOff + rowsForProvider;

        // Create matrix data
        Object[][] data = new Object[nRows][nColumns];
        data[0] = headers;

        var mainObjName = Context.getMainObjective().getName();

        for (int i = 1; i < cutOff; i++) {
            var r = results.get(i - 1);

            data[i][RawSheetCol.INSTANCE_NAME.getIndex()] = r.getInstanceName();
            data[i][RawSheetCol.ALG_NAME.getIndex()] = r.getAlgorithmName();
            data[i][RawSheetCol.ITERATION.getIndex()] = r.getIteration();
            data[i][RawSheetCol.SCORE.getIndex()] = r.getObjectives().get(mainObjName);
            data[i][RawSheetCol.TOTAL_TIME.getIndex()] = nanosToSecs(r.getExecutionTime());
            data[i][RawSheetCol.TTB.getIndex()] = nanosToSecs(r.getTimeToBest());
            int excelRowIndex = i + 1; // Current row +1 because Excel starts indexing rows on 1.
//          // Example: =IF(D2=L2,1,0) with L2 best known for instance and D2 current score
            data[i][RawSheetCol.BEST_KNOWN_FOR_INSTANCE.getIndex()] = String.format("%s(IF(%3$s:%3$s=%3$s%4$s,%2$s:%2$s))·%5$s%4$s", maximizing ? "MAX" : "MIN", RawSheetCol.SCORE.getExcelColIndex(),  RawSheetCol.INSTANCE_NAME.getExcelColIndex(), excelRowIndex, RawSheetCol.BEST_KNOWN_FOR_INSTANCE.getExcelColIndex());
            data[i][RawSheetCol.IS_BEST_KNOWN.getIndex()] = String.format("IF(%s%s=%s%s,1,0)", RawSheetCol.BEST_KNOWN_FOR_INSTANCE.getExcelColIndex(), excelRowIndex, RawSheetCol.SCORE.getExcelColIndex(), excelRowIndex);
            data[i][RawSheetCol.DEV_TO_BEST.getIndex()] = String.format("ABS(%s%s-%s%s)/%s%s", RawSheetCol.SCORE.getExcelColIndex(), excelRowIndex, RawSheetCol.BEST_KNOWN_FOR_INSTANCE.getExcelColIndex(), excelRowIndex, RawSheetCol.BEST_KNOWN_FOR_INSTANCE.getExcelColIndex(), excelRowIndex);

//            var userProps = r.getUserDefinedProperties();
//            for (int j = 0; j < customProperties.length; j++) {
//                var propName = customProperties[j];
//                data[i][commonHeaders.length + j] = userProps.get(propName);
//            }
        }

        int currentRow = cutOff;
        for(String instanceName: bestValuesPerInstance.keySet()){
            for(var provider: referenceResultProviders){
                var refResult = provider.getValueFor(instanceName);
                double refScore = refResult.getScores().getOrDefault(mainObjName, Double.NaN);

                data[currentRow][RawSheetCol.INSTANCE_NAME.getIndex()] = instanceName;
                data[currentRow][RawSheetCol.ALG_NAME.getIndex()] = provider.getProviderName();
                data[currentRow][RawSheetCol.ITERATION.getIndex()] = 0;
                data[currentRow][RawSheetCol.SCORE.getIndex()] = nanInfiniteFilter(maximizing, refScore);
                data[currentRow][RawSheetCol.TOTAL_TIME.getIndex()] = nanInfiniteFilter(false, refResult.getTimeInSeconds());
                data[currentRow][RawSheetCol.TTB.getIndex()] = nanInfiniteFilter(false, refResult.getTimeToBestInSeconds());

                int excelRowIndex = currentRow + 1; // Current row +1 because Excel starts indexing rows on 1.
                data[currentRow][RawSheetCol.BEST_KNOWN_FOR_INSTANCE.getIndex()] = String.format("%s(IF(%3$s:%3$s=%3$s%4$s,%2$s:%2$s))·%5$s%4$s", maximizing ? "MAX" : "MIN", RawSheetCol.SCORE.getExcelColIndex(),  RawSheetCol.INSTANCE_NAME.getExcelColIndex(), excelRowIndex, RawSheetCol.BEST_KNOWN_FOR_INSTANCE.getExcelColIndex());
                // Example: =IF(D2=L2,1,0) with L2 best known for instance and D2 current score
                data[currentRow][RawSheetCol.IS_BEST_KNOWN.getIndex()] = String.format("IF(%s%s=%s%s,1,0)", RawSheetCol.BEST_KNOWN_FOR_INSTANCE.getExcelColIndex(), excelRowIndex, RawSheetCol.SCORE.getExcelColIndex(), excelRowIndex);
                data[currentRow][RawSheetCol.DEV_TO_BEST.getIndex()] = String.format("ABS(%s%s-%s%s)/%s%s", RawSheetCol.SCORE.getExcelColIndex(), excelRowIndex, RawSheetCol.BEST_KNOWN_FOR_INSTANCE.getExcelColIndex(), excelRowIndex, RawSheetCol.BEST_KNOWN_FOR_INSTANCE.getExcelColIndex(), excelRowIndex);
                currentRow++;
            }
        }

        // Write matrix data to cell Excel sheet
        for (int i = 0; i < data.length; i++) {
            var row = rawSheet.createRow(i);
            for (int j = 0; j < data[i].length; j++) {
                var cell = row.createCell(j);
                // Header is NEVER a formula
                CType type = i == 0? CType.VALUE: RawSheetCol.getCTypeForIndex(j);
                writeCell(cell, data[i][j], type);
            }
        }

        // Return total area used
        return new AreaReference(convertNumToColString(0) + ":" + convertNumToColString(commonHeaders.length-1), SpreadsheetVersion.EXCEL2007);
    }
}
