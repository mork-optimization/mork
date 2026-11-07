package es.urjc.etsii.grafo.io.serializers.excel;

import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.executors.WorkUnitResult;
import es.urjc.etsii.grafo.experiment.reference.ReferenceResultProvider;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.io.InstanceManager;
import es.urjc.etsii.grafo.io.serializers.ResultsSerializer;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.util.ArrayUtil;
import es.urjc.etsii.grafo.benchmark.BenchmarkReport;
import es.urjc.etsii.grafo.benchmark.BenchmarkService;
import es.urjc.etsii.grafo.util.Context;
import es.urjc.etsii.grafo.util.ExceptionUtil;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.DataConsolidateFunction;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Table;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.StandardCopyOption;
import java.util.*;

import static es.urjc.etsii.grafo.io.serializers.excel.RawSheetCol.BEST_KNOWN_FOR_INSTANCE;
import static es.urjc.etsii.grafo.io.serializers.excel.RawSheetCol.DEV_TO_BEST;
import static es.urjc.etsii.grafo.io.serializers.excel.RawSheetCol.INSTANCE_NAME;
import static es.urjc.etsii.grafo.io.serializers.excel.RawSheetCol.IS_BEST_KNOWN;
import static es.urjc.etsii.grafo.io.serializers.excel.RawSheetCol.SCORE;
import static es.urjc.etsii.grafo.io.serializers.excel.RawSheetCol.TOTAL_TIME;
import static es.urjc.etsii.grafo.io.serializers.excel.RawSheetCol.TTB;
import static es.urjc.etsii.grafo.util.TimeUtil.nanosToSecs;
import static org.apache.poi.ss.util.CellReference.convertNumToColString;


/**
 * Serialize results to Excel XML format
 */
public class ExcelSerializer<S extends Solution<S,I>, I extends Instance>  extends ResultsSerializer<S,I> {

    private static final Logger log = LoggerFactory.getLogger(ExcelSerializer.class);

    /**
     * Raw sheet name
     */
    public static final String RAW_SHEET = "Raw Results";

    /** Failed executions, excluded from performance summaries. */
    public static final String FAILURES_SHEET = "Failures";

    /**
     * Raw results table name
     */
    public static final String RAW_TABLE = "RawResultsTable";

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

    /** Execution counts by algorithm, including failures and excluding reference rows. */
    public static final String EXECUTION_SUMMARY_SHEET = "Execution Summary";

    static final String PERCENT_NUMBER_FORMAT = "0.00%";

    /**
     * Display elapsed times with at most two decimal places while preserving the
     * full numeric value stored in the workbook.
     */
    static final String TIME_NUMBER_FORMAT = "0.##";

    static final String RAW_TABLE_INSTRUCTIONS = "To add results, paste rows with the same columns into the first row " +
            "directly below this table, then refresh the Pivot Table using Data > Refresh All.";

    private final boolean maximizing;
    private final Optional<ExcelCustomizer> excelCustomizer;
    private final ExcelConfig config;

    private final InstanceManager<I> instanceManager;
    private final BenchmarkService benchmark;

    /** Explicit inputs and execution metadata for offline reports; never infer these from the reporting host. */
    public void serializeCachedResults(List<WorkUnitResult<S, I>> results, List<String> inputs, Path output, BenchmarkReport metadata) {
        Objects.requireNonNull(metadata);
        try { java.nio.file.Files.createDirectories(output.toAbsolutePath().getParent()); }
        catch (IOException e) { throw new java.io.UncheckedIOException(e); }
        writeWorkbook(results, List.copyOf(inputs), output, metadata);
    }

