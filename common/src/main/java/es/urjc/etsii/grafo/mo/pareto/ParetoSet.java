package es.urjc.etsii.grafo.mo.pareto;

import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.util.Context;

import java.util.*;
import java.util.stream.Stream;

import static es.urjc.etsii.grafo.util.Context.Pareto.MAX_ELITE_SOLS_PER_OBJ;
import static es.urjc.etsii.grafo.util.Context.Pareto.MAX_TRACKED_SOLS;

public abstract class ParetoSet<S extends Solution<S,I>, I extends Instance> {

    Map<String, TreeSet<S>> elites;
    Map<double[], S> solutions = HashMap.newHashMap(MAX_TRACKED_SOLS);
    final int nObjectives;

    /**
     * Create a new Pareto set that will track solutions with n objectives
     * @param nObjectives
     */
    public ParetoSet(int nObjectives){
        this.nObjectives = nObjectives;
        resetElites();
    }


    public synchronized void resetElites(){
        this.elites = new HashMap<>();
        Map<String, Objective<?, S, I>> objectives = Context.getObjectives();
        for(var e: objectives.entrySet()){
            var name = e.getKey();
            elites.put(name, new TreeSet<>(e.getValue().comparator()));
        }
    }

    long lastModifiedTime = Integer.MIN_VALUE;


    public synchronized long getLastModifiedTime() {
        return lastModifiedTime;
    }

    /**
     * Try to add multiple solutions to the Pareto front
     * @param solutions solutions to try to add
     * @return true if the Pareto front has been modified, false otherwise (all solutions are dominated by those in the Pareto front)
     */
    public synchronized boolean add(Iterable<S> solutions) {
        boolean atLeastOne = false;
        for(var solution: solutions){
            atLeastOne |= add(solution);
        }
        return atLeastOne;
    }

    /**
     * Try add solution to Pareto front. Solution is only added if it is not dominated by any other solution in the Pareto front.
     * @param newSol solution to try to add to Pareto front
     * @return true if the solution was added to the pareto front and therefore the Pareto front has been updated, false otherwise
     */
    public synchronized boolean add(S newSol){
        Map<String, Objective<?, S, I>> objectives = Context.getObjectives();
        var objValues = new double[objectives.size()];
        int i = 0;
        for(var e: objectives.entrySet()){
            var objective = e.getValue();
            objValues[i] = objective.evalSol(newSol);
            if(objective.getFMode() == FMode.MAXIMIZE){
                // reverse sign of maximizing objectives to simplify handling of pareto front
                objValues[i] = -objValues[i];
            }
            i++;
        }
        var added = add(objValues);
        if(added){ // If solution not added to front, it is worse than all the solutions already inside
            lastModifiedTime = System.nanoTime();
            for(var elite: this.elites.values()){
                tryAddToElite(newSol, elite);
            }
            // If there is space in the solutions map, add it
            if(solutions.size() < MAX_TRACKED_SOLS){
                solutions.put(objValues, newSol);
            }
        }
        return added;
    }

    /**
     * Check if a solution weakly dominates another
     * @param a solution A scores
     * @param b solution B scores
     * @return true if A weakly dominates B, false otherwise
     */
    public static boolean weaklyDominates(double[] a, double[] b) {
        if(a.length != b.length){
            throw new IllegalArgumentException("Both solutions must have the same number of objectives. Got A:"+a.length+" and B:"+b.length+" instead.");
        }

        for (int i = 0; i < a.length; i++) {
            if (a[i] > b[i]) {
                return false;
            }
        }
        return true;
    }

    public static <S extends Solution<S,I>, I extends Instance> void tryAddToElite(S newSol, TreeSet<S> elite) {
        if(elite.contains(newSol)){
            return;
        }
        if(elite.size() < MAX_ELITE_SOLS_PER_OBJ){
            elite.add(newSol.cloneSolution());
        } else {
            // Is this a better solution than the worst in the current elite set?
            var worseSolutions = elite.tailSet(newSol);
            if(worseSolutions.isEmpty()){
                // No worse solutions than current one, end processing this elite set
                return;
            }
            // There is a worse solution than current one, replace it
            worseSolutions.removeLast();
            elite.add(newSol.cloneSolution());
        }
    }

    public void clear(){
        resetElites();
    }

    public abstract boolean add(double[] solution);
    public abstract int size();
    public abstract Stream<double[]> stream();

    public synchronized String toText() {
        StringBuilder stb = new StringBuilder();
        stream()
                .sorted(Comparator.comparingDouble((double[] o) -> o[3]))
                .forEach(sol -> {
                    for (double obj : sol) {
                        stb.append(obj).append("\t");
                    }
                    // replace last tab with newline
                    stb.setCharAt(stb.length()-1, '\n');
                });

        return stb.toString();
    }

    public synchronized double[][] toMatrix() {
        return stream().map(double[]::clone).toArray(double[][]::new);
    }

    public Map<String, TreeSet<S>> getElites() {
        return Collections.unmodifiableMap(elites);
    }

    public Iterable<S> getTrackedSolutions() {
        return solutions.values();
    }

    protected S ejectedSolution(double[] ejectedSolution) {
        return solutions.remove(ejectedSolution);
    }
}
