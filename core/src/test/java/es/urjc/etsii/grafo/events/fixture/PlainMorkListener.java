package es.urjc.etsii.grafo.events.fixture;

import es.urjc.etsii.grafo.events.MorkEventListener;
import es.urjc.etsii.grafo.events.MorkEventPublisher;
import es.urjc.etsii.grafo.events.types.MorkEvent;
import es.urjc.etsii.grafo.events.types.PingEvent;

import java.util.concurrent.atomic.AtomicInteger;

public class PlainMorkListener implements MorkEventListener {

    private final AtomicInteger calls = new AtomicInteger();
    private final MorkEventPublisher publisher;

    public PlainMorkListener(MorkEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void onEvent(MorkEvent event) {
        if (event instanceof PingEvent && calls.incrementAndGet() == 1) {
            publisher.publish(new PingEvent());
        }
    }

    public int calls() {
        return calls.get();
    }
}
