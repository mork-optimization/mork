package es.urjc.etsii.grafo.autoconfig.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/** Mutable accumulator owned by, and guarded by the lock of, AutoconfigRunState. */
final class ComponentUsageAggregator {
    private final Map<String, Counts> components = new HashMap<>();
    private final Map<ComponentUsage.RelationshipKey, Counts> relationships = new HashMap<>();
    private long configurationCount;
    private long decodedConfigurationCount;
    private long evaluationCount;
    private long candidatePlacementCount;
    private long evaluationPlacementCount;

    void add(ComponentUsage.Footprint footprint, boolean registerStructure, long evaluations) {
        evaluationCount += evaluations;
        if (registerStructure) {
            configurationCount++;
            if (footprint != null) {
                decodedConfigurationCount++;
            }
        }
        if (footprint == null) {
            return;
        }
        for (var entry : footprint.components().entrySet()) {
            long occurrences = entry.getValue();
            var counts = components.computeIfAbsent(entry.getKey(), ignored -> new Counts());
            counts.add(occurrences, registerStructure, evaluations);
            if (entry.getKey().equals(footprint.root())) {
                counts.root.add(1, registerStructure, evaluations);
            }
            if (registerStructure) {
                candidatePlacementCount += occurrences;
            }
            evaluationPlacementCount += occurrences * evaluations;
        }
        for (var entry : footprint.relationships().entrySet()) {
            relationships.computeIfAbsent(entry.getKey(), ignored -> new Counts())
                    .add(entry.getValue(), registerStructure, evaluations);
        }
    }

    ComponentUsage.Snapshot snapshot(String runId, ComponentUsage.Scope scope, long revision, Instant eliteUpdatedAt) {
        var componentViews = new ArrayList<ComponentUsage.Component>(components.size());
        for (var entry : components.entrySet()) {
            var count = entry.getValue();
            componentViews.add(new ComponentUsage.Component(entry.getKey(), count.placements, count.configurations,
                    count.evaluations, count.root.placements, count.root.configurations, count.root.evaluations));
        }
        componentViews.sort(Comparator.comparing(ComponentUsage.Component::name));
        var relationshipViews = new ArrayList<ComponentUsage.Relationship>(relationships.size());
        for (var entry : relationships.entrySet()) {
            var key = entry.getKey();
            var count = entry.getValue();
            relationshipViews.add(new ComponentUsage.Relationship(key.parent(), key.role(), key.child(),
                    count.placements, count.configurations, count.evaluations));
        }
        relationshipViews.sort(Comparator.comparing(ComponentUsage.Relationship::parent)
                .thenComparing(ComponentUsage.Relationship::role).thenComparing(ComponentUsage.Relationship::child));
        return new ComponentUsage.Snapshot(runId, scope, revision, eliteUpdatedAt,
                configurationCount, decodedConfigurationCount, configurationCount - decodedConfigurationCount,
                evaluationCount, candidatePlacementCount, evaluationPlacementCount, componentViews, relationshipViews);
    }

    private static class Totals {
        long placements;
        long configurations;
        long evaluations;

        void add(long occurrences, boolean registerStructure, long evaluationCount) {
            if (registerStructure) {
                placements += occurrences;
                configurations++;
            }
            evaluations += occurrences * evaluationCount;
        }
    }

    private static final class Counts extends Totals {
        final Totals root = new Totals();
    }
}
