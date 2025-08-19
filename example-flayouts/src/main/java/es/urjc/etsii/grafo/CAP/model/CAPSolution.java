package es.urjc.etsii.grafo.CAP.model;

import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.util.ArrayUtil;
import es.urjc.etsii.grafo.util.CollectionUtil;
import es.urjc.etsii.grafo.util.DoubleComparator;
import es.urjc.etsii.grafo.util.random.RandomManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Math.abs;
import static java.lang.Math.floor;

public class CAPSolution extends Solution<CAPSolution, CAPInstance> {

    // When java compiler detects that variable may not be initialized assign this value,
    // and check before return that the value is NOT equal to this to detect bgs
    private static final int NOT_INITIALIZED = Integer.MIN_VALUE / 2;

    private static Logger log = LoggerFactory.getLogger(CAPSolution.class);

    private final CAPInstance data;
    List<Integer>[] rho;
    int[] x;
    int obj = 0;

    /**
     * Initialize solution from instance
     *
     * @param data
     */
    public CAPSolution(CAPInstance data) {
        super(data);
        this.data = data;
        rho = (List<Integer>[]) new ArrayList[data.nM]; // (data.nM,row)
        for (int i = 0; i < rho.length; i++) {
            rho[i] = new ArrayList<>();
        }
        x = new int[data.nN]; //  List<Integer>(data.nN,0);
        obj = 0;
    }

    /**
     * Clone constructor
     *
     * @param s Solution to clone
     */
    public CAPSolution(CAPSolution s) {
        super(s);
        this.obj = s.obj;
        this.x = s.x.clone();
        this.data = s.data;
        this.rho = new List[s.rho.length];
        for (int i = 0; i < s.rho.length; i++) {
            this.rho[i] = new ArrayList<>(s.rho[i]);
        }
    }


    @Override
    public CAPSolution cloneSolution() {
        // You do not need to modify this method
        // Call clone constructor
        return new CAPSolution(this);
    }

    /**
     * Get the current solution score.
     * The difference between this method and recalculateScore is that
     * this result can be a property of the solution, or cached,
     * it does not have to be calculated each time this method is called
     *
     * @return current solution score as double
     */
    public double getScore() {
        return this.obj;
    }

    /**
     * Recalculate solution score from scratch, using the problem objective function.
     * The difference between this method and getScore is that we must recalculate the score from scratch,
     * without using any cache/shortcuts.
     * This method will be used to validate the correct behaviour of the getScore() method, and to help catch
     * bugs or mistakes when changing incremental score calculation.
     * DO NOT UPDATE CACHES IN THIS METHOD / MAKE SURE THIS METHOD DOES NOT HAVE SIDE EFFECTS
     * DO NOT UPDATE CACHES IN THIS METHOD / MAKE SURE THIS METHOD DOES NOT HAVE SIDE EFFECTS
     * and once more
     * DO NOT UPDATE CACHES IN THIS METHOD / MAKE SURE THIS METHOD DOES NOT HAVE SIDE EFFECTS
     *
     * @return current solution score as double
     */
    public double recalculateScore() {
        var copy = this.cloneSolution();
        copy.Evaluate();
        return copy.obj;
    }

    /**
     * Generate a string representation of this solution. Used when printing progress to console,
     * show as minimal info as possible
     *
     * @return Small string representing the current solution (Example: id + score)
     */
    @Override
    public String toString() {
        return "%s -> %s".formatted(this.obj / 4.0, Arrays.deepToString(this.rho));
    }

    // START helper methods that do not exist in Java, map them from C equivalents
    static void rndShuffle(List<?> data) {
        CollectionUtil.shuffle(data);
    }

    static boolean rndBool() {
        return RandomManager.getRandom().nextBoolean();
    }

    static int rndInt(int min, int max) {
        return RandomManager.getRandom().nextInt(min, max + 1);
    }

    static double rndDbl(double min, double max) {
        return RandomManager.getRandom().nextDouble(min, max);
    }

    private static class pair<T1, T2> {
        public T1 first; // final modifiers to fields added only temporarily to fix bugs
        public T2 second;


        protected pair(T1 first, T2 second) {
            this.first = first;
            this.second = second;
        }

        public static <T1, T2> pair<T1, T2> of(T1 t1, T2 t2) {
            return new pair<>(t1, t2);
        }

        public static <T1, T2, T3> pair<T1, pair<T2, T3>> of(T1 t1, T2 t2, T3 t3) {
            return new pair<>(t1, new pair<>(t2, t3));
        }

        @Override
        public String toString() {
            return "(" + first + ", " + second + ')';
        }
    }

    // END helper methods


//******************************************************************************
// Eval Operators: Full solution and full generic move
//******************************************************************************

    public void Evaluate() {

        // Calculate the centers of all the facilities
        CalculateCenters();

        // Evaluates de solution
        obj = 0;
        for (int i = 0; i < data.nM; i++) {
            for (int j = 0; j < rho[i].size(); j++) {
                int u = rho[i].get(j);
                for (int l = j + 1; l < rho[i].size(); l++) {
                    int v = rho[i].get(l);
                    obj += data.W[u][v] * (x[v] - x[u]);
                }
                for (int k = i + 1; k < data.nM; k++) {
                    for (int l = 0; l < rho[k].size(); l++) {
                        int v = rho[k].get(l);
                        obj += data.W[u][v] * abs(x[v] - x[u]);
                    }
                }
            }
        }

    }

    public int EvaluateMove() {

        int h = 0;

        // Calculate the centers of all the facilities
        CalculateCenters();

        // Evaluates de solution
        int objaux = 0;
        for (int i = 0; i < data.nM; i++) {
            for (int j = 0; j < rho[i].size(); j++) {
                int u = rho[i].get(j);
                for (int l = j + 1; l < rho[i].size(); l++) {
                    int v = rho[i].get(l);
                    objaux += data.W[u][v] * (x[v] - x[u]);
                }
                for (int k = i + 1; k < data.nM; k++) {
                    for (int l = 0; l < rho[k].size(); l++) {
                        int v = rho[k].get(l);
                        objaux += data.W[u][v] * abs(x[v] - x[u]);
                    }
                }
            }
        }

        // Calculates cost difference
        h = objaux - obj;

        return h;
    }

    public void CalculateCenters() {

        for (int i = 0; i < data.nM; i++) {
            int xaux = 0;
            for (int u : rho[i]) {
                xaux += data.L[u] / 2;
                x[u] = xaux;
                xaux += data.L[u] / 2;
            }
        }

    }

//******************************************************************************
// Neighborhood exploration strategies
//******************************************************************************

    public boolean Explore(String ME) {

        boolean improved = switch (ME) {
            // Strategy is first improvement, best improvement or hybrid
            // Ignorar todos los que no están marcados
            // Todos los metodos de explore ejecutan un unico movimiento
            case "exc_b" -> ExploreExchange(0);
            case "exc_h" -> ExploreExchange(1);             //
            case "exc_f" -> ExploreExchange(2);             //
            case "exc_bi" -> ExploreExchangeInc(0);         // Exchange Best Incremental, USAR
            case "exc_hi" -> ExploreExchangeInc(1);         // Exchange Hybrid Incremental, USAR
            case "exc_fi" -> ExploreExchangeInc(2);         // Exchange First Incremental, USAR, NOP
            case "exc_Hb" -> ExploreExchangeHorizontalBest();       //
            case "exc_Hbi" -> ExploreExchangeHorizontalBestInc();   //
            case "exc_Vb" -> ExploreExchangeVerticalBest();
            case "exc_Vbi" -> ExploreExchangeVerticalBestInc();
            case "ins_b" -> ExploreInsert(0);
            case "ins_h" -> ExploreInsert(1);

            case "ins_f" -> ExploreInsert(2);                // Equivale al insert ins_fi, USAR
            case "ins_bi" -> ExploreInsertInc(0);            // USAR
            case "ins_hi" -> ExploreInsertInc(1);            // USAR

            case "ins_Hb" -> ExploreInsertHorizontalBest();
            case "ins_Hbi" -> ExploreInsertHorizontalBestInc();
            case "ins_Vb" -> ExploreInsertVerticalBest();
            case "ins_Vbi" -> ExploreInsertVerticalBestInc();
            case "ext_b" -> ExploreExtendedBest();
            case "ext_bbi" -> ExploreExtendedBestBestInc();
            case "ext_bhi" -> ExploreExtendedBestHybridInc();
            case "ext_bfi" -> ExploreExtendedBestFirstInc();
            case "ext_hbi" -> ExploreExtendedHybridBestInc();
            case "ext_hhi" -> ExploreExtendedHybridHybridInc();
            case "ext_hfi" -> ExploreExtendedHybridFirstInc();
            case "ext_fbi" -> ExploreExtendedFirstBestInc();
            case "ext_fhi" -> ExploreExtendedFirstHybridInc();      // Exchange first, hybrid del insert, USAR
            case "ext_ffi" -> ExploreExtendedFirstFirstInc();
            default -> throw new RuntimeException("Wrong neighborhood exploration method.");
        };

        return improved;
    }

//******************************************************************************
// Shake Operators for VNS (Basic or General): Shake 1, 2, 3, 4, 5, 6
//******************************************************************************

    public void Shake(int ks) {

        if (data.V1 == 1) Shake1(ks, 1); // S1EXC      //                  Exchange
        else if (data.V1 == 2) Shake2(ks, 1); // S2EXC      // Horizontal       Exchange
        else if (data.V1 == 3) Shake3(ks, 1); // NOUSA      // Vertical-j       Exchange
        else if (data.V1 == 4) Shake4(ks, 1); // NOUSA      // Vertical-aligned Exchange
        else if (data.V1 == 5) Shake5(ks, 1); // S3EXC      // Vertical-contact Exchange

        else if (data.V1 == 6) Shake1(ks, 2); // S1INS      //                  Insert
        else if (data.V1 == 7) Shake2(ks, 2); // S2INS      // Horizontal       Insert
        else if (data.V1 == 8) Shake3(ks, 2); // NOUSA      // Vertical-j       Insert
        else if (data.V1 == 9) Shake4(ks, 2); // NOUSA      // Vertical-aligned Insert
        else if (data.V1 == 10) Shake5(ks, 2); // S3EXC      // Vertical-contact Insert
        else {
            throw new RuntimeException("Wrong Shake method.");
        }
        Evaluate();

    }
    // END solution.cpp

    // START constructives.cpp
    public void ConstructRandom(int c) {

        // Generates a random ordering of facilities (FL)
        List<Integer> FL = new ArrayList<>();
        for (int u = 0; u < data.nN; u++) FL.add(u);
        rndShuffle(FL);

        // Generates the size of each row
        int[] rhosize = new int[data.nM];
        Arrays.fill(rhosize, c);

        int Nleft = data.nN - c * data.nM;
        while (Nleft > 0) {
            rhosize[rndInt(0, data.nM - 1)]++;
            Nleft--;
        }

        // Adds the rest of facilities to fill the rows
        for (int i = 0; i < data.nM; i++) {
            for (int j = 0; j < rhosize[i]; j++) {
                rho[i].add(FL.get(FL.size() - 1));
                FL.remove(FL.size() - 1);
            }
        }
    }

    // Adds a facility (randomly selected from FL) to the end of the shortest row
//---------------------------------------------------------------------------
    public void ConstructBalanced() {

        // Generates a random ordering of facilities (FL)
        int[] FL = new int[data.nN];
        for (int u = 0; u < FL.length; u++) FL[u] = u;
        ArrayUtil.shuffle(FL);

        // Adds the rest of facilities selecting the shortest row at each iteration
        int[] Lx = new int[data.nM];
        for (int u : FL) {
            int i = ArrayUtil.minIndex(Lx);
            rho[i].add(u);
            Lx[i] += data.L[u];
        }
    }

    // Adds a facility (selected from FL according to the Greedy function) to the end of the shortest row
//---------------------------------------------------------------------------------------------------
    public void ConstructGreedyA(int greedy, double alpha) {

        // Generates a random ordering of facilities (FL)
        List<Integer> FL = new ArrayList<>(data.nN); // TODO review why this one uses nN and the one later uses nM, possible bug
        for (int i = 0; i < data.nN; i++) {
            FL.add(0);
        }
        for (int u = 0; u < FL.size(); u++) FL.set(u, u);
        rndShuffle(FL);

        // Adds one facility to each row at random
        for (int i = 0; i < data.nM; i++) {
            int u = FL.get(FL.size() - 1);
            FL.remove(FL.size() - 1);
            rho[i].add(u);
        }

        // Adds facilities according to the Greedy function value
        int c;
        int[] Lx = new int[data.nM];

        while (!FL.isEmpty()) {
            rndShuffle(FL);
            int i = ArrayUtil.minIndex(Lx);
            if (alpha == 0.0) alpha = rndDbl(0.0, 1.0);
            if (greedy == 1) c = getGreedyA1(FL, i, alpha); //G-R
            else c = getGreedyA2(FL, i, alpha); //R-G
            int u = FL.get(c);
            FL.remove(c);
            if (!rho[i].isEmpty())
                x[u] = x[rho[i].get(rho[i].size() - 1)] + (data.L[rho[i].get(rho[i].size() - 1)] + data.L[u]) / 2;
            else x[u] = data.L[u] / 2;
            Lx[i] = data.L[u];
            rho[i].add(u);
        }

    }

    public int getGreedyA1(List<Integer> FL, int i, double alpha) {

        int candidate = NOT_INITIALIZED;

        int gmin = Integer.MAX_VALUE;
        int gmax = Integer.MIN_VALUE;

        int[] g = new int[FL.size()];
        for (int c = 0; c < FL.size(); c++) {
            g[c] = EvaluateGreedy(FL.get(c), i);
            if (g[c] < gmin) gmin = g[c];
            if (g[c] > gmax) gmax = g[c];
        }
        double th = gmin + alpha * (gmax - gmin);
        for (int c = 0; c < FL.size(); c++)
            if (g[c] >= th) {
                candidate = c;
                break;
            }

        return throwIfNotInitialized(candidate);
    }

    public int getGreedyA2(List<Integer> FL, int i, double alpha) {

        int candidate = NOT_INITIALIZED;

        int s = (int) floor(alpha * FL.size());
        if (s == 0) s = 1;
        int gmax = Integer.MIN_VALUE;

        for (int c = 0; c < s; c++) {
            int g = EvaluateGreedy(FL.get(c), i);
            if (g > gmax) {
                gmax = g;
                candidate = c;
            }
        }

        return throwIfNotInitialized(candidate);
    }

    private static int throwIfNotInitialized(int candidate) {
        if (candidate == NOT_INITIALIZED) {
            throw new IllegalStateException("Variable not initialized");
        }
        return candidate;
    }

