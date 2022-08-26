package es.urjc.etsii.grafo.autoconfig.service;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.annotations.AlgorithmComponent;
import es.urjc.etsii.grafo.autoconfig.AlgorithmBuilderListener;
import es.urjc.etsii.grafo.autoconfig.AlgorithmBuilderUtil;
import es.urjc.etsii.grafo.autoconfig.BailErrorStrategy;
import es.urjc.etsii.grafo.autoconfig.antlr.AlgorithmLexer;
import es.urjc.etsii.grafo.autoconfig.antlr.AlgorithmParser;
import es.urjc.etsii.grafo.autoconfig.exception.AlgorithmParsingException;
import es.urjc.etsii.grafo.autoconfig.service.factories.AlgorithmComponentFactory;
import es.urjc.etsii.grafo.autoconfig.service.factories.CommonComponentFactory;
import es.urjc.etsii.grafo.create.Constructive;
import es.urjc.etsii.grafo.create.grasp.GRASPListManager;
import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.shake.Shake;
import es.urjc.etsii.grafo.util.ClassUtil;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.lang.reflect.Modifier;
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
    Map<String, String> aliases = getAliases();
    Map<String, AlgorithmComponentFactory> factories = getFactories();

    private static Map<String, AlgorithmComponentFactory> getFactories() {
        var map = new HashMap<String, AlgorithmComponentFactory>();
        map.put("NullConstructive", params -> Constructive.nul());
        map.put("NullImprover", params -> Improver.nul());
        map.put("NullShake", params -> Shake.nul());
        map.put("NullGraspListManager", params -> GRASPListManager.nul());
        map.put("GraspConstructive", CommonComponentFactory::createGRASP);
        return map;
    }

    private static Map<String, String> getAliases() {
        var map = new HashMap<String, String>();
        map.put("GRASP", "GraspConstructive");
        map.put("NullGRASPListManager", "NullGraspListManager");
        return map;
    }


    @Value("${advanced.scan-pkgs:es.urjc.etsii}")
    String pkgs;

    /**
     * Create an alias for target with name alias. Example registerAlias("ILS", "IteratedLocalSearch")
     * @param alias New name, reference target
     * @param target target name
     */
    public void registerAlias(String alias, String target){
        if(this.aliases.containsKey(target)){
            throw new IllegalArgumentException(String.format("%s is already an alias, call registerAlias(%s, %s) instead of registerAlias(%s, %s)", target, alias, this.aliases.get(target), alias, target));
        }
        if(!this.factories.containsKey(target) && !this.componentByName.containsKey(target)){
            throw new IllegalArgumentException(String.format("Cannot register alias for unknown target: %s --> %s", alias, target));
        }
        throwIfKnown(alias);
        this.aliases.put(alias, target);
    }

    private void throwIfKnown(String name){
        if(this.aliases.containsKey(name)){
            throw new IllegalArgumentException(String.format("Alias already exists: %s --> %s", name, this.aliases.get(name)));
        }
        if(this.factories.containsKey(name)){
            throw new IllegalArgumentException(String.format("Factory already exists with name: %s", name));
        }
        if(this.componentByName.containsKey(name)){
            throw new IllegalArgumentException(String.format("Component already exists with name: %s", name));
        }
    }

    /**
     * Provide a function that given a parameter map can create components with id 'name'
     * @param name name of the component
     * @param factory function to execute such as f(params) --> returns component of type name
     */
    public void registerFactory(String name, AlgorithmComponentFactory factory){
        throwIfKnown(name);
        this.factories.put(name, factory);
    }

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
            if(!isAccesible(type)){
                log.debug("Ignoring class {}", type);
                continue;
            }
            String componentName = type.getSimpleName();
            throwIfKnown(componentName);
            componentByName.put(componentName, type);
            classify(componentsByType, type);
        }
        log.info("Algorithm components found: {}", componentByName.keySet());
        log.debug("Classified algorithm components: {}", componentsByType);
    }

    private static boolean isAccesible(Class<?> clazz){
        return Modifier.isPublic(clazz.getModifiers());
    }

    private static void classify(Map<Class<?>, Collection<Class<?>>> componentsByType, Class<?> initialType) {
        for (Class<?> type = initialType; type != null; type = type.getSuperclass()){
            if(!ClassUtil.isObjectClass(type)){
                componentsByType.computeIfAbsent(type, a -> new HashSet<>()).add(initialType);
            }
        }
    }

    /**
     * Build any algorithm component given its name and a map of parameters
     * @param name component name, can be either an alias, a factory reference or a classname
     * @param params parameter map used to create this component, provides the necessary args to call the constructor
     * @return built component
     */
    public Object buildAlgorithmComponentByName(String name, Map<String, Object> params){
        if(aliases.containsKey(name)){
            // Resolve alias and keep trying to build
            return buildAlgorithmComponentByName(aliases.get(name), params);
        }
        if(this.factories.containsKey(name)){
            // If registered factory method, invoke it
            return this.factories.get(name).create(params);
        }

        // Either we have discovered the component automatically, or fail
        if(!this.componentByName.containsKey(name)){
            // fail
            throw new AlgorithmParsingException(String.format("Unknown component: %s, known components: %s, aliases: %s, factories: %s", name, this.componentByName.keySet(), this.aliases.keySet(), this.factories.keySet()));
        }
        var clazz = this.componentByName.get(name);
        return AlgorithmBuilderUtil.build(clazz, params);
    }

    /**
     * Build any algorithm from the given string description. Must follow the format: Component{param1=value1, ...}.
     * Any component can be a parameter too, example: Alg{constructive=GRASP{alpha=1}, ls=MyLS{}}
     * @param s String description
     * @return Built component
     */
    public Algorithm<?,?> buildAlgorithmFromString(String s){
        var component = buildAlgorithmComponentFromString(s);
        if(!(component instanceof Algorithm<?,?>)){
            throw new AlgorithmParsingException(String.format("String does not represent an algorithm, built class type: %s", component.getClass().getSimpleName()));
        }
        return (Algorithm<?,?>) component;
    }

    /**
     * Build any algorithm from the given string description. Must follow the format: Component{param1=value1, ...}.
     * Any component can be a parameter too, example: Alg{constructive=GRASP{alpha=1}, ls=MyLS{}}
     * @param s String description
     * @return Built component
     */
    public Object buildAlgorithmComponentFromString(String s){
        var parser = getParser(s);
        var listener = new AlgorithmBuilderListener(this);
        var walker = new ParseTreeWalker();
        walker.walk(listener, parser.init());

        var component = listener.getLastPropertyValue();
        return component;
    }

    /**
     * Get an instance of the algorithm parser, initialized as Parser --> TokenStream --> Lexer --> String
     * @param s source string
     * @return parser with BailOutStrategy, ie fails at the first error found.
     */
    public static AlgorithmParser getParser(String s){
        var lexer = new AlgorithmLexer(CharStreams.fromString(s));
        var tokens = new CommonTokenStream(lexer);
        var parser = new AlgorithmParser(tokens);
        parser.setErrorHandler(new BailErrorStrategy());
        // TODO Review Lexer error handler
        return parser;
    }
}
