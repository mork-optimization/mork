package es.urjc.etsii.grafo.io;

import es.urjc.etsii.grafo.solver.configuration.InstanceConfiguration;
import es.urjc.etsii.grafo.testutil.TestInstance;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class InstanceManagerTest {


    InstanceImporter<TestInstance> instanceImporter;
    String instancePath;
    File instance1File;
    File instance2File;


    @SuppressWarnings("unchecked")
    @BeforeEach
    public void setUpMock(@TempDir File tempFolder) throws IOException {
        instancePath = tempFolder.getAbsolutePath();
        instance1File = new File(tempFolder, "TestInstance1");
        instance1File.createNewFile();
        instance2File = new File(tempFolder, "TestInstance2");
        instance2File.createNewFile();

        var instance1 = new TestInstance("TestInstance1");
        instance1.setPath(instance1File.getAbsolutePath());

        var instance2 = new TestInstance("TestInstance2");
        instance2.setPath(instance2File.getAbsolutePath());

        instanceImporter = mock(InstanceImporter.class);
        when(instanceImporter.importInstance(instance1File)).thenReturn(instance1);
        when(instanceImporter.importInstance(instance2File)).thenReturn(instance2);
    }

    @Test
    public void testLazy(){
        InstanceConfiguration config = new InstanceConfiguration();
        config.setPreload(false);
        config.setPath(Map.of("Experiment1", instancePath));
        var manager = new InstanceManager<>(config, instanceImporter);
        var instances = manager.getInstanceSolveOrder("Experiment1");
        verifyNoInteractions(instanceImporter);
        Assertions.assertEquals(instances.get(0), instance1File.getAbsolutePath());
        Assertions.assertEquals(instances.get(1), instance2File.getAbsolutePath());
    }

    @Test
    public void testCustomOrder(){
        InstanceConfiguration config = new InstanceConfiguration();
        config.setPreload(true);
        config.setPath(Map.of("Experiment1", instancePath));
        var manager = new InstanceManager<>(config, instanceImporter);
        var instances = manager.getInstanceSolveOrder("Experiment1");
        verify(instanceImporter, times(2)).importInstance(any());
        Assertions.assertEquals(instances.get(1), instance1File.getAbsolutePath());
        Assertions.assertEquals(instances.get(0), instance2File.getAbsolutePath());
    }

    @Test
    public void checkCache(){
        InstanceConfiguration config = new InstanceConfiguration();
        config.setPreload(true);
        config.setPath(Map.of("Experiment1", instancePath));
        var manager = new InstanceManager<>(config, instanceImporter);
        var instances = manager.getInstanceSolveOrder("Experiment1");
        verify(instanceImporter, times(2)).importInstance(any());
        verifyNoMoreInteractions(instanceImporter);
        manager.getInstance(instances.get(0));
        manager.getInstance(instances.get(1));
    }

    @Test
    public void checkNoCache(){
        InstanceConfiguration config = new InstanceConfiguration();
        config.setPreload(false);
        config.setPath(Map.of("Experiment1", instancePath));
        var manager = new InstanceManager<>(config, instanceImporter);
        var instances = manager.getInstanceSolveOrder("Experiment1");
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
}
