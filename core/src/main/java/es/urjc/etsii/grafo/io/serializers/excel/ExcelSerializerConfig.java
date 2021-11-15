package es.urjc.etsii.grafo.io.serializers.excel;

import es.urjc.etsii.grafo.io.serializers.AbstractSerializerConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


/**
 * This class is used to configure the Excel serializer by the properties specified in the application.yml
 * Exports the results of each experiment.
 * {@see application.yml}
 */
@Configuration
@ConfigurationProperties(prefix = "serializers.xlsx")
public class ExcelSerializerConfig extends AbstractSerializerConfig {

    /**
     * If True, instances in rows, algorithms in columns
     * If False, instances in columns, algorithms in rows
     */
    private boolean algorithmsInColumns;

    /**
     * Show best (min or max) score in pivot table
     */
    private boolean bestScoreEnabled = true;

    /**
     * Show average score in pivot table
     */
    private boolean avgScoreEnabled = false;

    /**
     * Show standard deviation of solution score in pivot table. Uses Excel STD_DEVP function
     */
    private boolean stdScoreEnabled = false;

    /**
     * Show variance of score in pivot table. Uses Excel VARP function
     */
    private boolean varScoreEnabled = false;

    /**
     * Show average time in seconds per iteracion in pivot table.
     */
    private boolean avgTimeEnabled = false;

    /**
     * Show total time in seconds for a given (algorithm, instance) in pivot table.
     */
    private boolean totalTimeEnabled = true;

    /**
     * Show average time to best solution in seconds in pivot table.
     */
    private boolean avgTTBEnabled = false;

    /**
     * Show total time to best solution in seconds in pivot table.
     */
    private boolean totalTTBEnabled = false;

    /**
     * Show number of times a given algorithm reaches the best known solution.
     */
    private boolean sumBestKnownEnabled = false;

    /**
     * Show 1 if a given algorithm reaches the best solution for an instance, 0 otherwise.
     */
    private boolean hasBestKnownEnabled = true;

    /**
     * Show average percentage deviation to best known solution in pivot table.
     */
    private boolean avgDevToBestKnownEnabled = true;

    /**
     * Show minimum percentage deviation to best known solution in pivot table.
     */
    private boolean minDevToBestKnownEnabled = true;

    /**
     * Show generated grand total for rows in pivot table
     */

    private boolean rowGrandTotal = false;

    /**
     * Show generated grand total for rows in pivot table
     */
    private boolean columnGrandTotal = false;

    private CalculationMode calculationMode;

    private final int rowThreshold = 2000;

    /**
     * Defines how to handle calculated values when serializing to Excel 2007+
     */
    public enum CalculationMode {
        /**
         * Calculate data before serializing to Excel, extremely fast but extending Excel files will not update calculated data such as best value per instance or %Dev.
         */
        JAVA,

        /**
         * Calculate data when user opens Excel file for the first time. Much slower, but allows users to extend and easily modify Excel files.
         */
        EXCEL,

        /**
         * Decide strategy at runtime depending on the number of rows to serialize.
         */
        AUTO
    }

    public CalculationMode getCalculationMode() {
        return calculationMode;
    }

    public int getRowThreshold() {
        return rowThreshold;
    }

    public void setCalculationMode(CalculationMode calculationMode) {
        this.calculationMode = calculationMode;
    }

    public boolean isAlgorithmsInColumns() {
        return algorithmsInColumns;
    }

    public void setAlgorithmsInColumns(boolean algorithmsInColumns) {
        this.algorithmsInColumns = algorithmsInColumns;
    }

    public boolean isBestScoreEnabled() {
        return bestScoreEnabled;
    }

    public void setBestScoreEnabled(boolean bestScoreEnabled) {
        this.bestScoreEnabled = bestScoreEnabled;
    }

    public boolean isAvgScoreEnabled() {
        return avgScoreEnabled;
    }

    public void setAvgScoreEnabled(boolean avgScoreEnabled) {
        this.avgScoreEnabled = avgScoreEnabled;
    }

    public boolean isStdScoreEnabled() {
        return stdScoreEnabled;
    }

    public void setStdScoreEnabled(boolean stdScoreEnabled) {
        this.stdScoreEnabled = stdScoreEnabled;
    }

    public boolean isVarScoreEnabled() {
        return varScoreEnabled;
    }

    public void setVarScoreEnabled(boolean varScoreEnabled) {
        this.varScoreEnabled = varScoreEnabled;
    }

    public boolean isAvgTimeEnabled() {
        return avgTimeEnabled;
    }

    public void setAvgTimeEnabled(boolean avgTimeEnabled) {
        this.avgTimeEnabled = avgTimeEnabled;
    }

    public boolean isTotalTimeEnabled() {
        return totalTimeEnabled;
    }

    public void setTotalTimeEnabled(boolean totalTimeEnabled) {
        this.totalTimeEnabled = totalTimeEnabled;
    }

    public boolean isAvgTTBEnabled() {
        return avgTTBEnabled;
    }

    public void setAvgTTBEnabled(boolean avgTTBEnabled) {
        this.avgTTBEnabled = avgTTBEnabled;
    }

    public boolean isTotalTTBEnabled() {
        return totalTTBEnabled;
    }

    public void setTotalTTBEnabled(boolean totalTTBEnabled) {
        this.totalTTBEnabled = totalTTBEnabled;
    }

    public boolean isSumBestKnownEnabled() {
        return sumBestKnownEnabled;
    }

    public void setSumBestKnownEnabled(boolean sumBestKnownEnabled) {
        this.sumBestKnownEnabled = sumBestKnownEnabled;
    }

    public boolean isHasBestKnownEnabled() {
        return hasBestKnownEnabled;
    }

    public void setHasBestKnownEnabled(boolean hasBestKnownEnabled) {
        this.hasBestKnownEnabled = hasBestKnownEnabled;
    }

    public boolean isAvgDevToBestKnownEnabled() {
        return avgDevToBestKnownEnabled;
    }

    public void setAvgDevToBestKnownEnabled(boolean avgDevToBestKnownEnabled) {
        this.avgDevToBestKnownEnabled = avgDevToBestKnownEnabled;
    }

    public boolean isMinDevToBestKnownEnabled() {
        return minDevToBestKnownEnabled;
    }

    public void setMinDevToBestKnownEnabled(boolean minDevToBestKnownEnabled) {
        this.minDevToBestKnownEnabled = minDevToBestKnownEnabled;
    }

    public boolean isRowGrandTotal() {
        return rowGrandTotal;
    }

    public void setRowGrandTotal(boolean rowGrandTotal) {
        this.rowGrandTotal = rowGrandTotal;
    }

    public boolean isColumnGrandTotal() {
        return columnGrandTotal;
    }

    public void setColumnGrandTotal(boolean columnGrandTotal) {
        this.columnGrandTotal = columnGrandTotal;
    }
}
