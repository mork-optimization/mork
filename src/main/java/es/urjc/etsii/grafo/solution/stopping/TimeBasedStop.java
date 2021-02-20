//package es.urjc.etsii.grafo.solution.stopping;
//
//import java.util.concurrent.TimeUnit;
//
//public class TimeBasedStop implements StopPoint{
//
//    private long duration;
//    private long endTime;
//    private long startTime;
//
//    public TimeBasedStop(TimeUnit unit, long time) {
//        if(time <= 0){
//            throw new IllegalArgumentException("Positive time required");
//        }
//        duration = unit.toNanos(time);
//    }
//
//    @Override
//    public void start() {
//        if(startTime != 0){
//            throw new IllegalStateException("Already marked as started");
//        }
//        startTime = System.nanoTime();
//        this.endTime = startTime + duration;
//        if(duration <= 0 || endTime <= 0){
//            throw new IllegalStateException("Long overflow detected while calculating time limit, reduce it");
//        }
//    }
//
//    @Override
//    public boolean isStarted() {
//        return startTime != 0;
//    }
//
//    @Override
//    public boolean stop() {
//        return System.nanoTime() > endTime;
//    }
//}
