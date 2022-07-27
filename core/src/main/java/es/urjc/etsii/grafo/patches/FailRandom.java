package es.urjc.etsii.grafo.patches;

import es.urjc.etsii.grafo.exception.InvalidRandomException;

import java.util.Random;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

/**
 * Fake random that always fails, used to override the internal Java random in some APIs
 * such as Math.random() and Collections.shuffle()
 */
public class FailRandom extends Random {

    /**
     * {@inheritDoc}
     *
     * Fail always
     */
    @Override
    protected int next(int bits) {
        throw new InvalidRandomException();
    }

    /**
     * {@inheritDoc}
     *
     * Fail always
     */
    @Override
    public void nextBytes(byte[] bytes) {
        throw new InvalidRandomException();
    }

    /**
     * {@inheritDoc}
     *
     * Fail always
     */
    @Override
    public int nextInt() {
        throw new InvalidRandomException();
    }

    /**
     * {@inheritDoc}
     *
     * Fail always
     */
    @Override
    public int nextInt(int bound) {
        throw new InvalidRandomException();
    }

    /**
     * {@inheritDoc}
     *
     * Fail always
     */
    @Override
    public long nextLong() {
        throw new InvalidRandomException();
    }

    /**
     * {@inheritDoc}
     *
     * Fail always
     */
    @Override
    public boolean nextBoolean() {
        throw new InvalidRandomException();
    }

    /**
     * {@inheritDoc}
     *
     * Fail always
     */
    @Override
    public float nextFloat() {
        throw new InvalidRandomException();
    }

    /**
     * {@inheritDoc}
     *
     * Fail always
     */
    @Override
    public double nextDouble() {
        throw new InvalidRandomException();
    }

    /**
     * {@inheritDoc}
     *
     * Fail always
     */
    @Override
    public synchronized double nextGaussian() {
        throw new InvalidRandomException();
    }

    /**
     * {@inheritDoc}
     *
     * Fail always
     */
    @Override
    public IntStream ints(long streamSize) {
        throw new InvalidRandomException();
    }

    /**
     * {@inheritDoc}
     *
     * Fail always
     */
    @Override
    public IntStream ints() {
        throw new InvalidRandomException();
    }

    /**
     * {@inheritDoc}
     *
     * Fail always
     */
    @Override
    public IntStream ints(long streamSize, int randomNumberOrigin, int randomNumberBound) {
        throw new InvalidRandomException();
    }

    /**
     * {@inheritDoc}
     *
     * Fail always
     */
    @Override
    public IntStream ints(int randomNumberOrigin, int randomNumberBound) {
        throw new InvalidRandomException();
    }

    /**
     * {@inheritDoc}
     *
     * Fail always
     */
    @Override
    public LongStream longs(long streamSize) {
        throw new InvalidRandomException();
    }

    /**
     * {@inheritDoc}
     *
     * Fail always
     */
    @Override
    public LongStream longs() {
        throw new InvalidRandomException();
    }

    /**
     * {@inheritDoc}
     *
     * Fail always
     */
    @Override
    public LongStream longs(long streamSize, long randomNumberOrigin, long randomNumberBound) {
        throw new InvalidRandomException();
    }

    /**
     * {@inheritDoc}
     *
     * Fail always
     */
    @Override
    public LongStream longs(long randomNumberOrigin, long randomNumberBound) {
        throw new InvalidRandomException();
    }

    /**
     * {@inheritDoc}
     *
     * Fail always
     */
    @Override
    public DoubleStream doubles(long streamSize) {
        throw new InvalidRandomException();
    }

    /**
     * {@inheritDoc}
     *
     * Fail always
     */
    @Override
    public DoubleStream doubles() {
        throw new InvalidRandomException();
    }

    /**
     * {@inheritDoc}
     *
     * Fail always
     */
    @Override
    public DoubleStream doubles(long streamSize, double randomNumberOrigin, double randomNumberBound) {
        throw new InvalidRandomException();
    }

    /**
     * {@inheritDoc}
     *
     * Fail always
     */
    @Override
    public DoubleStream doubles(double randomNumberOrigin, double randomNumberBound) {
        throw new InvalidRandomException();
    }
}
