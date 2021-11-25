package es.urjc.etsii.grafo.solver.services.reference;

import java.util.Optional;

/**
 * Reference result for an instance
 */
public class ReferenceResult {
    private double score = Double.NaN;
    private double timeInSeconds = Double.NaN;
    private double timeToBestInSeconds = Double.NaN;

    /**
     * get score for this instance, or NaN if not defined
     *
     * @return score, NaN if not defined
     */
    public double getScoreOrNan() {
        return score;
    }

    /**
     * Get score if present, never NaN.
     *
     * @return optional score
     */
    public Optional<Double> getScore(){
        return Double.isNaN(this.score) ? Optional.empty() : Optional.of(this.score);
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
     * @param score score
     * @return ReferenceResult
     */
    public ReferenceResult setScore(double score) {
        this.score = score;
        return this;
    }

    /**
     * Set score parsing double from the given string
     *
     * @param score as a string
     * @return ReferenceResult
     */
    public ReferenceResult setScore(String score) {
        this.setScore(Double.parseDouble(score));
        return this;
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
}


