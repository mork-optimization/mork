package es.urjc.etsii.grafo.services;

import es.urjc.etsii.grafo.config.SolverConfig;
import es.urjc.etsii.grafo.metrics.AbstractMetric;
import es.urjc.etsii.grafo.metrics.Metrics;
import es.urjc.etsii.grafo.solver.Mork;
import es.urjc.etsii.grafo.util.ReflectionUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;

@Service
public class MetricsInitializer {

    private final SolverConfig solverConfig;
    private final String packages;

    public MetricsInitializer(SolverConfig config, @Value("${advanced.scan-pkgs:es.urjc.etsii}") String packages) {
        this.solverConfig = config;
        this.packages = packages;
    }

    @PostConstruct
    public void initializeMetrics(){
        Metrics.setSolvingMode(Mork.getFMode());
        if(solverConfig.isMetrics()){
            Metrics.enableMetrics();
        } else {
            Metrics.disableMetrics();
        }
        // Find all implemented metrics and register them in the metrics manager
        for(var pckg: packages.split(",")){
            var metricsTypes = ReflectionUtil.findTypesBySuper(pckg, AbstractMetric.class);
            for(var m: metricsTypes){
                Metrics.register(m, reflectionInitializer(m));
            }
        }
    }

    public static <T extends AbstractMetric> Function<Long, T> reflectionInitializer(Class<T> clazz){
        try {
            Constructor<? extends AbstractMetric> constructor = clazz.getConstructor(long.class);
            return reftime -> (T) construct(constructor, reftime);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Metric %s does not have a constructor that receives a single reference time", e);
        }
    }

    public static <T extends AbstractMetric> T construct(Constructor<T> constructor, long refTime){
        try {
            return constructor.newInstance(refTime);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to initialize metric %s using constructor".formatted(constructor), e);
        }
    }
}
