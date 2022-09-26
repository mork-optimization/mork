package es.urjc.etsii.grafo.solver;

import es.urjc.etsii.grafo.annotations.InheritedComponent;
import es.urjc.etsii.grafo.services.BannerProvider;
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

    private static boolean maximizing;

    /**
     * Procedure to launch the application.
     *
     * @param args     command line arguments, normally the parameter "String[] args" in the main method
     * @param maximize true if this is a maximization problem, false if minimizing
     */
    public static void start(String[] args, boolean maximize) {
        Mork.start(null, args, maximize);
    }

    /**
     * Procedure to launch the application.
     *
     * @param pkgRoot  Custom package root for component scanning if changed from the default package
     * @param args     command line arguments, normally the parameter "String[] args" in the main method
     * @param maximize true if this is a maximization problem, false if minimizing
     */
    public static void start(String pkgRoot, String[] args, boolean maximize) {
        args = argsProcessing(args);
        configurePackageScanning(pkgRoot);
        setSolvingMode(maximize);
        SpringApplication application = new SpringApplication(Mork.class);
        application.setBanner(new BannerProvider());
        application.setLogStartupInfo(false);
        application.run(args);
    }

    /**
     * Set solving mode,
     * Warning: Changing the solving mode once the solving engine has started has undefined behaviour
     *
     * @param maximize
     */
    public static void setSolvingMode(boolean maximize) {
        Mork.maximizing = maximize;
        System.setProperty("solver.maximizing", String.valueOf(maximize));
    }

    /**
     * Solving mode
     *
     * @return true if maximizing, false if minimizing
     */
    public static boolean isMaximizing() {
        return maximizing;
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
                case "--irace" -> "--spring.profiles.active=irace";
                case "--autoconfig" -> "--spring.profiles.active=autoconfig";
                case "--util" -> "--spring.profiles.active=util";
                default -> args[i];
            };
        }

        // By default, active profile is user-experiment, unless overridden
        result[result.length-1] = "--spring.profiles.default=user-experiment";
        return result;
    }
}
