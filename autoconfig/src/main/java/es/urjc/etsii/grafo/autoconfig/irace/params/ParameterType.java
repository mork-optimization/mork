package es.urjc.etsii.grafo.autoconfig.irace.params;

import java.util.EnumSet;

public enum ParameterType {

    // irace
    REAL("r"),
    INTEGER("i"),
    CATEGORICAL("c"),
    ORDINAL("o"),

    PROVIDED("provided"),
    NOT_ANNOTATED("notannotated");

    private static final EnumSet<ParameterType> iraceTypes = EnumSet.of(REAL, INTEGER, CATEGORICAL, ORDINAL);

    private final String key;

    ParameterType(String key){
        this.key = key;
    }

    public String iraceType(){
        return this.key;
    }

    public boolean isIraceParam(){
        return iraceTypes.contains(this);
    }
}
