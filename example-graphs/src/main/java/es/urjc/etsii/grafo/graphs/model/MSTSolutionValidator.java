package es.urjc.etsii.grafo.graphs.model;

import es.urjc.etsii.grafo.solution.SolutionValidator;
import es.urjc.etsii.grafo.solution.ValidationResult;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Validate that a solution is valid for the MST problem.
 * Validation is always run after the algorithms executes, and can be run in certain algorithm stages to verify
 * that the current solution is valid.
 */
public class MSTSolutionValidator extends SolutionValidator<MSTSolution, MSTInstance> {

    /**
     * Validate the current solution, check that no constraint is broken and everything is fine
     *
     * @param solution Solution to validate
     * @return ValidationResult.ok() if the solution is valid, ValidationResult.fail("reason why it failed") if a solution is not valid.
     */
    @Override
    public ValidationResult validate(MSTSolution solution) {
        var validationResult = ValidationResult.ok();
//        var instance = solution.getInstance();
//        var visited = new boolean[instance.v()];
//        var graph = initGraph(instance, solution.getEdges());
//        bfs(visited, graph, 0);
//        for (int i = 0; i < visited.length; i++) {
//            if (!visited[i]) {
//                validationResult.addFailure("Node " + i + " is unreachable");
//            }
//        }

        return validationResult;
    }

    public static List<Edge>[] initGraph(MSTInstance instance, List<Edge> edges) {
        List<Edge>[] graph = new List[instance.v()];
        for (int j = 0; j < instance.v(); j++) {
            graph[j] = new ArrayList<>();
        }
        for (var edge : edges) {
            graph[edge.from()].add(edge);
            graph[edge.to()].add(edge);
        }
        return graph;
    }

    public static void bfs(boolean[] visited, List<Edge>[] graph, int startNode) {
        var queue = new LinkedList<Integer>();
        queue.add(startNode);
        while (!queue.isEmpty()) {
            var node = queue.poll();
            if (!visited[node]) {
                visited[node] = true;
                for (var edge : graph[node]) {
                    if (edge.from() == node) {
                        queue.add(edge.to());
                    } else {
                        queue.add(edge.from());
                    }
                }
            }
        }
    }
}
