package es.urjc.etsii.grafo.util;


import es.urjc.etsii.grafo.testutil.HelperFactory;
import es.urjc.etsii.grafo.util.random.RandomManager;
import es.urjc.etsii.grafo.util.random.RandomType;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RandomManagerTests {

    @Test
    @Order(1)
    public void initializationTest(){
        RandomType type = RandomType.LEGACY;
        int initialseed = 123456, repetitions = 10;
        RandomManager manager = HelperFactory.getRandomManager(type, initialseed, repetitions);

        RandomManager.reset(0);
        var myRandom = RandomManager.getRandom();

        var list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            list.add(myRandom.nextInt());
        }

        RandomManager.reset(0);
        myRandom = RandomManager.getRandom();
        for (int i = 0; i < 100; i++) {
            Assertions.assertEquals(list.get(i), myRandom.nextInt());
        }
    }

    @Test
    @Order(2)
    public void concurrentGenerationTest(){
        RandomType type = RandomType.LEGACY;
        int initialseed = 123456, repetitions = 8;
        RandomManager manager = HelperFactory.getRandomManager(type, initialseed, repetitions);
        var executor = Executors.newFixedThreadPool(4);
        for (int i = 0; i < repetitions; i++) {
            var iteration = i;
            executor.submit(()->{
                RandomManager.reset(iteration);
                var myRandom = RandomManager.getRandom();

                var list = new ArrayList<>();
                for (int n = 0; n < 1000; n++) {
                    list.add(myRandom.nextInt());
                }

                RandomManager.reset(iteration);
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
        RandomType type = RandomType.LEGACY;
        int initialseed = 123456, repetitions = 8;
        RandomManager manager = HelperFactory.getRandomManager(type, initialseed, repetitions);
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
                RandomManager.reinitialize(type, 1234, 100);
                RandomManager.reset(0);
                RandomManager.getRandom().nextInt(0, 100);
            });
        }
    }
}
