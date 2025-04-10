package es.urjc.etsii.grafo.autoconfig.controller;

import es.urjc.etsii.grafo.autoconfig.controller.dto.IraceExecuteConfig;
import es.urjc.etsii.grafo.autoconfig.irace.AlgorithmConfiguration;
import es.urjc.etsii.grafo.autoconfig.irace.IraceRuntimeConfiguration;

import java.util.ArrayList;
import java.util.Arrays;

public class IraceUtil {
    private IraceUtil(){}

    public static IraceRuntimeConfiguration toIraceRuntimeConfig(String commandline) {
        String[] args = commandline.split("\\s+");

        String candidateConfiguration = args[0];
        String instanceId = args[1];
        String seed = args[2];
        String instance = args[3];

        String[] algParams = Arrays.copyOfRange(args, 4, args.length);

        return new IraceRuntimeConfiguration(candidateConfiguration, instanceId, seed, instance, new AlgorithmConfiguration(algParams));
    }

    public static IraceRuntimeConfiguration toIraceRuntimeConfig(IraceExecuteConfig config) {
        var paramValues = config.getConfiguration();
        var algParams = new ArrayList<String>(paramValues.size());
        for(var e: paramValues.entrySet()){
            var paramName = e.getKey() + "=";
            var paramValue = e.getValue();
            if(!paramValue.equals("NA")){
                algParams.add(paramName + paramValue);
            }
        }

        return new IraceRuntimeConfiguration(config.getName(), config.getInstanceId(), config.getSeed(), config.getInstance(), new AlgorithmConfiguration(algParams.toArray(new String[0])));
    }
}
