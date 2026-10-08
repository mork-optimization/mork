// SPDX-License-Identifier: MPL-2.0
package es.urjc.etsii.grafo.moocore;

import java.util.function.ToDoubleFunction;

/** Reusable exact hypervolume indicator. */
public class Hypervolume implements ToDoubleFunction<double[][]> {

    private final double[] reference;
    private final boolean[] maximise;

    public Hypervolume(double[] reference) {
        this(reference, new boolean[]{false});
    }

    public Hypervolume(double[] reference, boolean[] maximise) {
        if (reference == null || reference.length == 0) {
            throw new IllegalArgumentException("reference cannot be empty");
        }
        this.reference = reference.clone();
        this.maximise = maximise == null ? new boolean[]{false} : maximise.clone();
    }

    public Hypervolume(double reference, boolean maximise) {
        this(new double[]{reference}, new boolean[]{maximise});
    }

    /** Compute hypervolume for the supplied points. */
    public double compute(double[][] points) {
        return MooCore.hypervolume(points, reference, maximise);
    }

    @Override
    public double applyAsDouble(double[][] points) {
        return compute(points);
    }
}