    // Adds a facility (selected from FL according to the Greedy function) to its best position in the layout
//-------------------------------------------------------------------------------------------------------
    public void ConstructGreedyB(int greedy, double alpha) {

        // Generates a random ordering of facilities (Candidate List of facilities, FL)
        List<Integer> FL = new ArrayList<>(data.nN);
        for (int u = 0; u < data.nN; u++) {
            FL.add(u);
        }
        rndShuffle(FL);

// // Adds a random facility to a random row
//    int u = FL.get(FL.size()-1);
//    FL.remove(FL.size()-1);
//    rho[rndInt(0,data.nM-1)].add(u);

        // Adds one facility to each row at random
        int u;
        for (int i = 0; i < data.nM; i++) {
            u = FL.get(FL.size() - 1);
            FL.remove(FL.size() - 1);
            rho[i].add(u);
        }

        // Adds facilities according to the Greedy function value
        pair<Integer, pair<Integer, Integer>> gre;
        while (!FL.isEmpty()) {
            CalculateCenters();
            rndShuffle(FL);
            if (alpha == 0.0) alpha = rndDbl(0.0, 1.0);
            if (greedy == 1) gre = getGreedyB1(FL, alpha); //G-R
            else gre = getGreedyB2(FL, alpha); //R-G
            int c = gre.first;
            int i = gre.second.first;
            int j = gre.second.second;
            u = FL.get(c);
            FL.remove(c);
            rho[i].add(j, u);
        }

    }

    public pair<Integer, pair<Integer, Integer>> getGreedyB1(List<Integer> CL, double alpha) {

        int gmin = Integer.MAX_VALUE;
        int gmax = Integer.MIN_VALUE;

        ArrayList<pair<Integer, pair<Integer, Integer>>> g = new ArrayList<>();
        pair<Integer, pair<Integer, Integer>> gre = null;
        for (int c : CL) {
            gre = pair.of(Integer.MAX_VALUE, NOT_INITIALIZED, NOT_INITIALIZED);
            for (int i = 0; i < data.nM; i++) {
                for (int j = 0; j < rho[i].size() + 1; j++) {
                    rho[i].add(j, c);
                    int h = EvaluateMove();
                    rho[i].remove(j);
                    if (h < gre.first) {
                        gre = pair.of(h, i, j);
                    } else if (h == gre.first) {
                        if (i == gre.second.first && rndBool()) {
                            gre = pair.of(h, i, j);
                        } else {
                            int ui, uib, Li, Lib;
                            if (rho[i].isEmpty()) Li = 0;
                            else {
                                ui = rho[i].get(rho[i].size() - 1);
                                Li = x[ui] + data.L[ui] / 2;
                            }
                            if (rho[gre.second.first].isEmpty()) Lib = 0;
                            else {
                                uib = rho[gre.second.first].get(rho[gre.second.first].size() - 1);
                                Lib = x[uib] + data.L[uib] / 2;
                            }
                            if (Li < Lib || (Li == Lib && rndBool())) {
                                gre = pair.of(h, i, j);
                            }
                        }
                    }
                }
            }
            if (gre.first < gmin) gmin = gre.first;
            if (gre.first > gmax) gmax = gre.first;
            g.add(gre);
        }

        List<Integer> RCL = new ArrayList<>();
        double th = gmax - alpha * (gmax - gmin);
        for (int c = 0; c < CL.size(); c++) if (g.get(c).first <= th) RCL.add(c);
        int candidate = RCL.get(rndInt(0, (RCL.size() - 1)));
        gre = pair.of(candidate, g.get(candidate).second);

        return gre;
    }

    public pair<Integer, pair<Integer, Integer>> getGreedyB2(List<Integer> CL, double alpha) {

        int s = (int) floor(alpha * CL.size());
        if (s == 0) s = 1;
        int gmin = Integer.MAX_VALUE;

        pair<Integer, pair<Integer, Integer>> gre = null,
                gremin = null;
        for (int c = 0; c < s; c++) {
            gre = pair.of(Integer.MAX_VALUE, NOT_INITIALIZED, NOT_INITIALIZED);
            for (int i = 0; i < data.nM; i++) {
                for (int j = 0; j < rho[i].size() + 1; j++) {
                    rho[i].add(j, CL.get(c));
                    int h = EvaluateMove();
                    rho[i].remove(j);
                    if (h < gre.first) {
                        gre = pair.of(h, i, j);
                    } else if (h == gre.first) {
                        if (i == gre.second.first && rndBool()) {
                            gre = pair.of(h, i, j);
                        } else {
                            int ui, uib, Li, Lib;
                            if (rho[i].isEmpty()) Li = 0;
                            else {
                                ui = rho[i].get(rho[i].size() - 1);
                                Li = x[ui] + data.L[ui] / 2;
                            }
                            if (rho[gre.second.first].isEmpty()) Lib = 0;
                            else {
                                uib = rho[gre.second.first].get(rho[gre.second.first].size() - 1);
                                Lib = x[uib] + data.L[uib] / 2;
                            }
                            if (Li < Lib || (Li == Lib && rndBool())) {
                                gre = pair.of(h, i, j);
                            }
                        }
                    }
                }
            }
            if (gre.first < gmin || (gre.first == gmin && rndBool())) {
                gmin = gre.first;
                gremin = pair.of(c, gre.second);
            }
        }

        return gremin;
    }

    // Adds a facility (randomly selected from FL) at a position of the layout selected according to the Greedy function
    public void ConstructGreedyC(int greedy, double alpha) {

        // Generates a random ordering of facilities
        List<Integer> FL = new ArrayList<>(data.nN);
        for (int u = 0; u < data.nN; u++) FL.add(u);
        rndShuffle(FL);

        // Adds one facility to each row at random
        for (int i = 0; i < data.nM; i++) {
            int u = FL.get(FL.size() - 1);
            FL.remove(FL.size() - 1);
            rho[i].add(u);
        }

        // Generates the Candidate List of positions (PL)
        ArrayList<pair<Integer, pair<Integer, Integer>>> PL = new ArrayList<>();
        for (int i = 0; i < data.nM; i++) {
            for (int j = 0; j < rho[i].size() + 1; j++) {
                PL.add(new pair<>(0, new pair<>(i, j)));
            }
        }

        // Adds facilities according to the Greedy function value
        while (!FL.isEmpty()) {
            rndShuffle(PL);
            int u = FL.get(FL.size() - 1);
            FL.remove(FL.size() - 1);
            if (alpha == 0.0) alpha = rndDbl(0.0, 1.0);
            var pos = (greedy == 1) ?
                    getGreedyC1(PL, u, alpha) : // GR
                    getGreedyC2(PL, u, alpha); // RG
            int i = pos.first;
            int j = pos.second;
            rho[i].add(j, u);
            PL.add(new pair<>(0, new pair<>(i, rho[i].size())));
        }

    }

    public pair<Integer, Integer> getGreedyC1(ArrayList<pair<Integer, pair<Integer, Integer>>> CL, int u, double alpha) {

        int gmin = Integer.MAX_VALUE;
        int gmax = Integer.MIN_VALUE;

        for (var c : CL) {
            int i = c.second.first;
            int j = c.second.second;
            rho[i].add(j, u);
            c.first = EvaluateMove();
            rho[i].remove(j);
            if (c.first < gmin) gmin = c.first;
            if (c.first > gmax) gmax = c.first;
        }

        List<Integer> RCL = new ArrayList<>();
        double th = gmax - alpha * (gmax - gmin);
        for (int c = 0; c < CL.size(); c++) if (CL.get(c).first <= th) RCL.add(c);
        int candidate = RCL.get(rndInt(0, RCL.size() - 1));

//    int candidate;
//    int th = gmax - alpha*(gmax-gmin);
//    for(int c=0; c<CL.size(); c++) if(CL[c].first<=th) { candidate = c; break; };

        return CL.get(candidate).second;
    }

    public pair<Integer, Integer> getGreedyC2(ArrayList<pair<Integer, pair<Integer, Integer>>> CL, int u, double alpha) {

        int s = (int) floor(alpha * CL.size());
        if (s == 0) s = 1;
        int gmin = Integer.MAX_VALUE;

        int candidate = NOT_INITIALIZED;
        for (int c = 0; c < s; c++) {
            int i = CL.get(c).second.first;
            int j = CL.get(c).second.second;
            rho[i].add(j, u);
            CL.get(c).first = EvaluateMove();
            rho[i].remove(j);
            if (CL.get(c).first < gmin || (CL.get(c).first == gmin && rndBool())) {
                gmin = CL.get(c).first;
                candidate = c;
            }
        }

        return CL.get(throwIfNotInitialized(candidate)).second;
    }

    public int EvaluateGreedy(int u, int i) {

        int g = 0;

        int xu = -1;
        if (!rho[i].isEmpty())
            xu = x[rho[i].get(rho[i].size() - 1)] + (data.L[rho[i].get(rho[i].size() - 1)] + data.L[u]) / 2;
        else xu = data.L[xu] / 2;
        for (int k = 0; k < data.nM; k++) {
            for (int l = 0; l < rho[k].size(); l++) {
                int rhokl = rho[k].get(l);
                g += data.W[u][rhokl] * abs(xu - x[rhokl]);
            }
        }

        return g;
    }

    // END constructives.cpp

    // START exchange.cpp
    public void Exchange(int i, int j, int k, int l) {

        int temp = rho[i].get(j);
        rho[i].set(j, rho[k].get(l));
        rho[k].set(l, temp);

    }

    public int DeltaExchange(int f, int g, int xfp, int xgp) {
        return data.W[f][g] * (abs(xfp - xgp) - abs(x[f] - x[g]));
    }

    public int EvaluateExchangeSame(int i, int j, int l) {

        int h = 0;

        // Variables involved in the move
        int u = rho[i].get(j);
        int v = rho[i].get(l);
        int Laux = data.L[u] - data.L[v];
        int xup = x[v] - Laux / 2;
        int xvp = x[u] - Laux / 2;

        // Delta_a: Evaluates the cost change of facilities u and v with all the facilities in zones 1-4
        for (int s = 0; s < j; s++)
            h += DeltaExchange(u, rho[i].get(s), xup, x[rho[i].get(s)]) + DeltaExchange(v, rho[i].get(s), xvp, x[rho[i].get(s)]);
        for (int s = j + 1; s < l; s++)
            h += DeltaExchange(u, rho[i].get(s), xup, x[rho[i].get(s)] - Laux) + DeltaExchange(v, rho[i].get(s), xvp, x[rho[i].get(s)] - Laux);
        for (int s = l + 1; s < rho[i].size(); s++)
            h += DeltaExchange(u, rho[i].get(s), xup, x[rho[i].get(s)]) + DeltaExchange(v, rho[i].get(s), xvp, x[rho[i].get(s)]);
        for (int r = 0; r < data.nM; r++)
            if (r != i) for (int f : rho[r]) h += DeltaExchange(u, f, xup, x[f]) + DeltaExchange(v, f, xvp, x[f]);

        // Delta_b: Evaluates the cost change of facilities in zone 2 with all the facilities in zones 1, 3 and 4
        if (data.L[u] != data.L[v]) { // Otherwise, facilities in zone 2 does not change its position
            for (int t = j + 1; t < l; t++) {
                int f = rho[i].get(t);
                int xfp = x[f] - Laux;
                for (int s = 0; s < j; s++) h += DeltaExchange(f, rho[i].get(s), xfp, x[rho[i].get(s)]);
                for (int s = l + 1; s < rho[i].size(); s++) h += DeltaExchange(f, rho[i].get(s), xfp, x[rho[i].get(s)]);
                for (int r = 0; r < data.nM; r++) if (r != i) for (int g : rho[r]) h += DeltaExchange(f, g, xfp, x[g]);
            }
        }

        return h;
    }

    public int EvaluateExchangeDiff(int i, int j, int k, int l) {

        int h = 0;

        // Variables involved in the move
        int u = rho[i].get(j);
        int v = rho[k].get(l);
        int Laux = data.L[u] - data.L[v];
        int xup = x[v] + Laux / 2;
        int xvp = x[u] - Laux / 2;

        // Evaluates the cost change between facilities u and v
        h += DeltaExchange(u, v, xup, xvp);

        // Delta_c: Evaluates the cost change of facilities u and v with all the facilities in zones 1-5
        for (int s = 0; s < j; s++)
            h += DeltaExchange(u, rho[i].get(s), xup, x[rho[i].get(s)]) + DeltaExchange(v, rho[i].get(s), xvp, x[rho[i].get(s)]);
        for (int s = j + 1; s < rho[i].size(); s++)
            h += DeltaExchange(u, rho[i].get(s), xup, x[rho[i].get(s)] - Laux) + DeltaExchange(v, rho[i].get(s), xvp, x[rho[i].get(s)] - Laux);
        for (int s = 0; s < l; s++)
            h += DeltaExchange(u, rho[k].get(s), xup, x[rho[k].get(s)]) + DeltaExchange(v, rho[k].get(s), xvp, x[rho[k].get(s)]);
        for (int s = l + 1; s < rho[k].size(); s++)
            h += DeltaExchange(u, rho[k].get(s), xup, x[rho[k].get(s)] + Laux) + DeltaExchange(v, rho[k].get(s), xvp, x[rho[k].get(s)] + Laux);
        for (int r = 0; r < data.nM; r++)
            if ((r != i) && (r != k))
                for (int f : rho[r]) h += DeltaExchange(u, f, xup, x[f]) + DeltaExchange(v, f, xvp, x[f]);

        // Delta_d: Evaluates the cost change of facilities in zone 2 and 4 with all the facilities in zones 1, 3 and 5
        if (data.L[u] != data.L[v]) { // Otherwise, facilities in zone 2 does not change its position
            for (int t = j + 1; t < rho[i].size(); t++) { // Zone 2 with zones 1, 3 and 5
                int f = rho[i].get(t);
                int xfp = x[f] - Laux;
                for (int s = 0; s < j; s++) h += DeltaExchange(f, rho[i].get(s), xfp, x[rho[i].get(s)]);
                for (int s = 0; s < l; s++) h += DeltaExchange(f, rho[k].get(s), xfp, x[rho[k].get(s)]);
                for (int r = 0; r < data.nM; r++)
                    if ((r != i) && (r != k)) for (int g : rho[r]) h += DeltaExchange(f, g, xfp, x[g]);
            }
            for (int t = l + 1; t < rho[k].size(); t++) { // Zone 4 with zones 1, 3 and 5
                int f = rho[k].get(t);
                int xfp = x[f] + Laux;
                for (int s = 0; s < j; s++) h += DeltaExchange(f, rho[i].get(s), xfp, x[rho[i].get(s)]);
                for (int s = 0; s < l; s++) h += DeltaExchange(f, rho[k].get(s), xfp, x[rho[k].get(s)]);
                for (int r = 0; r < data.nM; r++)
                    if ((r != i) && (r != k)) for (int g : rho[r]) h += DeltaExchange(f, g, xfp, x[g]);
            }
            for (int t = j + 1; t < rho[i].size(); t++) { // Zone 2 with zone 4
                int f = rho[i].get(t);
                int xfp = x[f] - Laux;
                for (int s = l + 1; s < rho[k].size(); s++)
                    h += DeltaExchange(f, rho[k].get(s), xfp, x[rho[k].get(s)] + Laux);
            }
        }

        return h;
    }

