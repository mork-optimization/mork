package es.urjc.etsii.grafo.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class TimeControlTest {

    @BeforeEach
    void reset(){
        TimeControl.remove();
    }

    @Test
    void failIfNotConfigured(){
        assertThrows(IllegalStateException.class, TimeControl::remaining);
        TimeControl.setMaxExecutionTime(100L, TimeUnit.MILLISECONDS);
        assertThrows(IllegalStateException.class, TimeControl::remaining);
    }

    @Test
    void checkTimeUp(){
        assertFalse(TimeControl.isEnabled());
        TimeControl.setMaxExecutionTime(50, TimeUnit.MILLISECONDS);
        assertFalse(TimeControl.isEnabled());

        TimeControl.start();
        assertTrue(TimeControl.isEnabled());
        assertTrue(TimeControl.remaining() > 0);
        assertFalse(TimeControl.isTimeUp());

        ConcurrencyUtil.sleep(50, TimeUnit.MILLISECONDS);
        assertTrue(TimeControl.isEnabled());
        assertTrue(TimeControl.remaining() < 0);
        assertTrue(TimeControl.isTimeUp());

        TimeControl.remove();
        assertFalse(TimeControl.isEnabled());
    }
}