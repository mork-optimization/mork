package es.urjc.etsii.grafo.util.random;

import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

/**
 * Minimum set of available random generators.
 * See official Javadoc for a detailed comparison between Random providers: https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/random/package-summary.html#algorithms
 */
public enum RandomType {
//    /**
//     * L128X1024MixRandom
//     */
//    L128X1024MIX("L128X1024MixRandom"),

//    /**
//     * L128X128MixRandom
//     */
//    L128X128MIX("L128X128MixRandom"),

//    /**
//     * L128X256MixRandom
//     */
//    L128X256MIX("L128X256MixRandom"),

//    /**
//     * L32X64MixRandom
//     */
//    L32X64MIX("L32X64MixRandom"),

//    /**
//     * L64X1024MixRandom
//     */
//    L64X1024MIX("L64X1024MixRandom"),

//    /**
//     * L64X128MixRandom
//     */
//    L64X128MIX("L64X128MixRandom"),

//    /**
//     * L64X128StarStarRandom
//     */
//    L64X128STAR("L64X128StarStarRandom"),

//    /**
//     * L64X256MixRandom
//     */
//    L64X256MIX("L64X256MixRandom"),

    /**
     * Xoroshiro128PlusPlus
     */
    DEFAULT("Xoroshiro128PlusPlus"),

//    /**
//     * Legacy Java Random
//     */
//    LEGACY("Random"),

    /**
     * Xoroshiro128PlusPlus
     */
    XOROSHIRO128PP("Xoroshiro128PlusPlus"),

    /**
     * Xoshiro256PlusPlus
     */
    XOSHIRO256PP("Xoshiro256PlusPlus"),

    /** Park-Miller, including the native bounded-integer conversion. */
    PARK_MILLER("ParkMiller");

    private final String javaName;

    /**
     * RandomType with associated Java name value
     * @param javaName name for Java APIs
     */
    RandomType(String javaName){
        this.javaName = javaName;
    }

    /**
     * Get Java name
     *
     * @return Java reference name as string
     */
    public String getJavaName() {
        return javaName;
    }

    /**
     * Create a seeded generator, including providers supplied by Mork.
     * @param seed initial generator seed
     * @return jumpable generator compatible with Mork's thread-local contexts
     */
    public RandomGenerator.JumpableGenerator create(long seed) {
        if (this == PARK_MILLER) return new ParkMillerRandom(seed);
        var random = RandomGeneratorFactory.of(javaName).create(seed);
        if (random instanceof RandomGenerator.JumpableGenerator jumpable) return jumpable;
        throw new IllegalArgumentException("RandomGenerator is not jumpable: " + javaName);
    }
}
