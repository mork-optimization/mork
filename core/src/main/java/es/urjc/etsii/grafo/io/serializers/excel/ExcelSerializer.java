package es.urjc.etsii.grafo.io.serializers.excel;

import es.urjc.etsii.grafo.io.serializers.ResultsSerializer;
import es.urjc.etsii.grafo.solver.SolverConfig;
import es.urjc.etsii.grafo.solver.services.events.AbstractEventStorage;
import es.urjc.etsii.grafo.solver.services.events.types.SolutionGeneratedEvent;
import es.urjc.etsii.grafo.solver.services.reference.ReferenceResultProvider;
import org.apache.poi.ss.usermodel.DataConsolidateFunction;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;


/**
 * Serialize results to Excel XML format
 */
public class ExcelSerializer extends ResultsSerializer {

    private static final Logger log = Logger.getLogger(ExcelSerializer.class.getName());

    /**
     * Raw sheet name
     */
    public static final String RAW_SHEET = "Raw Results";

    /**
     * Pivot table sheet name
     */
    public static final String PIVOT_SHEET = "Pivot Table";

    /**
     * Positive infinity value to use in Excel
     */
    public static final double POSITIVE_INFINITY = 1e99;

    /**
     * Negative infinity value to use in Excel
     */
    public static final double NEGATIVE_INFINITY = -1e99;

    private final boolean maximizing;
    private final List<ReferenceResultProvider> referenceResultProviders;
    private final Optional<ExcelCustomizer> excelCustomizer;
    private final ExcelConfig config;

    /**
     * Create an Excel serializer
     *
     * @param serializerConfig excel serializer configuration
     * @param solverConfig solver configuration
     * @param referenceResultProviders reference result providers if available
     * @param excelCustomizer customizer if available
     * @param eventStorage event storage
     */
    public ExcelSerializer(
            AbstractEventStorage eventStorage,
            ExcelConfig serializerConfig,
            SolverConfig solverConfig,
            List<ReferenceResultProvider> referenceResultProviders,
            Optional<ExcelCustomizer> excelCustomizer
    ) {
        super(eventStorage, serializerConfig);
        this.config = serializerConfig;
        this.maximizing = solverConfig.isMaximizing();
        this.referenceResultProviders = referenceResultProviders;
        this.excelCustomizer = excelCustomizer;
    }

    /** {@inheritDoc} */
    public void _serializeResults(List<? extends SolutionGeneratedEvent<?, ?>> results, Path p) {
        log.info("Exporting result data to XLSX...");

        File f = p.toFile();
        try (
                var outputStream = new FileOutputStream(f);
                var excelBook = new XSSFWorkbook();
        ) {
            var pivotSheet = excelBook.createSheet(PIVOT_SHEET);
            var rawSheet = excelBook.createSheet(RAW_SHEET);

            RawSheetWriter writer = getRawSheetWriter(results);;
            var area = writer.fillRawSheet(rawSheet, maximizing, results, referenceResultProviders);
            
            fillPivotSheet(pivotSheet, area, rawSheet);
            if(this.excelCustomizer.isPresent()){
                var realExcelCustomizer = excelCustomizer.get();
                log.info("Calling Excel customizer: " + realExcelCustomizer.getClass().getSimpleName());
                realExcelCustomizer.customize(excelBook, this.eventStorage);
            } else {
                log.fine("ExcelCustomizer implementation not found");
            }
            // Excel should recalculate on open always
            excelBook.setForceFormulaRecalculation(true);
            excelBook.write(outputStream);
            log.info("XLSX created successfully");
        } catch (Exception e) {
            throw new RuntimeException(String.format("Exception while trying to save Excel file: %s, reason: %s", f.getAbsolutePath(), e.getClass().getSimpleName()), e.getCause());
        }
    }

    private RawSheetWriter getRawSheetWriter(List<? extends SolutionGeneratedEvent<?, ?>> results) {
        switch (this.config.getCalculationMode()){
            case EXCEL:
                return new ExcelCalculatedRawSheetWriter();
            case JAVA:
                return new JavaCalculatedRawSheetWriter();
            case AUTO:
                if(results.size() > this.config.getRowThreshold()){
                    return new JavaCalculatedRawSheetWriter();
                } else {
                    return new ExcelCalculatedRawSheetWriter();
                }
            default:
                throw new UnsupportedOperationException("Not implemented type: " + this.config.getCalculationMode());
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
            pivotTable.addRowLabel(RawSheetCol.INSTANCE_NAME.getIndex()); // Instances label in rows
            pivotTable.addColLabel(RawSheetCol.ALG_NAME.getIndex());      // Algorithm labels in columns
        } else {
            pivotTable.addColLabel(RawSheetCol.INSTANCE_NAME.getIndex()); // Instances label in columns
            pivotTable.addRowLabel(RawSheetCol.ALG_NAME.getIndex());      // Algorithm labels in rows
        }

        if (maximizing) {
            pivotTable.addColumnLabel(DataConsolidateFunction.MAX, RawSheetCol.SCORE.getIndex(), "Max. score");
        } else {
            pivotTable.addColumnLabel(DataConsolidateFunction.MIN, RawSheetCol.SCORE.getIndex(), "Min. score");
        }

        // Optional fields
        if(config.isAvgScoreEnabled()){
            pivotTable.addColumnLabel(DataConsolidateFunction.AVERAGE, RawSheetCol.SCORE.getIndex(), "Avg. score");
        }
        if(config.isStdScoreEnabled()){
            pivotTable.addColumnLabel(DataConsolidateFunction.STD_DEVP, RawSheetCol.SCORE.getIndex(), "STDDEVP score");
        }

        if(config.isVarScoreEnabled()){
            pivotTable.addColumnLabel(DataConsolidateFunction.VARP, RawSheetCol.SCORE.getIndex(), "VARP Score");
        }

        if(config.isAvgTimeEnabled()){
            pivotTable.addColumnLabel(DataConsolidateFunction.AVERAGE, RawSheetCol.TOTAL_TIME.getIndex(), "Avg. Total T(s)");
        }

        if(config.isTotalTimeEnabled()){
            pivotTable.addColumnLabel(DataConsolidateFunction.SUM, RawSheetCol.TOTAL_TIME.getIndex(), "Sum Total T(s)");
        }

        if(config.isAvgTTBEnabled()){
            pivotTable.addColumnLabel(DataConsolidateFunction.AVERAGE, RawSheetCol.TTB.getIndex(), "Avg. TTB(s)");
        }

        if(config.isTotalTTBEnabled()){
            pivotTable.addColumnLabel(DataConsolidateFunction.SUM, RawSheetCol.TTB.getIndex(), "Sum TTB(s)");
        }

        if(config.isSumBestKnownEnabled()){
            pivotTable.addColumnLabel(DataConsolidateFunction.SUM, RawSheetCol.IS_BEST_KNOWN.getIndex(), "#Best");
        }

        if(config.isHasBestKnownEnabled()){
            pivotTable.addColumnLabel(DataConsolidateFunction.MAX, RawSheetCol.IS_BEST_KNOWN.getIndex(), "hasBest");
        }

        if(config.isMinDevToBestKnownEnabled()){
            pivotTable.addColumnLabel(DataConsolidateFunction.MIN, RawSheetCol.DEV_TO_BEST.getIndex(), "Min. %Dev2Best");
        }

        if(config.isAvgDevToBestKnownEnabled()){
            pivotTable.addColumnLabel(DataConsolidateFunction.AVERAGE, RawSheetCol.DEV_TO_BEST.getIndex(), "Avg. %Dev2Best");
        }
    }
    
}
