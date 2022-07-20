package es.urjc.etsii.grafo.solver.services;

import es.urjc.etsii.grafo.annotations.AlgorithmComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

@Service
public class AlgComponentService {
    private static final Logger log = LoggerFactory.getLogger(AlgComponentService.class);

    Map<Class<?>, Collection<Class<?>>> componentsByType = new HashMap<>();
    Map<String, Class<?>> componentByName = new HashMap<>();

    @Value("${advanced.scan-pkgs:es.urjc.etsii}")
    String pkgs;

    @PostConstruct
    protected void initialize(){
        var types = new ArrayList<Class<?>>();
        for(var pkg: pkgs.split(",")){
            types.addAll(ClassUtil.findTypesByAnnotation(pkg, AlgorithmComponent.class));
        }
        for(var type: types){
            componentByName.put(type.getSimpleName(), type);
            classify(componentsByType, type);
        }
        log.info("Algorithm components found: {}", componentByName.keySet());
        log.debug("Classified algorithm components: {}", componentsByType);
    }

    private static void classify(Map<Class<?>, Collection<Class<?>>> componentsByType, Class<?> initialType) {
        for (Class<?> type = initialType; type != null; type = type.getSuperclass()){
            if(!ClassUtil.isObjectClass(type)){
                componentsByType.computeIfAbsent(type, a -> new HashSet<>()).add(initialType);
            }
        }
    }
}
