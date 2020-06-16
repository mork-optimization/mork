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
     * @param n number of elements to hold
     * @param offset Offset elements by an index ex 1 wil be 0-9 --> 1-10
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
     * Create a Disjoint set where the n elements are initially all disjoint
     * @param n number of elements to hold
     */
    public DisjointSet(int n) {
        this(n, 0);
    }

    public int size(int n){
        n -= offset;
        if(n<0 || n>= this.size.length){
            throw new IllegalArgumentException("Out of bounds n: " + n);
        }
        return size[_find(n)];
    }

    public int find(int n){
        n -= offset;
        if(n<0 || n>= this.size.length){
            throw new IllegalArgumentException("Out of bounds n: " + n);
        }
        return offset + _find(n);
    }

    private int _find(int n){
        if(parent[n] != n){
            parent[n] = _find(parent[n]);
        }
        return parent[n];
    }

    /**
     * Join the sets a and b are members of
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

        int sizeA = size(rootA);
        int sizeB = size(rootB);

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
