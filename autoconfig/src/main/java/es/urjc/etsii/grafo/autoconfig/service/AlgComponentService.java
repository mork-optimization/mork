package es.urjc.etsii.grafo.autoconfig.service;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.annotations.AlgorithmComponent;
import es.urjc.etsii.grafo.autoconfig.AlgorithmBuilderListener;
import es.urjc.etsii.grafo.autoconfig.BailErrorStrategy;
import es.urjc.etsii.grafo.autoconfig.antlr.AlgorithmLexer;
import es.urjc.etsii.grafo.autoconfig.antlr.AlgorithmParser;
import es.urjc.etsii.grafo.solver.services.ClassUtil;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * Manage algorithm components at runtime. When starting the application, discovers all available components.
 * Can build any configured component easily from string,
 * as long as there is a matching constructor in the algorithm component.
 */
@Service
public class AlgComponentService {
    private static final Logger log = LoggerFactory.getLogger(AlgComponentService.class);

    Map<Class<?>, Collection<Class<?>>> componentsByType = new HashMap<>();
    Map<String, Class<?>> componentByName = new HashMap<>();

    @Value("${advanced.scan-pkgs:es.urjc.etsii}")
    String pkgs;

    @PostConstruct
    protected void runComponentDiscovery(){
        runComponentDiscovery(this.pkgs);
    }

    protected void runComponentDiscovery(String pkgs){
        var types = new ArrayList<Class<?>>();
        for(var pkg: pkgs.split(",")){
            types.addAll(ClassUtil.findTypesByAnnotation(pkg, AlgorithmComponent.class));
        }
        for(var type: types){
            if(componentByName.containsKey(type.getSimpleName())){
                throw new IllegalArgumentException("Duplicated component name: " + type.getSimpleName());
            }
            componentByName.put(type.getSimpleName(), type);
            classify(componentsByType, type);
        }
        log.info("Algorithm components found: {}", componentByName.keySet());
        log.debug("Classified algorithm components: {}", componentsByType);
    }

    private static void classify(Map<Class<?>, Collection<Class<?>>> componentsByType, Class<?> initialType) {
        for (Class<?> type = initialType; type != null; type = type.getSuperclass()){
            if(!ClassUtil.isObjectClass(type)){
                componentsByType.computeIfAbsent(type, a -> new HashSet<>()).add(initialType);
            }
        }
    }

    public Class<?> byName(String name){
        if(!this.componentByName.containsKey(name)){
            throw new IllegalArgumentException(String.format("Unknown component: %s, known components: %s", name, this.componentByName.keySet()));
        }
        return this.componentByName.get(name);
    }

    public Algorithm<?,?> buildFromString(String s){
        var parser = getParser(s);
        var listener = new AlgorithmBuilderListener(this);
        var walker = new ParseTreeWalker();
        walker.walk(listener, parser.init());

        var algorithm = listener.getLastPropertyValue();
        if(!(algorithm instanceof Algorithm<?,?>)){
            throw new IllegalArgumentException(String.format("String does not represent an algorithm, built class type: %s", algorithm.getClass().getSimpleName()));
        }
        return (Algorithm<?,?>) algorithm;
    }

    public static AlgorithmParser getParser(String s){
        var lexer = new AlgorithmLexer(CharStreams.fromString(s));
        var tokens = new CommonTokenStream(lexer);
        var parser = new AlgorithmParser(tokens);
        parser.setErrorHandler(new BailErrorStrategy());
        // TODO Lexer error handler
        return parser;
    }
}