    public boolean ExploreExchange(int strategy) {

        boolean improved = false;

        int ibest = NOT_INITIALIZED, jbest = NOT_INITIALIZED;
        int kbest = NOT_INITIALIZED, lbest = NOT_INITIALIZED;

        boolean[][] mat = new boolean[data.nN][data.nN];
        for (int i = 0; i < data.nN; i++) mat[i][i] = true;

        List<pair<Integer, Integer>> vecpos = new ArrayList<>();
        for (int iii = 0; iii < data.nM; iii++) {
            for (int jjj = 0; jjj < rho[iii].size(); jjj++) {
                vecpos.add(pair.of(iii, jjj));
            }
        }
        rndShuffle(vecpos);

        int hbest = 0;
        for (var posi : vecpos) {
            int i = posi.first;
            int j = posi.second;
            int u = rho[i].get(j);
            for (var posk : vecpos) {
                int k = posk.first;
                int l = posk.second;
                int v = rho[k].get(l);
                if (!mat[u][v]) {
                    mat[u][v] = true;
                    mat[v][u] = true;
                    Exchange(i, j, k, l);
                    int h = EvaluateMove();
                    Exchange(k, l, i, j);
                    if (h < hbest) {
                        ibest = i;
                        jbest = j;
                        kbest = k;
                        lbest = l;
                        hbest = h;
                        improved = true;
                    }
                }
                if ((strategy > 1) && (improved)) break;
            }
            if ((strategy > 0) && (improved)) break;
        }

        if (improved) {
            obj += hbest;
            Exchange(throwIfNotInitialized(ibest), throwIfNotInitialized(jbest), throwIfNotInitialized(kbest), throwIfNotInitialized(lbest));
            CalculateCenters();
        }

        return improved;
    }

    public boolean ExploreExchangeInc(int strategy) {

        boolean improved = false;

        int ibest = NOT_INITIALIZED, jbest = NOT_INITIALIZED;
        int kbest = NOT_INITIALIZED, lbest = NOT_INITIALIZED;

        boolean[][] mat = new boolean[data.nN][data.nN];

        for (int i = 0; i < data.nN; i++) mat[i][i] = true;


        List<pair<Integer, Integer>> vecpos = new ArrayList<>();
        for (int iii = 0; iii < data.nM; iii++) {
            for (int jjj = 0; jjj < rho[iii].size(); jjj++) {
                pair<Integer, Integer> pos = new pair<>(iii, jjj);
                vecpos.add(pos);
            }
        }
        rndShuffle(vecpos);

        int hbest = 0;
        for (var posi : vecpos) {
            int i = posi.first;
            int j = posi.second;
            int u = rho[i].get(j);
            for (var posk : vecpos) {
                int k = posk.first;
                int l = posk.second;
                int v = rho[k].get(l);
                if (!mat[u][v]) {
                    mat[u][v] = true;
                    mat[v][u] = true;
                    int h = 0;
                    if (k == i) {
                        if (l < j) h = EvaluateExchangeSame(i, l, j);
                        else h = EvaluateExchangeSame(i, j, l);
                    } else h = EvaluateExchangeDiff(i, j, k, l);
                    if (h < hbest) {
                        ibest = i;
                        jbest = j;
                        kbest = k;
                        lbest = l;
                        hbest = h;
                        improved = true;
                    }
                }
                if ((strategy > 1) && (improved)) break;
            }
            if ((strategy > 0) && (improved)) break;
        }

        if (improved) {
            obj += hbest;
            Exchange(throwIfNotInitialized(ibest), throwIfNotInitialized(jbest), throwIfNotInitialized(kbest), throwIfNotInitialized(lbest));
            CalculateCenters();
        }

        return improved;
    }

    public boolean ExploreExchangeHorizontalBest() {

        boolean improved = false;

        int ibest = NOT_INITIALIZED, jbest = NOT_INITIALIZED;
        int kbest = NOT_INITIALIZED, lbest = NOT_INITIALIZED;

        int hbest = 0;
        for (int i = 0; i < data.nM; i++) {
            for (int j = 0; j < rho[i].size() - 1; j++) {
                for (int l = j + 1; l < rho[i].size(); l++) {
                    Exchange(i, j, i, l);
                    int h = EvaluateMove();
                    Exchange(i, l, i, j);
                    if (h < hbest || ((h == hbest) && (h < 0) && (rndBool()))) {
                        ibest = i;
                        jbest = j;
                        kbest = i;
                        lbest = l;
                        hbest = h;
                        improved = true;
                    }
                }
            }
        }

        if (improved) {
            obj += hbest;
            Exchange(throwIfNotInitialized(ibest), throwIfNotInitialized(jbest), throwIfNotInitialized(kbest), throwIfNotInitialized(lbest));
            CalculateCenters();
        }

        return improved;
    }

    public boolean ExploreExchangeHorizontalBestInc() {

        boolean improved = false;

        int ibest = NOT_INITIALIZED, jbest = NOT_INITIALIZED;
        int kbest = NOT_INITIALIZED, lbest = NOT_INITIALIZED;

        int hbest = 0;
        for (int i = 0; i < data.nM; i++) {
            for (int j = 0; j < rho[i].size() - 1; j++) {
                for (int l = j + 1; l < rho[i].size(); l++) {
                    int h = EvaluateExchangeSame(i, j, l);
                    if (h < hbest || ((h == hbest) && (h < 0) && (rndBool()))) {
                        ibest = i;
                        jbest = j;
                        kbest = i;
                        lbest = l;
                        hbest = h;
                        improved = true;
                    }
                }
            }
        }

        if (improved) {
            obj += hbest;
            Exchange(throwIfNotInitialized(ibest), throwIfNotInitialized(jbest), throwIfNotInitialized(kbest), throwIfNotInitialized(lbest));
            CalculateCenters();
        }

        return improved;
    }

    public boolean ExploreExchangeVerticalBest() {

        boolean improved = false;

        int ibest = NOT_INITIALIZED, jbest = NOT_INITIALIZED;
        int kbest = NOT_INITIALIZED, lbest = NOT_INITIALIZED;

        int hbest = 0;
        for (int i = 0; i < data.nM; i++) {
            for (int j = 0; j < rho[i].size(); j++) {
                for (int k = i + 1; k < data.nM; k++) {
                    for (int l = 0; l < rho[k].size(); l++) {
                        Exchange(i, j, k, l);
                        int h = EvaluateMove();
                        Exchange(k, l, i, j);
                        if (h < hbest || ((h == hbest) && (h < 0) && (rndBool()))) {
                            ibest = i;
                            jbest = j;
                            kbest = k;
                            lbest = l;
                            hbest = h;
                            improved = true;
                        }
                    }
                }
            }
        }

        if (improved) {
            obj += hbest;
            Exchange(throwIfNotInitialized(ibest), throwIfNotInitialized(jbest), throwIfNotInitialized(kbest), throwIfNotInitialized(lbest));
            CalculateCenters();
        }

        return improved;
    }

    public boolean ExploreExchangeVerticalBestInc() {

        boolean improved = false;

        int ibest = NOT_INITIALIZED, jbest = NOT_INITIALIZED;
        int kbest = NOT_INITIALIZED, lbest = NOT_INITIALIZED;

        int hbest = 0;
        for (int i = 0; i < data.nM; i++) {
            for (int j = 0; j < rho[i].size(); j++) {
                for (int k = i + 1; k < data.nM; k++) {
                    for (int l = 0; l < rho[k].size(); l++) {
                        int h = EvaluateExchangeDiff(i, j, k, l);
                        if (h < hbest || ((h == hbest) && (h < 0) && (rndBool()))) {
                            ibest = i;
                            jbest = j;
                            kbest = k;
                            lbest = l;
                            hbest = h;
                            improved = true;
                        }
                    }
                }
            }
        }

        if (improved) {
            obj += hbest;
            Exchange(throwIfNotInitialized(ibest), throwIfNotInitialized(jbest), throwIfNotInitialized(kbest), throwIfNotInitialized(lbest));
            CalculateCenters();
        }

        return improved;
    }
    // END exchange.cpp
    // START extended.cpp

    public boolean ExploreExtendedBest() {

        boolean improved = false;

        int ibest_exc = NOT_INITIALIZED, jbest_exc = NOT_INITIALIZED;
        int kbest_exc = NOT_INITIALIZED, lbest_exc = NOT_INITIALIZED;

        int ibest_ins = NOT_INITIALIZED, jbest_ins = NOT_INITIALIZED;
        int kbest_ins = NOT_INITIALIZED, lbest_ins = NOT_INITIALIZED;

        int hbest_exc = 0;
        for (int i = 0; i < data.nM; i++) {
            for (int j = 0; j < rho[i].size(); j++) {
                for (int l = j + 1; l < rho[i].size(); l++) {
                    Exchange(i, j, i, l);
                    int h = EvaluateMove();
                    Exchange(i, l, i, j);
                    if (h < hbest_exc || ((h == hbest_exc) && (h < 0) && (rndBool()))) {
                        ibest_exc = i;
                        jbest_exc = j;
                        kbest_exc = i;
                        lbest_exc = l;
                        hbest_exc = h;
                    }
                }
                for (int k = i + 1; k < data.nM; k++) {
                    for (int l = 0; l < rho[k].size(); l++) {
                        Exchange(i, j, k, l);
                        int h = EvaluateMove();
                        Exchange(k, l, i, j);
                        if (h < hbest_exc || ((h == hbest_exc) && (h < 0) && (rndBool()))) {
                            ibest_exc = i;
                            jbest_exc = j;
                            kbest_exc = k;
                            lbest_exc = l;
                            hbest_exc = h;
                        }
                    }
                }
            }
        }

        int hbest_ins = 0;
        for (int i = 0; i < data.nM; i++) {
            if (rho[i].size() > 1) {
                for (int j = 0; j < rho[i].size(); j++) {
                    // Backward Swaps
                    for (int l = j; l > 0; l--) {
                        Insert(i, j, i, l - 1);
                        int h = EvaluateMove();
                        Insert(i, l - 1, i, j);
                        if (h < hbest_ins || ((h == hbest_ins) && (h < 0) && (rndBool()))) {
                            ibest_ins = i;
                            jbest_ins = j;
                            kbest_ins = i;
                            lbest_ins = l - 1;
                            hbest_ins = h;
                        }
                    }
                    // Forward Swaps
                    for (int l = j; l < rho[i].size() - 1; l++) {
                        Insert(i, j, i, l + 1);
                        int h = EvaluateMove();
                        Insert(i, l + 1, i, j);
                        if (h < hbest_ins || ((h == hbest_ins) && (h < 0) && (rndBool()))) {
                            ibest_ins = i;
                            jbest_ins = j;
                            kbest_ins = i;
                            lbest_ins = l + 1;
                            hbest_ins = h;
                        }
                    }
                    // Insert into a different row
                    for (int k = 0; k < data.nM; k++) {
                        if (k != i) {
                            for (int l = rho[k].size(); l >= 0; l--) {
                                Insert(i, j, k, l);
                                int h = EvaluateMove();
                                Insert(k, l, i, j);
                                if (h < hbest_ins || ((h == hbest_ins) && (h < 0) && (rndBool()))) {
                                    ibest_ins = i;
                                    jbest_ins = j;
                                    kbest_ins = k;
                                    lbest_ins = l;
                                    hbest_ins = h;
                                }
                            }
                        }
                    }
                }
            }
        }

        // Checks the extended move
        int hbest = 0;
        int move = 0;
        if (hbest_exc < hbest_ins) move = 1;
        else if (hbest_ins < hbest_exc) move = 2;
        else {
            if (rndDbl(0.0, 1.0) < 0.5) move = 1;
            else move = 2;
        }
        switch (move) {
            case 1:
                if (hbest_exc < 0) {
                    improved = true;
                    obj += hbest_exc;
                    Exchange(throwIfNotInitialized(ibest_exc), throwIfNotInitialized(jbest_exc), throwIfNotInitialized(kbest_exc), throwIfNotInitialized(lbest_exc));
                    CalculateCenters();
                } else improved = false;
                break;
            case 2:
                if (hbest_ins < 0) {
                    improved = true;
                    obj += hbest_ins;
                    Insert(throwIfNotInitialized(ibest_ins), throwIfNotInitialized(jbest_ins), throwIfNotInitialized(kbest_ins), throwIfNotInitialized(lbest_ins));
                    CalculateCenters();
                } else improved = false;
                break;
            default:
                improved = false;
        }

        return improved;
    }

