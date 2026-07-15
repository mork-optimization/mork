package es.urjc.etsii.grafo.events;

import es.urjc.etsii.grafo.events.fixture.PlainMorkListener;
import es.urjc.etsii.grafo.events.types.PingEvent;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MorkEventListenerSpringIntegrationTest {

    @Test
    void discoversUnannotatedListenerAndSupportsRecursivePublication() {
        try (var context = new AnnotationConfigApplicationContext()) {
            context.registerBean(SimpMessagingTemplate.class, () -> org.mockito.Mockito.mock(SimpMessagingTemplate.class));
            context.registerBean(InMemoryEventLog.class);
            context.registerBean(MorkEventPublisher.class);
            var scanner = new ClassPathBeanDefinitionScanner(context, false);
            scanner.addIncludeFilter(new AssignableTypeFilter(MorkEventListener.class));

            assertEquals(1, scanner.scan(PlainMorkListener.class.getPackageName()));
            context.refresh();

            var listener = context.getBean(PlainMorkListener.class);
            var publisher = context.getBean(MorkEventPublisher.class);
            publisher.publish(new PingEvent());
            publisher.drainAndStop();
            assertEquals(2, listener.calls());
        }
    }
}
