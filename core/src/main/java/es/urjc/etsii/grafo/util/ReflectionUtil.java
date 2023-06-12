package es.urjc.etsii.grafo.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ReflectionUtil {

    // Based on the work by: https://gist.github.com/jrichardsz/a34480c1bcc31c45da730c48c4f41331
    private static final Logger log = LoggerFactory.getLogger(ReflectionUtil.class);

    public static List<Class<?>> findTypesByAnnotation(String packageName, Class<? extends Annotation> clazz) {
        var result = new ArrayList<Class<?>>();
        var provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AnnotationTypeFilter(clazz));
        for (var candidateBean : provider.findCandidateComponents(packageName)) {
            try {
                var candidateClass = Class.forName(candidateBean.getBeanClassName());
                result.add(candidateClass);
            } catch (ClassNotFoundException e) {
                log.warn("Could not resolve class object for bean definition", e);
            }
        }
        return result;
    }


    /**
     * Check if the given class is equivalent to Object.class
     * @param clazz class to test
     * @return true if the given class is a reference to the Object class, false otherwise
     */
    public static boolean isObjectClass(Class<?> clazz) {
        return clazz.getCanonicalName().equals(Object.class.getCanonicalName());
    }

    /**
     * Check if the given class or any of the classes it extends, or the interfaces it implements, is contained in the set
     * Example: hierarchyContainsAny(ArrayList.class, Set.of(List.class)) would return true.
     * @param clazz class to test
     * @param set set of reference classes
     * @return return true if the class extends any class in the set, or is directly in the set, false otherwise
     */
    public static boolean hierarchyContainsAny(Class<?> clazz, Set<Class<?>> set){
        for (Class<?> type = clazz; type != null; type = type.getSuperclass()){
            if(set.contains(type)){
                return true;
            }
        }
        // Check interfaces too
        for(var interf: ClassUtils.getAllInterfacesForClass(clazz)){
            if(set.contains(interf)){
                return true;
            }
        }
        return false;
    }
}