    public boolean ExploreExtendedBestBestInc() {

        boolean improved = false;

        int ibest_exc = NOT_INITIALIZED, jbest_exc = NOT_INITIALIZED;
        int kbest_exc = NOT_INITIALIZED, lbest_exc = NOT_INITIALIZED;

        int ibest_ins = NOT_INITIALIZED, jbest_ins = NOT_INITIALIZED;
        int kbest_ins = NOT_INITIALIZED, lbest_ins = NOT_INITIALIZED;

        int[] xaux = x.clone();

        int hbest_exc = 0;
        for (int i = 0; i < data.nM; i++) {
            for (int j = 0; j < rho[i].size(); j++) {
                for (int l = j + 1; l < rho[i].size(); l++) {
                    int h = EvaluateExchangeSame(i, j, l);
                    if (h < hbest_exc || ((h == hbest_exc) && (h < 0) && (rndBool()))) {
                        ibest_exc = i;
                        jbest_exc = j;
                        kbest_exc = i;
                        lbest_exc = l;
                        hbest_exc = h;
                    }
                }
                for (int k = i + 1; k < data.nM; k++) {
                    for (int l = 0; l < rho[k].size(); l++) {
                        int h = EvaluateExchangeDiff(i, j, k, l);
                        if (h < hbest_exc || ((h == hbest_exc) && (h < 0) && (rndBool()))) {
                            ibest_exc = i;
                            jbest_exc = j;
                            kbest_exc = k;
                            lbest_exc = l;
                            hbest_exc = h;
                        }
                    }
                }
            }
        }

        int hbest_ins = 0;
        for (int i = 0; i < data.nM; i++) {
            if (rho[i].size() > 1) {
                for (int j = 0; j < rho[i].size(); j++) {
                    // Backward Swaps
                    int h = 0;
                    for (int l = j; l > 0; l--) {
                        h += EvaluateSwap(i, l - 1);
                        //h += EvaluateExchangeSame(i,l-1,l);
                        if (h < hbest_ins || ((h == hbest_ins) && (h < 0) && (rndBool()))) {
                            ibest_ins = i;
                            jbest_ins = j;
                            kbest_ins = i;
                            lbest_ins = l - 1;
                            hbest_ins = h;
                        }
                        x[rho[i].get(l - 1)] += data.L[rho[i].get(l)];
                        x[rho[i].get(l)] -= data.L[rho[i].get(l - 1)];
                        Exchange(i, l - 1, i, l);
                    }
                    // Restore solution
                    if (j > 0) {
                        Insert(i, 0, i, j);
                        for (int s = 0; s <= j; s++) x[rho[i].get(s)] = xaux[rho[i].get(s)];
                    }
                    // Forward Swaps
                    h = 0;
                    for (int l = j; l < rho[i].size() - 1; l++) {
                        h += EvaluateSwap(i, l);
                        //h += EvaluateExchangeSame(i,l,l+1);
                        if (h < hbest_ins || ((h == hbest_ins) && (h < 0) && (rndBool()))) {
                            ibest_ins = i;
                            jbest_ins = j;
                            kbest_ins = i;
                            lbest_ins = l + 1;
                            hbest_ins = h;
                        }
                        x[rho[i].get(l)] += data.L[rho[i].get(l + 1)];
                        x[rho[i].get(l + 1)] -= data.L[rho[i].get(l)];
                        Exchange(i, l, i, l + 1);
                    }
                    // Insert into a different row
                    int f = rho[i].remove(rho[i].size() - 1);
                    h += EvaluateDrop(f);
                    for (int k = 0; k < data.nM; k++) {
                        if (k != i) {
                            // Insert at the end of row k
                            if (!rho[k].isEmpty())
                                x[f] = x[rho[k].get(rho[k].size() - 1)] + (data.L[rho[k].get(rho[k].size() - 1)] + data.L[f]) / 2;
                            else x[f] = data.L[f] / 2;
                            rho[k].add(f);
                            int haux = h + EvaluateAdd(f, k);
                            if (haux < hbest_ins || ((haux == hbest_ins) && (haux < 0) && (rndBool()))) {
                                ibest_ins = i;
                                jbest_ins = j;
                                kbest_ins = k;
                                lbest_ins = rho[k].size() - 1;
                                hbest_ins = haux;
                            }
                            //  Backward Swaps
                            for (int l = rho[k].size() - 1; l > 0; l--) {
                                haux += EvaluateSwap(k, l - 1);
                                //haux += EvaluateExchangeSame(k,l-1,l);
                                if (haux < hbest_ins || ((haux == hbest_ins) && (haux < 0) && (rndBool()))) {
                                    ibest_ins = i;
                                    jbest_ins = j;
                                    kbest_ins = k;
                                    lbest_ins = l - 1;
                                    hbest_ins = haux;
                                }
                                x[rho[k].get(l - 1)] += data.L[rho[k].get(l)];
                                x[rho[k].get(l)] -= data.L[rho[k].get(l - 1)];
                                Exchange(k, l - 1, k, l);
                            }
                            // Restore partial solution
                            rho[k].remove(0);
                            for (int u : rho[k]) x[u] = xaux[u];
                        }
                    }
                    // Restore solution
                    rho[i].add(j, f);
                    for (int s = j; s < rho[i].size(); s++) x[rho[i].get(s)] = xaux[rho[i].get(s)];
                }
            }
        }

        // Checks the extended move
        int hbest = 0;
        int move = 0;
        if (hbest_exc < hbest_ins) move = 1;
        else if (hbest_ins < hbest_exc) move = 2;
        else {
            if (rndDbl(0.0, 1.0) < 0.5) move = 1;
            else move = 2;
        }
        switch (move) {
            case 1:
                if (hbest_exc < 0) {
                    improved = true;
                    obj += hbest_exc;
                    Exchange(throwIfNotInitialized(ibest_exc), throwIfNotInitialized(jbest_exc), throwIfNotInitialized(kbest_exc), throwIfNotInitialized(lbest_exc));
                    CalculateCenters();
                } else improved = false;
                break;
            case 2:
                if (hbest_ins < 0) {
                    improved = true;
                    obj += hbest_ins;
                    Insert(throwIfNotInitialized(ibest_ins), throwIfNotInitialized(jbest_ins), throwIfNotInitialized(kbest_ins), throwIfNotInitialized(lbest_ins));
                    CalculateCenters();
                } else improved = false;
                break;
            default:
                improved = false;
        }

        return improved;
    }

    public boolean ExploreExtendedBestHybridInc() {

        boolean improved = false;

        int ibest_exc = NOT_INITIALIZED, jbest_exc = NOT_INITIALIZED;
        int kbest_exc = NOT_INITIALIZED, lbest_exc = NOT_INITIALIZED;

        int ibest_ins = NOT_INITIALIZED, jbest_ins = NOT_INITIALIZED;
        int kbest_ins = NOT_INITIALIZED, lbest_ins = NOT_INITIALIZED;

        int[] xaux = x.clone();

        int hbest_exc = 0;
        for (int i = 0; i < data.nM; i++) {
            for (int j = 0; j < rho[i].size(); j++) {
                for (int l = j + 1; l < rho[i].size(); l++) {
                    int h = EvaluateExchangeSame(i, j, l);
                    if (h < hbest_exc || ((h == hbest_exc) && (h < 0) && (rndBool()))) {
                        ibest_exc = i;
                        jbest_exc = j;
                        kbest_exc = i;
                        lbest_exc = l;
                        hbest_exc = h;
                    }
                }
                for (int k = i + 1; k < data.nM; k++) {
                    for (int l = 0; l < rho[k].size(); l++) {
                        int h = EvaluateExchangeDiff(i, j, k, l);
                        if (h < hbest_exc || ((h == hbest_exc) && (h < 0) && (rndBool()))) {
                            ibest_exc = i;
                            jbest_exc = j;
                            kbest_exc = k;
                            lbest_exc = l;
                            hbest_exc = h;
                        }
                    }
                }
            }
        }

        ArrayList<pair<Integer, Integer>> vecposi = new ArrayList<>();
        for (int iii = 0; iii < data.nM; iii++) {
            for (int jjj = 0; jjj < rho[iii].size(); jjj++) {
                vecposi.add(pair.of(iii, jjj));
            }
        }
        rndShuffle(vecposi);

        improved = false;
        int hbest_ins = 0;
        for (var posi : vecposi) {
            int i = posi.first;
            int j = posi.second;
            if (rho[i].size() > 1) {
                // Backward Swaps
                int h = 0;
                for (int l = j; l > 0; l--) {
                    h += EvaluateSwap(i, l - 1);
                    if (h < hbest_ins || ((h == hbest_ins) && (h < 0) && (rndBool()))) {
                        ibest_ins = i;
                        jbest_ins = j;
                        kbest_ins = i;
                        lbest_ins = l - 1;
                        hbest_ins = h;
                        improved = true;
                    }
                    x[rho[i].get(l - 1)] += data.L[rho[i].get(l)];
                    x[rho[i].get(l)] -= data.L[rho[i].get(l - 1)];
                    Exchange(i, l - 1, i, l);
                }
                // Restore solution
                if (j > 0) {
                    Insert(i, 0, i, j);
                    for (int s = 0; s <= j; s++) x[rho[i].get(s)] = xaux[rho[i].get(s)];
                }
                // Forward Swaps
                h = 0;
                for (int l = j; l < rho[i].size() - 1; l++) {
                    h += EvaluateSwap(i, l);
                    if (h < hbest_ins || ((h == hbest_ins) && (h < 0) && (rndBool()))) {
                        ibest_ins = i;
                        jbest_ins = j;
                        kbest_ins = i;
                        lbest_ins = l + 1;
                        hbest_ins = h;
                        improved = true;
                    }
                    x[rho[i].get(l)] += data.L[rho[i].get(l + 1)];
                    x[rho[i].get(l + 1)] -= data.L[rho[i].get(l)];
                    Exchange(i, l, i, l + 1);
                }
                // Insert into a different row
                int f = rho[i].remove(rho[i].size() - 1);
                h += EvaluateDrop(f);
                for (int k = 0; k < data.nM; k++) {
                    if (k != i) {
                        // Insert at the end of row k
                        x[f] = x[rho[k].get(rho[k].size() - 1)] + (data.L[rho[k].get(rho[k].size() - 1)] + data.L[f]) / 2;
                        rho[k].add(f);
                        int haux = h + EvaluateAdd(f, k);
                        if (haux < hbest_ins || ((haux == hbest_ins) && (haux < 0) && (rndBool()))) {
                            ibest_ins = i;
                            jbest_ins = j;
                            kbest_ins = k;
                            lbest_ins = rho[k].size() - 1;
                            hbest_ins = haux;
                            improved = true;
                        }
                        //  Backward Swaps
                        for (int l = rho[k].size() - 1; l > 0; l--) {
                            haux += EvaluateSwap(k, l - 1);
                            if (haux < hbest_ins || ((haux == hbest_ins) && (haux < 0) && (rndBool()))) {
                                ibest_ins = i;
                                jbest_ins = j;
                                kbest_ins = k;
                                lbest_ins = l - 1;
                                hbest_ins = haux;
                                improved = true;
                            }
                            x[rho[k].get(l - 1)] += data.L[rho[k].get(l)];
                            x[rho[k].get(l)] -= data.L[rho[k].get(l - 1)];
                            Exchange(k, l - 1, k, l);
                        }
                        // Restore partial solution
                        rho[k].remove(0);
                        for (int u : rho[k]) x[u] = xaux[u];
                    }
                }
                // Restore solution
                rho[i].add(j, f);
                for (int s = j; s < rho[i].size(); s++) x[rho[i].get(s)] = xaux[rho[i].get(s)];
                if (improved) break;
            }
        }

        // Checks the extended move
        int hbest = 0;
        int move = 0;
        if (hbest_exc < hbest_ins) move = 1;
        else if (hbest_ins < hbest_exc) move = 2;
        else {
            if (rndDbl(0.0, 1.0) < 0.5) move = 1;
            else move = 2;
        }
        improved = false;
        switch (move) {
            case 1:
                if (hbest_exc < 0) {
                    improved = true;
                    obj += hbest_exc;
                    Exchange(throwIfNotInitialized(ibest_exc), throwIfNotInitialized(jbest_exc), throwIfNotInitialized(kbest_exc), throwIfNotInitialized(lbest_exc));
                    CalculateCenters();
                } else improved = false;
                break;
            case 2:
                if (hbest_ins < 0) {
                    improved = true;
                    obj += hbest_ins;
                    Insert(throwIfNotInitialized(ibest_ins), throwIfNotInitialized(jbest_ins), throwIfNotInitialized(kbest_ins), throwIfNotInitialized(lbest_ins));
                    CalculateCenters();
                } else improved = false;
                break;
            default:
                improved = false;
        }

        return improved;
    }

    public boolean ExploreExtendedBestFirstInc() {

        boolean improved = false;

        int ibest_exc = NOT_INITIALIZED, jbest_exc = NOT_INITIALIZED;
        int kbest_exc = NOT_INITIALIZED, lbest_exc = NOT_INITIALIZED;

        int ibest_ins = NOT_INITIALIZED, jbest_ins = NOT_INITIALIZED;
        int kbest_ins = NOT_INITIALIZED, lbest_ins = NOT_INITIALIZED;

        int[] xaux = x.clone();

        int hbest_exc = 0;
        for (int i = 0; i < data.nM; i++) {
            for (int j = 0; j < rho[i].size(); j++) {
                for (int l = j + 1; l < rho[i].size(); l++) {
                    int h = EvaluateExchangeSame(i, j, l);
                    if (h < hbest_exc || ((h == hbest_exc) && (h < 0) && (rndBool()))) {
                        ibest_exc = i;
                        jbest_exc = j;
                        kbest_exc = i;
                        lbest_exc = l;
                        hbest_exc = h;
                    }
                }
                for (int k = i + 1; k < data.nM; k++) {
                    for (int l = 0; l < rho[k].size(); l++) {
                        int h = EvaluateExchangeDiff(i, j, k, l);
                        if (h < hbest_exc || ((h == hbest_exc) && (h < 0) && (rndBool()))) {
                            ibest_exc = i;
                            jbest_exc = j;
                            kbest_exc = k;
                            lbest_exc = l;
                            hbest_exc = h;
                        }
                    }
                }
            }
        }

        ArrayList<pair<Integer, Integer>> vecposi = new ArrayList<>();
        for (int iii = 0; iii < data.nM; iii++) {
            for (int jjj = 0; jjj < rho[iii].size(); jjj++) {
                vecposi.add(pair.of(iii, jjj));
            }
        }
        rndShuffle(vecposi);

        ArrayList<pair<Integer, Integer>> vecposk = new ArrayList<>();
        for (int iii = 0; iii < data.nM; iii++) {
            for (int jjj = 0; jjj < rho[iii].size(); jjj++) {
                vecposk.add(pair.of(iii, jjj));
            }
        }
        rndShuffle(vecposk);

        improved = false;
        int hbest_ins = 0;
        for (var posi : vecposi) {
            int i = posi.first;
            int j = posi.second;
            if (rho[i].size() > 1) {
                for (var posk : vecposk) {
                    int k = posk.first;
                    int l = posk.second;
                    if ((i != k) || ((i == k) && (l < rho[i].size()))) {
                        Insert(i, j, k, l);
                        int h = EvaluateMove();
                        Insert(k, l, i, j);
                        if (h < hbest_ins || ((h == hbest_ins) && (h < 0) && (rndBool()))) {
                            ibest_ins = i;
                            jbest_ins = j;
                            kbest_ins = k;
                            lbest_ins = l;
                            hbest_ins = h;
                            improved = true;
                        }
                    }
                    if (improved) break;
                }
                if (improved) break;
            }
        }

        // Checks the extended move
        int hbest = 0;
        int move = 0;
        if (hbest_exc < hbest_ins) move = 1;
        else if (hbest_ins < hbest_exc) move = 2;
        else {
            if (rndDbl(0.0, 1.0) < 0.5) move = 1;
            else move = 2;
        }
        improved = false;
        switch (move) {
            case 1:
                if (hbest_exc < 0) {
                    improved = true;
                    obj += hbest_exc;
                    Exchange(throwIfNotInitialized(ibest_exc), throwIfNotInitialized(jbest_exc), throwIfNotInitialized(kbest_exc), throwIfNotInitialized(lbest_exc));
                    CalculateCenters();
                } else improved = false;
                break;
            case 2:
                if (hbest_ins < 0) {
                    improved = true;
                    obj += hbest_ins;
                    Insert(throwIfNotInitialized(ibest_ins), throwIfNotInitialized(jbest_ins), throwIfNotInitialized(kbest_ins), throwIfNotInitialized(lbest_ins));
                    CalculateCenters();
                } else improved = false;
                break;
            default:
                improved = false;
        }

        return improved;
    }

