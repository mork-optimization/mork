package es.urjc.etsii.grafo.solver.services;

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

        String banner = "   ____  __  __            _     ____\n" +
                "  / / / |  \\/  |          | |    \\ \\ \\\n" +
                " | | |  | \\  / | ___  _ __| | __  | | |\n" +
                " | | |  | |\\/| |/ _ \\| '__| |/ /  | | |\n" +
                " | | |  | |  | | (_) | |  |   <   | | |\n" +
                " | | |  |_|  |_|\\___/|_|  |_|\\_\\  | | |\n" +
                "  \\_\\_\\ ________________________ /_/_/\n" +
                "                        Version: " + Mork.class.getPackage().getImplementationVersion();
        out.println(banner);
    }
}
