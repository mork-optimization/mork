package es.urjc.etsii.grafo.solver.algorithms;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.create.Constructive;
import es.urjc.etsii.grafo.solver.create.builder.SolutionBuilder;
import es.urjc.etsii.grafo.solver.destructor.Shake;
import es.urjc.etsii.grafo.solver.improve.Improver;
import es.urjc.etsii.grafo.util.DoubleComparator;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * Example simple algorithm, executes:
 * Constructive --> (Optional, if present) Local Searches --> (Optional, if present) Shake --> If did not improve end
 *                                               ^_________________________________________|   else repeat
 * This class can be used to test all the pieces if they are working properly, or as a base for more complex algorithms
 * @param <S> Solution class
 * @param <I> Instance class
 */
public class SimpleAlgorithm<S extends Solution<I>, I extends Instance> extends Algorithm<S,I>{

    private static Logger log = Logger.getLogger(SimpleAlgorithm.class.getName());

    Constructive<S,I> constructive;
    List<Improver<S,I>> improvers;
    Shake<S, I> shake;

    @SafeVarargs
    public SimpleAlgorithm(Constructive<S, I> constructive, Shake<S,I> shake, Improver<S,I>... improvers){
        this.constructive = constructive;
        this.shake = shake;
        if(improvers != null && improvers.length >= 1){
            this.improvers = Arrays.asList(improvers);
        }
    }

    public SimpleAlgorithm(Constructive<S, I> constructive){
        this(constructive, (Improver<S, I>[]) null);
    }

    @SafeVarargs
    public SimpleAlgorithm(Constructive<S, I> constructive, Improver<S,I>... improvers){
        this(constructive, null, improvers);
    }

    /**
     * Algorithm: Execute a single construction and then all the local searchs a single time.
     * @param instance Instance the algorithm will use
     * @return Returns a valid solution
     */
    @Override
    public S algorithm(I instance, SolutionBuilder<S,I> builder) {
        var solution = builder.initializeSolution(instance);
        solution = constructive.construct(solution);
        printStatus("Constructive", solution);
        solution = localSearch(solution);

        S previous;
        if(this.shake != null){
            while(true) {
                previous = solution.cloneSolution();
                previous = localSearch(previous);
                if(shake != null){
                    previous = shake.shake(previous, 1, 1, true);
                }
                if(DoubleComparator.equals(previous.getScore(), solution.getScore())){
                    break;
                } else {
                    solution = previous;
                }
            }
        }

        return solution;
    }

    private S localSearch(S solution) {
        if(improvers != null){
            for (int i = 0; i < improvers.size(); i++) {
                Improver<S, I> ls = improvers.get(i);
                solution = ls.improve(solution);
                printStatus("Improver " + i, solution);
            }
        }
        return solution;
    }

    private void printStatus(String phase, S s){
        log.fine(() -> String.format("\t\t%s: %s", phase, s));
    }

    @Override
    public String toString() {
        return "Simple{" +
                "cnstr=" + constructive +
                ", impr=" + improvers +
                ", shk=" + shake +
                '}';
    }
}
