package es.urjc.etsii.grafo.util;

import org.apache.commons.lang3.exception.ExceptionUtils;

public class ExceptionUtil {

    public static String[] filterTerms = new String[]{
            "$AjcClosure",
            "org.springframework",
            "java.base/java.util.stream",
            "org.aspectj.runtime.reflect.JoinPointImpl.proceed",
            "_aroundBody0",
            "es.urjc.etsii.grafo.aop.TimedAspect."
    };

    /**
     * Get the root cause of an exception
     * @param e exception to investigate
     * @return root cause
     */
    public static Throwable getRootCause(Throwable e){
        if(e == null){
            throw new IllegalArgumentException("Exception cannot be null");
        }
        while(e.getCause() != null){
            e = e.getCause();
        }
        return e;
    }

    public static String filteredStacktrace(Throwable e){
        var frames = ExceptionUtils.getStackFrames(e);
        StringBuilder sb = new StringBuilder();
        for(var frame: frames){
            if(!filterFrame(frame)){
                sb.append(frame).append(System.lineSeparator());
            }
        }
        return sb.toString();
    }

    private static boolean filterFrame(String frame){
        for(var filter: filterTerms){
            if(frame.contains(filter)){
                return true;
            }
        }
        return false;
    }
}
