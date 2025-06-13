package es.urjc.etsii.grafo.algorithms.vns;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.annotations.IntegerParam;
import es.urjc.etsii.grafo.annotations.ProvidedParam;
import es.urjc.etsii.grafo.create.Constructive;
import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.metrics.Metrics;
import es.urjc.etsii.grafo.shake.Shake;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.util.Context;
import es.urjc.etsii.grafo.util.StringUtil;
import es.urjc.etsii.grafo.util.TimeControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

/**
 * Variable neighborhood search (VNS) is a metaheuristic for solving combinatorial
 * and global optimization problems. Its basic idea is the systematic change of
 * neighborhood both in a descent phase to find a local optimum and in a perturbation
 * phase to exit the corresponding local optimum
 * <p>
 * Algorithmic outline one of the simplest versions of VNS
 * </p>
 * <pre>
 * s = GenerateInitialSolution
 * while (Termination criteria is not met){
 *      k = 1
 *      while (k != kmax){
 *          s' = Shake(s,k)
 *          s'' = Improve (s')
 *          NeighborhoodChange(s,s'',k)
 *      }
 * }
 * </pre>
 * <p>
 * More information can be found in:
 * Hansen P., Mladenović N. (2018) Variable Neighborhood Search.
 * In: Martí R., Pardalos P., Resende M. (eds) Handbook of Heuristics.
 * Springer, Cham. <a href="https://doi.org/10.1007/978-3-319-07124-4_19">...</a>
 *
 * @param <S> type of the problem solution
 * @param <I> type of the problem instance
 */
public class VNS<S extends Solution<S, I>, I extends Instance> extends Algorithm<S, I> {

    private static final Logger log = LoggerFactory.getLogger(VNS.class);

    protected Improver<S, I> improver;

    /**
     * Constructive procedure
     */
    protected Constructive<S, I> constructive;

    /**
     * Shake procedure
     */
    protected Shake<S, I> shake;

    /**
     * Calculates K value for each VNS step. {@see KMapper}
     */
    protected VNSNeighChange<S, I> neighChange;

    /**
     * Objective function to optimize
     */
    protected Objective<?, S, I> objective;

    /**
     * VNS with default KMapper, which starts at 0 and increments by 1 each time the solution does not improve.
     * Stops when k >= 5. Behaviour can be customized passing a custom kMapper, such as:
     * <pre>
     * {@code
     * (solution, originalK) -> originalK >= 10 ? KMapper.STOPNOW : originalK
     * }
     * </pre>
     *
     * @param algorithmName Algorithm name, example "VNS1-GRASP". Uniquely identifies the current algorithm. Tip: If you dont care about the name, generate a random one using {@link StringUtil#randomAlgorithmName()}
     * @param shake         Perturbation method
     * @param constructive  Constructive method
     * @param improver      List of improvers/local searches
     */
    @AutoconfigConstructor
    protected VNS(@ProvidedParam String algorithmName, @IntegerParam(min = 1, max = 100) int maxK, Constructive<S, I> constructive, Shake<S, I> shake, Improver<S, I> improver) {
        this(algorithmName, Context.getMainObjective(), new DefaultVNSNeighChange<>(maxK, 1), constructive, shake, improver);
    }

    /**
     * Execute VNS until finished
     *
     * @param algorithmName Algorithm name, example: "VNSWithRandomConstructive"
     * @param objective     function to optimize
     * @param neighChange   k value provider, @see VNS.KMapper
     * @param shake         Perturbation method
     * @param constructive  Constructive method
     * @param improver      List of improvers/local searches
     */
    public VNS(String algorithmName, Objective<?, S, I> objective, VNSNeighChange<S, I> neighChange, Constructive<S, I> constructive, Shake<S, I> shake, Improver<S, I> improver) {
        super(algorithmName);
        this.objective = objective;
        this.neighChange = neighChange;

        // Ensure Ks are sorted, maxK is the last element
        this.shake = shake;
        this.constructive = constructive;
        this.improver = improver;
    }

    /**
     * Executes a single VNS step.
     *
     * @param name         step name
     * @param stepFunction function to execute
     * @param solution     current solution
     * @return new solution after applying the function
     */
    private S step(String name, Function<S, S> stepFunction, S solution) {
        double originalScore = objective.evalSol(solution);
        S newSolution = stepFunction.apply(solution);
        assert Context.validate(newSolution);
        if (objective.isBetter(newSolution, originalScore)) {
            Metrics.addCurrentObjectives(newSolution);
            log.trace("{} improved {} -> {}", name, originalScore, newSolution);
        } else {
            log.trace("{} did not improve {} -> {}", name, originalScore, newSolution);
        }
        return newSolution;
    }

    /**
     * VNS algorithm. This procedure follows this schema:
     * <pre>
     *     s = GenerateInitial solution
     *     k = 1
     *     while (k != kmax){
     *     s' = Shake(s,k)
     *     s'' = Improve (s')
     *     NeighborhoodChange(s,s'',k)
     *     }
     * </pre>
     * <p>
     * To run the VNS procedure multiples time consider use MultiStart algorithm class {@see es.urjc.etsii.grafo.solver.algorithms.multistart.MultiStartAlgorithm}
     *
     * @param instance Instance to solve
     * @return best solution found when the VNS procedure ends
     */
    public S algorithm(I instance) {
        // Equivalent to calling "new SolutionImplementation(instance)"
        // but indirectly, since we do not know the implementation class at compile time
        var solution = this.newSolution(instance);

        solution = step("Initial Construct", constructive::construct, solution);
        solution = step("Initial Improve", improver::improve, solution);

        int k = 0;
        while (!TimeControl.isTimeUp() && !(k == VNSNeighChange.STOPNOW)) {

            S copy = solution.cloneSolution();
            copy = shake.shake(copy, k);
            step("Improve k=" + k, improver::improve, copy);

            if (objective.isBetter(copy, solution)) {
                solution = copy;
                k = 0;
            } else {
                k = neighChange.apply(solution, k);
            }
        }
        return solution;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "VNS{" + "improvers=" + improver + ", constructive=" + constructive + ", shakes=" + shake + ", kmap=" + neighChange + '}';
    }
}
