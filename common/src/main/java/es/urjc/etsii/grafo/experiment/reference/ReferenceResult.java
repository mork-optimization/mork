package es.urjc.etsii.grafo.experiment.reference;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static es.urjc.etsii.grafo.util.TimeUtil.secsToNanos;

/**
 * Reference result for an instance
 */
public class ReferenceResult {
    private Map<String, Double> scores = new HashMap<>();
    private double timeInSeconds = Double.NaN;
    private double timeToBestInSeconds = Double.NaN;
    private boolean isOptimalValue = false;

    /**
     * Get score if present, never NaN.
     *
     * @return optional score
     */
    public Map<String, Double> getScores(){
        return this.scores;
    }

    /**
     * Get score if present, never NaN.
     *
     * @return optional score
     */
    public Optional<Double> getScore(String objective){
        var score = this.scores.get(objective);
        if(score != null && !Double.isNaN(score)){
            return Optional.of(score);
        } else {
            return Optional.empty();
        }
    }

    /**
     * get time in seconds needed to generate this reference value
     *
     * @return time in seconds
     */
    public double getTimeInSeconds() {
        return timeInSeconds;
    }

    /**
     * get time in seconds needed to generate this reference value
     *
     * @return time to best in seconds
     */
    public double getTimeToBestInSeconds() {
        return timeToBestInSeconds;
    }


    /**
     * Set score
     *
     * @param scores score
     * @return ReferenceResult
     */
    public ReferenceResult addScores(Map<String, Double> scores) {
        this.scores.putAll(scores);
        return this;
    }

    /**
     * Set score
     *
     * @param objectiveName objective name
     * @param score score
     * @return ReferenceResult
     */
    public ReferenceResult addScore(String objectiveName, double score) {
        this.scores.put(objectiveName, score);
        return this;
    }

    /**
     * Set score
     *
     * @param objectiveName objective name
     * @param score score
     * @return ReferenceResult
     */
    public ReferenceResult addScore(String objectiveName, String score) {
        return this.addScore(objectiveName, Double.parseDouble(score));
    }

    /**
     * <p>Setter for the field <code>timeInSeconds</code>.</p>
     *
     * @param timeInSeconds a double.
     * @return ReferenceResult
     */
    public ReferenceResult setTimeInSeconds(double timeInSeconds) {
        this.timeInSeconds = timeInSeconds;
        return this;
    }

    /**
     * <p>Setter for the field <code>timeInSeconds</code>.</p>
     *
     * @param timeInSeconds a {@link java.lang.String} object.
     * @return ReferenceResult
     */
    public ReferenceResult setTimeInSeconds(String timeInSeconds) {
        return this.setTimeInSeconds(Double.parseDouble(timeInSeconds));
    }

    /**
     * <p>Setter for the field <code>timeToBestInSeconds</code>.</p>
     *
     * @param timeToBestInSeconds a double.
     * @return ReferenceResult
     */
    public ReferenceResult setTimeToBestInSeconds(double timeToBestInSeconds) {
        this.timeToBestInSeconds = timeToBestInSeconds;
        return this;
    }

    /**
     * <p>Setter for the field <code>timeToBestInSeconds</code>.</p>
     *
     * @param timeToBestInSeconds a {@link java.lang.String} object.
     * @return ReferenceResult
     */
    public ReferenceResult setTimeToBestInSeconds(String timeToBestInSeconds) {
        return this.setTimeToBestInSeconds(Double.parseDouble(timeToBestInSeconds));
    }

    /**
     * Get execution time in nanoseconds
     * @return execution time in nanoseconds
     */
    public long getTimeInNanos(){
        return secsToNanos(this.timeInSeconds);
    }

    /**
     * Get time to best in nanoseconds
     * @return time to best in nanoseconds
     */
    public long getTimeToBestInNanos(){
        return secsToNanos(this.timeToBestInSeconds);
    }

    /**
     * Is the current reference value returned by {@link ReferenceResult#getScores()} optimal?
     * @return True if the value returned by getScore is known to be optimal, false otherwise
     */
    public boolean isOptimalValue() {
        return isOptimalValue;
    }

    /**
     * Specify if the values provided in {@link ReferenceResult#addScores(Map)} is optimal.
     * If not specified, defaults to false.
     * @param isOptimal True if the value returned by getScore is known to be optimal, false otherwise
     */
    public void setOptimalValue(boolean isOptimal) {
        isOptimalValue = isOptimal;
    }
}


