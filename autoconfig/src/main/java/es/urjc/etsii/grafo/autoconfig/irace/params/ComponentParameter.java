package es.urjc.etsii.grafo.autoconfig.irace.params;

import es.urjc.etsii.grafo.annotations.*;
import es.urjc.etsii.grafo.autoconfig.irace.IraceParameterValueUtil;

import java.util.Arrays;
import java.util.Collection;

import static es.urjc.etsii.grafo.autoconfig.irace.params.ParameterType.*;

public class ComponentParameter {
    private final String name;
    private final ParameterType type;
    private final Class<?> javaType;
    private final Class<?> componentType;
    private final int min;
    private final int max;
    private final Object[] values;

    public static final String NAMEVALUE_SEP = "_";
    public static final String PARAM_SEP = ".";

    public ComponentParameter(String name, Class<?> javaType, ParameterType type, Object[] values) {
        this(name, javaType, javaType, type, values, 1, 1);
    }

    private ComponentParameter(String name, Class<?> javaType, Class<?> componentType, ParameterType type, Object[] values, int min, int max) {
        this.name = name;
        this.javaType = javaType;
        this.componentType = componentType;
        this.type = type;
        this.values = values.clone();
        this.min = min;
        this.max = max;
    }

    public ComponentParameter(String name, Class<?> javaType, ParameterType type, Object min, Object max) {
        this(name, javaType, type, new Object[]{min, max});
    }

    public static ComponentParameter from(String name, Class<?> javaType, CategoricalParam p) {
        var values = resolveStringValues(javaType, p.strings());
        return new ComponentParameter(name, javaType, CATEGORICAL, values);
    }

    public static ComponentParameter from(String name, Class<?> javaType, OrdinalParam p) {
        var values = resolveStringValues(javaType, p.strings());
        return new ComponentParameter(name, javaType, ORDINAL, values);
    }

    public static ComponentParameter from(String name, Class<?> javaType, IntegerParam p) {
        return new ComponentParameter(name, javaType, INTEGER, p.min(), p.max());
    }

    public static ComponentParameter from(String name, Class<?> javaType, RealParam p) {
        return new ComponentParameter(name, javaType, REAL, p.min(), p.max());
    }

    public static ComponentParameter from(String name, Class<?> javaType, ProvidedParam p) {
        return new ComponentParameter(name, javaType, PROVIDED, new Object[0]);
    }

    public static ComponentParameter from(String name, Class<?> javaType, Collection<Class<?>> candidates) {
        Class<?>[] names = new Class<?>[candidates.size()];
        var iterator = candidates.iterator();
        for (int i = 0; i < candidates.size(); i++) {
            names[i] = iterator.next();
        }
        return new ComponentParameter(name, javaType, NOT_ANNOTATED, names);
    }

    public static ComponentParameter combination(String name, Class<?> javaType, Class<?> componentType, Collection<Class<?>> candidates, int min, int max) {
        Class<?>[] values = new Class<?>[candidates.size()];
        var iterator = candidates.iterator();
        for (int i = 0; i < candidates.size(); i++) {
            values[i] = iterator.next();
        }
        return new ComponentParameter(name, javaType, componentType, COMBINATION, values, min, max);
    }

    private static Object[] checkLength(Object[] values) {
        if (values.length == 0) {
            throw new IllegalArgumentException("Categorical and ordinal params must have at least one value, 0 provided");
        }
        return values;
    }

    private static Object[] resolveStringValues(Class<?> javaType, String[] values) {
        if (values.length > 0) {
            return values;
        }
        if (!javaType.isEnum()) {
            return checkLength(values);
        }
        var enumValues = javaType.getEnumConstants();
        var enumNames = new String[enumValues.length];
        for (int i = 0; i < enumValues.length; i++) {
            enumNames[i] = ((Enum<?>) enumValues[i]).name();
        }
        return checkLength(enumNames);
    }

    public String getName() {
        return this.name;
    }

    public boolean recursive() {
        return this.type == NOT_ANNOTATED || this.type == COMBINATION;
    }

    public boolean combination() {
        return this.type == COMBINATION;
    }

    @Override
    public String toString() {
        return "ComponentParameter{" +
                "name='" + getName() + '\'' +
                ", type=" + type +
                ", v=" + Arrays.toString(values) +
                '}';
    }

    public static String toIraceParameterString(String name, ParameterType type, Object[] values, String condition) {
        String iraceType = type.iraceType();
        if (iraceType.length() != 1) {
            throw new IllegalArgumentException("Invalid irace type, must be single char: " + iraceType);
        }

        StringBuilder valString = new StringBuilder();
        valString.append(name).append("\t\t"); // Param name
        valString.append("\"").append(name).append("=\"\t\t"); // Param switch, ie --blabla=
        valString.append(iraceType).append("\t\t");

        valString.append("(");
        for (int i = 0; i < values.length; i++) {
            Object value = values[i];
            if(value instanceof Number){
                valString.append(value);
            } else if (value instanceof String){
                valString.append(IraceParameterValueUtil.encodeCategorical((String) value));
            } else if(value instanceof Class<?> c){
                valString.append('"').append(c.getSimpleName()).append('"');
            } else {
                throw new IllegalArgumentException("values for component parameter contains type that currently is not implemented: " + value.getClass().getSimpleName());
                //valString.append('"').append(value).append('"');
            }

            if (i != values.length - 1) {
                valString.append(", ");
            }
        }
        valString.append(")\t\t");
        if (!condition.isBlank()) {
            valString.append("| ").append(condition);
        }
        return valString.toString();
    }

    public String toIraceParameterString(String name, String condition) {
        if (getType() == NOT_ANNOTATED || getType() == COMBINATION) {
            throw new IllegalArgumentException("Only valid for scalar IRACE parameters, current is " + getType());
        }
        return toIraceParameterString(name, getType(), values, condition);
    }

    public ParameterType getType() {
        return type;
    }

    public Class<?> getJavaType() {
        return javaType;
    }

    public Class<?> getComponentType() {
        return componentType;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public Object[] getValues() {
        return values.clone();
    }

    public ComponentParameter withValues(Object[] values) {
        return new ComponentParameter(name, javaType, componentType, type, values, min, max);
    }
}
