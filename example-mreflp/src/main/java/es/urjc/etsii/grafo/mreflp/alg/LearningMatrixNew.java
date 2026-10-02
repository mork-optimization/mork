package es.urjc.etsii.grafo.mreflp.alg;

import es.urjc.etsii.grafo.mreflp.model.MREFLPSolution;
import java.util.Arrays;

/** Copy of the literal learning equations with optional row normalization after smoothing. */
public final class LearningMatrixNew {
    private final double[][] eta;
    private final double alpha, beta, gamma, rho;
    private final boolean normalize;
    public LearningMatrixNew(int n, int k, double alpha, double beta, double gamma, double rho, boolean normalize) {
        if (n < 1 || k < 1) throw new IllegalArgumentException("Invalid learning dimensions");
        for (double factor : new double[]{alpha, beta, gamma, rho}) {
            if (!(factor > 0 && factor < 1)) throw new IllegalArgumentException("Learning factors must be in (0,1)");
        }
        eta = new double[n][k];
        this.alpha = alpha;
        this.beta = beta;
        this.gamma = gamma;
        this.rho = rho;
        this.normalize = normalize;
        for (double[] row : eta) Arrays.fill(row, 1.0 / k);
    }
    public double value(int v, int g) { return eta[v][g]; }

    public void update(int[] initial, MREFLPSolution improved) {
        for (int v = 0; v < eta.length; v++) {
            int origin = initial[v], destination = improved.group(v);
            for (int g = 0; g < eta[v].length; g++) {
                double value = eta[v][g];
                if (origin == destination) {
                    value = (1 - alpha) * value + (g == origin ? alpha : 0);
                } else {
                    value *= (1 - beta) * (1 - gamma);
                    if (g != origin) value += (1 - gamma) * beta / (eta[v].length - 1);
                    if (g == destination) value += gamma;
                }
                eta[v][g] = smooth(value, rho);
            }
            if (normalize) {
                double sum = 0;
                for (double value : eta[v]) sum += value;
                for (int g = 0; g < eta[v].length; g++) eta[v][g] /= sum;
            }
        }
    }

    public static double smooth(double value, double rho) {
        if (value < 0.05) return rho + (1 - rho) * value;
        if (value > 0.95) return (1 - rho) * value;
        return value;
    }
}
