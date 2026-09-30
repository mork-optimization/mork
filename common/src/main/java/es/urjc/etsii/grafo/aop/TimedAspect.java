package es.urjc.etsii.grafo.aop;

import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.metrics.Metrics;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.util.TimeStatsUtil;
import es.urjc.etsii.grafo.metrics.timing.TimeStatsMethod;
import org.aspectj.lang.Signature;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

@Aspect
@SuppressWarnings({"rawtypes", "unchecked"}) // todo investigate if we can avoid using raw types, probably not
public final class TimedAspect {

    private static final Logger log = LoggerFactory.getLogger(TimedAspect.class);

    private static final ClassValue<ConcurrentHashMap<Signature, TimeStatsMethod>> METHODS = new ClassValue<>() {
        @Override
        protected ConcurrentHashMap<Signature, TimeStatsMethod> computeValue(Class<?> type) {
            return new ConcurrentHashMap<>();
        }
    };

    @Pointcut("execution(* es.urjc.etsii.grafo.algorithms.Algorithm+.algorithm(..))")
    private void algorithmExecution() {}

    @Pointcut("execution(* es.urjc.etsii.grafo.create.Constructive+.construct(..))")
    private void constructiveExecution() {}

    @Pointcut("execution(* es.urjc.etsii.grafo.improve.Improver+.improve(..))"
            + " && target(es.urjc.etsii.grafo.improve.Improver) && args(es.urjc.etsii.grafo.solution.Solution)")
    private void improverExecution() {}

    @Pointcut("execution(* es.urjc.etsii.grafo.shake.Shake+.shake(es.urjc.etsii.grafo.solution.Solution+, int))"
            + " && target(es.urjc.etsii.grafo.shake.Shake)")
    private void shakeExecution() {}

    // The automatic component advices already time these methods.
    @Around("execution(* *(..)) && @annotation(es.urjc.etsii.grafo.aop.TimeStats)"
            + " && !(algorithmExecution() || constructiveExecution() || improverExecution() || shakeExecution())")
    public Object log(ProceedingJoinPoint point) throws Throwable {
        return commonLog(point);
    }

    @Around(value = "improverExecution() && target(improver) && args(solution)", argNames = "point,improver,solution")
    public Object logImprover(ProceedingJoinPoint point, Improver improver, Solution solution) throws Throwable {
        Objective objective = improver.getObjective();
        double initialScore = objective.evalSol(solution);
        Solution improvedSolution = (Solution) commonLog(point);
        double endScore = objective.evalSol(improvedSolution);

        // Log, verify and store
        log.debug("{}: {} --> {}", improver.getClass().getSimpleName(), initialScore, endScore);
        if(objective.isBetter(initialScore, endScore)){
            throw new IllegalStateException(String.format("Score has worsened after executing an improvement method: %s --> %s", initialScore, endScore));
        }
        Metrics.addCurrentObjectives(improvedSolution);

        return improvedSolution;
    }

    @Around("algorithmExecution()")
    public Object logAlgorithm(ProceedingJoinPoint point) throws Throwable {
        return commonLog(point);
    }

    @Around("shakeExecution()")
    public Object logShake(ProceedingJoinPoint point) throws Throwable {
        return commonLog(point);
    }

    @Around("constructiveExecution()")
    public Object logConstruct(ProceedingJoinPoint point) throws Throwable {
        return commonLog(point);
    }

    public Object commonLog(ProceedingJoinPoint point) throws Throwable {
        var recorder = TimeStatsUtil.current();
        if (recorder == null || !recorder.isActive()) return point.proceed();

        var signature = point.getSignature();
        var target = point.getThis();
        var clazz = target == null ? signature.getDeclaringType() : target.getClass();
        var methods = METHODS.get(clazz);
        var method = methods.get(signature);
        if (method == null) {
            method = new TimeStatsMethod(clazz.getName(), signature.toLongString());
            var previous = methods.putIfAbsent(signature, method);
            if (previous != null) method = previous;
        }
        long start = System.nanoTime();
        try {
            return point.proceed();
        } finally {
            recorder.record(method, start, System.nanoTime());
        }
    }
}
