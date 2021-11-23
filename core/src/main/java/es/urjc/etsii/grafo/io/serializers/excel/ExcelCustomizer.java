package es.urjc.etsii.grafo.io.serializers.excel;

import es.urjc.etsii.grafo.solver.annotations.InheritedComponent;
import es.urjc.etsii.grafo.solver.services.events.AbstractEventStorage;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Customize Excel file with results.
 * Users may add their custom metrics, charts, etc.
 */
@InheritedComponent
public abstract class ExcelCustomizer {

    /**
     * Customize the Excel Workbook. This method is called after the sheets "Raw Results" and "Pivot Table" are calculated,
     * but before the workbook is closed and saved to disk.
     *
     * @param excelBook Excel book
     * @param eventStorage event storage, all generated events can be retrieved in order to calculate custom metrics.
     */
    public abstract void customize(XSSFWorkbook excelBook, AbstractEventStorage eventStorage);

}
