package es.urjc.etsii.grafo.util;

public class ExceptionUtil {
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
}
