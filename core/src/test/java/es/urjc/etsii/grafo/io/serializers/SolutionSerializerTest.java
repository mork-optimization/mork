package es.urjc.etsii.grafo.io.serializers;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestSerializerConfigUtils;
import es.urjc.etsii.grafo.testutil.TestSolution;
import es.urjc.etsii.grafo.testutil.TestSerializerConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.platform.commons.function.Try;

public class SolutionSerializerTest {

    /**
     * Create a new solution serializer with the given config
     *
     * @param config
     */
    public static SolutionSerializer<TestSolution, TestInstance> initSerializer(TestSerializerConfig config) {
        SolutionSerializer<TestSolution, TestInstance> serializer = new SolutionSerializer<TestSolution, TestInstance>(config) {
            @Override
            public void export(BufferedWriter writer, TestSolution testSolution) throws IOException {
                writer.write(testSolution.toString());

            }
        };
        return serializer;
    }

    public String formatHour(int hour) {
        return (hour < 10) ? "0" + hour : "" + hour;
    }

    @Test
    public void getNameTest(@TempDir Path temp) {
        ArrayList<String[]> values = new ArrayList<>();
        values.add(new String[]{"", "experiment_instance_alg_0_"});
        values.add(new String[]{"'Result'", "experiment_instance_alg_0_Result"});
        values.add(new String[]{"'Result'yyyy", "experiment_instance_alg_0_Result" + LocalDate.now().getYear()});
        values.add(new String[]{"'Result'-yyyy", "experiment_instance_alg_0_Result-" + LocalDate.now().getYear()});
        values.add(new String[]{"'Result'HH", "experiment_instance_alg_0_Result" + formatHour(LocalTime.now().getHour())});
        values.add(new String[]{"'Result'-HH", "experiment_instance_alg_0_Result-" + formatHour(LocalTime.now().getHour())});

        for (String[] value : values) {
            var config = TestSerializerConfigUtils.create(true, AbstractResultSerializerConfig.Frequency.EXPERIMENT_END, temp, value[0]);
            var serializer = SolutionSerializerTest.initSerializer(config);
            var name = serializer.getFilename("experiment", "instance", "alg", "0");
            Assertions.assertEquals(name, value[1]);
        }


    }

    @Test
    public void getNameTestTimeFormats(@TempDir Path temp) {
        ArrayList<String[]> values = new ArrayList<>();
        values.add(new String[]{"'Results'_yyyy-MM-dd_HH-mm-ss.'test'", "0"});
        values.add(new String[]{"'Results'_yyyy-MM-test-dd_HH-mm-ss.'test'", "-1"});
        values.add(new String[]{"'Results'_HH-mm-ss.'test'", "0"});
        values.add(new String[]{"'Results'_HH-mmtest-ss.'test'", "-1"});
        values.add(new String[]{"'Results'_yyyy-MM-dd.'test'", "0"});
        values.add(new String[]{"'Results'_yyyy-MM-test-dd.'test'", "-1"});

        for (String[] value : values) {
            var config = TestSerializerConfigUtils.create(true, AbstractResultSerializerConfig.Frequency.EXPERIMENT_END, temp, value[0]);
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
