package es.urjc.etsii.grafo.io.serializers.excel;

import es.urjc.etsii.grafo.io.serializers.AbstractResultSerializerConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


/**
 * This class is used to configure the Excel serializer by the properties specified in the application.yml
 * Exports the results of each experiment.
 * {@see application.yml}
 */
@Configuration
@ConfigurationProperties(prefix = "serializers.xlsx")
public class ExcelConfig extends AbstractResultSerializerConfig {

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

    /**
     * Defines how to handle calculated values when serializing to Excel 2007+
     *
     * @return a value from CalculationMode enum
     */
    public CalculationMode getCalculationMode() {
        return calculationMode;
    }

    /**
     * When calculationMode is AUTO,
     * less than the threshold will use Excel mode,
     * more than threshold will use Java mode (much faster).
     *
     * @return row threshold to change mode automatically
     */
    public int getRowThreshold() {
        return rowThreshold;
    }

    /**
     * Defines how to handle calculated values when serializing to Excel 2007+
     *
     * @param calculationMode new calculation mode
     */
    public void setCalculationMode(CalculationMode calculationMode) {
        this.calculationMode = calculationMode;
    }

    /**
     * When generating the pivot table, should algorithms be in rows or columns?
     *
     * @return True: Instances per row, algorithms in columns
     *         False: Algorithms in rows, instances in columns
     */
    public boolean isAlgorithmsInColumns() {
        return algorithmsInColumns;
    }

    /**
     * When generating the pivot table, should algorithms be in rows or columns?
     *
     * @param algorithmsInColumns  True: Instances per row, algorithms in columns
     *         False: Algorithms in rows, instances in columns
     */
    public void setAlgorithmsInColumns(boolean algorithmsInColumns) {
        this.algorithmsInColumns = algorithmsInColumns;
    }

    /**
     * Show best (min or max) score column in pivot table
     *
     * @return true to show, false to hide
     */
    public boolean isBestScoreEnabled() {
        return bestScoreEnabled;
    }

    /**
     * Show best (min or max) score column in pivot table
     *
     * @param bestScoreEnabled  true to show, false to hide
     */
    public void setBestScoreEnabled(boolean bestScoreEnabled) {
        this.bestScoreEnabled = bestScoreEnabled;
    }

    /**
     * Show average score column in pivot table
     *
     * @return true to show, false to hide
     */
    public boolean isAvgScoreEnabled() {
        return avgScoreEnabled;
    }

    /**
     * Show average score column in pivot table
     *
     * @param avgScoreEnabled  true to show, false to hide
     */
    public void setAvgScoreEnabled(boolean avgScoreEnabled) {
        this.avgScoreEnabled = avgScoreEnabled;
    }

    /**
     * Show standard deviation of solution score column in pivot table
     *
     * @return true to show, false to hide
     */
    public boolean isStdScoreEnabled() {
        return stdScoreEnabled;
    }

    /**
     * Show standard deviation of solution score column in pivot table
     *
     * @param stdScoreEnabled  true to show, false to hide
     */
    public void setStdScoreEnabled(boolean stdScoreEnabled) {
        this.stdScoreEnabled = stdScoreEnabled;
    }

    /**
     * Show variance of solution score column in pivot table
     *
     * @return true to show, false to hide
     */
    public boolean isVarScoreEnabled() {
        return varScoreEnabled;
    }

    /**
     * Show variance of solution score column in pivot table
     *
     * @param varScoreEnabled  true to show, false to hide
     */
    public void setVarScoreEnabled(boolean varScoreEnabled) {
        this.varScoreEnabled = varScoreEnabled;
    }

    /**
     * Show average time in seconds per iteration in pivot table
     *
     * @return true to show, false to hide
     */
    public boolean isAvgTimeEnabled() {
        return avgTimeEnabled;
    }

    /**
     * Show average time in seconds per iteration in pivot table
     *
     * @param avgTimeEnabled  true to show, false to hide
     */
    public void setAvgTimeEnabled(boolean avgTimeEnabled) {
        this.avgTimeEnabled = avgTimeEnabled;
    }

    /**
     * Show total time in seconds for a given (algorithm, instance) in pivot table.
     *
     * @return true to show, false to hide
     */
    public boolean isTotalTimeEnabled() {
        return totalTimeEnabled;
    }

