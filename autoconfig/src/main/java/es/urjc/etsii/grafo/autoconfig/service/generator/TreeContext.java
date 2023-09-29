package es.urjc.etsii.grafo.autoconfig.service.generator;

import java.util.ArrayDeque;
import java.util.HashMap;

/**
 * Tracks the exploration of the autoconfig grammar.
 * @param maxDepth maximum allowed depth
 * @param maxRepeat how many times can each derivation rule be applied.
 * @param branch current branch, represented as a list of component implementation names
 * @param derivationCounter counts how many times each derivation rule has been applied in the current branch
 */
public record TreeContext(int maxDepth, int maxRepeat, ArrayDeque<String> branch, HashMap<Derivation, Integer> derivationCounter) {
    public TreeContext(int maxDepth, int maxRepeatDerivation) {
        this(maxDepth, maxRepeatDerivation, new ArrayDeque<>(), new HashMap<>());
    }

    public void push(String component){
        branch.push(component);
    }

    public void pop(){
        branch.pop();
    }

    public void pushDerivation(Derivation d){
        derivationCounter.put(d, derivationCounter.getOrDefault(d, 0) + 1);
    }

    public int getDerivationCount(Derivation d){
        return derivationCounter.getOrDefault(d, 0);
    }

    public void popDerivation(Derivation d){
        var count = this.derivationCounter.getOrDefault(d, 0);
        assert count > 0 : "[BUG FOUND] Cannot pop derivation " + d + " because it has not been applied yet";
        this.derivationCounter.put(d, count - 1);
    }

    public boolean inLimits(Derivation d){
        return this.branch.size() <= maxDepth && getDerivationCount(d) <= maxRepeat;
    }
}
