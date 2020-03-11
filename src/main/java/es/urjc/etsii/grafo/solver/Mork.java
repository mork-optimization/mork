package es.urjc.etsii.grafo.solver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("es.urjc.etsii")
public class Mork {
    public static void start(String[] args) {
        SpringApplication.run(Mork.class, args);
    }
}