    /**
     * Show total time in seconds for a given (algorithm, instance) in pivot table.
     *
     * @param totalTimeEnabled true to show, false to hide
     */
    public void setTotalTimeEnabled(boolean totalTimeEnabled) {
        this.totalTimeEnabled = totalTimeEnabled;
    }

    /**
     * Show average time to the best solution in seconds in pivot table.
     *
     * @return true to show, false to hide
     */
    public boolean isAvgTTBEnabled() {
        return avgTTBEnabled;
    }

    /**
     * Show average time to the best solution in seconds in pivot table.
     *
     * @param avgTTBEnabled true to show, false to hide
     */
    public void setAvgTTBEnabled(boolean avgTTBEnabled) {
        this.avgTTBEnabled = avgTTBEnabled;
    }

    /**
     * Show total time to the best solution in seconds in pivot table.
     *
     * @return true to show, false to hide
     */
    public boolean isTotalTTBEnabled() {
        return totalTTBEnabled;
    }

    /**
     * Show total time to the best solution in seconds in pivot table.
     *
     * @param totalTTBEnabled true to show, false to hide
     */
    public void setTotalTTBEnabled(boolean totalTTBEnabled) {
        this.totalTTBEnabled = totalTTBEnabled;
    }

    /**
     * Show number of times a given algorithm reaches the best known solution.
     *
     * @return true to show, false to hide
     */
    public boolean isSumBestKnownEnabled() {
        return sumBestKnownEnabled;
    }

    /**
     * Show 1 if a given algorithm reaches the best solution for an instance, 0 otherwise.
     *
     * @param sumBestKnownEnabled true to show, false to hide
     */
    public void setSumBestKnownEnabled(boolean sumBestKnownEnabled) {
        this.sumBestKnownEnabled = sumBestKnownEnabled;
    }

    /**
     * Show 1 if a given algorithm reaches the best solution for an instance, 0 otherwise.
     *
     * @return true to show, false to hide
     */
    public boolean isHasBestKnownEnabled() {
        return hasBestKnownEnabled;
    }

    /**
     * <p>Setter for the field <code>hasBestKnownEnabled</code>.</p>
     *
     * @param hasBestKnownEnabled true to show, false to hide
     */
    public void setHasBestKnownEnabled(boolean hasBestKnownEnabled) {
        this.hasBestKnownEnabled = hasBestKnownEnabled;
    }

    /**
     * Show average percentage deviation to best known solution in pivot table.
     *
     * @return true to show, false to hide
     */
    public boolean isAvgDevToBestKnownEnabled() {
        return avgDevToBestKnownEnabled;
    }

    /**
     * Show average percentage deviation to best known solution in pivot table.
     *
     * @param avgDevToBestKnownEnabled true to show, false to hide
     */
    public void setAvgDevToBestKnownEnabled(boolean avgDevToBestKnownEnabled) {
        this.avgDevToBestKnownEnabled = avgDevToBestKnownEnabled;
    }

    /**
     * Show minimum percentage deviation to best known solution in pivot table.
     *
     * @return true to show, false to hide
     */
    public boolean isMinDevToBestKnownEnabled() {
        return minDevToBestKnownEnabled;
    }

    /**
     * Show minimum percentage deviation to best known solution in pivot table.
     *
     * @param minDevToBestKnownEnabled true to show, false to hide
     */
    public void setMinDevToBestKnownEnabled(boolean minDevToBestKnownEnabled) {
        this.minDevToBestKnownEnabled = minDevToBestKnownEnabled;
    }

    /**
     * Show generated grand total for rows in pivot table
     *
     * @return true to show, false to hide
     */
    public boolean isRowGrandTotal() {
        return rowGrandTotal;
    }

    /**
     * Show generated grand total for rows in pivot table
     *
     * @param rowGrandTotal true to show, false to hide
     */
    public void setRowGrandTotal(boolean rowGrandTotal) {
        this.rowGrandTotal = rowGrandTotal;
    }

    /**
     * Show generated grand total for columns in pivot table
     *
     * @return true to show, false to hide
     */
    public boolean isColumnGrandTotal() {
        return columnGrandTotal;
    }

    /**
     * Show generated grand total for columns in pivot table
     *
     * @param columnGrandTotal true to show, false to hide
     */
    public void setColumnGrandTotal(boolean columnGrandTotal) {
        this.columnGrandTotal = columnGrandTotal;
    }
}
