package es.urjc.etsii.grafo.io;

/**
 * The result of the execution of an algorithm
 * Contains all the generated solutions and some stats about them
 */
public class SimplifiedResult {

    private final String algorithmName;
    private final String instanceName;
    private final String avgValue;
    private final String bestValue;
    private final String std;
    private final String avgTimeInSeconds;
    private final String totalTimeInSeconds;

    public SimplifiedResult(String algorithmName, String instanceName, String avgValue, String bestValue, String std, String avgTimeInSeconds, String totalTimeInSeconds) {
        this.algorithmName = algorithmName;
        this.instanceName = instanceName;
        this.avgValue = avgValue;
        this.bestValue = bestValue;
        this.std = std;
        this.avgTimeInSeconds = avgTimeInSeconds;
        this.totalTimeInSeconds = totalTimeInSeconds;
    }

    public String getAlgorithmName() {
        return algorithmName;
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

    public String getAvgTimeInSeconds() {
        return avgTimeInSeconds;
    }

    public String getTotalTimeInSeconds() {
        return totalTimeInSeconds;
    }

    @Override
    public String toString() {
        return "SimplifiedResult{" +
                "algorythmName='" + algorithmName + '\'' +
                ", instanceName='" + instanceName + '\'' +
                ", avgValue='" + avgValue + '\'' +
                ", bestValue='" + bestValue + '\'' +
                ", std='" + std + '\'' +
                ", avgTime='" + avgTimeInSeconds + '\'' +
                ", bestTime='" + totalTimeInSeconds + '\'' +
                '}';
    }
}
