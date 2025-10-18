package es.urjc.etsii.grafo.bmssc.create;

import es.urjc.etsii.grafo.bmssc.model.BMSSCInstance;
import es.urjc.etsii.grafo.bmssc.model.sol.AssignMove;
import es.urjc.etsii.grafo.bmssc.model.sol.BMSSCSolution;
import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.annotations.RealParam;
import es.urjc.etsii.grafo.create.Constructive;
import es.urjc.etsii.grafo.metrics.Metrics;
import es.urjc.etsii.grafo.util.random.RandomManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BMSSCGRASPConstructor extends Constructive<BMSSCSolution, BMSSCInstance> {

    private double a;

    private float margin;

    /**
     * GRASP Constructor
     * @param alpha Randomness, adjusts the candidate list size.
     *                   Takes values between [0,1] being 1 --> totally random, 0 --> full greedy.
     *                   Special Values:
     *                   MIN_VALUE (Generate a random value each construction, and keep it until the solution is built)
     *                   MAX_VALUE (Generate a random value each time we read it, this way it will take different values in the same construction)
     */
    @AutoconfigConstructor
    public BMSSCGRASPConstructor(
            @RealParam(min = 0, max = 1) double alpha
    ){
        assert alpha == Double.MIN_VALUE || alpha == Double.MAX_VALUE || (alpha >= 0d && alpha <= 1d);
        a = (alpha == Double.MIN_VALUE)? RandomManager.getRandom().nextDouble() : alpha;
    }

    @Override
    public BMSSCSolution construct(BMSSCSolution solution) {
        var ins = solution.getInstance();
        var r = RandomManager.getRandom();

        assert solution.getInstance().k > 1 && solution.getInstance().n > 1;

        // Assign a random point to the first cluster
        int firstPoint = r.nextInt(ins.n);
        new AssignMove(solution, firstPoint, 0).execute(solution);

        // Assign a point to each remaining cluster
        for (int j = 1; j < ins.k; j++) {

            double globalMax = 0;
            int point = -1;

            for (int n = 0; n < ins.n; n++) {

                if (solution.isAssigned(n))
                    continue;

                double localMin = Double.MAX_VALUE;
                for (int l = 0; l < j; l++) {
                    double distanceToPoint = ins.distance(solution.getCluster(l).iterator().next(), n);
                    if (distanceToPoint < localMin)
                        localMin = distanceToPoint;
                }


                if (localMin > globalMax) {
                    globalMax = localMin;
                    point = n;
                }
            }

            assert point != -1;
            var move = new AssignMove(solution, point, j);
            move.execute(solution);
        }

        List<PointDistance> pd = generateCandidateList(solution);

        while (!pd.isEmpty()) {

            double min = pd.get(0).cost;
            double max = pd.get(pd.size() - 1).cost;
            double umbral = min + (a == Double.MAX_VALUE? RandomManager.getRandom().nextDouble() : a ) * (max - min);

            int indexTope;
            for (indexTope = 0; indexTope < pd.size(); indexTope++) {

                if (pd.get(indexTope).cost > umbral)
                    break;
            }
            PointDistance chosen = pd.remove(r.nextInt(indexTope));
            var assignMove = new AssignMove(solution, chosen.point, chosen.cluster);
            assignMove.execute(solution);
            //solution.assign(chosen.point, chosen.cluster, chosen.cost);

            pd = updateCandidateList(solution, pd, chosen.cluster);

        }

        solution.notifyUpdate();
        Metrics.addCurrentObjectives(solution);
        return solution;
    }


    private List<PointDistance> generateCandidateList(BMSSCSolution solution) {
        var ins = solution.getInstance();
        List<PointDistance> candidateList = new ArrayList<>(ins.n);

        for (int i = 0; i < ins.n; i++) {

            if (solution.isAssigned(i))
                continue;

            PointDistance temp = new PointDistance(i, new double[ins.k]);

            for (int k = 0; k < ins.k; k++) {
                assert !solution.isFullCluster(k);
                temp.othercosts[k] = solution.cachedAssignCost(i, k);
            }

            candidateList.add(temp.refresh());
        }

        Collections.sort(candidateList);
        return candidateList;
    }


    @Override
    public String toString() {
        return "BMSSCGRASPConstructor{" +
                "a=" + a +
                ", margin=" + margin +
                '}';
    }

    private List<PointDistance> updateCandidateList(BMSSCSolution solution, List<PointDistance> oldList, int cluster) {

        boolean isFull = solution.isFullCluster(cluster);
        for (PointDistance item : oldList) {

            // Update the cost of the updated cluster, refresh mininum cost and best cluster
            item.othercosts[cluster] = isFull ? Double.MAX_VALUE : solution.cachedAssignCost(item.point, cluster);
            if (cluster == item.cluster)
                item.refresh();
        }

        // Some points may have updated their best cluster, resort.
        Collections.sort(oldList);
        return oldList;
    }

    private static class PointDistance implements Comparable<PointDistance> {

        int point;
        double cost;
        int cluster = -1;

        double[] othercosts;

        PointDistance(int point, double[] othercosts) {
            this.point = point;
            this.othercosts = othercosts;
        }

        @Override
        public int compareTo(PointDistance pointDistance) {
            return Double.compare(cost, pointDistance.cost);
        }

        PointDistance refresh() {
            cost = Double.MAX_VALUE;
            for (int i = 0; i < othercosts.length; i++) {
                if (othercosts[i] < cost) {
                    cost = othercosts[i];
                    cluster = i;
                }
            }
            assert cost < Double.MAX_VALUE;
            return this;
        }
    }
}
