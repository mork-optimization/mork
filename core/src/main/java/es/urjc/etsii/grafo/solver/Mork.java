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
     *
     * @param args     command line arguments, normally the parameter "String[] args" in the main method
     * @param objective Objective to optimize, assumes single objective
     */
    public static <M extends Move<S,I>, S extends Solution<S,I>, I extends Instance> boolean start(String[] args, Objective<M,S,I> objective){
        return Mork.start(null, args, false, objective);
    }

    /**
     * Procedure to launch the application.
     *
     * @param args     command line arguments, normally the parameter "String[] args" in the main method
     * @param multiobjective true if the problem is multiobjective, false otherwise
     * @param objectives List of objectives to track
     */
    @SafeVarargs
    public static <M extends Move<S,I>, S extends Solution<S,I>, I extends Instance> boolean start(String[] args, boolean multiobjective, Objective<M,S,I>... objectives){
        return Mork.start(null, args, multiobjective, objectives);
    }

    /**
     * Procedure to launch the application.
     *
     * @param pkgRoot  Custom package root for component scanning if changed from the default package
     * @param args     command line arguments, normally the parameter "String[] args" in the main method
     * @param multiobjective true if the problem is multiobjective, false otherwise
     * @param objectives List of objectives to track
     */
    @SafeVarargs
    public static <M extends Move<S,I>, S extends Solution<S,I>, I extends Instance> boolean start(String pkgRoot, String[] args, boolean multiobjective, Objective<M, S, I>... objectives) {
        configurePackageScanning(pkgRoot);
        configureDeserialization();
        Context.Configurator.setObjectives(multiobjective, objectives);
        SpringApplication application = new SpringApplication(Mork.class);
        application.setBanner(new BannerProvider());
        application.setLogStartupInfo(false);
        try {
            application.run(args);
            return true;
        } catch (Exception e) {
            var rootCause = ExceptionUtil.getRootCause(e);
            log.error("%s: %s".formatted(rootCause.getClass().getSimpleName(), rootCause.getMessage()));
            log.info("Simplified stacktrace: {}", ExceptionUtil.filteredStacktrace(e));
            log.trace("Full stacktrace: ", e);
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

    private static void configurePackageScanning(String pkgRoot) {
        String pkgs = "es.urjc.etsii";
        if (pkgRoot != null) {
            pkgs += "," + pkgRoot;
        }
        System.setProperty("advanced.scan-pkgs", pkgs);
    }
}
