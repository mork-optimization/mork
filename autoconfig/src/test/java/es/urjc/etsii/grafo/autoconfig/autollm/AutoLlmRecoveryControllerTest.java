package es.urjc.etsii.grafo.autoconfig.autollm;

import es.urjc.etsii.grafo.monitoring.MonitorModeState;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.Map;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AutoLlmRecoveryControllerTest {
    @Test void exposesEligibilityAndRoutesEachScopedRecoveryWithoutStartingAnotherCampaign() throws Exception {
        var service = mock(AutoLlmCampaignService.class); var mode = mock(MonitorModeState.class);
        when(mode.mode()).thenReturn(MonitorModeState.Mode.AUTOLLM);
        var allowed = AutoLlmRecoveryUtil.action(null, "Preserve saved evidence");
        when(service.recovery("saved")).thenReturn(new AutoLlmRecoveryUtil.Recovery(allowed, Map.of(9, new AutoLlmRecoveryUtil.TrialActions(allowed, allowed)), Map.of()));
        var mvc = MockMvcBuilders.standaloneSetup(new AutoLlmController(service, mock(AutoLlmCampaignStore.class), mode, new MockEnvironment())).build();
        String base = "/api/autollm/campaigns/saved";
        mvc.perform(get(base + "/recovery")).andExpect(status().isOk()).andExpect(jsonPath("$.trials.9.fail.allowed").value(true));
        mvc.perform(post(base + "/trials/9/fail").contentType(MediaType.APPLICATION_JSON).content("{\"reason\":\"Sampling exhausted\"}")).andExpect(status().isOk());
        mvc.perform(post(base + "/trials/3/resume")).andExpect(status().isOk());
        mvc.perform(post(base + "/targets/worker-a/requalify")).andExpect(status().isOk());
        verify(service).failTrial("saved", 9, "Sampling exhausted"); verify(service).resumeTrial("saved", 3); verify(service).requalifyTarget("saved", "worker-a");
        verify(service, never()).start(anyString(), anyBoolean(), anySet());
        when(mode.mode()).thenReturn(MonitorModeState.Mode.STANDARD);
        mvc.perform(post(base + "/trials/4/resume")).andExpect(status().isConflict());
        verify(service, never()).resumeTrial("saved", 4);
    }
}
