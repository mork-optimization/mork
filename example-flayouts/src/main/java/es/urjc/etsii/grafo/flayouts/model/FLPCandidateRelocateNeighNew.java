package es.urjc.etsii.grafo.flayouts.model;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.annotations.CategoricalParam;
import es.urjc.etsii.grafo.util.TimeControl;
import es.urjc.etsii.grafo.util.random.RandomManager;

import java.util.ArrayList;
import java.util.Comparator;

/** Restricted directed relocation with random coverage and optional full scans. */
public class FLPCandidateRelocateNeighNew extends FLPPreservingNeighNew {
    public enum Relation { FLOW, NEAR, MIXED }
    private final int candidates;
    private final int fullScanFrequency;
    private final Relation relation;

    @AutoconfigConstructor
    public FLPCandidateRelocateNeighNew(
            @CategoricalParam(strings = {"4", "8", "16", "32"}) int candidates,
            @CategoricalParam(strings = {"0", "1", "5", "10"}) int fullScanFrequency,
            @CategoricalParam(strings = {"FLOW", "NEAR", "MIXED"}) Relation relation) {
        super(true, true);
        if (candidates < 1 || fullScanFrequency < 0) throw new IllegalArgumentException("Invalid candidate or full-scan count");
        this.candidates = candidates;
        this.fullScanFrequency = fullScanFrequency;
        this.relation = java.util.Objects.requireNonNull(relation);
    }

    @Override
    protected FLPNewUtil.MoveSpace space(FLPSolution s) {
        // A full scan every frequency calls in expectation; no mutable state survives between runs.
        if (fullScanFrequency > 0 && RandomManager.getRandom().nextInt(fullScanFrequency) == 0) {
            return FLPNewUtil.relocationSpace(s, 1, false, FLPBlockRelocateNeighNew.Scope.ALL, true);
        }
        var positions = FLPNewUtil.positions(s);
        var gaps = FLPNewUtil.gaps(s);
        int n = s.getInstance().nFacilities();
        boolean[][] allowed = new boolean[n][n];
        for (var position : positions) {
            if (TimeControl.isTimeUp()) break;
            int facility = s.rows[position.row()][position.pos()];
            var partners = new ArrayList<Integer>();
            for (var p : positions) {
                int other = s.rows[p.row()][p.pos()];
                if (other != facility) partners.add(other);
            }
            partners.sort(Comparator.<Integer>comparingDouble(other -> relationScore(s, facility, other)).reversed());
            for (int i = 0; i < Math.min(candidates, partners.size()); i++) allowed[facility][partners.get(i)] = true;
            if (!partners.isEmpty()) allowed[facility][partners.get(RandomManager.getRandom().nextInt(partners.size()))] = true;
        }
        return new FLPNewUtil.MoveSpace((long) positions.length * gaps.length, index -> {
            var origin = positions[(int) (index / gaps.length)];
            var target = gaps[(int) (index % gaps.length)];
            if (origin.row() == target.row() && (target.pos() == origin.pos() || target.pos() == origin.pos() + 1)) return null;
            int facility = s.rows[origin.row()][origin.pos()];
            boolean edge = target.pos() == 0 || target.pos() == s.rowSize(target.row());
            boolean nearBefore = target.pos() < s.rowSize(target.row()) && allowed[facility][s.rows[target.row()][target.pos()]];
            boolean nearAfter = target.pos() > 0 && allowed[facility][s.rows[target.row()][target.pos() - 1]];
            if (!edge && !nearBefore && !nearAfter) return null;
            return FLPNewUtil.relocate(s, origin.row(), origin.pos(), 1, target.row(), target.pos(), false, true);
        });
    }

    private double relationScore(FLPSolution s, int first, int second) {
        double distance = Math.abs(s.center[first] - s.center[second]);
        return switch (relation) {
            case FLOW -> s.getInstance().flow(first, second);
            case NEAR -> -distance;
            case MIXED -> s.getInstance().flow(first, second) / (1 + distance);
        };
    }

    @Override
    public String toString() { return "FLPCandidateRelocateNeighNew{candidates=" + candidates + ", fullScanFrequency=" + fullScanFrequency + ", relation=" + relation + "}"; }
}
