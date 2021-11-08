package es.urjc.etsii.grafo.solver.improve.sa;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solution.neighborhood.EagerNeighborhood;
import es.urjc.etsii.grafo.solution.neighborhood.Neighborhood;
import es.urjc.etsii.grafo.solver.improve.Improver;
import es.urjc.etsii.grafo.solver.improve.sa.cd.CoolDownControl;
import es.urjc.etsii.grafo.solver.improve.sa.initialt.InitialTemperatureCalculator;
import es.urjc.etsii.grafo.util.RandomManager;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class SimulatedAnnealing<M extends Move<S,I>, S extends Solution<I>, I extends Instance> extends Improver<S,I> {

    private static final Logger log = Logger.getLogger(SimulatedAnnealing.class.getName());

    private final Neighborhood<M, S, I> neighborhood;
    private final TerminationCriteria<M,S,I> terminationCriteria;
    private final CoolDownControl<M,S,I> coolDownControl;
    private final InitialTemperatureCalculator<M,S,I> initialTemperatureCalculator;

    protected SimulatedAnnealing(Neighborhood<M, S, I> ps, InitialTemperatureCalculator<M, S, I> initialTemperatureCalculator, TerminationCriteria<M,S,I> terminationCriteria, CoolDownControl<M,S,I> coolDownControl) {
        this.neighborhood = ps;
        this.terminationCriteria = terminationCriteria;
        this.coolDownControl = coolDownControl;
        this.initialTemperatureCalculator = initialTemperatureCalculator;
    }

    public static <M extends Move<S,I>, S extends Solution<I>, I extends Instance> SimulatedAnnealingBuilder<M,S,I> builder(){
        return new SimulatedAnnealingBuilder<>();
    }

    @Override
    public S _improve(S s) {
        S best = s.cloneSolution();
        double currentTemperature = this.initialTemperatureCalculator.initial(best, neighborhood);
        log.fine("Initial temperature: " + currentTemperature);
        int currentIteration = 0;
        while(!terminationCriteria.terminate(best, neighborhood, currentTemperature, currentIteration)){
            var currentMoves = getMoves(best);
            boolean executed = false;
            for(M move: currentMoves){
                if(move.improves() || accept(move, currentTemperature)){
                    move.execute();
                    executed = true;
                    break;
                }
            }
            if(!executed){
                log.fine(String.format("Terminating early, no valid movement found. Current iter %s, t %s", currentIteration, currentTemperature));
                break;
            }

            currentTemperature = coolDownControl.coolDown(best, neighborhood, currentTemperature, currentIteration);
            currentIteration++;
            log.fine(String.format("Next iter %s with t: %s", currentIteration, currentTemperature));
        }
        return best.getBetterSolution(s);
    }

    private boolean accept(M move, double currentTemperature) {
        double change = Math.abs(move.getValue());
        double metropolis = Math.exp(- change / currentTemperature);
        double roll = RandomManager.getRandom().nextDouble();
        return roll < metropolis;
    }

    private Collection<M> getMoves(S s){
        List<M> moves;
        if(neighborhood instanceof EagerNeighborhood eagerNeighborhood){
            moves = eagerNeighborhood.getMovements(s);
        } else {
            moves = neighborhood.stream(s).collect(Collectors.toList());
        }
        Collections.shuffle(moves, RandomManager.getRandom());
        return moves;
    }

}
