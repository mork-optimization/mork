package es.urjc.etsii.grafo.util.random;

import java.util.random.RandomGenerator;

/** Park-Miller generator and bounded-integer conversion used by the native TSPTW solver. */
public final class ParkMillerRandom implements RandomGenerator.JumpableGenerator {
    private static final long MODULUS = 2_147_483_647L;
    private static final long MULTIPLIER = 16_807;
    private static final int JUMP_DISTANCE = 1 << 20;
    private long state;

    public ParkMillerRandom(long seed) {
        if (seed <= 0 || seed >= MODULUS) {
            throw new IllegalArgumentException("Park-Miller seed must be between 1 and 2147483646: " + seed);
        }
        state = seed;
    }

    /** Current native RNG state, for deterministic replay and checkpointing. */
    public long getState() {
        return state;
    }

    @Override
    public double nextDouble() {
        long quotient = state / 127_773;
        state = MULTIPLIER * (state - quotient * 127_773) - 2_836 * quotient;
        if (state < 0) state += MODULUS;
        return state * (1.0 / MODULUS);
    }

    @Override
    public int nextInt(int bound) {
        if (bound <= 0) throw new IllegalArgumentException("Bound must be positive: " + bound);
        return (int) (nextDouble() * bound);
    }

    @Override
    public long nextLong() {
        long high = (long) (nextDouble() * (1L << 31));
        long middle = (long) (nextDouble() * (1L << 31));
        long low = (long) (nextDouble() * 4);
        return (high << 33) | (middle << 2) | low;
    }

    @Override
    public ParkMillerRandom copy() {
        return new ParkMillerRandom(state);
    }

    @Override
    public void jump() {
        long factor = 1;
        long power = MULTIPLIER;
        for (int distance = JUMP_DISTANCE; distance > 0; distance >>>= 1) {
            if ((distance & 1) != 0) factor = factor * power % MODULUS;
            power = power * power % MODULUS;
        }
        state = state * factor % MODULUS;
    }

    @Override
    public double jumpDistance() {
        return JUMP_DISTANCE;
    }
}
