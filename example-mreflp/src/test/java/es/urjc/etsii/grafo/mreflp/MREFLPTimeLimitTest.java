package es.urjc.etsii.grafo.mreflp;

import es.urjc.etsii.grafo.mreflp.experiments.MREFLPTimeLimit;
import es.urjc.etsii.grafo.mreflp.experiments.ReferenceCatalog;
import es.urjc.etsii.grafo.mreflp.model.MREFLPInstanceImporter;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class MREFLPTimeLimitTest {
    @Test
    void usesPublishedTimesWithPositiveBudgetsForZerosAndMissingReferences() throws Exception {
        var limits = new MREFLPTimeLimit(new MREFLPConfig(), new ReferenceCatalog());
        var importer = new MREFLPInstanceImporter();
        assertEquals(1, limits.timeLimitInMillis(importer.importInstance("instances/cases/A-10-10-r2.case"), null));
        assertEquals(583_420, limits.timeLimitInMillis(importer.importInstance("instances/cases/sko81_03-r3.case"), null));
        assertEquals(1_000, limits.timeLimitInMillis(importer.importInstance("instances/cases/N-15-r2.case"), null));
    }

    @Test
    void allCasesHaveBudgetsAndOnlyN15NeedsTheMissingReferenceFallback() throws Exception {
        var references = new ReferenceCatalog();
        var limits = new MREFLPTimeLimit(new MREFLPConfig(), references);
        var importer = new MREFLPInstanceImporter();
        int cases = 0, missing = 0;
        for (String entry : Files.readAllLines(Path.of("instances/all.index"))) {
            var instance = importer.importInstance(Path.of("instances").resolve(entry).toString());
            long budget = limits.timeLimitInMillis(instance, null);
            assertTrue(budget >= 1 && budget <= 583_420, instance.getId());
            if (references.get(instance.getId(), "LMLS") == null) {
                assertEquals("N-15", instance.name());
                assertEquals(1_000, budget);
                missing++;
            }
            cases++;
        }
        assertEquals(572, cases);
        assertEquals(4, missing);
    }

    @Test
    void anExplicitUniformBudgetOverridesReferencesAndCanBeCleared() throws Exception {
        var config = new MREFLPConfig();
        var limits = new MREFLPTimeLimit(config, new ReferenceCatalog());
        var importer = new MREFLPInstanceImporter();
        var zero = importer.importInstance("instances/cases/A-10-10-r2.case");
        var missing = importer.importInstance("instances/cases/N-15-r2.case");
        config.setTimeLimitSeconds(600.0);
        assertEquals(600_000, limits.timeLimitInMillis(zero, null));
        assertEquals(600_000, limits.timeLimitInMillis(missing, null));
        config.setTimeLimitSeconds(null);
        assertEquals(1, limits.timeLimitInMillis(zero, null));
    }

    @Test
    void rejectsInvalidUniformBudgets() {
        var config = new MREFLPConfig();
        for (double seconds : new double[]{0, -1, Double.NaN, Double.POSITIVE_INFINITY, Double.MAX_VALUE}) {
            assertThrows(IllegalArgumentException.class, () -> config.setTimeLimitSeconds(seconds));
        }
    }
}
