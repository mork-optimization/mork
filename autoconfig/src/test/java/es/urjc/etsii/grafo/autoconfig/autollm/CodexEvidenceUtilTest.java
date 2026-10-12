package es.urjc.etsii.grafo.autoconfig.autollm;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class CodexEvidenceUtilTest {
    @TempDir Path temp;
    private Path home() { return temp.resolve("custom-home"); }
    private Path checkout() { return temp.resolve("checkout"); }
    private Path session(String id, String parent) {
        Path path = home().resolve("sessions/2026/10/12/rollout-" + id + ".jsonl");
        var payload = new LinkedHashMap<String,Object>(Map.of("id", id, "cwd", checkout().toString(), "cli_version", "0.162.1"));
        if (parent != null) payload.put("source", Map.of("subagent", Map.of("thread_spawn", Map.of("parent_thread_id", parent))));
        AutoLlmFileUtil.append(path, Map.of("type", "session_meta", "payload", payload));
        AutoLlmFileUtil.append(path, Map.of("type", "turn_context", "payload", Map.of("model", "fixture-model")));
        return path;
    }
    private Map<String,Object> counts(int input, int output) {
        return Map.of("input_tokens", input, "cached_input_tokens", 2, "cache_write_input_tokens", 0, "output_tokens", output, "reasoning_output_tokens", 1, "total_tokens", input + output);
    }
    private void response(Path path, String response, int input, int output) {
        AutoLlmFileUtil.append(path, Map.of("type", "token_usage_record", "payload", Map.of("response_id", response, "usage", counts(input, output))));
    }
    private void counter(Path path, int input, int output) {
        AutoLlmFileUtil.append(path, Map.of("type", "event_msg", "payload", Map.of("type", "token_count", "info", Map.of("total_token_usage", counts(input, output)))));
    }
    private CodexEvidenceUtil.Evidence collect(Path output, String id) { return CodexEvidenceUtil.collect(home(), checkout(), output, id); }
    private String id() { return UUID.randomUUID().toString(); }

    @Test void exactConversationAndLinkedChildrenAreArchivedWithDeduplicatedUsage() throws Exception {
        Path output = temp.resolve("attempt"); CodexEvidenceUtil.begin(home(), checkout(), output, null);
        String root = id(), child = id(), grandchild = id();
        Path main = session(root, null), childPath = session(child, root); session(grandchild, child);
        response(main, "response-1", 10, 4); response(childPath, "response-1", 10, 4); response(childPath, "response-2", 20, 6);
        response(session(id(), null), "unrelated", 999, 999);
        Files.writeString(home().resolve("auth.json"), "secret-auth-data");
        var evidence = collect(output, root);
        assertEquals("ARCHIVED", evidence.status()); assertEquals(3, evidence.conversations().size());
        assertEquals(30L, evidence.usage().tokens().inputTokens()); assertEquals(10L, evidence.usage().tokens().outputTokens());
        assertEquals("PARTIAL", evidence.usage().status(), "Child with no usage is explicitly unknown");
        assertEquals(Files.readString(main), Files.readString(output.resolve(evidence.conversations().getFirst().path())));
        assertFalse(Files.exists(output.resolve("auth.json")));
        assertEquals("0.162.1", evidence.conversations().getFirst().cliVersion());
        assertEquals(List.of("fixture-model"), evidence.conversations().getFirst().models());
    }
    @Test void repairsAndRecoveryDoNotCountEarlierResponsesAgainAndArchivesStayImmutable() throws Exception {
        String root = id(); Path first = temp.resolve("first"), repair = temp.resolve("repair");
        CodexEvidenceUtil.begin(home(), checkout(), first, null); Path main = session(root, null);
        response(main, "a", 10, 4); var original = collect(first, root);
        String archived = Files.readString(first.resolve(original.conversations().getFirst().path()));
        CodexEvidenceUtil.begin(home(), checkout(), repair, root);
        response(main, "a", 10, 4); response(main, "b", 20, 6);
        var result = collect(repair, root); assertEquals(26L, result.usage().tokens().totalTokens());
        assertEquals(result, collect(repair, root)); assertEquals(original, collect(first, root));
        assertEquals(archived, Files.readString(first.resolve(original.conversations().getFirst().path())));
        var campaign = new AutoLlmCampaign(); campaign.id = root;
        var trial = new AutoLlmCampaign.Trial(); trial.number = 1; campaign.trials.add(trial);
        for (Path directory : List.of(first, repair)) {
            var attempt = new AutoLlmCampaign.Attempt(); attempt.stage = "GENERATION"; attempt.status = "INTERRUPTED"; attempt.directory = temp.relativize(directory).toString(); trial.attempts.add(attempt);
        }
        CodexEvidenceUtil.report(campaign, temp); assertEquals(40L, campaign.codexUsage.tokens().totalTokens());
        CodexEvidenceUtil.report(campaign, temp); assertEquals(40L, campaign.codexUsage.tokens().totalTokens());
    }
    @Test void cumulativeCountersUseValidatedDeltasAndCliUsageIsOnlySupplemental() {
        String root = id(); Path main = session(root, null); counter(main, 100, 10);
        Path repair = temp.resolve("repair"); CodexEvidenceUtil.begin(home(), checkout(), repair, root);
        counter(main, 130, 20); counter(main, 130, 20);
        AutoLlmFileUtil.append(repair.resolve("stdout.log"), Map.of("type", "turn.completed", "usage", counts(130, 20)));
        var evidence = collect(repair, root);
        assertEquals(30L, evidence.usage().tokens().inputTokens()); assertEquals(10L, evidence.usage().tokens().outputTokens());
        var usage = AutoLlmFileUtil.read(repair.resolve("usage.json"), CodexEvidenceUtil.Usage.class);
        assertEquals(1, usage.cliReportedUsage().size()); assertEquals("COMPLETE", usage.summary().status());
    }
    @Test void decreasedCountersAreUnavailableRatherThanNegativeOrInvented() {
        String root = id(); Path main = session(root, null); counter(main, 100, 10);
        Path repair = temp.resolve("repair"); CodexEvidenceUtil.begin(home(), checkout(), repair, root); counter(main, 50, 5);
        var result = collect(repair, root); assertEquals("ARCHIVED", result.status()); assertEquals("UNAVAILABLE", result.usage().status()); assertNull(result.usage().tokens().inputTokens());
    }
    @Test void truncatedAndMalformedNativeRecordsArePreservedAndUsageIsPartial() throws Exception {
        String root = id(); Path output = temp.resolve("attempt"); CodexEvidenceUtil.begin(home(), checkout(), output, null);
        Path main = session(root, null); response(main, "a", 10, 4);
        Files.writeString(main, "{malformed}\n", StandardOpenOption.APPEND); response(main, "b", 20, 6); Files.writeString(main, "{\"partial", StandardOpenOption.APPEND);
        var result = collect(output, root); assertEquals("PARTIAL", result.usage().status()); assertEquals(40L, result.usage().tokens().totalTokens());
        assertEquals(Files.readString(main), Files.readString(output.resolve(result.conversations().getFirst().path())));
    }
    @Test void absentOptionalTokenFieldsStayUnknown() {
        String root = id(); Path output = temp.resolve("attempt"); CodexEvidenceUtil.begin(home(), checkout(), output, null);
        Path main = session(root, null);
        AutoLlmFileUtil.append(main, Map.of("type", "token_usage_record", "payload", Map.of("response_id", "a", "usage", Map.of("input_tokens", 10, "output_tokens", 3))));
        var result = collect(output, root); assertEquals("PARTIAL", result.usage().status()); assertNull(result.usage().tokens().cachedInputTokens()); assertNull(result.usage().tokens().totalTokens());
    }
    @Test void missingExactSessionAndWrongCheckoutStopArchival() {
        String root = id(); session(id(), null);
        assertThrows(CodexEvidenceUtil.ArchiveException.class, () -> collect(temp.resolve("missing"), root));
        Path file = session(root, null);
        assertThrows(CodexEvidenceUtil.ArchiveException.class, () -> CodexEvidenceUtil.collect(home(), temp.resolve("other"), temp.resolve("wrong"), root));
        assertTrue(Files.exists(file));
    }
    @Test void corruptedCompletedArchiveFailsVerification() throws Exception {
        String root = id(); Path output = temp.resolve("attempt"); CodexEvidenceUtil.begin(home(), checkout(), output, null); session(root, null);
        var result = collect(output, root); Files.writeString(output.resolve(result.conversations().getFirst().path()), "changed");
        assertThrows(CodexEvidenceUtil.ArchiveException.class, () -> collect(output, root));
    }
    @Test void olderCampaignsRemainReadableWithUnavailableUsage() {
        var campaign = AutoLlmFileUtil.JSON.readValue("{\"schemaVersion\":1,\"id\":\"legacy\",\"trials\":[{\"number\":1,\"attempts\":[{\"stage\":\"GENERATION\",\"directory\":\"old\",\"status\":\"FAILED\"}]}]}", AutoLlmCampaign.class);
        CodexEvidenceUtil.report(campaign, temp); assertEquals("UNAVAILABLE", campaign.codexUsage.status()); assertNull(campaign.codexUsage.tokens().inputTokens()); assertNull(campaign.trials.getFirst().attempts.getFirst().codexEvidence);
    }
    @Test void recoveryWithoutPreAttemptBaselineArchivesButDoesNotInventTokenDeltas() {
        String root = id(); Path main = session(root, null); response(main, "a", 10, 4);
        var result = collect(temp.resolve("old"), root); assertEquals("ARCHIVED", result.status()); assertEquals("UNAVAILABLE", result.usage().status());
    }
    @Test void recoveryUsesSavedCodexHomeAndFindsArchivedSessions() throws Exception {
        String root = id(); Path output = temp.resolve("attempt"); CodexEvidenceUtil.begin(home(), checkout(), output, null);
        Path main = session(root, null); response(main, "a", 10, 4);
        Files.createDirectories(home().resolve("archived_sessions")); Files.move(main, home().resolve("archived_sessions/rollout-" + root + ".jsonl"));
        var result = CodexEvidenceUtil.collect(temp.resolve("different-home"), checkout(), output, root);
        assertEquals("ARCHIVED", result.status()); assertEquals(14L, result.usage().tokens().totalTokens());
    }
    @Test void ambiguityInUnrelatedSessionsIsIgnoredButSelectedSessionMustBeUnique() throws Exception {
        String unrelated = id(), root = id(); Path main = session(root, null), other = session(unrelated, null);
        Files.copy(other, other.resolveSibling("duplicate.jsonl"));
        assertEquals("ARCHIVED", collect(temp.resolve("valid"), root).status());
        Files.copy(main, main.resolveSibling("duplicate-root.jsonl"));
        assertThrows(CodexEvidenceUtil.ArchiveException.class, () -> collect(temp.resolve("ambiguous"), root));
    }
    @Test void newlyPersistedChildDoesNotRecountResponsesAlreadyPresentInParentBaseline() {
        String root = id(), child = id(); Path main = session(root, null); response(main, "old", 10, 4);
        Path output = temp.resolve("repair"); CodexEvidenceUtil.begin(home(), checkout(), output, root);
        Path childPath = session(child, root); response(childPath, "old", 10, 4); response(childPath, "new", 20, 6);
        var result = collect(output, root); assertEquals(26L, result.usage().tokens().totalTokens());
    }
    @Test void cumulativeFallbackStillWorksWhenEarlierPerResponseRecordsExist() {
        String root = id(); Path main = session(root, null); response(main, "old", 100, 10); counter(main, 100, 10);
        Path output = temp.resolve("repair"); CodexEvidenceUtil.begin(home(), checkout(), output, root); counter(main, 130, 20);
        assertEquals(40L, collect(output, root).usage().tokens().totalTokens());
    }
    @Test void missingConversationBeforeRepairIsAnInfrastructureFailureWithArtifacts() {
        Path output = temp.resolve("repair");
        assertThrows(CodexEvidenceUtil.ArchiveException.class, () -> CodexEvidenceUtil.begin(home(), checkout(), output, id()));
        assertEquals("FAILED", AutoLlmFileUtil.read(output.resolve("evidence.json"), CodexEvidenceUtil.Evidence.class).status());
    }
    @Test void interruptedCliRecordsDoNotPreventNativeArchivalAndWrongThreadIsRejected() throws Exception {
        String root = id(); Path output = temp.resolve("attempt"); CodexEvidenceUtil.begin(home(), checkout(), output, null); session(root, null);
        Files.writeString(output.resolve("stdout.log"), "{\"type\":\"thread.started\",\"thread_id\":\"" + root + "\"}\n{truncated");
        assertEquals("ARCHIVED", collect(output, root).status());
        Path wrong = temp.resolve("wrong-thread"); Files.createDirectories(wrong);
        Files.writeString(wrong.resolve("stdout.log"), "{\"type\":\"thread.started\",\"thread_id\":\"" + root + "\"}\n");
        assertThrows(CodexEvidenceUtil.ArchiveException.class, () -> collect(wrong, id()));
    }
}
