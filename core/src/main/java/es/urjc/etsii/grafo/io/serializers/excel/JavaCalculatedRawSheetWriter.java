package es.urjc.etsii.grafo.io.serializers.excel;

import es.urjc.etsii.grafo.solver.services.events.types.SolutionGeneratedEvent;
import es.urjc.etsii.grafo.solver.services.reference.ReferenceResultProvider;
import es.urjc.etsii.grafo.util.DoubleComparator;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import java.util.List;
import java.util.Map;


/**
 * Serialize raw sheet calculating all relevant data before hand. This method is extremely fast,
 * but because the data is serialized after being calculated, some columns are not automatically updated if the user manually modifies
 * the raw sheet (%Dev to best, isBest value, etc).
 */
public class JavaCalculatedRawSheetWriter extends RawSheetWriter {
    /** {@inheritDoc} */
    @Override
    public AreaReference fillRawSheet(XSSFSheet rawSheet, boolean maximizing, List<? extends SolutionGeneratedEvent<?, ?>> results, List<ReferenceResultProvider> referenceResultProviders) {
        // Best values per instance
        Map<String, Double> bestValuesPerInstance = bestResultPerInstance(results, referenceResultProviders, maximizing);

        // Create headers
        String[] header = new String[]{
                RawSheetCol.INSTANCE_NAME.getName(),
                RawSheetCol.ALG_NAME.getName(),
                RawSheetCol.ITERATION.getName(),
                RawSheetCol.SCORE.getName(),
                RawSheetCol.TOTAL_TIME.getName(),
                RawSheetCol.TTB.getName(),
                RawSheetCol.IS_BEST_KNOWN.getName(),
                RawSheetCol.DEV_TO_BEST.getName()
        };

        int nColumns = header.length;
        int cutOff = results.size() + 1;
        int rowsForProvider = referenceResultProviders.size() * bestValuesPerInstance.keySet().size();
        int nRows = cutOff + rowsForProvider;


        // Create matrix data
        Object[][] data = new Object[nRows][nColumns];
        data[0] = header;

        for (int i = 1; i < cutOff; i++) {
            var r = results.get(i - 1);
            double bestValueForInstance = bestValuesPerInstance.get(r.getInstanceName());
            boolean isBest = DoubleComparator.equals(bestValueForInstance, r.getScore());

            data[i][RawSheetCol.INSTANCE_NAME.getIndex()] = r.getInstanceName();
            data[i][RawSheetCol.ALG_NAME.getIndex()] = r.getAlgorithmName();
            data[i][RawSheetCol.ITERATION.getIndex()] = r.getIteration();
            data[i][RawSheetCol.SCORE.getIndex()] = r.getScore();
            data[i][RawSheetCol.TOTAL_TIME.getIndex()] = nanoToSecs(r.getExecutionTime());
            data[i][RawSheetCol.TTB.getIndex()] = nanoToSecs(r.getTimeToBest());
            data[i][RawSheetCol.IS_BEST_KNOWN.getIndex()] = isBest ? 1 : 0;
            data[i][RawSheetCol.DEV_TO_BEST.getIndex()] = getPercentageDevToBest(r.getScore(), bestValueForInstance);
        }

        int currentRow = cutOff;
        for(String instaceName: bestValuesPerInstance.keySet()){
            for(var provider: referenceResultProviders){
                double bestValueForInstance = bestValuesPerInstance.get(instaceName);
                var result = provider.getValueFor(instaceName);
                double score = result.getScoreOrNan();
                boolean isBest = Double.isFinite(score) && DoubleComparator.equals(bestValueForInstance, score);

                data[currentRow][RawSheetCol.INSTANCE_NAME.getIndex()] = instaceName;
                data[currentRow][RawSheetCol.ALG_NAME.getIndex()] = provider.getProviderName();
                data[currentRow][RawSheetCol.ITERATION.getIndex()] = 0;
                data[currentRow][RawSheetCol.SCORE.getIndex()] = nanInfiniteFilter(maximizing, score);
                data[currentRow][RawSheetCol.TOTAL_TIME.getIndex()] = nanInfiniteFilter(false, result.getTimeInSeconds());
                data[currentRow][RawSheetCol.TTB.getIndex()] = nanInfiniteFilter(false, result.getTimeToBestInSeconds());
                data[currentRow][RawSheetCol.IS_BEST_KNOWN.getIndex()] = isBest ? 1 : 0;
                data[currentRow][RawSheetCol.DEV_TO_BEST.getIndex()] = getPercentageDevToBest(nanInfiniteFilter(maximizing, score), bestValueForInstance);
                currentRow++;
            }
        }

        // Write matrix data to cell Excel sheet
        for (int i = 0; i < data.length; i++) {
            var row = rawSheet.createRow(i);
            for (int j = 0; j < data[0].length; j++) {
                var cell = row.createCell(j);
                writeCell(cell, data[i][j], CType.VALUE);
            }
        }

        // Return total area used
        return new AreaReference(new CellReference(0, 0), new CellReference(nRows - 1, nColumns - 1), SpreadsheetVersion.EXCEL2007);
    }
}
