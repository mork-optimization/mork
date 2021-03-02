//package es.urjc.etsii.grafo.solver.improve;
//
//import es.urjc.etsii.grafo.io.Instance;
//import es.urjc.etsii.grafo.solution.Solution;
//import es.urjc.etsii.grafo.util.DoubleComparator;
//
//import java.util.List;
//
//public class VND<S extends Solution<I>,I extends Instance> extends Improver<S,I>{
//
//    private final List<Improver<S,I>> improvers;
//
//    public VND(List<Improver<S, I>> improvers) {
//        this.improvers = improvers;
//    }
//
//    public boolean iteration(S current) {
//        // TODO hacer esto bien mover el metodo a la clase LocalSearch en vz de improver
//        throw new IllegalArgumentException();
//    }
//
//    @Override
//    public S improve(S s) {
//        int currentLS = 0;
//        while(currentLS < improvers.size()){
//            double prev = s.getScore();
//            var ls = improvers.get(currentLS);
//            s = ls.improve(s);
//            // TODO depende si es maximizar o minimizar ESTO CAMBIA
//            if (DoubleComparator.isLessOrEquals(prev, s.getScore())) {
//                currentLS++;
//            } else {
//                currentLS = 0;
//            }
//        }
//        return s;
//    }

//      // TODO toString()
//}
