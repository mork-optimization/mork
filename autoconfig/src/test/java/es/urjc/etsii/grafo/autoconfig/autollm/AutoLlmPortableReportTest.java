package es.urjc.etsii.grafo.autoconfig.autollm;

import es.urjc.etsii.grafo.algorithms.FMode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class AutoLlmPortableReportTest {
    @TempDir Path root;
    private AutoLlmPortableReport.Snapshot report(AutoLlmCampaign c) {
        return AutoLlmPortableReport.build(c, root, new AutoLlmPortableReport.Selection("cost", FMode.MINIMIZE), Instant.parse("2026-11-01T01:00:00Z"));
    }
    @Test void reportsActualOverlapAndHistoricalProvenanceWithoutReadingDatasetPayloads() {
        var c = AutoLlmReportFixture.campaign(root);
        AutoLlmFileUtil.write(root.resolve("data/training-manifest.json"), Map.of("/gone/a", Map.of("path", "/gone/a"), "/gone/c", Map.of("path", "/gone/c")));
        AutoLlmFileUtil.write(root.resolve("data/comparison-manifest.json"), Map.of("/gone/a", Map.of("path", "/gone/a"), "/gone/b", Map.of("path", "/gone/b")));
        AutoLlmFileUtil.write(root.resolve("control/dataset-origin.json"), Map.of("selectionId", "saved-selection", "historicalHost", "old-host"));
        var snapshot = report(c);
        assertEquals(Map.of("training", 2, "comparison", 2, "overlap", 1), snapshot.datasetSizes());
        String html = AutoLlmReportHtml.render(snapshot);
        assertTrue(html.contains("overlap: 1")); assertTrue(html.contains("saved-selection")); assertTrue(html.contains("historical"));
        AutoLlmFileUtil.write(root.resolve("data/training-manifest.json"), Map.of("/gone/c", Map.of("path", "/gone/c")));
        assertEquals(0, report(c).datasetSizes().get("overlap"));
    }
    @Test void totalsUseOnlyCompleteHostMatchedTrialsAndPreservePartialFailures() {
        var c = AutoLlmReportFixture.campaign(root); var r = report(c);
        assertEquals(1, r.includedTrials(), r.trials().getFirst().exclusion()); assertEquals(1, r.wins()); assertEquals(1, r.ties()); assertEquals(0, r.losses());
        assertEquals(600000L, r.elapsed()); assertEquals(180000, r.attemptMillis());
        assertEquals(10.0, r.trials().getFirst().comparison().meanImprovementPercent());
        assertTrue(r.trials().get(1).exclusion().contains("FAILED")); assertEquals(2, r.trials().get(1).partial().size());
        assertEquals(260L, c.generationUsage.tokens().totalTokens());
        var component = r.trials().getFirst().components().getFirst(); assertTrue(component.verified()); assertTrue(component.selected());
        assertEquals(List.of(new AutoLlmPortableReport.Interval("BUILD",10000L), new AutoLlmPortableReport.Interval("METADATA",20000L)), r.trials().getFirst().attempts().get(1).substages());
        assertEquals("cost", AutoLlmPortableReport.selection(c,root,null).objective());
    }
    @Test void htmlIsSelfContainedEscapedAndPrintsEveryAttempt() {
        var c = AutoLlmReportFixture.campaign(root); String html = AutoLlmReportHtml.render(report(c));
        assertTrue(html.contains("-10.000%")); assertTrue(html.contains("SimpleAlgorithm")); assertTrue(html.contains("Trial token usage"));
        assertTrue(html.contains("&lt;script&gt;")); assertFalse(html.contains("<script>"));
        assertFalse(html.contains("src=\"http")); assertTrue(html.contains("@media print")); assertTrue(html.contains("id=trial-2"));
        assertTrue(html.contains("Regret insertion")); assertTrue(html.contains("Partial comparison evidence"));
        assertTrue(html.contains("Attempt 3")); assertTrue(html.contains("Cached input"));
    }
    @Test void absentOriginalInputsAndCompressedJournalsProduceIdenticalStatistics() throws Exception {
        var c = AutoLlmReportFixture.campaign(root); var before = report(c);
        for (var t : c.trials) AutoLlmEvidenceFiles.compact(root.resolve(t.latestDirectory(AutoLlmCampaign.Stage.COMPARISON) + "/execution/events.jsonl"));
        var after = report(c);
        assertEquals(before.wins(),after.wins()); assertEquals(before.trials().getFirst().comparison(), after.trials().getFirst().comparison());
    }
    @Test void wrongMembershipOrExtraRepetitionsExcludeTheComparison() {
        var c = AutoLlmReportFixture.campaign(root); var t = c.trials.getFirst();
        AutoLlmFileUtil.write(root.resolve(t.directory + "/data/comparison-manifest.json"), Map.of("/gone/c",Map.of("path","/gone/c"),"/gone/b",Map.of("path","/gone/b")));
        assertTrue(report(c).trials().getFirst().exclusion().contains("approved"));
        AutoLlmFileUtil.write(root.resolve(t.directory + "/data/comparison-manifest.json"), Map.of("/gone/a",Map.of("path","/gone/a"),"/gone/b",Map.of("path","/gone/b")));
        AutoLlmReportFixture.result(root,t,"a","SOTA",3,"SUCCEEDED",100);
        assertEquals(0,report(c).includedTrials());
    }
    @Test void remoteInstancePathsResolveThroughSavedAssignmentEvidence() throws Exception {
        var c=AutoLlmReportFixture.campaign(root); var t=c.trials.getFirst(); Path directory=root.resolve(t.directory);
        var ssh=AutoLlmFileUtil.JSON.convertValue(Map.of("execution",Map.of("target","SSH","ssh",Map.of("servers",List.of(Map.of("id","worker","host","offline","workspace","/remote"))))),AutoLlmConfig.class);
        AutoLlmFileUtil.write(directory.resolve("control/config.json"),ssh);
        var inputs=new LinkedHashMap<String,Object>();
        for(String name:List.of("a","b")) inputs.put("/gone/"+name,Map.of("path",directory.toRealPath().resolve("data/"+name).toString()));
        AutoLlmFileUtil.write(directory.resolve("data/staged-inputs.json"),inputs);
        String remote=es.urjc.etsii.grafo.autoconfig.autollm.execution.WorkloadPathUtil.remoteRoot(ssh,directory);
        Path journal=root.resolve(t.latestDirectory(AutoLlmCampaign.Stage.COMPARISON)+"/execution/events.jsonl");
        String text=Files.readString(journal).replace("/gone/a",remote+"/data/a").replace("/gone/b",remote+"/data/b"); Files.writeString(journal,text);
        assertEquals(1,report(c).includedTrials(),report(c).trials().getFirst().exclusion());
    }
    @Test void missingUsageAndOldManifestsAreNotInvented() throws Exception {
        var c = AutoLlmReportFixture.campaign(root); var t = c.trials.getFirst(); Path generation = root.resolve(t.attempts.getFirst().directory);
        Files.delete(generation.resolve("usage.json"));
        AutoLlmFileUtil.write(generation.resolve("manifest.json"),Map.of("components",List.of("GreedyNew: a recorded regret constructor")));
        GenerationEvidence.report(c,root);
        var r=report(c); assertEquals("PARTIAL",c.generationUsage.status());
        assertEquals("GreedyNew: a recorded regret constructor",r.trials().getFirst().components().getFirst().description());
        AutoLlmFileUtil.write(generation.resolve("manifest.json"),Map.of("components",List.of("Unrelated notes")));
        assertTrue(report(c).trials().getFirst().components().getFirst().description().contains("unavailable"));
    }
    @Test void timingsHandleLiveSnapshotsMissingEndsAndHistoricalSpans() {
        var c=AutoLlmReportFixture.campaign(root); c.startedAt=null; c.stoppedAt=null;
        assertTrue(report(c).durationBasis().startsWith("Approximate")); assertEquals(150000L,report(c).elapsed());
        var attempt=c.trials.getFirst().attempts.getLast(); attempt.finishedAt=null;
        assertEquals(1,report(c).trials().getFirst().unknownDurations());
        c.status=AutoLlmCampaign.CampaignStatus.RUNNING; c.trials.getFirst().status=AutoLlmCampaign.TrialStatus.COMPARISON; attempt.status=AutoLlmCampaign.AttemptStatus.RUNNING;
        assertEquals(3420000L,report(c).trials().getFirst().attempts().getLast().millis());
    }
    @Test void explicitMaximizationAndZeroReferenceSemanticsMatchExistingComparison() throws Exception {
        var c=AutoLlmReportFixture.campaign(root);
        var r=AutoLlmPortableReport.build(c,root,new AutoLlmPortableReport.Selection("quality",FMode.MAXIMIZE),Instant.now());
        assertEquals(1,r.losses()); assertEquals(1,r.ties());
        var t=c.trials.getFirst(); Files.delete(root.resolve(t.latestDirectory(AutoLlmCampaign.Stage.COMPARISON)+"/execution/events.jsonl"));
        for (String i:List.of("a","b")) for(String role:List.of("SOTA","IraceProposal")) for(int rep=0;rep<2;rep++) AutoLlmReportFixture.result(root,t,i,role,rep,"SUCCEEDED",0);
        assertNull(report(c).trials().getFirst().comparison().meanImprovementPercent()); assertEquals(2,report(c).ties());
    }
    @Test void missingObjectiveStillProducesAnHonestOverview() {
        var c=AutoLlmReportFixture.campaign(root);
        var r=AutoLlmPortableReport.build(c,root,new AutoLlmPortableReport.Selection(null,null),Instant.now());
        assertEquals(0,r.includedTrials()); assertFalse(r.warnings().isEmpty()); assertEquals(2,r.trials().size());
    }
    @Test void absentOrMalformedTimestampsRemainUnavailable() {
        var c=AutoLlmReportFixture.campaign(root); c.startedAt=null; c.stoppedAt=null;
        for (var t:c.trials) for (var a:t.attempts) { a.startedAt="unknown"; a.finishedAt=null; }
        assertNull(report(c).elapsed()); assertEquals(3,report(c).trials().getFirst().unknownDurations());
        for (var t:c.trials) t.attempts.clear();
        assertNull(report(c).elapsed()); assertDoesNotThrow(()->AutoLlmReportHtml.render(report(c)));
    }
    @Test void aReceiptKeepsItsOwnHtmlAfterAnotherObjectiveIsPublished() throws Exception {
        var c=AutoLlmReportFixture.campaign(root);
        var first=AutoLlmHtmlReports.generate(c,root,AutoLlmHtmlReports.queued(c,root,null),null);
        String original=Files.readString(root.resolve(first.path()));
        var second=AutoLlmHtmlReports.generate(c,root,AutoLlmHtmlReports.queued(c,root,new AutoLlmPortableReport.Selection("quality",FMode.MAXIMIZE)),null);
        assertNotEquals(first.path(),second.path());
        assertEquals(original,Files.readString(root.resolve(first.path())));
        assertEquals(Files.readString(root.resolve(second.path())),Files.readString(root.resolve("reports/report.html")));
    }
    @Test void publicationKeepsLastGoodHtmlAfterFailureAndRejectsCleanup() throws Exception {
        var c=AutoLlmReportFixture.campaign(root); var request=AutoLlmHtmlReports.queued(c,root,null);
        assertEquals("SUCCEEDED",AutoLlmHtmlReports.generate(c,root,request,null).state());
        String good=Files.readString(root.resolve("reports/report.html"));
        assertEquals(request.requestId(),AutoLlmHtmlReports.status(c,root,request.requestId()).requestId());
        c.trials.getFirst().attempts.getFirst().directory="../escape";
        var failed=AutoLlmHtmlReports.generate(c,root,AutoLlmHtmlReports.queued(c,root,null),null);
        assertEquals("FAILED",failed.state()); assertEquals(good,Files.readString(root.resolve("reports/report.html")));
        Files.delete(root.resolve("campaign.json"));
        assertThrows(IllegalStateException.class,()->AutoLlmHtmlReports.queued(c,root,null));
    }
}
