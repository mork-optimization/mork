package es.urjc.etsii.grafo.autoconfig.builder;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.autoconfig.exception.AlgorithmParsingException;
import es.urjc.etsii.grafo.autoconfig.inventory.AlgorithmInventoryService;
import es.urjc.etsii.grafo.autoconfig.irace.params.ParameterType;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AlgorithmBuilderService {

    private final AlgorithmInventoryService inventoryService;
    private final ComponentSpecJsonCodec jsonCodec;

    public AlgorithmBuilderService(AlgorithmInventoryService inventoryService) {
        this.inventoryService = inventoryService;
        this.jsonCodec = new ComponentSpecJsonCodec();
    }

    /**
     * Build any algorithm component given its name and a map of parameters
     * @param name component name, can be either an alias, a factory reference or a classname
     * @param params parameter map used to create this component, provides the necessary args to call the constructor
     * @return built component
     */
    public Object buildAlgorithmComponentByName(String name, Map<String, Object> params){
        var inventory = this.inventoryService.getInventory();
        if(inventory.aliases().containsKey(name)){
            // Resolve alias and keep trying to build
            return buildAlgorithmComponentByName(inventory.aliases().get(name), params);
        }
        if(inventory.factories().containsKey(name)){
            // If registered factory method, invoke it adding our provided values
            var factory = inventory.factories().get(name);
            var factoryParams = new HashMap<>(params);
            for(var p: factory.getRequiredParameters()){
                if(p.getType() == ParameterType.PROVIDED && !params.containsKey(p.getName())){
                    factoryParams.put(p.getName(), AlgorithmBuilderUtil.getProvidedValue(p.getJavaType(), p.getName(), inventory.paramProviders()));
                }
            }
            return factory.buildComponent(factoryParams);
        }

        // Either we have discovered the component automatically, or fail
        if(!inventory.componentByName().containsKey(name)){
            // fail
            throw new AlgorithmParsingException(String.format("Unknown component: %s, known components: %s, aliases: %s, factories: %s", name, inventory.componentByName().keySet(), inventory.aliases().keySet(), inventory.factories().keySet()));
        }
        var clazz = inventory.componentByName().get(name);
        return AlgorithmBuilderUtil.build(clazz, params, inventory.paramProviders());
    }

    /**
     * Build an algorithm from a component specification.
     *
     * @param spec algorithm component specification
     * @throws AlgorithmParsingException if the built component is not a valid algorithm
     * @return built algorithm
     */
    public Algorithm<?,?> buildAlgorithm(ComponentSpec spec){
        var component = buildAlgorithmComponent(spec);
        if(!(component instanceof Algorithm<?,?>)){
            String componentName = component == null? "null value": component.getClass().getSimpleName();
            throw new AlgorithmParsingException(String.format("Description does not represent an algorithm, built class type: %s", componentName));
        }
        return (Algorithm<?,?>) component;
    }

    /**
     * Parse a JSON algorithm description and build it.
     *
     * @param json flattened JSON algorithm description
     * @return built algorithm
     */
    public Algorithm<?,?> buildAlgorithmFromJson(String json) {
        return buildAlgorithm(jsonCodec.parse(json));
    }

    /**
     * Build any algorithm component from a component specification.
     *
     * @param spec component specification
     * @return built component
     */
    public Object buildAlgorithmComponent(ComponentSpec spec) {
        var parameters = new LinkedHashMap<String, Object>();
        for (var parameter : spec.parameters().entrySet()) {
            parameters.put(parameter.getKey(), buildNestedValue(parameter.getValue()));
        }
        return buildAlgorithmComponentByName(spec.component(), parameters);
    }

    /**
     * Parse a JSON component description and build it.
     *
     * @param json flattened JSON component description
     * @return built component
     */
    public Object buildAlgorithmComponentFromJson(String json) {
        return buildAlgorithmComponent(jsonCodec.parse(json));
    }

    public String toJson(ComponentSpec spec) {
        return jsonCodec.toJson(spec);
    }

    public JsonNode toJsonTree(ComponentSpec spec) {
        return jsonCodec.toJsonTree(spec);
    }

    private Object buildNestedValue(Object value) {
        if (value instanceof ComponentSpec spec) {
            return buildAlgorithmComponent(spec);
        }
        if (value instanceof List<?> list) {
            var builtValues = new java.util.ArrayList<Object>(list.size());
            for (var item : list) {
                builtValues.add(buildNestedValue(item));
            }
            return builtValues;
        }
        return value;
    }
}
