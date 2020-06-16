package es.urjc.etsii.grafo.util;

import es.urjc.etsii.grafo.util.collections.DisjointSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class DisjointSetTests {

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -5, Integer.MIN_VALUE})
    void negativeOrZeroSize(int size){
        Assertions.assertThrows(IllegalArgumentException.class, () -> new DisjointSet(size));
    }

    @Test
    void joinTest(){
        DisjointSet set = new DisjointSet(100);
        assertNotEquals(set.find(1), set.find(3));
        assertNotEquals(set.find(1), set.find(2));
        set.union(1, 3);
        assertEquals(set.find(1), set.find(3));
        assertNotEquals(set.find(1), set.find(2));
    }

    @Test
    void joinWithOffsetTest(){
        DisjointSet set = new DisjointSet(100);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 5, 20})
    void size(int n){
        DisjointSet set = new DisjointSet(100);
        assertEquals(1, set.size(n));
        set.union(n, 50);
        assertEquals(2, set.size(n));
        set.union(60, 61);
        set.union(n, 60);
        assertEquals(4, set.size(n));
    }


    @ParameterizedTest
    @ValueSource(ints = {-1, -50, Integer.MIN_VALUE, Integer.MAX_VALUE, 1000, 10})
    void findOutOfRange(int n){
        DisjointSet set = new DisjointSet(10);
        Assertions.assertThrows(IllegalArgumentException.class, () -> set.find(n));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -50, Integer.MIN_VALUE, Integer.MAX_VALUE, 1000, 10})
    void sizeOutOfRange(int n){
        DisjointSet set = new DisjointSet(10);
        Assertions.assertThrows(IllegalArgumentException.class, () -> set.size(n));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -50, Integer.MIN_VALUE, Integer.MAX_VALUE, 1000, 10})
    void unionOutOfRange(int n){
        DisjointSet set = new DisjointSet(10);
        Assertions.assertThrows(IllegalArgumentException.class, () -> set.union(1, n));
        Assertions.assertThrows(IllegalArgumentException.class, () -> set.union(n,1));
    }

    @Test
    void cloneTest(){
        DisjointSet set = new DisjointSet(10);
        set.union(1, 2);
        set.union(3, 4);
        DisjointSet copy = new DisjointSet(set);
        assertEquals(set.size(1), copy.size(1));
        assertEquals(set.size(2), copy.size(2));
        assertEquals(set.size(3), copy.size(4));
        assertEquals(set.find(1), copy.find(2));
        assertEquals(set.find(3), copy.find(4));
    }

}
