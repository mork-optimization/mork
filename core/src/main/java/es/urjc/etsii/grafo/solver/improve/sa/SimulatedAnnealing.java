package es.urjc.etsii.grafo.solver.improve.sa;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.RandomizableNeighborhood;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solution.neighborhood.EagerNeighborhood;
import es.urjc.etsii.grafo.solution.neighborhood.Neighborhood;
import es.urjc.etsii.grafo.solver.improve.Improver;
import es.urjc.etsii.grafo.solver.improve.sa.cd.CoolDownControl;
import es.urjc.etsii.grafo.solver.improve.sa.initialt.InitialTemperatureCalculator;
import es.urjc.etsii.grafo.util.CollectionUtil;
import es.urjc.etsii.grafo.util.DoubleComparator;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class SimulatedAnnealing<M extends Move<S,I>, S extends Solution<S,I>, I extends Instance> extends Improver<S,I> {

    private static final Logger log = Logger.getLogger(SimulatedAnnealing.class.getName());


    private final AcceptanceCriteria<M,S,I> acceptanceCriteria;
    private final Neighborhood<M, S, I> neighborhood;
    private final TerminationCriteria<M,S,I> terminationCriteria;
    private final CoolDownControl<M,S,I> coolDownControl;
    private final InitialTemperatureCalculator<M,S,I> initialTemperatureCalculator;
    private final int cycleLength;

    private record CycleResult<S>(boolean atLeastOneMove, boolean shortExecution, S bestSolution, S currentSolution){}

    protected SimulatedAnnealing(AcceptanceCriteria<M, S, I> acceptanceCriteria, Neighborhood<M, S, I> ps, InitialTemperatureCalculator<M, S, I> initialTemperatureCalculator, TerminationCriteria<M, S, I> terminationCriteria, CoolDownControl<M, S, I> coolDownControl, int cycleLength) {
        this.acceptanceCriteria = acceptanceCriteria;
        this.neighborhood = ps;
        this.terminationCriteria = terminationCriteria;
        this.coolDownControl = coolDownControl;
        this.initialTemperatureCalculator = initialTemperatureCalculator;
        this.cycleLength = cycleLength;
    }

    public static <M extends Move<S,I>, S extends Solution<S,I>, I extends Instance> SimulatedAnnealingBuilder<M,S,I> builder(){
        return new SimulatedAnnealingBuilder<>();
    }

    @Override
    protected S _improve(S solution) {
        S best = solution.cloneSolution();
        double currentTemperature = this.initialTemperatureCalculator.initial(best, neighborhood);
        log.fine("Initial temperature: " + currentTemperature);
        int currentIteration = 0;
        while(!terminationCriteria.terminate(best, neighborhood, currentTemperature, currentIteration)){
            CycleResult<S> cycleResult = neighborhood instanceof RandomizableNeighborhood rn?
                            doCycleFast(rn, solution, best, currentTemperature) :
                            doCycleSlow(neighborhood, solution, best, currentTemperature);

            best = cycleResult.bestSolution;
            solution = cycleResult.currentSolution;
            if(!cycleResult.atLeastOneMove){
                log.fine(String.format("Terminating early, no valid movement found. Current iter %s, t %s", currentIteration, currentTemperature));
                break;
            }

            double newTemperature = coolDownControl.coolDown(best, neighborhood, currentTemperature, currentIteration);
            assert DoubleComparator.isLessThan(newTemperature, currentTemperature) : String.format("Next Temp %s should be < than prev %s", newTemperature, currentTemperature);
            currentTemperature = newTemperature;
            currentIteration++;
            log.fine(String.format("Next iter %s with t: %s", currentIteration, currentTemperature));
        }
        return best;
    }

    /**
     * Does a cycle with the same temperature. Always works on the same solution.
     * NON RANDOMIZABLE NEIGHBORHOOD implementation
     * @param solution current working solution
     * @param best best solution found until now
     * @param currentTemperature current temperature
     * @return best solution found, remember that you should NOT use the returned solution
     */
    private CycleResult<S> doCycleSlow(Neighborhood<M,S,I> neighborhood, S solution, S best, double currentTemperature) {
        boolean atLeastOne = false;
        for (int i = 0; i < this.cycleLength; i++) {
            boolean executed = false;
            var currentMoves = getMoves(solution);
            for(M move: currentMoves){
                if(move.improves() || acceptanceCriteria.accept(move, currentTemperature)){
                    atLeastOne = true;
                    move.execute();
                    executed = true;
                    if(solution.isBetterThan(best)){
                        best = solution.cloneSolution();
                    }
                    break;
                }
            }
            if(!executed){
                log.fine(String.format("Breaking cycle at %s/%s", i, this.cycleLength));
                return new CycleResult<>(atLeastOne, true, best, solution);
            }
        }
        return new CycleResult<>(atLeastOne, false, best, solution);
    }

    /**
     * Does a cycle with the same temperature. Always works on the same solution.
     * NON RANDOMIZABLE NEIGHBORHOOD implementation
     * @param solution current working solution
     * @param best best solution found until now
     * @param currentTemperature current temperature
     * @return best solution found, remember that you should NOT use the returned solution
     */
    private CycleResult<S> doCycleFast(RandomizableNeighborhood<M,S,I> neighborhood, S solution, S best, double currentTemperature) {
        boolean atLeastOne = false;
        for (int i = 0; i < this.cycleLength; i++) {
            int fails = 0;
            int maxRetries = 3;
            Set<M> testedMoves = new HashSet<>();
            while(fails < maxRetries){
                Optional<M> optionalMove = neighborhood.getRandomMove(solution);
                if(optionalMove.isEmpty()){
                    fails++; continue;
                }
                M move = optionalMove.get();
                if(testedMoves.contains(move)){
                    fails++; continue;
                }
                testedMoves.add(move);
                if(move.improves() || acceptanceCriteria.accept(move, currentTemperature)){
                    atLeastOne = true;
                    move.execute();
                    if(solution.isBetterThan(best)){
                        best = solution.cloneSolution();
                    }
                    break;
                }
            }
            if(fails >= maxRetries){
                log.fine(String.format("Breaking cycle at %s/%s", i, this.cycleLength));
                return new CycleResult<>(atLeastOne, true, best, solution);
            }
        }
        return new CycleResult<>(atLeastOne, false, best, solution);
    }



    private Collection<M> getMoves(S s){
        List<M> moves;
        if(neighborhood instanceof EagerNeighborhood eagerNeighborhood){
            moves = eagerNeighborhood.getMovements(s);
        } else {
            moves = neighborhood.stream(s).collect(Collectors.toList());
        }
        CollectionUtil.shuffle(moves);
        return moves;
    }

}