    /**
     * Create an Excel serializer
     *
     * @param serializerConfig         excel serializer configuration
     * @param referenceResultProviders reference result providers if available
     * @param excelCustomizer          customizer if available
     * @param instanceManager
     */
    public ExcelSerializer(
            ExcelConfig serializerConfig,
            List<ReferenceResultProvider> referenceResultProviders,
            Optional<ExcelCustomizer> excelCustomizer,
            InstanceManager<I> instanceManager,
            BenchmarkService benchmark) {
        super(serializerConfig, referenceResultProviders);
        this.config = serializerConfig;
        this.maximizing = Context.getMainObjective().getFMode() == FMode.MAXIMIZE;
        this.excelCustomizer = excelCustomizer;
        this.instanceManager = instanceManager;
        this.benchmark = benchmark;
    }

    /** {@inheritDoc} */
    @Override
    public void _serializeResults(String experimentName, List<WorkUnitResult<S, I>> results, Path p) {
        // Only resolve configured inputs when their properties will be exported.
        List<String> inputs = config.isInstanceSheetEnabled() && !Instance.getUniquePropertiesKeys().isEmpty()
                ? instanceManager.getInstanceSolveOrder(experimentName) : List.of();
        writeWorkbook(results, inputs, p, benchmark.report());
    }

    private void writeWorkbook(List<WorkUnitResult<S, I>> results, List<String> inputs, Path output, BenchmarkReport metadata) {
        Path temporary = null;
        try {
            Path destination = output.toAbsolutePath();
            temporary = Files.createTempFile(destination.getParent(), ".mork-excel-", ".xlsx");
            writeWorkbookData(results, inputs, temporary, metadata);
            try {
                Files.move(temporary, destination, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING);
            }
            log.debug("XLSX created successfully: {}", destination);
        } catch (Exception e) {
            if (temporary != null) {
                try { Files.deleteIfExists(temporary); }
                catch (IOException cleanup) { e.addSuppressed(cleanup); }
            }
            Throwable rootCause = ExceptionUtil.getRootCause(e);
            throw new RuntimeException(String.format(
                    "Exception while trying to save Excel file: %s. Root cause: %s: %s",
                    output.toAbsolutePath(), rootCause.getClass().getSimpleName(), rootCause.getMessage()), e);
        }
    }

    private void writeWorkbookData(List<WorkUnitResult<S, I>> results, List<String> inputs, Path output,
                                   BenchmarkReport metadata) throws IOException {
        var successful = new ArrayList<WorkUnitResult<S, I>>();
        var failed = new ArrayList<WorkUnitResult<S, I>>();
        for (var result : results) {
            if (result.success()) successful.add(result);
            else failed.add(result);
        }
        validateDataRowCount(FAILURES_SHEET, failed.size());
        String[] customProperties = getCustomPropertyHeaders(successful);
        String[] headers = getRawHeaders(customProperties);
        var instanceNames = validateSuccessfulResults(successful);
        long rawRows = successful.size() + (long) referenceResultProviders.size() * instanceNames.size();
        validateDataRowCount(RAW_SHEET, rawRows);

        try (var excelBook = new XSSFWorkbook()) {
            var rawXssfSheet = excelBook.createSheet(RAW_SHEET);
            writeHeader(rawXssfSheet, headers);
            var rawTable = createRawTable(rawXssfSheet, (int) rawRows, headers.length);

            try (var streamExcelBook = new SXSSFWorkbook(excelBook, 100, true, false)) {
                // Create editable XSSF-only sheets after wrapping, so SXSSF does not replace their data.
                var pivotSheet = excelBook.createSheet(PIVOT_SHEET);
                var otherDataSheet = excelBook.createSheet(OTHER_DATA_SHEET);
                if (successful.isEmpty()) {
                    String message = failed.isEmpty() ? "No successful executions to summarize."
                            : "No successful executions to summarize. See the Failures sheet for failed executions.";
                    writeCell(pivotSheet.createRow(0).createCell(0), message, CType.VALUE);
                } else {
                    fillPivotSheet(pivotSheet, rawTable);
                }

                // The XSSF header initializes the table and pivot cache; stream it with the data afterwards.
                rawXssfSheet.removeRow(rawXssfSheet.getRow(0));
                addRawTableInstructions(rawXssfSheet);
                var rawSheet = streamExcelBook.getSheet(RAW_SHEET);
                writeHeader(rawSheet, headers);
                fillInstanceSheet(streamExcelBook, inputs);
                fillOtherDataSheet(otherDataSheet, metadata, successful.size(), failed.size());
                writeRawSheetData(rawSheet, successful, instanceNames, customProperties);
                writeFailuresSheet(streamExcelBook, failed);
                writeExecutionSummary(streamExcelBook, results);

                if (excelCustomizer.isPresent()) {
                    excelCustomizer.get().customize(excelBook, List.copyOf(results));
                }
                streamExcelBook.setForceFormulaRecalculation(true);
                try (var outputStream = Files.newOutputStream(output)) {
                    streamExcelBook.write(outputStream);
                }
            }
        }
    }

