package es.urjc.etsii.grafo.patches;

import java.util.Random;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

public class FailRandom extends Random {
    private static final String FAIL_MESSAGE = "Invalid Random() usage, use RandomManager!";
//    @Override
//    public synchronized void setSeed(long seed) {
//        throw new UnsupportedOperationException(FAIL_MESSAGE);
//    }

    @Override
    protected int next(int bits) {
        throw new UnsupportedOperationException(FAIL_MESSAGE);
    }

    @Override
    public void nextBytes(byte[] bytes) {
        throw new UnsupportedOperationException(FAIL_MESSAGE);
    }

    @Override
    public int nextInt() {
        throw new UnsupportedOperationException(FAIL_MESSAGE);
    }

    @Override
    public int nextInt(int bound) {
        throw new UnsupportedOperationException(FAIL_MESSAGE);
    }

    @Override
    public long nextLong() {
        throw new UnsupportedOperationException(FAIL_MESSAGE);
    }

    @Override
    public boolean nextBoolean() {
        throw new UnsupportedOperationException(FAIL_MESSAGE);
    }

    @Override
    public float nextFloat() {
        throw new UnsupportedOperationException(FAIL_MESSAGE);
    }

    @Override
    public double nextDouble() {
        throw new UnsupportedOperationException(FAIL_MESSAGE);
    }

    @Override
    public synchronized double nextGaussian() {
        throw new UnsupportedOperationException(FAIL_MESSAGE);
    }

    @Override
    public IntStream ints(long streamSize) {
        throw new UnsupportedOperationException(FAIL_MESSAGE);
    }

    @Override
    public IntStream ints() {
        throw new UnsupportedOperationException(FAIL_MESSAGE);
    }

    @Override
    public IntStream ints(long streamSize, int randomNumberOrigin, int randomNumberBound) {
        throw new UnsupportedOperationException(FAIL_MESSAGE);
    }

    @Override
    public IntStream ints(int randomNumberOrigin, int randomNumberBound) {
        throw new UnsupportedOperationException(FAIL_MESSAGE);
    }

    @Override
    public LongStream longs(long streamSize) {
        throw new UnsupportedOperationException(FAIL_MESSAGE);
    }

    @Override
    public LongStream longs() {
        throw new UnsupportedOperationException(FAIL_MESSAGE);
    }

    @Override
    public LongStream longs(long streamSize, long randomNumberOrigin, long randomNumberBound) {
        throw new UnsupportedOperationException(FAIL_MESSAGE);
    }

    @Override
    public LongStream longs(long randomNumberOrigin, long randomNumberBound) {
        throw new UnsupportedOperationException(FAIL_MESSAGE);
    }

    @Override
    public DoubleStream doubles(long streamSize) {
        throw new UnsupportedOperationException(FAIL_MESSAGE);
    }

    @Override
    public DoubleStream doubles() {
        throw new UnsupportedOperationException(FAIL_MESSAGE);
    }

    @Override
    public DoubleStream doubles(long streamSize, double randomNumberOrigin, double randomNumberBound) {
        throw new UnsupportedOperationException(FAIL_MESSAGE);
    }

    @Override
    public DoubleStream doubles(double randomNumberOrigin, double randomNumberBound) {
        throw new UnsupportedOperationException(FAIL_MESSAGE);
    }
}
