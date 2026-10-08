// SPDX-License-Identifier: MPL-2.0
package es.urjc.etsii.grafo.moocore;

/** Hypervolume deviation relative to a reference front; lower values are better. */
public final class RelativeHypervolume extends Hypervolume {

    private final double referenceHypervolume;

    public RelativeHypervolume(double[] reference, double[][] referenceSet) {
        this(reference, referenceSet, new boolean[]{false});
    }

    public RelativeHypervolume(double[] reference, double[][] referenceSet, boolean[] maximise) {
        super(reference, maximise);
        referenceHypervolume = super.compute(referenceSet);
        if (referenceHypervolume == 0.0) {
            throw new IllegalArgumentException("reference-set hypervolume is zero");
        }
    }

    @Override
    public double compute(double[][] points) {
        return 1.0 - super.compute(points) / referenceHypervolume;
    }
}
