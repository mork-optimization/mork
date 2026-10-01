package es.urjc.etsii.grafo.mreflp.experiments;

import es.urjc.etsii.grafo.experiment.reference.ReferenceResult;
import es.urjc.etsii.grafo.experiment.reference.ReferenceResultProvider;

public final class PublishedReferenceProviders {
    private PublishedReferenceProviders() {}
    public abstract static class Provider extends ReferenceResultProvider {
        private final ReferenceCatalog catalog;
        private final String method;
        protected Provider(ReferenceCatalog catalog, String method) { this.catalog = catalog; this.method = method; }
        @Override public ReferenceResult getValueFor(String instanceName) {
            var reference = catalog.get(instanceName, method);
            // Unverified suspect values remain in the audit/report, not in Mork's reference comparisons.
            if (reference == null || reference.flagged()) return EMPTY_REFERENCE_RESULT;
            var result = new ReferenceResult().addScore("Cost", reference.cost());
            if (reference.timeToBestSeconds() != null) result.setTimeToBestInSeconds(reference.timeToBestSeconds());
            result.setOptimalValue(reference.optimal());
            return result;
        }
        @Override public String getProviderName() { return "Paper-" + method; }
    }
    public static class BKV extends Provider { public BKV(ReferenceCatalog c) { super(c, "BKV"); } }
    public static class ILP extends Provider { public ILP(ReferenceCatalog c) { super(c, "re-ILP"); } }
    public static class SDP extends Provider { public SDP(ReferenceCatalog c) { super(c, "SDP"); } }
    public static class AMA2 extends Provider { public AMA2(ReferenceCatalog c) { super(c, "AMA2"); } }
    public static class GRASP extends Provider { public GRASP(ReferenceCatalog c) { super(c, "GRASP"); } }
    public static class GRASPRelaxed extends Provider { public GRASPRelaxed(ReferenceCatalog c) { super(c, "GRASP_relaxed"); } }
    public static class LMLS extends Provider { public LMLS(ReferenceCatalog c) { super(c, "LMLS"); } }
    public static class LMLSRelaxed extends Provider { public LMLSRelaxed(ReferenceCatalog c) { super(c, "LMLS_relaxed"); } }
}
