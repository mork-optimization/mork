package es.urjc.etsii.grafo.io;

/**
 * The result of the execution of an algorithm
 * Contains all the generated solutions and some stats about them
 */
public class Result {

    private final String algorythmName;
    private final String instanceName;
    private final String avgValue;
    private final String bestValue;
    private final String std;
    private final String avgTimeInMs;
    private final String totalTimeInMs;

    public Result(String algorythmName, String instanceName, String avgValue, String bestValue, String std, String avgTimeInMs, String totalTimeInMs) {
        this.algorythmName = algorythmName;
        this.instanceName = instanceName;
        this.avgValue = avgValue;
        this.bestValue = bestValue;
        this.std = std;
        this.avgTimeInMs = avgTimeInMs;
        this.totalTimeInMs = totalTimeInMs;
    }

    public String getAlgorythmName() {
        return algorythmName;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public String getAvgValue() {
        return avgValue;
    }

    public String getBestValue() {
        return bestValue;
    }

    public String getStd() {
        return std;
    }

    public String getAvgTimeInMs() {
        return avgTimeInMs;
    }

    public String getTotalTimeInMs() {
        return totalTimeInMs;
    }

    @Override
    public String toString() {
        return "Result{" +
                "algorythmName='" + algorythmName + '\'' +
                ", instanceName='" + instanceName + '\'' +
                ", avgValue='" + avgValue + '\'' +
                ", bestValue='" + bestValue + '\'' +
                ", std='" + std + '\'' +
                ", avgTime='" + avgTimeInMs + '\'' +
                ", bestTime='" + totalTimeInMs + '\'' +
                '}';
    }
}
