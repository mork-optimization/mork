package es.urjc.etsii.grafo.autoconfig.autollm;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AutoLlmReconnectPolicyTest {
    @Test void exponentialDelayIsBoundedAndDoesNotOverflow() {
        var policy = new AutoLlmConfig.Reconnect(2_000L, 60_000L, 1_800_000L);
        for (int failures : new int[]{1, 2, 3, 8, Integer.MAX_VALUE}) {
            long delay = policy.delay(failures);
            long expectedCap = failures == 1 ? 2_000 : failures == 2 ? 4_000 : failures == 3 ? 8_000 : 60_000;
            assertTrue(delay >= expectedCap * 4 / 5 && delay <= expectedCap);
        }
        assertThrows(IllegalArgumentException.class, () -> new AutoLlmConfig.Reconnect(100L, 50L, 1000L));
        assertTrue(new AutoLlmConfig.Reconnect(Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE).delay(Integer.MAX_VALUE) > 0);
    }
}
