package es.urjc.etsii.grafo.solver;

import es.urjc.etsii.grafo.solver.services.BannerProvider;
import es.urjc.etsii.grafo.solver.annotations.InheritedComponent;
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

    /**
     * Procedure to launch the application.
     *
     * @param args program arguments
     */
    public static void start(String[] args) {
        Mork.start(null, args);
    }

    public static void start(String pkgRoot, String[] args){
        configurePackageScanning(pkgRoot);
        configureLogging();
        SpringApplication application = new SpringApplication(Mork.class);
        application.setBanner(new BannerProvider());
        application.run(args);
    }

    private static void configurePackageScanning(String pkgRoot){
        String pkgs = "es.urjc.etsii";
        if(pkgRoot != null){
            pkgs += "," + pkgRoot;
        }
        System.setProperty("advanced.scan-pkgs", pkgs);
    }

    private static void configureLogging(){
        setProperty("logging.level.org.apache.catalina", "WARN");
        setProperty("logging.level.org.springframework", "WARN");
    }

    private static boolean setProperty(String k, String v){
        if(System.getProperty(k) != null){
            return false;
        }
        System.setProperty(k, v);
        return true;
    }
}
