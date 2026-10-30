package es.urjc.etsii.grafo.autoconfig.autollm;

import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.util.StatisticsUtil;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class AutoLlmComparisonReportTest {
    private AutoLlmComparisonUtil.Measured measured(double value) {
        var stats = StatisticsUtil.summarize(List.of(value, value), FMode.MINIMIZE, null);
        return new AutoLlmComparisonUtil.Measured(stats, stats, stats);
    }
    private List<AutoLlmComparisonUtil.MeasuredInstance> instance(double reference, double proposal) {
        return List.of(new AutoLlmComparisonUtil.MeasuredInstance("same-input", "instance", Map.of("SOTA", Map.of("SUCCEEDED", 2), "IraceProposal", Map.of("SUCCEEDED", 2)),
                Map.of("SOTA", measured(reference), "IraceProposal", measured(proposal))));
    }
    @Test void rankingUsesACommonReferenceInsteadOfRewardingAWorseHostBaseline() {
        var first = AutoLlmComparisonReport.run(1, "first", 2, 1, "first.jsonl", instance(100, 70), FMode.MINIMIZE);
        var second = AutoLlmComparisonReport.run(2, "second", 2, 1, "second.jsonl", instance(200, 80), FMode.MINIMIZE);
        assertEquals(30, first.meanImprovementPercent()); assertEquals(60, second.meanImprovementPercent());
        assertEquals(1, AutoLlmComparisonReport.rank(List.of(first, second), FMode.MINIMIZE).getFirst().trial());
        assertEquals(1, first.wins());
    }
    @Test void zeroReferencesStayUndefinedAndIncompleteRepetitionsAreRejected() {
        var zero = AutoLlmComparisonReport.run(1, "first", 2, 1, "events.jsonl", instance(0, 0), FMode.MAXIMIZE);
        assertNull(zero.meanImprovementPercent()); assertEquals(1, zero.ties());
        assertThrows(IllegalStateException.class, () -> AutoLlmComparisonReport.rank(List.of(zero), FMode.MAXIMIZE));
        assertThrows(IllegalStateException.class, () -> AutoLlmComparisonReport.run(1, "first", 30, 1, "events.jsonl", instance(1, 2), FMode.MINIMIZE));
    }
    @Test void approvalHashIsIndependentOfMapInsertionOrder() {
        var first = new LinkedHashMap<String,Object>(); first.put("b", 2); first.put("a", Map.of("z", 1, "x", 3));
        var second = new LinkedHashMap<String,Object>(); second.put("a", Map.of("x", 3, "z", 1)); second.put("b", 2);
        assertEquals(AutoLlmFileUtil.canonicalHash(first), AutoLlmFileUtil.canonicalHash(second));
    }
}
