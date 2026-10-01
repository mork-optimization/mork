package es.urjc.etsii.grafo.mreflp.alg;

public enum LMLSVariant {
    LMLS("LMLS"), RANDOM("LMLS1"), GREEDY("LMLS2"), WITHOUT_TABU("LMLS-WithoutTabu"),
    WITHOUT_SWAP("LMLS-WithoutSwap"), DIRECT_ONE_MOVE("LMLS3"), DIRECT_SWAP("LMLS4");
    private final String paperName;
    LMLSVariant(String paperName) { this.paperName = paperName; }
    public String paperName() { return paperName; }
    public boolean learns() { return this != RANDOM && this != GREEDY; }
}
