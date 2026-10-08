// SPDX-License-Identifier: MPL-2.0
package es.urjc.etsii.grafo.moocore.internal;

import es.urjc.etsii.grafo.moocore.HypervolumeApproximation;
import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorSpecies;

import java.util.random.RandomGenerator;

public final class HypervolumeApproximationAlgorithms {

    private static final double MINIMUM_WEIGHT = 1e-20;
    private static final VectorSpecies<Double> DOUBLE_SPECIES = DoubleVector.SPECIES_PREFERRED;
    private static final double[] SPHERE_CONSTANTS = sphereConstants();
    private static final double[] SIN_POWER_HALF_PI = sinPowerHalfPi();
    private static final double[] RECIPROCAL_PHI = reciprocalGoldenRatios();

    private HypervolumeApproximationAlgorithms() {
    }

    public static double approximate(double[][] input, double[] reference, boolean[] maximise,
                                     long samples, long seed, HypervolumeApproximation method) {
        if (input == null) {
            throw new IllegalArgumentException("points cannot be null");
        }
        if (samples <= 0 || samples > 2_147_483_648L) {
            throw new IllegalArgumentException("samples must be in [1, 2147483648]");
        }
        if (input.length == 0) {
            int dimensions = emptyDimensions(reference, maximise);
            MatrixUtils.vector(reference, dimensions, "reference");
            MatrixUtils.directions(maximise, dimensions);
            return 0.0;
        }
        int dimensions = MatrixUtils.validate(input, 1, 31);
        if (dimensions == 1) {
            return HypervolumeAlgorithms.hypervolume(input, reference, maximise);
        }
        boolean[] directions = MatrixUtils.directions(maximise, dimensions);
        double[] ref = MatrixUtils.vector(reference, dimensions, "reference");
        int stride = input.length;
        double[] transformed = new double[dimensions * stride];
        int validPoints = 0;
        for (double[] point : input) {
            boolean valid = true;
            for (int objective = 0; objective < dimensions; objective++) {
                double distance = directions[objective]
                        ? point[objective] - ref[objective]
                        : ref[objective] - point[objective];
                transformed[objective * stride + validPoints] = distance;
                valid &= distance > 0.0;
            }
            if (valid) {
                validPoints++;
            }
        }
        if (validPoints == 0) {
            return 0.0;
        }
        RadialPoints points = new RadialPoints(transformed, validPoints, dimensions, stride);
        double expected = switch (method) {
            case DZ2019_MC -> monteCarlo(points, samples, seed);
            case DZ2019_HW -> huaWang(points, samples);
            case RPHI_FWE_PLUS -> rPhi(points, samples);
        };
        return sphereConstant(dimensions) * expected / samples;
    }

    private static int emptyDimensions(double[] reference, boolean[] maximise) {
        if (reference == null || reference.length == 0) {
            throw new IllegalArgumentException("reference cannot be empty");
        }
        int dimensions = reference.length;
        if (dimensions == 1 && maximise != null && maximise.length > 1) {
            dimensions = maximise.length;
        }
        if (dimensions > 31) {
            throw new UnsupportedOperationException("at most 31 objectives are supported");
        }
        return dimensions;
    }

    private static double monteCarlo(RadialPoints points, long samples, long seed) {
        RandomGenerator random = RandomGenerators.create(seed);
        int dimensions = points.dimensions;
        double[] inverseDirection = new double[dimensions];
        double result = 0.0;
        for (long sample = 0; sample < samples; sample++) {
            double norm = 0.0;
            for (int objective = 0; objective < dimensions; objective++) {
                double value = Math.max(Math.abs(random.nextGaussian()), MINIMUM_WEIGHT);
                inverseDirection[objective] = value;
                norm += value * value;
            }
            norm = Math.sqrt(norm);
            for (int objective = 0; objective < dimensions; objective++) {
                inverseDirection[objective] = norm / inverseDirection[objective];
            }
            result += radialPower(points, inverseDirection);
        }
        return result;
    }

