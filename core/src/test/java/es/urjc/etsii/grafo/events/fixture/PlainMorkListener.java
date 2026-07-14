package es.urjc.etsii.grafo.events.fixture;

import es.urjc.etsii.grafo.events.MorkEventListener;
import es.urjc.etsii.grafo.events.types.PingEvent;

import java.util.concurrent.atomic.AtomicInteger;

/** Test fixture intentionally lacking a component stereotype. */
public class PlainMorkListener {

    private final AtomicInteger calls = new AtomicInteger();

    @MorkEventListener
    void onPing(PingEvent event) {
        calls.incrementAndGet();
    }

    public int calls() {
        return calls.get();
    }
}
