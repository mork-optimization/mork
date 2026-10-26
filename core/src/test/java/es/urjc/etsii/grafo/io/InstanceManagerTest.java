package es.urjc.etsii.grafo.io;

import es.urjc.etsii.grafo.config.InstanceConfiguration;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestInstanceImporter;
import es.urjc.etsii.grafo.util.IOUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

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
        InstanceConfiguration config = new InstanceConfiguration();
        config.setPreload(preload);
        config.setPath(Map.of(TEST_EXPERIMENT, instancePath));
        return new InstanceManager<>(config, instanceImporter);
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
    void configuredFolderAuthorizesNormalizedRequestedPath(){
        var manager = buildManager(false, instancePath);
        Path requestedPath = instance1File.toPath().getParent()
                .resolve("folder")
                .resolve("..")
                .resolve(instance1File.getName());

        var authorizedPath = manager.requireConfiguredInstancePath(TEST_EXPERIMENT, requestedPath.toString());

        assertEquals(instance1File.getAbsolutePath(), authorizedPath);
        assertEquals(instance2File.getAbsolutePath(), manager.requireConfiguredInstancePath(TEST_EXPERIMENT, instance2File.getName()));
        verifyNoInteractions(instanceImporter);
    }

    @Test
    void configuredFolderRejectsTraversalOutsideConfiguredPath() throws IOException {
        Path tempRoot = Path.of(instancePath);
        Path configuredFolder = Files.createDirectory(tempRoot.resolve("configured"));
        Files.createFile(configuredFolder.resolve("AllowedInstance"));
        Path outsideFile = Files.createFile(tempRoot.resolve("OutsideInstance"));
        var manager = buildManager(false, configuredFolder.toString());
        Path traversalPath = configuredFolder.resolve("..").resolve(outsideFile.getFileName());

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> manager.requireConfiguredInstancePath(TEST_EXPERIMENT, traversalPath.toString()));
        verifyNoInteractions(instanceImporter);
    }

    @Test
    void configuredIndexAuthorizesOnlyListedEntries(){
        var manager = buildManager(false, indexFile.getAbsolutePath());

        var authorizedPath = manager.requireConfiguredInstancePath(TEST_EXPERIMENT, instance1File.getAbsolutePath());

        assertEquals(instance1File.getAbsolutePath(), authorizedPath);
        assertEquals(instance3File.getAbsolutePath(), manager.requireConfiguredInstancePath(TEST_EXPERIMENT, instance3File.getName()));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> manager.requireConfiguredInstancePath(TEST_EXPERIMENT, instance2File.getAbsolutePath()));
        verifyNoInteractions(instanceImporter);
    }

    @Test
    void configuredArchiveAuthorizesKnownCompressedEntryOnly() throws IOException {
        Path targetDirectory = Path.of("target");
        Files.createDirectories(targetDirectory);
        Path archiveDirectory = Files.createTempDirectory(targetDirectory, "authorized-archive");
        Path sourceArchive = Path.of("src/test/resources/instzip/data1.zip");
        Path archive = archiveDirectory.resolve("data1.zip");
        Files.copy(sourceArchive, archive);
        String authorizedLoadPath = IOUtil.iterate(archive.toString()).get(0);
        String compressedEntry = IOUtil.entryPath(authorizedLoadPath);
        String normalizedRequest = archiveDirectory.resolve(".").resolve("data1.zip") + "!" + compressedEntry;
        var manager = buildManager(false, archive.toString(), new TestInstanceImporter());

        var authorizedPath = manager.requireConfiguredInstancePath(TEST_EXPERIMENT, normalizedRequest);

        assertEquals(authorizedLoadPath, authorizedPath);
        assertEquals(authorizedLoadPath, manager.requireConfiguredInstancePath(TEST_EXPERIMENT, archive.getFileName() + "!" + compressedEntry));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> manager.requireConfiguredInstancePath(TEST_EXPERIMENT, archive + "!missing-entry.txt"));
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
    void referenceUsesExplicitInstancePathFromConfigurationMap(){
        var properties = new MapConfigurationPropertySource(Map.of(
                "instances.path.default", instancePath,
                "instances.path.reference", "  " + instance2File.getAbsolutePath() + "  "));
        var config = new Binder(properties).bind("instances", Bindable.of(InstanceConfiguration.class)).get();
        var manager = new InstanceManager<>(config, instanceImporter);
        var selected = manager.getReferenceInstancePath(TEST_EXPERIMENT);

        assertEquals(instance2File.getAbsolutePath(), selected);
        verifyNoInteractions(instanceImporter);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void referenceAutoSelectsFastestLoadedInstanceRegardlessOfPreloading(boolean preload){
        var manager = buildManager(preload, instancePath);
        manager.getInstanceSolveOrder(TEST_EXPERIMENT);

        manager.getInstance(instance1File.getAbsolutePath()).setProperty(Instance.LOAD_TIME_NANOS, 30L);
        manager.getInstance(instance2File.getAbsolutePath()).setProperty(Instance.LOAD_TIME_NANOS, 10L);
        manager.getInstance(instance3File.getAbsolutePath()).setProperty(Instance.LOAD_TIME_NANOS, 20L);

        clearInvocations(instanceImporter);
        var selected = manager.getReferenceInstancePath(TEST_EXPERIMENT);

        assertEquals(instance2File.getAbsolutePath(), selected);
        verifyNoInteractions(instanceImporter);
    }

    @Test
    void referenceRetainsLoadTimesAfterInstanceCacheIsCleared() {
        var manager = buildManager(false, instancePath);
        manager.getInstance(instance1File.getAbsolutePath()).setProperty(Instance.LOAD_TIME_NANOS, 30L);
        manager.getInstance(instance2File.getAbsolutePath()).setProperty(Instance.LOAD_TIME_NANOS, 10L);
        manager.getInstance(instance3File.getAbsolutePath()).setProperty(Instance.LOAD_TIME_NANOS, 20L);
        assertEquals(instance2File.getAbsolutePath(), manager.getReferenceInstancePath(TEST_EXPERIMENT));
        clearInvocations(instanceImporter);
        manager.purgeCache();

        var selected = manager.getReferenceInstancePath(TEST_EXPERIMENT);

        assertEquals(instance2File.getAbsolutePath(), selected);
        verifyNoInteractions(instanceImporter);
    }

    @SuppressWarnings("unchecked")
    @Test
    void referenceUsesCachedLoadTimesForRelativizedSolveOrderPaths() throws IOException {
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
        manager.getInstanceSolveOrder(TEST_EXPERIMENT);
        manager.getInstance(slowPath).setProperty(Instance.LOAD_TIME_NANOS, 30L);
        manager.getInstance(fastPath).setProperty(Instance.LOAD_TIME_NANOS, 10L);
        clearInvocations(importer);

        var selected = manager.getReferenceInstancePath(TEST_EXPERIMENT);

        assertEquals(fastInstance.getPath(), selected);
        verifyNoInteractions(importer);
    }

    @Test
    void referenceMeasuresUnknownLoadTimesInsteadOfUsingFileSize() throws IOException {
        Files.writeString(instance1File.toPath(), "123456789");
        Files.writeString(instance2File.toPath(), "1234");
        Files.writeString(instance3File.toPath(), "1");

        var manager = buildManager(false, instancePath);
        manager.getInstance(instance1File.getAbsolutePath()).setProperty(Instance.LOAD_TIME_NANOS, 0L);
        var selected = manager.getReferenceInstancePath(TEST_EXPERIMENT);

        assertEquals(instance1File.getAbsolutePath(), selected);
        verify(instanceImporter).importInstance(instance1File.getAbsolutePath());
        verify(instanceImporter).importInstance(instance2File.getAbsolutePath());
        verify(instanceImporter).importInstance(instance3File.getAbsolutePath());
        verifyNoMoreInteractions(instanceImporter);
    }

    @Test
    void referenceBreaksEqualLoadTimesByPathRatherThanSolveOrder() {
        var manager = buildManager(true, instancePath);
        var instances = manager.getInstanceSolveOrder(TEST_EXPERIMENT);
        assertEquals(instance3File.getAbsolutePath(), instances.getFirst());
        for (String path : instances) manager.getInstance(path).setProperty(Instance.LOAD_TIME_NANOS, 10L);

        assertEquals(instance1File.getAbsolutePath(), manager.getReferenceInstancePath(TEST_EXPERIMENT));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void blankReferenceUsesAutomaticSelectionWithoutFallingBackToDefault(String reference) {
        var config = new InstanceConfiguration();
        config.setPreload(false);
        config.setPath(Map.of("default", instancePath, "reference", reference));
        var manager = new InstanceManager<>(config, instanceImporter);
        manager.getInstance(instance1File.getAbsolutePath()).setProperty(Instance.LOAD_TIME_NANOS, 0L);

        assertEquals(instance1File.getAbsolutePath(), manager.getReferenceInstancePath(TEST_EXPERIMENT));
    }

    @Test
    void referenceRejectsAnExplicitInstanceOutsideTheConfiguredIndex() {
        var config = new InstanceConfiguration();
        config.setPath(Map.of(TEST_EXPERIMENT, indexFile.getAbsolutePath(), "reference", instance2File.getAbsolutePath()));
        var manager = new InstanceManager<>(config, instanceImporter);

        Assertions.assertThrows(IllegalArgumentException.class, () -> manager.getReferenceInstancePath(TEST_EXPERIMENT));
        verifyNoInteractions(instanceImporter);
    }

    @Test
    void referenceResolvesExplicitPathsRelativeToTheConfiguredIndex() {
        var config = new InstanceConfiguration();
        config.setPath(Map.of(TEST_EXPERIMENT, indexFile.getAbsolutePath(), "reference", instance3File.getName()));
        var manager = new InstanceManager<>(config, instanceImporter);

        assertEquals(instance3File.getAbsolutePath(), manager.getReferenceInstancePath(TEST_EXPERIMENT));
        verifyNoInteractions(instanceImporter);
    }

    @Test
    void referenceResolvesExplicitArchiveEntriesRelativeToTheConfiguredIndex() throws IOException {
        Path archive = Path.of(instancePath).resolve("data.zip");
        Files.copy(Path.of("src/test/resources/instzip/data1.zip"), archive);
        String entry = IOUtil.entryPath(IOUtil.iterate(archive.toString()).getFirst());
        Files.writeString(indexFile.toPath(), "data.zip!" + entry);
        var config = new InstanceConfiguration();
        config.setPath(Map.of(TEST_EXPERIMENT, indexFile.getAbsolutePath(), "reference", "data.zip!" + entry));
        var manager = new InstanceManager<>(config, instanceImporter);

        assertEquals(archive + "!" + entry, manager.getReferenceInstancePath(TEST_EXPERIMENT));
        verifyNoInteractions(instanceImporter);
    }

    @Test
    void referenceRejectsAnEmptyCandidateSet() throws IOException {
        Files.writeString(indexFile.toPath(), "");
        var manager = buildManager(false, indexFile.getAbsolutePath());

        Assertions.assertThrows(IllegalArgumentException.class, () -> manager.getReferenceInstancePath(TEST_EXPERIMENT));
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
