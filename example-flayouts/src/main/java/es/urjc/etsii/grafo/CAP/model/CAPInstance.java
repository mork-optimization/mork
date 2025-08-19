package es.urjc.etsii.grafo.CAP.model;

import es.urjc.etsii.grafo.io.Instance;

import java.util.Comparator;

public class CAPInstance extends Instance {

    // Problem parameters
    public int     nN;
    public int nM = -1; // nRows, parsed from instance file
    public int[] L;
    public int[][]  W;

    public static Comparator<CAPInstance> comparator = Comparator.comparing(CAPInstance::nFacilities);

    // Algorithm parameters
    int     NI = 1;             // Number of Islands
    int     NM = 1;             // Number of Algorithms in each Island
    String  MM = "none";        // Migration Strategy: none, ring, fully, master
    int     R  = 1;             // Best solutions migrates at every N·R iterations
    int     S  = -1;            // Logs are saved at every S iterations
    double  TC = 1.0;           // Termination Criteria

    // Metaheuristic parameters
    int     NC = 1;             // Number of constructions
    String  MC = "random";      // Construction: random, greedy11, greedy12, greedy21, greedy22

    // DO NOT USE
    // double  AG = 0.75;          // Alpha coefficient controlling the greediness: 0-purely random, 1-purely greedy

    String  ME = "extended";    // Exploration: vinser, vinter, einser, einter, extended
    String  MS = "grasp";       // Search: grasp, ils, vnd, bvns, gvns, sa
    int     V0 = 1;             // Perturbation procedure for ILS
    int     V1 = 1;             // Shake procedure for VNS (Basic or General)
    int     V2 = 1;             // Exploration sequence of LS's in VND

    // Tabu List specific parameters
    int     NT = 0;             // Maximum number of elements in the Tabu list

    // GRASP specific parameters
    int     KW = 20;            // Number of iterations of the warming phase
    int     Q  = 2;             // Maximum number of dev away from the best

    // VNS specific parameters
    double  B1 = 0.1;           // Beta coefficient for VNS (Basic or General)
    double  B2 = 0.1;           // Beta coefficient for VND

    // SA specific parameters
    int     NL = 100;           // Number of iterations in each cycle
    double  AL = 0.99;          // Cooling speed

    public CAPInstance(String name){
        super(name);

    }

    @Override
    public int compareTo(Instance other) {
        return comparator.compare(this, (CAPInstance) other);
    }

    public Integer nFacilities(){
        assert this.L.length == this.W.length;
        return this.L.length;
    }


}
