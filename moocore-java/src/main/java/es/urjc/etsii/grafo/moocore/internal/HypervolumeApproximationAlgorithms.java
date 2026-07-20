// SPDX-License-Identifier: MPL-2.0
package es.urjc.etsii.grafo.moocore.internal;

import es.urjc.etsii.grafo.moocore.HypervolumeApproximation;

import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;

public final class HypervolumeApproximationAlgorithms {

    private static final double MINIMUM_WEIGHT = 1e-20;

    private HypervolumeApproximationAlgorithms() {
    }

    public static double approximate(double[][] input, double[] reference, boolean[] maximise,
                                     long samples, long seed, HypervolumeApproximation method) {
        int dimensions = MatrixUtils.validate(input, 1, 31);
        if (samples <= 0 || samples > 2_147_483_648L) {
            throw new IllegalArgumentException("samples must be in [1, 2147483648]");
        }
        if (dimensions == 1) {
            return HypervolumeAlgorithms.hypervolume(input, reference, maximise);
        }
        boolean[] directions = MatrixUtils.directions(maximise, dimensions);
        double[] ref = MatrixUtils.vector(reference, dimensions, "reference");
        List<double[]> transformed = new ArrayList<>();
        for (double[] point : input) {
            double[] distance = new double[dimensions];
            boolean valid = true;
            for (int objective = 0; objective < dimensions; objective++) {
                distance[objective] = directions[objective]
                        ? point[objective] - ref[objective]
                        : ref[objective] - point[objective];
                valid &= distance[objective] > 0.0;
            }
            if (valid) {
                transformed.add(distance);
            }
        }
        if (transformed.isEmpty()) {
            return 0.0;
        }
        double[][] points = transformed.toArray(double[][]::new);
        double expected = switch (method) {
            case DZ2019_MC -> monteCarlo(points, samples, seed);
            case DZ2019_HW -> huaWang(points, samples);
            case RPHI_FWE_PLUS -> rPhi(points, samples);
        };
        return sphereConstant(dimensions) * expected / samples;
    }

    private static double monteCarlo(double[][] points, long samples, long seed) {
        RandomGenerator random = new java.util.Random(seed);
        int dimensions = points[0].length;
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

    private static double huaWang(double[][] points, long samples) {
        int dimensions = points[0].length;
        int angleCount = dimensions - 1;
        int prime = primeForDimension(angleCount);
        long[] coefficients = new long[angleCount];
        coefficients[0] = 1;
        for (int i = 1; i < angleCount; i++) {
            double value = Math.abs(2.0 * Math.cos(2.0 * Math.PI * i / prime));
            coefficients[i] = Math.round(samples * fractional(value));
        }
        double[] angles = new double[angleCount];
        double[] inverseDirection = new double[dimensions];
        double result = 0.0;
        for (long sample = 0; sample < samples; sample++) {
            double factor = sample + 1 < samples ? (sample + 1.0) / samples : 0.0;
            for (int i = 0; i < angleCount; i++) {
                double uniform = fractional(factor * coefficients[i]);
                int power = dimensions - i - 2;
                angles[i] = inverseSinPowerIntegral(uniform, power);
            }
            huaWangDirection(inverseDirection, angles);
            result += radialPower(points, inverseDirection);
        }
        return result;
    }

    private static double rPhi(double[][] points, long samples) {
        int dimensions = points[0].length;
        int sequenceDimensions = dimensions - 1;
        double reciprocalPhi = reciprocalGeneralizedGoldenRatio(sequenceDimensions);
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

    private static double radialPower(double[][] points, double[] inverseDirection) {
        double maximum = 0.0;
        for (double[] point : points) {
            double minimum = Double.POSITIVE_INFINITY;
            for (int objective = 0; objective < point.length; objective++) {
                minimum = Math.min(minimum, point[objective] * inverseDirection[objective]);
            }
            maximum = Math.max(maximum, minimum);
        }
        return Math.pow(maximum, points[0].length);
    }

    private static void huaWangDirection(double[] inverseDirection, double[] angles) {
        int dimensions = inverseDirection.length;
        double[] sine = new double[angles.length];
        double[] cosine = new double[angles.length];
        for (int i = 0; i < angles.length; i++) {
            sine[i] = Math.sin(angles[i]);
            cosine[i] = Math.cos(angles[i]);
        }
        double[] direction = new double[dimensions];
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

    private static double inverseSinPowerIntegral(double fraction, int power) {
        if (fraction <= 0.0) {
            return 0.0;
        }
        if (fraction >= 1.0) {
            return Math.PI * 0.5;
        }
        double total = sinPowerIntegral(power, Math.PI * 0.5);
        double target = fraction * total;
        double low = 0.0;
        double high = Math.PI * 0.5;
        for (int iteration = 0; iteration < 60; iteration++) {
            double middle = (low + high) * 0.5;
            if (sinPowerIntegral(power, middle) < target) {
                low = middle;
            } else {
                high = middle;
            }
        }
        return (low + high) * 0.5;
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
        return -cosine * Math.pow(sine, power - 1) / power
                + (power - 1.0) / power * sinPowerIntegral(power - 2, upper);
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
            double power = Math.pow(phi, dimensions + 1);
            double value = power - phi - 1.0;
            double derivative = (dimensions + 1.0) * Math.pow(phi, dimensions) - 1.0;
            phi -= value / derivative;
        }
        return 1.0 / phi;
    }

    private static double sphereConstant(int dimensions) {
        double sphereArea = 2.0 * Math.pow(Math.PI, dimensions / 2.0) / gamma(dimensions / 2.0);
        return sphereArea / (dimensions * Math.pow(2.0, dimensions));
    }

    private static double gamma(double value) {
        double[] coefficients = {
                676.5203681218851, -1259.1392167224028, 771.32342877765313,
                -176.61502916214059, 12.507343278686905, -0.13857109526572012,
                9.9843695780195716e-6, 1.5056327351493116e-7
        };
        if (value < 0.5) {
            return Math.PI / (Math.sin(Math.PI * value) * gamma(1.0 - value));
        }
        double shifted = value - 1.0;
        double sum = 0.99999999999980993;
        for (int i = 0; i < coefficients.length; i++) {
            sum += coefficients[i] / (shifted + i + 1.0);
        }
        double t = shifted + coefficients.length - 0.5;
        return Math.sqrt(2.0 * Math.PI) * Math.pow(t, shifted + 0.5) * Math.exp(-t) * sum;
    }

    private static double fractional(double value) {
        return value - Math.floor(value);
    }
}
