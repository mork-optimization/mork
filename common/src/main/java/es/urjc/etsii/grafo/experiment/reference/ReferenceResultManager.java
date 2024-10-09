package es.urjc.etsii.grafo.experiment.reference;

import es.urjc.etsii.grafo.annotations.InheritedComponent;
import es.urjc.etsii.grafo.util.Context;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@InheritedComponent
public class ReferenceResultManager {
    private static final Logger log = LoggerFactory.getLogger(ReferenceResultManager.class);

    private final List<ReferenceResultProvider> referenceResultProviders;

    public ReferenceResultManager(List<ReferenceResultProvider> referenceResultProviders) {
        this.referenceResultProviders = referenceResultProviders;
    }

    @PostConstruct
    public void init() {
        Context.Configurator.setRefResultManager(this);
    }

    public Map<String, Double> getRefValueForAllObjectives(String instanceName, boolean onlyOptimal) {
        var objectives = Context.getObjectives();
        Map<String, Double> bestPerObjective = new HashMap<>();
        for (var r : referenceResultProviders) {
            var ref = r.getValueFor(instanceName);
            if(ref == null || onlyOptimal && !ref.isOptimalValue()){
                // Skip provider if no value for this instance
                // Or the value is not optimal and we only want optimal values
                continue;
            }
            // Update best value for each objective
            for (var entry : ref.getScores().entrySet()) {
                var objName = entry.getKey();
                var objValue = entry.getValue();
                if(!Double.isFinite(objValue)){
                    continue;
                }
                // Value is valid, find corresponding objective
                var objective = objectives.get(objName);
                if(objective == null){
                    log.warn("Objective '{}' not declared in context, but provider '{}' has a value for it in instance '{}'.", objName, r.getProviderName(), instanceName);
                    continue;
                }
                bestPerObjective.putIfAbsent(objName, objValue);
                bestPerObjective.put(objName, objective.best(bestPerObjective.get(objName), objValue));
            }
        }
        return bestPerObjective;
    }
}
