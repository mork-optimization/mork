package es.urjc.etsii.grafo.TSPTW.alg;

import es.urjc.etsii.grafo.TSPTW.constructives.TSPTWRandomConstructive;
import es.urjc.etsii.grafo.TSPTW.model.TSPTWInstance;
import es.urjc.etsii.grafo.TSPTW.model.TSPTWSolution;
import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.util.TimeControl;
import es.urjc.etsii.grafo.util.TimeUtil;
import org.slf4j.Logger;


import static org.slf4j.LoggerFactory.*;

public class GVNS extends Algorithm<TSPTWSolution, TSPTWInstance> {

    private static final Logger logger = getLogger(GVNS.class);
    private final TSPTWRandomConstructive constructive = new TSPTWRandomConstructive();

    private final int level_max;

    /**
     * Initialize common algorithm fields
     *
     * @param algorithmName algorithm name. See {@link #setName(String)}
     */
    public GVNS(String algorithmName, int level_max) {
        super(algorithmName);
        this.level_max = level_max;
    }

    public GVNS() {
        this("GVNS", 8); // original impl uses a default value of 8 for level_max
    }

    @Override
    public TSPTWSolution algorithm(TSPTWInstance instance) {
        TSPTWSolution x = vns_feasible(instance);
        if (x.constraint_violations() == 0) {
            gvns(x);
        }
        return x;
    }

    public TSPTWSolution vns_feasible(TSPTWInstance instance) {
        TSPTWSolution x;
        TSPTWSolution x2;
        int level_max_vns_feasible = instance.n() / 2;

        do {
            int level = 1;
            x = constructive.construct(new TSPTWSolution(instance));
            x.ls_feasibility_1shift_first();
            x2 = x.clone_solution();

            while (x.constraint_violations() > 0 && level < level_max_vns_feasible
                    && !TimeControl.isTimeUp()) {
                x2.perturb_1shift(level);
                x2.ls_feasibility_1shift_first();

                if (x2.infeasibility() < x.infeasibility()) {
                    logger.debug("vnd_f {} {} {} {} s remaning\n",
                            level, x2.cost(), x2.constraint_violations(), TimeUtil.nanosToSecs(TimeControl.remaining()));
                    x = x2.clone_solution();
                    level = 1; // Improved
                } else {
                    x2 = x.clone_solution();
                    level++; // Not improved
                }
            }
        } while (x.constraint_violations() > 0 && !TimeControl.isTimeUp());

        logger.debug("# (vnd feasible)\t {}", x.cost());
        return x;
    }

    public void gvns(TSPTWSolution x) {
        int level = 1;
        int iterlevel_max = 30;
        int iterlevel = 0;
        TSPTWSolution x2 = x.clone_solution();

        while (level < level_max && !TimeControl.isTimeUp()) {
            x2.perturb_1shift_feasible(level);
            vnd(x2);

            if (x2.cost() < x.cost()) {
                logger.debug("# (pert. {})\t{}", level, x2.cost());
                logger.debug("gvns {} {} {} {} s remaining",
                        level, x2.cost(), x2.constraint_violations(), TimeUtil.nanosToSecs(TimeControl.remaining()));
                x.copy_from(x2);
                level = 1;
                iterlevel = 0;
            } else {
                x2.copy_from(x);
                iterlevel++;
                if (iterlevel > iterlevel_max) {
                    level++;
                    iterlevel = 0;
                }
            }
        }
    }

    public void vnd(TSPTWSolution x) {
        boolean improved = false;
        assert x.constraint_violations() == 0;

        do {
            while (x.feasible_1shift_first()) {
                improved = true;
            }
            if (improved) {
                logger.debug("insert {} {} {} {} s remaining",
                        improved ? 1 : 0, x.cost(), x.constraint_violations(), TimeUtil.nanosToSecs(TimeControl.remaining()));
            }

            improved = false;
            while (x.two_opt_first()) {
                improved = true;
            }
            if (improved) {
                logger.debug("2opt {} {} {} {} s remaining",
                        improved ? 1 : 0, x.cost(), x.constraint_violations(), TimeUtil.nanosToSecs(TimeControl.remaining()));
            }
        } while (improved);
    }

}
