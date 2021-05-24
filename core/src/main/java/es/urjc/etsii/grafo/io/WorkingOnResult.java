package es.urjc.etsii.grafo.io;

import es.urjc.etsii.grafo.solution.Solution;

import java.util.ArrayList;
import java.util.Optional;

/**
 * The result of the execution of an algorithm
 * Contains all the generated solutions and some stats about them
 */
public class WorkingOnResult<S extends Solution<I>, I extends Instance> {

    private final ArrayList<SolutionData> solutions;
    private Instance instance;
    private S best;
    private final String algorithmName;
    private final String instanceName;

    /**
     * Initialize a result class.
     *
     * @param nSolutions   estimated number of solutions we are generating
     * @param algName      Algorithm's name
     * @param instanceName Instance name
     */
    public WorkingOnResult(int nSolutions, String algName, String instanceName) {
        this.solutions = new ArrayList<>(nSolutions);
        this.algorithmName = algName;
        this.instanceName = instanceName;
    }

    /**
     * Store new result
     * @param s Calculated solution
     * @param totalTime Time used to calculate the given solution
     * @param timeToBest Time until last modification was made, i.e. time to best solution.
     */
    public synchronized void addSolution(S s, long totalTime, long timeToBest) {
        if(this.best == null){
            this.best = s;
        }
        this.best = best.getBetterSolution(s);
        this.solutions.add(new SolutionData(s.getScore(), totalTime, timeToBest));
        if (instance == null) {
            instance = s.getInstance();
        } else if (instance != s.getInstance()) {
            throw new AssertionError(String.format("Instance mismatch, expected %s got %s", instance.getName(), s.getInstance().getName()));
        }
    }

    public double getAverageExecTimeInSeconds() {
        double totalTime = 0;
        for (var solution : this.solutions) {
            totalTime += solution.getExecutionTimeInNanos();
        }
        return totalTime / this.solutions.size() / 1_000_000_000; // 1 second = 10^9 seconds
    }

    public String getFormattedAverageFO(int nDecimales) {
        String formatString = "%." + nDecimales + "f";
        return String.format(formatString, this.getAverageFOValue());
    }

    public double getAverageFOValue() {
        double value = 0;
        for (var solution : solutions) {
            value += solution.getValue();
        }
        return value / solutions.size();
    }

    public double getTotalTimeInSeconds() {
        long totalTime = 0;
        for (var solution : solutions) {
            totalTime += solution.getExecutionTimeInNanos();
        }
        return totalTime / (double) 1_000_000_000; // 1 second = 10^9 seconds
    }

    /**
     * Returns the standard deviation for the objective values of all solutions
     *
     * @return The STD
     */
    public double getStd() {
        double total = 0;
        double avg = getAverageFOValue();
        for (var solution : this.solutions) {
            // La varianza es la suma de las diferencias al cuadrado
            // dividido entre el tamaño del conjunto menos 1
            // La desviacion estandar es sqrt(varianza)
            double difference = solution.getValue() - avg;
            total += difference * difference;
        }

        return Math.sqrt(total / (this.solutions.size() - 1));
    }

    public S getBestSolution() {
        return best;
    }

    public String getAlgorithmName() {
        return this.algorithmName;
    }

    public String getInstanceName() {
        return instanceName;
    }

    // TODO review result to string
    public String toString() {
        return "Instance Name: " + this.instanceName
                + "\nAlgorythm Used: " + this.algorithmName
                + "\nAverage Obj.Function: " + getFormattedAverageFO(2)
                + "\nExecution Time (ms): " + this.getAverageExecTimeInSeconds()
                + "\n---------------------------------------------------------";
    }

    public Optional<SimplifiedResult> finish() {
        if(this.best == null){
            return Optional.empty();
        }
        return Optional.of(new SimplifiedResult(
                this.algorithmName,
                this.instanceName,
                Double.toString(this.getAverageFOValue()),
                Double.toString(this.getBestSolution().getScore()),
                Double.toString(this.getStd()),
                Double.toString(this.getAverageExecTimeInSeconds()),
                Double.toString(this.getTotalTimeInSeconds())
        ));
    }

    /**
     * Let the solutions and the instances get garbage collected, only keep whatever is interesting for us
     */
    private static class SolutionData {
        private final double value;
        private final long executionTimeInNanos;
        private final long timeToTargetInNanos;

        public SolutionData(double value, long executionTimeInNanos, long timeToTargetInNanos) {
            this.value = value;
            this.executionTimeInNanos = executionTimeInNanos;
            this.timeToTargetInNanos = timeToTargetInNanos;
        }

        public double getValue() {
            return value;
        }

        public long getExecutionTimeInNanos() {
            return executionTimeInNanos;
        }

        public long getTimeToTargetInNanos() {
            return timeToTargetInNanos;
        }
    }
}
