package es.urjc.etsii.grafo.solution;

import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.testutil.TestMove;
import es.urjc.etsii.grafo.testutil.TestSolution;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ObjectiveTest {

    @Test
    void minimizeMoveComp(){
        var data = TestMove.generateSeq(-1, 7, 3, 9);
        var objective = Objective.of("Default", FMode.MINIMIZE, TestSolution::getScore, TestMove::getValue);
        data.sort(objective.comparatorMove());
        assertEquals(-1, data.get(0).getValue());
        assertEquals(3, data.get(1).getValue());
        assertEquals(7, data.get(2).getValue());
        assertEquals(9, data.get(3).getValue());
    }

    @Test
    void maximizeMoveComp(){
        var data = TestMove.generateSeq(-1, 7, 3, 9);
        var objective = Objective.of("Default", FMode.MAXIMIZE, TestSolution::getScore, TestMove::getValue);
        data.sort(objective.comparatorMove());
        assertEquals(9, data.get(0).getValue());
        assertEquals(7, data.get(1).getValue());
        assertEquals(3, data.get(2).getValue());
        assertEquals(-1, data.get(3).getValue());
    }

    @Test
    void minimizeSolComp(){
        var data = TestSolution.from(-1, 7, 3, 9);
        var objective = Objective.of("Default", FMode.MINIMIZE, TestSolution::getScore, TestMove::getValue);
        Arrays.sort(data, objective.comparator());
        assertEquals(-1, data[0].getScore());
        assertEquals(3, data[1].getScore());
        assertEquals(7, data[2].getScore());
        assertEquals(9, data[3].getScore());
    }

    @Test
    void maximizeSolComp(){
        var data = TestSolution.from(-1, 7, 3, 9);
        var objective = Objective.of("Default", FMode.MAXIMIZE, TestSolution::getScore, TestMove::getValue);
        Arrays.sort(data, objective.comparator());
        assertEquals(9, data[0].getScore());
        assertEquals(7, data[1].getScore());
        assertEquals(3, data[2].getScore());
        assertEquals(-1, data[3].getScore());
    }
}
