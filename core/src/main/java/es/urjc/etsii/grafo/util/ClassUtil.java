package es.urjc.etsii.grafo.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

public class ClassUtil {

    // Based on the work by: https://gist.github.com/jrichardsz/a34480c1bcc31c45da730c48c4f41331
    private static final Logger log = LoggerFactory.getLogger(ClassUtil.class);

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

    public static boolean isObjectClass(Class<?> clazz) {
        return clazz.getCanonicalName().equals(Object.class.getCanonicalName());
    }


}
