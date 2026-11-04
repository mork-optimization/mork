package es.urjc.etsii.grafo.autoconfig.autollm;

import es.urjc.etsii.grafo.config.InstanceConfiguration;
import es.urjc.etsii.grafo.algorithms.FMode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AutoLlmReportLifecycleTest {
    @TempDir Path workspace;
    @Test void completedCampaignAutomaticallyPublishesWithoutReexecutingTrials() throws Exception {
        var config=AutoLlmFileUtil.JSON.convertValue(Map.of("workspace",workspace.toString()),AutoLlmConfig.class);
        var store=new AutoLlmCampaignStore(config); String id=UUID.randomUUID().toString(); Path root=store.directory(id);
        var campaign=AutoLlmReportFixture.campaign(root); campaign.id=id; store.save(campaign);
        var processes=mock(AutoLlmProcessRunner.class); var codex=mock(CodexTaskRunner.class);
        var service=new AutoLlmCampaignService(config,store,processes,codex,mock(AutoLlmReportService.class),mock(InstanceConfiguration.class));
        try {
            org.springframework.test.util.ReflectionTestUtils.setField(service,"current",campaign);
            org.springframework.test.util.ReflectionTestUtils.setField(service,"active",id);
            org.springframework.test.util.ReflectionTestUtils.invokeMethod(service,"settle");
            assertTimeout(java.time.Duration.ofSeconds(2), () -> {
                while (service.active() != null) Thread.sleep(5);
            });
            assertEquals(AutoLlmCampaign.CampaignStatus.COMPLETED_WITH_FAILURES,store.load(id).status);
        } finally { service.close(); }
        assertTrue(Files.isRegularFile(root.resolve("reports/report.html")));
        assertEquals("SUCCEEDED",AutoLlmHtmlReports.status(campaign,root,null).state());
        verifyNoInteractions(processes,codex);
    }
    @Test void activeCampaignReportsAreQueuedAndCleanupWaitsForTheWriter() throws Exception {
        var config=AutoLlmFileUtil.JSON.convertValue(Map.of("workspace",workspace.toString()),AutoLlmConfig.class);
        var store=new AutoLlmCampaignStore(config); String id=UUID.randomUUID().toString(); Path root=store.directory(id);
        var campaign=AutoLlmReportFixture.campaign(root); campaign.id=id; campaign.status=AutoLlmCampaign.CampaignStatus.RUNNING; store.save(campaign);
        var entered=new CountDownLatch(1); var finish=new CountDownLatch(1);
        var exports=mock(AutoLlmReportService.class);
        doAnswer(call->{ entered.countDown(); assertTrue(finish.await(5,TimeUnit.SECONDS)); return null; }).when(exports).report(any(),any());
        var service=new AutoLlmCampaignService(config,store,mock(AutoLlmProcessRunner.class),mock(CodexTaskRunner.class),exports,mock(InstanceConfiguration.class));
        try {
            var receipt=service.report(id,new AutoLlmPortableReport.Selection("cost",FMode.MINIMIZE));
            assertEquals("QUEUED",receipt.state()); assertTrue(entered.await(5,TimeUnit.SECONDS));
            assertEquals(AutoLlmCampaign.CampaignStatus.RUNNING,store.load(id).status);
            // Mimic the same cleanup marker read by both the controller and report publisher.
            var cleanup=new AutoLlmCleanup(store);
            new es.urjc.etsii.grafo.autoconfig.autollm.execution.OwnershipManifest(workspace).directory(root,"campaign",root.toString());
            var plan=cleanup.plan(new AutoLlmCleanup.Selection("campaign",id,null,false,true));
            cleanup.requestCancellation(plan.digest());
            assertTrue(service.hasSelectedReports());
            assertThrows(IllegalStateException.class,()->service.report(id,null));
            finish.countDown();
        } finally { finish.countDown(); service.close(); }
        assertFalse(Files.exists(root.resolve("reports/report.html")),"Cleanup must suppress report publication");
    }
}
