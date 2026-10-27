package es.urjc.etsii.grafo.autoconfig.autollm;

import es.urjc.etsii.grafo.monitoring.MonitorModeState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AutoLlmComparisonControllerTest {
    @TempDir Path temp;

    @Test void summariesAreIncrementalAndCurvesAreOnlyReadOnDemand() throws Exception {
        var campaign = new AutoLlmCampaign(); campaign.id = "saved";
        var trial = new AutoLlmCampaign.Trial(); trial.number = 1; trial.complete(AutoLlmCampaign.Stage.COMPARISON, "comparison", null);
        campaign.trials.add(trial);
        var store = mock(AutoLlmCampaignStore.class);
        when(store.status(campaign.id)).thenReturn(campaign);
        when(store.directory(campaign.id)).thenReturn(temp);
        var controller = new AutoLlmController(mock(AutoLlmCampaignService.class), store, mock(MonitorModeState.class), new MockEnvironment());
        var mvc = MockMvcBuilders.standaloneSetup(controller).build();
        String url = "/api/autollm/campaigns/saved/trials/1/comparison";
        Path journal = temp.resolve("comparison/execution/events.jsonl");
        Files.createDirectories(journal.getParent());
        String row = """
                {"kind":"evaluation-result","measured":true,"state":"SUCCEEDED","algorithm":"SOTA",
                 "instance":"instance.txt","instanceId":"instance","iteration":1,"objectives":{},
                 "executionTimeNanos":1000000,"timeToBestNanos":500000,
                 "curves":{"Cost":[{"instant":0,"value":30}]}}
                """.replace("\n", "") + "\n";
        Files.writeString(journal, row);
        long size = Files.size(journal); var modified = Files.getLastModifiedTime(journal);
        mvc.perform(get(url)).andExpect(status().isOk())
                .andExpect(jsonPath("$.counts.SOTA.SUCCEEDED").value(1))
                .andExpect(jsonPath("$.curves").doesNotExist());
        mvc.perform(get(url.replace("comparison", "curves"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.curves[0].curves.Cost[0].value").value(30));

        Files.writeString(journal, row.replace("\"value\":30", "\"value\":29"));
        Files.setLastModifiedTime(journal, modified);
        assertEquals(size, Files.size(journal));
        assertEquals(modified, Files.getLastModifiedTime(journal));
        mvc.perform(get(url.replace("comparison", "curves"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.curves[0].curves.Cost[0].value").value(29));
        mvc.perform(get(url)).andExpect(jsonPath("$.counts.SOTA.SUCCEEDED").value(1));
        Files.writeString(journal, row, StandardOpenOption.APPEND);
        mvc.perform(get(url)).andExpect(jsonPath("$.counts.SOTA.SUCCEEDED").value(2));

        Files.writeString(journal, "");
        mvc.perform(get(url)).andExpect(status().isOk())
                .andExpect(jsonPath("$.counts").isEmpty())
                .andExpect(jsonPath("$.curves").doesNotExist());
    }
}
