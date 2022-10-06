package es.urjc.etsii.grafo.util;

import java.util.concurrent.TimeUnit;

/**
 * Tracks time usage across different threads.
 * Long-running methods should frequently check if they are exceeding their time budget
 * Once the budget has been consumed, they should return as soon as possible
 */
public class TimeControl {

    private static final ThreadLocal<TimeStatus> timeStatus = ThreadLocal.withInitial(TimeStatus::new);

    private TimeControl(){}

    /**
     * Set max execution time. Example:
     * <pre> {@code
     * setMaxExecutionTime(5, TimeUnit.SECONDS);}</pre>
     *
     * @param time time quantity
     * @param unit time unit (minutes, seconds, milliseconds, etc)
     */
    public static void setMaxExecutionTime(long time, TimeUnit unit){
        long nanos = unit.toNanos(time);
        timeStatus.get().setDuration(nanos);
    }

    /**
     * Start counting time
     */
    public static void start(){
        var t = timeStatus.get();
        t.setEnabled(true);
        t.setStart(System.nanoTime());
    }

    /**
     * Remove time restrictions for the current thread
     */
    public static void remove(){
        timeStatus.remove();
    }

    /**
     * Check if we should end.
     * @return true if the algorithm component should try to immediately end
     */
    public static boolean isTimeUp(){
        var t = timeStatus.get();
        var current = System.nanoTime();
        return t.enabled() && (current - t.start() > t.duration());
    }

    /**
     * Get remaining time
     * @return time remaining in nanoseconds. If the time has elapsed, it will return a negative number indicating how much extra time has passed.
     */
    public static long remaining(){
        var t = timeStatus.get();
        if(!t.enabled()){
            throw new IllegalStateException("Time control is not enabled. Call TimeControl::start from the current thread");
        }
        return t.start() + t.duration() - System.nanoTime();
    }

    /**
     * Is the time control enabled?
     * @return true if enabled, false otherwise
     */
    public static boolean isEnabled(){
        return timeStatus.get().enabled();
    }

    /**
     * Simple class to store if enabled, length and start time.
     */
    private static final class TimeStatus {
        private boolean enabled;
        private long start;
        private long duration;

        public boolean enabled() {
            return enabled;
        }

        public long start() {
            return start;
        }

        public long duration() {
            return duration;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public void setStart(long start) {
            this.start = start;
        }

        public void setDuration(long duration) {
            this.duration = duration;
        }
    }
}

