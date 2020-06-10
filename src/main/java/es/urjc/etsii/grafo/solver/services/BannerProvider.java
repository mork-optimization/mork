package es.urjc.etsii.grafo.solver.services;

import org.springframework.boot.Banner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.PrintStream;

@Service
public class BannerProvider implements Banner {
    @Override
    public void printBanner(Environment environment, Class<?> sourceClass, PrintStream out) {

        String banner = "   ____  __  __            _     ____\n" +
                "  / / / |  \\/  |          | |    \\ \\ \\\n" +
                " | | |  | \\  / | ___  _ __| | __  | | |\n" +
                " | | |  | |\\/| |/ _ \\| '__| |/ /  | | |\n" +
                " | | |  | |  | | (_) | |  |   <   | | |\n" +
                " | | |  |_|  |_|\\___/|_|  |_|\\_\\  | | |\n" +
                "  \\_\\_\\ ________________________ /_/_/\n" +
                "                        "+environment.getProperty("application.formatted-version");
        out.println(banner);
    }
}
