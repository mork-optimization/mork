package es.urjc.etsii.grafo.util;


import org.junit.jupiter.api.*;

import java.util.Random;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RandomManagerTests {

    @Test
    @Order(1)
    public void initializationTest(){
        int initialseed = 123456;
        Assertions.assertThrows(IllegalStateException.class, () -> RandomManager.nextInt(0, 10));
        Assertions.assertThrows(IllegalStateException.class, RandomManager::reset);
        Assertions.assertThrows(IllegalStateException.class, RandomManager::getRandom);

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

}
