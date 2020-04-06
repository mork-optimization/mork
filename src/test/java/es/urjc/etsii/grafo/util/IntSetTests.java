package es.urjc.etsii.grafo.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class IntSetTests {

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -5, Integer.MIN_VALUE})
    void negativeOrZeroSize(int size){
        Assertions.assertThrows(IllegalArgumentException.class, () -> new IntSet(-5));
    }

    @Test
    void addTest(){
        IntSet set = new IntSet(100);
        assertTrue(set.add(1));
        assertFalse(set.add(1));

        assertTrue(set.add(2));
        assertFalse(set.add(2));

        assertFalse(set.add(1));

        assertTrue(set.add(10));
        assertTrue(set.add(12));
        assertTrue(set.add(14));
        assertFalse(set.add(10));
        assertFalse(set.add(12));
        assertFalse(set.add(14));

        assertFalse(set.add(2));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 5, 20})
    void contains(int n){
        IntSet set = new IntSet(100);
        assertFalse(set.contains(n));
        set.add(n);
        assertTrue(set.contains(n));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 5, 20})
    void remove(int n){
        IntSet set = new IntSet(100);
        assertFalse(set.remove(n));
        set.add(n);
        assertTrue(set.remove(n));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 5, 20})
    void removeMultiple(int n){
        var list = Arrays.asList(1,2,3,4,6,7,8,9,10);
        IntSet set = new IntSet(100);
        set.addAll(list);
        assertFalse(set.remove(n));
        set.add(n);
        assertTrue(set.remove(n));
    }

    @Test
    void testSize(){
        IntSet set = new IntSet(100);
        var list = Arrays.asList(1, 2, 3, 7, 20);
        assertTrue(set.isEmpty());
        assertTrue(set.size() == 0);
        set.addAll(list);
        assertEquals(list.size(), set.size());
        assertFalse(set.isEmpty());
    }

    @Test
    void iterator(){
        IntSet set = new IntSet(100);
        var list = Arrays.asList(1,2,3,4,6,7,8,9,10);
        set.addAll(list);
        assertEquals(set.size(), list.size());
        for (int integer : set) {
            assertTrue(list.contains(integer));
        }
        assertEquals(set.size(), list.size());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -50, Integer.MIN_VALUE, Integer.MAX_VALUE, 1000, 10})
    void addOutOfRange(int n){
        IntSet set = new IntSet(10);
        Assertions.assertThrows(IllegalArgumentException.class, () -> set.add(n));
    }
}
