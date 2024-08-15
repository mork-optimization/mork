package es.urjc.etsii.grafo.algorithms;

import es.urjc.etsii.grafo.testutil.TestMove;
import es.urjc.etsii.grafo.testutil.TestSolution;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class FModeTest {

    @Test
    void minimizeMoveComp(){
        var data = TestMove.generateSeq(-1, 7, 3, 9);
        data.sort(FMode.MINIMIZE.comparatorMove());
        assertEquals(-1, data.get(0).getValue());
        assertEquals(3, data.get(1).getValue());
        assertEquals(7, data.get(2).getValue());
        assertEquals(9, data.get(3).getValue());
    }

    @Test
    void maximizeMoveComp(){
        var data = TestMove.generateSeq(-1, 7, 3, 9);
        data.sort(FMode.MAXIMIZE.comparatorMove());
        assertEquals(9, data.get(0).getValue());
        assertEquals(7, data.get(1).getValue());
        assertEquals(3, data.get(2).getValue());
        assertEquals(-1, data.get(3).getValue());
    }

    @Test
    void minimizeSolComp(){
        var data = TestSolution.from(-1, 7, 3, 9);
        Arrays.sort(data, FMode.MINIMIZE.comparator());
        assertEquals(-1, data[0].getScore());
        assertEquals(3, data[1].getScore());
        assertEquals(7, data[2].getScore());
        assertEquals(9, data[3].getScore());
    }

    @Test
    void maximizeSolComp(){
        var data = TestSolution.from(-1, 7, 3, 9);
        Arrays.sort(data, FMode.MAXIMIZE.comparator());
        assertEquals(9, data[0].getScore());
        assertEquals(7, data[1].getScore());
        assertEquals(3, data[2].getScore());
        assertEquals(-1, data[3].getScore());
    }

    @Test
    void minimizeImproves(){
        assertTrue(FMode.MINIMIZE.improves(-0.01));
        assertTrue(FMode.MINIMIZE.improves(-1));
        assertTrue(FMode.MINIMIZE.improves(Integer.MIN_VALUE));
        assertFalse(FMode.MINIMIZE.improves(0));
        assertFalse(FMode.MINIMIZE.improves(-0.000000000000000000000000000000000000000000000000001D));
        assertFalse(FMode.MINIMIZE.improves(7));
        assertFalse(FMode.MINIMIZE.improves(Integer.MAX_VALUE));
    }

    @Test
    void maximizeImproves(){
        assertFalse(FMode.MAXIMIZE.improves(-0.01));
        assertFalse(FMode.MAXIMIZE.improves(-1));
        assertFalse(FMode.MAXIMIZE.improves(Integer.MIN_VALUE));
        assertFalse(FMode.MAXIMIZE.improves(0));
        assertFalse(FMode.MAXIMIZE.improves(-0.000000000000000000000000000000000000000000000000001D));
        assertTrue(FMode.MAXIMIZE.improves(7));
        assertTrue(FMode.MAXIMIZE.improves(Integer.MAX_VALUE));
    }

    @Test
    void minimizeIsBetter(){
        assertTrue(FMode.MINIMIZE.isBetter(-1, 0));
        assertFalse(FMode.MINIMIZE.isBetter(1, 0));
        assertFalse(FMode.MINIMIZE.isBetter(-1, -1));
        assertFalse(FMode.MINIMIZE.isBetter(0, 0));
        assertTrue(FMode.MINIMIZE.isBetter(Integer.MIN_VALUE, Integer.MAX_VALUE));
        assertFalse(FMode.MINIMIZE.isBetter(Integer.MAX_VALUE, Integer.MIN_VALUE));
        assertTrue(FMode.MINIMIZE.isBetter(123, 123.1));
        assertFalse(FMode.MINIMIZE.isBetter(0, 0.0000000000000000000000000000001D));
    }

    @Test
    void maximizeIsBetter(){
        assertFalse(FMode.MAXIMIZE.isBetter(-1, 0));
        assertTrue(FMode.MAXIMIZE.isBetter(1, 0));
        assertFalse(FMode.MAXIMIZE.isBetter(-1, -1));
        assertFalse(FMode.MAXIMIZE.isBetter(0, 0));
        assertFalse(FMode.MAXIMIZE.isBetter(Integer.MIN_VALUE, Integer.MAX_VALUE));
        assertTrue(FMode.MAXIMIZE.isBetter(Integer.MAX_VALUE, Integer.MIN_VALUE));
        assertTrue(FMode.MAXIMIZE.isBetter(123.1, 123));
        assertFalse(FMode.MAXIMIZE.isBetter(0.0000000000000000000000000000001D, 0));
    }

    @Test
    void minimizeIsBetterEq(){
        assertTrue(FMode.MINIMIZE.isBetterOrEqual(-1, 0));
        assertFalse(FMode.MINIMIZE.isBetterOrEqual(1, 0));
        assertTrue(FMode.MINIMIZE.isBetterOrEqual(-1, -1));
        assertTrue(FMode.MINIMIZE.isBetterOrEqual(0, 0));
        assertTrue(FMode.MINIMIZE.isBetterOrEqual(Integer.MIN_VALUE, Integer.MAX_VALUE));
        assertFalse(FMode.MINIMIZE.isBetterOrEqual(Integer.MAX_VALUE, Integer.MIN_VALUE));
        assertTrue(FMode.MINIMIZE.isBetterOrEqual(123, 123.1));
        assertTrue(FMode.MINIMIZE.isBetterOrEqual(0, 0.0000000000000000000000000000001D));
    }

    @Test
    void maximizeIsBetterEq(){
        assertFalse(FMode.MAXIMIZE.isBetterOrEqual(-1, 0));
        assertTrue(FMode.MAXIMIZE.isBetterOrEqual(1, 0));
        assertTrue(FMode.MAXIMIZE.isBetterOrEqual(-1, -1));
        assertTrue(FMode.MAXIMIZE.isBetterOrEqual(0, 0));
        assertFalse(FMode.MAXIMIZE.isBetterOrEqual(Integer.MIN_VALUE, Integer.MAX_VALUE));
        assertTrue(FMode.MAXIMIZE.isBetterOrEqual(Integer.MAX_VALUE, Integer.MIN_VALUE));
        assertTrue(FMode.MAXIMIZE.isBetterOrEqual(123.1, 123));
        assertTrue(FMode.MAXIMIZE.isBetterOrEqual(0.0000000000000000000000000000001D, 0));
    }
}
