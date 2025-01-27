package es.urjc.etsii.grafo.aop;

import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.metrics.Metrics;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.util.Context;
import es.urjc.etsii.grafo.util.TimeUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

@Aspect
@SuppressWarnings({"rawtypes", "unchecked"}) // todo investigate if we can avoid using raw types, probably not
public final class TimedAspect {

    @Around("execution(* *(..)) && @annotation(es.urjc.etsii.grafo.aop.TimeStats)")
    public Object log(ProceedingJoinPoint point) throws Throwable {
        return commonLog(point);
    }

    @Around(value = "execution(* es.urjc.etsii.grafo.improve.Improver+.improve(..)) && target(improver) && args(solution)", argNames = "point,improver,solution")
    public Object logImprover(ProceedingJoinPoint point, Improver improver, Solution solution) throws Throwable {
        Objective objective = improver.getObjective();
        double initialScore = objective.evalSol(solution);
        Solution improvedSolution = (Solution) commonLog(point);
        double endScore = objective.evalSol(improvedSolution);

        // Log, verify and store
        Improver.log.debug("{} --> {}", initialScore, endScore);
        if(objective.isBetter(initialScore, endScore)){
            throw new IllegalStateException(String.format("Score has worsened after executing an improvement method: %s --> %s", initialScore, endScore));
        }
        Metrics.addCurrentObjectives(solution);

        return improvedSolution;
    }

    @Around("execution(* es.urjc.etsii.grafo.algorithms.Algorithm+.algorithm(..))")
    public Object logAlgorithm(ProceedingJoinPoint point) throws Throwable {
        return commonLog(point);
    }

    @Around("execution(* es.urjc.etsii.grafo.shake.Shake+.shake(..))")
    public Object logShake(ProceedingJoinPoint point) throws Throwable {
        return commonLog(point);
    }

    @Around("execution(* es.urjc.etsii.grafo.create.Constructive+.construct(..))")
    public Object logConstruct(ProceedingJoinPoint point) throws Throwable {
        return commonLog(point);
    }

    public Object commonLog(ProceedingJoinPoint point) throws Throwable {
        var methodSignature = (MethodSignature) point.getSignature();
        Method method = methodSignature.getMethod();
        //var clazz = method.getDeclaringClass();
        var clazz = point.getThis().getClass();
        Logger log = LoggerFactory.getLogger(clazz);

        var annotation = method.getAnnotation(TimeStats.class);
        long start = System.nanoTime();
        if(Metrics.areMetricsEnabled()){
            Context.addTimeEvent(true, start, clazz.getSimpleName(), method.getName());
        }
        var retVal = point.proceed();
        if(Metrics.areMetricsEnabled()){
            long end = System.nanoTime();
            Context.addTimeEvent(false, end, clazz.getSimpleName(), method.getName());
            log.trace("{}() took {} ms", method.getName(), TimeUtil.convert(end-start, TimeUnit.NANOSECONDS, TimeUnit.MILLISECONDS));
        }

        return retVal;
    }
}

