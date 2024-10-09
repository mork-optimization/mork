package es.urjc.etsii.grafo.util.collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.*;

import static es.urjc.etsii.grafo.util.collections.BitSet.*;
import static java.lang.Integer.valueOf;
import static org.junit.jupiter.api.Assertions.*;

public class BitSetTest {

    @ParameterizedTest
    @ValueSource(ints = {-1, -5, Integer.MIN_VALUE})
    void negativeOrZeroSize(int size){
        Assertions.assertThrows(NegativeArraySizeException.class, () -> new BitSet(size));
    }

    @Test
    void testAddGet(){
        BitSet set = new BitSet(100);
        assertFalse(set.get(1));
        assertTrue(set.add(1));
        assertTrue(set.get(1));
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
        int bef = set.hashCode();
        set.add(2, 2); // Does nothing
        int after = set.hashCode();
        assertEquals(bef, after);

        assertThrows(IndexOutOfBoundsException.class, () -> set.get(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> set.get(100*2));
    }

    @Test
    void addTestJDK(){
        BitSet set = new BitSet(100);
        assertTrue(set.add(valueOf(1)));
        assertFalse(set.add(valueOf(1)));

        assertTrue(set.add(valueOf(2)));
        assertFalse(set.add(valueOf(2)));

        assertFalse(set.add(valueOf(1)));

        assertTrue(set.add(valueOf(10)));
        assertTrue(set.add(valueOf(12)));
        assertTrue(set.add(valueOf(14)));
        assertFalse(set.add(valueOf(10)));
        assertFalse(set.add(valueOf(12)));
        assertFalse(set.add(valueOf(14)));

        assertFalse(set.add(valueOf(2)));
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
    void containsJDK(Integer n){
        BitSet set = new BitSet(100);
        assertFalse(set.contains(n));
        set.add(n);
        assertTrue(set.contains(n));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 5, 20})
    void testRemove(int n){
        BitSet set = new BitSet(100);
        assertFalse(set.remove(n));
        set.add(n);
        assertTrue(set.remove(n));

        assertThrows(IndexOutOfBoundsException.class, () -> set.remove(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> set.remove(100*2));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 5, 20})
    void testRemoveWithRange(int n){
        BitSet set = new BitSet(100);
        assertFalse(set.remove(n));
        set.add(n);
        set.remove(n, n);
        assertTrue(set.contains(n));
        set.remove(n, n+1);
        assertFalse(set.contains(n));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 5, 20})
    void testRemoveJDK(Integer n){
        BitSet set = new BitSet(100);
        assertFalse(set.remove((Object) n));
        set.add(n);
        assertTrue(set.remove((Object) n));
        Set<Integer> myset = new BitSet(100);
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
    void testIterator(){
        BitSet set = new BitSet(100);
        var list = Arrays.asList(1,2,3,4,6,7,8,9,10);
        set.addAll(list);
        assertEquals(set.size(), list.size());
        for (int integer : set) {
            assertTrue(list.contains(integer));
        }
        assertEquals(set.size(), list.size());
    }

    @Test
    void testIteratorRemove(){
        BitSet set = new BitSet(100);
        set.addAll(Arrays.asList(1,2,3,4,6,7,8,9,10,99));
        for (var iterator = set.iterator(); iterator.hasNext(); ) {
            int integer = iterator.next();
            if(integer == 4 || integer == 1 || integer == 99){
                iterator.remove();
            }
        }

        var correct = new BitSet(100);
        correct.addAll(Arrays.asList(2,3,6,7,8,9,10));
        assertEquals(correct, set);
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

    @Test
    void testToString(){
        var set = new BitSet(1_000_000);
        assertEquals("{}", set.toString());
        set.add(1);
        assertEquals("{1}", set.toString());
        for (int i = 0; i < 128*64+1; i++) {
            set.add(i);
        }
        var str = set.toString();
        assertEquals(128*64+1, set.size());
        assertEquals(set.size(), str.split(",").length);
    }

    @Test
    void addRemoveRanges(){
        int size = 1024;
        var set = new BitSet(size);
        for (int i = 0; i < size; i++) {
            assertEquals(-1, set.nextSetBit(i));
            assertEquals(-1, set.previousSetBit(i));

            assertEquals(i, set.nextClearBit(i));
            assertEquals(i, set.previousClearBit(i));
        }

        set.add(1, 10);
        assertEquals(9, set.size());
        assertFalse(set.contains(0));
        for (int i = 1; i < 10; i++) {
            assertTrue(set.contains(i));
        }
        for (int i = 10; i < 100; i++) {
            assertFalse(set.contains(i));
        }
        set.remove(5, 7);
        assertEquals(7, set.size());

        assertFalse(set.contains(0));
        assertTrue(set.contains(1));
        assertTrue(set.contains(2));
        assertTrue(set.contains(3));
        assertTrue(set.contains(4));
        assertFalse(set.contains(5));
        assertFalse(set.contains(6));
        assertTrue(set.contains(7));
        assertTrue(set.contains(8));
        assertTrue(set.contains(9));

        for (int i = 10; i < size; i++) {
            assertFalse(set.contains(i));
        }

        set.add(0, size);
        for (int i = 0; i < size; i++) {
            assertEquals(i, set.nextSetBit(i));
            assertEquals(i, set.previousSetBit(i));

            assertEquals(-1, set.nextClearBit(i));
            assertEquals(-1, set.previousClearBit(i));
        }
        assertEquals(size, set.size());
        set.remove(0, size);
        for (int i = 0; i < size; i++) {
            assertEquals(-1, set.nextSetBit(i));
            assertEquals(-1, set.previousSetBit(i));

            assertEquals(i, set.nextClearBit(i));
            assertEquals(i, set.previousClearBit(i));
        }
        assertEquals(0, set.size());

        assertThrows(IndexOutOfBoundsException.class, () -> set.add(-1, 1));
        assertThrows(IndexOutOfBoundsException.class, () -> set.add(1, size *2));
        assertThrows(IndexOutOfBoundsException.class, () -> set.add(size*2, size*4));
        assertThrows(IndexOutOfBoundsException.class, () -> set.add(6, 5));

        assertThrows(IndexOutOfBoundsException.class, () -> set.remove(-1, 1));
        assertThrows(IndexOutOfBoundsException.class, () -> set.remove(1, size *2));
        assertThrows(IndexOutOfBoundsException.class, () -> set.remove(size*2, size*4));
        assertThrows(IndexOutOfBoundsException.class, () -> set.remove(6, 5));
    }

    @Test
    void testHashcode(){
        int size = 10000;
        var set = new BitSet(size);
        var values = new HashSet<>(size);

        for (int i = 0; i < size; i++) {
            set.add(i);
            values.add(set.hashCode());
        }
        assertTrue(size*0.8 < values.size()); // Less than 20% of collisions
    }

    @Test
    void testNot(){
        var set1 = BitSet.of(100, 1, 7, 9);
        var set2 = BitSet.not(set1);
        assertEquals(3, set1.size());
        assertEquals(100 - set1.size(), set2.size());
        for(var i: set1){
            assertEquals(set1.contains(i), !set2.contains(i));
        }
    }

    @Test
    void testCloneSet(){
        int size = 1000;
        var set1 = new BitSet(size);

        Random r = new Random(0);
        for (int i = 0; i < size; i++) {
            set1.add(r.nextInt(size));
        }
        var set2 = set1.clone();
        assertEquals(set1, set2);
        assertEquals(set1.hashCode(), set2.hashCode());

        for (int i = 0; i < size; i++) {
            assertEquals(set1.contains(i), set2.contains(i));
        }

        set2.xor(set1);
        assertEquals(0, set2.size());
    }

    @Test
    void cloneSetCollections(){
        int size = 1000;
        var set1 = new BitSet(size);

        Random r = new Random(0);
        for (int i = 0; i < size; i++) {
            set1.add(r.nextInt(size));
        }
        var set2 = new BitSet(size, set1);
        assertEquals(set1, set2);
        assertEquals(set1.hashCode(), set2.hashCode());

        for (int i = 0; i < size; i++) {
            assertEquals(set1.contains(i), set2.contains(i));
        }

        set2.xor(set1);
        assertEquals(0, set2.size());
    }

    @Test
    void flip(){
        int size = 1000;
        var set = new BitSet(size);
        assertFalse(set.contains(0));
        assertEquals(0, set.size());

        set.flip(0);
        assertTrue(set.contains(0));
        assertEquals(1, set.size());

        set.flip(0);
        assertFalse(set.contains(0));
        assertEquals(0, set.size());

        set.flip(0, size);
        assertEquals(size, set.size());
        set.flip(0, size);
        assertEquals(0, set.size());

        set.flip(0, 0); // Does nothing
        assertEquals(0, set.size());
        set.flip(0, 1); // One bit flip
        assertTrue(set.contains(0));

        assertThrows(IndexOutOfBoundsException.class, () -> set.flip(-1, 5));
        assertThrows(IndexOutOfBoundsException.class, () -> set.flip(5, size*2));
        assertThrows(IndexOutOfBoundsException.class, () -> set.flip(5, -1));
        assertThrows(IndexOutOfBoundsException.class, () -> set.flip(-1));
    }

    @Test
    void copyFromCollectionAndStream(){
        var list = Arrays.asList(1, 3, 5, 7, 9);
        var set = new BitSet(100, list);
        assertEquals(set.size(), list.size());
        for (int i = 0; i < 100; i++) {
            assertEquals(list.contains(i), set.contains(i));
        }
        set.forEach(e -> assertTrue(list.contains(e)));
        set.stream().parallel().forEach(e -> assertTrue(list.contains(e)));
    }

    @Test
    void testCapacity(){
        assertThrows(NegativeArraySizeException.class, () -> new BitSet(-1));
        assertEquals(0, new BitSet(0).getCapacity());
        assertEquals(1, new BitSet(1).getCapacity());
        assertEquals(2, new BitSet(2).getCapacity());
        assertEquals(64, new BitSet(64).getCapacity());
        assertEquals(63, new BitSet(63).getCapacity());
        assertEquals(47, new BitSet(47).getCapacity());
        assertEquals(65, new BitSet(65).getCapacity());
        assertEquals(127, new BitSet(127).getCapacity());
        assertEquals(128, new BitSet(128).getCapacity());
        assertEquals(129, new BitSet(129).getCapacity());
    }

    @Test
    void retainAllRemoveAll(){
        int size = 1024;
        BitSet set = new BitSet(size);
        set.addAll(Arrays.asList(1, 3, 5, 7, 9, 11, 13, 15, 17));
        set.retainAll(Arrays.asList(1,2,3,4,5,6,7,8,9));
        assertEquals(of(size, 1,3,5,7,9), set);
        set.retainAll(of(1024, 1,2,3,4,6,7,8,9));
        assertEquals(of(size, 1,3,7,9), set);

        assertTrue(set.removeAll(Arrays.asList(1, 5)));
        assertEquals(of(size, 3,7,9), set);
        assertFalse(set.removeAll(Arrays.asList(1, 5)));

        set.removeAll(of(1024, 3,9));
        assertEquals(of(size, 7), set);
    }

    @Test
    void testIntersects(){
        int size = 1024;
        BitSet set = new BitSet(size);
        set.add(1);
        BitSet set2 = new BitSet(size);
        assertFalse(set.intersects(set2));
        set2.add(2);
        assertFalse(set.intersects(set2));
        set.add(2);
        assertTrue(set.intersects(set2));
        set2.add(1);
        assertTrue(set.intersects(set2));
        set2.remove(2);
        assertTrue(set.intersects(set2));
    }

    @Test
    void testContainsAll(){
        int size = 1024;
        BitSet set = new BitSet(size);
        BitSet set2 = new BitSet(size);
        var set3 = new HashSet<Integer>();

        assertTrue(set.containsAll(set2));
        assertTrue(set.containsAll(set3));
        assertTrue(set2.containsAll(set));
        assertTrue(set2.containsAll(set3));
        set.add(1);
        assertTrue(set.containsAll(set2));
        assertFalse(set2.containsAll(set));
        set2.add(2);
        assertFalse(set.containsAll(set2));
        assertFalse(set2.containsAll(set));
        set.add(2);
        assertTrue(set.containsAll(set2));
        assertFalse(set2.containsAll(set));
        set2.add(1);
        assertTrue(set.containsAll(set2));
        assertTrue(set2.containsAll(set));
        set2.remove(2);
        assertTrue(set.containsAll(set2));
        assertFalse(set2.containsAll(set));
    }

    @Test
    void invalidSizes(){
        var set = new BitSet(1024);
        assertThrows(IndexOutOfBoundsException.class, () -> set.add(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> set.add(1024));
        assertThrows(IndexOutOfBoundsException.class, () -> set.add(5, 4));
    }

    @Test
    void testUnion(){
        int size = 1024;
        assertEquals(union(of(size, 0), of(size, 4, 5)), of(size, 0, 4, 5));
        assertEquals(union(of(size), of(size, 4, 5)), of(size, 4, 5));
        assertEquals(union(of(size), of(size)), of(size));
        assertEquals(union(of(size, 1, 2, 3), of(size, 2, 3)), of(size, 1, 2, 3));
        assertEquals(union(of(size, 2, 3), of(size, 1, 2, 3)), of(size, 1, 2, 3));
        assertEquals(union(of(size, 1, 2, 3, 5), of(size, 1, 2, 3, 4)), of(size, 1, 2, 3, 4, 5));

        assertThrows(IllegalArgumentException.class, () -> union(of(size), of(size * 2)));
    }

    @Test
    void testIntersection(){
        int size = 1024;
        assertEquals(intersection(of(size, 0), of(size, 4, 5)), of(size));
        assertEquals(intersection(of(size), of(size, 4, 5)), of(size));
        assertEquals(intersection(of(size), of(size)), of(size));
        assertEquals(intersection(of(size, 1, 2, 3), of(size, 2, 3)), of(size, 2, 3));
        assertEquals(intersection(of(size, 2, 3), of(size, 1, 2, 3)), of(size, 2, 3));
        assertEquals(intersection(of(size, 1, 2, 3, 5), of(size, 1, 2, 3, 4)), of(size, 1, 2, 3));

        assertThrows(IllegalArgumentException.class, () -> intersection(of(size), of(size * 2)));

    }

    @Test
    void testDifference(){
        int size = 1024;
        assertEquals(difference(of(size, 0), of(size, 4, 5)), of(size, 0));
        assertEquals(difference(of(size), of(size, 4, 5)), of(size));
        assertEquals(difference(of(size), of(size)), of(size));
        assertEquals(difference(of(size, 1, 2, 3), of(size, 2, 3)), of(size, 1));
        assertEquals(difference(of(size, 2, 3), of(size, 1, 2, 3)), of(size));
        assertEquals(difference(of(size, 1, 2, 3, 5), of(size, 1, 2, 3, 4)), of(size, 5));

        assertThrows(IllegalArgumentException.class, () -> difference(of(size), of(size * 2)));

    }

    @Test
    void testSymmetricDifference(){
        int size = 1024;
        assertEquals(symmetricDifference(of(size, 0), of(size, 4, 5)), of(size, 0, 4, 5));
        assertEquals(symmetricDifference(of(size), of(size, 4, 5)), of(size, 4, 5));
        assertEquals(symmetricDifference(of(size), of(size)), of(size));
        assertEquals(symmetricDifference(of(size, 1, 2, 3), of(size, 2, 3)), of(size, 1));
        assertEquals(symmetricDifference(of(size, 2, 3), of(size, 1, 2, 3)), of(size, 1));
        assertEquals(symmetricDifference(of(size, 1, 2, 3, 5), of(size, 1, 2, 3, 4)), of(size, 4, 5));

        assertThrows(IllegalArgumentException.class, () -> symmetricDifference(of(size), of(size * 2)));
    }

    @Test
    void testEquals(){
        var set = of(10, 1, 3, 5);
        assertNotEquals(set, this);
        assertNotEquals(set, of(1024, 1, 3, 5));
        assertEquals(set, of(10, 1, 3, 5));
        assertEquals(set, set);
        assertNotEquals(set, of(10, 1, 5));
    }


    static void trash(int n){
        System.out.println(n);
    }

    @Test
    void testSpliterator(){
        var set = of(1024, 1, 2, 4, 6, 8);
        Spliterator<Integer> spliterator = set.spliterator();
        assertEquals(5, spliterator.estimateSize());
        assertTrue(spliterator.tryAdvance(BitSetTest::trash));
        assertTrue(spliterator.tryAdvance(BitSetTest::trash));
        assertTrue(spliterator.tryAdvance(BitSetTest::trash));
        assertTrue(spliterator.tryAdvance(BitSetTest::trash));
        assertTrue(spliterator.tryAdvance(BitSetTest::trash));
        assertFalse(spliterator.tryAdvance(BitSetTest::trash));
    }

    @Test
    void testNextPreviousBitExceptions(){
        int size = 1024;
        var set = of(size, 1, 2, 4, 6, 8);

        assertThrows(IndexOutOfBoundsException.class, () -> set.previousSetBit(-2));
        assertThrows(IndexOutOfBoundsException.class, () -> set.previousSetBit(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> set.previousSetBit(size*2));
        assertThrows(IndexOutOfBoundsException.class, () -> set.previousClearBit(-2));
        assertThrows(IndexOutOfBoundsException.class, () -> set.previousClearBit(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> set.previousClearBit(size*2));

        assertThrows(IndexOutOfBoundsException.class, () -> set.nextSetBit(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> set.nextSetBit(-2));
        assertThrows(IndexOutOfBoundsException.class, () -> set.nextSetBit(size*2));

        assertThrows(IndexOutOfBoundsException.class, () -> set.nextClearBit(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> set.nextClearBit(-2));
        assertThrows(IndexOutOfBoundsException.class, () -> set.nextClearBit(size*2));
    }

    @Test
    void validateCapacityBehavior(){
        var set1 = new BitSet(6);
        assertThrows(IndexOutOfBoundsException.class, () -> set1.add(7));
        assertThrows(IndexOutOfBoundsException.class, () -> set1.remove(7));
        assertThrows(IndexOutOfBoundsException.class, () -> set1.get(7));
    }

    @Test
    void testXor(){
        var set1 = BitSet.of(10, 1, 5, 8);
        var set2 = BitSet.of(10, 2, 4, 5);
        var expectedSet3 = BitSet.of(10, 1, 2, 4, 8);
        set1.xor(set2);

        assertEquals(expectedSet3, set1);
    }
}
