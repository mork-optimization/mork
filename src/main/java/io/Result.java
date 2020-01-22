package io;

import solution.Solution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The result of the execution of an algorithm
 * Contains all the generated solutions and some stats about them
 */
public class Result {

    private final ArrayList<Solution> s;
    private Instance instance;
    private final String algorythmName;
    private final String instanceName;

    /**
     * Initialize a result class.
     * @param nSolutions estimated number of solutions we are generating
     * @param algName Algorithm's name
     * @param instanceName Instance name
     */
    public Result(int nSolutions, String algName, String instanceName) {
        this.s = new ArrayList<>(nSolutions);
        this.algorythmName = algName;
        this.instanceName = instanceName;
    }

    /**
     * Store new result
     * @param s Calculated solution
     * @param nanos Time used to calculate the given solution
     */
    public void addSolution(Solution s, long nanos){
        s.setExecutionTimeInNanos(nanos);
        this.s.add(s);
        if(instance == null){
            instance = s.getInstance();
        } else if(instance != s.getInstance()){
            throw new AssertionError(String.format("Instance mismatch, expected %s got %s", instance.getName(), s.getInstance().getName()));
        }
    }

    public long getAverageExecTime(){
        long totalTime = 0;
        for (Solution solution : this.s) {
            totalTime += solution.getExecutionTimeInNanos();
        }
        return totalTime / this.s.size() / 1000000; // 1 millisecond = 10^6 nanos
    }

    public String getFormattedAverageFO(int nDecimales){
        String formatString = "%." + nDecimales + "f";
        return String.format(formatString, this.getAverageFOValue());
    }

    public double getAverageFOValue(){
        double value = 0;
        for (Solution solution : s) {
            value += solution.getOptimalValue();
        }
        return value / s.size();
    }

    /**
     * Returns the standard deviation for the objective values of all solutions
     * @return The STD
     */
    public double getStd(){
        double total = 0;
        double avg = getAverageFOValue();
        for (Solution solution : this.s) {
            // La varianza es la suma de las diferencias al cuadrado
            // dividido entre el tamaño del conjunto menos 1
            // La desviacion estandar es sqrt(varianza)
            double difference = solution.getOptimalValue() - avg;
            total += difference * difference;
        }

        return Math.sqrt(total / (this.s.size() - 1));
    }

    public Solution getBestSolution(){
        double best = Double.MAX_VALUE;
        Solution chosen = null;
        for (Solution solution : s) {
            if (solution.getOptimalValue() < best) {
                chosen = solution;
                best = solution.getOptimalValue();
            }
        }
        return chosen;
    }

    public List<Solution> getSolutions(){
        return Collections.unmodifiableList(this.s);
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
}
