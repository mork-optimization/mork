package es.urjc.etsii.grafo.io;

import es.urjc.etsii.grafo.config.InstanceConfiguration;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestInstanceImporter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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
        var path = "src/test/resources/instancesnocompr";
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
