package es.urjc.etsii.grafo.CAP;

import es.urjc.etsii.grafo.CAP.model.CAPInstance;
import es.urjc.etsii.grafo.CAP.model.CAPSolution;
import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.services.TimeLimitCalculator;
import es.urjc.etsii.grafo.util.TimeUtil;

import java.util.concurrent.TimeUnit;

public class CAPTimeControl extends TimeLimitCalculator<CAPSolution, CAPInstance> {

    public static final long DEFAULT_IF_UNKNOWN = 5_000; // 5 seconds if UNKNOWN REF VALUE
    private final CAPReferenceResults referenceResults;

    public CAPTimeControl(CAPReferenceResults referenceResults) {
        this.referenceResults = referenceResults;
    }

    @Override
    public long timeLimitInMillis(CAPInstance instance, Algorithm<CAPSolution, CAPInstance> algorithm) {
        var refResult = referenceResults.getValueFor(instance.getId());
        if(refResult == null){
            return DEFAULT_IF_UNKNOWN;
        }
        var nanos = refResult.getTimeInNanos();
        if(nanos <= 0){
            return DEFAULT_IF_UNKNOWN; // Zero or negative values are invalid time limits
        }
        var timelimit = TimeUtil.convert(nanos, TimeUnit.NANOSECONDS, TimeUnit.MILLISECONDS) * 2;
        return Math.max(1_000, timelimit); // If the timelimit is really small, use 1 second as timelimit.
    }
}
