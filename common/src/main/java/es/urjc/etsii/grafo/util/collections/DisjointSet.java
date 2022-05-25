package es.urjc.etsii.grafo.util.collections;

import java.util.Arrays;

/**
 * UnionFind/Disjoint sets implementation
 */
public class DisjointSet {

    private final int offset;
    private final int[] parent;
    private final int[] size;


    /**
     * Create a Disjoint set where the n elements are initially all disjoint
     *
     * @param n number of elements to hold
     * @param offset Offset elements by an index ex 1 wil be 0-9 → 1-10
     */
    public DisjointSet(int n, int offset) {
        if(n<=0){
            throw new IllegalArgumentException("N must be greater than 0, provided: " + n);
        }
        this.offset = offset;
        this.parent = new int[n];
        for (int i = 0; i < parent.length; i++) {
            parent[i] = i;
        }
        this.size = new int[n];
        Arrays.fill(size,1);

    }

    /**
     * Clone a DisjointSet
     *
     * @param original original disjointSet
     */
    public DisjointSet(DisjointSet original){
        this.offset = original.offset;
        this.parent = Arrays.copyOf(original.parent, original.parent.length);
        this.size = Arrays.copyOf(original.size, original.size.length);
    }

    /**
     * Create a Disjoint set where the n elements are initially all disjoint
     *
     * @param n number of elements to hold
     */
    public DisjointSet(int n) {
        this(n, 0);
    }

    /**
     * Returns the size of set/cluster the given points is part of
     *
     * @param n node to check
     * @return size of the cluster n is part of
     */
    public int size(int n){
        n -= offset;
        return _size(n);
    }

    private int _size(int n){
        if(n<0 || n>= this.size.length){
            throw new IllegalArgumentException("Out of bounds n: " + n);
        }
        return size[_find(n)];
    }

    /**
     * Find the set id of a given element
     *
     * @param n element
     * @return set id
     */
    public int find(int n){
        n -= offset;
        if(n<0 || n>= this.size.length){
            throw new IllegalArgumentException("Out of bounds n: " + n);
        }
        return offset + _find(n);
    }

    /**
     * Check if two nodes are in the same set
     *
     * @param a first node to check
     * @param b second node to check
     * @return true if the two nodes are in the same set/cluster, false otherwise
     */
    public boolean areJoined(int a, int b){
        return find(a) == find(b);
    }

    private int _find(int n){
        if(parent[n] != n){
            // Path compression
            parent[n] = _find(parent[n]);
        }
        return parent[n];
    }

    /**
     * Join the sets a and b are members of
     *
     * @param a any element from set A
     * @param b any element from set B
     * @return true if the sets have been joined, false if they were already part of the same set
     */
    public boolean union(int a, int b){
        a -= offset;
        b -= offset;
        if(a<0 || a>= this.size.length){
            throw new IllegalArgumentException("Out of bounds a: " + a);
        }
        if(b<0 || b>= this.size.length){
            throw new IllegalArgumentException("Out of bounds b: " + b);
        }
        int rootA = _find(a);
        int rootB = _find(b);

        if(rootA == rootB) return false;

        int sizeA = _size(rootA);
        int sizeB = _size(rootB);

        if(sizeA < sizeB){ //SWAP
            int t = rootA;
            rootA = rootB;
            rootB = t;
        }

        parent[rootB] = rootA;
        size[rootA] += size[rootB];
        return true;
    }
}
