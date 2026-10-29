package es.urjc.etsii.grafo.autoconfig.autollm;

import es.urjc.etsii.grafo.monitoring.MonitorModeState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AutoLlmComparisonControllerTest {
    @TempDir Path temp;
    private static final String URL = "/api/autollm/campaigns/saved/trials/1/";

    private MockMvc controller() throws Exception {
        var campaign = new AutoLlmCampaign(); campaign.id = "saved";
        var trial = new AutoLlmCampaign.Trial(); trial.number = 1; trial.complete(AutoLlmCampaign.Stage.COMPARISON, "comparison", null);
        campaign.trials.add(trial);
        var store = mock(AutoLlmCampaignStore.class);
        when(store.status(campaign.id)).thenReturn(campaign);
        when(store.directory(campaign.id)).thenReturn(temp);
        Files.createDirectories(journal().getParent());
        return MockMvcBuilders.standaloneSetup(new AutoLlmController(mock(AutoLlmCampaignService.class), store, mock(MonitorModeState.class), new MockEnvironment())).build();
    }
    private Path journal() { return temp.resolve("comparison/execution/events.jsonl"); }
    private String row(String instance, String algorithm, int iteration, String state) {
        var points = new ArrayList<Object>();
        for (int i = 0; i < 600; i++) points.add(Map.of("instant", i, "value", 600 - i));
        var event = new LinkedHashMap<String,Object>();
        event.put("kind", "evaluation-result"); event.put("measured", true); event.put("state", state);
        event.put("algorithm", algorithm); event.put("instance", "/remote/" + instance); event.put("instanceId", instance); event.put("iteration", String.valueOf(iteration));
        event.put("objectives", Map.of()); event.put("executionTimeNanos", 1000000); event.put("timeToBestNanos", 500000);
        event.put("curves", Map.of("Cost", points));
        return AutoLlmFileUtil.JSON.writeValueAsString(event) + "\n";
    }
    @Test void returnsBothAlgorithmsForOneInstanceWithoutPaginationAndSamplesEndpoints() throws Exception {
        var mvc = controller();
        // More than the old 4 MiB read limit, with many non-result events before either algorithm.
        var journal = new StringBuilder(("{\"kind\":\"evaluation-phase\",\"padding\":\"" + "x".repeat(2000) + "\"}\n").repeat(2200));
        for (String algorithm : List.of("SOTA", "IraceProposal")) for (int i = 0; i < 30; i++) journal.append(row("one", algorithm, i, "SUCCEEDED"));
        journal.append(row("two", "SOTA", 0, "SUCCEEDED"));
        journal.append(row("one", "SOTA", 30, "FAILED"));
        journal.append(row("one", "SOTA", 31, "SUCCEEDED").replace("\"measured\":true", "\"measured\":false"));
        Files.writeString(journal(), journal);
        // Curves work on a cold cache, without first requesting a summary.
        mvc.perform(get(URL + "curves").param("instanceId", "one")).andExpect(status().isOk())
                .andExpect(jsonPath("$.curves.length()").value(60))
                .andExpect(jsonPath("$.curves[0].algorithm").value("SOTA"))
                .andExpect(jsonPath("$.curves[30].algorithm").value("IraceProposal"))
                .andExpect(jsonPath("$.curves[0].curves.Cost.length()").value(256))
                .andExpect(jsonPath("$.curves[0].curves.Cost[0].instant").value(0))
                .andExpect(jsonPath("$.curves[0].curves.Cost[255].instant").value(599))
                .andExpect(jsonPath("$.nextCursor").doesNotExist()).andExpect(jsonPath("$.more").doesNotExist());
        mvc.perform(get(URL + "comparison")).andExpect(jsonPath("$.curves").doesNotExist())
                .andExpect(jsonPath("$.instances.length()").value(2))
                .andExpect(jsonPath("$.instances[0].instanceId").value("one"))
                .andExpect(jsonPath("$.instances[0].counts.SOTA.SUCCEEDED").value(30))
                .andExpect(jsonPath("$.instances[0].counts.SOTA.FAILED").value(1))
                .andExpect(jsonPath("$.instances[0].counts.IraceProposal.SUCCEEDED").value(30));
        mvc.perform(get(URL + "curves").param("instanceId", "two")).andExpect(jsonPath("$.curves.length()").value(1));
        mvc.perform(get(URL + "curves").param("instanceId", "missing")).andExpect(jsonPath("$.curves").isEmpty());
        mvc.perform(get(URL + "curves")).andExpect(status().isBadRequest());
    }
    @Test void incrementallyIndexesCompleteRowsAndResetsAfterTruncationOrReplacement() throws Exception {
        var mvc = controller();
        mvc.perform(get(URL + "curves").param("instanceId", "one")).andExpect(jsonPath("$.curves").isEmpty());
        String first = row("one", "SOTA", 0, "SUCCEEDED");
        Files.writeString(journal(), first + "malformed\n" + row("one", "IraceProposal", 0, "SUCCEEDED").stripTrailing());
        mvc.perform(get(URL + "comparison")).andExpect(jsonPath("$.counts.SOTA.SUCCEEDED").value(1))
                .andExpect(jsonPath("$.counts.IraceProposal").doesNotExist()).andExpect(jsonPath("$.omittedEvents").value(1));
        mvc.perform(get(URL + "curves").param("instanceId", "one")).andExpect(jsonPath("$.curves.length()").value(1));
        Files.writeString(journal(), "\n", StandardOpenOption.APPEND);
        mvc.perform(get(URL + "curves").param("instanceId", "one")).andExpect(jsonPath("$.curves.length()").value(2));
        mvc.perform(get(URL + "comparison")).andExpect(jsonPath("$.counts.IraceProposal.SUCCEEDED").value(1));
        mvc.perform(get(URL + "comparison")).andExpect(jsonPath("$.counts.SOTA.SUCCEEDED").value(1));
        Files.writeString(journal(), "");
        mvc.perform(get(URL + "comparison")).andExpect(jsonPath("$.instances").isEmpty());
        Files.writeString(journal(), first);
        mvc.perform(get(URL + "curves").param("instanceId", "one")).andExpect(jsonPath("$.curves.length()").value(1));
        Path replacement = temp.resolve("replacement"); Files.writeString(replacement, row("two", "SOTA", 0, "SUCCEEDED"));
        Files.move(replacement, journal(), StandardCopyOption.REPLACE_EXISTING);
        mvc.perform(get(URL + "curves").param("instanceId", "one")).andExpect(jsonPath("$.curves").isEmpty());
        mvc.perform(get(URL + "comparison")).andExpect(jsonPath("$.instances[0].instanceId").value("two"));
    }
    @Test void skipsOversizedRowsThroughTheirNewlineWithoutLosingFollowingResults() throws Exception {
        var mvc = controller();
        Files.writeString(journal(), "x".repeat(4 * 1024 * 1024 + 100) + "\n" + row("one", "SOTA", 0, "SUCCEEDED"));
        mvc.perform(get(URL + "curves").param("instanceId", "one")).andExpect(jsonPath("$.curves.length()").value(1));
        mvc.perform(get(URL + "comparison")).andExpect(jsonPath("$.omittedEvents").value(1));
    }
    @Test void readsCurveValuesOnDemandRatherThanCachingAllPoints() throws Exception {
        var mvc = controller();
        String row = row("one", "SOTA", 0, "SUCCEEDED"); Files.writeString(journal(), row);
        mvc.perform(get(URL + "comparison")).andExpect(jsonPath("$.counts.SOTA.SUCCEEDED").value(1));
        var modified = Files.getLastModifiedTime(journal()); long size = Files.size(journal());
        Files.writeString(journal(), row.replace("\"value\":600", "\"value\":599")); Files.setLastModifiedTime(journal(), modified);
        assertEquals(size, Files.size(journal()));
        mvc.perform(get(URL + "curves").param("instanceId", "one")).andExpect(jsonPath("$.curves[0].curves.Cost[0].value").value(599));
    }
}
