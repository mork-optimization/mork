package es.urjc.etsii.grafo.autoconfig.controller;

import es.urjc.etsii.grafo.autoconfig.irace.AlgorithmConfiguration;
import es.urjc.etsii.grafo.autoconfig.irace.IraceRuntimeConfiguration;

import java.util.Arrays;

public class IraceUtil {
    private IraceUtil(){}

    public static IraceRuntimeConfiguration toIraceRuntimeConfig(String[] args) {
        String candidateConfiguration = args[0];
        String instanceId = args[1];
        String seed = args[2];
        String instance = args[3];

        String[] algParams = Arrays.copyOfRange(args, 4, args.length);

        return new IraceRuntimeConfiguration(candidateConfiguration, instanceId, Long.parseLong(seed), instance, new AlgorithmConfiguration(algParams));
    }
}
