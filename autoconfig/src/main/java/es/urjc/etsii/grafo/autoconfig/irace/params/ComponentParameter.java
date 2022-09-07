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
        return new ComponentParameter(name, javaType, CATEGORICAL, false, values);
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

    public String name() {
        return this.name;
    }

    public boolean recursive() {
        return this.type == NOT_ANNOTATED;
    }

    @Override
    public String toString() {
        return "ComponentParameter{" +
                "name='" + name() + '\'' +
                ", type=" + type +
                ", v=" + Arrays.toString(values) +
                '}';
    }

    public static String toIraceParameterString(String name, String type, String[] values, String parentName, String parentValue, String condition) {
        if (type.length() != 1) {
            throw new IllegalArgumentException("Invalid irace type, must be single char: " + type);
        }

        StringBuilder valString = new StringBuilder();
        valString.append(name).append("\t\t"); // Param name
        valString.append("\"").append(name).append("=\"\t\t"); // Param switch, ie --blabla=
        valString.append(type).append("\t\t");

        valString.append("(");
        for (int i = 0; i < values.length; i++) {
            valString.append('"').append(values[i]).append('"');
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

    public String toIraceParameterString(String name, String parentName, String parentValue) {
        String iraceType;
        String[] iraceValues = new String[this.values.length];
        if(getType() == NOT_ANNOTATED){
            iraceType = CATEGORICAL.iraceType();
            for (int i = 0; i < this.values.length; i++) {
                iraceValues[i] = ((Class<?>) this.values[i]).getSimpleName();
            }
            Arrays.sort(iraceValues);
        } else {
            iraceType = getType().iraceType();
            for (int i = 0; i < this.values.length; i++) {
                iraceValues[i] = this.values[i].toString();
            }
        }
        return toIraceParameterString(name, iraceType, iraceValues, parentName, parentValue, condition);
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
