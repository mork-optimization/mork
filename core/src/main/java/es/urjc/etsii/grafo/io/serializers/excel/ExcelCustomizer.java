package es.urjc.etsii.grafo.io.serializers.excel;

import es.urjc.etsii.grafo.annotations.InheritedComponent;
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
     */
    public abstract void customize(XSSFWorkbook excelBook);

}
