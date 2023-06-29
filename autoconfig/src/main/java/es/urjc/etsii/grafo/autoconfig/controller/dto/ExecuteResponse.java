package es.urjc.etsii.grafo.autoconfig.controller.dto;

import es.urjc.etsii.grafo.util.TimeUtil;

/**
 * DTO for answering an execution request for a given instance and algorithm.
 * Currently, used for IRACE integration via the ExecutionController.
 */
public class ExecuteResponse {
 /**
     * Translate failures so Irace understands what has happened.
     * See "10.8 Unreliable target algorithms and immediate rejection" of the Irace Manual for full details
     */
    public static final String FAILED_RESULT = "Inf 0";

    /**
     * Objective function is called cost in irace
     */
    private final double cost;

    /**
     * Execution time in seconds, as used in irace
     */
    private final double time;

    public ExecuteResponse(double cost, double time) {
        this.cost = cost;
        this.time = time;
    }

    public ExecuteResponse(double cost, long time) {
        this(cost, TimeUtil.nanosToSecs(time));
    }

    public ExecuteResponse(){
        this(Double.NaN, Double.NaN);
    }

    public double getCost() {
        return cost;
    }

    public double getTime() {
        return time;
    }

    public String toIraceResultString(){
        if(Double.isNaN(this.cost) || Double.isNaN(this.time)){
            return FAILED_RESULT;
        }

        return "%s %s".formatted(this.cost, this.time);
    }
}
