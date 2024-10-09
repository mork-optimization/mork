package es.urjc.etsii.grafo.util.random;

import es.urjc.etsii.grafo.testutil.TestCommonUtils;
import es.urjc.etsii.grafo.util.Context;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RandomManagerTest {

    @Test
    @Order(1)
    public void initializationTest(){
        int initialseed = 123456, repetitions = 10;
        var config = TestCommonUtils.solverConfig(RandomType.DEFAULT, initialseed, repetitions);
        TestCommonUtils.initRandom(config);

        var myRandom = RandomManager.getRandom();

        var list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            list.add(myRandom.nextInt());
        }

        Context.Configurator.resetRandom(config, 0);
        myRandom = RandomManager.getRandom();
        for (int i = 0; i < 100; i++) {
            Assertions.assertEquals(list.get(i), myRandom.nextInt());
        }
    }

    @Test
    @Order(2)
    public void concurrentGenerationTest(){
        int initialseed = 123456, repetitions = 8;
        var executor = Executors.newFixedThreadPool(4);
        for (int i = 0; i < repetitions; i++) {
            var iteration = i;
            executor.submit(()->{
                var config = TestCommonUtils.solverConfig(RandomType.DEFAULT, initialseed, repetitions);
                var myRandom = TestCommonUtils.initRandom(config, iteration);

                var list = new ArrayList<>();
                for (int n = 0; n < 1000; n++) {
                    list.add(myRandom.nextInt());
                }

                Context.Configurator.resetRandom(config, iteration);
                myRandom = RandomManager.getRandom();
                for (int n = 0; n < 1000; n++) {
                    Assertions.assertEquals(list.get(n), myRandom.nextInt());
                }
            });
        }
        executor.shutdown();
        try {
            boolean finished = executor.awaitTermination(1, TimeUnit.SECONDS);
            if(!finished){
                Assertions.fail();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(3)
    public void testExceptions(){
        int initialseed = 123456, repetitions = 8;
        var config = TestCommonUtils.solverConfig(RandomType.DEFAULT, initialseed, repetitions);
        Context.Configurator.resetRandom(config, 0);
        Assertions.assertThrows(IllegalArgumentException.class, () -> RandomManager.getRandom().nextInt(0, 0));
        Assertions.assertThrows(IllegalArgumentException.class, () -> RandomManager.getRandom().nextInt(1, 0));
        Assertions.assertDoesNotThrow(() -> RandomManager.getRandom().nextInt(0, 1));
    }

    /**
     * All RandomType providers must be available
     */
    @Test
    @Order(4)
    public void allAvailable(){
        for(var type: RandomType.values()){
            Assertions.assertDoesNotThrow(() -> {
                var config = TestCommonUtils.solverConfig(type, 1234, 100);
                Context.Configurator.resetRandom(config, 0);
                RandomManager.getRandom().nextInt(0, 100);
            });
        }
    }
}