    private static double huaWang(RadialPoints points, long samples) {
        int dimensions = points.dimensions;
        int angleCount = dimensions - 1;
        int prime = primeForDimension(angleCount);
        long[] coefficients = new long[angleCount];
        coefficients[0] = 1;
        for (int i = 1; i < angleCount; i++) {
            double value = Math.abs(2.0 * Math.cos(2.0 * Math.PI * i / prime));
            coefficients[i] = Math.round(samples * fractional(value));
        }
        double[] angles = new double[angleCount];
        double[] sine = new double[angleCount];
        double[] cosine = new double[angleCount];
        double[] direction = new double[dimensions];
        double[] inverseDirection = new double[dimensions];
        double result = 0.0;
        for (long sample = 0; sample < samples; sample++) {
            double factor = sample + 1 < samples ? (sample + 1.0) / samples : 0.0;
            for (int i = 0; i < angleCount; i++) {
                double uniform = fractional(factor * coefficients[i]);
                int power = dimensions - i - 2;
                angles[i] = inverseSinPowerIntegral(
                        uniform, power, SIN_POWER_HALF_PI[power]);
            }
            huaWangDirection(inverseDirection, angles, sine, cosine, direction);
            result += radialPower(points, inverseDirection);
        }
        return result;
    }

    private static double rPhi(RadialPoints points, long samples) {
        int dimensions = points.dimensions;
        int sequenceDimensions = dimensions - 1;
        double reciprocalPhi = RECIPROCAL_PHI[sequenceDimensions];
        double[] alpha = new double[sequenceDimensions];
        double[] uniform = new double[sequenceDimensions];
        double[] direction = new double[dimensions];
        double[] inverseDirection = new double[dimensions];
        alpha[0] = reciprocalPhi;
        uniform[0] = 0.5;
        for (int i = 1; i < sequenceDimensions; i++) {
            alpha[i] = alpha[i - 1] * reciprocalPhi;
            uniform[i] = 0.5;
        }
        double result = 0.0;
        for (long sample = 0; sample < samples; sample++) {
            for (int i = 0; i < sequenceDimensions; i++) {
                uniform[i] = fractional(uniform[i] + alpha[i]);
            }
            fangWangDirection(direction, uniform);
            for (int i = 0; i < dimensions; i++) {
                inverseDirection[i] = 1.0 / Math.max(direction[i], MINIMUM_WEIGHT);
            }
            result += radialPower(points, inverseDirection);
        }
        return result;
    }

    private static double radialPower(RadialPoints points, double[] inverseDirection) {
        double maximum = 0.0;
        int width = DOUBLE_SPECIES.length();
        int vectorBound = DOUBLE_SPECIES.loopBound(points.count);
        int point = 0;
        for (; point < vectorBound; point += width) {
            DoubleVector minimum = DoubleVector.broadcast(
                    DOUBLE_SPECIES, Double.POSITIVE_INFINITY);
            for (int objective = 0; objective < points.dimensions; objective++) {
                DoubleVector coordinate = DoubleVector.fromArray(
                        DOUBLE_SPECIES, points.values, objective * points.stride + point);
                minimum = minimum.min(coordinate.mul(inverseDirection[objective]));
            }
            maximum = Math.max(maximum, minimum.reduceLanes(VectorOperators.MAX));
        }
        for (; point < points.count; point++) {
            double minimum = Double.POSITIVE_INFINITY;
            for (int objective = 0; objective < points.dimensions; objective++) {
                minimum = Math.min(minimum,
                        points.values[objective * points.stride + point]
                                * inverseDirection[objective]);
            }
            maximum = Math.max(maximum, minimum);
        }
        return integerPower(maximum, points.dimensions);
    }

    private static void huaWangDirection(double[] inverseDirection, double[] angles,
                                         double[] sine, double[] cosine, double[] direction) {
        int dimensions = inverseDirection.length;
        for (int i = 0; i < angles.length; i++) {
            sine[i] = Math.sin(angles[i]);
            cosine[i] = Math.cos(angles[i]);
        }
        for (int i = 0; i < dimensions - 1; i++) {
            direction[i] = sine[0];
        }
        for (int j = dimensions - 2; j > 0; j--) {
            direction[j - 1] = sine[dimensions - j - 1] * direction[j];
        }
        for (int j = 1; j < dimensions - 1; j++) {
            direction[j] *= cosine[dimensions - j - 1];
        }
        direction[dimensions - 1] = cosine[0];
        for (int i = 0; i < dimensions; i++) {
            inverseDirection[i] = 1.0 / Math.max(Math.abs(direction[i]), MINIMUM_WEIGHT);
        }
    }

