package es.urjc.etsii.grafo.BMSSC.create;

import es.urjc.etsii.grafo.BMSSC.model.BMSSCInstance;
import es.urjc.etsii.grafo.BMSSC.model.sol.AssignMove;
import es.urjc.etsii.grafo.BMSSC.model.sol.BMSSCSolution;
import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.create.grasp.GRASPListManager;
import es.urjc.etsii.grafo.util.random.RandomManager;

import java.util.ArrayList;
import java.util.List;

public class BMSSCListManager extends GRASPListManager<AssignMove, BMSSCSolution, BMSSCInstance> {

    @AutoconfigConstructor
    public BMSSCListManager() {}

    @Override
    public void beforeGRASP(BMSSCSolution solution) {
        super.beforeGRASP(solution);
        // Assign first point randomly
        var instance = solution.getInstance();
        var r = RandomManager.getRandom();
        int firstPoint = r.nextInt(instance.n);

        var firstMove = new AssignMove(solution, firstPoint, 0);
        firstMove.execute(solution);

        // For each remaining cluster, assign the point with the maximum minimum distance to each cluster
        for (int j = 1; j < instance.k; j++) {

            double globalMax = 0;
            int point = -1;
            for (int n = 0; n < instance.n; n++) {

                if (solution.isAssigned(n)) {
                    continue;
                }

                double localMin = Double.MAX_VALUE;
                for (int l = 0; l < j; l++) {
                    double distanceToPoint = instance.distance(solution.getCluster(l).iterator().next(), n);
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
    }

    @Override
    public List<AssignMove> buildInitialCandidateList(BMSSCSolution solution) {
        var notAssigned = solution.getNotAssignedPoints();
        var instance = solution.getInstance();
        var list = new ArrayList<AssignMove>(notAssigned.size() * instance.k);

        for (int i = 0; i < instance.k; i++) {
            if(solution.isFullCluster(i)) continue;
            for(var p: notAssigned){
                list.add(new AssignMove(solution, p, i));
            }
        }
        return list;
    }

    @Override
    public List<AssignMove> updateCandidateList(BMSSCSolution solution, AssignMove move, List<AssignMove> candidateList, int index) {
//        var newList = new ArrayList<AssignMove>(candidateList.size());
//        int changedCluster = move.getCluster();
//        boolean isFullNow = solution.isFullCluster(changedCluster);
//        int assignedPoint = move.getPoint();
//        for(var m: candidateList){
//            if(m.getPoint() == assignedPoint) continue;
//            if(m.getCluster() == changedCluster){
//                if(isFullNow) continue;
//                m = m.update();
//            }
//            newList.add(m);
//        }
//        return newList;
        return buildInitialCandidateList(solution);
    }
}
