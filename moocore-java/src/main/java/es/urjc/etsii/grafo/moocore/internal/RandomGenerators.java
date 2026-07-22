// SPDX-License-Identifier: MPL-2.0
package es.urjc.etsii.grafo.moocore.internal;

import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

/** Seeded random generators aligned with Mork's default provider. */
public final class RandomGenerators {

    private static final RandomGeneratorFactory<RandomGenerator> DEFAULT_FACTORY =
            RandomGeneratorFactory.of("Xoroshiro128PlusPlus");

    private RandomGenerators() {
    }

    public static RandomGenerator create(long seed) {
        return DEFAULT_FACTORY.create(seed);
    }
}
