package es.urjc.etsii.grafo.services;

import es.urjc.etsii.grafo.solver.Mork;
import org.springframework.boot.Banner;
import org.springframework.core.env.Environment;

import java.io.PrintStream;

/**
 * Banner generator on startup
 */
public class BannerProvider implements Banner {

    /**
     * {@inheritDoc}
     *
     * Get aplication banner
     */
    @Override
    public void printBanner(Environment environment, Class<?> sourceClass, PrintStream out) {

        String version = Mork.class.getPackage().getImplementationVersion();
        String banner = """
                ▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄
                █▄─▀█▀─▄█─▄▄─█▄─▄▄▀█▄─█─▄█
                ██─█▄█─██─██─██─▄─▄██─▄▀██
                █▄▄▄█▄▄▄█▄▄▄▄█▄▄█▄▄█▄▄█▄▄█
                   Version: %s
                   """
                .indent(1)
                .formatted(version);

        out.println(banner);
    }
}
