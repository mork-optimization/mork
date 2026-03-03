package es.urjc.etsii.grafo.io.serializers.excel;

import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.events.types.SolutionGeneratedEvent;
import es.urjc.etsii.grafo.experiment.reference.ReferenceResultProvider;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.io.InstanceManager;
import es.urjc.etsii.grafo.io.serializers.ResultsSerializer;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.util.ArrayUtil;
import es.urjc.etsii.grafo.util.BenchmarkUtil;
import es.urjc.etsii.grafo.util.Context;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.DataConsolidateFunction;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static es.urjc.etsii.grafo.io.serializers.excel.RawSheetCol.ALG_NAME;
import static es.urjc.etsii.grafo.io.serializers.excel.RawSheetCol.BEST_KNOWN_FOR_INSTANCE;
import static es.urjc.etsii.grafo.io.serializers.excel.RawSheetCol.DEV_TO_BEST;
import static es.urjc.etsii.grafo.io.serializers.excel.RawSheetCol.INSTANCE_NAME;
import static es.urjc.etsii.grafo.io.serializers.excel.RawSheetCol.IS_BEST_KNOWN;
import static es.urjc.etsii.grafo.io.serializers.excel.RawSheetCol.ITERATION;
import static es.urjc.etsii.grafo.io.serializers.excel.RawSheetCol.SCORE;
import static es.urjc.etsii.grafo.io.serializers.excel.RawSheetCol.TOTAL_TIME;
import static es.urjc.etsii.grafo.io.serializers.excel.RawSheetCol.TTB;
import static es.urjc.etsii.grafo.io.serializers.excel.RawSheetCol.getCTypeForIndex;
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
     * @param referenceResultProviders reference result providers if available
     * @param excelCustomizer          customizer if available
     * @param instanceManager
     */
    public ExcelSerializer(
            ExcelConfig serializerConfig,
            List<ReferenceResultProvider> referenceResultProviders,
            Optional<ExcelCustomizer> excelCustomizer,
            InstanceManager<I> instanceManager) {
        super(serializerConfig, referenceResultProviders);
        this.config = serializerConfig;
        this.maximizing = Context.getMainObjective().getFMode() == FMode.MAXIMIZE;
        this.excelCustomizer = excelCustomizer;
        this.instanceManager = instanceManager;
    }

    /** {@inheritDoc} */
    public void _serializeResults(String experimentName, List<SolutionGeneratedEvent<S, I>> results, Path p) {
        log.debug("Exporting result data to XLSX...");

        File f = p.toFile();
        var excelBook = new XSSFWorkbook();

        try (
                var outputStream = new FileOutputStream(f);
                var streamExcelBook = new SXSSFWorkbook(excelBook, 100, true);
        ) {
            var rawSheet = streamExcelBook.createSheet(RAW_SHEET);
            var pivotSheet = excelBook.createSheet(PIVOT_SHEET);
            var otherDataSheet = excelBook.createSheet(OTHER_DATA_SHEET);

            // Extract custom properties from results
            Set<String> customPropertyKeysSet = new LinkedHashSet<>();
            for (var result : results) {
                customPropertyKeysSet.addAll(result.getSolutionProperties().keySet());
            }
            String[] customProperties = customPropertyKeysSet.toArray(new String[0]);

            var areaString = String.format("%s1:%s%s", convertNumToColString(0), convertNumToColString(getCommonHeaders().length + customProperties.length - 1), 1_000_000);
            var area = new AreaReference(areaString, SpreadsheetVersion.EXCEL2007);
            headRawSheet(rawSheet, customProperties);
            fillPivotSheet(pivotSheet, area, rawSheet);

            // Check and fill instance sheet if appropiate
            fillInstanceSheet(experimentName, excelBook);

            fillOtherDataSheet(otherDataSheet);

            fillRawSheet(rawSheet, maximizing, results, referenceResultProviders);

            if(this.excelCustomizer.isPresent()){
                var realExcelCustomizer = excelCustomizer.get();
                log.debug("Calling Excel customizer: {}", realExcelCustomizer.getClass().getSimpleName());
                realExcelCustomizer.customize(excelBook);
            } else {
                log.debug("ExcelCustomizer implementation not found");
            }
            // Excel should recalculate on open always
            streamExcelBook.setForceFormulaRecalculation(true);
            streamExcelBook.write(outputStream);
            log.debug("XLSX created successfully");
        } catch (Exception e) {
            throw new RuntimeException(String.format("Exception while trying to save Excel file: %s, reason: %s", f.getAbsolutePath(), e.getClass().getSimpleName()), e);
        }
    }

    private void fillOtherDataSheet(XSSFSheet sheet) {
        var benchmarkInfo = BenchmarkUtil.parseCache();
        var row0 = sheet.createRow(0);
        var firstCell = row0.createCell(0);
        if(benchmarkInfo == null){
            writeCell(firstCell, "Benchmark disabled, enable to save system info", CType.VALUE);
            return;
        }

        // Header
        writeCell(firstCell, "System properties", CType.VALUE);

        // Benchmark score
        var row1 = sheet.createRow(1);
        writeCell(row1.createCell(0), "Benchmark Score", CType.VALUE);
        writeCell(row1.createCell(1), benchmarkInfo.score(), CType.VALUE);

        var row2 = sheet.createRow(2);
        writeCell(row2.createCell(0), "VM Version", CType.VALUE);
        writeCell(row2.createCell(1), benchmarkInfo.info().vmVersion(), CType.VALUE);

        var row3 = sheet.createRow(3);
        writeCell(row3.createCell(0), "Java version", CType.VALUE);
        writeCell(row3.createCell(1), benchmarkInfo.info().javaVersion(), CType.VALUE);

        var row4 = sheet.createRow(4);
        writeCell(row4.createCell(0), "N Processors", CType.VALUE);
        writeCell(row4.createCell(1), benchmarkInfo.info().nProcessors(), CType.VALUE);
    }

    protected void fillInstanceSheet(String expName, XSSFWorkbook excelBook) {
        if(!this.config.isInstanceSheetEnabled()){
            log.debug("Instance sheet disabled");
            return;
        }

        log.debug("Creating instance sheet...");
        var keys = Instance.getUniquePropertiesKeys().toArray(new String[0]);

        if (keys.length == 0) {
            log.debug("Instance sheet enabled, but no data available, skipping");
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
                writeCell(cell, rowData[j], CType.VALUE);
            }
        }
        log.debug("Instance sheet created");
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

    private void fillPivotSheet(XSSFSheet pivotSheet, AreaReference sourceDataArea, SXSSFSheet source) {
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

    /**
     * Get best result for a given instance
     *
     * @param results our results
     * @param providers reference values
     * @param maximizing true if this is a maximizing problem, false otherwise
     * @return best value known for a given instance
     */
    protected static Map<String, Double> bestResultPerInstance(Objective<?, ?, ?> objective, List<? extends SolutionGeneratedEvent<?, ?>> results, List<ReferenceResultProvider> providers, boolean maximizing) {

        Map<String, Double> ourBestValuePerInstance = results
                .stream()
                .collect(Collectors.toMap(
                        SolutionGeneratedEvent::getInstanceName,
                        solGenEvent -> solGenEvent.getObjectives().get(objective.getName()),
                        (a, b) -> maximizing ? Math.max(a, b) : Math.min(a, b)
                ));

        Map<String, Double> bestValuePerInstance = new HashMap<>();

        for(var instance: ourBestValuePerInstance.keySet()){
            double best = ourBestValuePerInstance.get(instance);
            for(var reference: providers){
                var optionalValue = reference.getValueFor(instance).getScore(objective.getName());
                if(maximizing){
                    best = Math.max(best, optionalValue.orElse(ExcelSerializer.NEGATIVE_INFINITY));
                } else {
                    best = Math.min(best, optionalValue.orElse(ExcelSerializer.POSITIVE_INFINITY));
                }
            }
            bestValuePerInstance.put(instance, best);
        }

        return bestValuePerInstance;
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

    /**
     * Write data to raw sheet
     *
     * @param rawSheet sheet reference where data should be written to
     * @param maximizing true if this is a maximizing problem, false otherwise
     * @param results list of results to serialize
     * @param referenceResultProviders reference result providers if available
     */
    public void fillRawSheet(SXSSFSheet rawSheet, boolean maximizing, List<? extends SolutionGeneratedEvent<?, ?>> results, List<ReferenceResultProvider> referenceResultProviders) {
        // Best values per instance
        Map<String, Double> bestValuesPerInstance = bestResultPerInstance(Context.getMainObjective(), results, referenceResultProviders, maximizing);
        
        // Extract unique custom property keys from all results
        Set<String> customPropertyKeysSet = new LinkedHashSet<>();
        for (var result : results) {
            customPropertyKeysSet.addAll(result.getSolutionProperties().keySet());
        }
        String[] customProperties = customPropertyKeysSet.toArray(new String[0]);

        // Create headers
        String[] commonHeaders = getCommonHeaders();
        String[] headers = ArrayUtil.merge(commonHeaders, customProperties);

        int nColumns = headers.length;
        int cutOff = results.size() + 1;
        int rowsForProvider = referenceResultProviders.size() * bestValuesPerInstance.size();
        int nRows = cutOff + rowsForProvider;

        // Create matrix data
        Object[][] data = new Object[nRows][nColumns];
        data[0] = headers;

        var mainObjName = Context.getMainObjective().getName();

        for (int i = 1; i < cutOff; i++) {
            var r = results.get(i - 1);

            data[i][INSTANCE_NAME.getIndex()] = r.getInstanceName();
            data[i][ALG_NAME.getIndex()] = r.getAlgorithmName();
            data[i][ITERATION.getIndex()] = r.getIteration();
            data[i][SCORE.getIndex()] = r.getObjectives().get(mainObjName);
            data[i][TOTAL_TIME.getIndex()] = nanosToSecs(r.getExecutionTime());
            data[i][TTB.getIndex()] = nanosToSecs(r.getTimeToBest());
            int excelRowIndex = i + 1; // Current row +1 because Excel starts indexing rows on 1.

            // MINIFS/MAXIFS (scores, instancenames, instancename)
            String function = "_xlfn." + (maximizing ? "MAXIFS" : "MINIFS");
            String refInstanceNameCol = INSTANCE_NAME.getExcelColIdx() + ":" + INSTANCE_NAME.getExcelColIdx();
            String refScoreCol = SCORE.getExcelColIdx() + ":" + SCORE.getExcelColIdx();
            String refCurrentInstance = INSTANCE_NAME.getExcelColIdx() + excelRowIndex;
            String refBestKnownForInstance = BEST_KNOWN_FOR_INSTANCE.getExcelColIdx() + excelRowIndex;
            String refCurrentScore = SCORE.getExcelColIdx() + excelRowIndex;

            data[i][BEST_KNOWN_FOR_INSTANCE.getIndex()] = String.format("%s(%s, %s, %s)", function, refScoreCol, refInstanceNameCol, refCurrentInstance);
            data[i][IS_BEST_KNOWN.getIndex()] = String.format("IF(%s=%s,1,0)", refBestKnownForInstance, refCurrentScore);
            data[i][DEV_TO_BEST.getIndex()] = String.format("ABS(%s-%s)/%s", refCurrentScore, refBestKnownForInstance, refBestKnownForInstance);
            
            // Fill custom properties
            var solutionProps = r.getSolutionProperties();
            for (int j = 0; j < customProperties.length; j++) {
                String propName = customProperties[j];
                data[i][commonHeaders.length + j] = solutionProps.getOrDefault(propName, null);
            }
        }

        int currentRow = cutOff;
        for(String instanceName: bestValuesPerInstance.keySet()){
            for(var provider: referenceResultProviders){
                var refResult = provider.getValueFor(instanceName);
                double refScore = refResult.getScores().getOrDefault(mainObjName, Double.NaN);

                data[currentRow][INSTANCE_NAME.getIndex()] = instanceName;
                data[currentRow][ALG_NAME.getIndex()] = provider.getProviderName();
                data[currentRow][ITERATION.getIndex()] = 0;
                data[currentRow][SCORE.getIndex()] = nanInfiniteFilter(maximizing, refScore);
                data[currentRow][TOTAL_TIME.getIndex()] = nanInfiniteFilter(false, refResult.getTimeInSeconds());
                data[currentRow][TTB.getIndex()] = nanInfiniteFilter(false, refResult.getTimeToBestInSeconds());


                int excelRowIndex = currentRow + 1; // Current row +1 because Excel starts indexing rows on 1.
                // MINIFS/MAXIFS (scores, instancenames, instancename)
                String function = "_xlfn." + (maximizing ? "MAXIFS" : "MINIFS");
                String refInstanceNameCol = INSTANCE_NAME.getExcelColIdx() + ":" + INSTANCE_NAME.getExcelColIdx();
                String refScoreCol = SCORE.getExcelColIdx() + ":" + SCORE.getExcelColIdx();
                String refCurrentInstance = INSTANCE_NAME.getExcelColIdx() + excelRowIndex;
                String refBestKnownForInstance = BEST_KNOWN_FOR_INSTANCE.getExcelColIdx() + excelRowIndex;
                String refCurrentScore = SCORE.getExcelColIdx() + excelRowIndex;

                data[currentRow][BEST_KNOWN_FOR_INSTANCE.getIndex()] = String.format("%s(%s, %s, %s)", function, refScoreCol, refInstanceNameCol, refCurrentInstance);
                data[currentRow][IS_BEST_KNOWN.getIndex()] = String.format("IF(%s=%s,1,0)", refBestKnownForInstance, refCurrentScore);
                data[currentRow][DEV_TO_BEST.getIndex()] = String.format("ABS(%s-%s)/%s", refCurrentScore, refBestKnownForInstance, refBestKnownForInstance);
                currentRow++;
            }
        }

        // Write matrix data to cell Excel sheet
        for (int i = 1; i < data.length; i++) {
            var row = rawSheet.createRow(i);
            for (int j = 0; j < data[i].length; j++) {
                var cell = row.createCell(j);
                writeCell(cell, data[i][j], getCTypeForIndex(j));
            }
        }
    }

    public void headRawSheet(SXSSFSheet rawSheet, String[] customProperties) {
        // Create headers
        String[] commonHeaders = getCommonHeaders();
        String[] headers = ArrayUtil.merge(commonHeaders, customProperties);
        var headerRow = rawSheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            var cell = headerRow.createCell(i);
            writeCell(cell, headers[i], CType.VALUE);
        }
    }

    /**
     * Transform NaNs and other special double values to valid values in Excel
     *
     * @param maximizing true if this is a maximizing problem, false otherwise
     * @param value value to transform
     * @return transformed value
     */
    protected static double nanInfiniteFilter(boolean maximizing, double value){
        if(Double.isFinite(value)){
            return value;
        }
        return maximizing ? ExcelSerializer.NEGATIVE_INFINITY : ExcelSerializer.POSITIVE_INFINITY;
    }

    /**
     * Write value to sheet cell
     *
     * @param cell cell where value will be written
     * @param d value to write
     * @param type hints how the value should be interpreted. May or may not be honored.
     */
    protected static void writeCell(SXSSFCell cell, Object d, CType type) {
        if(d == null){
            cell.setBlank();
            return;
        }
        switch (type) {
            case FORMULA -> {
                if (!(d instanceof String)) {
                    throw new IllegalArgumentException("Trying to set cell as formula but not a String: " + d);
                }
                cell.setCellFormula((String) d);
            }
            case ARRAY_FORMULA -> {
                if (!(d instanceof String)) {
                    throw new IllegalArgumentException("Trying to set cell as formula but not a String: " + d);
                }
                String[] parts = ((String) d).split("·");
                if (parts.length != 2) {
                    throw new IllegalArgumentException("Invalid setArrayFormula: " + d);
                }
                cell.getSheet().setArrayFormula(parts[0], CellRangeAddress.valueOf(parts[1]));
            }
            case VALUE -> {
                switch (d) {
                    case Number n -> cell.setCellValue(n.doubleValue());
                    case String s -> cell.setCellValue(s);
                    case Boolean b -> cell.setCellValue(b);
                    default -> throw new IllegalArgumentException("Invalid datatype: " + d.getClass().getSimpleName());
                }
            }
        }
    }

    /**
     * Write value to sheet cell
     *
     * @param cell cell where value will be written
     * @param d value to write
     * @param type hints how the value should be interpreted. May or may not be honored.
     */
    protected static void writeCell(XSSFCell cell, Object d, CType type) {
        if(d == null){
            cell.setBlank();
            return;
        }
        switch (type) {
            case FORMULA -> {
                if (!(d instanceof String)) {
                    throw new IllegalArgumentException("Trying to set cell as formula but not a String: " + d);
                }
                cell.setCellFormula((String) d);
            }
            case ARRAY_FORMULA -> {
                if (!(d instanceof String)) {
                    throw new IllegalArgumentException("Trying to set cell as formula but not a String: " + d);
                }
                String[] parts = ((String) d).split("·");
                if (parts.length != 2) {
                    throw new IllegalArgumentException("Invalid setArrayFormula: " + d);
                }
                cell.getSheet().setArrayFormula(parts[0], CellRangeAddress.valueOf(parts[1]));
            }
            case VALUE -> {
                switch (d) {
                    case Number n -> cell.setCellValue(n.doubleValue());
                    case String s -> cell.setCellValue(s);
                    case Boolean b -> cell.setCellValue(b);
                    default -> throw new IllegalArgumentException("Invalid datatype: " + d.getClass().getSimpleName());
                }
            }
        }
    }
}
