package es.urjc.etsii.grafo.solution.stopping;

import java.util.concurrent.TimeUnit;

public class TimeBasedStop implements StopPoint{

    private final long endTime;

    public TimeBasedStop(TimeUnit unit, long time) {
        if(time <= 0){
            throw new IllegalArgumentException("Positive time required");
        }
        long executionTime = unit.toNanos(time);
        this.endTime = System.nanoTime() + executionTime;
        if(executionTime <= 0 || endTime <= 0){
            throw new IllegalArgumentException("Long overflow while calculating time limit, reduce it");
        }
    }

    @Override
    public boolean stop() {
        return System.nanoTime() > endTime;
    }
}
