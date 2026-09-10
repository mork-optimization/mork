package es.urjc.etsii.grafo.cph.constructives;

import es.urjc.etsii.grafo.cph.model.CPHInstance;
import es.urjc.etsii.grafo.cph.model.CPHSolution;
import es.urjc.etsii.grafo.create.Constructive;
import es.urjc.etsii.grafo.util.random.RandomManager;

import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;

/**
 * Builds a random CPH solution: picks {@code p} random hubs and then assigns random clients to
 * them while respecting each hub's capacity. Same construction logic as the {@code jmh}
 * random constructive, but now it extends Mork's reusable {@link Constructive} base so it can be
 * plugged into {@code SimpleAlgorithm} and the local searches.
 */
public class CPHRandomConstructive extends Constructive<CPHSolution, CPHInstance> {

    @Override
    public CPHSolution construct(CPHSolution solution) {
        CPHInstance instance = solution.getInstance();
        RandomGenerator random = RandomManager.getRandom();

        List<Integer> assignedNodes = new ArrayList<>();
        List<Integer> hubs = new ArrayList<>();
        int[] hubLoads = new int[instance.getNumNodes()];
        int[] spokes = new int[instance.getNumNodes()];

        // A random client assignment can become infeasible (no open hub has enough
        // remaining capacity for any unassigned client): give up and restart the
        // whole construction with a fresh random draw instead of retrying forever.
        int stagnantIterations = 0;
        int maxStagnantIterations = 4 * instance.getNumNodes() + 10;

        do {
            boolean progressed = false;

            if (hubs.size() != instance.getNumHubs()) {

                // Select a random node as hub
                int randomNode = random.nextInt(instance.getNumNodes());
                if (!assignedNodes.contains(randomNode)) {
                    hubs.add(randomNode);
                    spokes[randomNode] = -1;
                    assignedNodes.add(randomNode);
                    progressed = true;
                }

            } else {

                // Try to assign a random client to each hub, respecting the hub capacity
                for (int hub : hubs) {
                    int randomNode = random.nextInt(instance.getNumNodes());
                    if (!assignedNodes.contains(randomNode)) {
                        int demand = instance.getDemand(randomNode);
                        if (hubLoads[hub] + demand <= instance.getHubCapacity()) {
                            hubLoads[hub] += demand;
                            spokes[randomNode] = hub;
                            assignedNodes.add(randomNode);
                            progressed = true;
                        }
                    }
                }
            }

            if (progressed) {
                stagnantIterations = 0;
            } else if (++stagnantIterations > maxStagnantIterations) {
                assignedNodes.clear();
                hubs.clear();
                java.util.Arrays.fill(hubLoads, 0);
                java.util.Arrays.fill(spokes, 0);
                stagnantIterations = 0;
            }
        } while (hubs.size() != instance.getNumHubs()
                || assignedNodes.size() != instance.getNumNodes());

        solution.setHubsAndSpokes(hubs, spokes);
        solution.setScore(solution.recalculateScore());
        solution.notifyUpdate();
        return solution;
    }
}
