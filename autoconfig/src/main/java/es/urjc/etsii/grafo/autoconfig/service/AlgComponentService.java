package es.urjc.etsii.grafo.autoconfig.service;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.annotations.AlgorithmComponent;
import es.urjc.etsii.grafo.autoconfig.AlgorithmBuilderListener;
import es.urjc.etsii.grafo.autoconfig.AlgorithmBuilderUtil;
import es.urjc.etsii.grafo.autoconfig.AlgorithmParsingException;
import es.urjc.etsii.grafo.autoconfig.BailErrorStrategy;
import es.urjc.etsii.grafo.autoconfig.antlr.AlgorithmLexer;
import es.urjc.etsii.grafo.autoconfig.antlr.AlgorithmParser;
import es.urjc.etsii.grafo.create.Constructive;
import es.urjc.etsii.grafo.create.grasp.GRASPListManager;
import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.shake.Shake;
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
import java.util.function.Function;

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
    Map<String, String> aliases = getAliases();
    Map<String, Function<Map<String, Object>, Object>> factories = getFactories();

    private static Map<String, Function<Map<String, Object>, Object>> getFactories() {
        var map = new HashMap<String, Function<Map<String, Object>, Object>>();
        map.put("NullConstructive", params -> Constructive.nul());
        map.put("NullImprover", params -> Improver.nul());
        map.put("NullShake", params -> Shake.nul());
        map.put("NullGraspListManager", params -> GRASPListManager.nul());
        return map;
    }

    private static Map<String, String> getAliases() {
        var map = new HashMap<String, String>();
        map.put("GRASP", "GreedyRandomGRASPConstructive");
        map.put("NullGRASPListManager", "NullGraspListManager");
        return map;
    }


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

    public Object buildAlgorithmComponentByName(String name, Map<String, Object> params){
        if(aliases.containsKey(name)){
            // Resolve alias and keep trying to build
            return buildAlgorithmComponentByName(aliases.get(name), params);
        }
        if(this.factories.containsKey(name)){
            // If registered factory method, invoke it
            return this.factories.get(name).apply(params);
        }

        // Either we have discovered the component automatically, or fail
        if(!this.componentByName.containsKey(name)){
            // fail
            throw new AlgorithmParsingException(String.format("Unknown component: %s, known components: %s, aliases: %s, factories: %s", name, this.componentByName.keySet(), this.aliases.keySet(), this.factories.keySet()));
        }
        var clazz = this.componentByName.get(name);
        return AlgorithmBuilderUtil.build(clazz, params);
    }

    public Algorithm<?,?> buildFromString(String s){
        var parser = getParser(s);
        var listener = new AlgorithmBuilderListener(this);
        var walker = new ParseTreeWalker();
        walker.walk(listener, parser.init());

        var algorithm = listener.getLastPropertyValue();
        if(!(algorithm instanceof Algorithm<?,?>)){
            throw new AlgorithmParsingException(String.format("String does not represent an algorithm, built class type: %s", algorithm.getClass().getSimpleName()));
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
