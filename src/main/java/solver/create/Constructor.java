package solver.create;

import io.Instance;
import solution.Solution;

public interface Constructor {

    /**
     * Constructs a solution for the GOP problem
     * @param ins Inmutable instance data
     * @return
     */
    Solution construct(Instance ins);
}
