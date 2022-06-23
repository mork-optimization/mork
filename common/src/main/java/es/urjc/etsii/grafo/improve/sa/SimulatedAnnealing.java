package es.urjc.etsii.grafo.improve.sa;

import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.improve.sa.cd.CoolDownControl;
import es.urjc.etsii.grafo.improve.sa.initialt.InitialTemperatureCalculator;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solution.neighborhood.EagerNeighborhood;
import es.urjc.etsii.grafo.solution.neighborhood.Neighborhood;
import es.urjc.etsii.grafo.solution.neighborhood.RandomizableNeighborhood;
import es.urjc.etsii.grafo.util.CollectionUtil;
import es.urjc.etsii.grafo.util.DoubleComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Simulated annealing (SA) is a metaheuristic whose name comes from annealing in metallurgy.
 * This metaheuristic considers some neighboring state s* of the current state s,
 * and probabilistically decides between moving the system to state s* or staying in-state s.
 * The transition is always performed if it improves the current solution, while it depends on the current temperature if not.
 * The decision to transition is controlled by the {@link AcceptanceCriteria}.
 * These probabilities ultimately lead the system to move to lower energy states. After each possible transition,
 * the current temperature is lowered, using the {@link CoolDownControl}.
 * The process is repeated until the {@link TerminationCriteria} is met.
 * The initial temperature is configured using {@link InitialTemperatureCalculator},
 * which usually defaults to something like the Max diff between the moves in the neighborhood,
 * as implemented in {@link es.urjc.etsii.grafo.improve.sa.initialt.MaxDifferenceInitialTemperature}.
 *
 *
 * @see <a href="https://en.wikipedia.org/wiki/Simulated_annealing"></a>
 * @see SimulatedAnnealingBuilder
 * @param <M> Move type
 * @param <S> Solution type
 * @param <I> Instance type
 */
public class SimulatedAnnealing<M extends Move<S,I>, S extends Solution<S,I>, I extends Instance> extends Improver<S,I> {

    private static final Logger log = LoggerFactory.getLogger(SimulatedAnnealing.class);

    private final AcceptanceCriteria<M,S,I> acceptanceCriteria;
    private final Neighborhood<M, S, I> neighborhood;
    private final TerminationCriteria<M,S,I> terminationCriteria;
    private final CoolDownControl<M,S,I> coolDownControl;
    private final InitialTemperatureCalculator<M,S,I> initialTemperatureCalculator;
    private final int cycleLength;

    private record CycleResult<S>(boolean atLeastOneMove, S bestSolution, S currentSolution){}

    /**
     * Internal constructor, use {@link SimulatedAnnealing#builder()}.
     * @param acceptanceCriteria
     * @param ps
     * @param initialTemperatureCalculator
     * @param terminationCriteria
     * @param coolDownControl
     * @param cycleLength
     */
    protected SimulatedAnnealing(AcceptanceCriteria<M, S, I> acceptanceCriteria, Neighborhood<M, S, I> ps, InitialTemperatureCalculator<M, S, I> initialTemperatureCalculator, TerminationCriteria<M, S, I> terminationCriteria, CoolDownControl<M, S, I> coolDownControl, int cycleLength) {
        this.acceptanceCriteria = acceptanceCriteria;
        this.neighborhood = ps;
        this.terminationCriteria = terminationCriteria;
        this.coolDownControl = coolDownControl;
        this.initialTemperatureCalculator = initialTemperatureCalculator;
        this.cycleLength = cycleLength;
    }

    /**
     * Get simulated annealing builder.
     * @return simulated annealing builder
     * @param <M> Move type
     * @param <S> Solution type
     * @param <I> Instance type
     */
    public static <M extends Move<S,I>, S extends Solution<S,I>, I extends Instance> SimulatedAnnealingBuilder<M,S,I> builder(){
        return new SimulatedAnnealingBuilder<>();
    }

    @Override
    protected S _improve(S solution) {
        S best = solution.cloneSolution();
        double currentTemperature = this.initialTemperatureCalculator.initial(best, neighborhood);
        log.debug("Initial temperature: {}", currentTemperature);
        int currentIteration = 0;
        while(!terminationCriteria.terminate(best, neighborhood, currentTemperature, currentIteration)){
            CycleResult<S> cycleResult = neighborhood instanceof RandomizableNeighborhood rn?
                            doCycleFast(rn, solution, best, currentTemperature) :
                            doCycleSlow(neighborhood, solution, best, currentTemperature);

            best = cycleResult.bestSolution;
            solution = cycleResult.currentSolution;
            if(!cycleResult.atLeastOneMove){
                log.debug("Terminating early, no valid movement found. Current iter {}, t {}", currentIteration, currentTemperature);
                break;
            }

            double newTemperature = coolDownControl.coolDown(best, neighborhood, currentTemperature, currentIteration);
            assert DoubleComparator.isLessThan(newTemperature, currentTemperature) : String.format("Next Temp %s should be < than prev %s", newTemperature, currentTemperature);
            currentTemperature = newTemperature;
            currentIteration++;
            log.debug("Next iter {} with t: {}", currentIteration, currentTemperature);
        }
        return best;
    }

    /**
     * Does a cycle with the same temperature. Always works on the same solution.
     * Slow implementation as a fallback, when {@link SimulatedAnnealing#doCycleFast(RandomizableNeighborhood, Solution, Solution, double)} is not possible.
     * @param solution current working solution
     * @param best best solution found until now
     * @param currentTemperature current temperature
     * @return best solution found, remember that you should NOT use the returned solution
     */
    protected CycleResult<S> doCycleSlow(Neighborhood<M,S,I> neighborhood, S solution, S best, double currentTemperature) {
        boolean atLeastOne = false;
        for (int i = 0; i < this.cycleLength; i++) {
            boolean executed = false;
            var currentMoves = getMoves(neighborhood, solution);
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
                log.debug("Breaking cycle at {}/{}", i, this.cycleLength);
                return new CycleResult<>(atLeastOne, best, solution);
            }
        }
        return new CycleResult<>(atLeastOne, best, solution);
    }

    /**
     * Does a cycle with the same temperature. Always works on the same solution.
     * Fast implementation that can only be used if the {@link Neighborhood} implements {@link RandomizableNeighborhood}.
     * @param solution current working solution
     * @param best best solution found until now
     * @param currentTemperature current temperature
     * @return best solution found, remember that you should NOT use the returned solution
     */
    protected CycleResult<S> doCycleFast(RandomizableNeighborhood<M,S,I> neighborhood, S solution, S best, double currentTemperature) {
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
                log.debug("Breaking cycle at {}/{}", i, this.cycleLength);
                return new CycleResult<>(atLeastOne, best, solution);
            }
        }
        return new CycleResult<>(atLeastOne, best, solution);
    }

    private Collection<M> getMoves(Neighborhood<M,S,I> neighborhood, S s){
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