    private void writeExecutionSummary(SXSSFWorkbook workbook, List<WorkUnitResult<S, I>> results) {
        var counts = new TreeMap<String, long[]>();
        for (var result : results) {
            var count = counts.computeIfAbsent(result.algorithm().getName(), ignored -> new long[2]);
            count[result.success() ? 0 : 1]++;
        }
        validateDataRowCount(EXECUTION_SUMMARY_SHEET, counts.size());
        var sheet = workbook.createSheet(EXECUTION_SUMMARY_SHEET);
        writeHeader(sheet, new String[]{"Algorithm Name", "Total executions", "Successful executions", "Failed executions", "Success rate"});
        var percent = workbook.createCellStyle();
        percent.setDataFormat(workbook.createDataFormat().getFormat(PERCENT_NUMBER_FORMAT));
        int index = 1;
        for (var entry : counts.entrySet()) {
            var count = entry.getValue();
            long total = count[0] + count[1];
            var row = sheet.createRow(index++);
            writeValues(row, entry.getKey(), total, count[0], count[1], (double) count[0] / total);
            row.getCell(4).setCellStyle(percent);
        }
        sheet.createFreezePane(0, 1);
        sheet.setAutoFilter(new CellRangeAddress(0, counts.size(), 0, 4));
    }

    private void fillOtherDataSheet(XSSFSheet sheet, BenchmarkReport metadata, int successful, int failed) {
        writeCell(sheet.createRow(0).createCell(0), "System properties", CType.VALUE);
        int row = 1;
        for (var entry : new TreeMap<>(metadata.environment()).entrySet()) {
            otherDataRow(sheet, row++, entry.getKey(), entry.getValue());
        }
        otherDataRow(sheet, row++, "Total executions", successful + failed);
        otherDataRow(sheet, row++, "Successful executions", successful);
        otherDataRow(sheet, row++, "Failed executions", failed);
        otherDataRow(sheet, row++, "Performance summaries", "Successful executions only");
        otherDataRow(sheet, row++, "Benchmark status", metadata.status().name());
        otherDataRow(sheet, row++, "Benchmark details", metadata.detail());
        var measurement = metadata.result();
        if (measurement == null) return;
        otherDataRow(sheet, row++, "Benchmark Score", measurement.score());
        otherDataRow(sheet, row++, "Benchmark units", "Approximate MFLOPS (single-threaded numerical JVM reference)");
        otherDataRow(sheet, row++, "Benchmark measured at", measurement.measuredAt());
        otherDataRow(sheet, row++, "Benchmark protocol", measurement.protocol().suite());
        otherDataRow(sheet, row++, "Benchmark seed", measurement.protocol().seed());
        otherDataRow(sheet, row++, "Minimum kernel seconds", measurement.protocol().minimumSeconds());
        var scores = measurement.scores();
        otherDataRow(sheet, row++, "FFT", scores.fft());
        otherDataRow(sheet, row++, "SOR", scores.sor());
        otherDataRow(sheet, row++, "Monte Carlo", scores.monteCarlo());
        otherDataRow(sheet, row++, "Sparse matrix multiplication", scores.sparseMatmult());
        otherDataRow(sheet, row, "LU", scores.lu());
    }

