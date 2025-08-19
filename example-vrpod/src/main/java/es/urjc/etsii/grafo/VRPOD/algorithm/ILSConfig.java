package es.urjc.etsii.grafo.VRPOD.algorithm;

import es.urjc.etsii.grafo.VRPOD.model.instance.VRPODInstance;
import es.urjc.etsii.grafo.VRPOD.model.solution.VRPODSolution;
import es.urjc.etsii.grafo.create.Constructive;
import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.shake.Shake;

import java.util.function.Supplier;

public class ILSConfig {
    final Constructive<VRPODSolution, VRPODInstance> constructor;
    final Shake<VRPODSolution, VRPODInstance> shake;
    final Improver<VRPODSolution, VRPODInstance> improver;
    final int shakeStrength;
    final int nShakes;

    public ILSConfig(
            int shakeStrength,
            int nShakes,
            Supplier<Constructive<VRPODSolution, VRPODInstance>> constructorSupplier,
            Supplier<Shake<VRPODSolution, VRPODInstance>> destructorSupplier,
            Supplier<Improver<VRPODSolution, VRPODInstance>> improver) {
        this.shakeStrength = shakeStrength;
        this.nShakes = nShakes;
        this.constructor = constructorSupplier.get();
        this.shake = destructorSupplier.get();
        this.improver = improver.get();
    }

    public ILSConfig(int shakeStrength, Supplier<Constructive<VRPODSolution, VRPODInstance>> constructorSupplier,
                     Supplier<Shake<VRPODSolution, VRPODInstance>> destructorSupplier,
                     Supplier<Improver<VRPODSolution, VRPODInstance>> improver
    ) {
        this(shakeStrength, -1, constructorSupplier, destructorSupplier, improver);
    }

    @Override
    public String toString() {
        return "ILSConfig{" +
                "shakeStrength=" + shakeStrength +
                ", nShakes=" + nShakes +
                ", constructor=" + constructor +
                ", destructor=" + shake +
                ", improver=" + improver +
                '}';
    }
}
