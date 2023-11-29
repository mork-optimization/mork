package es.urjc.etsii.grafo.util.random;

import es.urjc.etsii.grafo.testutil.TestCommonUtils;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.random.RandomGenerator;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RandomManagerTest {

    @Test
    @Order(1)
    public void initializationTest(){
        int initialseed = 123456, repetitions = 10;
        RandomGenerator manager = TestCommonUtils.initRandom(RandomType.DEFAULT, initialseed, repetitions);

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
        int initialseed = 123456, repetitions = 8;
        var executor = Executors.newFixedThreadPool(4);
        for (int i = 0; i < repetitions; i++) {
            var iteration = i;
            executor.submit(()->{
                var myRandom = TestCommonUtils.initRandom(RandomType.DEFAULT, initialseed, repetitions, iteration);

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
        int initialseed = 123456, repetitions = 8;
        RandomGenerator random = TestCommonUtils.initRandom(RandomType.DEFAULT, initialseed, repetitions);
        Assertions.assertThrows(IllegalArgumentException.class, () -> RandomManager.getRandom().nextInt(0, 0));
        Assertions.assertThrows(IllegalArgumentException.class, () -> RandomManager.getRandom().nextInt(1, 0));
        Assertions.assertDoesNotThrow(() -> RandomManager.getRandom().nextInt(0, 1));
    }

    /**
     * All RandomType providers must be available
     */
    @Test
    @Order(4)
    @Disabled("Change available random generators, all must implement JumpableGenerator")
    public void allAvailable(){
        for(var type: RandomType.values()){
            Assertions.assertDoesNotThrow(() -> {
                RandomManager.globalConfiguration(type, 1234, 100);
                RandomManager.reset(0);
                RandomManager.getRandom().nextInt(0, 100);
            });
        }
    }
}
