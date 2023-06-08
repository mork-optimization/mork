package es.urjc.etsii.grafo.autoconfig;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.annotations.ProvidedParam;
import es.urjc.etsii.grafo.autoconfig.exception.AlgorithmParsingException;
import es.urjc.etsii.grafo.autoconfig.fill.ParameterProvider;
import org.apache.commons.lang3.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.*;

/**
 * Magic (based on reflection) util methods to create instances of algorithm components at runtime
 */
public class AlgorithmBuilderUtil {
    private static final Logger log = LoggerFactory.getLogger(AlgorithmBuilderUtil.class);

    private record UNKNOWNCLASS() {
    }

    /**
     * Build algorithm component given a set of parameters
     *
     * @param clazz Algorithm component class
     * @param args  arguments for the constructor
     * @return instance if class built with the given params
     */
    public static Object build(Class<?> clazz, Map<String, Object> args, List<ParameterProvider> paramProviders) {
        Map<String, Class<?>> argTypes = new HashMap<>();
        args.forEach((k, v) -> argTypes.put(k, v == null ? UNKNOWNCLASS.class : v.getClass()));
        var constructor = findConstructor(clazz, argTypes, paramProviders);
        if (constructor == null) {
            throw new AlgorithmParsingException(String.format("Failed to find constructor method in class %s for params %s, types %s", clazz.getSimpleName(), args, argTypes));
        }
        var cParams = constructor.getParameters();
        var params = new Object[cParams.length];

        for (int i = 0; i < cParams.length; i++) {
            var nextParamName = cParams[i].getName();
            var nextParamType = cParams[i].getType();
            // Either the value is in our map
            if (args.containsKey(nextParamName)) {
                var value = args.get(nextParamName);
                params[i] = prepareParameterValue(value, nextParamType);

            } else {
                // Or it is a provided value
                params[i] = getProvidedValue(cParams[i], paramProviders);
            }
        }
        try {
            return constructor.newInstance(params);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object getProvidedValue(Parameter p, List<ParameterProvider> paramProviders) {
        if (!p.isAnnotationPresent(ProvidedParam.class)) {
            throw new IllegalArgumentException(String.format("Parameter %s not annotated with @ProvidedParam", p));
        }
        return getProvidedValue(p.getType(), p.getName(), paramProviders);
    }

    public static Object getProvidedValue(Class<?> pType, String pName, List<ParameterProvider> paramProviders) {
        for(var provider: paramProviders){
            if(provider.provides(pType, pName)){
                return provider.getValue(pType, pName);
            }
        }
        throw new IllegalArgumentException("No providers available for p {type=%s, name=%s}, list %s".formatted(pType, pName, paramProviders));
    }


    /**
     * Find a constructor method in the target class that accepts the given combination of parameter (name, type) in any order.
     * Autoboxing and widening are allowed, example from int to double, or int to Integer.
     *
     * @param clazz target class
     * @param mandatoryParams  argument map of all parameters that must be used
     * @param optionalParams  argument map of all parameters that might be used if necessary
     * @param <T>   Constructor for class T
     * @return Constructor if found one that matches the given parameters, null if no constructor matches
     */
    @SuppressWarnings("unchecked")
    public static <T> Constructor<T> findConstructor(Class<T> clazz, Map<String, Class<?>> mandatoryParams, List<ParameterProvider> optionalParams) {
        var constructors = clazz.getConstructors();

        for (var c : constructors) {
            boolean matches = doParamsMatch(c, mandatoryParams, optionalParams);
            if (matches) {
                log.debug("Found matching constructor {} for mandatoryParams {}", c, mandatoryParams);
                return (Constructor<T>) c;
            }
        }
        log.debug("Failed to to found a matching constructor for class {} and mandatoryParams {}, detected constructors: {}", clazz.getSimpleName(), mandatoryParams, constructors);
        return null;
    }

    /**
     * Analyze an algorithm component class to find which constructor is annotated with @AutoconfigConstructor
     *
     * @param clazz Algorithm component to analyze
     * @return constructor annotated with @AutoconfigConstructor if present, null otherwise
     */
    @SuppressWarnings("unchecked")
    public static <T> Constructor<T> findAutoconfigConstructor(Class<T> clazz) {
        Constructor<T>[] constructors = (Constructor<T>[]) clazz.getConstructors();
        var autoconfigConstructors = new ArrayList<Constructor<T>>();
        for (var c : constructors) {
            var annotation = c.getAnnotation(AutoconfigConstructor.class);
            if (annotation != null) {
                autoconfigConstructors.add(c);
            }
        }
        switch (autoconfigConstructors.size()) {
            case 0 -> {
                log.debug("No constructor annotated with @AutoconfigConstructor found for class {}", clazz.getSimpleName());
                return null;
            }
            case 1 -> {
                log.debug("Found constructor annotated with @AutoconfigConstructor for class {}: {}", clazz.getSimpleName(), autoconfigConstructors.get(0));
                return autoconfigConstructors.get(0);
            }
            default -> {
                log.debug("Found multiple constructors annotated with @AutoconfigConstructor for class {}: {}", clazz.getSimpleName(), autoconfigConstructors);
                throw new IllegalArgumentException("Multiple constructors annotated with @AutoconfigConstructor found for class " + clazz.getSimpleName());
            }
        }
    }

    private static boolean doParamsMatch(Constructor<?> c, Map<String, Class<?>> mandatoryParams, List<ParameterProvider> paramsProviders) {
        Set<String> unusedMandatoryParams = new HashSet<>(mandatoryParams.keySet());

        // Check if all parameters for the current constructor are either in our parameter map or as provided params
        for (var cp : c.getParameters()) {
            String cpName = cp.getName();
            var cpClass = cp.getType();

            if(mandatoryParams.containsKey(cpName)){
                var expectedTypeIfMandatory = mandatoryParams.get(cpName);

                // Check that if the parameter has the same name, the types are compatible
                if(!isAssignable(expectedTypeIfMandatory, cpClass)){
                    log.debug("Constructor {} ignored, arg {} with type {} is not assignable to {}, params {}; provided params {}", c, cpName, expectedTypeIfMandatory, cpClass, mandatoryParams, paramsProviders);
                    return false;
                }
                // Parameter is valid, mark it
                unusedMandatoryParams.remove(cpName);

            } else if(cp.isAnnotationPresent(ProvidedParam.class)){
                // Value must be in at least one provider
                int countProviders = 0;
                for(var provider: paramsProviders){
                    if (provider.provides(cpClass, cpName)) {
                        countProviders++;

                    }
                }
                if(countProviders > 1){
                    log.debug("NOTE: Parameter type {}, name {} is provided my multiple: {}", cpClass, cpName, paramsProviders);
                }
                if(countProviders == 0){
                    return false;
                }

            } else {
                // Parameter is not in available values, and is not provided by any provider, ignore constructor
                log.debug("Constructor {} ignored, arg {} does not exist either in params {}; provided params {}", c, cpName, mandatoryParams, paramsProviders);
                return false;
            }
        }

        if(!unusedMandatoryParams.isEmpty()){
            log.debug("Constructor {} ignored, unused mandatory args {}, args {}, provided params {}", c, unusedMandatoryParams, mandatoryParams, paramsProviders);
            return false;
        }
        log.debug("Constructor {} matches", c);
        return true;
    }

    public static boolean isAssignable(Class<?> origin, Class<?> target){
        // Null values do not have a known class, but can be used as any parameter
        // IF AND ONLY IF the target class is not a primitive type
        if (origin == UNKNOWNCLASS.class && !target.isPrimitive()){
            return true;
        }

        // If the origin class is number like, it can always be promoted to a Double
        if(ClassUtils.isAssignable(origin, Number.class) && target == Double.class){
            return true;
        }

        // If the origin class is a string and the target is an enum type, assume the enum contains the string
        if(ClassUtils.isAssignable(origin, String.class) && target.isEnum()){
            return true;
        }

        // For all remaining types, use Apache Lang3 with autoboxing checks enabled
        return ClassUtils.isAssignable(origin, target, true);
    }

    public static Object prepareParameterValue(Object value, Class<?> target){
        // If the origin class is number like, promote to Double
        if(value instanceof Number n && target == Double.class){
            return n.doubleValue();
        }

        // If the origin class is a string and the target is an enum type, assume the enum contains the string
        if(value instanceof String s && target.isEnum()){
            //noinspection unchecked
            return Enum.valueOf((Class<Enum>)target, s);
        }

        // Return as is if no special handling is implemented
        return value;
    }
}
