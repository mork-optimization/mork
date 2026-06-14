package es.urjc.etsii.grafo.io;

import es.urjc.etsii.grafo.config.InstanceConfiguration;
import es.urjc.etsii.grafo.config.SolverConfig;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestInstanceImporter;
import es.urjc.etsii.grafo.util.IOUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class InstanceManagerTest {

    public static final String TEST_EXPERIMENT = "Experiment1";
    InstanceImporter<TestInstance> instanceImporter;
    String instancePath;
    File instance1File;
    File instance2File;
    File instance3File;
    File indexFile;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUpMock(@TempDir File tempFolder) throws IOException {
        instancePath = tempFolder.getAbsolutePath();
        instance1File = new File(tempFolder, "TestInstance1");
        instance1File.createNewFile();
        instance2File = new File(tempFolder, "TestInstance2");
        instance2File.createNewFile();
        instance3File = new File(tempFolder, "TestInstance3");
        instance3File.createNewFile();

        indexFile = new File(tempFolder, "instances.index");
        Files.writeString(indexFile.toPath(), "TestInstance3\nTestInstance1");

        var instance1 = new TestInstance("TestInstance1");
        instance1.setPath(instance1File.getAbsolutePath());

        var instance2 = new TestInstance("TestInstance2");
        instance2.setPath(instance2File.getAbsolutePath());

        var instance3 = new TestInstance("TestInstance3");
        instance3.setPath(instance3File.getAbsolutePath());

        instanceImporter = mock(InstanceImporter.class);


        when(instanceImporter.importInstance(instance1File.getAbsolutePath())).thenReturn(instance1);
        when(instanceImporter.importInstance(instance2File.getAbsolutePath())).thenReturn(instance2);
        when(instanceImporter.importInstance(instance3File.getAbsolutePath())).thenReturn(instance3);

        // If the instance manager requests to load a non-existing instance, or the index file, throw an exception
        when(instanceImporter.importInstance(indexFile.getAbsolutePath())).thenThrow(RuntimeException.class);
    }

    private InstanceManager<TestInstance> buildManager(boolean preload, String instancePath){
        return buildManager(preload, instancePath, this.instanceImporter);
    }

    private InstanceManager<TestInstance> buildManager(boolean preload, String instancePath, InstanceImporter<TestInstance> instanceImporter){
        return buildManager(preload, instancePath, instanceImporter, new SolverConfig());
    }

    private InstanceManager<TestInstance> buildManager(boolean preload, String instancePath, InstanceImporter<TestInstance> instanceImporter, SolverConfig solverConfig){
        InstanceConfiguration config = new InstanceConfiguration();
        config.setPreload(preload);
        config.setPath(Map.of(TEST_EXPERIMENT, instancePath));
        return new InstanceManager<>(config, solverConfig, instanceImporter);
    }

    @Test
    void testLazy(){
        var manager = buildManager(false, instancePath);
        var instances = manager.getInstanceSolveOrder(TEST_EXPERIMENT);
        verifyNoInteractions(instanceImporter);
        assertEquals(instances.get(0), instance1File.getAbsolutePath());
        assertEquals(instances.get(1), instance2File.getAbsolutePath());
    }

    @Test
    void testCustomOrder(){
        var manager = buildManager(true, instancePath);
        var instances = manager.getInstanceSolveOrder(TEST_EXPERIMENT);
        verify(instanceImporter, times(3)).importInstance(any());
        assertEquals(instances.get(2), instance1File.getAbsolutePath());
        assertEquals(instances.get(1), instance2File.getAbsolutePath());
        assertEquals(instances.get(0), instance3File.getAbsolutePath());
    }

    @Test
    void testIndexFileOrdered(){
        var manager = buildManager(true, indexFile.getAbsolutePath());
        var instances = manager.getInstanceSolveOrder(TEST_EXPERIMENT);
        // there are 3 instances in the folder, but only instance3 and 1 should have been loaded
        assertEquals(2, instances.size());
        verify(instanceImporter, times(1)).importInstance(instance1File.getAbsolutePath());
        verify(instanceImporter, times(1)).importInstance(instance3File.getAbsolutePath());
        verifyNoMoreInteractions(instanceImporter);
    }

    @Test
    void testIndexFileWithBomAndComment() throws IOException {
        Files.writeString(indexFile.toPath(), "\uFEFF#comment\nTestInstance3\n\nTestInstance1\n");

        var manager = buildManager(true, indexFile.getAbsolutePath());
        var instances = manager.getInstanceSolveOrder(TEST_EXPERIMENT);

        assertEquals(2, instances.size());
        Assertions.assertTrue(instances.contains(instance1File.getAbsolutePath()));
        Assertions.assertTrue(instances.contains(instance3File.getAbsolutePath()));
        verify(instanceImporter, times(1)).importInstance(instance1File.getAbsolutePath());
        verify(instanceImporter, times(1)).importInstance(instance3File.getAbsolutePath());
        verifyNoMoreInteractions(instanceImporter);
    }

    @Test
    void testIndexFileWithCompressedRelativeLoadPath() throws IOException {
        Path targetDirectory = Path.of("target");
        Files.createDirectories(targetDirectory);
        Path indexDirectory = Files.createTempDirectory(targetDirectory, "compressed-index");
        Path sourceArchive = Path.of("src/test/resources/instzip/data1.zip");
        Path archive = indexDirectory.resolve("data1.zip");
        Files.copy(sourceArchive, archive);
        String compressedEntry = IOUtil.entryPath(IOUtil.iterate(archive.toString()).get(0));
        Path compressedIndexFile = indexDirectory.resolve("instances.index");
        Files.writeString(compressedIndexFile, "data1.zip!" + compressedEntry);

        var manager = buildManager(false, compressedIndexFile.toString(), new TestInstanceImporter());
        var instances = manager.getInstanceSolveOrder(TEST_EXPERIMENT);

        assertEquals(1, instances.size());
        assertEquals(
                InstanceCache.canonicalize(archive.toAbsolutePath() + "!" + compressedEntry),
                InstanceCache.canonicalize(instances.get(0))
        );
        Assertions.assertNotNull(manager.getInstance(instances.get(0)));
    }

    @Test
    void checkCache(){
        var manager = buildManager(true, instancePath);
        var instances = manager.getInstanceSolveOrder(TEST_EXPERIMENT);
        verify(instanceImporter, times(3)).importInstance(any());
        manager.getInstance(instances.get(0));
        manager.getInstance(instances.get(1));
        verify(instanceImporter, times(3)).importInstance(any());
        manager.purgeCache();
        manager.getInstance(instances.get(0));
        manager.getInstance(instances.get(1));
        verify(instanceImporter, times(5)).importInstance(any());
    }

    @Test
    void checkNoCache(){
        var manager = buildManager(false, instancePath);
        var instances = manager.getInstanceSolveOrder(TEST_EXPERIMENT);
        verifyNoInteractions(instanceImporter);
        manager.getInstance(instances.get(0));
        verify(instanceImporter, times(1)).importInstance(any());
        manager.getInstance(instances.get(1));
        verify(instanceImporter, times(2)).importInstance(any());
        manager.getInstance(instances.get(1));
        verify(instanceImporter, times(2)).importInstance(any());

        manager.purgeCache();
        manager.getInstance(instances.get(1));
        verify(instanceImporter, times(3)).importInstance(any());

        verifyNoMoreInteractions(instanceImporter);
        manager.getInstance(instances.get(1));
    }

    @Test
    void warmupUsesExplicitInstancePath(){
        var solverConfig = new SolverConfig();
        solverConfig.getWarmup().setInstancePath(instance2File.getAbsolutePath());

        var manager = buildManager(false, instancePath, instanceImporter, solverConfig);
        var selected = manager.getWarmupInstancePath(TEST_EXPERIMENT, List.of(instance1File.getAbsolutePath(), instance3File.getAbsolutePath()));

        assertEquals(instance2File.getAbsolutePath(), selected);
        verifyNoInteractions(instanceImporter);
    }

    @Test
    void warmupAutoSelectsFastestLoadedInstance(){
        var manager = buildManager(true, instancePath);
        manager.getInstanceSolveOrder(TEST_EXPERIMENT);

        manager.getInstance(instance1File.getAbsolutePath()).setProperty(Instance.LOAD_TIME_NANOS, 30L);
        manager.getInstance(instance2File.getAbsolutePath()).setProperty(Instance.LOAD_TIME_NANOS, 10L);
        manager.getInstance(instance3File.getAbsolutePath()).setProperty(Instance.LOAD_TIME_NANOS, 20L);

        var selected = manager.getWarmupInstancePath(TEST_EXPERIMENT, List.of(
                instance1File.getAbsolutePath(),
                instance2File.getAbsolutePath(),
                instance3File.getAbsolutePath()
        ));

        assertEquals(instance2File.getAbsolutePath(), selected);
    }

    @Test
    void warmupAutoFallsBackToSmallestFileWhenLoadedTimesAreUnavailable() throws IOException {
        Files.writeString(instance1File.toPath(), "1234");
        Files.writeString(instance2File.toPath(), "123456");
        Files.writeString(instance3File.toPath(), "1");

        var manager = buildManager(true, instancePath);
        var instances = manager.getInstanceSolveOrder(TEST_EXPERIMENT);
        clearInvocations(instanceImporter);
        manager.purgeCache();

        var selected = manager.getWarmupInstancePath(TEST_EXPERIMENT, instances);

        assertEquals(instance3File.getAbsolutePath(), selected);
        verifyNoInteractions(instanceImporter);
    }

    @SuppressWarnings("unchecked")
    @Test
    void warmupAutoSelectionUsesCachedLoadTimesForRelativizedSolveOrderPaths() throws IOException {
        Path targetDirectory = Path.of("target");
        Files.createDirectories(targetDirectory);
        Path relativeFolder = Files.createTempDirectory(targetDirectory, "warmup-cache");
        var slowFile = relativeFolder.resolve("SlowInstance").toFile();
        var fastFile = relativeFolder.resolve("FastInstance").toFile();
        slowFile.createNewFile();
        fastFile.createNewFile();

        String slowPath = slowFile.getAbsolutePath();
        String fastPath = fastFile.getAbsolutePath();
        var slowInstance = new TestInstance("SlowInstance");
        slowInstance.setPath(slowPath);
        var fastInstance = new TestInstance("FastInstance");
        fastInstance.setPath(fastPath);

        InstanceImporter<TestInstance> importer = mock(InstanceImporter.class);
        when(importer.importInstance(slowPath)).thenReturn(slowInstance);
        when(importer.importInstance(fastPath)).thenReturn(fastInstance);

        var manager = buildManager(true, relativeFolder.toString(), importer);
        var instances = manager.getInstanceSolveOrder(TEST_EXPERIMENT);
        manager.getInstance(slowPath).setProperty(Instance.LOAD_TIME_NANOS, 30L);
        manager.getInstance(fastPath).setProperty(Instance.LOAD_TIME_NANOS, 10L);
        clearInvocations(importer);

        var selected = manager.getWarmupInstancePath(TEST_EXPERIMENT, instances);

        assertEquals(fastInstance.getPath(), selected);
        verifyNoInteractions(importer);
    }

    @Test
    void warmupAutoSelectsSmallestFileIfPreloadDisabled() throws IOException {
        Files.writeString(instance1File.toPath(), "1234");
        Files.writeString(instance2File.toPath(), "123456");
        Files.writeString(instance3File.toPath(), "1");

        var manager = buildManager(false, instancePath);
        var instances = manager.getInstanceSolveOrder(TEST_EXPERIMENT);
        var selected = manager.getWarmupInstancePath(TEST_EXPERIMENT, instances);

        assertEquals(instance3File.getAbsolutePath(), selected);
        verifyNoInteractions(instanceImporter);
    }

    @Test
    void validateTest(){
        var manager = buildManager(false, instancePath);
        assertEquals(instanceImporter, manager.getUserImporterImplementation());
        Assertions.assertThrows(IllegalArgumentException.class, () -> manager.validate(new ArrayList<>(), TEST_EXPERIMENT));
        Assertions.assertThrows(IllegalArgumentException.class, () -> manager.validate(List.of(
                new TestInstance("Instance 1"), new TestInstance("Instance 2"), new TestInstance("Instance 1")
        ), TEST_EXPERIMENT));
    }

    @Test
    void validateNoCompression(){
        var path = "src/test/resources/instances";
        var manager = buildManager(false, path, new TestInstanceImporter());
        var instances = manager.getInstanceSolveOrder(TEST_EXPERIMENT);
        assertEquals(6, instances.size());
    }

    @Test
    void validateZipCompression(){
        var path = "src/test/resources/instzip";
        var manager = buildManager(false, path, new TestInstanceImporter());
        var instances = manager.getInstanceSolveOrder(TEST_EXPERIMENT);
        assertEquals(6, instances.size());
    }

    @Test
    void validate7zCompression(){
        var path = "src/test/resources/inst7z";
        var manager = buildManager(false, path, new TestInstanceImporter());
        var instances = manager.getInstanceSolveOrder(TEST_EXPERIMENT);
        assertEquals(6, instances.size());
        for(var s: instances){
            var instance = manager.getInstance(s);
            Assertions.assertNotNull(instance);
        }
    }
}
