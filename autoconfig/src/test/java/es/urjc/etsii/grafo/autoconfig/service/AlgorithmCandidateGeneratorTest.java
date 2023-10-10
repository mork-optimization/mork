package es.urjc.etsii.grafo.autoconfig.service;

import es.urjc.etsii.grafo.autoconfig.generator.AlgorithmCandidateGenerator;
import es.urjc.etsii.grafo.autoconfig.generator.DefaultExplorationFilter;
import es.urjc.etsii.grafo.autoconfig.generator.TreeNode;
import es.urjc.etsii.grafo.autoconfig.inventory.AlgorithmInventoryService;
import es.urjc.etsii.grafo.autoconfig.testutil.ComponentWhitelistDuringTesting;
import es.urjc.etsii.grafo.autoconfig.testutil.TestUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;

class AlgorithmCandidateGeneratorTest {


    static AlgorithmInventoryService inventoryService;
    static AlgorithmCandidateGenerator candidateGenerator;
    static Set<String> forbiddenComponents;

    @BeforeAll
    static void setup(){
        inventoryService = new AlgorithmInventoryService(new ComponentWhitelistDuringTesting(), TestUtil.getTestFactories(), TestUtil.getTestProviders());
        inventoryService.runComponentDiscovery("es.urjc.etsii");
        candidateGenerator = new AlgorithmCandidateGenerator(inventoryService, new DefaultExplorationFilter());
        // Components that should never be considered for autoconfig, either because they are not in the whitelist,
        // or they are in the whitelist but they are not autoconfigurable
        forbiddenComponents = Set.of("MultiStartAlgorithm", "VND", "FakeGRASPConstructive");
    }

    @Test
    void noDepthNoRepeat(){
        doChecks(1, 0);
    }

    @Test
    void depthNoRepeat(){
        doChecks(1_000_000, 0);
    }

    @Test
    void smallDepthRepeat(){
        doChecks(4, 1_000_000);
    }

    @Test
    void bothDepthAndRepeat(){
        doChecks(1_000_000, 4);
    }

    // TODO missing test to validate recursive behaviour

    private void doChecks(int maxDepth, int maxRepeat){
        var roots = candidateGenerator.buildTree(maxDepth, maxRepeat);
        var params = candidateGenerator.toIraceParams(roots);
        printParams(params);
        printTree(roots);
        checkForForbiddenComponents(roots);
        checkMaxDepth(roots, maxDepth);
    }


    private void checkMaxDepth(List<TreeNode> roots, int maxDepth) {
        for(var node: roots){
            checkMaxDepth(node, maxDepth, 0);
        }
    }

    private void checkMaxDepth(TreeNode node, int maxDepth, int currentDepth) {
        Assertions.assertTrue(currentDepth <= maxDepth, "Max depth exceeded: " + currentDepth);
        for(var param: node.children().values()){
            for(var childNode: param){
                checkMaxDepth(childNode, maxDepth, currentDepth + 1);
            }
        }
    }

    private void printParams(List<String> params){
        Collections.sort(params);
        for(var p: params){
            System.out.println(p);
        }
    }

    private void printTree(List<TreeNode> nodes){
        for(var node: nodes){
            var sb = new StringBuilder();
            printTree(node, 0, sb);
            System.out.println(sb);
        }
    }

    private void printTree(TreeNode node, int level, StringBuilder stringBuilder){
        for (int i = 0; i < level; i++) {
            stringBuilder.append("  ");
        }
        stringBuilder
                .append(node.paramName())
                .append("=")
                .append(node.className())
                .append("\n");
        for(var param: node.children().values()){
            for(var childNode: param){
                printTree(childNode, level + 1, stringBuilder);
            }
        }
    }

    private void checkForForbiddenComponents(List<TreeNode> roots){
        for(var node: roots){
            checkForForbiddenComponents(node);
        }
    }

    private void checkForForbiddenComponents(TreeNode node){
        assertFalse(forbiddenComponents.contains(node.className()), "Forbidden component found: " + node.className());
        for(var param: node.children().values()){
            for(var childNode: param){
                checkForForbiddenComponents(childNode);
            }
        }
    }
}