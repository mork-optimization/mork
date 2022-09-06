package es.urjc.etsii.grafo.autoconfig;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.annotations.ProvidedParam;
import es.urjc.etsii.grafo.autoconfig.exception.AlgorithmParsingException;
import es.urjc.etsii.grafo.solver.Mork;
import es.urjc.etsii.grafo.util.StringUtil;
import org.apache.commons.lang3.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Magic (based on reflection) util methods to create instances of algorithm components at runtime
 */
public class AlgorithmBuilderUtil {
    private static final Logger log = LoggerFactory.getLogger(AlgorithmBuilderUtil.class);

    private record UNKNOWNCLASS(){}

    /**
     * Build algorithm component reflectively
     * @param clazz Algorithm component class
     * @param args arguments for the constructor
     * @return instance if class built with the given params
     */
    public static Object build(Class<?> clazz, Map<String, Object> args){
        Map<String, Class<?>> argTypes = new HashMap<>();
        args.forEach((k, v) -> argTypes.put(k, v == null? UNKNOWNCLASS.class: v.getClass()));
        var constructor = findConstructor(clazz, argTypes);
        if(constructor == null){
            throw new AlgorithmParsingException(String.format("Failed to find constructor method in class %s for params %s, types %s", clazz.getSimpleName(), args, argTypes));
        }
        var cParams = constructor.getParameters();
        var params = new Object[cParams.length];

        for (int i = 0; i < cParams.length; i++) {
            var nextParamName = cParams[i].getName();
            if(args.containsKey(nextParamName)){
                // Either the value is in our map
                params[i] = args.get(nextParamName);
            } else {
                // Or it is a provided value
                params[i] = getProvidedValue(cParams[i]);
            }
        }
        try {
            return constructor.newInstance(params);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object getProvidedValue(Parameter p){
        if(!p.isAnnotationPresent(ProvidedParam.class)){
            throw new IllegalArgumentException(String.format("Parameter %s not annotated with @ProvidedParam", p));
        }

        var provided = p.getAnnotation(ProvidedParam.class);
        return switch (provided.type()){
            case UNKNOWN -> throw new IllegalArgumentException(String.format("Parameter %s is annotated with @ProvidedParam, but the type property has not been specified", p));
            case MAXIMIZE -> Mork.isMaximizing();
            case ALGORITHM_NAME -> StringUtil.randomAlgorithmName();
        };
    }

    /**
     * Find a constructor method in the target class that accepts the given combination of parameter (name, type) in any order.
     * Autoboxing and widening are allowed, example from int to double, or int to Integer.
     * @param clazz target class
     * @param args argument map
     * @return Constructor if found one that matches the given parameters, null if no constructor matches
     * @param <T> Constructor for class T
     */
    @SuppressWarnings("unchecked")
    public static <T> Constructor<T> findConstructor(Class<T> clazz, Map<String, Class<?>> args){
        var constructors = clazz.getConstructors();

        for(var c: constructors){
            boolean matches = doParamsMatch(c, args);
            if(matches){
                log.debug("Found matching constructor {} for args {}", c, args);
                return (Constructor<T>) c;
            }
        }
        log.debug("Failed to to found a matching constructor for class {} and args {}, detected constructors: {}", clazz.getSimpleName(), args, constructors);
        return null;
    }

    /**
     * Analyze an algorithm component class to find which constructor is annotated with @AutoconfigConstructor
     * @param algComponentClass Algorithm component to analyze
     * @return constructor annotated with @AutoconfigConstructor if present, null otherwise
     */
    public static Constructor<?> findAutoconfigConstructor(Class<?> algComponentClass){
        var constructors = algComponentClass.getConstructors();
        for(var c: constructors){
            var annotation = c.getAnnotation(AutoconfigConstructor.class);
            if(annotation != null){
                return c;
            }
        }
        return null;
    }

    private static boolean doParamsMatch(Constructor<?> c, Map<String, Class<?>> params) {
        var unfilteredParams = c.getParameters();

        // Skip all params annotated with @ProvidedParam
        List<Parameter> filteredParams = new ArrayList<>(unfilteredParams.length);
        for(var p: unfilteredParams){
            if (!p.isAnnotationPresent(ProvidedParam.class)) {
                filteredParams.add(p);
            }
        }

        if(filteredParams.size() != params.size()){
            log.debug("Constructor {} ignored, args size mismatch, |params|={}", c, params.size());
            return false;
        }

        // Check if all parameters for the current constructor are in our parameter map
        for(var p: filteredParams){
            String cParamName = p.getName();
            var cParamClass = p.getType();
            if(!params.containsKey(cParamName)){
                log.debug("Constructor {} ignored, arg {} does not exist in map {}", c, cParamName, params);
                return false;
            }
            var parsedType = params.get(cParamName);
            if(parsedType == UNKNOWNCLASS.class && !cParamClass.isPrimitive()){
                // Null values do not have a known class, but can be used as any parameter as long as it is not a primitive type
                continue;
            }
            if(!ClassUtils.isAssignable(parsedType, cParamClass)){
                log.debug("Constructor {} ignored, arg {} with type {} is not assignable to {}, map {}", c, cParamName, parsedType, cParamClass, params);
                return false;
            }
        }
        return true;
    }

}
