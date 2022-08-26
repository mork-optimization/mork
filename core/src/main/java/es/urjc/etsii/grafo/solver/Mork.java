package es.urjc.etsii.grafo.solver;

import es.urjc.etsii.grafo.annotations.InheritedComponent;
import es.urjc.etsii.grafo.services.BannerProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;


/**
 *  This class is in charge of launching Mork.
 */
@SpringBootApplication
@EnableAsync
@ComponentScan(basePackages = "${advanced.scan-pkgs:es.urjc.etsii}", includeFilters = @ComponentScan.Filter(InheritedComponent.class))
public class Mork {

    private static boolean maximizing;

    /**
     * Procedure to launch the application.
     *
     * @param args command line arguments, normally the parameter "String[] args" in the main method
     * @param maximize true if this is a maximization problem, false if minimizing
     */
    public static void start(String[] args, boolean maximize) {
        Mork.start(null, args, maximize);
    }

    /**
     * Procedure to launch the application.
     *
     * @param pkgRoot Custom package root for component scanning if changed from the default package
     * @param args command line arguments, normally the parameter "String[] args" in the main method
     * @param maximize true if this is a maximization problem, false if minimizing
     */
    public static void start(String pkgRoot, String[] args, boolean maximize){
        configurePackageScanning(pkgRoot);
        configureLogging();
        setSolvingMode(maximize);
        SpringApplication application = new SpringApplication(Mork.class);
        application.setBanner(new BannerProvider());
        application.run(args);
    }

    /**
     * Set solving mode,
     * Warning: Changing the solving mode once the solving engine has started has undefined behaviour
     * @param maximize
     */
    public static void setSolvingMode(boolean maximize) {
        Mork.maximizing = maximize;
        System.setProperty("solver.maximizing", String.valueOf(maximize));
    }

    /**
     * Solving mode
     * @return true if maximizing, false if minimizing
     */
    public static boolean isMaximizing(){
        return maximizing;
    }

    private static void configurePackageScanning(String pkgRoot){
        String pkgs = "es.urjc.etsii";
        if(pkgRoot != null){
            pkgs += "," + pkgRoot;
        }
        System.setProperty("advanced.scan-pkgs", pkgs);
    }

    private static void configureLogging(){
        // Log only important messages from Spring and web server
        setProperty("logging.level.org.apache.catalina", "WARN");
        setProperty("logging.level.org.springframework", "WARN");

        // Set default log level, if not overriden by the user, to INFO
        setProperty("logging.level.root", "INFO");
        //setProperty("es.urjc.etsii.grafo", "INFO");

        // Export logs to file "log.txt" by default
        setProperty("logging.file.name", "log.txt");

        // Default log formatting
        setProperty("logging.pattern.console", "%clr([%d{${LOG_DATEFORMAT_PATTERN:HH:mm:ss}}]){faint} %clr(${LOG_LEVEL_PATTERN:%3p}) %clr(%-26.26logger{25}){cyan} %clr(:){faint} %m%n${LOG_EXCEPTION_CONVERSION_WORD:%wEx}");
        setProperty("logging.pattern.file", "%clr([%d{${LOG_DATEFORMAT_PATTERN:HH:mm:ss}}]){faint} %clr(${LOG_LEVEL_PATTERN:%3p}) %clr(%-26.26logger{25}){cyan} %clr(:){faint} %m%n${LOG_EXCEPTION_CONVERSION_WORD:%wEx}");
    }

    private static boolean setProperty(String k, String v){
        if(System.getProperty(k) != null){
            return false;
        }
        System.setProperty(k, v);
        return true;
    }
}