    public boolean ExploreExtendedHybridBestInc() {

        boolean improved = false;

        int ibest_exc = NOT_INITIALIZED, jbest_exc = NOT_INITIALIZED;
        int kbest_exc = NOT_INITIALIZED, lbest_exc = NOT_INITIALIZED;

        int ibest_ins = NOT_INITIALIZED, jbest_ins = NOT_INITIALIZED;
        int kbest_ins = NOT_INITIALIZED, lbest_ins = NOT_INITIALIZED;

        int[] xaux = x.clone();

        List<pair<Integer, Integer>> vecposi = new ArrayList<>();
        for (int iii = 0; iii < data.nM; iii++) {
            for (int jjj = 0; jjj < rho[iii].size(); jjj++) {
                vecposi.add(pair.of(iii, jjj));
            }
        }
        rndShuffle(vecposi);

        int hbest_exc = 0;
        for (var posi : vecposi) {
            int i = posi.first;
            int j = posi.second;
            for (int k = 0; k < data.nM; k++) {
                if (k == i) {
                    for (int l = 0; l < rho[i].size(); l++) {
                        if (l != j) {
                            int h = 0;
                            if (l < j) h = EvaluateExchangeSame(i, l, j);
                            else h = EvaluateExchangeSame(i, j, l);
                            if (h < hbest_exc || ((h == hbest_exc) && (h < 0) && (rndBool()))) {
                                ibest_exc = i;
                                jbest_exc = j;
                                kbest_exc = i;
                                lbest_exc = l;
                                hbest_exc = h;
                                improved = true;
                            }
                        }
                    }
                } else {
                    for (int l = 0; l < rho[k].size(); l++) {
                        int h = EvaluateExchangeDiff(i, j, k, l);
                        if (h < hbest_exc || ((h == hbest_exc) && (h < 0) && (rndBool()))) {
                            ibest_exc = i;
                            jbest_exc = j;
                            kbest_exc = k;
                            lbest_exc = l;
                            hbest_exc = h;
                            improved = true;
                        }
                    }
                }
            }
            if (improved) break;
        }

        int hbest_ins = 0;
        for (int i = 0; i < data.nM; i++) {
            if (rho[i].size() > 1) {
                for (int j = 0; j < rho[i].size(); j++) {
                    // Backward Swaps
                    int h = 0;
                    for (int l = j; l > 0; l--) {
                        h += EvaluateSwap(i, l - 1);
                        //h += EvaluateExchangeSame(i,l-1,l);
                        if (h < hbest_ins || ((h == hbest_ins) && (h < 0) && (rndBool()))) {
                            ibest_ins = i;
                            jbest_ins = j;
                            kbest_ins = i;
                            lbest_ins = l - 1;
                            hbest_ins = h;
                        }
                        x[rho[i].get(l - 1)] += data.L[rho[i].get(l)];
                        x[rho[i].get(l)] -= data.L[rho[i].get(l - 1)];
                        Exchange(i, l - 1, i, l);
                    }
                    // Restore solution
                    if (j > 0) {
                        Insert(i, 0, i, j);
                        for (int s = 0; s <= j; s++) x[rho[i].get(s)] = xaux[rho[i].get(s)];
                    }
                    // Forward Swaps
                    h = 0;
                    for (int l = j; l < rho[i].size() - 1; l++) {
                        h += EvaluateSwap(i, l);
                        //h += EvaluateExchangeSame(i,l,l+1);
                        if (h < hbest_ins || ((h == hbest_ins) && (h < 0) && (rndBool()))) {
                            ibest_ins = i;
                            jbest_ins = j;
                            kbest_ins = i;
                            lbest_ins = l + 1;
                            hbest_ins = h;
                        }
                        x[rho[i].get(l)] += data.L[rho[i].get(l + 1)];
                        x[rho[i].get(l + 1)] -= data.L[rho[i].get(l)];
                        Exchange(i, l, i, l + 1);
                    }
                    // Insert into a different row
                    int f = rho[i].remove(rho[i].size() - 1);
                    h += EvaluateDrop(f);
                    for (int k = 0; k < data.nM; k++) {
                        if (k != i) {
                            // Insert at the end of row k
                            if (!rho[k].isEmpty())
                                x[f] = x[rho[k].get(rho[k].size() - 1)] + (data.L[rho[k].get(rho[k].size() - 1)] + data.L[f]) / 2;
                            else x[f] = data.L[f] / 2;
                            rho[k].add(f);
                            int haux = h + EvaluateAdd(f, k);
                            if (haux < hbest_ins || ((haux == hbest_ins) && (haux < 0) && (rndBool()))) {
                                ibest_ins = i;
                                jbest_ins = j;
                                kbest_ins = k;
                                lbest_ins = (rho[k].size() - 1);
                                hbest_ins = haux;
                            }
                            //  Backward Swaps
                            for (int l = (rho[k].size() - 1); l > 0; l--) {
                                haux += EvaluateSwap(k, l - 1);
                                //haux += EvaluateExchangeSame(k,l-1,l);
                                if (haux < hbest_ins || ((haux == hbest_ins) && (haux < 0) && (rndBool()))) {
                                    ibest_ins = i;
                                    jbest_ins = j;
                                    kbest_ins = k;
                                    lbest_ins = l - 1;
                                    hbest_ins = haux;
                                }
                                x[rho[k].get(l - 1)] += data.L[rho[k].get(l)];
                                x[rho[k].get(l)] -= data.L[rho[k].get(l - 1)];
                                Exchange(k, l - 1, k, l);
                            }
                            // Restore partial solution
                            rho[k].remove(0);
                            for (int u : rho[k]) x[u] = xaux[u];
                        }
                    }
                    // Restore solution
                    rho[i].add(j, f);
                    for (int s = j; s < rho[i].size(); s++) x[rho[i].get(s)] = xaux[rho[i].get(s)];
                }
            }
        }

        // Checks the extended move
        int hbest = 0;
        int move = 0;
        if (hbest_exc < hbest_ins) move = 1;
        else if (hbest_ins < hbest_exc) move = 2;
        else {
            if (rndDbl(0.0, 1.0) < 0.5) move = 1;
            else move = 2;
        }
        improved = false;
        switch (move) {
            case 1:
                if (hbest_exc < 0) {
                    improved = true;
                    obj += hbest_exc;
                    Exchange(throwIfNotInitialized(ibest_exc), throwIfNotInitialized(jbest_exc), throwIfNotInitialized(kbest_exc), throwIfNotInitialized(lbest_exc));
                    CalculateCenters();
                } else improved = false;
                break;
            case 2:
                if (hbest_ins < 0) {
                    improved = true;
                    obj += hbest_ins;
                    Insert(throwIfNotInitialized(ibest_ins), throwIfNotInitialized(jbest_ins), throwIfNotInitialized(kbest_ins), throwIfNotInitialized(lbest_ins));
                    CalculateCenters();
                } else improved = false;
                break;
            default:
                improved = false;
        }

        return improved;
    }

    public boolean ExploreExtendedHybridHybridInc() {

        boolean improved = false;

        int ibest_exc = NOT_INITIALIZED, jbest_exc = NOT_INITIALIZED;
        int kbest_exc = NOT_INITIALIZED, lbest_exc = NOT_INITIALIZED;

        int ibest_ins = NOT_INITIALIZED, jbest_ins = NOT_INITIALIZED;
        int kbest_ins = NOT_INITIALIZED, lbest_ins = NOT_INITIALIZED;

        int[] xaux = x.clone();

        ArrayList<pair<Integer, Integer>> vecposi = new ArrayList<>();
        for (int iii = 0; iii < data.nM; iii++) {
            for (int jjj = 0; jjj < rho[iii].size(); jjj++) {
                vecposi.add(pair.of(iii, jjj));
            }
        }
        rndShuffle(vecposi);

        int hbest_exc = 0;
        for (var posi : vecposi) {
            int i = posi.first;
            int j = posi.second;
            for (int k = 0; k < data.nM; k++) {
                if (k == i) {
                    for (int l = 0; l < rho[i].size(); l++) {
                        if (l != j) {
                            int h = 0;
                            if (l < j) h = EvaluateExchangeSame(i, l, j);
                            else h = EvaluateExchangeSame(i, j, l);
                            if (h < hbest_exc || ((h == hbest_exc) && (h < 0) && (rndBool()))) {
                                ibest_exc = i;
                                jbest_exc = j;
                                kbest_exc = i;
                                lbest_exc = l;
                                hbest_exc = h;
                                improved = true;
                            }
                        }
                    }
                } else {
                    for (int l = 0; l < rho[k].size(); l++) {
                        int h = EvaluateExchangeDiff(i, j, k, l);
                        if (h < hbest_exc || ((h == hbest_exc) && (h < 0) && (rndBool()))) {
                            ibest_exc = i;
                            jbest_exc = j;
                            kbest_exc = k;
                            lbest_exc = l;
                            hbest_exc = h;
                            improved = true;
                        }
                    }
                }
            }
            if (improved) break;
        }

        improved = false;
        int hbest_ins = 0;
        rndShuffle(vecposi);
        for (var posi : vecposi) {
            int i = posi.first;
            int j = posi.second;
            if (rho[i].size() > 1) {
                // Backward Swaps
                int h = 0;
                for (int l = j; l > 0; l--) {
                    h += EvaluateSwap(i, l - 1);
                    if (h < hbest_ins || ((h == hbest_ins) && (h < 0) && (rndBool()))) {
                        ibest_ins = i;
                        jbest_ins = j;
                        kbest_ins = i;
                        lbest_ins = l - 1;
                        hbest_ins = h;
                        improved = true;
                    }
                    x[rho[i].get(l - 1)] += data.L[rho[i].get(l)];
                    x[rho[i].get(l)] -= data.L[rho[i].get(l - 1)];
                    Exchange(i, l - 1, i, l);
                }
                // Restore solution
                if (j > 0) {
                    Insert(i, 0, i, j);
                    for (int s = 0; s <= j; s++) x[rho[i].get(s)] = xaux[rho[i].get(s)];
                }
                // Forward Swaps
                h = 0;
                for (int l = j; l < rho[i].size() - 1; l++) {
                    h += EvaluateSwap(i, l);
                    if (h < hbest_ins || ((h == hbest_ins) && (h < 0) && (rndBool()))) {
                        ibest_ins = i;
                        jbest_ins = j;
                        kbest_ins = i;
                        lbest_ins = l + 1;
                        hbest_ins = h;
                        improved = true;
                    }
                    x[rho[i].get(l)] += data.L[rho[i].get(l + 1)];
                    x[rho[i].get(l + 1)] -= data.L[rho[i].get(l)];
                    Exchange(i, l, i, l + 1);
                }
                // Insert into a different row
                int f = rho[i].remove(rho[i].size() - 1);
                h += EvaluateDrop(f);
                for (int k = 0; k < data.nM; k++) {
                    if (k != i) {
                        // Insert at the end of row k
                        x[f] = x[rho[k].get(rho[k].size() - 1)] + (data.L[rho[k].get(rho[k].size() - 1)] + data.L[f]) / 2;
                        rho[k].add(f);
                        int haux = h + EvaluateAdd(f, k);
                        if (haux < hbest_ins || ((haux == hbest_ins) && (haux < 0) && (rndBool()))) {
                            ibest_ins = i;
                            jbest_ins = j;
                            kbest_ins = k;
                            lbest_ins = (rho[k].size() - 1);
                            hbest_ins = haux;
                            improved = true;
                        }
                        //  Backward Swaps
                        for (int l = (rho[k].size() - 1); l > 0; l--) {
                            haux += EvaluateSwap(k, l - 1);
                            if (haux < hbest_ins || ((haux == hbest_ins) && (haux < 0) && (rndBool()))) {
                                ibest_ins = i;
                                jbest_ins = j;
                                kbest_ins = k;
                                lbest_ins = l - 1;
                                hbest_ins = haux;
                                improved = true;
                            }
                            x[rho[k].get(l - 1)] += data.L[rho[k].get(l)];
                            x[rho[k].get(l)] -= data.L[rho[k].get(l - 1)];
                            Exchange(k, l - 1, k, l);
                        }
                        // Restore partial solution
                        rho[k].remove(0);
                        for (int u : rho[k]) x[u] = xaux[u];
                    }
                }
                // Restore solution
                rho[i].add(j, f);
                for (int s = j; s < rho[i].size(); s++) x[rho[i].get(s)] = xaux[rho[i].get(s)];
                if (improved) break;
            }
        }

        // Checks the extended move
        int hbest = 0;
        int move = 0;
        if (hbest_exc < hbest_ins) move = 1;
        else if (hbest_ins < hbest_exc) move = 2;
        else {
            if (rndDbl(0.0, 1.0) < 0.5) move = 1;
            else move = 2;
        }
        improved = false;
        switch (move) {
            case 1:
                if (hbest_exc < 0) {
                    improved = true;
                    obj += hbest_exc;
                    Exchange(throwIfNotInitialized(ibest_exc), throwIfNotInitialized(jbest_exc), throwIfNotInitialized(kbest_exc), throwIfNotInitialized(lbest_exc));
                    CalculateCenters();
                } else improved = false;
                break;
            case 2:
                if (hbest_ins < 0) {
                    improved = true;
                    obj += hbest_ins;
                    Insert(throwIfNotInitialized(ibest_ins), throwIfNotInitialized(jbest_ins), throwIfNotInitialized(kbest_ins), throwIfNotInitialized(lbest_ins));
                    CalculateCenters();
                } else improved = false;
                break;
            default:
                improved = false;
        }

        return improved;
    }

