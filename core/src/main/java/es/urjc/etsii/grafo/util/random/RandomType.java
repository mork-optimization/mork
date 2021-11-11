package es.urjc.etsii.grafo.util.random;

/**
 * Minimum set of available random generators.
 * See official Javadoc for a detailed comparison between Random providers: https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/random/package-summary.html#algorithms
 */
public enum RandomType {
    L128X1024MIX("L128X1024MixRandom"),
    L128X128MIX("L128X128MixRandom"),
    L128X256MIX("L128X256MixRandom"),
    L32X64MIX("L32X64MixRandom"),
    L64X1024MIX("L64X1024MixRandom"),
    L64X128MIX("L64X128MixRandom"),
    L64X128STAR("L64X128StarStarRandom"),
    L64X256MIX("L64X256MixRandom"),
    DEFAULT("Xoroshiro128PlusPlus"),
    LEGACY("Random"),
    XOROSHIRO128PP("Xoroshiro128PlusPlus"),
    XOSHIRO256PP("Xoshiro256PlusPlus");

    private final String javaName;
    RandomType(String javaName){
        this.javaName = javaName;
    }

    public String getJavaName() {
        return javaName;
    }
}
