package es.urjc.etsii.grafo.util.graph_algorithms;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class DinicTests {

    @Test
    void flowTest1() {
        Dinic dinic = new Dinic(100);
        dinic.add(0, 1, 16);
        dinic.add(0, 2, 13);
        dinic.add(1, 2, 10);
        dinic.add(1, 3, 12);
        dinic.add(2, 1, 4);
        dinic.add(2, 4, 14);
        dinic.add(3, 2, 9);
        dinic.add(3, 5, 20);
        dinic.add(4, 3, 7);
        dinic.add(4, 5, 4);
        assertEquals(23, dinic.flow(0, 5));
    }

    @Test
    void flowTest2() {
        Dinic dinic = new Dinic(100);
        dinic.add(0, 1, 3);
        dinic.add(0, 2, 7);
        dinic.add(1, 3, 9);
        dinic.add(1, 4, 9);
        dinic.add(2, 1, 9);
        dinic.add(2, 4, 9);
        dinic.add(2, 5, 4);
        dinic.add(3, 5, 3);
        dinic.add(4, 5, 7);
        dinic.add(0, 4, 10);
        assertEquals(14, dinic.flow(0, 5));
    }

    @Test
    void flowTest3() {
        Dinic dinic = new Dinic(100);
        dinic.add(0, 1, 10);
        dinic.add(0, 2, 10);
        dinic.add(1, 3, 4);
        dinic.add(1, 4, 8);
        dinic.add(1, 2, 2);
        dinic.add(2, 4, 9);
        dinic.add(3, 5, 10);
        dinic.add(4, 3, 6);
        dinic.add(4, 5, 10);
        assertEquals(19, dinic.flow(0, 5));
    }
}