    public boolean ExploreExtendedHybridFirstInc() {

        boolean improved = false;

        int ibest_exc = NOT_INITIALIZED, jbest_exc = NOT_INITIALIZED;
        int kbest_exc = NOT_INITIALIZED, lbest_exc = NOT_INITIALIZED;

        int ibest_ins = NOT_INITIALIZED, jbest_ins = NOT_INITIALIZED;
        int kbest_ins = NOT_INITIALIZED, lbest_ins = NOT_INITIALIZED;

        int[] xaux = x.clone();

        ArrayList<pair<Integer, Integer>> vecposi = new ArrayList<>();
        for (int iii = 0; iii < data.nM; iii++) {
            for (int jjj = 0; jjj < rho[iii].size(); jjj++) {
                vecposi.add(pair.of(iii, jjj));
            }
        }
        rndShuffle(vecposi);

        int hbest_exc = 0;
        for (var posi : vecposi) {
            int i = posi.first;
            int j = posi.second;
            for (int k = 0; k < data.nM; k++) {
                if (k == i) {
                    for (int l = 0; l < rho[i].size(); l++) {
                        if (l != j) {
                            int h = 0;
                            if (l < j) h = EvaluateExchangeSame(i, l, j);
                            else h = EvaluateExchangeSame(i, j, l);
                            if (h < hbest_exc || ((h == hbest_exc) && (h < 0) && (rndBool()))) {
                                ibest_exc = i;
                                jbest_exc = j;
                                kbest_exc = i;
                                lbest_exc = l;
                                hbest_exc = h;
                                improved = true;
                            }
                        }
                    }
                } else {
                    for (int l = 0; l < rho[k].size(); l++) {
                        int h = EvaluateExchangeDiff(i, j, k, l);
                        if (h < hbest_exc || ((h == hbest_exc) && (h < 0) && (rndBool()))) {
                            ibest_exc = i;
                            jbest_exc = j;
                            kbest_exc = k;
                            lbest_exc = l;
                            hbest_exc = h;
                            improved = true;
                        }
                    }
                }
            }
            if (improved) break;
        }

        rndShuffle(vecposi);

        ArrayList<pair<Integer, Integer>> vecposk = new ArrayList<>();
        for (int iii = 0; iii < data.nM; iii++) {
            for (int jjj = 0; jjj < rho[iii].size() + 1; jjj++) {
                 vecposk.add(pair.of(iii, jjj));
            }
        }
        rndShuffle(vecposk);

        improved = false;
        int hbest_ins = 0;
        for (var posi : vecposi) {
            int i = posi.first;
            int j = posi.second;
            if (rho[i].size() > 1) {
                for (var posk : vecposk) {
                    int k = posk.first;
                    int l = posk.second;
                    if ((i != k) || ((i == k) && (l < rho[i].size()))) {
                        Insert(i, j, k, l);
                        int h = EvaluateMove();
                        Insert(k, l, i, j);
                        if (h < hbest_ins || ((h == hbest_ins) && (h < 0) && (rndBool()))) {
                            ibest_ins = i;
                            jbest_ins = j;
                            kbest_ins = k;
                            lbest_ins = l;
                            hbest_ins = h;
                            improved = true;
                        }
                    }
                    if (improved) break;
                }
                if (improved) break;
            }
        }

        // Checks the extended move
        int hbest = 0;
        int move = 0;
        if (hbest_exc < hbest_ins) move = 1;
        else if (hbest_ins < hbest_exc) move = 2;
        else {
            if (rndDbl(0.0, 1.0) < 0.5) move = 1;
            else move = 2;
        }
        improved = false;
        switch (move) {
            case 1:
                if (hbest_exc < 0) {
                    improved = true;
                    obj += hbest_exc;
                    Exchange(throwIfNotInitialized(ibest_exc), throwIfNotInitialized(jbest_exc), throwIfNotInitialized(kbest_exc), throwIfNotInitialized(lbest_exc));
                    CalculateCenters();
                } else improved = false;
                break;
            case 2:
                if (hbest_ins < 0) {
                    improved = true;
                    obj += hbest_ins;
                    Insert(throwIfNotInitialized(ibest_ins), throwIfNotInitialized(jbest_ins), throwIfNotInitialized(kbest_ins), throwIfNotInitialized(lbest_ins));
                    CalculateCenters();
                } else improved = false;
                break;
            default:
                improved = false;
        }

        return improved;
    }

    public boolean ExploreExtendedFirstBestInc() {

        boolean improved = false;

        int ibest_exc = NOT_INITIALIZED, jbest_exc = NOT_INITIALIZED;
        int kbest_exc = NOT_INITIALIZED, lbest_exc = NOT_INITIALIZED;

        int ibest_ins = NOT_INITIALIZED, jbest_ins = NOT_INITIALIZED;
        int kbest_ins = NOT_INITIALIZED, lbest_ins = NOT_INITIALIZED;

        int[] xaux = x.clone();

        ArrayList<pair<Integer, Integer>> vecposi = new ArrayList<>();
        for (int iii = 0; iii < data.nM; iii++) {
            for (int jjj = 0; jjj < rho[iii].size(); jjj++) {
                vecposi.add(pair.of(iii, jjj));
            }
        }
        rndShuffle(vecposi);

        ArrayList<pair<Integer, Integer>> vecposk = new ArrayList<>();
        for (int iii = 0; iii < data.nM; iii++) {
            for (int jjj = 0; jjj < rho[iii].size(); jjj++) {
                // El unico vecposk que no tiene +1 el segundo bucle?
                vecposk.add(pair.of(iii, jjj));
            }
        }
        rndShuffle(vecposk);

        int hbest_exc = 0;
        for (var posi : vecposi) {
            int i = posi.first;
            int j = posi.second;
            for (var posk : vecposk) {
                int k = posk.first;
                int l = posk.second;
                if (k == i) {
                    if (l != j) {
                        int h = 0;
                        if (l < j) h = EvaluateExchangeSame(i, l, j);
                        else h = EvaluateExchangeSame(i, j, l);
                        if (h < hbest_exc || ((h == hbest_exc) && (h < 0) && (rndBool()))) {
                            ibest_exc = i;
                            jbest_exc = j;
                            kbest_exc = i;
                            lbest_exc = l;
                            hbest_exc = h;
                            improved = true;
                        }
                    }
                } else {
                    int h = EvaluateExchangeDiff(i, j, k, l);
                    if (h < hbest_exc || ((h == hbest_exc) && (h < 0) && (rndBool()))) {
                        ibest_exc = i;
                        jbest_exc = j;
                        kbest_exc = k;
                        lbest_exc = l;
                        hbest_exc = h;
                        improved = true;
                    }
                }
                if (improved) break;
            }
            if (improved) break;
        }

        int hbest_ins = 0;
        for (int i = 0; i < data.nM; i++) {
            if (rho[i].size() > 1) {
                for (int j = 0; j < rho[i].size(); j++) {
                    // Backward Swaps
                    int h = 0;
                    for (int l = j; l > 0; l--) {
                        h += EvaluateSwap(i, l - 1);
                        //h += EvaluateExchangeSame(i,l-1,l);
                        if (h < hbest_ins || ((h == hbest_ins) && (h < 0) && (rndBool()))) {
                            ibest_ins = i;
                            jbest_ins = j;
                            kbest_ins = i;
                            lbest_ins = l - 1;
                            hbest_ins = h;
                        }
                        x[rho[i].get(l - 1)] += data.L[rho[i].get(l)];
                        x[rho[i].get(l)] -= data.L[rho[i].get(l - 1)];
                        Exchange(i, l - 1, i, l);
                    }
                    // Restore solution
                    if (j > 0) {
                        Insert(i, 0, i, j);
                        for (int s = 0; s <= j; s++) x[rho[i].get(s)] = xaux[rho[i].get(s)];
                    }
                    // Forward Swaps
                    h = 0;
                    for (int l = j; l < rho[i].size() - 1; l++) {
                        h += EvaluateSwap(i, l);
                        //h += EvaluateExchangeSame(i,l,l+1);
                        if (h < hbest_ins || ((h == hbest_ins) && (h < 0) && (rndBool()))) {
                            ibest_ins = i;
                            jbest_ins = j;
                            kbest_ins = i;
                            lbest_ins = l + 1;
                            hbest_ins = h;
                        }
                        x[rho[i].get(l)] += data.L[rho[i].get(l + 1)];
                        x[rho[i].get(l + 1)] -= data.L[rho[i].get(l)];
                        Exchange(i, l, i, l + 1);
                    }
                    // Insert into a different row
                    int f = rho[i].remove(rho[i].size() - 1);
                    h += EvaluateDrop(f);
                    for (int k = 0; k < data.nM; k++) {
                        if (k != i) {
                            // Insert at the end of row k
                            if (!rho[k].isEmpty())
                                x[f] = x[rho[k].get(rho[k].size() - 1)] + (data.L[rho[k].get(rho[k].size() - 1)] + data.L[f]) / 2;
                            else x[f] = data.L[f] / 2;
                            rho[k].add(f);
                            int haux = h + EvaluateAdd(f, k);
                            if (haux < hbest_ins || ((haux == hbest_ins) && (haux < 0) && (rndBool()))) {
                                ibest_ins = i;
                                jbest_ins = j;
                                kbest_ins = k;
                                lbest_ins = (rho[k].size() - 1);
                                hbest_ins = haux;
                            }
                            //  Backward Swaps
                            for (int l = (rho[k].size() - 1); l > 0; l--) {
                                haux += EvaluateSwap(k, l - 1);
                                //haux += EvaluateExchangeSame(k,l-1,l);
                                if (haux < hbest_ins || ((haux == hbest_ins) && (haux < 0) && (rndBool()))) {
                                    ibest_ins = i;
                                    jbest_ins = j;
                                    kbest_ins = k;
                                    lbest_ins = l - 1;
                                    hbest_ins = haux;
                                }
                                x[rho[k].get(l - 1)] += data.L[rho[k].get(l)];
                                x[rho[k].get(l)] -= data.L[rho[k].get(l - 1)];
                                Exchange(k, l - 1, k, l);
                            }
                            // Restore partial solution
                            rho[k].remove(0);
                            for (int u : rho[k]) x[u] = xaux[u];
                        }
                    }
                    // Restore solution
                    rho[i].add(j, f);
                    for (int s = j; s < rho[i].size(); s++) x[rho[i].get(s)] = xaux[rho[i].get(s)];
                }
            }
        }

        // Checks the extended move
        int hbest = 0;
        int move = 0;
        if (hbest_exc < hbest_ins) move = 1;
        else if (hbest_ins < hbest_exc) move = 2;
        else {
            if (rndDbl(0.0, 1.0) < 0.5) move = 1;
            else move = 2;
        }
        improved = false;
        switch (move) {
            case 1:
                if (hbest_exc < 0) {
                    improved = true;
                    obj += hbest_exc;
                    Exchange(throwIfNotInitialized(ibest_exc), throwIfNotInitialized(jbest_exc), throwIfNotInitialized(kbest_exc), throwIfNotInitialized(lbest_exc));
                    CalculateCenters();
                } else improved = false;
                break;
            case 2:
                if (hbest_ins < 0) {
                    improved = true;
                    obj += hbest_ins;
                    Insert(throwIfNotInitialized(ibest_ins), throwIfNotInitialized(jbest_ins), throwIfNotInitialized(kbest_ins), throwIfNotInitialized(lbest_ins));
                    CalculateCenters();
                } else improved = false;
                break;
            default:
                improved = false;
        }

        return improved;
    }

    public boolean ExploreExtendedFirstHybridInc() {

        boolean improved = false;

        int ibest_exc = NOT_INITIALIZED, jbest_exc = NOT_INITIALIZED;
        int kbest_exc = NOT_INITIALIZED, lbest_exc = NOT_INITIALIZED;

        int ibest_ins = NOT_INITIALIZED, jbest_ins = NOT_INITIALIZED;
        int kbest_ins = NOT_INITIALIZED, lbest_ins = NOT_INITIALIZED;

        int[] xaux = x.clone();

        ArrayList<pair<Integer, Integer>> vecposi = new ArrayList<>();
        for (int iii = 0; iii < data.nM; iii++) {
            for (int jjj = 0; jjj < rho[iii].size(); jjj++) {
                vecposi.add(pair.of(iii, jjj));
            }
        }
        rndShuffle(vecposi);

        ArrayList<pair<Integer, Integer>> vecposk = new ArrayList<>();
        for (int iii = 0; iii < data.nM; iii++) {
            for (int jjj = 0; jjj < rho[iii].size(); jjj++) {
                vecposk.add(pair.of(iii, jjj));
            }
        }
        rndShuffle(vecposk);

        int hbest_exc = 0;
        for (var posi : vecposi) {
            int i = posi.first;
            int j = posi.second;
            for (var posk : vecposk) {
                int k = posk.first;
                int l = posk.second;
                if (k == i) {
                    if (l != j) {
                        int h = 0;
                        if (l < j) h = EvaluateExchangeSame(i, l, j);
                        else h = EvaluateExchangeSame(i, j, l);
                        if (h < hbest_exc || ((h == hbest_exc) && (h < 0) && (rndBool()))) {
                            ibest_exc = i;
                            jbest_exc = j;
                            kbest_exc = i;
                            lbest_exc = l;
                            hbest_exc = h;
                            improved = true;
                        }
                    }
                } else {
                    int h = EvaluateExchangeDiff(i, j, k, l);
                    if (h < hbest_exc || ((h == hbest_exc) && (h < 0) && (rndBool()))) {
                        ibest_exc = i;
                        jbest_exc = j;
                        kbest_exc = k;
                        lbest_exc = l;
                        hbest_exc = h;
                        improved = true;
                    }
                }
                if (improved) break;
            }
            if (improved) break;
        }

        rndShuffle(vecposi);

        improved = false;
        int hbest_ins = 0;
        for (var posi : vecposi) {
            int i = posi.first;
            int j = posi.second;
            if (rho[i].size() > 1) {
                // Backward Swaps
                int h = 0;
                for (int l = j; l > 0; l--) {
                    h += EvaluateSwap(i, l - 1);
                    if (h < hbest_ins || ((h == hbest_ins) && (h < 0) && (rndBool()))) {
                        ibest_ins = i;
                        jbest_ins = j;
                        kbest_ins = i;
                        lbest_ins = l - 1;
                        hbest_ins = h;
                        improved = true;
                    }
                    x[rho[i].get(l - 1)] += data.L[rho[i].get(l)];
                    x[rho[i].get(l)] -= data.L[rho[i].get(l - 1)];
                    Exchange(i, l - 1, i, l);
                }
                // Restore solution
                if (j > 0) {
                    Insert(i, 0, i, j);
                    for (int s = 0; s <= j; s++) x[rho[i].get(s)] = xaux[rho[i].get(s)];
                }
                // Forward Swaps
                h = 0;
                for (int l = j; l < rho[i].size() - 1; l++) {
                    h += EvaluateSwap(i, l);
                    if (h < hbest_ins || ((h == hbest_ins) && (h < 0) && (rndBool()))) {
                        ibest_ins = i;
                        jbest_ins = j;
                        kbest_ins = i;
                        lbest_ins = l + 1;
                        hbest_ins = h;
                        improved = true;
                    }
                    x[rho[i].get(l)] += data.L[rho[i].get(l + 1)];
                    x[rho[i].get(l + 1)] -= data.L[rho[i].get(l)];
                    Exchange(i, l, i, l + 1);
                }
                // Insert into a different row
                int f = rho[i].remove(rho[i].size() - 1);
                h += EvaluateDrop(f);
                for (int k = 0; k < data.nM; k++) {
                    if (k != i) {
                        // Insert at the end of row k
                        x[f] = x[rho[k].get(rho[k].size() - 1)] + (data.L[rho[k].get(rho[k].size() - 1)] + data.L[f]) / 2;
                        rho[k].add(f);
                        int haux = h + EvaluateAdd(f, k);
                        if (haux < hbest_ins || ((haux == hbest_ins) && (haux < 0) && (rndBool()))) {
                            ibest_ins = i;
                            jbest_ins = j;
                            kbest_ins = k;
                            lbest_ins = (rho[k].size() - 1);
                            hbest_ins = haux;
                            improved = true;
                        }
                        //  Backward Swaps
                        for (int l = (rho[k].size() - 1); l > 0; l--) {
                            haux += EvaluateSwap(k, l - 1);
                            if (haux < hbest_ins || ((haux == hbest_ins) && (haux < 0) && (rndBool()))) {
                                ibest_ins = i;
                                jbest_ins = j;
                                kbest_ins = k;
                                lbest_ins = l - 1;
                                hbest_ins = haux;
                                improved = true;
                            }
                            x[rho[k].get(l - 1)] += data.L[rho[k].get(l)];
                            x[rho[k].get(l)] -= data.L[rho[k].get(l - 1)];
                            Exchange(k, l - 1, k, l);
                        }
                        // Restore partial solution
                        rho[k].remove(0);
                        for (int u : rho[k]) x[u] = xaux[u];
                    }
                }
                // Restore solution
                rho[i].add(j, f);
                for (int s = j; s < rho[i].size(); s++) x[rho[i].get(s)] = xaux[rho[i].get(s)];
                if (improved) break;
            }
        }

        // Checks the extended move
        int hbest = 0;
        int move = 0;
        if (hbest_exc < hbest_ins) move = 1;
        else if (hbest_ins < hbest_exc) move = 2;
        else {
            if (rndDbl(0.0, 1.0) < 0.5) move = 1;
            else move = 2;
        }
        improved = false;
        switch (move) {
            case 1:
                if (hbest_exc < 0) {
                    improved = true;
                    obj += hbest_exc;
                    Exchange(throwIfNotInitialized(ibest_exc), throwIfNotInitialized(jbest_exc), throwIfNotInitialized(kbest_exc), throwIfNotInitialized(lbest_exc));
                    CalculateCenters();
                } else improved = false;
                break;
            case 2:
                if (hbest_ins < 0) {
                    improved = true;
                    obj += hbest_ins;
                    Insert(throwIfNotInitialized(ibest_ins), throwIfNotInitialized(jbest_ins), throwIfNotInitialized(kbest_ins), throwIfNotInitialized(lbest_ins));
                    CalculateCenters();
                } else improved = false;
                break;
            default:
                improved = false;
        }

        return improved;
    }

