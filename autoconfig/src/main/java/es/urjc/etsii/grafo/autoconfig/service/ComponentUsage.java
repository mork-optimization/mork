package es.urjc.etsii.grafo.autoconfig.service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/** Immutable component analytics contracts for the current automatic run. */
public final class ComponentUsage {
    private ComponentUsage() {}

    public enum Scope { ALL, CURRENT_ELITES }
    public enum Basis { CANDIDATE, EVALUATION }

    public record RelationshipKey(String parent, String role, String child) {}

    public record Footprint(String root, Map<String, Long> components, Map<RelationshipKey, Long> relationships) {
        public Footprint {
            components = Map.copyOf(components);
            relationships = Map.copyOf(relationships);
        }
    }

    public record Component(
            String name, long candidatePlacements, long configurationCount, long evaluationPlacements,
            long rootCandidatePlacements, long rootConfigurationCount, long rootEvaluationPlacements
    ) {}

    public record Relationship(
            String parent, String role, String child,
            long candidatePlacements, long configurationCount, long evaluationPlacements
    ) {}

    public record Snapshot(
            String runId, Scope scope, long latestEvaluationRevision, Instant eliteUpdatedAt,
            long configurationCount, long decodedConfigurationCount, long unavailableConfigurationCount,
            long evaluationCount, long candidatePlacementCount, long evaluationPlacementCount,
            List<Component> components, List<Relationship> relationships
    ) {
        public Snapshot {
            components = List.copyOf(components);
            relationships = List.copyOf(relationships);
        }
    }

    public record Candidate(
            String configurationId, long occurrences, long evaluationPlacements,
            Integer elitePosition, AutoconfigRunState.CandidateEvaluationCounts evaluations
    ) {}

    public record CandidatePage(
            String runId, Scope scope, int total, int offset, Integer nextOffset, List<Candidate> candidates
    ) {
        public CandidatePage { candidates = List.copyOf(candidates); }
    }
}
