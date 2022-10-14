package es.urjc.etsii.grafo.autoconfig.irace.params;

import es.urjc.etsii.grafo.annotations.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;

import static es.urjc.etsii.grafo.autoconfig.irace.params.ParameterType.*;

public class ComponentParameter {
    private final String name;
    private final ParameterType type;
    private final Class<?> javaType;
    private Object[] values;
    private String condition = "";

    public static final String NAMEVALUE_SEP = "_";
    public static final String PARAM_SEP = ".";

    public ComponentParameter(String name, Class<?> javaType, ParameterType type, Object[] values) {
        this.name = name;
        this.javaType = javaType;
        this.type = type;
        this.values = values;
    }

    public ComponentParameter(String name, Class<?> javaType, ParameterType type, Object min, Object max) {
        this(name, javaType, type, new Object[]{min, max});
    }

    public static ComponentParameter from(String name, Class<?> javaType, CategoricalParam p) {
        var values = checkLength(p.strings());
        return new ComponentParameter(name, javaType, CATEGORICAL, values);
    }

    public static ComponentParameter from(String name, Class<?> javaType, OrdinalParam p) {
        var values = checkLength(p.strings());
        return new ComponentParameter(name, javaType, ORDINAL, values);
    }

    public static ComponentParameter from(String name, Class<?> javaType, IntegerParam p) {
        return new ComponentParameter(name, javaType, INTEGER, p.min(), p.max());
    }

    public static ComponentParameter from(String name, Class<?> javaType, RealParam p) {
        return new ComponentParameter(name, javaType, REAL, p.min(), p.max());
    }

    public static ComponentParameter from(String name, Class<?> javaType, ProvidedParam p) {
        return new ComponentParameter(name, javaType, PROVIDED, new Object[]{p.type()});
    }

    public static ComponentParameter from(String name, Class<?> javaType, Collection<Class<?>> candidates) {
        Class[] names = new Class[candidates.size()];
        var iterator = candidates.iterator();
        for (int i = 0; i < candidates.size(); i++) {
            names[i] = iterator.next();
        }
        return new ComponentParameter(name, javaType, NOT_ANNOTATED, names);
    }

    private static Object[] checkLength(Object[] values) {
        if (values.length == 0) {
            throw new IllegalArgumentException("Categorical and ordinal params must have at least one value, 0 provided");
        }
        return values;
    }

    public String getName() {
        return this.name;
    }

    public boolean recursive() {
        return this.type == NOT_ANNOTATED;
    }

    @Override
    public String toString() {
        return "ComponentParameter{" +
                "name='" + getName() + '\'' +
                ", type=" + type +
                ", v=" + Arrays.toString(values) +
                '}';
    }

    public static String toIraceParameterString(String name, ParameterType type, Object[] values, String parentName, String parentValue, String condition) {
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
                valString.append("'\"").append(value).append("\"'");
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
        valString.append("| ").append(parentName).append(" %in% c(\"").append(parentValue).append("\")");
        if (!condition.isBlank()) {
            valString.append("&& ").append(condition);
        }
        return valString.toString();
    }

    public String toIraceParameterStringNotAnnotated(String name, String parentName, String parentValue, Object[] values){
        if(getType() != NOT_ANNOTATED){
            throw new IllegalArgumentException("Only valid for parameters with type NOT_ANNOTATED");
        }

        // Cannot use this.values[i] as some tree branches may have been pruned for the current node
        // Must use the set given as a parameter
        return toIraceParameterString(name, CATEGORICAL, values, parentName, parentValue, condition);
    }

    public String toIraceParameterString(String name, String parentName, String parentValue) {
        if(getType() == NOT_ANNOTATED){
            throw new IllegalArgumentException("Only valid for parameters with type different to NOT_ANNOTATED, current is " + getType());
        }
        return toIraceParameterString(name, getType(), values, parentName, parentValue, condition);
    }

    public ParameterType getType() {
        return type;
    }

    public Class<?> getJavaType() {
        return javaType;
    }

    public Object[] getValues() {
        return values;
    }

    public void setValues(Object[] values){
        this.values = values;
    }

    public static String toIraceParamName(Deque<String> context) {
        var sb = new StringBuilder();
        for (var iterator = context.descendingIterator(); iterator.hasNext(); ) {
            sb.append(iterator.next());
            if (iterator.hasNext()) {
                sb.append(PARAM_SEP);
            }
        }
        return sb.toString();
    }
}
