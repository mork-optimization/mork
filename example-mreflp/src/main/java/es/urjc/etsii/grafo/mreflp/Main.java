package es.urjc.etsii.grafo.mreflp;

import es.urjc.etsii.grafo.solver.Mork;
import es.urjc.etsii.grafo.mreflp.io.BenchmarkPreparationUtil;
import es.urjc.etsii.grafo.mreflp.io.ReproductionReportUtil;
import es.urjc.etsii.grafo.mreflp.model.*;
import es.urjc.etsii.grafo.solution.Objective;
import java.nio.file.Path;
import java.util.ArrayList;

public class Main {
    public static final Objective<MREFLPMove, MREFLPSolution, MREFLPInstance> COST =
            Objective.ofMinimizing("Cost", MREFLPSolution::cost, MREFLPMove::delta);

    public static void main(String[] args) throws Exception {
        if (args.length > 0 && args[0].equals("prepare")) {
            if (args.length != 1) throw new IllegalArgumentException("Usage: prepare");
            BenchmarkPreparationUtil.prepare(Path.of("."));
        } else if (args.length > 0 && args[0].equals("report")) {
            if (args.length < 3) throw new IllegalArgumentException("Usage: report OUTPUT INPUT [INPUT ...]");
            var inputs = new ArrayList<Path>();
            for (int i = 2; i < args.length; i++) inputs.add(Path.of(args[i]));
            System.out.println(ReproductionReportUtil.report(Path.of("."), Path.of(args[1]), inputs));
        } else if (!Mork.start(args, COST)) {
            System.exit(1);
        }
    }
}
