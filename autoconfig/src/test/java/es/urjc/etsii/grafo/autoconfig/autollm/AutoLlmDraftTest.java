package es.urjc.etsii.grafo.autoconfig.autollm;

import es.urjc.etsii.grafo.config.InstanceConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AutoLlmDraftTest {
    @TempDir Path temp;
    @Test void configurationEditsRetainScreeningAndRequireExplicitDatasetEdits() throws Exception {
        Files.createDirectory(temp.resolve(".git"));
        Path instance = temp.resolve("instance.txt"), sota = temp.resolve("sota.json");
        Files.writeString(instance, "input"); Files.writeString(sota, "{\"$component\":\"Dummy\"}");
        var configuration = new AutoLlmConfig(new AutoLlmConfig.Project(temp.toString(), "fixed", List.of("build"), null, null),
                temp.resolve("campaigns").toString(), 1, null, null, sota.toString(), null, instance.toString(), instance.toString(), null, null, null, null, null);
        var store = new AutoLlmCampaignStore(configuration);
        var service = new AutoLlmCampaignService(configuration, store, mock(AutoLlmProcessRunner.class), mock(CodexTaskRunner.class), mock(AutoLlmReportService.class), new InstanceConfiguration());
        try {
            var draft = service.create(configuration); draft.screeningPath = temp.resolve("screening-evidence").toString(); store.save(draft);
            Path root = store.directory(draft.id);
            AutoLlmFileUtil.write(root.resolve("control/input-aliases.json"), Map.of("original", instance.toString()));
            AutoLlmFileUtil.write(root.resolve("control/dataset-decisions.json"), Map.of("excluded", Map.of()));
            AutoLlmFileUtil.write(root.resolve("control/original-pool.json"), List.of(instance.toString()));
            var c = draft.config;
            var revised = new AutoLlmConfig(c.project(), c.workspace(), c.trials(), c.seed(), c.maxParameters(), c.sotaFile(), c.initialConfigurationsFile(),
                    c.trainingPath(), c.comparisonPath(), c.codex(), new AutoLlmConfig.Preparation(null, null, null, 3, null, null, null), c.tuning(), c.comparison(), c.execution());
            var replacement = service.editDraft(draft.id, revised);
            assertNotEquals(draft.id, replacement.id); assertEquals(draft.screeningPath, replacement.screeningPath);
            for (String file : List.of("input-aliases.json", "dataset-decisions.json", "original-pool.json"))
                assertEquals(Files.readString(root.resolve("control/" + file)), Files.readString(store.directory(replacement.id).resolve("control/" + file)));
            Path other = temp.resolve("other.txt"); Files.writeString(other, "other input");
            assertThrows(IllegalArgumentException.class, () -> service.editDraft(replacement.id, revised.withPaths(other.toString(), other.toString())));
            assertNull(service.active());
        } finally { service.close(); }
    }
}
