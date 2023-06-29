package es.urjc.etsii.grafo.autoconfig.service.filter;

import es.urjc.etsii.grafo.annotations.InheritedComponent;
import es.urjc.etsii.grafo.util.ReflectionUtil;

import java.util.Set;

/**
 * Includes ALL algorithm components by default, ignores components that implement or extend any element in the blacklist.
 */
@InheritedComponent
public abstract class BlacklistFilterStrategy implements InventoryFilterStrategy {

    protected final Set<Class<?>> blacklistedClasses;

    protected BlacklistFilterStrategy() {
        blacklistedClasses = getBlacklist();
    }

    public abstract Set<Class<?>> getBlacklist();

    @Override
    public boolean include(Class<?> clazz) {
        return !ReflectionUtil.hierarchyContainsAny(clazz, blacklistedClasses);
    }
}
