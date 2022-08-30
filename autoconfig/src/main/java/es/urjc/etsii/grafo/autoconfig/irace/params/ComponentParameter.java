package es.urjc.etsii.grafo.autoconfig.irace.params;

import es.urjc.etsii.grafo.annotations.CategoricalParam;
import es.urjc.etsii.grafo.annotations.IntegerParam;
import es.urjc.etsii.grafo.annotations.OrdinalParam;
import es.urjc.etsii.grafo.annotations.RealParam;

import java.util.Arrays;
import java.util.Collection;

public class ComponentParameter {
    private final String name;
    private final ParameterType type;
    private final boolean recursive;
    private final Object[] values;

    public ComponentParameter(String name, ParameterType type, boolean recursive, Object[] values){
        this.name = name;
        this.type = type;
        this.values = values;
        this.recursive = recursive;
    }

    public ComponentParameter(String name, ParameterType type, boolean recursive, Object min, Object max){
        this(name, type, recursive, new Object[]{min, max});
    }

    public static ComponentParameter from(String name, CategoricalParam p){
        var values = checkLength(p.strings());
        return new ComponentParameter(name, ParameterType.CATEGORICAL, false, values);
    }

    public static ComponentParameter from(String name, OrdinalParam p){
        var values = checkLength(p.strings());
        return new ComponentParameter(name, ParameterType.ORDINAL, false, values);
    }

    public static ComponentParameter from(String name, IntegerParam p){
        return new ComponentParameter(name, ParameterType.INTEGER, false, p.min(), p.max());
    }

    public static ComponentParameter from(String name, RealParam p){
        return new ComponentParameter(name, ParameterType.REAL, false, p.min(), p.max());
    }

    public static ComponentParameter from(String name, Collection<Class<?>> candidates){
        String[] names = new String[candidates.size()];
        var iterator = candidates.iterator();
        for (int i = 0; i < candidates.size(); i++) {
            names[i] = iterator.next().getSimpleName();
        }
        return new ComponentParameter(name, ParameterType.CATEGORICAL, true, names);
    }

    private static Object[] checkLength(Object[] values){
        if(values.length == 0){
            throw new IllegalArgumentException("Categorical and ordinal params must have at least one value, 0 provided");
        }
        return values;
    }

    @Override
    public String toString() {
        return "ComponentParameter{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", recursive=" + recursive +
                ", values=" + Arrays.toString(values) +
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
        return String.format("%s\t\t\"%s=\"\t\t%s\t\t%s", name, name, type.iraceType(), valString);
    }

    public String getName() {
        return name;
    }

    public ParameterType getType() {
        return type;
    }

    public Object[] getValues() {
        return values;
    }

    public boolean isRecursive() {
        return recursive;
    }
}
