package es.urjc.etsii.grafo.io.serializers.excel;

import es.urjc.etsii.grafo.events.types.SolutionGeneratedEvent;
import es.urjc.etsii.grafo.experiment.reference.ReferenceResultProvider;
import es.urjc.etsii.grafo.util.ArrayUtil;
import es.urjc.etsii.grafo.util.Context;
import es.urjc.etsii.grafo.util.DoubleComparator;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import java.util.List;
import java.util.Map;

import static es.urjc.etsii.grafo.util.TimeUtil.nanosToSecs;


/**
 * Serialize raw sheet calculating all relevant data beforehand. This method is extremely fast,
 * but because the data is serialized after being calculated, some columns are not automatically updated if the user manually modifies
 * the raw sheet (%Dev to best, isBest value, etc).
 */
public class JavaCalculatedRawSheetWriter extends RawSheetWriter {
    /** {@inheritDoc} */
    @Override
    public AreaReference fillRawSheet(XSSFSheet rawSheet, boolean maximizing, List<? extends SolutionGeneratedEvent<?, ?>> results, List<ReferenceResultProvider> referenceResultProviders) {
        // Best values per instance
        Map<String, Double> bestValuesPerInstance = bestResultPerInstance(Context.getMainObjective(), results, referenceResultProviders, maximizing);

        // Create headers
        String[] customProperties = getCustomPropertyNames(results);
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
            double bestValueForInstance = bestValuesPerInstance.get(r.getInstanceName());
            var objValues = r.getObjectives();
            var mainObjValue = objValues.get(mainObjName);
            boolean isBest = DoubleComparator.equals(bestValueForInstance, mainObjValue);

            data[i][RawSheetCol.INSTANCE_NAME.getIndex()] = r.getInstanceName();
            data[i][RawSheetCol.ALG_NAME.getIndex()] = r.getAlgorithmName();
            data[i][RawSheetCol.ITERATION.getIndex()] = r.getIteration();
            data[i][RawSheetCol.SCORE.getIndex()] = r.getObjectives().get(mainObjName);
            data[i][RawSheetCol.TOTAL_TIME.getIndex()] = nanosToSecs(r.getExecutionTime());
            data[i][RawSheetCol.TTB.getIndex()] = nanosToSecs(r.getTimeToBest());
            data[i][RawSheetCol.BEST_KNOWN_FOR_INSTANCE.getIndex()] = bestValueForInstance;
            data[i][RawSheetCol.IS_BEST_KNOWN.getIndex()] = isBest ? 1 : 0;
            data[i][RawSheetCol.DEV_TO_BEST.getIndex()] = getPercentageDevToBest(mainObjValue, bestValueForInstance);

//            var userProps = r.getUserDefinedProperties();
//            for (int j = 0; j < customProperties.length; j++) {
//                var propName = customProperties[j];
//                data[i][commonHeaders.length + j] = userProps.get(propName);
//            }
        }

        int currentRow = cutOff;
        for(String instaceName: bestValuesPerInstance.keySet()){
            for(var provider: referenceResultProviders){
                var refResult = provider.getValueFor(instaceName);
                double bestValueForInstance = bestValuesPerInstance.get(instaceName);
                double score = refResult.getScores().getOrDefault(mainObjName, Double.NaN);
                boolean isBest = Double.isFinite(score) && DoubleComparator.equals(bestValueForInstance, score);

                data[currentRow][RawSheetCol.INSTANCE_NAME.getIndex()] = instaceName;
                data[currentRow][RawSheetCol.ALG_NAME.getIndex()] = provider.getProviderName();
                data[currentRow][RawSheetCol.ITERATION.getIndex()] = 0;
                data[currentRow][RawSheetCol.SCORE.getIndex()] = nanInfiniteFilter(maximizing, score);
                data[currentRow][RawSheetCol.TOTAL_TIME.getIndex()] = nanInfiniteFilter(false, refResult.getTimeInSeconds());
                data[currentRow][RawSheetCol.TTB.getIndex()] = nanInfiniteFilter(false, refResult.getTimeToBestInSeconds());
                data[currentRow][RawSheetCol.IS_BEST_KNOWN.getIndex()] = isBest ? 1 : 0;
                data[currentRow][RawSheetCol.DEV_TO_BEST.getIndex()] = getPercentageDevToBest(nanInfiniteFilter(maximizing, score), bestValueForInstance);
                currentRow++;
            }
        }

        // Write matrix data to cell Excel sheet
        for (int i = 0; i < data.length; i++) {
            var row = rawSheet.createRow(i);
            for (int j = 0; j < data[i].length; j++) {
                var cell = row.createCell(j);
                writeCell(cell, data[i][j], CType.VALUE);
            }
        }

        // Return total area used
        return new AreaReference(new CellReference(0, 0), new CellReference(nRows - 1, commonHeaders.length - 1), SpreadsheetVersion.EXCEL2007);
    }
}
