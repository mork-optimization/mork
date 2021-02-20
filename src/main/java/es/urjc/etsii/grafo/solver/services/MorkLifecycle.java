package es.urjc.etsii.grafo.solver.services;

public class MorkLifecycle {
    private static volatile boolean stopping = false;

    /*
     * Listen to shutdown signal to safely finalize all pending tasks
     */
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(()-> {
            stopping = true;
        }));
    }


    /**
     *
     * @return true if the app is finalizing, false if the app is running
     */
    public static boolean stop(){
        return stopping;
    }
}
