package es.urjc.etsii.grafo.solver;

import es.urjc.etsii.grafo.solver.services.BannerProvider;
import es.urjc.etsii.grafo.solver.annotations.InheritedComponent;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication
@ComponentScan(basePackages = "es.urjc.etsii", includeFilters = @ComponentScan.Filter(InheritedComponent.class))
public class Mork {
    public static void start(String[] args) {
        SpringApplication application = new SpringApplication(Mork.class);
        application.setBanner(new BannerProvider());
        application.run(args);
    }
}
