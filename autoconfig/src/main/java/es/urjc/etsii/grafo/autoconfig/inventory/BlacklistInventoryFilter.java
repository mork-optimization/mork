package es.urjc.etsii.grafo.autoconfig.inventory;

import es.urjc.etsii.grafo.annotations.InheritedComponent;
import es.urjc.etsii.grafo.util.ReflectionUtil;

import java.util.Set;

/**
 * Includes ALL algorithm components by default, ignores components that implement or extend any element in the blacklist.
 */
public abstract class BlacklistInventoryFilter implements IInventoryFilter {

    protected final Set<Class<?>> blacklistedClasses;

    protected BlacklistInventoryFilter() {
        blacklistedClasses = getBlacklist();
    }

    public abstract Set<Class<?>> getBlacklist();

    @Override
    public boolean include(Class<?> clazz) {
        return !ReflectionUtil.hierarchyContainsAny(clazz, blacklistedClasses);
    }
}
