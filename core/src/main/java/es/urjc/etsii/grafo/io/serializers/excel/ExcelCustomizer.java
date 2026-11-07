package es.urjc.etsii.grafo.io.serializers.excel;

import es.urjc.etsii.grafo.annotations.InheritedComponent;
import es.urjc.etsii.grafo.executors.WorkUnitResult;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.List;

/**
 * Customize Excel file with results.
 * Users may add their custom metrics, charts, etc.
 */
@InheritedComponent
public abstract class ExcelCustomizer {

    /**
     * Customize the workbook after its default sheets have been generated, before saving.
     * The Pivot Table and Other sheets can be edited, and new sheets may be added.
     * Raw Results, Failures, Instances and Execution Summary are streamed: their rows
     * cannot be read or modified through this workbook. Use the supplied results for analysis.
     * These rows are merged into the saved file after this hook returns; the supplied workbook
     * is not a reopened copy of the completed report. Do not recreate the streamed sheets or rows.
     * Formula values are calculated by Excel when opening the file.
     * Modify the workbook in place and return; the serializer owns writing and closing it.
     * Do not retain the workbook for asynchronous work. Exceptions fail the export while
     * preserving the previous report. Custom sheets are held in memory.
     *
     * @param excelBook Excel book
     * @param results read-only list of this report's successful and failed executions. Cached
     *                objectives and properties are available even when solutions are absent
     *                (for example, in offline campaign exports). Reference rows are not executions.
     */
    public abstract void customize(XSSFWorkbook excelBook, List<? extends WorkUnitResult<?, ?>> results);

}
