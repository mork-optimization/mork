package es.urjc.etsii.grafo.events;

import es.urjc.etsii.grafo.events.types.PingEvent;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;

class InMemoryEventLogTest {
    @Test void emptyHeadHasIdentityAndExplicitMinusOneCursor() {
        var log = new InMemoryEventLog(); assertEquals(-1, log.head().lastEventId());
        assertNotEquals(log.eventLogId(), new InMemoryEventLog().eventLogId());
        assertTrue(log.page(log.eventLogId(), 0, 0).events().isEmpty());
    }
    @Test void pagesAreBoundedScopedAndHalfOpen() {
        var log = new InMemoryEventLog();
        for (int i = 0; i < 2001; i++) log.append(new EventEnvelope(log.eventLogId(), i, "PingEvent", 1, "worker", new PingEvent()));
        assertEquals(1000, log.page(log.eventLogId(), 1000, 2000).events().size());
        assertEquals(2000, log.page(log.eventLogId(), 2000, 2001).events().getFirst().eventId());
        assertThrows(ResponseStatusException.class, () -> log.page("old", 0, 1));
        assertThrows(IllegalArgumentException.class, () -> log.page(log.eventLogId(), 0, 1001));
        assertThrows(IllegalArgumentException.class, () -> log.page(log.eventLogId(), -1, 0));
    }
}
