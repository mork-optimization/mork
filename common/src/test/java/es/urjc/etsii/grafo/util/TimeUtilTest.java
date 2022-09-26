package es.urjc.etsii.grafo.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

class TimeUtilTest {

    @Test
    void testNanosToSecs(){
        Assertions.assertEquals(1, TimeUtil.nanosToSecs(1_000_000_000));
        Assertions.assertEquals(-2, TimeUtil.nanosToSecs(-2_000_000_000));
        Assertions.assertEquals(0, TimeUtil.nanosToSecs(0));
    }

    @Test
    void testSecsToNanos(){
        Assertions.assertEquals(1_000_000_000, TimeUtil.secsToNanos(1));
        Assertions.assertEquals(-2_000_000_000, TimeUtil.secsToNanos(-2));
        Assertions.assertEquals(5_123_456_000L, TimeUtil.secsToNanos(5.123456));
        Assertions.assertEquals(0, TimeUtil.secsToNanos(0));
    }

    @Test
    void convertTest(){
        Assertions.assertEquals(24, TimeUtil.convert(1, TimeUnit.DAYS, TimeUnit.HOURS));
        Assertions.assertEquals(36_000, TimeUtil.convert(10, TimeUnit.HOURS, TimeUnit.SECONDS));
        Assertions.assertEquals(10_000, TimeUtil.convert(10, TimeUnit.SECONDS, TimeUnit.MILLISECONDS));
        Assertions.assertEquals(TimeUtil.NANOS_IN_SECOND, TimeUtil.convert(1, TimeUnit.SECONDS, TimeUnit.NANOSECONDS));
        Assertions.assertEquals(TimeUtil.NANOS_IN_MILLISECOND, TimeUtil.convert(1, TimeUnit.MILLISECONDS, TimeUnit.NANOSECONDS));
    }
}
