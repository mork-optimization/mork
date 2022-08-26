package es.urjc.etsii.grafo.services;

import es.urjc.etsii.grafo.create.builder.SolutionBuilder;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.testutil.TestSolution;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>ReflectiveSolutionBuilder class.</p>
 */
@SuppressWarnings("unchecked")
public class ReflectiveSolutionBuilder<S extends Solution<S, I>, I extends Instance> extends SolutionBuilder<S, I> {

    private final Class<S> solClass;
    private Constructor<S> constructor;

    /**
     * <p>Constructor for ReflectiveSolutionBuilder.</p>
     */
    public ReflectiveSolutionBuilder() {
        Set<Class<S>> set = findSolutionCandidates(getPkgsToScan());
        if (set.isEmpty()) {
            throw new RuntimeException("Cannot find any class extending Solution<S,I>");
        }
        if (set.size() > 1) {
            throw new RuntimeException(String.format("Found multiple classes extending Solution<S,I> (%s), provide only one or extend the class SolutionBuilder and implement the required method", set));
        }

        solClass = set.iterator().next();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public S initializeSolution(I instance) {
        if (constructor == null) {
            initializeConstructorReference(instance);
        }
        try {
            var solution = constructor.newInstance(instance);
            return solution;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get correct constructor reference and cache it.
     * This method can be called by several threads at the same time
     *
     * @param i Instance class, get constructor by correct type
     */
    private synchronized void initializeConstructorReference(I i) {
        if (constructor != null) return;

        try {
            constructor = this.solClass.getConstructor(i.getClass());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Could not found Solution constructor Solution(Instance)");
        }
    }

    private String[] getPkgsToScan() {
        var pkgs = System.getProperty("advanced.scan-pkgs", "es.urjc.etsii");
        return pkgs.split(",");
    }

    private Set<Class<S>> findSolutionCandidates(String[] pkgs) {
        var provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AssignableTypeFilter(Solution.class));

        Set<BeanDefinition> allComponentes = new HashSet<>();
        for (var pkg : pkgs) {
            allComponentes.addAll(provider.findCandidateComponents(pkg.replace(".", "/")));
        }
        Set<Class<S>> solutionImplementations = new HashSet<>();

        for (BeanDefinition component : allComponentes) {
            try {
                Class<S> cls = (Class<S>) Class.forName(component.getBeanClassName());
                if (!TestSolution.class.isAssignableFrom(cls)) {
                    // Skip test implementations
                    solutionImplementations.add(cls);
                }
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return solutionImplementations;
    }
}
