package es.urjc.etsii.grafo.solver;

import com.fasterxml.jackson.core.StreamReadConstraints;
import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.annotations.InheritedComponent;
import es.urjc.etsii.grafo.services.BannerProvider;
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
    private static FMode fmode;

    /**
     * Procedure to launch the application.
     *
     * @param args     command line arguments, normally the parameter "String[] args" in the main method
     * @param fmode MAXIMIZE if the objective function of this problem should be maximized, MINIMIZE if it should be minimized
     */
    public static boolean start(String[] args, FMode fmode) {
        return Mork.start(null, args, fmode);
    }

    /**
     * Procedure to launch the application, alias to start, fails if mode is not specified
     *
     * @param args     command line arguments, normally the parameter "String[] args" in the main method
     */
    public static boolean main(String[] args) {
        if(Mork.fmode == null){
            throw new IllegalStateException("FMode not set");
        }
        return Mork.start(null, args, Mork.fmode);
    }


    /**
     * Procedure to launch the application.
     *
     * @param pkgRoot  Custom package root for component scanning if changed from the default package
     * @param args     command line arguments, normally the parameter "String[] args" in the main method
     * @param fmode MAXIMIZE if the objective function of this problem should be maximized, MINIMIZE if it should be minimized
     */
    public static boolean start(String pkgRoot, String[] args, FMode fmode) {
        args = argsProcessing(args);
        configurePackageScanning(pkgRoot);
        configureDeserialization();
        setSolvingMode(fmode);
        SpringApplication application = new SpringApplication(Mork.class);
        application.setBanner(new BannerProvider());
        application.setLogStartupInfo(false);
        try {
            application.run(args);
            return true;
        } catch (Exception e) {
            var rootCause = ExceptionUtil.getRootCause(e);
            log.error("%s: %s".formatted(rootCause.getClass().getSimpleName(), rootCause.getMessage()));
            log.info("Unhandled exception: ", e);
            return false;
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
     * Set solving mode,
     * Warning: Changing the solving mode once the solving engine has started has undefined behaviour
     *
     * @param fmode MAXIMIZE if maximizing o.f, MINIMIZING if minimizing
     */
    public static void setSolvingMode(FMode fmode) {
        Mork.fmode = fmode;
    }

    /**
     * Solving mode
     *
     * @return true if maximizing, false if minimizing
     */
    public static boolean isMaximizing() {
        return fmode == FMode.MAXIMIZE;
    }

    /**
     * Solving mode
     *
     * @return true if minimizing, false if maximizing
     */
    public static boolean isMinimizing() {
        return fmode == FMode.MINIMIZE;
    }

    public static FMode getFMode(){
        return fmode;
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
