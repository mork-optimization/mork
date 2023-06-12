package es.urjc.etsii.tsptests;

import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.events.AbstractEventStorage;
import es.urjc.etsii.grafo.executors.Executor;
import es.urjc.etsii.grafo.orchestrator.AbstractOrchestrator;
import es.urjc.etsii.grafo.solver.Mork;
import es.urjc.etsii.grafo.solver.RunOnStart;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = {Mork.class})
@ActiveProfiles(profiles = {"test", "user-experiment"})
class SmokeTest {

    @Autowired
    private AbstractOrchestrator orchestrator;

    @Autowired
    private Executor<?, ?> executor;

    @Autowired
    private AbstractEventStorage abstractEventStorage;

    @Autowired
    private ApplicationContext applicationContext;

    @BeforeAll
    public static void before(){
        Mork.setSolvingMode(FMode.MINIMIZE);
    }

    @Test
    void checkInjected(){
        // There must exactly one implementation of the following components loaded
        Assertions.assertNotNull(orchestrator);
        Assertions.assertNotNull(executor);
        Assertions.assertNotNull(abstractEventStorage);
        Assertions.assertNotNull(applicationContext);
    }

    @Test
    void checkNotRunner(){
        Assertions.assertThrows(NoSuchBeanDefinitionException.class,
                () -> this.applicationContext.getBean(RunOnStart.class),
                "CommandLineRunner should not be loaded during this integration test");
    }
}
