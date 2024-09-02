package es.urjc.etsii.grafo.aop;

import es.urjc.etsii.grafo.metrics.Metrics;
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
public final class TimedAspect {

    //private static final Logger log = LoggerFactory.getLogger(TimedAspect.class);

    @Around("execution(* *(..)) && @annotation(es.urjc.etsii.grafo.aop.TimeStats)")
    public Object log(ProceedingJoinPoint point) throws Throwable {
        return commonLog(point);
    }

    @Around("execution(* es.urjc.etsii.grafo.improve.Improver+.improve(..))")
    public Object logImprover(ProceedingJoinPoint point) throws Throwable {
        return commonLog(point);
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
        var response = point.proceed();
        if(Metrics.areMetricsEnabled()){
            long end = System.nanoTime();
            Context.addTimeEvent(false, end, clazz.getSimpleName(), method.getName());
            log.trace("{}() took {} ms", method.getName(), TimeUtil.convert(end-start, TimeUnit.NANOSECONDS, TimeUnit.MILLISECONDS));
        }

        return response;
    }
}

