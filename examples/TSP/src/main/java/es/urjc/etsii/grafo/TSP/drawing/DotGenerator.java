package es.urjc.etsii.grafo.TSP.drawing;

import es.urjc.etsii.grafo.TSP.model.TSPSolution;
import es.urjc.etsii.grafo.solver.services.events.MemoryEventStorage;
import es.urjc.etsii.grafo.solver.services.events.types.SolutionGeneratedEvent;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

@RestController
public class DotGenerator {

    /**
     * Common features for all files.
     * Layout is set as "neato", other layout as "dot", "fdp", "sfdp" "twopi" or "circo" can be selected.
     * More information: https://www.graphviz.org
     */
    private static final String header = """
            digraph G {
              layout="neato"
              node[style=filled fillcolor="white", fixedsize=true, shape=circle]""";

    /**
     * Common feature for all files.
     */
    private static final String footer = "\n}";


    /**
     * Generate a dot diagram of the solution found
     * @param solution solution found
     * @return
     */
    protected static String generateDotDiagram(TSPSolution solution) {
        return String.join("\n", header, generateDotLocation(solution), generateDotRoute(solution), footer);
    }

    /**
     * Generate string of edges in DOT language that represent the route found, i.e., a solution of the TSP
     *
     * @param solution candidate graph to be embedded
     * @return string of the edges to be added to the dot file
     */
    private static String generateDotRoute(TSPSolution solution) {
        var instance = solution.getInstance();
        StringBuilder edges = new StringBuilder();
        for (int i = 0; i < instance.numberOfLocations(); i++) {
            edges.append(solution.getLocation(i))
                    .append("->")
                    .append(solution.getLocation((i + 1) % instance.numberOfLocations()))
                    .append("\n");
        }
        return edges.toString();
    }


    /**
     * Generate a string of the coordinates of the locations in graphviz format
     *
     * @param solution solution to generate dot
     * @return a string in dot format of the locations
     */
    private static String generateDotLocation(TSPSolution solution) {
        StringBuilder locations = new StringBuilder();
        var instance = solution.getInstance();
        for (int i = 0; i < instance.numberOfLocations(); i++) {
            locations.append(i)
                    .append("[pos=\"")
                    .append(10 * instance.getCoordinate(i).x() / instance.numberOfLocations())
                    .append(",")
                    .append(10 * instance.getCoordinate(i).y() / instance.numberOfLocations())
                    .append("!\", shape = \"circle\"];\n");
        }
        return locations.toString();
    }

    @Autowired
    private MemoryEventStorage eventStorage;

    @GetMapping("/api/generategraph/{eventId}")
    public String getSolutionAsDotString(@PathVariable int eventId) throws IOException {
        var event = (SolutionGeneratedEvent) eventStorage.getEvents(eventId, eventId + 1).get(0);
        var solution = (TSPSolution) event.getSolution().get();
        var viz = Graphviz.fromString(generateDotDiagram(solution));
        BufferedImage image = viz.render(Format.PNG).toImage();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        byte[] bytes = baos.toByteArray();
        return new String(Base64.getEncoder().encode(bytes));
    }

}