package es.urjc.etsii.grafo.autoconfig.screening;

import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.autoconfig.autollm.AutoLlmFileUtil;
import es.urjc.etsii.grafo.autoconfig.builder.AlgorithmBuilderService;
import es.urjc.etsii.grafo.create.builder.SolutionBuilder;
import es.urjc.etsii.grafo.io.InstanceManager;
import es.urjc.etsii.grafo.metrics.TimeValue;
import es.urjc.etsii.grafo.solution.SolutionValidator;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestSolution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static es.urjc.etsii.grafo.autoconfig.screening.ScreeningData.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ScreeningWorkerTest {
    @TempDir Path output;
    private Job job;
    private ScreeningWorker<TestSolution, TestInstance> worker;
    private InstanceManager<TestInstance> instances;
    private final List<Measurement> measurements = new ArrayList<>();
    private record Measurement(String input, long seed, long millis) {}

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setup() throws Exception {
        Path sota = output.resolve("sota.json");
        Files.writeString(sota, "{}");
        var inputs = new ArrayList<Input>();
        for (String name : List.of("fast", "slow")) {
            Path path = output.resolve(name);
            Files.writeString(path, name);
            inputs.add(new Input(path.toString(), ScreeningFileUtil.inputHash(path.toString())));
        }
        var config = new InstanceScreeningConfig(sota.toString(), output.toString(), null, null, null,
                2, null, null, null, null, null);
        var manifest = new Manifest(2, "fingerprint", config, 1234, "Xoroshiro128PlusPlus", .15,
                "Cost", FMode.MINIMIZE, Map.of(), inputs, AutoLlmFileUtil.hash(sota), "jar", List.of(), "QUALIFIED_SERVER_POOL");
        job = new Job(manifest, output.toString());
        instances = mock(InstanceManager.class);
        when(instances.getInstance(anyString())).thenAnswer(call -> new TestInstance(call.getArgument(0)));
        worker = spy(new ScreeningWorker<>(instances, mock(AlgorithmBuilderService.class),
                (SolutionBuilder<TestSolution, TestInstance>) mock(SolutionBuilder.class),
                (SolutionValidator<TestSolution, TestInstance>) mock(SolutionValidator.class)));
        doAnswer(call -> {
            Input input = call.getArgument(1);
            long seed = call.getArgument(4), millis = call.getArgument(5);
            measurements.add(new Measurement(input.path(), seed, millis));
            return result(input, seed);
        }).when(worker).measure(any(Manifest.class), any(Input.class), any(TestInstance.class), eq("{}"), anyLong(), anyLong());
    }

    private Run result(Input input, long seed) {
        long ttb = input.equals(job.manifest().inputs().getFirst()) ? 1_000_000 : 40_000_000;
        return new Run(job.manifest().fingerprint(), input.path(), seed, true, 10.0,
                job.manifest().settings().horizonNanos(), List.of(new TimeValue(ttb, 10)), null);
    }

    private void useThirtyConfirmations() {
        var previous = job.manifest();
        var settings = new InstanceScreeningConfig(previous.settings().sotaFile(), output.toString(), null, null, null,
                30, null, null, null, null, null);
        job = new Job(new Manifest(previous.version(), previous.fingerprint(), settings, previous.seed(), previous.randomType(),
                previous.trainingFraction(), previous.objective(), previous.mode(), previous.host(), previous.inputs(),
                previous.sotaSha256(), previous.artifactSha256(), previous.runArguments(), previous.reusePolicy()), output.toString());
    }

    private Run confirmation(Input input, long seed, double objective, long ttb) {
        return new Run(job.manifest().fingerprint(), input.path(), seed, true, objective,
                job.manifest().settings().horizonNanos(), List.of(new TimeValue(ttb, objective)), null);
    }

    @Test void twoMissesSkipRemainingConfirmationsAndContinueWithTheNextCandidate() throws Exception {
        useThirtyConfirmations();
        doAnswer(call -> {
            Input input = call.getArgument(1);
            long seed = call.getArgument(4);
            measurements.add(new Measurement(input.path(), seed, call.getArgument(5)));
            long ttb = seed > 1234 && input.equals(job.manifest().inputs().getFirst()) ? 20_000_000 : 1_000_000;
            return confirmation(input, seed, 10, ttb);
        }).when(worker).measure(any(Manifest.class), any(Input.class), any(TestInstance.class), eq("{}"), anyLong(), anyLong());
        worker.screen(job);
        Input first = job.manifest().inputs().getFirst(), second = job.manifest().inputs().getLast();
        var stopped = ScreeningFileUtil.readConfirmations(output, job.manifest(), first);
        assertEquals(2, stopped.size());
        assertNull(ScreeningFileUtil.readRun(output, job.manifest(), first, 1237));
        assertEquals("RETAINED", ScreeningUtil.classify(result(first, 1234), stopped, job.manifest().settings(), FMode.MINIMIZE).classification());
        assertEquals(30, ScreeningFileUtil.readConfirmations(output, job.manifest(), second).size());
        assertNotNull(ScreeningFileUtil.readRun(output, job.manifest(), second, 1264));
        var progress = AutoLlmFileUtil.JSON.readTree(Files.readString(output.resolve("screening-progress.json")));
        assertEquals(32, progress.path("confirmationsCompleted").asInt());
        assertEquals(32, progress.path("confirmationsTotal").asInt());
        for (Measurement measurement : measurements) assertEquals(1_000, measurement.millis());
    }

    @Test void oneMissStillRunsAllConfirmationsAndCanExcludeTheInstance() throws Exception {
        useThirtyConfirmations();
        Input input = job.manifest().inputs().getFirst();
        ScreeningFileUtil.writeRun(output, confirmation(input, 1235, 10, 20_000_000));
        worker.screen(job);
        var runs = ScreeningFileUtil.readConfirmations(output, job.manifest(), input);
        assertEquals(30, runs.size());
        assertEquals("TRIVIAL", ScreeningUtil.classify(result(input, 1234), runs, job.manifest().settings(), FMode.MINIMIZE).classification());
    }

    @Test void aBetterTargetMakesEarlierFastRunsMissAndStopsNewMeasurements() throws Exception {
        useThirtyConfirmations();
        Input input = job.manifest().inputs().getFirst();
        doReturn(confirmation(input, 1236, 9, 20_000_000)).when(worker)
                .measure(any(Manifest.class), eq(input), any(TestInstance.class), eq("{}"), eq(1236L), anyLong());
        worker.screen(job);
        var runs = ScreeningFileUtil.readConfirmations(output, job.manifest(), input);
        assertEquals(2, runs.size());
        assertEquals(10, runs.getFirst().objective());
        assertEquals(9, runs.getLast().objective());
        assertNull(ScreeningFileUtil.readRun(output, job.manifest(), input, 1237));
    }

    @Test void sequence30Then30Then29StopsBeforeTheFourthConfirmation() throws Exception {
        useThirtyConfirmations();
        Input input = job.manifest().inputs().getFirst();
        ScreeningFileUtil.writeRun(output, confirmation(input, 1234, 30, 1_000_000));
        ScreeningFileUtil.writeRun(output, confirmation(input, 1235, 30, 1_000_000));
        ScreeningFileUtil.writeRun(output, confirmation(input, 1236, 30, 1_000_000));
        doReturn(confirmation(input, 1237, 29, 1_000_000)).when(worker)
                .measure(any(Manifest.class), eq(input), any(TestInstance.class), eq("{}"), eq(1237L), anyLong());
        worker.screen(job);
        var runs = ScreeningFileUtil.readConfirmations(output, job.manifest(), input);
        assertEquals(3, runs.size());
        assertEquals(List.of(30.0, 30.0, 29.0), List.of(runs.get(0).objective(), runs.get(1).objective(), runs.get(2).objective()));
        assertNull(ScreeningFileUtil.readRun(output, job.manifest(), input, 1238));
        var decision = ScreeningUtil.classify(confirmation(input, 1234, 30, 1_000_000), runs, job.manifest().settings(), FMode.MINIMIZE);
        assertEquals("RETAINED", decision.classification());
        assertEquals(1, decision.fastHits());
    }

    @Test void resumedEarlyRetentionUsesAllCachedSeedsWithoutLoadingOrWarmup() throws Exception {
        useThirtyConfirmations();
        for (Input input : job.manifest().inputs()) ScreeningFileUtil.writeRun(output, result(input, 1234));
        Input input = job.manifest().inputs().getFirst();
        ScreeningFileUtil.writeRun(output, confirmation(input, 1235, 10, 1_000_000));
        ScreeningFileUtil.writeRun(output, confirmation(input, 1237, 9, 20_000_000));
        String cached = AutoLlmFileUtil.hash(ScreeningFileUtil.runPath(output, input.path(), 1237));
        worker.screen(job);
        assertTrue(measurements.isEmpty());
        verifyNoInteractions(instances);
        assertNull(ScreeningFileUtil.readRun(output, job.manifest(), input, 1236));
        assertEquals(cached, AutoLlmFileUtil.hash(ScreeningFileUtil.runPath(output, input.path(), 1237)));
        var runs = ScreeningFileUtil.readConfirmations(output, job.manifest(), input);
        assertEquals(List.of(1235L, 1237L), List.of(runs.getFirst().seed(), runs.getLast().seed()));
        assertEquals("RETAINED", ScreeningUtil.classify(result(input, 1234), runs, job.manifest().settings(), FMode.MINIMIZE).classification());
        var progress = AutoLlmFileUtil.JSON.readTree(Files.readString(output.resolve("screening-progress.json")));
        assertEquals(2, progress.path("confirmationsCompleted").asInt());
        assertEquals(2, progress.path("confirmationsTotal").asInt());
        worker.screen(job);
        assertTrue(measurements.isEmpty());
    }

    @Test void oneWorkerWarmsOnceAndCompletesAllInitialInputsBeforeConfirmations() throws Exception {
        worker.screen(job);
        var seeds = new ArrayList<Long>();
        for (Measurement measurement : measurements) seeds.add(measurement.seed());
        assertEquals(List.of(234L, 233L, 232L, 231L, 230L, 1234L, 1234L, 1235L, 1236L),
                seeds);
        assertEquals(job.manifest().inputs().getLast().path(), measurements.get(6).input());
        assertEquals(1_000, measurements.getLast().millis());
        assertNotNull(ScreeningFileUtil.readRun(output, job.manifest(), job.manifest().inputs().getFirst(), 1236));
        assertNull(ScreeningFileUtil.readRun(output, job.manifest(), job.manifest().inputs().getLast(), 1235));
        var progress = AutoLlmFileUtil.JSON.readTree(Files.readString(output.resolve("screening-progress.json")));
        assertEquals("MEASUREMENTS_COMPLETE", progress.path("phase").asString()); assertEquals(2, progress.path("initialCompleted").asInt()); assertEquals(2, progress.path("confirmationsCompleted").asInt());
    }

    @Test void resumedConfirmationsWarmOnceAndPreserveCompletedEvidence() throws Exception {
        for (Input input : job.manifest().inputs()) ScreeningFileUtil.writeRun(output, result(input, 1234));
        Input fast = job.manifest().inputs().getFirst();
        ScreeningFileUtil.writeRun(output, result(fast, 1235));
        String cached = AutoLlmFileUtil.hash(ScreeningFileUtil.runPath(output, fast.path(), 1235));
        worker.screen(job);
        var seeds = new ArrayList<Long>();
        for (Measurement measurement : measurements) seeds.add(measurement.seed());
        assertEquals(List.of(234L, 233L, 232L, 231L, 230L, 1236L),
                seeds);
        assertEquals(cached, AutoLlmFileUtil.hash(ScreeningFileUtil.runPath(output, fast.path(), 1235)));
        measurements.clear();
        worker.screen(job);
        assertTrue(measurements.isEmpty());
        assertEquals(2, AutoLlmFileUtil.JSON.readTree(Files.readString(output.resolve("screening-progress.json"))).path("confirmationsTotal").asInt());
    }

    @Test void failedInputLoadingIsRecordedAndOtherInputsStillRun() throws Exception {
        Input fast = job.manifest().inputs().getFirst();
        when(instances.getInstance(fast.path())).thenThrow(new IllegalArgumentException("bad input"));
        worker.screen(job);
        Run failed = ScreeningFileUtil.readRun(output, job.manifest(), fast, 1234);
        assertFalse(failed.success());
        assertTrue(failed.failure().contains("bad input"));
        assertTrue(ScreeningFileUtil.readRun(output, job.manifest(), job.manifest().inputs().getLast(), 1234).success());
        assertNull(ScreeningFileUtil.readRun(output, job.manifest(), fast, 1235));
    }

    @Test void changedFrozenInputsStopMeasurement() throws Exception {
        Files.writeString(Path.of(job.manifest().inputs().getFirst().path()), "changed");
        assertThrows(IllegalStateException.class, () -> worker.screen(job));
        assertTrue(measurements.isEmpty());
    }
}
