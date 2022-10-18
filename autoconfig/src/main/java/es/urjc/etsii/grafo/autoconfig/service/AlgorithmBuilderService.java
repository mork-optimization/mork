package es.urjc.etsii.grafo.autoconfig.service;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.autoconfig.AlgorithmBuilderListener;
import es.urjc.etsii.grafo.autoconfig.AlgorithmBuilderUtil;
import es.urjc.etsii.grafo.autoconfig.BailErrorStrategy;
import es.urjc.etsii.grafo.autoconfig.antlr.AlgorithmLexer;
import es.urjc.etsii.grafo.autoconfig.antlr.AlgorithmParser;
import es.urjc.etsii.grafo.autoconfig.exception.AlgorithmParsingException;
import es.urjc.etsii.grafo.autoconfig.irace.params.ParameterType;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AlgorithmBuilderService {

    private final AlgorithmInventoryService inventoryService;

    public AlgorithmBuilderService(AlgorithmInventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    /**
     * Build any algorithm component given its name and a map of parameters
     * @param name component name, can be either an alias, a factory reference or a classname
     * @param params parameter map used to create this component, provides the necessary args to call the constructor
     * @return built component
     */
    public Object buildAlgorithmComponentByName(String name, Map<String, Object> params){
        var inventory = this.inventoryService.getInventory();
        if(inventory.aliases().containsKey(name)){
            // Resolve alias and keep trying to build
            return buildAlgorithmComponentByName(inventory.aliases().get(name), params);
        }
        if(inventory.factories().containsKey(name)){
            // If registered factory method, invoke it adding our provided values
            var factory = inventory.factories().get(name);
            var factoryParams = new HashMap<>(params);
            for(var p: factory.getRequiredParameters()){
                if(p.getType() == ParameterType.PROVIDED){
                    factoryParams.put(p.getName(), AlgorithmBuilderUtil.getProvidedValue(p));
                }
            }
            return factory.buildComponent(factoryParams);
        }

        // Either we have discovered the component automatically, or fail
        if(!inventory.componentByName().containsKey(name)){
            // fail
            throw new AlgorithmParsingException(String.format("Unknown component: %s, known components: %s, aliases: %s, factories: %s", name, inventory.componentByName().keySet(), inventory.aliases().keySet(), inventory.factories().keySet()));
        }
        var clazz = inventory.componentByName().get(name);
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