    private static void fangWangDirection(double[] direction, double[] uniform) {
        int dimensions = direction.length;
        double product = 1.0;
        int pairs = dimensions / 2 - 1;
        for (int i = 0; i < pairs; i++) {
            int l = dimensions - 2 * i - 2;
            double value = Math.pow(uniform[l - 1], 1.0 / l);
            double radius = Math.sqrt(Math.max(0.0, 1.0 - value * value)) * product;
            product *= value;
            double angle = Math.PI * 0.5 * uniform[l];
            direction[2 * i] = radius * Math.sin(angle);
            direction[2 * i + 1] = radius * Math.cos(angle);
        }
        double angle;
        if (dimensions % 2 == 0) {
            angle = Math.PI * 0.5 * uniform[0];
        } else {
            direction[dimensions - 3] = product * uniform[0];
            product *= Math.sqrt(Math.max(0.0, 1.0 - uniform[0] * uniform[0]));
            angle = Math.PI * 0.5 * uniform[1];
        }
        direction[dimensions - 2] = product * Math.sin(angle);
        direction[dimensions - 1] = product * Math.cos(angle);
    }

    private static double inverseSinPowerIntegral(double fraction, int power, double total) {
        if (fraction <= 0.0) {
            return 0.0;
        }
        if (fraction >= 1.0) {
            return Math.PI * 0.5;
        }
        double target = fraction * total;
        double angle = Math.PI * 0.5;
        double residual = total - target;
        while (Math.abs(residual) > 1e-15) {
            double derivative = integerPower(Math.sin(angle), power);
            angle -= residual / derivative;
            residual = sinPowerIntegral(power, angle) - target;
        }
        return angle;
    }

    private static double sinPowerIntegral(int power, double upper) {
        if (power == 0) {
            return upper;
        }
        if (power == 1) {
            return 1.0 - Math.cos(upper);
        }
        double sine = Math.sin(upper);
        double cosine = Math.cos(upper);
        int currentPower = (power & 1) == 0 ? 0 : 1;
        double result = currentPower == 0 ? upper : 1.0 - cosine;
        double sinePower = currentPower == 0 ? sine : sine * sine;
        for (currentPower += 2; currentPower <= power; currentPower += 2) {
            result = -cosine * sinePower / currentPower
                    + (currentPower - 1.0) / currentPower * result;
            sinePower *= sine * sine;
        }
        return result;
    }

    private static int primeForDimension(int dimensions) {
        int candidate = Math.max(3, 2 * dimensions + 1);
        if (candidate % 2 == 0) {
            candidate++;
        }
        while (!isPrime(candidate)) {
            candidate += 2;
        }
        return candidate;
    }

    private static boolean isPrime(int value) {
        for (int divisor = 2; divisor * divisor <= value; divisor++) {
            if (value % divisor == 0) {
                return false;
            }
        }
        return true;
    }

    private static double reciprocalGeneralizedGoldenRatio(int dimensions) {
        double phi = 1.5;
        for (int iteration = 0; iteration < 80; iteration++) {
            double power = integerPower(phi, dimensions + 1);
            double value = power - phi - 1.0;
            double derivative = (dimensions + 1.0) * integerPower(phi, dimensions) - 1.0;
            phi -= value / derivative;
        }
        return 1.0 / phi;
    }

    private static double sphereConstant(int dimensions) {
        return SPHERE_CONSTANTS[dimensions];
    }

    private static double[] sphereConstants() {
        double[] result = new double[32];
        result[1] = 1.0;
        result[2] = Math.PI * 0.25;
        for (int dimensions = 3; dimensions < result.length; dimensions++) {
            result[dimensions] = result[dimensions - 2] * Math.PI / (2.0 * dimensions);
        }
        return result;
    }

    private static double[] sinPowerHalfPi() {
        double[] result = new double[31];
        result[0] = Math.PI * 0.5;
        result[1] = 1.0;
        for (int power = 2; power < result.length; power++) {
            result[power] = (power - 1.0) / power * result[power - 2];
        }
        return result;
    }

    private static double[] reciprocalGoldenRatios() {
        double[] result = new double[31];
        for (int dimensions = 1; dimensions < result.length; dimensions++) {
            result[dimensions] = reciprocalGeneralizedGoldenRatio(dimensions);
        }
        return result;
    }

    private static double integerPower(double value, int exponent) {
        double result = 1.0;
        double factor = value;
        int remaining = exponent;
        while (remaining > 0) {
            if ((remaining & 1) != 0) {
                result *= factor;
            }
            factor *= factor;
            remaining >>>= 1;
        }
        return result;
    }

    private static double fractional(double value) {
        return value - Math.floor(value);
    }

    private record RadialPoints(double[] values, int count, int dimensions, int stride) {
    }
}
