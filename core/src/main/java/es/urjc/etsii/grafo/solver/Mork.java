package es.urjc.etsii.grafo.solver;

import com.fasterxml.jackson.core.StreamReadConstraints;
import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.annotations.InheritedComponent;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.services.BannerProvider;
import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.util.Context;
import es.urjc.etsii.grafo.util.ExceptionUtil;
import org.slf4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;


/**
 * This class is in charge of launching Mork.
 */
@SpringBootApplication
@EnableAsync
@ComponentScan(basePackages = "${advanced.scan-pkgs:es.urjc.etsii}", includeFilters = @ComponentScan.Filter(InheritedComponent.class))
public class Mork {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(Mork.class);

    /**
     * Procedure to launch the application.
     * Deprecated: Use the version with the objectives parameter instead
     * @see Mork#start(String, String[], Objective[])
     * @param args     command line arguments, normally the parameter "String[] args" in the main method
     * @param fmode MAXIMIZE if the objective function of this problem should be maximized, MINIMIZE if it should be minimized
     */
    public static <S extends Solution<S,I>, I extends Instance> void start(String[] args, FMode fmode) {
        Mork.start(null, args, Objective.of(fmode, S::getScore, Move::getValue));
    }

    /**
     * Procedure to launch the application.
     * Deprecated: Use the version with the objectives parameter instead
     * @see Mork#start(String, String[], Objective[])
     * @param pkgRoot  Custom package root for component scanning if changed from the default package
     *                 (es.urjc.etsii)
     * @param args     command line arguments, normally the parameter "String[] args" in the main method
     * @param fmode MAXIMIZE if the objective function of this problem should be maximized, MINIMIZE if it should be minimized
     */
    @Deprecated
    public static <S extends Solution<S,I>, I extends Instance> void start(String pkgRoot, String[] args, FMode fmode) {
        Mork.start(pkgRoot, args, Objective.of(fmode, S::getScore, Move::getValue));
    }

    /**
     * Procedure to launch the application.
     *
     * @param args     command line arguments, normally the parameter "String[] args" in the main method
     * @param objectives List of objectives to track
     */
    public static <S extends Solution<S,I>, I extends Instance> void start(String[] args, Objective<M,S,I>... objectives){
        Mork.start(null, args, objectives);
    }

    /**
     * Procedure to launch the application.
     *
     * @param pkgRoot  Custom package root for component scanning if changed from the default package
     * @param args     command line arguments, normally the parameter "String[] args" in the main method
     * @param objectives List of objectives to track
     */
    public static void start(String pkgRoot, String[] args, Objective<?, ?>... objectives) {
        args = argsProcessing(args);
        configurePackageScanning(pkgRoot);
        configureDeserialization();
        Context.Configurator.initialize();
        Context.Configurator.setObjectives(objectives);
        SpringApplication application = new SpringApplication(Mork.class);
        application.setBanner(new BannerProvider());
        application.setLogStartupInfo(false);
        try {
            application.run(args);
        } catch (Exception e) {
            var rootCause = ExceptionUtil.getRootCause(e);
            log.error("%s: %s".formatted(rootCause.getClass().getSimpleName(), rootCause.getMessage()));
            log.info("Unhandled exception: ", e);
        }
    }

    /**
     * Configure JSON deserialization to allow reading very long strings, required by the current autoconfig implementation
     */
    private static void configureDeserialization() {
        var morkReadConstraints = StreamReadConstraints.builder().maxStringLength(Integer.MAX_VALUE).build();
        StreamReadConstraints.overrideDefaultStreamReadConstraints(morkReadConstraints);
    }

    /**
     * Set solving mode
     * Warning: Changing the objectives after the solving engine has started has undefined behaviour
     *
     * @param objectives List of objectives to track
     */
    @SafeVarargs
    public static <S extends Solution<S,I>, I extends Instance> void setObjectives(Objective<M,S,I>... objectives) {

    }

    private static void configurePackageScanning(String pkgRoot) {
        String pkgs = "es.urjc.etsii";
        if (pkgRoot != null) {
            pkgs += "," + pkgRoot;
        }
        System.setProperty("advanced.scan-pkgs", pkgs);
    }

    private static boolean setProperty(String k, String v) {
        if (System.getProperty(k) != null) {
            return false;
        }
        System.setProperty(k, v);
        return true;
    }

    private static String[] argsProcessing(String[] args) {
        // Enable profile according to user config
        String[] result = new String[args.length+1];
        for (int i = 0; i < args.length; i++) {
            result[i] = switch (args[i].trim()) {
                case "--instance-selector" -> "--spring.profiles.active=instance-selector";
                case "--irace" -> "--spring.profiles.active=irace";
                case "--autoconfig" -> "--spring.profiles.active=autoconfig";
                case "--util" -> "--spring.profiles.active=util";
                default -> args[i];
            };
        }

        boolean profileSet = false;
        for(var arg: result){
            if(arg != null && arg.startsWith("--spring.profiles.active=")){
                profileSet = true;
                break;
            }
        }
        // Default profile
        if(!profileSet){
            result[result.length-1] = "--spring.profiles.active=user-experiment";
        } else {
            result[result.length-1] = "";
        }
        return result;
    }
}
