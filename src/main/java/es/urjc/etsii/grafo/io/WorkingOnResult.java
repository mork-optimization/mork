package es.urjc.etsii.grafo.io;

import es.urjc.etsii.grafo.solution.Solution;

import java.util.ArrayList;

/**
 * The result of the execution of an algorithm
 * Contains all the generated solutions and some stats about them
 */
public class WorkingOnResult {

    private final ArrayList<SolutionData> solutions;
    private Instance instance;
    private Solution best;
    private final String algorythmName;
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
        this.algorythmName = algName;
        this.instanceName = instanceName;
    }

    /**
     * Store new result
     *  @param s     Calculated es.urjc.etsii.grafo.solution
     * @param nanos Time used to calculate the given es.urjc.etsii.grafo.solution
     * @param timeToTarget
     */
    public void addSolution(Solution<? extends Instance> s, long nanos, long timeToTarget) {
        if(this.best == null){
            this.best = s;
        }
        this.best = best.getBetterSolution(s);
        s.setExecutionTimeInNanos(nanos);
        this.solutions.add(new SolutionData(s.getScore(), nanos, timeToTarget));
        if (instance == null) {
            instance = s.getInstance();
        } else if (instance != s.getInstance()) {
            throw new AssertionError(String.format("Instance mismatch, expected %s got %s", instance.getName(), s.getInstance().getName()));
        }
    }

    public long getAverageExecTime() {
        long totalTime = 0;
        for (var solution : this.solutions) {
            totalTime += solution.getExecutionTimeInNanos();
        }
        return totalTime / this.solutions.size() / 1_000_000; // 1 millisecond = 10^6 nanos
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

    public long getTotalTime() {
        long totalTime = 0;
        for (var solution : solutions) {
            totalTime += solution.getExecutionTimeInNanos();
        }
        return totalTime / 1_000_000;
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

    public Solution getBestSolution() {
        return best;
    }

    public String getAlgorythmName() {
        return this.algorythmName;
    }

    public String getInstanceName() {
        return instanceName;
    }

    // TODO review result to string
    public String toString() {
        return "Instance Name: " + this.instanceName
                + "\nAlgorythm Used: " + this.algorythmName
                + "\nAverage Obj.Function: " + getFormattedAverageFO(2)
                + "\nExecution Time (ms): " + this.getAverageExecTime()
                + "\n---------------------------------------------------------";
    }

    public Result finish() {
        return new Result(
                this.algorythmName,
                this.instanceName,
                Double.toString(this.getAverageFOValue()),
                Double.toString(this.getBestSolution().getScore()),
                Double.toString(this.getStd()),
                Double.toString(this.getAverageExecTime()),
                Long.toString(this.getTotalTime())
        );
    }

    /**
     * Let the solutions and the instances get garbage collected, only keep whatever is interesing for us
     */
    private class SolutionData {
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
