package es.urjc.etsii.grafo.aop;

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
        var methodSignature = (MethodSignature) point.getSignature();
        Method method = methodSignature.getMethod();

        Logger log = LoggerFactory.getLogger(method.getDeclaringClass());
        var annotation = method.getAnnotation(TimeStats.class);

        log.error("Started execution of {}()", method.getName());
        long start = System.nanoTime();
        var response = point.proceed();
        long end = System.nanoTime();
        log.error("Execution of method {}() took {} ms", method.getName(), TimeUtil.convert(end-start, TimeUnit.NANOSECONDS, TimeUnit.MILLISECONDS));

        return response;
    }
}

