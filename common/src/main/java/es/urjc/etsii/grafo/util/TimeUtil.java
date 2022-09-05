package es.urjc.etsii.grafo.util;

import java.util.concurrent.TimeUnit;

/**
 * Common time operations
 */
public class TimeUtil {

    /**
     * Nanoseconds in 1 second
     */
    public static long NANOS_IN_SECOND = 1_000_000_000;

    /**
     * Nanoseconds in 1 millisecond
     */
    public static long NANOS_IN_MILLISECOND = 1_000_000;

    /**
     * Convert nanoseconds to seconds
     *
     * @param nanos nanoseconds
     * @return seconds as a double value
     */
    public static double nanosToSecs(long nanos){
        return nanos / (double) NANOS_IN_SECOND;
    }

    /**
     * Convert seconds to nanos
     *
     * @param seconds seconds, can be a decimal number
     * @return nanoseconds, discards decimals after the ninth.
     */
    public static long secsToNanos(double seconds){
        return (long)(seconds * NANOS_IN_SECOND);
    }

    /**
     * Convert time between different timeunits
     * @param value value to convert
     * @param from TimeUnit of the value, example seconds
     * @param to desired TimeUnit, example Milliseconds
     * @return converted value to the given TimeUnit
     */
    public static long convert(long value, TimeUnit from, TimeUnit to){
        return to.convert(value, from);
    }

}
