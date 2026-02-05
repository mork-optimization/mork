package es.urjc.etsii.grafo.autoconfig.generator;

import es.urjc.etsii.grafo.autoconfig.inventory.BlacklistInventoryFilter;
import es.urjc.etsii.grafo.autoconfig.inventory.DefaultInventoryFilter;
import es.urjc.etsii.grafo.autoconfig.inventory.IInventoryFilter;
import es.urjc.etsii.grafo.autoconfig.inventory.WhitelistInventoryFilter;
import es.urjc.etsii.grafo.util.ReflectionUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class FilterConfig {

    @Value("${advanced.scan-pkgs:es.urjc.etsii}")
    String pkgs;

    @Value("${whitelist:}")
    String whitelistParam;

    @Value("${blacklist:}")
    String blacklistParam;

    @Bean
    public IInventoryFilter getDefaultFilterStrategy(){
        // Do not allow both flags at the same time
        if (!whitelistParam.isBlank() && !blacklistParam.isBlank()){
            throw new IllegalArgumentException("Both --whitelist and --blacklist provided. Please specify only one.");
        }

        if (!whitelistParam.isBlank()){
            return resolveFilter(WhitelistInventoryFilter.class, whitelistParam);
        }
        if (!blacklistParam.isBlank()){
            return resolveFilter(BlacklistInventoryFilter.class, blacklistParam);
        }
        return new DefaultInventoryFilter();
    }

    private <T> IInventoryFilter resolveFilter(Class<T> baseType, String param){
        // If param is a boolean-like flag (e.g., --whitelist), try to auto-pick when a single implementation exists
        if (isTrueFlag(param)){
            var impls = findImplementations(baseType);
            if (impls.isEmpty()){
                throw new IllegalArgumentException("No implementations found for " + baseType.getSimpleName() + ". Please provide a class using --" + baseType.getSimpleName().toLowerCase().replace("inventoryfilter","") + "=<FQN or SimpleName>");
            }
            if (impls.size() > 1){
                throw new IllegalArgumentException("Multiple implementations found for " + baseType.getSimpleName() + ": " + impls + ". Please select one using --" + (baseType == WhitelistInventoryFilter.class ? "whitelist" : "blacklist") + "=<FQN or SimpleName>");
            }
            return instantiate(impls.get(0));
        }

        // param contains a class name, which can be FQN or simple name
        Class<? extends IInventoryFilter> clazz = resolveByName(baseType, param);
        return instantiate(clazz);
    }

    private boolean isTrueFlag(String value){
        return value.equalsIgnoreCase("true") || value.equals("1");
    }

    @SuppressWarnings("unchecked")
    private <T> List<Class<? extends IInventoryFilter>> findImplementations(Class<T> baseType){
        var result = new ArrayList<Class<? extends IInventoryFilter>>();
        for (var pkg : pkgs.split(",")){
            var found = ReflectionUtil.findTypesBySuper(pkg, baseType);
            for (var c : found){
                // Exclude abstract base type itself if present
                if (!c.equals(baseType)){
                    result.add((Class<? extends IInventoryFilter>) c);
                }
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private <T> Class<? extends IInventoryFilter> resolveByName(Class<T> baseType, String name){
        // Try FQN
        try{
            var c = Class.forName(name);
            if (!baseType.isAssignableFrom(c)){
                throw new IllegalArgumentException(name + " does not extend/implement " + baseType.getSimpleName());
            }
            return (Class<? extends IInventoryFilter>) c;
        } catch (ClassNotFoundException ignored){
            // Try simple name by scanning
            var impls = findImplementations(baseType);
            var matches = impls.stream().filter(c -> c.getSimpleName().equals(name)).toList();
            if (matches.isEmpty()){
                throw new IllegalArgumentException("Cannot find class '" + name + "' for " + baseType.getSimpleName() + ". Available: " + impls);
            }
            if (matches.size() > 1){
                throw new IllegalArgumentException("Ambiguous simple name '" + name + "'. Matches: " + matches + ". Please use fully qualified name.");
            }
            return matches.get(0);
        }
    }

    private IInventoryFilter instantiate(Class<? extends IInventoryFilter> clazz){
        try{
            Constructor<? extends IInventoryFilter> ctor = clazz.getDeclaredConstructor();
            ctor.setAccessible(true);
            return ctor.newInstance();
        } catch (Exception e){
            throw new IllegalArgumentException("Failed to instantiate filter '" + clazz.getName() + "'. Ensure it has a public no-arg constructor.", e);
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public IExplorationFilter getDefaultExplorationFilter(){
        return new DefaultExplorationFilter();
    }

}