    private void otherDataRow(XSSFSheet sheet, int index, String label, Object value) {
        var row = sheet.createRow(index);
        writeCell(row.createCell(0), label, CType.VALUE);
        writeCell(row.createCell(1), value, CType.VALUE);
    }

    private void writeFailuresSheet(SXSSFWorkbook workbook, List<WorkUnitResult<S, I>> failed) {
        if (failed.isEmpty()) return;

        var sheet = workbook.createSheet(FAILURES_SHEET);
        String[] headers = {"Result ID", "Instance Name", "Instance Path", "Algorithm Name", "Iteration", "Status", "Total Time (s)"};
        var header = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            writeCell(header.createCell(i), headers[i], CType.VALUE);
        }
        var timeStyle = workbook.createCellStyle();
        timeStyle.setDataFormat(workbook.createDataFormat().getFormat(TIME_NUMBER_FORMAT));
        int index = 1;
        for (var result : failed) {
            var row = sheet.createRow(index++);
            writeCell(row.createCell(0), result.resultId().toString(), CType.VALUE);
            writeCell(row.createCell(1), result.instanceId(), CType.VALUE);
            writeCell(row.createCell(2), result.instancePath(), CType.VALUE);
            writeCell(row.createCell(3), result.algorithm().getName(), CType.VALUE);
            writeCell(row.createCell(4), result.iteration(), CType.VALUE);
            writeCell(row.createCell(5), "FAILED", CType.VALUE);
            var time = row.createCell(6);
            writeCell(time, result.executionTime() >= 0 ? nanosToSecs(result.executionTime()) : null, CType.VALUE);
            time.setCellStyle(timeStyle);
        }
        sheet.createFreezePane(0, 1);
        sheet.setAutoFilter(new CellRangeAddress(0, failed.size(), 0, headers.length - 1));
    }

    private Table createRawTable(XSSFSheet rawSheet, int rawDataRows, int columnCount) {
        int lastRow = Math.max(1, rawDataRows);
        var tableArea = new AreaReference(
                new CellReference(0, 0),
                new CellReference(lastRow, columnCount - 1),
                SpreadsheetVersion.EXCEL2007
        );
        var table = rawSheet.createTable(tableArea);
        table.setName(RAW_TABLE);
        table.setDisplayName(RAW_TABLE);
        table.setStyleName("TableStyleLight1");
        table.getCTTable().addNewAutoFilter().setRef(tableArea.formatAsString());
        return table;
    }

    private void addRawTableInstructions(XSSFSheet rawSheet) {
        var creationHelper = rawSheet.getWorkbook().getCreationHelper();
        var anchor = creationHelper.createClientAnchor();
        anchor.setCol1(0);
        anchor.setCol2(4);
        anchor.setRow1(0);
        anchor.setRow2(4);

        var comment = rawSheet.createDrawingPatriarch().createCellComment(anchor);
        comment.setAddress(0, 0);
        comment.setAuthor("Mork");
        comment.setString(creationHelper.createRichTextString(RAW_TABLE_INSTRUCTIONS));
    }

    private void fillInstanceSheet(SXSSFWorkbook workbook, List<String> inputs) {
        if (!config.isInstanceSheetEnabled()) return;
        var sheet = workbook.createSheet(INSTANCE_SHEET);
        var keys = new TreeSet<>(Instance.getUniquePropertiesKeys()).toArray(new String[0]);
        if (keys.length == 0) {
            writeValues(sheet.createRow(0), "Instance sheet is enabled but no properties found for any instance, remember to define properties using Instance::setProperty. If you do not want to use this feature, set serializers.xlsx.instance-sheet-enabled to false");
            return;
        }
        validateDataRowCount(INSTANCE_SHEET, inputs.size());
        writeHeader(sheet, ArrayUtil.merge(new String[]{"Instance name"}, keys));
        int index = 1;
        for (String path : inputs) {
            var instance = instanceManager.getInstance(path);
            var row = sheet.createRow(index++);
            writeCell(row.createCell(0), instance.getId(), CType.VALUE);
            for (int i = 0; i < keys.length; i++) {
                Object value = instance.getPropertyOrDefault(keys[i], "");
                validateInstancePropertyValue(keys[i], value, instance);
                writeCell(row.createCell(i + 1), value, CType.VALUE);
            }
        }
    }

    private static void validateInstancePropertyValue(String propertyName, Object value, Instance instance) {
        if (isExcelScalarValue(value)) {
            return;
        }

        throw new IllegalArgumentException(String.format(
                "Instance property '%s' for instance '%s' cannot be exported to Excel sheet '%s'. Supported values are null, finite Number, String or Boolean, but found %s: %s",
                propertyName,
                instance.getId(),
                INSTANCE_SHEET,
                describeValueType(value),
                value
        ));
    }

    void fillPivotSheet(XSSFSheet pivotSheet, Table sourceTable) {
        // Generate tables like
        /*                  ____________________________________________________________________________________
         *  ______________ |                Algorithm 1              |                Algorithm 2              |
         * | Instance name | Best Value | isBest | %Dev | Total t(s) | Best Value | isBest | %Dev | Total t(s) |
         * | Instance1     |            |        |      |            |            |        |      |            |
         * | Instance1     |            |        |      |            |            |        |      |            |
           .....................................................................................................
         *  etc
         */
        var pivotTable = pivotSheet.createPivotTable(sourceTable, new CellReference(0, 0));

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

        if (config.isBestScoreEnabled()) {
            if (maximizing) {
                pivotTable.addColumnLabel(DataConsolidateFunction.MAX, RawSheetCol.SCORE.getIndex(), "Max. score");
            } else {
                pivotTable.addColumnLabel(DataConsolidateFunction.MIN, RawSheetCol.SCORE.getIndex(), "Min. score");
            }
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
            pivotTable.addColumnLabel(DataConsolidateFunction.AVERAGE, RawSheetCol.TOTAL_TIME.getIndex(), "Avg. Total T(s)", TIME_NUMBER_FORMAT);
        }

        if(config.isTotalTimeEnabled()){
            pivotTable.addColumnLabel(DataConsolidateFunction.SUM, RawSheetCol.TOTAL_TIME.getIndex(), "Sum Total T(s)", TIME_NUMBER_FORMAT);
        }

        if(config.isAvgTTBEnabled()){
            pivotTable.addColumnLabel(DataConsolidateFunction.AVERAGE, RawSheetCol.TTB.getIndex(), "Avg. TTB(s)", TIME_NUMBER_FORMAT);
        }

        if(config.isTotalTTBEnabled()){
            pivotTable.addColumnLabel(DataConsolidateFunction.SUM, RawSheetCol.TTB.getIndex(), "Sum TTB(s)", TIME_NUMBER_FORMAT);
        }

        if(config.isSumBestKnownEnabled()){
            pivotTable.addColumnLabel(DataConsolidateFunction.SUM, RawSheetCol.IS_BEST_KNOWN.getIndex(), "#Best");
        }

        if(config.isHasBestKnownEnabled()){
            pivotTable.addColumnLabel(DataConsolidateFunction.MAX, RawSheetCol.IS_BEST_KNOWN.getIndex(), "hasBest");
        }

        if(config.isMinDevToBestKnownEnabled()){
            pivotTable.addColumnLabel(DataConsolidateFunction.MIN, RawSheetCol.DEV_TO_BEST.getIndex(), "Min. %Dev2Best", PERCENT_NUMBER_FORMAT);
        }

        if(config.isAvgDevToBestKnownEnabled()){
            pivotTable.addColumnLabel(DataConsolidateFunction.AVERAGE, RawSheetCol.DEV_TO_BEST.getIndex(), "Avg. %Dev2Best", PERCENT_NUMBER_FORMAT);
        }
    }


    protected String[] getCommonHeaders(){
        return new String[]{
                RawSheetCol.INSTANCE_NAME.getName(),
                RawSheetCol.ALG_NAME.getName(),
                RawSheetCol.ITERATION.getName(),
                RawSheetCol.SCORE.getName(),
                RawSheetCol.TOTAL_TIME.getName(),
                RawSheetCol.TTB.getName(),
                RawSheetCol.IS_BEST_KNOWN.getName(),
                RawSheetCol.DEV_TO_BEST.getName(),
                RawSheetCol.BEST_KNOWN_FOR_INSTANCE.getName()
        };
    }

    protected String[] getRawHeaders(String[] customProperties) {
        return ArrayUtil.merge(getCommonHeaders(), customProperties);
    }

    protected String[] getCustomPropertyHeaders(List<? extends WorkUnitResult<?, ?>> results) {
        Set<String> customPropertyKeys = new TreeSet<>();
        Set<String> commonHeaders = Set.of(getCommonHeaders());

        for (var result : results) {
            for (var property : result.solutionProperties().entrySet()) {
                String propertyName = property.getKey();
                validateCustomPropertyName(propertyName, commonHeaders);
                validateCustomPropertyValue(propertyName, property.getValue(), result);
                customPropertyKeys.add(propertyName);
            }
        }

        return customPropertyKeys.toArray(new String[0]);
    }

    private static void validateCustomPropertyName(String propertyName, Set<String> commonHeaders) {
        if (propertyName == null) {
            throw new IllegalArgumentException("Solution property names exported to Excel cannot be null");
        }
        if (commonHeaders.contains(propertyName)) {
            throw new IllegalArgumentException(String.format("Solution property '%s' cannot be exported to Excel because it duplicates a built-in raw sheet header", propertyName));
        }
    }

    private static void validateCustomPropertyValue(String propertyName, Object value, WorkUnitResult<?, ?> result) {
        if (isExcelScalarValue(value)) {
            return;
        }

        throw new IllegalArgumentException(String.format(
                "Solution property '%s' for instance '%s', algorithm '%s', iteration '%s' cannot be exported to Excel. Solution properties must be a finite Number, String or Boolean, but found %s: %s",
                propertyName,
                result.instanceId(),
                result.algorithm().getName(),
                result.iteration(),
                describeValueType(value),
                value
        ));
    }

    private static String describeValueType(Object value) {
        return value == null ? "null" : value.getClass().getName();
    }

    private static boolean isExcelScalarValue(Object value) {
        if (value instanceof String || value instanceof Boolean) {
            return true;
        }
        if (value instanceof Number n) {
            return Double.isFinite(n.doubleValue());
        }
        return false;
    }

    /**
     * Value types to use as hint when serializing to Excel cells.
     * Necessary because for example we cannot determine if a string is a formula,
     * an array formula or should be interpreted as a literal
     */
    protected enum CType {
        /**
         * Serialize as value
         */
        VALUE,

        /**
         * Serialize as a normal formula
         */
        FORMULA,

        /**
         * Serialize as an array formula
         */
        ARRAY_FORMULA
    }

    private Set<String> validateSuccessfulResults(List<WorkUnitResult<S, I>> results) {
        var mainObjName = Context.getMainObjective().getName();
        var instanceNames = new LinkedHashSet<String>();
        for (var result : results) {
            var value = result.objectives().get(mainObjName);
            if (value == null || !Double.isFinite(value)) {
                throw new IllegalArgumentException(String.format(
                        "Successful result for instance '%s', algorithm '%s', iteration '%s' must contain a finite value for objective '%s', but found %s",
                        result.instanceId(), result.algorithm().getName(), result.iteration(), mainObjName, value));
            }
            instanceNames.add(result.instanceId());
        }
        return instanceNames;
    }

    private void writeRawSheetData(SXSSFSheet sheet, List<WorkUnitResult<S, I>> results,
                                   Set<String> instanceNames, String[] customProperties) {
        var workbook = sheet.getWorkbook();
        var timeStyle = workbook.createCellStyle();
        timeStyle.setDataFormat(workbook.createDataFormat().getFormat(TIME_NUMBER_FORMAT));
        var percentStyle = workbook.createCellStyle();
        percentStyle.setDataFormat(workbook.createDataFormat().getFormat(PERCENT_NUMBER_FORMAT));
        String objective = Context.getMainObjective().getName();
        int index = 1;
        for (var result : results) {
            var row = sheet.createRow(index++);
            writeValues(row, result.instanceId(), result.algorithm().getName(), result.iteration(),
                    result.objectives().get(objective), secondsOrBlank(result.executionTime()), secondsOrBlank(result.timeToTarget()));
            writeComparisonColumns(row, timeStyle, percentStyle);
            for (int j = 0; j < customProperties.length; j++) {
                writeCell(row.createCell(RawSheetCol.values().length + j), result.solutionProperties().get(customProperties[j]), CType.VALUE);
            }
        }
        for (String instance : instanceNames) {
            for (var provider : referenceResultProviders) {
                var reference = provider.getValueFor(instance);
                var row = sheet.createRow(index++);
                writeValues(row, instance, provider.getProviderName(), 0,
                        finiteOrBlank(reference.getScores().get(objective)),
                        finiteOrBlank(reference.getTimeInSeconds()), finiteOrBlank(reference.getTimeToBestInSeconds()));
                writeComparisonColumns(row, timeStyle, percentStyle);
                for (int j = 0; j < customProperties.length; j++) {
                    row.createCell(RawSheetCol.values().length + j).setBlank();
                }
            }
        }
        if (results.isEmpty()) {
            var row = sheet.createRow(1);
            for (int j = 0; j < RawSheetCol.values().length + customProperties.length; j++) row.createCell(j).setBlank();
        }
    }

    private void writeComparisonColumns(Row row, CellStyle timeStyle, CellStyle percentStyle) {
        int index = row.getRowNum() + 1;
        String score = SCORE.getExcelColIdx() + index;
        String best = BEST_KNOWN_FOR_INSTANCE.getExcelColIdx() + index;
        String scores = RAW_TABLE + "[" + SCORE.getName() + "]";
        String instances = RAW_TABLE + "[" + INSTANCE_NAME.getName() + "]";
        String function = "_xlfn." + (maximizing ? "MAXIFS" : "MINIFS");
        // Exclude blank reference scores, which must never compete as zero-valued solutions.
        writeCell(row.createCell(BEST_KNOWN_FOR_INSTANCE.getIndex()),
                String.format("%s(%s,%s,%s,%s,\"<>\")", function, scores, instances, INSTANCE_NAME.getExcelColIdx() + index, scores), CType.FORMULA);
        writeCell(row.createCell(IS_BEST_KNOWN.getIndex()),
                String.format("IF(ISNUMBER(%s),IF(%s=%s,1,0),\"\")", score, best, score), CType.FORMULA);
        // Relative deviation against zero is undefined unless the score is also zero.
        writeCell(row.createCell(DEV_TO_BEST.getIndex()),
                String.format("IF(ISNUMBER(%s),IF(%s=0,IF(%s=0,0,\"\"),ABS(%s-%s)/ABS(%s)),\"\")", score, best, score, score, best, best), CType.FORMULA);
        row.getCell(TOTAL_TIME.getIndex()).setCellStyle(timeStyle);
        row.getCell(TTB.getIndex()).setCellStyle(timeStyle);
        row.getCell(DEV_TO_BEST.getIndex()).setCellStyle(percentStyle);
    }

    private static Double finiteOrBlank(Double value) {
        return value != null && Double.isFinite(value) ? value : null;
    }

    private static Double secondsOrBlank(long nanos) {
        return nanos < 0 ? null : nanosToSecs(nanos);
    }

    static void validateDataRowCount(String sheetName, long dataRowCount) {
        int maxDataRows = SpreadsheetVersion.EXCEL2007.getMaxRows() - 1;
        if (dataRowCount > maxDataRows) {
            throw new IllegalArgumentException(String.format(
                    "Cannot export %s data rows to Excel sheet '%s': Excel supports at most %s data rows after the header",
                    dataRowCount,
                    sheetName,
                    maxDataRows
            ));
        }
    }

    private static void writeHeader(Sheet sheet, String[] headers) {
        writeValues(sheet.createRow(0), (Object[]) headers);
    }

    private static void writeValues(Row row, Object... values) {
        for (int i = 0; i < values.length; i++) writeCell(row.createCell(i), values[i], CType.VALUE);
    }

    /**
     * Write value to sheet cell
     *
     * @param cell cell where value will be written
     * @param d value to write
     * @param type hints how the value should be interpreted. May or may not be honored.
     */
    protected static void writeCell(Cell cell, Object d, CType type) {
        if(d == null){
            cell.setBlank();
            return;
        }
        switch (type) {
            case FORMULA -> {
                if (!(d instanceof String)) {
                    throw invalidFormulaCellValue(cell.getSheet().getSheetName(), cell.getRowIndex(), cell.getColumnIndex(), d);
                }
                cell.setCellFormula((String) d);
            }
            case ARRAY_FORMULA -> {
                if (!(d instanceof String)) {
                    throw invalidFormulaCellValue(cell.getSheet().getSheetName(), cell.getRowIndex(), cell.getColumnIndex(), d);
                }
                String[] parts = ((String) d).split("·");
                if (parts.length != 2) {
                    throw new IllegalArgumentException(String.format(
                            "Invalid setArrayFormula at Excel sheet '%s' cell %s: %s",
                            cell.getSheet().getSheetName(),
                            excelCellAddress(cell.getRowIndex(), cell.getColumnIndex()),
                            d
                    ));
                }
                cell.getSheet().setArrayFormula(parts[0], CellRangeAddress.valueOf(parts[1]));
            }
            case VALUE -> {
                switch (d) {
                    case Number n -> {
                        validateFiniteNumber(cell.getSheet().getSheetName(), cell.getRowIndex(), cell.getColumnIndex(), n);
                        cell.setCellValue(n.doubleValue());
                    }
                    case String s -> cell.setCellValue(s);
                    case Boolean b -> cell.setCellValue(b);
                    default -> throw invalidValueCellValue(cell.getSheet().getSheetName(), cell.getRowIndex(), cell.getColumnIndex(), d);
                }
            }
        }
    }

    private static void validateFiniteNumber(String sheetName, int rowIndex, int columnIndex, Number value) {
        if (!Double.isFinite(value.doubleValue())) {
            throw new IllegalArgumentException(String.format(
                    "Cannot write non-finite number to Excel sheet '%s' cell %s. Supported values are null, finite Number, String or Boolean, but found %s: %s",
                    sheetName,
                    excelCellAddress(rowIndex, columnIndex),
                    describeValueType(value),
                    value
            ));
        }
    }

    private static IllegalArgumentException invalidFormulaCellValue(String sheetName, int rowIndex, int columnIndex, Object value) {
        return new IllegalArgumentException(String.format(
                "Cannot write Excel formula at sheet '%s' cell %s because value must be a String, but found %s: %s",
                sheetName,
                excelCellAddress(rowIndex, columnIndex),
                describeValueType(value),
                value
        ));
    }

    private static IllegalArgumentException invalidValueCellValue(String sheetName, int rowIndex, int columnIndex, Object value) {
        return new IllegalArgumentException(String.format(
                "Cannot write value to Excel sheet '%s' cell %s. Supported values are null, finite Number, String or Boolean, but found %s: %s",
                sheetName,
                excelCellAddress(rowIndex, columnIndex),
                describeValueType(value),
                value
        ));
    }

    private static String excelCellAddress(int rowIndex, int columnIndex) {
        return convertNumToColString(columnIndex) + (rowIndex + 1);
    }
}
