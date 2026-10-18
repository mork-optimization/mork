package es.urjc.etsii.grafo.tsptw.experiments;

import es.urjc.etsii.grafo.annotations.InheritedComponent;
import es.urjc.etsii.grafo.experiment.AbstractExperiment;
import es.urjc.etsii.grafo.experiment.reference.ReferenceResultManager;
import es.urjc.etsii.grafo.experiment.reference.ReferenceResultProvider;
import es.urjc.etsii.grafo.tsptw.Main;
import es.urjc.etsii.grafo.tsptw.improve.TSPTWInsertionSearch;
import es.urjc.etsii.grafo.tsptw.model.TSPTWInstance;
import es.urjc.etsii.grafo.tsptw.model.TSPTWInstanceImporter;
import es.urjc.etsii.grafo.util.Context;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

import static es.urjc.etsii.grafo.tsptw.model.TSPTWTestUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class TSPTWReferenceResultsTest {

    private static final String TARGET = "GendreauDumasExtended.zip!n80w200.001.txt";
    private TSPTWReferenceResults references;

    @BeforeEach
    void setUp() throws Exception {
        initialize();
        references = new TSPTWReferenceResults();
        new ReferenceResultManager(List.of(references)).init();
    }

    @AfterEach
    void tearDown() {
        cleanup();
    }

    @ParameterizedTest
    @CsvSource({"Dumas,135,135", "GendreauDumasExtended,130,113", "OhlmannThomas,25,16"})
    void publishedToursMatchReferencesAndPassMorkValidation(String dataset, int expectedTours, int expectedOptimal) throws Exception {
        var importer = new TSPTWInstanceImporter();
        int tours = 0;
        int optimal = 0;
        var resource = new ClassPathResource("reference-results/" + dataset + "-best-known-traveltime.txt");
        try (var reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                var data = line.split("#", 2)[0].trim();
                if (data.isEmpty()) {
                    continue;
                }
                var parts = data.split("\\s+");
                var entry = dataset.equals("Dumas") ? "Dumas/" + parts[0] : parts[0];
                var instance = importer.importInstance(Path.of("instances", dataset + ".zip") + "!" + entry);
                var reference = references.getValueFor(instance.getId());
                double cost = Double.parseDouble(parts[1]);
                assertEquals(0, Integer.parseInt(parts[2]));
                assertEquals(cost, reference.getScore(Main.OBJECTIVE.getName()).orElseThrow());
                var customers = new int[parts.length - 3];
                for (int i = 0; i < customers.length; i++) {
                    customers[i] = Integer.parseInt(parts[i + 3]);
                }
                var solution = tour(instance, customers);
                assertEquals(cost, solution.cost(), 1e-9);
                assertTrue(Context.validate(solution), instance.getId());
                tours++;
                if (reference.isOptimalValue()) {
                    optimal++;
                }
            }
        }
        assertEquals(expectedTours, tours);
        assertEquals(expectedOptimal, optimal);
    }

    @Test
    void springDiscoversReferencesAndRegistersOptimalValuesForValidation() {
        try (var context = new AnnotationConfigApplicationContext()) {
            var scanner = new ClassPathBeanDefinitionScanner(context, false);
            scanner.addIncludeFilter(new AnnotationTypeFilter(InheritedComponent.class));
            scanner.addExcludeFilter(new AssignableTypeFilter(AbstractExperiment.class));
            scanner.scan(TSPTWReferenceResults.class.getPackageName());
            context.register(ReferenceResultManager.class);
            context.refresh();
            assertEquals(1, context.getBeansOfType(ReferenceResultProvider.class).size());
            var manager = context.getBean(ReferenceResultManager.class);
            assertSame(manager, Context.Configurator.getRefResultManager());
            assertEquals(490, manager.getRefValueForAllObjectives(TARGET, true).get("Cost"));
        }
    }

    @ParameterizedTest
    @ValueSource(doubles = {489, 490, 491, 492})
    void validatesComponentResultsAgainstTheKnownOptimum(double improvedCost) {
        // Deliberately synthetic distances under the known ID exercise a component
        // crossing the reference while keeping its feasibility and caches correct.
        double crossEdge = improvedCost - 365;
        var instance = new TSPTWInstance(TARGET, 4,
                new double[][]{{0, 120, 125, 130}, {120, 0, 130, crossEdge},
                        {125, 130, 0, 120}, {130, crossEdge, 120, 0}},
                new int[4], new int[]{10000, 10000, 10000, 10000});
        var solution = tour(instance, 1, 2, 3);
        assertEquals(500, solution.cost());
        assertTrue(Context.validate(solution));
        new TSPTWInsertionSearch().improve(solution);
        assertEquals(improvedCost, solution.cost());
        if (improvedCost < 490) {
            var error = assertThrows(AssertionError.class, () -> Context.validate(solution));
            assertTrue(error.getMessage().contains("Solution has improved an optimal value: 490.0 (Cost)"));
        } else {
            assertTrue(Context.validate(solution));
        }
    }

    @Test
    void permitsImprovementOverAnUnresolvedBestKnownValue() {
        String id = "GendreauDumasExtended.zip!n80w100.001.txt";
        var reference = references.getValueFor(id);
        assertEquals(565, reference.getScore("Cost").orElseThrow());
        assertFalse(reference.isOptimalValue());
        var instance = new TSPTWInstance(id, 3,
                new double[][]{{0, 180, 180}, {180, 0, 204}, {180, 204, 0}},
                new int[3], new int[]{10000, 10000, 10000});
        var solution = tour(instance, 1, 2);
        assertEquals(564, solution.cost());
        assertTrue(Context.validate(solution));
    }

    @Test
    void unrelatedIdsDoNotInheritOptimalReferences() {
        for (var id : List.of("n80w200.001.txt", "DaSilvaUrrutia.zip!n80w200.001.txt", "unknown")) {
            var reference = references.getValueFor(id);
            assertTrue(reference.getScores().isEmpty(), id);
            assertFalse(reference.isOptimalValue(), id);
        }
    }
}
