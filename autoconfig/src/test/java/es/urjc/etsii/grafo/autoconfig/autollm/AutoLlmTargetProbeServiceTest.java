package es.urjc.etsii.grafo.autoconfig.autollm;

import es.urjc.etsii.grafo.autoconfig.autollm.execution.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AutoLlmTargetProbeServiceTest {
    @TempDir Path temp;
    private AutoLlmConfig pool() throws Exception {
        Path runner = temp.resolve("runner.jar"); Files.writeString(runner, "fixture");
        var servers = new ArrayList<AutoLlmConfig.Server>();
        for (int i = 0; i < 7; i++) servers.add(new AutoLlmConfig.Server("s" + i, "host" + i, "/work", "java", Map.of(), null, null, null));
        return new AutoLlmConfig(null, temp.toString(), null, null, null, null, null, null, null, null, null, null, null,
                new AutoLlmConfig.Execution(null, null, null, null, null, null, null, null, null, AutoLlmConfig.Target.SSH, new AutoLlmConfig.Ssh("s0", runner.toString(), servers)));
    }
    private AutoLlmTargetProbeService.Probe finished(AutoLlmTargetProbeService service, String id) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        var probe = service.load(id);
        while (probe.status() == AutoLlmTargetProbeService.ProbeStatus.RUNNING && System.nanoTime() < deadline) { Thread.sleep(10); probe = service.load(id); }
        assertNotEquals("RUNNING", probe.status().name()); return probe;
    }
    @Test void returnsAnIdBeforeChecksCompleteAndBoundsParallelismWhileRetainingFailures() throws Exception {
        var config = pool(); var store = new AutoLlmCampaignStore(config);
        var entered = new CountDownLatch(4); var finish = new CountDownLatch(1);
        var running = new AtomicInteger(); var maximum = new AtomicInteger(); var pythonTargets = ConcurrentHashMap.<String>newKeySet();
        try (var service = new AutoLlmTargetProbeService(store, mock(AutoLlmProcessRunner.class)) {
            @Override protected WorkloadProtocol.Host inspect(AutoLlmConfig c, Path root, String python) {
                int count = running.incrementAndGet(); maximum.accumulateAndGet(count, Math::max); entered.countDown();
                String id = c.execution().ssh().onlyServer().id(); if (python != null) pythonTargets.add(id);
                try {
                    if (!finish.await(10, TimeUnit.SECONDS)) throw new IllegalStateException("test barrier timed out");
                    if (id.equals("s2")) throw new IllegalStateException("offline");
                    return new WorkloadProtocol.Host(WorkloadProtocol.VERSION, Map.of("host", id), 1, 1, 1, 1, 1, List.of());
                } catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new CancellationException(); }
                finally { running.decrementAndGet(); }
            }
        }) {
            try {
                var accepted = service.start(config); assertEquals("RUNNING", accepted.status().name()); assertEquals(7, accepted.targets().size());
                assertTrue(Files.exists(store.root().resolve("target-probes/" + accepted.id() + "/probe.json")));
                assertTrue(entered.await(5, TimeUnit.SECONDS)); assertEquals(4, maximum.get());
                finish.countDown(); var result = finished(service, accepted.id());
                assertEquals("COMPLETED_WITH_FAILURES", result.status().name()); assertEquals("FAILED", result.targets().get("s2").status().name());
                for (var entry : result.targets().entrySet()) if (!entry.getKey().equals("s2")) assertEquals("SUCCEEDED", entry.getValue().status().name());
                assertEquals(Set.of("s0"), pythonTargets); assertEquals(4, maximum.get());
                assertEquals(result, service.load(accepted.id()));
            } finally { finish.countDown(); }
        }
    }
    @Test void shutdownSealsQueuedAndRunningTargetsAsInterrupted() throws Exception {
        var config = pool(); var store = new AutoLlmCampaignStore(config); var entered = new CountDownLatch(1);
        var service = new AutoLlmTargetProbeService(store, mock(AutoLlmProcessRunner.class)) {
            @Override protected WorkloadProtocol.Host inspect(AutoLlmConfig c, Path root, String python) {
                entered.countDown();
                try { new CountDownLatch(1).await(); throw new AssertionError(); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new CancellationException(); }
            }
        };
        var accepted = service.start(config);
        try { assertTrue(entered.await(5, TimeUnit.SECONDS)); } finally { service.close(); }
        try (var restarted = new AutoLlmTargetProbeService(store, mock(AutoLlmProcessRunner.class))) {
            var result = restarted.load(accepted.id()); assertEquals("INTERRUPTED", result.status().name());
            for (var target : result.targets().values()) assertEquals("INTERRUPTED", target.status().name());
        }
    }
}
