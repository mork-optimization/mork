//package es.urjc.etsii.grafo.solver.irace;
//
//import es.urjc.etsii.grafo.io.Instance;
//import es.urjc.etsii.grafo.solution.Solution;
//import es.urjc.etsii.grafo.solver.algorithms.Algorithm;
//import es.urjc.etsii.grafo.solver.create.builder.ReflectiveSolutionBuilder;
//import es.urjc.etsii.grafo.solver.create.builder.SolutionBuilder;
//import es.urjc.etsii.grafo.solver.executors.Executor;
//import es.urjc.etsii.grafo.solver.executors.SequentialExecutor;
//import es.urjc.etsii.grafo.solver.services.*;
//import es.urjc.etsii.grafo.solver.services.events.EventPublisher;
//import es.urjc.etsii.grafo.solver.services.events.types.*;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.Optional;
//import java.util.logging.Logger;
//import java.util.stream.Collectors;
//
//@Service
//@ConditionalOnExpression(value = "!'${irace.enabled}' and '${irace.worker}'")
//public class IraceWorkerOrquestrator<S extends Solution<S,I>, I extends Instance> extends AbstractOrchestrator {
//
//    private static final Logger log = Logger.getLogger(IraceWorkerOrquestrator.class.toString());
//
//    private final boolean isMaximizing;
//    private final IOManager<S, I> io;
//    private final ExceptionHandler<S, I> exceptionHandler;
//    private final SolutionBuilder<S, I> solutionBuilder;
//    private final IraceConfiguration iraceConfiguration;
//    private final IraceAlgorithmGenerator<S,I> algorithmGenerator;
//
//    public IraceWorkerOrquestrator(
//            @Value("${solver.maximizing}") boolean isMaximizing,
//            IOManager<S,I> io,
//            List<ExceptionHandler<S,I>> exceptionHandlers,
//            List<SolutionBuilder<S,I>> solutionBuilders,
//            Optional<IraceAlgorithmGenerator<S,I>> algorithmGenerator,
//            IraceConfiguration iraceConfiguration
//    ) {
//        this.isMaximizing = isMaximizing;
//        this.io = io;
//        this.exceptionHandler = decideImplementation(exceptionHandlers, DefaultExceptionHandler.class);
//        this.solutionBuilder = decideImplementation(solutionBuilders, ReflectiveSolutionBuilder.class);
//        this.iraceConfiguration = iraceConfiguration;
//        log.info("Using SolutionBuilder implementation: "+this.solutionBuilder.getClass().getSimpleName());
//
//        this.algorithmGenerator = algorithmGenerator.orElseThrow(() -> new RuntimeException("IRACE mode enabled but no implementation of IraceAlgorithmGenerator has been found. Check the Mork docs section about IRACE."));
//        log.info("IRACE mode enabled, using generator: " + this.algorithmGenerator.getClass().getSimpleName());
//    }
//
//    private boolean isJAR(){
//        String className = this.getClass().getName().replace('.', '/');
//        String protocol = this.getClass().getResource("/" + className + ".class").getProtocol();
//        return protocol.equals("jar");
//    }
//
//    @Override
//    public void run(String... args) {
//        log.info("App started in IRACE Worker mode, ready to start solving!");
//        try{
//            var algorithm = this.algorithmGenerator.buildAlgorithm(this.iraceConfiguration);
//            doIraceRun(algorithm);
//        } catch (Exception e){
//            // TODO proper handling
//            e.printStackTrace();
//        }
//    }
//
//    private void doIraceRun(Algorithm<S,I> algorithm) {
//        List<I> instances = io.getInstances("default").collect(Collectors.toList());
//        if(instances.size() != 1){
//            throw new IllegalArgumentException("Instance path (default experiment) does not correspond with a single instance");
//        }
//        var instance = instances.get(0);
//        var solution = solutionBuilder.initializeSolution(instance);
//        long startTime = System.nanoTime();
//        var result = algorithm.algorithm(solution);
//        long endTime = System.nanoTime();
//        double score = result.getScore();
//        if(isMaximizing){
//            score *= -1; // Irace only minimizes
//        }
//        System.out.format("%s %.2g%n", score, (endTime - startTime) / 1e9);
//    }
//}
