package es.urjc.etsii.morktests;

import es.urjc.etsii.grafo.autoconfig.autollm.*;
import org.junit.jupiter.api.Test;
import es.urjc.etsii.grafo.autoconfig.screening.*;
import es.urjc.etsii.grafo.config.*;
import org.junit.jupiter.api.io.TempDir;
import java.io.File;
import java.net.URI;
import java.net.http.*;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

class AutoLlmIntegrationTest {
    @TempDir(cleanup = org.junit.jupiter.api.io.CleanupMode.ON_SUCCESS) Path temp;
    @Test void realMorkAndIraceCompleteCampaignThroughHttpApi() throws Exception { campaign(false); }
    @Test void remoteScreeningAndMorkCampaignCompleteThroughSsh() throws Exception { campaign(true); }
    private void campaign(boolean remote) throws Exception {
        Path repo = temp.resolve("baseline"); Files.createDirectories(repo.resolve("src/main/java"));
        Files.writeString(repo.resolve("src/main/java/Original.java"), "public class Original {}\n");
        Files.writeString(repo.resolve("instance.txt"), "integration instance\n");
        Files.writeString(repo.resolve("instance-second.txt"), "another integration instance\n");
        Files.writeString(repo.resolve("training.index"), repo.resolve("instance.txt") + "\n" + repo.resolve("instance-second.txt") + "\n");
        Files.writeString(repo.resolve("sota.json"), "{\"$component\":\"SimpleAlgorithm\",\"constructive\":{\"$component\":\"FasterInvertedConstructive\",\"sumThis\":-10.0},\"improver\":{\"$component\":\"IncrementImprover\"}}");
        git(repo, "init"); git(repo, "add", "."); git(repo, "-c", "user.name=Test", "-c", "user.email=test@example.test", "commit", "-m", "baseline");
        String java = Path.of(System.getProperty("java.home"), "bin/java").toString();
        var classpath = new ArrayList<String>(); for (String entry : System.getProperty("java.class.path").split(File.pathSeparator)) classpath.add(Path.of(entry).toAbsolutePath().toString());
        String cp = String.join(File.pathSeparator, classpath);
        try (var ssh = remote ? new SshIntegrationUtil(temp.resolve("ssh")) : null;
             var second = remote ? new SshIntegrationUtil(temp.resolve("ssh-second")) : null) {
        Path codex = temp.resolve("codex-fixture");
        Files.writeString(codex, "#!/bin/sh\nexec '" + java.replace("'", "'\\''") + "' -cp '" + cp.replace("'", "'\\''") + "' " + AutoLlmFixture.class.getName() + " \"$@\"\n"); assertTrue(codex.toFile().setExecutable(true));
        Path workspace = temp.resolve("campaigns"), parentExecution = temp.resolve("parent/execution");
        var command = List.of(java, "-Dmork.execution.directory=" + parentExecution, "-cp", cp, "es.urjc.etsii.grafo.autoconfigtests.Main", "--autollm", "--server.port=0", "--server.address=0.0.0.0", "--whitelist=ACITestWhitelist", "--solver.benchmark=false", "--instances.preload=false", "--instances.path.default=" + repo.resolve("training.index"), "--autollm.workspace=" + workspace);
        var parentBuilder = new ProcessBuilder(command).directory(temp.toFile()).redirectErrorStream(true).redirectOutput(temp.resolve("parent.log").toFile());
        parentBuilder.environment().put("CODEX_HOME", temp.resolve("codex-home").toString());
        var parent = parentBuilder.start();
        try {
            long deadline = System.currentTimeMillis() + (remote ? 420_000 : 150_000);
            while (!Files.exists(parentExecution.resolve("readiness.json")) && parent.isAlive() && System.currentTimeMillis() < deadline) Thread.sleep(100);
            assertTrue(Files.exists(parentExecution.resolve("readiness.json")), () -> read(temp.resolve("parent.log")));
            int port = AutoLlmFileUtil.JSON.readTree(Files.readString(parentExecution.resolve("readiness.json"))).path("port").intValue();
            String base = "http://127.0.0.1:" + port + "/api/autollm";
            var client = HttpClient.newHttpClient();
            // The 32-worker fixture can oversubscribe a development host; allow its first objective before scoring.
            var settings = new AutoLlmConfig(new AutoLlmConfig.Project(repo.toString(), "HEAD", List.of(java, "-cp", cp, AutoLlmFixture.class.getName(), "build"), "target/project.jar", List.of("--whitelist=ACITestWhitelist", "--solver.benchmark=false")), workspace.toString(), remote ? 2 : 1, 1234L, 299,
                    "sota.json", null, "training.index", "training.index", new AutoLlmConfig.Codex(codex.toString(), null, null, null, null, null), new AutoLlmConfig.Preparation(30_000L, 0), new AutoLlmConfig.Tuning(300, remote ? 1000L : 50L, remote ? 100L : 50L, false), new AutoLlmConfig.Comparison(2, 50L), new AutoLlmConfig.Execution(remote ? 32 : 1, 5000L, 30_000L, 30_000L, 30_000L, 30_000L, 30_000L, 180_000L, "256m", remote ? AutoLlmConfig.Target.SSH : AutoLlmConfig.Target.LOCAL, ssh == null ? null : ssh.ssh));
            if (remote) {
                var firstServer = ssh.ssh.screeningServer(); var next = second.ssh.screeningServer();
                var pool = new AutoLlmConfig.Ssh("server", ssh.ssh.runnerArtifact(), List.of(firstServer,
                        new AutoLlmConfig.Server("second", next.host(), next.workspace(), next.javaExecutable(), next.environment(), next.configFile(), 8, "256m")));
                settings = new AutoLlmConfig(settings.project(), settings.workspace(), settings.trials(), settings.seed(), settings.maxParameters(), settings.sotaFile(), settings.initialConfigurationsFile(), settings.trainingPath(), settings.comparisonPath(), settings.codex(), settings.preparation(), settings.tuning(), settings.comparison(),
                        new AutoLlmConfig.Execution(32, 5000L, 30_000L, 30_000L, 30_000L, 30_000L, 30_000L, 180_000L, "256m", AutoLlmConfig.Target.SSH, pool));
            }
            var capabilities = get(client, base + "/capabilities");
            assertEquals(3, capabilities.path("apiVersion").asInt()); assertEquals(3, capabilities.path("checkpointVersion").asInt());
            assertTrue(Files.exists(workspace.resolve("session.json")));
            String python = remote ? ssh.ssh.screeningServer().environment().get("MORK_SELECTOR_PYTHON") : Path.of("../example-tsptw/venv/bin/python3").toAbsolutePath().normalize().toString();
            var screening = new InstanceScreeningConfig(repo.resolve("sota.json").toString(), null, 0.000001, 3.0, 1000L, 1, 0.95, python, 1, 1000L, null);
            var screeningRequest = new ScreeningJobService.Request(settings, screening, repo.resolve("training.index").toString(), 0.15);
            String screeningId = request(client, base + "/screenings", AutoLlmFileUtil.JSON.writeValueAsString(screeningRequest)).path("id").asString();
            assertEquals("DRAFT", get(client, base + "/screenings/" + screeningId).path("status").asString());
            request(client, base + "/screenings/" + screeningId + "/resume", "{}");
            tools.jackson.databind.JsonNode selection;
            do { Thread.sleep(200); selection = get(client, base + "/screenings/" + screeningId); }
            while (List.of("RUNNING", "WAITING_FOR_REMOTE", "CANCELLATION_PENDING").contains(selection.path("status").asString()) && System.currentTimeMillis() < deadline);
            assertEquals("AWAITING_APPROVAL", selection.path("status").asString(), selection.toString());
            assertEquals(2, selection.path("progress").path("initialTotal").asInt());
            assertEquals(2, selection.path("progress").path("initialCompleted").asInt());
            Path mirror = Path.of(selection.path("mirror").asString());
            assertFalse(Files.exists(mirror.resolve("proposal/approved.json")), "Preparation must not approve automatically");
            var review = get(client, base + "/screenings/" + screeningId + "/review");
            assertEquals(1, review.path("proposal").path("trainingCount").asInt()); assertEquals(2, review.path("proposal").path("testingCount").asInt());
            String approvalUrl = base + "/screenings/" + screeningId + "/approve";
            var stale = client.send(HttpRequest.newBuilder(URI.create(approvalUrl)).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString("{\"proposalSha256\":\"stale\"}")).build(), HttpResponse.BodyHandlers.ofString());
            assertEquals(400, stale.statusCode()); assertFalse(Files.exists(mirror.resolve("proposal/approved.json")));
            request(client, approvalUrl, AutoLlmFileUtil.JSON.writeValueAsString(Map.of("proposalSha256", review.path("proposalSha256").asString())));
            do { Thread.sleep(200); selection = get(client, base + "/screenings/" + screeningId); }
            while (List.of("RUNNING", "WAITING_FOR_REMOTE", "CANCELLATION_PENDING").contains(selection.path("status").asString()) && System.currentTimeMillis() < deadline);
            assertEquals("APPROVED", selection.path("status").asString(), selection.toString());
            while (get(client, base + "/screenings").path("active").asString().equals(screeningId) && System.currentTimeMillis() < deadline) Thread.sleep(100);
            settings = settings.withPaths(mirror.resolve("proposal/train.index").toString(), mirror.resolve("proposal/test.index").toString());
            String targetId = remote ? "server" : "local";
            var created = request(client, base + "/campaigns", AutoLlmFileUtil.JSON.writeValueAsString(settings));
            String id = created.path("id").stringValue();
            var metadata = request(client, base + "/campaigns/" + id + "/baseline-check", "{}"); assertTrue(metadata.path("withinLimit").asBoolean());
            var draft = AutoLlmFileUtil.read(workspace.resolve(id).resolve("campaign.json"), AutoLlmCampaign.class); assertEquals("DRAFT", draft.status);
            assertNotEquals("HEAD", draft.config.project().ref());
            assertTrue(Files.exists(workspace.resolve(id).resolve("targets/" + targetId + "/baseline-preflight/reproducibility/verified.json")));
            var summary = get(client, base + "/campaigns/" + id + "/summary").path("targets").path(targetId); assertTrue(summary.path("comparisonLimitsResolved").asBoolean());
            assertEquals(50, summary.path("comparisonLimitMillis").get(0).asInt());
            request(client, base + "/campaigns/" + id + "/start", "{}");
            AutoLlmCampaign campaign;
            do {
                Thread.sleep(200);
                campaign = AutoLlmFileUtil.read(workspace.resolve(id).resolve("campaign.json"), AutoLlmCampaign.class);
            } while (List.of("RUNNING", "WAITING_FOR_REMOTE", "CANCELLATION_PENDING").contains(campaign.status) && parent.isAlive() && System.currentTimeMillis() < deadline);
            tools.jackson.databind.JsonNode campaigns;
            do {
                campaigns = AutoLlmFileUtil.JSON.readTree(client.send(HttpRequest.newBuilder(URI.create(base + "/campaigns")).GET().build(), HttpResponse.BodyHandlers.ofString()).body());
                if (!id.equals(campaigns.path("active").stringValue())) break;
                Thread.sleep(100);
            } while (parent.isAlive() && System.currentTimeMillis() < deadline);
            assertFalse(id.equals(campaigns.path("active").stringValue()), "Campaign must release the workspace after reporting");
            assertEquals("SUCCEEDED", campaign.status, campaign.failure + "\n" + campaign.trials.getFirst().failure + "\n" + read(temp.resolve("parent.log")));
            var trial = campaign.trials.getFirst(); targetId = trial.targetId; Path root = workspace.resolve(id);
            assertEquals("SUCCEEDED", trial.status, trial.failure);
            assertTrue(Files.exists(root.resolve(trial.preparedArtifact)));
            try (var zip = new java.util.zip.ZipFile(root.resolve("targets/" + targetId + "/trials/001/frozen/source.zip").toFile())) {
                assertNotNull(zip.getEntry("src/main/java/es/urjc/etsii/grafo/autoconfigtests/components/FasterInvertedConstructiveNew.java"));
            }
            assertTrue(Files.exists(root.resolve("reports/targets/" + targetId + "/campaign.xlsx")), () -> read(root.resolve("campaign-events.jsonl")));
            int measured = 0;
            for (var event : AutoLlmFileUtil.events(root.resolve(trial.comparisonDirectory).resolve("execution/events.jsonl"))) if ("evaluation-result".equals(event.get("kind")) && Boolean.TRUE.equals(event.get("measured"))) { measured++; assertEquals("SUCCEEDED", event.get("state")); }
            assertEquals(8, measured);
            assertTrue(read(root.resolve(trial.mainDirectory).resolve("runner.R")).contains("if (\"false\" == \"true\")"));
            assertFalse(Files.exists(root.resolve(trial.mainDirectory).resolve("log-ablation.Rdata")));
            assertTrue(read(root.resolve("targets/" + targetId + "/trials/001/frozen/search-space.json")).contains("FasterInvertedConstructiveNew"));
            assertEquals(50, AutoLlmFileUtil.JSON.readTree(read(root.resolve(trial.comparisonDirectory).resolve("execution/time-limits.json"))).properties().iterator().next().getValue().longValue());
            assertFalse(Files.exists(repo.resolve("src/main/java/es/urjc/etsii/grafo/autoconfigtests/components/FasterInvertedConstructiveNew.java")));
            var regenerated = request(client, base + "/campaigns/" + id + "/report", "{}"); assertTrue(regenerated.path("reportFailure").isNull());
            assertEquals("COMPLETE", regenerated.path("codexUsage").path("status").stringValue());
            assertEquals(remote ? 40 : 20, regenerated.path("codexUsage").path("tokens").path("totalTokens").longValue());
            var evidence = trial.attempts.getFirst().codexEvidence;
            var progress = get(client, base + "/campaigns/" + id + "/trials/1/progress");
            assertEquals("SUCCEEDED", progress.path("status").asString());
            var activity = get(client, base + "/campaigns/" + id + "/trials/1/activity?attempt=1&cursor=0");
            assertTrue(activity.path("text").asString().contains("thread.started")); assertTrue(activity.path("nextCursor").asLong() > 0);
            assertEquals("ARCHIVED", evidence.status()); assertEquals("fixture-model", evidence.conversations().getFirst().models().getFirst());
            String archive = trial.attempts.getFirst().directory + "/" + evidence.conversations().getFirst().path();
            var downloaded = client.send(HttpRequest.newBuilder(URI.create(base + "/campaigns/" + id + "/artifact?path=" + archive)).GET().build(), HttpResponse.BodyHandlers.ofString());
            assertEquals(200, downloaded.statusCode()); assertEquals(read(root.resolve(archive)), downloaded.body());
            assertTrue(Files.exists(root.resolve("reports/llm-usage.json"))); assertTrue(Files.exists(root.resolve("targets/" + targetId + "/trials/001/llm-usage.json")));
            if (remote) {
                assertEquals(2, campaign.targets.size()); assertEquals(2, campaign.trials.size());
                var assignedTargets = new HashSet<String>();
                for (var completed : campaign.trials) {
                    assignedTargets.add(completed.targetId); assertEquals("SUCCEEDED", completed.status, completed.failure);
                    assertTrue(Files.exists(root.resolve("reports/targets/" + completed.targetId + "/campaign.xlsx")));
                    assertTrue(completed.comparisonDirectory.startsWith("targets/" + completed.targetId + "/"));
                    var descriptor = AutoLlmFileUtil.JSON.readTree(read(root.resolve(completed.comparisonDirectory).resolve("remote-job.json")));
                    assertTrue(descriptor.path("root").asString().endsWith("/campaigns/" + id + "/targets/" + completed.targetId));
                }
                var generationIntervals = new ArrayList<AutoLlmCampaign.Attempt>();
                for (var completed : campaign.trials) generationIntervals.add(completed.attempts.getFirst());
                generationIntervals.sort(Comparator.comparing(value -> value.startedAt));
                assertFalse(Instant.parse(generationIntervals.get(1).startedAt).isBefore(Instant.parse(generationIntervals.get(0).finishedAt)), "Default Codex quota must serialize generation");
                assertEquals(Set.of("server", "second"), assignedTargets); assertFalse(Files.exists(root.resolve("reports/campaign.xlsx")));
                assertEquals(32, campaign.targets.get("server").host.effectiveWorkers());
                assertEquals(8, campaign.targets.get("second").host.effectiveWorkers());
                try (var files = Files.walk(root.resolve("targets/" + targetId + "/trials/001/metadata"))) { assertTrue(files.anyMatch(file -> file.getFileName().toString().equals("check.json"))); }
                assertTrue(Files.exists(root.resolve("targets/" + targetId + "/baseline-preflight/build/adopted-screening.json")));
            }
        } finally { parent.destroy(); if (!parent.waitFor(5, TimeUnit.SECONDS)) parent.destroyForcibly(); new AutoLlmProcessRunner().reconcile(workspace); }
        }
    }
    private tools.jackson.databind.JsonNode get(HttpClient client, String url) throws Exception {
        var response = client.send(HttpRequest.newBuilder(URI.create(url)).GET().build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), response.body()); return AutoLlmFileUtil.JSON.readTree(response.body());
    }
    private tools.jackson.databind.JsonNode request(HttpClient client, String url, String json) throws Exception {
        var response = client.send(HttpRequest.newBuilder(URI.create(url)).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(json)).build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), response.body()); return AutoLlmFileUtil.JSON.readTree(response.body());
    }
    private static String read(Path file) { try { return Files.readString(file); } catch (Exception e) { return file + ": " + e; } }
    private void git(Path repo, String... args) throws Exception { var command = new ArrayList<>(List.of("git")); command.addAll(List.of(args)); var process = new ProcessBuilder(command).directory(repo.toFile()).redirectErrorStream(true).start(); String output = new String(process.getInputStream().readAllBytes()); assertEquals(0, process.waitFor(), output); }
}
