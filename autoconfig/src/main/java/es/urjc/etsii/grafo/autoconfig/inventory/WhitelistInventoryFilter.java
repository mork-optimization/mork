package es.urjc.etsii.grafo.autoconfig.inventory;

import es.urjc.etsii.grafo.annotations.InheritedComponent;
import es.urjc.etsii.grafo.util.ReflectionUtil;

import java.util.Set;

/**
 * Includes ONLY the given classes/interfaces and their implementations, ignores the rest. By default, includes nothing
 */
public abstract class WhitelistInventoryFilter implements IInventoryFilter {

    protected final Set<Class<?>> whitelistedClasses;

    protected WhitelistInventoryFilter() {
        whitelistedClasses = getWhitelist();
    }

    public abstract Set<Class<?>> getWhitelist();

    @Override
    public boolean include(Class<?> clazz) {
        return ReflectionUtil.hierarchyContainsAny(clazz, whitelistedClasses);
    }
}
