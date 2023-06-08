package es.urjc.etsii.grafo.autoconfig;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.annotations.ProvidedParam;
import es.urjc.etsii.grafo.autoconfig.exception.AlgorithmParsingException;
import es.urjc.etsii.grafo.autoconfig.fill.ParameterProvider;
import es.urjc.etsii.grafo.util.DoubleComparator;
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
        for (var provider : paramProviders) {
            if (provider.provides(pType, pName)) {
                return provider.getValue(pType, pName);
            }
        }
        throw new IllegalArgumentException("No providers available for p {type=%s, name=%s}, list %s".formatted(pType, pName, paramProviders));
    }

    private enum MatchType {
        NO_MATCH,
        NAMES_MATCH,
        NAMES_TYPES_MATCH
    }

    private record RankedConstructor<T>(MatchType matchType,
                                        Constructor<T> constructor) implements Comparable<RankedConstructor<T>> {
        public int score() {
            int nParameters = constructor.getParameterCount();
            int score = nParameters + switch (matchType) {
                case NO_MATCH -> 0;
                case NAMES_MATCH -> 100;
                case NAMES_TYPES_MATCH -> 1000;
            };
            return -score;
        }

        @Override
        public int compareTo(RankedConstructor o) {
            return Integer.compare(this.score(), o.score());
        }
    }

    /**
     * Find a constructor method in the target class that accepts the given combination of parameter (name, type) in any order.
     * Autoboxing and widening are allowed, example from int to double, or int to Integer.
     *
     * @param clazz           target class
     * @param mandatoryParams argument map of all parameters that must be used
     * @param optionalParams  argument map of all parameters that might be used if necessary
     * @param <T>             Constructor for class T
     * @return Constructor if found one that matches the given parameters, null if no constructor matches
     */
    @SuppressWarnings("unchecked")
    public static <T> Constructor<T> findConstructor(Class<T> clazz, Map<String, Class<?>> mandatoryParams, List<ParameterProvider> optionalParams) {
        Constructor<T>[] constructors = (Constructor<T>[]) clazz.getConstructors();
        var rankedConstructors = new ArrayList<RankedConstructor<T>>();

        for (var c : constructors) {
            var rc = paramsMatch(c, mandatoryParams, optionalParams);
            rankedConstructors.add(rc);
        }

        Collections.sort(rankedConstructors); // Constructor methods are ordered depending on how well they match the given parameters
        if (rankedConstructors.isEmpty()) {
            log.debug("Failed to to found any constructor method for class {}", clazz.getSimpleName());
            return null;
        } else {
            log.debug("Found constructors {} for mandatoryParams {}, optionalParams {}", rankedConstructors, mandatoryParams, optionalParams);
            var rc = rankedConstructors.get(0);
            switch (rc.matchType) {
                case NO_MATCH:
                    log.debug("Failed to find a matching constructor for class {}; mandatoryParams {}; optionalParams {}", clazz.getSimpleName(), mandatoryParams, optionalParams);
                    return null;
                case NAMES_MATCH:
                    log.debug("Choosing constructor that matches parameter names but not types: {}, for class {}, mandatoryParams {}, optionalParams: {}", rc.constructor, clazz.getSimpleName(), mandatoryParams, optionalParams);
                    return rc.constructor;
                case NAMES_TYPES_MATCH:
                    log.debug("Choosing constructor that matches all parameter names and all types look assignable: {}, for class {}, mandatoryParams {}, optionalParams: {}", rc.constructor, clazz.getSimpleName(), mandatoryParams, optionalParams);
                    return rc.constructor;
            }
        }
        throw new IllegalStateException("Impossible to reach this point");
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

    private static <T> RankedConstructor<T> paramsMatch(Constructor<T> c, Map<String, Class<?>> mandatoryParams, List<ParameterProvider> paramsProviders) {
        var unusedMandatoryParams = new HashSet<>(mandatoryParams.keySet());
        var debugMsgs = new ArrayList<String>();
        boolean allAssignable = true; // If any param cannot be assigned trip this flag
        boolean allNamesMatch = true; // If any param name does not match trip this flag

        // Check if all parameters for the current constructor are either in our parameter map or as provided params
        for (var cp : c.getParameters()) {
            String cpName = cp.getName();
            var cpClass = cp.getType();

            if (mandatoryParams.containsKey(cpName)) {
                var expectedTypeIfMandatory = mandatoryParams.get(cpName);
                unusedMandatoryParams.remove(cpName); // Remove parameter to mark it as used

                // Check that if the parameter has the same name, the types are compatible
                // Even if the parameter does not look assignable, it may still be valid,
                // for example using String to int conversion, or similar transformations. Keep it with a lower priority
                if (!isAssignable(expectedTypeIfMandatory, cpClass)) {
                    debugMsgs.add(String.format("Parameter %s with type %s is NOT assignable from class %s", cpName, cpClass, expectedTypeIfMandatory));
                    allAssignable = false;
                } else {
                    debugMsgs.add(String.format("Parameter %s with type %s is assignable from class %s", cpName, cpClass, expectedTypeIfMandatory));
                }

            } else if (cp.isAnnotationPresent(ProvidedParam.class)) {
                // Value must be in at least one provider
                int countProviders = 0;
                for (var provider : paramsProviders) {
                    if (provider.provides(cpClass, cpName)) {
                        countProviders++;
                    }
                }
                if (countProviders > 1) {
                    debugMsgs.add(String.format("Warning: Parameter %s with type %s is provided my multiple providers: %s", cpName, cpClass, paramsProviders));
                }
                if (countProviders == 0) {
                    debugMsgs.add(String.format("No provider found for parameter %s with type %s", cpName, cpClass));
                    allAssignable = false;
                    allNamesMatch = false;
                }

            } else {
                // Parameter is not in available values, and is not provided by any provider, ignore constructor
                debugMsgs.add("Required arg %s with type %s does not exist".formatted(cpName, cpClass));
                allAssignable = false;
                allNamesMatch = false;
            }
        }

        log.debug("Dbg msgs for params: {}, mandatory params: {}, provided params: {}, constructor: {}", debugMsgs, mandatoryParams, paramsProviders, c);

        if (!unusedMandatoryParams.isEmpty()) {
            log.debug("Constructor {} ignored, unused mandatory args {}, args {}, provided params {}", c, unusedMandatoryParams, mandatoryParams, paramsProviders);
            return new RankedConstructor<>(MatchType.NO_MATCH, c);
        }

        if (allNamesMatch && allAssignable) {
            return new RankedConstructor<>(MatchType.NAMES_TYPES_MATCH, c);
        } else if (allNamesMatch) {
            return new RankedConstructor<>(MatchType.NAMES_MATCH, c);
        } else {
            return new RankedConstructor<>(MatchType.NO_MATCH, c);
        }
    }

    public static boolean isAssignable(Class<?> origin, Class<?> target) {
        // Null values do not have a known class, but can be used as any parameter
        // IF AND ONLY IF the target class is not a primitive type
        if (origin == UNKNOWNCLASS.class && !target.isPrimitive()) {
            return true;
        }

        // If the origin class is number like, it can always be promoted to a Double
        if (ClassUtils.isAssignable(origin, Number.class) && target == Double.class) {
            return true;
        }

        // If the origin class is a string and the target is an enum type, assume the enum contains the string
        if (ClassUtils.isAssignable(origin, String.class) && target.isEnum()) {
            return true;
        }

        // For all remaining types, use Apache Lang3 with autoboxing checks enabled
        return ClassUtils.isAssignable(origin, target, true);
    }

    public static Object prepareParameterValue(Object value, Class<?> target) {
        // If the origin class is number like, transform to correct type only if no conversion loss occurs
        if (value instanceof Number n) {
            return prepareNumericParameterValue(n, target);
        }

        if(value instanceof String s){
            return prepareStringParameterValue(s, target);
        }


        // Return as is if no special handling is implemented
        return value;
    }

    private static Object prepareStringParameterValue(String s, Class<?> target) {
        // If the origin class is a string and the target is number like,
        // try to parse to double and perform appropriate numeric conversion
        if (ClassUtils.isAssignable(target, Number.class)) {
            Double doubleValue = Double.parseDouble(s);
            return prepareNumericParameterValue(doubleValue, target);
        }

        // If the origin class is a string and the target is an enum type, assume the enum contains the string
        if (target.isEnum()) {
            //noinspection unchecked
            return Enum.valueOf((Class<Enum>) target, s);
        }

        // If the origin class is a string and the target is a boolean like value, try to parse as boolean
        if (target == Boolean.class || target == boolean.class) {
            return Boolean.parseBoolean(s);
        }

        // If no specific string conversion exists return as is and hope for the best
        return s;
    }

    public static Object prepareNumericParameterValue(Number value, Class<?> target) {
        double doubleValue = value.doubleValue();
        if (target == Double.class || target == double.class) {
            return doubleValue;
        }
        if (target == Float.class || target == float.class) {
            float floatValue = value.floatValue();
            checkLoss(floatValue, doubleValue);
            return floatValue;
        }
        if (target == Long.class || target == long.class) {
            long longValue = value.longValue();
            checkLoss(longValue, doubleValue);
            return longValue;
        }
        if (target == Integer.class || target == int.class) {
            int intValue = value.intValue();
            checkLoss(intValue, doubleValue);
            return intValue;
        }
        if (target == Short.class || target == short.class) {
            short shortValue = value.shortValue();
            checkLoss(shortValue, doubleValue);
            return shortValue;
        }
        if (target == Byte.class || target == byte.class) {
            byte byteValue = value.byteValue();
            checkLoss(byteValue, doubleValue);
            return byteValue;
        }
        if (target == String.class) {
            return value.toString();
        }
        throw new IllegalArgumentException("Cannot transform numeric value %s to type %s".formatted(value, target));
    }

    public static void checkLoss(double value, double reference) {
        if (!DoubleComparator.equals(value, reference)) {
            throw new IllegalArgumentException("Loss of precision detected with numbers %s and %s".formatted(value, reference));
        }
    }

}
