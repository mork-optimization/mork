package es.urjc.etsii.grafo.util.collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class BitSetTest {

    @ParameterizedTest
    @ValueSource(ints = {-1, -5, Integer.MIN_VALUE})
    void negativeOrZeroSize(int size){
        Assertions.assertThrows(NegativeArraySizeException.class, () -> new BitSet(size));
    }

    @Test
    void addTest(){
        BitSet set = new BitSet(100);
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
        BitSet set = new BitSet(100);
        assertFalse(set.contains(n));
        set.add(n);
        assertTrue(set.contains(n));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 5, 20})
    void remove(int n){
        BitSet set = new BitSet(100);
        assertFalse(set.remove(n));
        set.add(n);
        assertTrue(set.remove(n));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 5, 20})
    void removeMultiple(int n){
        var list = Arrays.asList(1,2,3,4,6,7,8,9,10);
        BitSet set = new BitSet(100);
        set.addAll(list);
        assertFalse(set.remove(n));
        set.add(n);
        assertTrue(set.remove(n));
    }

    @Test
    void testSize(){
        BitSet set = new BitSet(100);
        var list = Arrays.asList(1, 2, 3, 7, 20);
        assertTrue(set.isEmpty());
        assertTrue(set.size() == 0);
        set.addAll(list);
        assertEquals(list.size(), set.size());
        assertFalse(set.isEmpty());
    }

    @Test
    void iterator(){
        BitSet set = new BitSet(100);
        var list = Arrays.asList(1,2,3,4,6,7,8,9,10);
        set.addAll(list);
        assertEquals(set.size(), list.size());
        for (int integer : set) {
            assertTrue(list.contains(integer));
        }
        assertEquals(set.size(), list.size());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -50, Integer.MIN_VALUE, Integer.MAX_VALUE, 1000, 64})
    void addOutOfRange(int n){
        BitSet set = new BitSet(10);
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> set.add(n));
    }

    @Test
    void checkIteratorNoException(){
        BitSet set = new BitSet(10);
        int total = 4 + 7 + 8;
        set.add(4);
        set.add(7);
        set.add(8);
        for (Integer integer : set) {
            total -= integer;
        }
        assertEquals(0, total);
    }

    @Test
    void checkIterator(){
        BitSet set = new BitSet(10);
        set.add(4);
        set.add(7);
        set.add(8);

        var it = set.iterator();

        assertTrue(it.hasNext());
        assertDoesNotThrow(it::next);
        assertTrue(it.hasNext());
        assertDoesNotThrow(it::next);
        assertTrue(it.hasNext());
        assertDoesNotThrow(it::next);

        assertFalse(it.hasNext());
        assertThrows(RuntimeException.class, it::next);
    }
}
