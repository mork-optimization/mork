package es.urjc.etsii.grafo.io.serializers.excel;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.io.InstanceManager;
import es.urjc.etsii.grafo.io.serializers.ResultsSerializer;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.SolverConfig;
import es.urjc.etsii.grafo.solver.services.events.types.SolutionGeneratedEvent;
import es.urjc.etsii.grafo.solver.services.reference.ReferenceResultProvider;
import es.urjc.etsii.grafo.util.BenchmarkUtil;
import org.apache.poi.ss.usermodel.DataConsolidateFunction;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import static es.urjc.etsii.grafo.io.serializers.excel.RawSheetWriter.writeCell;


/**
 * Serialize results to Excel XML format
 */
public class ExcelSerializer<S extends Solution<S,I>, I extends Instance>  extends ResultsSerializer<S,I> {

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
     * Sheet name where common data such as VM version, benchmark score, etc will be stored
     */
    public static final String OTHER_DATA_SHEET = "Other";

    /**
     * Instance data sheet
     */
    public static final String INSTANCE_SHEET = "Instances";

    /**
     * Positive infinity value to use in Excel
     */
    public static final double POSITIVE_INFINITY = 1e99;

    /**
     * Negative infinity value to use in Excel
     */
    public static final double NEGATIVE_INFINITY = -1e99;

    private final boolean maximizing;
    private final Optional<ExcelCustomizer> excelCustomizer;
    private final ExcelConfig config;

    private final InstanceManager<I> instanceManager;

    private Object[][] instancePropertyData;

    /**
     * Create an Excel serializer
     *
     * @param serializerConfig         excel serializer configuration
     * @param solverConfig             solver configuration
     * @param referenceResultProviders reference result providers if available
     * @param excelCustomizer          customizer if available
     * @param instanceManager
     */
    public ExcelSerializer(
            ExcelConfig serializerConfig,
            SolverConfig solverConfig,
            List<ReferenceResultProvider> referenceResultProviders,
            Optional<ExcelCustomizer> excelCustomizer,
            InstanceManager<I> instanceManager) {
        super(serializerConfig, referenceResultProviders);
        this.config = serializerConfig;
        this.maximizing = solverConfig.isMaximizing();
        this.excelCustomizer = excelCustomizer;
        this.instanceManager = instanceManager;
    }

    /** {@inheritDoc} */
    public void _serializeResults(String experimentName, List<SolutionGeneratedEvent<S, I>> results, Path p) {
        log.info("Exporting result data to XLSX...");

        File f = p.toFile();
        try (
                var outputStream = new FileOutputStream(f);
                var excelBook = new XSSFWorkbook();
        ) {
            var pivotSheet = excelBook.createSheet(PIVOT_SHEET);
            var rawSheet = excelBook.createSheet(RAW_SHEET);
            var otherDataSheet = excelBook.createSheet(OTHER_DATA_SHEET);

            RawSheetWriter writer = getRawSheetWriter(results);
            var area = writer.fillRawSheet(rawSheet, maximizing, results, referenceResultProviders);
            
            fillPivotSheet(pivotSheet, area, rawSheet);

            // Check and fill instance sheet if appropiate
            fillInstanceSheet(experimentName, excelBook);

            fillOtherDataSheet(otherDataSheet);

            if(this.excelCustomizer.isPresent()){
                var realExcelCustomizer = excelCustomizer.get();
                log.info("Calling Excel customizer: " + realExcelCustomizer.getClass().getSimpleName());
                realExcelCustomizer.customize(excelBook);
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

    private void fillOtherDataSheet(XSSFSheet sheet) {
        var benchmarkInfo = BenchmarkUtil.parseCache();
        var row0 = sheet.createRow(0);
        var firstCell = row0.createCell(0);
        if(benchmarkInfo == null){
            writeCell(firstCell, "Benchmark disabled, enable to save system info", RawSheetWriter.CType.VALUE);
            return;
        }

        // Header
        writeCell(firstCell, "System properties", RawSheetWriter.CType.VALUE);

        // Benchmark score
        var row1 = sheet.createRow(1);
        writeCell(row1.createCell(0), "Benchmark Score", RawSheetWriter.CType.VALUE);
        writeCell(row1.createCell(1), benchmarkInfo.score(), RawSheetWriter.CType.VALUE);

        var row2 = sheet.createRow(2);
        writeCell(row2.createCell(0), "VM Version", RawSheetWriter.CType.VALUE);
        writeCell(row2.createCell(1), benchmarkInfo.info().vmVersion(), RawSheetWriter.CType.VALUE);

        var row3 = sheet.createRow(3);
        writeCell(row3.createCell(0), "Java version", RawSheetWriter.CType.VALUE);
        writeCell(row3.createCell(1), benchmarkInfo.info().javaVersion(), RawSheetWriter.CType.VALUE);

        var row4 = sheet.createRow(4);
        writeCell(row4.createCell(0), "N Processors", RawSheetWriter.CType.VALUE);
        writeCell(row4.createCell(1), benchmarkInfo.info().nProcessors(), RawSheetWriter.CType.VALUE);
    }

    protected void fillInstanceSheet(String expName, XSSFWorkbook excelBook) {
        if(!this.config.isInstanceSheetEnabled()){
            log.fine("Instance sheet disabled");
            return;
        }

        log.fine("Creating instance sheet...");
        var keys = Instance.getUniquePropertiesKeys().toArray(new String[0]);

        if (keys.length == 0) {
            log.info("Instance sheet enabled, but no data available, skipping");
            this.instancePropertyData = new Object[][]{{"Instance sheet is enabled but no properties found for any instance, remember to define properties using Instance::setProperty. If you do not want to use this feature, set serializers.xlsx.instance-sheet-enabled to false"}};
        } else {
            if(this.instancePropertyData == null){
                this.instancePropertyData = getInstancePropertyData(expName, keys);
            }
        }
        var sheet = excelBook.createSheet(INSTANCE_SHEET);

        // Write matrix data to cell Excel sheet
        for (int i = 0; i < this.instancePropertyData.length; i++) {
            var row = sheet.createRow(i);
            var rowData = this.instancePropertyData[i];
            for (int j = 0; j < rowData.length; j++) {
                var cell = row.createCell(j);
                writeCell(cell, rowData[j], RawSheetWriter.CType.VALUE);
            }
        }
        log.fine("Instance sheet created");
    }

    private Object[][] getInstancePropertyData(String expName, String[] keys) {
        List<Object[]> properties = new ArrayList<>();
        Object[] header = new Object[keys.length + 1];
        header[0] = "Instance name";
        System.arraycopy(keys, 0, header, 1, keys.length);
        properties.add(header);
        instanceManager.getInstanceSolveOrder(expName).stream().map(instanceManager::getInstance).forEach(instance -> {
            Object[] row = new Object[keys.length+1];
            row[0] = instance.getId();
            for (int i = 0; i < keys.length; i++) {
                row[i+1] = instance.getPropertyOrDefault(keys[i], "");
            }
            properties.add(row);
        });
        return properties.toArray(new Object[0][]);
    }

    private RawSheetWriter getRawSheetWriter(List<? extends SolutionGeneratedEvent<S, I>> results) {
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
