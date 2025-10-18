package es.urjc.etsii.grafo.autoconfig.testutil;

import es.urjc.etsii.grafo.algorithms.SimpleAlgorithm;
import es.urjc.etsii.grafo.algorithms.vns.VNS;
import es.urjc.etsii.grafo.algorithms.multistart.MultiStartAlgorithm;
import es.urjc.etsii.grafo.autoconfig.fakecomponents.*;
import es.urjc.etsii.grafo.autoconfig.inventory.WhitelistInventoryFilter;
import es.urjc.etsii.grafo.create.grasp.GreedyRandomGRASPConstructive;
import es.urjc.etsii.grafo.create.grasp.RandomGreedyGRASPConstructive;
import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.improve.ls.LocalSearchBestImprovement;
import es.urjc.etsii.grafo.improve.ls.LocalSearchFirstImprovement;
import es.urjc.etsii.grafo.shake.RandomMoveShake;
import es.urjc.etsii.grafo.shake.Shake;

import java.util.Set;

public class ComponentWhitelistDuringTesting extends WhitelistInventoryFilter {
    @Override
    public Set<Class<?>> getWhitelist() {
        // List of components that will be considered while executing tests in the autoconfig module
        return Set.of(
                SimpleAlgorithm.class,
                MultiStartAlgorithm.class,
                TestAlgorithmA.class,
                Improver.SequentialImprover.class,
                Improver.NullImprover.class,
                FakeGRASPConstructive.class,
                FakeGRASPListManager.class,
                RandomGreedyGRASPConstructive.class,
                GreedyRandomGRASPConstructive.class,
                TestAutoconfigNeighborhood.class,
                LocalSearchBestImprovement.class,
                LocalSearchFirstImprovement.class,
                VNS.class,
                Shake.NullShake.class,
                RandomMoveShake.class,
                DoNothingConstructive.class
        );
    }
}
