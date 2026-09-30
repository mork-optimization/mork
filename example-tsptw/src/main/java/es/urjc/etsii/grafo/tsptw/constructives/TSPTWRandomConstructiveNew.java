package es.urjc.etsii.grafo.tsptw.constructives;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.annotations.IntegerParam;
import es.urjc.etsii.grafo.annotations.RealParam;
import es.urjc.etsii.grafo.tsptw.model.TSPTWSolution;
import es.urjc.etsii.grafo.tsptw.model.TSPTWUtil;
import es.urjc.etsii.grafo.util.TimeControl;
import es.urjc.etsii.grafo.util.random.RandomManager;

/** Informed variant of TSPTWRandomConstructive. Always returns a complete tour for feasibility repair. */
public class TSPTWRandomConstructiveNew extends TSPTWRandomConstructive {
    private final double urgencyWeight;
    private final int candidateListSize;

    @AutoconfigConstructor
    public TSPTWRandomConstructiveNew(@RealParam(min = 0, max = 1) double urgencyWeight,
                                    @IntegerParam(min = 1, max = 8) int candidateListSize) {
        if (!Double.isFinite(urgencyWeight) || urgencyWeight < 0 || urgencyWeight > 1) {
            throw new IllegalArgumentException("Urgency weight must be in [0, 1]");
        }
        if (candidateListSize < 1 || candidateListSize > 8) throw new IllegalArgumentException("Candidate list size must be in [1, 8]");
        this.urgencyWeight = urgencyWeight;
        this.candidateListSize = candidateListSize;
    }

    public TSPTWRandomConstructiveNew() { this(0.5, 3); }

    @Override
    public TSPTWSolution construct(TSPTWSolution solution) {
        var instance = solution.getInstance();
        TSPTWUtil.requireMinimumSize(instance);
        int size = instance.n() - 1;
        int[] tour = new int[size], candidates = new int[candidateListSize];
        double[] arrival = new double[size], slack = new double[size], scores = new double[candidateListSize];
        for (int i = 0; i < size; i++) tour[i] = i + 1;
        var random = RandomManager.getRandom();
        int previous = 0;
        double time = 0;
        for (int position = 0; position < size && !TimeControl.isTimeUp(); position++) {
            boolean hasFeasible = false;
            for (int j = position; j < size && !TimeControl.isTimeUp(); j++) {
                arrival[j] = Math.max(time + instance.dist(previous, tour[j]), instance.getWindowStart(tour[j]));
                slack[j] = instance.getWindowEnd(tour[j]) - arrival[j];
                if (slack[j] >= 0) hasFeasible = true;
            }
            if (TimeControl.isTimeUp()) break;
            double minIncrement = Double.POSITIVE_INFINITY, maxIncrement = Double.NEGATIVE_INFINITY;
            double minSlack = Double.POSITIVE_INFINITY, maxSlack = Double.NEGATIVE_INFINITY;
            for (int j = position; j < size && !TimeControl.isTimeUp(); j++) {
                if (hasFeasible && slack[j] < 0) continue;
                minIncrement = Math.min(minIncrement, arrival[j] - time);
                maxIncrement = Math.max(maxIncrement, arrival[j] - time);
                minSlack = Math.min(minSlack, slack[j]);
                maxSlack = Math.max(maxSlack, slack[j]);
            }
            int count = 0;
            for (int j = position; j < size && !TimeControl.isTimeUp(); j++) {
                if (hasFeasible && slack[j] < 0) continue;
                double incrementScore = maxIncrement > minIncrement ? (arrival[j] - time - minIncrement) / (maxIncrement - minIncrement) : 0;
                double urgencyScore = maxSlack > minSlack ? (slack[j] - minSlack) / (maxSlack - minSlack) : 0;
                // If every extension is late, prefer less lateness while completing the tour for repair.
                double score = hasFeasible ? (1 - urgencyWeight) * incrementScore + urgencyWeight * urgencyScore : -slack[j];
                int rank = count;
                while (rank > 0 && score < scores[rank - 1]) rank--;
                if (rank >= candidateListSize) continue;
                for (int k = Math.min(count, candidateListSize - 1); k > rank; k--) {
                    scores[k] = scores[k - 1];
                    candidates[k] = candidates[k - 1];
                }
                scores[rank] = score;
                candidates[rank] = j;
                count = Math.min(count + 1, candidateListSize);
            }
            if (TimeControl.isTimeUp()) break;
            int selected = candidates[random.nextInt(count)];
            time = arrival[selected];
            previous = tour[selected];
            tour[selected] = tour[position];
            tour[position] = previous;
        }
        // The unselected suffix still contains every remaining customer, even after interruption.
        solution.add(tour);
        solution.notifyUpdate();
        return solution;
    }

    @Override
    public String toString() {
        return "TSPTWRandomConstructiveNew{urgencyWeight=" + urgencyWeight + ", candidateListSize=" + candidateListSize + "}";
    }
}