    public boolean ExploreExtendedFirstFirstInc() {

        boolean improved = false;

        int ibest_exc = NOT_INITIALIZED, jbest_exc = NOT_INITIALIZED;
        int kbest_exc = NOT_INITIALIZED, lbest_exc = NOT_INITIALIZED;

        int ibest_ins = NOT_INITIALIZED, jbest_ins = NOT_INITIALIZED;
        int kbest_ins = NOT_INITIALIZED, lbest_ins = NOT_INITIALIZED;

        int[] xaux = x.clone();

        ArrayList<pair<Integer, Integer>> vecposi = new ArrayList<>();
        for (int iii = 0; iii < data.nM; iii++) {
            for (int jjj = 0; jjj < rho[iii].size(); jjj++) {
                vecposi.add(pair.of(iii, jjj));
            }
        }
        rndShuffle(vecposi);

        ArrayList<pair<Integer, Integer>> vecposk = new ArrayList<>();
        for (int iii = 0; iii < data.nM; iii++) {
            for (int jjj = 0; jjj < rho[iii].size(); jjj++) {
                vecposk.add(pair.of(iii, jjj));
            }
        }
        rndShuffle(vecposk);

        int hbest_exc = 0;
        for (var posi : vecposi) {
            int i = posi.first;
            int j = posi.second;
            for (var posk : vecposk) {
                int k = posk.first;
                int l = posk.second;
                if (k == i) {
                    if (l != j) {
                        int h = 0;
                        if (l < j) h = EvaluateExchangeSame(i, l, j);
                        else h = EvaluateExchangeSame(i, j, l);
                        if (h < hbest_exc || ((h == hbest_exc) && (h < 0) && (rndBool()))) {
                            ibest_exc = i;
                            jbest_exc = j;
                            kbest_exc = i;
                            lbest_exc = l;
                            hbest_exc = h;
                            improved = true;
                        }
                    }
                } else {
                    int h = EvaluateExchangeDiff(i, j, k, l);
                    if (h < hbest_exc || ((h == hbest_exc) && (h < 0) && (rndBool()))) {
                        ibest_exc = i;
                        jbest_exc = j;
                        kbest_exc = k;
                        lbest_exc = l;
                        hbest_exc = h;
                        improved = true;
                    }
                }
                if (improved) break;
            }
            if (improved) break;
        }

        rndShuffle(vecposi);

        vecposk.clear();
        for (int iii = 0; iii < data.nM; iii++) {
            for (int jjj = 0; jjj < rho[iii].size()+1; jjj++) {
                vecposk.add(pair.of(iii, jjj));
            }
        }
        rndShuffle(vecposk);

        improved = false;
        int hbest_ins = 0;
        for (var posi : vecposi) {
            int i = posi.first;
            int j = posi.second;
            if (rho[i].size() > 1) {
                for (var posk : vecposk) {
                    int k = posk.first;
                    int l = posk.second;
                    if ((i != k) || ((i == k) && (l < rho[i].size()))) {
                        Insert(i, j, k, l);
                        int h = EvaluateMove();
                        Insert(k, l, i, j);
                        if (h < hbest_ins || ((h == hbest_ins) && (h < 0) && (rndBool()))) {
                            ibest_ins = i;
                            jbest_ins = j;
                            kbest_ins = k;
                            lbest_ins = l;
                            hbest_ins = h;
                            improved = true;
                        }
                    }
                    if (improved) break;
                }
                if (improved) break;
            }
        }

        // Checks the extended move
        int hbest = 0;
        int move = 0;
        if (hbest_exc < hbest_ins) move = 1;
        else if (hbest_ins < hbest_exc) move = 2;
        else {
            if (rndDbl(0.0, 1.0) < 0.5) move = 1;
            else move = 2;
        }
        improved = false;
        switch (move) {
            case 1:
                if (hbest_exc < 0) {
                    improved = true;
                    obj += hbest_exc;
                    Exchange(throwIfNotInitialized(ibest_exc), throwIfNotInitialized(jbest_exc), throwIfNotInitialized(kbest_exc), throwIfNotInitialized(lbest_exc));
                    CalculateCenters();
                } else improved = false;
                break;
            case 2:
                if (hbest_ins < 0) {
                    improved = true;
                    obj += hbest_ins;
                    Insert(throwIfNotInitialized(ibest_ins), throwIfNotInitialized(jbest_ins), throwIfNotInitialized(kbest_ins), throwIfNotInitialized(lbest_ins));
                    CalculateCenters();
                } else improved = false;
                break;
            default:
                improved = false;
        }

        return improved;
    }
    // END extended.cpp
    // START insert.cp

    public void Insert(int i, int j, int k, int l) {
        int f = rho[i].remove(j);
        ;
        rho[k].add(l, f);
    }

    public int EvaluateSwap(int i, int j) {

        // Variables involved in the move
        int u = rho[i].get(j);
        int v = rho[i].get(j + 1);
        int xu = x[u];
        int xv = x[v];
        int Lu = data.L[u];
        int Lv = data.L[v];
        int xup = xu + Lv;
        int xvp = xv - Lu;
        int Luv = Lu - 2 * xv;
        int Lvu = Lv + 2 * xu;

        // Delta_a: Evaluates the cost change of facilities u and v with all the facilities in zones 1-4
        int delta1 = 0, delta2 = 0, delta4 = 0, delta5 = 0, delta6 = 0;
        for (int s = 0; s < j; s++) delta1 += data.W[u][rho[i].get(s)];
        for (int s = j + 2; s < rho[i].size(); s++) delta1 -= data.W[u][rho[i].get(s)];
        for (int s = 0; s < j; s++) delta2 += data.W[v][rho[i].get(s)];
        for (int s = j + 2; s < rho[i].size(); s++) delta2 -= data.W[v][rho[i].get(s)];

        for (int r = 0; r < data.nM; r++) {
            if (r == i) continue;
            for (int s = 0; s < rho[r].size(); s++) {
                int f = rho[r].get(s);
                int xf = x[f];
                if (xf < xu) delta4 += data.W[u][f];
                else if (xf < xup) delta6 += data.W[u][f] * (Lvu - 2 * xf);
                else delta4 -= data.W[u][f];
                if (xf < xvp) delta5 += data.W[v][f];
                else if (xf < xv) delta6 += data.W[v][f] * (Luv + 2 * xf);
                else delta5 -= data.W[v][f];
            }
        }

        return Lv * (delta1 + delta4) - Lu * (delta2 + delta5) + delta6;
    }

    public int EvaluateDrop(int f) {

        int h = 0;

        // Evaluates the drop of f from the end of its row
        for (int r = 0; r < data.nM; r++)
            for (int s = 0; s < rho[r].size(); s++) h -= data.W[f][rho[r].get(s)] * abs(x[f] - x[rho[r].get(s)]);

        return h;
    }

    public int EvaluateAdd(int f, int k) {

        int h = 0;

        // Evaluates the addition of f to the end of row k
        for (int r = 0; r < data.nM; r++) {
            if (r != k)
                for (int s = 0; s < rho[r].size(); s++) h += data.W[f][rho[r].get(s)] * abs(x[f] - x[rho[r].get(s)]);
            else for (int s = 0; s < rho[k].size() - 1; s++) h += data.W[f][rho[k].get(s)] * (x[f] - x[rho[k].get(s)]);
        }

        return h;
    }

    public boolean ExploreInsert(int strategy) {

        boolean improved = false;

        int ibest = NOT_INITIALIZED, jbest = NOT_INITIALIZED;
        int kbest = NOT_INITIALIZED, lbest = NOT_INITIALIZED;

        ArrayList<pair<Integer, Integer>> vecpos = new ArrayList<>();
        for (int iii = 0; iii < data.nM; iii++) {
            for (int jjj = 0; jjj < rho[iii].size(); jjj++) {
                vecpos.add(pair.of(iii, jjj));
            }
        }
        rndShuffle(vecpos);

        int hbest = 0;
        for (var posi : vecpos) {
            int i = posi.first;
            int j = posi.second;
            if (rho[i].size() > 1) {
                for (var posk : vecpos) {
                    int k = posk.first;
                    int l = posk.second;
                    if ((i != k) || ((i == k) && (l < rho[i].size()))) {
                        Insert(i, j, k, l);
                        int h = EvaluateMove();
                        Insert(k, l, i, j);
                        if (h < hbest) {
                            ibest = i;
                            jbest = j;
                            kbest = k;
                            lbest = l;
                            hbest = h;
                            improved = true;
                        }
                    }
                    if ((strategy > 1) && (improved)) break;
                }
                if ((strategy > 0) && (improved)) break;
            }
        }

        if (improved) {
            obj += hbest;
            Insert(throwIfNotInitialized(ibest), throwIfNotInitialized(jbest), throwIfNotInitialized(kbest), throwIfNotInitialized(lbest));
            CalculateCenters();
        }

        return improved;
    }

    public boolean ExploreInsertInc(int strategy) {

        boolean improved = false;

        int ibest = NOT_INITIALIZED, jbest = NOT_INITIALIZED;
        int kbest = NOT_INITIALIZED, lbest = NOT_INITIALIZED;

        int[] xaux = x.clone();

        ArrayList<pair<Integer, Integer>> vecposi = new ArrayList<>();
        for (int iii = 0; iii < data.nM; iii++) {
            for (int jjj = 0; jjj < rho[iii].size(); jjj++) {
                vecposi.add(pair.of(iii, jjj));
            }
        }
        rndShuffle(vecposi);

        int hbest = 0;
        for (var posi : vecposi) {
            int i = posi.first;
            int j = posi.second;
            if (rho[i].size() > 1) {
                // Backward Swaps
                int h = 0;
                for (int l = j; l > 0; l--) {
                    h += EvaluateSwap(i, l - 1);
                    if (h < hbest) {
                        ibest = i;
                        jbest = j;
                        kbest = i;
                        lbest = l - 1;
                        hbest = h;
                        improved = true;
                    }
                    x[rho[i].get(l - 1)] += data.L[rho[i].get(l)];
                    x[rho[i].get(l)] -= data.L[rho[i].get(l - 1)];
                    Exchange(i, l - 1, i, l);
                }
                // Restore solution
                if (j > 0) {
                    Insert(i, 0, i, j);
                    for (int s = 0; s <= j; s++) x[rho[i].get(s)] = xaux[rho[i].get(s)];
                }
                // Forward Swaps
                h = 0;
                for (int l = j; l < rho[i].size() - 1; l++) {
                    h += EvaluateSwap(i, l);
                    if (h < hbest) {
                        ibest = i;
                        jbest = j;
                        kbest = i;
                        lbest = l + 1;
                        hbest = h;
                        improved = true;
                    }
                    x[rho[i].get(l)] += data.L[rho[i].get(l + 1)];
                    x[rho[i].get(l + 1)] -= data.L[rho[i].get(l)];
                    Exchange(i, l, i, l + 1);
                }
                // Insert into a different row
                int f = rho[i].remove(rho[i].size() - 1);
                h += EvaluateDrop(f);
                for (int k = 0; k < data.nM; k++) {
                    if (k != i) {
                        // Insert at the end of row k
                        x[f] = x[rho[k].get(rho[k].size() - 1)] + (data.L[rho[k].get(rho[k].size() - 1)] + data.L[f]) / 2;
                        rho[k].add(f);
                        int haux = h + EvaluateAdd(f, k);
                        if (haux < hbest) {
                            ibest = i;
                            jbest = j;
                            kbest = k;
                            lbest = (rho[k].size() - 1);
                            hbest = haux;
                            improved = true;
                        }
                        //  Backward Swaps
                        for (int l = (rho[k].size() - 1); l > 0; l--) {
                            haux += EvaluateSwap(k, l - 1);
                            if (haux < hbest) {
                                ibest = i;
                                jbest = j;
                                kbest = k;
                                lbest = l - 1;
                                hbest = haux;
                                improved = true;
                            }
                            x[rho[k].get(l - 1)] += data.L[rho[k].get(l)];
                            x[rho[k].get(l)] -= data.L[rho[k].get(l - 1)];
                            Exchange(k, l - 1, k, l);
                        }
                        // Restore partial solution
                        rho[k].remove(0);
                        for (int u : rho[k]) x[u] = xaux[u];
                    }
                }
                // Restore solution
                rho[i].add(j, f);
                for (int s = j; s < rho[i].size(); s++) x[rho[i].get(s)] = xaux[rho[i].get(s)];
                if ((strategy > 0) && (improved)) break;
            }
        }

        if (improved) {
            obj += hbest;
            Insert(throwIfNotInitialized(ibest), throwIfNotInitialized(jbest), throwIfNotInitialized(kbest), throwIfNotInitialized(lbest));
            CalculateCenters();
        }

        return improved;
    }


