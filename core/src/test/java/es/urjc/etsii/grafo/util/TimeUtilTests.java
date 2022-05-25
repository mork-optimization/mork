package es.urjc.etsii.grafo.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TimeUtilTests {

    @Test
    public void testNanosToSecs(){
        Assertions.assertEquals(1, TimeUtil.nanosToSecs(1_000_000_000));
        Assertions.assertEquals(-2, TimeUtil.nanosToSecs(-2_000_000_000));
        Assertions.assertEquals(0, TimeUtil.nanosToSecs(0));
    }

    @Test
    public void testSecsToNanos(){
        Assertions.assertEquals(1_000_000_000, TimeUtil.secsToNanos(1));
        Assertions.assertEquals(-2_000_000_000, TimeUtil.secsToNanos(-2));
        Assertions.assertEquals(5_123_456_000L, TimeUtil.secsToNanos(5.123456));
        Assertions.assertEquals(0, TimeUtil.secsToNanos(0));

    }
}
