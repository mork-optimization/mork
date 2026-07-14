package es.urjc.etsii.grafo.events;

import es.urjc.etsii.grafo.events.types.PingEvent;
import org.junit.jupiter.api.Test;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MorkEventListenerTypeFilterTest {

    private final MorkEventListenerTypeFilter filter = new MorkEventListenerTypeFilter();
    private final SimpleMetadataReaderFactory metadataReaderFactory = new SimpleMetadataReaderFactory();

    @Test
    void matchesClassesWithMorkEventListenerMethods() throws IOException {
        assertTrue(matches(PlainListener.class));
    }

    @Test
    void matchesInheritedMorkEventListenerMethods() throws IOException {
        assertTrue(matches(InheritedListener.class));
    }

    @Test
    void matchesInterfaceMorkEventListenerMethods() throws IOException {
        assertTrue(matches(InterfaceListener.class));
    }

    @Test
    void ignoresClassesWithoutMorkEventListenerMethods() throws IOException {
        assertFalse(matches(NoListener.class));
    }

    private boolean matches(Class<?> type) throws IOException {
        var metadataReader = metadataReaderFactory.getMetadataReader(type.getName());
        return filter.match(metadataReader, metadataReaderFactory);
    }

    static class PlainListener {
        @MorkEventListener
        void onPing(PingEvent event) {
        }
    }

    abstract static class BaseListener {
        @MorkEventListener
        void onPing(PingEvent event) {
        }
    }

    static class InheritedListener extends BaseListener {
    }

    interface ListenerContract {
        @MorkEventListener
        void onPing(PingEvent event);
    }

    static class InterfaceListener implements ListenerContract {
        @Override
        public void onPing(PingEvent event) {
        }
    }

    static class NoListener {
        void onPing(PingEvent event) {
        }
    }
}