    public boolean ExploreInsertHorizontalBest() {

        boolean improved = false;

        int ibest = NOT_INITIALIZED, jbest = NOT_INITIALIZED;
        int kbest = NOT_INITIALIZED, lbest = NOT_INITIALIZED;

        int hbest = 0;
        for (int i = 0; i < data.nM; i++) {
            if (rho[i].size() > 1) {
                for (int j = 0; j < rho[i].size(); j++) {
                    for (int l = j + 1; l < rho[i].size(); l++) {
                        Insert(i, j, i, l);
                        int h = EvaluateMove();
                        Insert(i, l, i, j);
                        if (h < hbest || ((h == hbest) && (h < 0) && (rndBool()))) {
                            ibest = i;
                            jbest = j;
                            kbest = i;
                            lbest = l;
                            hbest = h;
                            improved = true;
                        }
                    }
                }
            }
        }

        if (improved) {
            obj += hbest;
            Exchange(throwIfNotInitialized(ibest), throwIfNotInitialized(jbest), throwIfNotInitialized(kbest), throwIfNotInitialized(lbest));
            CalculateCenters();
        }

        return improved;
    }

    public boolean ExploreInsertHorizontalBestInc() {

        boolean improved = false;

        int ibest = NOT_INITIALIZED, jbest = NOT_INITIALIZED;
        int kbest = NOT_INITIALIZED, lbest = NOT_INITIALIZED;

        int[] xaux = x.clone();

        int hbest = 0;
        for (int i = 0; i < data.nM; i++) {
            if (rho[i].size() > 1) {
                for (int j = 0; j < rho[i].size(); j++) {
                    // Backward Swaps
                    int h = 0;
                    for (int l = j; l > 0; l--) {
                        h += EvaluateSwap(i, l - 1);
                        if (h < hbest || ((h == hbest) && (h < 0) && (rndBool()))) {
                            ibest = i;
                            jbest = j;
                            kbest = i;
                            lbest = l - 1;
                            hbest = h;
                            improved = true;
                        }
                        x[rho[i].get(l - 1)] += data.L[rho[i].get(l)];
                        x[rho[i].get(l)] -= data.L[rho[i].get(l - 1)];
                        Exchange(i, l - 1, i, l);
                    }
                    // Restore solution
                    if (j > 0) {
                        Insert(i, 0, i, j);
                        for (int s = 0; s <= j; s++) x[rho[i].get(s)] = xaux[rho[i].get(s)];
                    }
                    // Forward Swaps
                    h = 0;
                    for (int l = j; l < rho[i].size() - 1; l++) {
                        h += EvaluateSwap(i, l);
                        if (h < hbest || ((h == hbest) && (h < 0) && (rndBool()))) {
                            ibest = i;
                            jbest = j;
                            kbest = i;
                            lbest = l + 1;
                            hbest = h;
                            improved = true;
                        }
                        x[rho[i].get(l)] += data.L[rho[i].get(l + 1)];
                        x[rho[i].get(l + 1)] -= data.L[rho[i].get(l)];
                        Exchange(i, l, i, l + 1);
                    }
                    // Restore solution
                    if (j < rho[i].size() - 1) {
                        Insert(i, (rho[i].size() - 1), i, j);
                        for (int s = j; s < rho[i].size(); s++) x[rho[i].get(s)] = xaux[rho[i].get(s)];
                    }
                }
            }
        }

        if (improved) {
            obj += hbest;
            Insert(throwIfNotInitialized(ibest), throwIfNotInitialized(jbest), throwIfNotInitialized(kbest), throwIfNotInitialized(lbest));
            CalculateCenters();
        }

        return improved;
    }

    public boolean ExploreInsertVerticalBest() {

        boolean improved = false;

        int ibest = NOT_INITIALIZED, jbest = NOT_INITIALIZED;
        int kbest = NOT_INITIALIZED, lbest = NOT_INITIALIZED;

        int hbest = 0;
        for (int i = 0; i < data.nM; i++) {
            if (rho[i].size() > 1) {
                for (int j = 0; j < rho[i].size(); j++) {
                    for (int k = 0; k < data.nM; k++) {
                        if (k != i) {
                            for (int l = 0; l < rho[k].size() - 1; l++) {
                                Insert(i, j, k, l);
                                int h = EvaluateMove();
                                Insert(k, l, i, j);
                                if (h < hbest || ((h == hbest) && (h < 0) && (rndBool()))) {
                                    ibest = i;
                                    jbest = j;
                                    kbest = k;
                                    lbest = l;
                                    hbest = h;
                                    improved = true;
                                }
                            }
                        }
                    }
                }
            }
        }

        if (improved) {
            obj += hbest;
            Insert(throwIfNotInitialized(ibest), throwIfNotInitialized(jbest), throwIfNotInitialized(kbest), throwIfNotInitialized(lbest));
            CalculateCenters();
        }

        return improved;
    }

    public boolean ExploreInsertVerticalBestInc() {

        boolean improved = false;

        int ibest = NOT_INITIALIZED, jbest = NOT_INITIALIZED;
        int kbest = NOT_INITIALIZED, lbest = NOT_INITIALIZED;

        int[] xaux = x.clone();

        int hbest = 0;
        for (int i = 0; i < data.nM; i++) {
            if (rho[i].size() > 1) {
                for (int j = 0; j < rho[i].size(); j++) {
                    // Insert f at the end of row i
                    Insert(i, j, i, (rho[i].size() - 1));
                    int h = EvaluateMove();
                    // Insert into a different row
                    int f = rho[i].remove(rho[i].size() - 1);
                    h += EvaluateDrop(f);
                    for (int k = 0; k < data.nM; k++) {
                        if (k != i) {
                            // Insert at the end of row k
                            x[f] = x[rho[k].get(rho[k].size() - 1)] + (data.L[rho[k].get(rho[k].size() - 1)] + data.L[f]) / 2;
                            rho[k].add(f);
                            int haux = h + EvaluateAdd(f, k);
                            if (haux < hbest || ((haux == hbest) && (h < 0) && (rndBool()))) {
                                ibest = i;
                                jbest = j;
                                kbest = k;
                                lbest = (rho[k].size() - 1);
                                hbest = haux;
                                improved = true;
                            }
                            //  Backward Swaps
                            for (int l = (rho[k].size() - 1); l > 0; l--) {
                                haux += EvaluateSwap(k, l - 1);
                                if (haux < hbest || ((haux == hbest) && (h < 0) && (rndBool()))) {
                                    ibest = i;
                                    jbest = j;
                                    kbest = k;
                                    lbest = l - 1;
                                    hbest = haux;
                                    improved = true;
                                }
                                x[rho[k].get(l - 1)] += data.L[rho[k].get(l)];
                                x[rho[k].get(l)] -= data.L[rho[k].get(l - 1)];
                                Exchange(k, l - 1, k, l);
                            }
                            // Restore partial solution
                            rho[k].remove(0);
                            for (int u : rho[k]) x[u] = xaux[u];
                        }
                    }
                    // Restore solution
                    rho[i].add(j, f);
                    for (int s = j; s < rho[i].size(); s++) x[rho[i].get(s)] = xaux[rho[i].get(s)];
                }
            }
        }

        if (improved) {
            obj += hbest;
            Insert(throwIfNotInitialized(ibest), throwIfNotInitialized(jbest), throwIfNotInitialized(kbest), throwIfNotInitialized(lbest));
            CalculateCenters();
        }

        return improved;
    }
    // END insert.cpp

    // START shakes.cpp
    public void Shake1(int ks, int move) {

        int i, j, k, l;

        if (move == 1) {
            for (int iks = 0; iks < ks; iks++) {
                i = rndInt(0, data.nM - 1);
                j = rndInt(0, (rho[i].size() - 1));
                do {
                    k = rndInt(0, data.nM - 1);
                } while ((k == i) && (rho[i].size() == 1));
                if (i == k) do {
                    l = rndInt(0, (rho[i].size() - 1));
                } while (l == j);
                else l = rndInt(0, (rho[k].size() - 1));
                Exchange(i, j, k, l);
            }
        } else {
            for (int iks = 0; iks < ks; iks++) {
                do {
                    i = rndInt(0, data.nM - 1);
                } while (rho[i].size() == 1);
                j = rndInt(0, (rho[i].size() - 1));
                do {
                    k = rndInt(0, data.nM - 1);
                } while ((k == i) && (rho[i].size() == 1));
                if (i == k) do {
                    l = rndInt(0, (rho[i].size() - 1));
                } while (l == j);
                else l = rndInt(0, rho[k].size());
                Insert(i, j, k, l);
            }
        }

    }


    //----------------------------------------
// Shake 2: Horizontal move
//----------------------------------------
    public void Shake2(int ks, int move) {

        int i, j, l;

        if (data.nN > data.nM) {
            for (int iks = 0; iks < ks; iks++) {
                do {
                    i = rndInt(0, data.nM - 1);
                } while (rho[i].size() < 2);
                j = rndInt(0, (rho[i].size() - 1));
                do {
                    l = rndInt(0, (rho[i].size() - 1));
                } while (l == j);
                if (move == 1) {
                    Exchange(i, j, i, l);
                } else {
                    Insert(i, j, i, l);
                }
            }
        }

    }

    //----------------------------------------
// Shake 3: Vertical-j move
//----------------------------------------
    public void Shake3(int ks, int move) {

        int i, k, j;

        for (int iks = 0; iks < ks; iks++) {
            if (move == 1) i = rndInt(0, data.nM - 1);
            else do {
                i = rndInt(0, data.nM - 1);
            } while (rho[i].size() < 2);
            do {
                k = rndInt(0, data.nM - 1);
            } while (k == i);
            if (rho[i].size() > rho[k].size()) j = rndInt(0, (rho[k].size() - 1));
            else j = rndInt(0, (rho[i].size() - 1));
            if (move == 1) Exchange(i, j, k, j);
            else Insert(i, j, k, j);
        }

    }

    //----------------------------------------
// Shake 4: Vertical-aligned move
//----------------------------------------
//void Shake4(int ks,int move) {
//
//    int i, j, k, l;
//
//    for(int ik=0; ik<ks; ik++) {
//        if (move == 1) i = rndInt(0, data.nM - 1);
//        else do { i = rndInt(0, data.nM - 1); } while (rho[i].size() < 2);
//        j = rndInt(0, (rho[i].size() - 1));
//        int xij = x[rho[i].get(j)];
//        do { k = rndInt(0, data.nM - 1); } while (k == i);
//        l = 0;
//        int dprev = x[rho[k].get(l)] - xij;
//        while (dprev < 0) {
//            l++;
//            if (l > rho[k].size() - 1) {
//                l--;
//                break;
//            } else {
//                int dnext = x[rho[k].get(l)] - xij;
//                if (dnext >= 0) {
//                    if (abs(dprev) < dnext) l--;
//                    break;
//                } else dprev = dnext;
//            }
//        }
//        if (move == 1) Exchange(i, j, k, l);
//        else Insert(i, j, k, l);
//        CalculateCenters();
//    }
//
//}
    public void Shake4(int ks, int move) {

        int i, j, k, l;

        ArrayList<pair<Integer, Integer>> vecPos = new ArrayList<>();

        for (int ik = 0; ik < ks; ik++) {
            if (move == 1) i = rndInt(0, data.nM - 1);
            else do {
                i = rndInt(0, data.nM - 1);
            } while (rho[i].size() < 2);
            j = rndInt(0, (rho[i].size() - 1));
            int xij = x[rho[i].get(j)];
            vecPos.clear();
            for (k = 0; k < data.nM; k++) {
                if (k != i) {
                    l = 0;
                    int dprev = x[rho[k].get(l)] - xij;
                    while (dprev < 0) {
                        l++;
                        if (l > rho[k].size() - 1) {
                            l--;
                            break;
                        } else {
                            int dnext = x[rho[k].get(l)] - xij;
                            if (dnext >= 0) {
                                if (abs(dprev) < dnext) l--;
                                break;
                            } else dprev = dnext;
                        }
                    }
                    vecPos.add(pair.of(k, l));
                }
            }
            var pos = vecPos.get(rndInt(0, data.nM - 2));
            k = pos.first;
            l = pos.second;
            if (move == 1) Exchange(i, j, k, l);
            else Insert(i, j, k, l);
            CalculateCenters();
        }
    }

    //----------------------------------------
// Shake 5: Vertical-contact move
//----------------------------------------
    public void Shake5(int ks, int move) {

        int i, j, k, l;

        List<Integer> vecf = new ArrayList<>();
        for (int iks = 0; iks < ks; iks++) {
            if (move == 1) i = rndInt(0, data.nM - 1);
            else do {
                i = rndInt(0, data.nM - 1);
            } while (rho[i].size() < 2);
            j = rndInt(0, (rho[i].size() - 1));
            int u = rho[i].get(j);
            int Lbu = x[u] - data.L[u] / 2;
            int Ubu = x[u] + data.L[u] / 2;
            do {
                k = rndInt(0, data.nM - 1);
            } while (k == i);
            vecf.clear();
            l = 0;
            int v = rho[k].get(l);
            while ((x[v] - data.L[v] / 2) < Ubu) {
                if ((x[v] + data.L[v] / 2) > Lbu) vecf.add(l);
                if (l < rho[k].size() - 1) {
                    l++;
                    v = rho[k].get(l);
                } else break;
            }
            if (vecf.isEmpty()) l = (rho[k].size() - 1);
            else l = vecf.get(rndInt(0, (vecf.size() - 1)));
            if (move == 1) Exchange(i, j, k, l);
            else Insert(i, j, k, l);
            CalculateCenters();
        }
    }
    // END shakes.cpp


    public List<Integer>[] getRho() {
        return rho;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CAPSolution that = (CAPSolution) o;
        return Arrays.equals(rho, that.rho);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(rho);
    }
}
