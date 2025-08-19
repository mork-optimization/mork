package es.urjc.etsii.grafo.CAP.components;

import es.urjc.etsii.grafo.CAP.Main;
import es.urjc.etsii.grafo.CAP.model.CAPInstance;
import es.urjc.etsii.grafo.CAP.model.CAPSolution;
import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.aop.TimeStats;
import es.urjc.etsii.grafo.metrics.Metrics;
import es.urjc.etsii.grafo.services.TimeLimitCalculator;
import es.urjc.etsii.grafo.util.TimeUtil;
import es.urjc.etsii.grafo.util.random.RandomManager;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;
import static java.lang.Math.min;

public class CAPC extends Algorithm<CAPSolution, CAPInstance> {

    private final String config;
    private final TimeLimitCalculator<CAPSolution, CAPInstance> timeLimit;

    /**
     * Initialize common algorithm fields
     *
     * @param algorithmName algorithm name
     * @param timeLimit
     */
    public CAPC(String algorithmName, String config, TimeLimitCalculator<CAPSolution, CAPInstance> timeLimit) {
        super(algorithmName);
        this.config = config;
        this.timeLimit = timeLimit;
    }

    @Override
    @TimeStats
    public CAPSolution algorithm(CAPInstance instance) {
        var timeInMillis = this.timeLimit.timeLimitInMillis(instance, this);
        var timeInSeconds = timeInMillis / 1_000.0;
        var executablePath = "src_c/binary/SFMRFLP";
        var seed = RandomManager.getRandom().nextInt();
        var logsFrequency = 1;
        var args = new String[]{
                executablePath,
                instance.getPath(),
                String.valueOf(seed),
                String.valueOf(timeInSeconds),
                String.valueOf(logsFrequency),
                this.config
        };
        var output = execute(args);
        var solution = parseSolution(instance, output);
        return solution;
    }

    private CAPSolution parseSolution(CAPInstance instance, String output) {
        var refTime = Metrics.get(Main.OBJ.getName()).getReferenceNanoTime();
        var solution = new CAPSolution(instance);
        Pattern p = Pattern.compile("\\*?(\\d+)\\.\\s*T\\(s\\):\\s*(\\d+(?:\\.\\d+)?)\\s*Cost:\\s*(\\d+(?:\\.\\d+)?)\\s*(\\d+(?:\\.\\d+)?)\\s*(\\d+(?:\\.\\d+)?)\\s*(\\d+(?:\\.\\d+)?)");
        var lines = output.split("\n");
        int i;
        for(i = 0; i < lines.length; i++){
            var matcher = p.matcher(lines[i]);
            if(!matcher.find()){
                break;
            }
            int iteration = parseInt(matcher.group(1));
            double time = Double.parseDouble(matcher.group(2));
            double cost1 = Double.parseDouble(matcher.group(3));
            double cost2 = Double.parseDouble(matcher.group(4));
            double cost3 = Double.parseDouble(matcher.group(5));
            double cost4 = Double.parseDouble(matcher.group(6));
            double minCost = min(min(cost1, cost2), min(cost3, cost4));

            Metrics.add(Main.OBJ.getName(), (long) (time * TimeUtil.NANOS_IN_SECOND) + refTime, minCost);
        }
        while(i < lines.length && !lines[i].toLowerCase().contains("solution")){
            i++;
        }
        i++;
        int currentRow = 0;
        var raw = solution.getRho();
        while (i < lines.length){
            var line = lines[i];
            if(line.isBlank() || line.toLowerCase().contains("completed")){
                i++; continue;
            }
            for(var id: line.split("\\s+")){
                raw[currentRow].add(parseInt(id));
            }
            currentRow++;
            i++;
        }
        solution.Evaluate();
        solution.notifyUpdate();
        return solution;
    }

    private String execute(String[] args) {
        try {
            var process = Runtime.getRuntime().exec(args);
            var output = IOUtils.toString(process.getInputStream(), StandardCharsets.UTF_8);
            var error = IOUtils.toString(process.getErrorStream(), StandardCharsets.UTF_8);
            process.waitFor();
            return output + "\n\n" + error;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
