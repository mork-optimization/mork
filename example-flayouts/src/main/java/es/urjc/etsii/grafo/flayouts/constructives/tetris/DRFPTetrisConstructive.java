package es.urjc.etsii.grafo.flayouts.constructives.tetris;

import es.urjc.etsii.grafo.create.Reconstructive;
import es.urjc.etsii.grafo.flayouts.model.FLPInstance;
import es.urjc.etsii.grafo.flayouts.model.FLPSolution;
import es.urjc.etsii.grafo.util.CollectionUtil;
import es.urjc.etsii.grafo.util.DoubleComparator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

/**
 * "Tetris" like constructive. Each facility starts by being a single piece, and they are joined until only a piece remains.
 */
public class DRFPTetrisConstructive extends Reconstructive<FLPSolution, FLPInstance> {

    private static final Logger log = Logger.getLogger(DRFPTetrisConstructive.class.getName());
    private final double[] widths;
    private final double alpha;

    public DRFPTetrisConstructive(double[] widths, double alpha) {
        this.widths = widths;
        this.alpha = alpha;
    }

    public DRFPTetrisConstructive() {
        this.widths = new double[0];
        this.alpha = 0;
    }

    @Override
    public FLPSolution reconstruct(FLPSolution solution) {
        var notJoined = solution.getPendingPieces();
        var instance = solution.getInstance();

        if(notJoined.isEmpty()){
            log.warning("Skipping reconstruct, empty joined pieces");
            return solution;
        }

        while(notJoined.size()>1){
            var moves = generateMoves(instance, notJoined, 0.5); // todo: 0.5 is hardcoded, avoid magic numbers
            // Cogemos el primero porque tamos minimizando

            var m = choose(moves);
            notJoined.remove(m.a());
            notJoined.remove(m.b());
            notJoined.add(m);
        }

        // Construct solution using the matrix data
        assert notJoined.size() == 1;
        var e = (Piece) notJoined.toArray()[0];
        notJoined.clear();
        for (int i = 0; i < e.data().length; i++) {
            for (int j = 0; j < e.data()[i].length; j++) {
                solution.insertLast(i, instance.byId(e.data()[i][j]));
            }
        }
        solution.rebuildCaches();
        solution.updateLastModifiedTime();

        // both costs should match
        assert DoubleComparator.equals(solution.getScore(), e.cost());

        return solution;
    }

    @Override
    public FLPSolution construct(FLPSolution solution) {
        solution.addFakeFacilities(this.widths);
        for(var d: widths){
            assert DoubleComparator.equals(d, 0.5D);
        }
        var facilities = new ArrayList<>(solution.getNotAssignedFacilities());
        int nrows = solution.getInstance().getNRows();

        if(nrows != 2){
            throw new IllegalArgumentException("Tetris constructive has only been tested for the DRFLP, not in MRFLP");
        }
        var notJoined = solution.getPendingPieces();
        for (var f: facilities) {
            var p = new Piece(new int[][]{
                    {f.id},
                    {}
            }, 0, null, null);
            notJoined.add(p);
        }

        return reconstruct(solution);
    }

    private Piece choose(List<Piece> moves) {
        moves.sort(Comparator.comparing(Piece::increment));
        var candidates = new ArrayList<Piece>();
        double min = moves.get(0).increment();
        double max = moves.get(moves.size() - 1).increment();
        double umbral = min + (max - min) * alpha;
        for(var m: moves){
            if (DoubleComparator.isLessOrEquals(m.increment(), umbral)) {
                candidates.add(m);
            } else {
                break;
            }
        }
        return CollectionUtil.pickRandom(candidates);
    }

    private List<Piece> generateMoves(FLPInstance instance, List<Piece> notJoined, double fakeWidth) {
        var candidates = new ArrayList<>(notJoined);
        var next = new ArrayList<Piece>();
        for (int i = 0; i < candidates.size() - 1; i++) {
            var a = candidates.get(i);
            for (int j = i+1; j < candidates.size(); j++) {
                var b = candidates.get(j);

                // AB
                var data = doMerge(a.data()[0], a.data()[1], b.data()[0], b.data()[1]);
                next.add(new Piece(data, FLPSolution.evaluate(instance, data, fakeWidth),a,b));

                // BA
                data = doMerge(b.data()[0], b.data()[1], a.data()[0], a.data()[1]);
                next.add(new Piece(data, FLPSolution.evaluate(instance, data, fakeWidth),a,b));

                // ∀B
                data = doMerge(a.data()[1], a.data()[0], b.data()[0], b.data()[1]);
                next.add(new Piece(data, FLPSolution.evaluate(instance, data, fakeWidth),a,b));

                // B∀  // todo review possible optimization: if starting (ie very small pieces) this may repeat moves
                data = doMerge(b.data()[0], b.data()[1], a.data()[1], a.data()[0]);
                next.add(new Piece(data, FLPSolution.evaluate(instance, data, fakeWidth),a,b));
            }
        }
        return next;
    }

    private int[][] doMerge(int[] a_r1, int[] a_r2, int[] b_r1, int[] b_r2){
        int[][] result = new int[][]{
                new int[a_r1.length + b_r1.length],
                new int[a_r2.length + b_r2.length]
        };
        System.arraycopy(a_r1, 0, result[0], 0, a_r1.length);
        System.arraycopy(b_r1, 0, result[0], a_r1.length, b_r1.length);

        System.arraycopy(a_r2, 0, result[1], 0, a_r2.length);
        System.arraycopy(b_r2, 0, result[1], a_r2.length, b_r2.length);
        return result;
    }


    /*
    static class UnionFind {
        private final Piece[] data;
        private int nClusters;

        public UnionFind(int nodes){
            nClusters = nodes;
            this.data = new Piece[nodes];
            for (int i = 0; i < nodes; i++) {
                data[i] = new Piece(new int[][]{new ArrayList<Integer>(),new ArrayList<Integer>()}, true, i, 1);
            }
        }

        public int find(int node){
            if(data[node].parent != node){
                data[node].parent = find(data[node].parent);
            }
            return data[node].parent;
        }

        public boolean union(int x, int y){
            x = find(x);
            y = find(y);
            if(x == y){
                return false;
            }
            if(data[x].size < data[y].size){
                int t = x; // Swap
                x = y;
                y = t;
            }
            data[y].parent = x;
            data[y].active = false;
            data[x].size += data[y].size;
            nClusters--;
            return true;
        }

        public int size(int node){
            return data[find(node)].size;
        }

        public boolean areJoined(int x, int y){
            return find(x) == find(y);
        }

        public int getNClusters(){
            return this.nClusters;
        }

        // Warning, this is the only method with O(N) complexity
        public List<Piece> getPieces(){
            var list = new ArrayList<Piece>(this.nClusters);
            for(var e: this.data){
                if(e.active){
                    list.add(e);
                }
            }
            return list;
        }
    }
     **/

    @Override
    public String toString() {
        return "Tetris{" +
                "a=" + alpha +
                '}';
    }
}
