package es.urjc.etsii.grafo.solver.services.reference;

import java.util.Optional;

/**
 * <p>ReferenceResult class.</p>
 *
 */
public class ReferenceResult {
    private double score = Double.NaN;
    private double timeInSeconds = Double.NaN;
    private double timeToBestInSeconds = Double.NaN;

    /**
     * <p>getScoreOrNan.</p>
     *
     * @return a double.
     */
    public double getScoreOrNan() {
        return score;
    }

    /**
     * <p>Getter for the field <code>score</code>.</p>
     *
     * @return a {@link java.util.Optional} object.
     */
    public Optional<Double> getScore(){
        return Double.isNaN(this.score) ? Optional.empty() : Optional.of(this.score);
    }

    /**
     * <p>Getter for the field <code>timeInSeconds</code>.</p>
     *
     * @return a double.
     */
    public double getTimeInSeconds() {
        return timeInSeconds;
    }

    /**
     * <p>Getter for the field <code>timeToBestInSeconds</code>.</p>
     *
     * @return a double.
     */
    public double getTimeToBestInSeconds() {
        return timeToBestInSeconds;
    }

    /**
     * <p>Setter for the field <code>score</code>.</p>
     *
     * @param score a double.
     * @return a {@link es.urjc.etsii.grafo.solver.services.reference.ReferenceResult} object.
     */
    public ReferenceResult setScore(double score) {
        this.score = score;
        return this;
    }

    /**
     * <p>Setter for the field <code>score</code>.</p>
     *
     * @param score a {@link java.lang.String} object.
     * @return a {@link es.urjc.etsii.grafo.solver.services.reference.ReferenceResult} object.
     */
    public ReferenceResult setScore(String score) {
        this.setScore(Double.parseDouble(score));
        return this;
    }

    /**
     * <p>Setter for the field <code>timeInSeconds</code>.</p>
     *
     * @param timeInSeconds a double.
     * @return a {@link es.urjc.etsii.grafo.solver.services.reference.ReferenceResult} object.
     */
    public ReferenceResult setTimeInSeconds(double timeInSeconds) {
        this.timeInSeconds = timeInSeconds;
        return this;
    }

    /**
     * <p>Setter for the field <code>timeInSeconds</code>.</p>
     *
     * @param timeInSeconds a {@link java.lang.String} object.
     * @return a {@link es.urjc.etsii.grafo.solver.services.reference.ReferenceResult} object.
     */
    public ReferenceResult setTimeInSeconds(String timeInSeconds) {
        return this.setTimeInSeconds(Double.parseDouble(timeInSeconds));
    }

    /**
     * <p>Setter for the field <code>timeToBestInSeconds</code>.</p>
     *
     * @param timeToBestInSeconds a double.
     * @return a {@link es.urjc.etsii.grafo.solver.services.reference.ReferenceResult} object.
     */
    public ReferenceResult setTimeToBestInSeconds(double timeToBestInSeconds) {
        this.timeToBestInSeconds = timeToBestInSeconds;
        return this;
    }

    /**
     * <p>Setter for the field <code>timeToBestInSeconds</code>.</p>
     *
     * @param timeToBestInSeconds a {@link java.lang.String} object.
     * @return a {@link es.urjc.etsii.grafo.solver.services.reference.ReferenceResult} object.
     */
    public ReferenceResult setTimeToBestInSeconds(String timeToBestInSeconds) {
        return this.setTimeToBestInSeconds(Double.parseDouble(timeToBestInSeconds));
    }
}


