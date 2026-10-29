package es.urjc.etsii.grafo.autoconfig.autollm.execution;

import es.urjc.etsii.grafo.autoconfig.autollm.*;
import es.urjc.etsii.grafo.autoconfig.screening.ScreeningFileUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TargetPreflightBudgetTest {
    @TempDir Path temp;
    @Test void transportDeadlineCoversAllSequentialCheckBudgetsIncludingSelector() throws Exception {
        var processes = mock(AutoLlmProcessRunner.class); var total = new AtomicLong();
        when(processes.run(any(), any(), any(), anyLong(), anyLong(), anyLong(), any(), anyBoolean(), anyLong())).thenAnswer(call -> {
            total.addAndGet((long)call.getArgument(3) + (long)call.getArgument(5));
            return new AutoLlmProcessRunner.Result(0, false, 1);
        });
        var defaults = AutoLlmConfig.defaults();
        var config = new AutoLlmConfig(new AutoLlmConfig.Project(temp.toString(), "HEAD", List.of("mvn", "verify"), "target/app.jar", null), temp.toString(), null, null, null, null, null, null, null, null, null, null, null, null);
        try (var host = mockStatic(ScreeningFileUtil.class)) {
            host.when(ScreeningFileUtil::host).thenReturn(Map.of("hostname", "fixture"));
            var result = TargetPreflightUtil.inspect(config, temp, processes, "");
            assertEquals(5, result.prerequisites().size());
            assertTrue(TargetPreflightUtil.timeoutMillis(config, "") > total.get());
            assertTrue(TargetPreflightUtil.timeoutMillis(config, "") > TargetPreflightUtil.timeoutMillis(config, null));
            assertTrue(TargetPreflightUtil.timeoutMillis(config, "") > TargetPreflightUtil.timeoutMillis(defaults, ""));
            assertEquals("selector-python", result.prerequisites().get(3).name());
        }
    }
}
