package es.urjc.etsii.grafo.solver.services;
//
//import org.springframework.stereotype.Component;
//import sun.misc.Signal;
//import sun.misc.SignalHandler;
//
//@Component
//@SuppressWarnings("all")
//public class Global implements SignalHandler {
//
//    private static volatile boolean stopping = false;
//
//    /*
//     * Listen to shutdown signal to safely finalize all pending tasks
//     */
//    static {
//        Global.install("INT"); // Intercept Control+C
//    }
//
//    private SignalHandler oldHandler;
//
//    public static Global install(String signalName) {
//        Signal diagSignal = new Signal(signalName);
//        Global morkLifecycle = new Global();
//        morkLifecycle.oldHandler = Signal.handle(diagSignal, morkLifecycle);
//        return morkLifecycle;
//    }
//
//    /**
//     * @return true if the app is finalizing, false if the app is running
//     */
//    public static boolean stop() {
//        return stopping;
//    }
//
//    @Override
//    public void handle(Signal sig) {
//        System.out.println("Mork Lifecycle signal handler called for signal " + sig);
//        if(sig.getName().equals("INT")){
//            System.out.println("CONTROL + C RECEIVED, Stopping executors, saving best solutions and stopping...");
//        }
//        try {
//            // Output information for each thread
//            Thread[] threadArray = new Thread[Thread.activeCount()];
//            int numThreads = Thread.enumerate(threadArray);
//            System.out.println("Current threads:");
//            for (int i = 0; i < numThreads; i++) {
//                System.out.println("    " + threadArray[i]);
//            }
//            stopping = true;
//
//            // Chain back to previous handler, if one exists.
//            // Do not call, they may destroy our threads before they voluntarily finish
//            if (oldHandler != SIG_DFL && oldHandler != SIG_IGN) {
//                //oldHandler.handle(sig);
//            }
//        } catch (Exception e) {
//            System.out.println("Signal handler FAILED, reason: " + e);
//        }
//    }
//}

/**
 * <p>Global class.</p>
 *
 */
public class Global {
    private static volatile boolean stopping = false;

    /**
     * <p>stop.</p>
     *
     * @return a boolean.
     */
    public static boolean stop() {
        return stopping;
    }

    public static void setStop(boolean stop){
        Global.stopping = stop;
    }
}
