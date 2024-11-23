package es.urjc.etsii.grafo.mo.pareto;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;

import java.util.ArrayList;
import java.util.stream.Stream;

public class NDTree<S extends Solution<S,I>, I extends Instance> extends ParetoSet<S,I> {
    private NDTreeNode root;
    private final int maxListSizePerNode;
    private final int numberOfChildrenPerNode;

    /*
     * Constructor -- called by factory method
     */
    public NDTree(int numberOfObjectives) {
        this(numberOfObjectives, 20, numberOfObjectives + 1);
    }

    /*
     * Constructor -- called by factory method
     */
    public NDTree(int numberOfObjectives, int maxListSizePerNode, int numberOfChildrenPerNode) {
        super(numberOfObjectives);
        this.maxListSizePerNode = maxListSizePerNode;
        this.numberOfChildrenPerNode = numberOfChildrenPerNode;
        root = new NDTreeNode(this::ejectedSolution, maxListSizePerNode, numberOfChildrenPerNode);
    }

    @Override
    public synchronized boolean add(double[] s) {
        if (root.isEmpty()) {
            root.add(s);
            return true;
        } else {
            if (root.updateNode(s, null)) { // returns true if solution not covered by any member in tree
                if (root.isEmpty()) {
                    root.add(s); // Special case where s has dominated and cleared tree entirely
                } else {
                    root.insert(s);
                }
                return true;
            }
        }
        return false;
    }


    @Override
    public synchronized Stream<double[]> stream() {
        var s = new ArrayList<double[]>(this.size());
        if (root != null) {
            root.recursivelyExtract(s);
        }
        return s.stream();
    }


    public synchronized double[] getEstimatedIdeal() {
        if (root == null) {
            return null;
        }
        return root.getEstimatedIdeal();
    }

    public synchronized double[] getEstimatedNadir() {
        if (root == null) {
            return null;
        }
        return root.getEstimatedNadir();
    }

    public synchronized double[] getMidpoint() {
        if (root == null) {
            return null;
        }
        return root.getMidpoint();
    }

    @Override
    public synchronized int size() {
        return root.coverage();
    }

    @Override
    public synchronized void clear() {
        super.clear();
        root = new NDTreeNode(this::ejectedSolution, this.maxListSizePerNode, numberOfChildrenPerNode);
    }

//    @Override
//    public double[] getRandomMember() throws UnsupportedOperationException {
//        if (size() == 0) {
//            return null; //empty tree, so return null
//        }
//        return root.getRandom(rng);
//    }
//
//    /**
//     * Returns a list of solutions residing in one of the NDTree's leaves
//     */
//    public List<Solution> getRandomLeaf() {
//        if (size() == 0) {
//            return null; //empty tree, so return null
//        }
//        return root.getRandomLeaf(rng);
//    }


    /**
     * Returns the solution extremising the index objective
     */
    public synchronized double[] getExtremeMember(int index) {
        if (size() == 0) {
            return null; //empty tree, so return null
        }
        var solutions = new ArrayList<double[]>();
        root.getExtremeMember(solutions, index);
        var extreme = solutions.getFirst();
        for (int i = 1; i < solutions.size(); i++) {
            if (solutions.get(i)[index] < extreme[index]) {
                extreme = solutions.get(i);
            }
        }
        return extreme;
    }
}
