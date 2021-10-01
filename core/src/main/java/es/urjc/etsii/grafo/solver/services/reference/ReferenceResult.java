package es.urjc.etsii.grafo.solver.services.reference;

import java.util.Optional;

public class ReferenceResult {
    private double score = Double.NaN;
    private double timeInSeconds = Double.NaN;
    private double timeToBestInSeconds = Double.NaN;

    public double getScoreOrNan() {
        return score;
    }

    public Optional<Double> getScore(){
        return Double.isNaN(this.score) ? Optional.empty() : Optional.of(this.score);
    }

    public double getTimeInSeconds() {
        return timeInSeconds;
    }

    public double getTimeToBestInSeconds() {
        return timeToBestInSeconds;
    }

    public ReferenceResult setScore(double score) {
        this.score = score;
        return this;
    }

    public ReferenceResult setScore(String score) {
        this.setScore(Double.parseDouble(score));
        return this;
    }

    public ReferenceResult setTimeInSeconds(double timeInSeconds) {
        this.timeInSeconds = timeInSeconds;
        return this;
    }

    public ReferenceResult setTimeToBestInSeconds(double timeToBestInSeconds) {
        this.timeToBestInSeconds = timeToBestInSeconds;
        return this;
    }
}


