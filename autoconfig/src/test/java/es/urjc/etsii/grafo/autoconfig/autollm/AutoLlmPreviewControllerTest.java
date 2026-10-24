package es.urjc.etsii.grafo.autoconfig.autollm;

import es.urjc.etsii.grafo.monitoring.MonitorModeState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.nio.file.*;
import java.util.UUID;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AutoLlmPreviewControllerTest {
    @TempDir Path temp;

    @Test void previewsTheTailAndDownloadsTheUnabridgedFileWithTheSamePathChecks() throws Exception {
        var config = new AutoLlmConfig(null, temp.toString(), null, null, null, null, null, null, null, null, null, null, null, null);
        var store = new AutoLlmCampaignStore(config);
        String id = UUID.randomUUID().toString();
        Path file = store.directory(id).resolve("output.log"); Files.createDirectories(file.getParent());
        String contents = "first\nsecond\nthird\n"; Files.writeString(file, contents);
        Files.writeString(temp.resolve("outside.log"), "outside");
        var controller = new AutoLlmController(mock(AutoLlmCampaignService.class), store, mock(MonitorModeState.class), new MockEnvironment());
        var mvc = MockMvcBuilders.standaloneSetup(controller).build();
        String base = "/api/autollm/campaigns/" + id;
        mvc.perform(get(base + "/preview").param("path", "output.log").param("lines", "2"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.text").value("second\nthird\n"))
                .andExpect(jsonPath("$.lineCount").value(2)).andExpect(jsonPath("$.truncated").value(true));
        mvc.perform(get(base + "/artifact").param("path", "output.log"))
                .andExpect(status().isOk()).andExpect(header().string("Content-Disposition", "attachment; filename=\"output.log\""))
                .andExpect(content().string(contents));
        mvc.perform(get(base + "/preview").param("path", "output.log").param("lines", "5001")).andExpect(status().isBadRequest());
        mvc.perform(get(base + "/preview").param("path", "../outside.log")).andExpect(status().isBadRequest());
        mvc.perform(get(base + "/preview").param("path", "missing.log")).andExpect(status().isNotFound());
    }
    @Test void trialDiagnosticsListOnlyTheirOwnMetadataWithoutReadingFileHashes() throws Exception {
        var config = new AutoLlmConfig(null, temp.toString(), null, null, null, null, null, null, null, null, null, null, null, null);
        var store = new AutoLlmCampaignStore(config);
        var campaign = new AutoLlmCampaign(); campaign.id = UUID.randomUUID().toString(); campaign.config = config; campaign.createdAt = "now";
        var trial = new AutoLlmCampaign.Trial(); trial.number = 9; trial.directory = "targets/a/trials/009"; campaign.trials.add(trial); store.save(campaign);
        Path file = store.directory(campaign.id).resolve(trial.directory).resolve("stderr.log");
        Files.createDirectories(file.getParent()); Files.writeString(file, "failure evidence");
        Files.writeString(store.directory(campaign.id).resolve("other.log"), "unrelated");
        var mvc = MockMvcBuilders.standaloneSetup(new AutoLlmController(mock(AutoLlmCampaignService.class), store, mock(MonitorModeState.class), new MockEnvironment())).build();
        mvc.perform(get("/api/autollm/campaigns/" + campaign.id + "/artifacts").param("trial", "9").param("hashes", "false"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].path").value(trial.directory + "/stderr.log")).andExpect(jsonPath("$[0].sha256").isEmpty());
        org.junit.jupiter.api.Assertions.assertEquals(AutoLlmFileUtil.hash(file), store.artifacts(campaign.id, trial.directory, true).getFirst().sha256());
        mvc.perform(get("/api/autollm/campaigns/" + campaign.id + "/artifacts").param("trial", "10")).andExpect(status().isNotFound());
    }
}
