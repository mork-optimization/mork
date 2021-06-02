package es.urjc.etsii.grafo.util;

import jnt.scimark2.ScimarkAPI;

public class BenchmarkUtil {
    /**
     * Run a small benchmark and return score.
     * @return score as a double
     */
    public static double getBenchmarkScore(){
        var result = ScimarkAPI.runBenchmark();
        return result.getScore();
    }
}
