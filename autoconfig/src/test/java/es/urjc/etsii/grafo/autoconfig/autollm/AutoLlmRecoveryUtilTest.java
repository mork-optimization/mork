package es.urjc.etsii.grafo.autoconfig.autollm;

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class AutoLlmRecoveryUtilTest {
    private AutoLlmCampaign campaign() {
        var c = new AutoLlmCampaign(); c.id = "saved"; c.status = AutoLlmCampaign.CampaignStatus.RUNNING;
        var target = new AutoLlmCampaign.TargetState(); target.baselineReady = true; target.status = AutoLlmCampaign.TargetStatus.NEEDS_ATTENTION;
        c.targets.put("a", target);
        var trial = new AutoLlmCampaign.Trial(); trial.number = 1; trial.targetId = "a"; trial.status = AutoLlmCampaign.TrialStatus.BLOCKED; c.trials.add(trial);
        return c;
    }
    @Test void uncollectedRemoteJobsCanBeResumedButCannotBeFailedOrRequalified() {
        var recovery = AutoLlmRecoveryUtil.describe(campaign(), "saved", Set.of(), Set.of("a"), null);
        assertTrue(recovery.trials().get(1).resume().allowed());
        assertFalse(recovery.trials().get(1).fail().allowed());
        assertFalse(recovery.targets().get("a").requalify().allowed());
        assertTrue(recovery.trials().get(1).fail().reason().contains("collect"));
    }
    @Test void activeWorkBlocksEveryConflictingRecovery() {
        var recovery = AutoLlmRecoveryUtil.describe(campaign(), "saved", Set.of("a"), Set.of(), null);
        assertFalse(recovery.trials().get(1).resume().allowed()); assertFalse(recovery.trials().get(1).fail().allowed());
        assertFalse(recovery.targets().get("a").requalify().allowed());
        assertThrows(IllegalStateException.class, () -> recovery.targets().get("a").resume().requireAllowed());
    }
    @Test void stoppedCampaignRequiresExplicitResumeButCanRecordAReconciledFailure() {
        var c = campaign(); c.status = AutoLlmCampaign.CampaignStatus.INTERRUPTED; c.targets.get("a").draining = true;
        var recovery = AutoLlmRecoveryUtil.describe(c, null, Set.of(), Set.of(), null);
        assertTrue(recovery.resumeCampaign().allowed()); assertFalse(recovery.trials().get(1).resume().allowed());
        assertTrue(recovery.trials().get(1).fail().allowed());
        assertTrue(recovery.trials().get(1).fail().effect().contains("remains drained"));
        assertTrue(recovery.trials().get(1).fail().effect().contains("dispatch stays stopped"));
    }
    @Test void unqualifiedRemovedAndOtherOwnedOperationsHaveSpecificReasons() {
        var c = campaign(); c.targets.get("a").baselineReady = false;
        var recovery = AutoLlmRecoveryUtil.describe(c, "saved", Set.of(), Set.of(), null);
        assertTrue(recovery.trials().get(1).resume().reason().contains("Requalify"));
        assertTrue(recovery.targets().get("a").requalify().allowed());
        c.targets.get("a").status = AutoLlmCampaign.TargetStatus.REMOVED;
        assertFalse(AutoLlmRecoveryUtil.describe(c, "saved", Set.of(), Set.of(), null).targets().get("a").resume().allowed());
        assertEquals("Another campaign is active", AutoLlmRecoveryUtil.describe(c, "other", Set.of(), Set.of(), null).trials().get(1).fail().reason());
        assertEquals("Screening owns workspace", AutoLlmRecoveryUtil.describe(c, null, Set.of(), Set.of(), "Screening owns workspace").resumeCampaign().reason());
    }
}
