package es.urjc.etsii.grafo.autoconfig.irace.params;

import es.urjc.etsii.grafo.annotations.*;

import java.util.Arrays;
import java.util.Collection;

public class ComponentParameter {
    private final String name;
    private final ParameterType type;
    private final Class<?> javaType;
    private final Object[] values;

    public ComponentParameter(String name, Class<?> javaType, ParameterType type, Object[] values){
        this.name = name;
        this.javaType = javaType;
        this.type = type;
        this.values = values;
    }

    public ComponentParameter(String name, Class<?> javaType, ParameterType type, Object min, Object max){
        this(name, javaType, type, new Object[]{min, max});
    }

    public static ComponentParameter from(String name, Class<?> javaType, CategoricalParam p){
        var values = checkLength(p.strings());
        return new ComponentParameter(name, javaType, ParameterType.CATEGORICAL, false, values);
    }

    public static ComponentParameter from(String name, Class<?> javaType, OrdinalParam p){
        var values = checkLength(p.strings());
        return new ComponentParameter(name, javaType, ParameterType.ORDINAL, values);
    }

    public static ComponentParameter from(String name, Class<?> javaType, IntegerParam p){
        return new ComponentParameter(name, javaType, ParameterType.INTEGER,  p.min(), p.max());
    }

    public static ComponentParameter from(String name, Class<?> javaType, RealParam p){
        return new ComponentParameter(name, javaType, ParameterType.REAL,  p.min(), p.max());
    }
    public static ComponentParameter from(String name, Class<?> javaType, ProvidedParam p) {
        return new ComponentParameter(name, javaType, ParameterType.PROVIDED, new Object[]{p.type()});
    }

    public static ComponentParameter from(String name, Class<?> javaType, Collection<Class<?>> candidates){
        Class[] names = new Class[candidates.size()];
        var iterator = candidates.iterator();
        for (int i = 0; i < candidates.size(); i++) {
            names[i] = iterator.next();
        }
        return new ComponentParameter(name, javaType, ParameterType.NOT_ANNOTATED, names);
    }

    private static Object[] checkLength(Object[] values){
        if(values.length == 0){
            throw new IllegalArgumentException("Categorical and ordinal params must have at least one value, 0 provided");
        }
        return values;
    }

    public String name(){
        return this.name;
    }

    public boolean recursive(){
        return this.type == ParameterType.NOT_ANNOTATED;
    }

    @Override
    public String toString() {
        return "ComponentParameter{" +
                "name='" + name() + '\'' +
                ", type=" + type +
                ", v=" + Arrays.toString(values) +
                '}';
    }

    public String toParameterString(){
        StringBuilder valString = new StringBuilder();
        valString.append("(");
        for (int i = 0; i < values.length; i++) {
            valString.append('"').append(values[i].toString()).append('"');
            if(i != values.length -1){
                valString.append(", ");
            }
        }
        valString.append("(");
        return String.format("%s\t\t\"%s=\"\t\t%s\t\t%s", name(), name(), type.iraceType(), valString);
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
}
