package es.urjc.etsii.grafo.io.serializers;

import es.urjc.etsii.grafo.executors.WorkUnitResult;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestSerializerConfigUtils;
import es.urjc.etsii.grafo.testutil.TestSolution;
import es.urjc.etsii.grafo.testutil.TestSolutionSerializerConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.platform.commons.function.Try;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

public class SolutionSerializerTest {

    private static class TestSerializer extends SolutionSerializer<TestSolution, TestInstance> {
        /**
         * Create a new solution serializer with the given config
         *
         * @param config Common solution serializer configuration
         */
        protected TestSerializer(AbstractSolutionSerializerConfig config) {
            super(config);
        }

        @Override
        public void export(BufferedWriter writer, WorkUnitResult<TestSolution, TestInstance> result) throws IOException {
            writer.write(result.solution().toString());
        }
    }
    /**
     * Create a new solution serializer with the given config
     *
     * @param config
     */
    public static SolutionSerializer<TestSolution, TestInstance> initSerializer(TestSolutionSerializerConfig config) {
        SolutionSerializer<TestSolution, TestInstance> serializer = new TestSerializer(config);
        return serializer;
    }

    public String formatHour(int hour) {
        return (hour < 10) ? "0" + hour : "" + hour;
    }

    @Test
    void getNameTest(@TempDir Path temp) {
        ArrayList<String[]> values = new ArrayList<>();
        values.add(new String[]{"'a'", "experiment_instance_alg_0_a"});
        values.add(new String[]{"'Result'", "experiment_instance_alg_0_Result"});
        values.add(new String[]{"'Result'yyyy", "experiment_instance_alg_0_Result" + LocalDate.now().getYear()});
        values.add(new String[]{"'Result'-yyyy", "experiment_instance_alg_0_Result-" + LocalDate.now().getYear()});
        values.add(new String[]{"'Result'HH", "experiment_instance_alg_0_Result" + formatHour(LocalTime.now().getHour())});
        values.add(new String[]{"'Result'-HH", "experiment_instance_alg_0_Result-" + formatHour(LocalTime.now().getHour())});

        for (String[] value : values) {
            var config = TestSerializerConfigUtils.createSol(true, SolutionExportFrequency.ALL, temp, value[0]);
            var serializer = SolutionSerializerTest.initSerializer(config);
            var name = serializer.getFilename("experiment", "instance", "alg", "0");
            Assertions.assertEquals(name, value[1]);
        }

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                SolutionSerializerTest.initSerializer(
                        TestSerializerConfigUtils.createSol(true, SolutionExportFrequency.ALL, temp, "")
                ).getFilename("experiment", "instance", "alg", "0")
        );
    }

    @Test
    void getNameTestTimeFormats(@TempDir Path temp) {
        ArrayList<String[]> values = new ArrayList<>();
        values.add(new String[]{"'Results'_yyyy-MM-dd_HH-mm-ss.'test'", "0"});
        values.add(new String[]{"'Results'_yyyy-MM-test-dd_HH-mm-ss.'test'", "-1"});
        values.add(new String[]{"'Results'_HH-mm-ss.'test'", "0"});
        values.add(new String[]{"'Results'_HH-mmtest-ss.'test'", "-1"});
        values.add(new String[]{"'Results'_yyyy-MM-dd.'test'", "0"});
        values.add(new String[]{"'Results'_yyyy-MM-test-dd.'test'", "-1"});

        for (String[] value : values) {
            var config = TestSerializerConfigUtils.createSol(true, SolutionExportFrequency.ALL, temp, value[0]);
            var serializer = SolutionSerializerTest.initSerializer(config);
            var tryValue = Try.call(() -> serializer.getFilename("experiment", "instance", "alg", "0"));
            var tryOption = tryValue.toOptional();

            // If it works 0, otherwise -1
            var actual = "-1";
            if (tryOption.isPresent())
                actual = "0";
            Assertions.assertEquals(actual, value[1]);
        }
    }
}
