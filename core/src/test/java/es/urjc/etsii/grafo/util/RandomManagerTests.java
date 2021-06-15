package es.urjc.etsii.grafo.util;


import org.junit.jupiter.api.*;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RandomManagerTests {

    @Test
    @Order(1)
    public void initializationTest(){
        int initialseed = 123456;
        RandomManager manager = new RandomManager(initialseed);
        var myRandom = RandomManager.getRandom();
        var random = new Random(initialseed);

        for (int i = 0; i < 100; i++) {
            Assertions.assertEquals(myRandom.nextInt(), random.nextInt());
        }

        random.setSeed(initialseed);
        RandomManager.reset();

        for (int i = 0; i < 100; i++) {
            Assertions.assertEquals(myRandom.nextInt(), random.nextInt());
        }
    }

    @Test
    @Order(2)
    public void generationTest(){
        int initialseed = 123456;
        RandomManager manager = new RandomManager(initialseed);
        var myRandom = RandomManager.getRandom();
        var random = new Random(initialseed);

        for (int i = 0; i < 100; i++) {
            Assertions.assertEquals(myRandom.nextInt(), random.nextInt());
        }

        random.setSeed(initialseed);
        RandomManager.reset();

        for (int i = 0; i < 100; i++) {
            Assertions.assertEquals(myRandom.nextInt(), random.nextInt());
        }
    }

    @Test
    @Order(3)
    public void concurrentGenerationTest(){
        int initialseed = 123456;
        RandomManager manager = new RandomManager(initialseed);
        var executor = Executors.newFixedThreadPool(4);
        for (int repetitions = 0; repetitions < 8; repetitions++) {
            var iteration = repetitions;
            executor.submit(()->{
                RandomManager.reset(iteration);
                var myRandom = RandomManager.getRandom();
                var random = new Random(initialseed);

                for (int i = 0; i < 1000; i++) {
                    Assertions.assertEquals(myRandom.nextInt(), random.nextInt());
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

}
