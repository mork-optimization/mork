package es.urjc.etsii.grafo.events;

import es.urjc.etsii.grafo.events.fixture.PlainMorkListener;
import es.urjc.etsii.grafo.events.types.PingEvent;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MorkEventListenerSpringIntegrationTest {

    @Test
    void discoversRegistersAndInvokesPlainAnnotatedListener() {
        try (var context = new AnnotationConfigApplicationContext()) {
            context.register(MorkEventMulticasterConfiguration.class);
            var scanner = new ClassPathBeanDefinitionScanner(context, false);
            scanner.addIncludeFilter(new MorkEventListenerTypeFilter());

            assertEquals(1, scanner.scan(PlainMorkListener.class.getPackageName()));
            context.refresh();

            var listener = context.getBean(PlainMorkListener.class);
            context.publishEvent(new PingEvent());
            assertEquals(1, listener.calls());
        }
    }
}
