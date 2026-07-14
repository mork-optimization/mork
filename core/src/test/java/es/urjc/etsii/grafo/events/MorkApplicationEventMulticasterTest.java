package es.urjc.etsii.grafo.events;

import es.urjc.etsii.grafo.events.types.PingEvent;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.PayloadApplicationEvent;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MorkApplicationEventMulticasterTest {

    @Test
    void throwingMorkListenerDoesNotPreventRemainingListeners() {
        var multicaster = new MorkApplicationEventMulticaster();
        var successfulCalls = new AtomicInteger();
        multicaster.addApplicationListener(event -> {
            throw new IllegalStateException("broken listener");
        });
        multicaster.addApplicationListener(event -> successfulCalls.incrementAndGet());

        multicaster.multicastEvent(new PayloadApplicationEvent<>(this, new PingEvent()));

        assertEquals(1, successfulCalls.get());
    }

    @Test
    void unrelatedApplicationEventRetainsNormalExceptionBehavior() {
        var multicaster = new MorkApplicationEventMulticaster();
        var successfulCalls = new AtomicInteger();
        multicaster.addApplicationListener(event -> {
            throw new IllegalStateException("broken listener");
        });
        multicaster.addApplicationListener(event -> successfulCalls.incrementAndGet());

        assertThrows(IllegalStateException.class, () -> multicaster.multicastEvent(new TestApplicationEvent(this)));
        assertEquals(0, successfulCalls.get());
    }

    private static final class TestApplicationEvent extends ApplicationEvent {
        private TestApplicationEvent(Object source) {
            super(source);
        }
    }
}
