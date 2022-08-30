package es.urjc.etsii.grafo.autoconfig.irace.params;

public enum ParameterType {

    REAL("r"),
    INTEGER("i"),
    CATEGORICAL("c"),
    ORDINAL("o");

    private final String iraceType;

    ParameterType(String iraceType){
        this.iraceType = iraceType;
    }

    public String iraceType(){
        return this.iraceType;
    }
}
