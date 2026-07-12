package es.urjc.etsii.grafo.events;

import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Component scan filter that discovers classes declaring Mork event listeners.
 */
public class MorkEventListenerTypeFilter implements TypeFilter {

    @Override
    public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
        return hasMorkEventListenerMethod(metadataReader, metadataReaderFactory, new HashSet<>());
    }

    private boolean hasMorkEventListenerMethod(
            MetadataReader metadataReader,
            MetadataReaderFactory metadataReaderFactory,
            Set<String> visitedClasses
    ) throws IOException {
        String className = metadataReader.getClassMetadata().getClassName();
        if (!visitedClasses.add(className)) {
            return false;
        }
        return metadataReader.getAnnotationMetadata().hasAnnotatedMethods(MorkEventListener.class.getName())
                || hasListenerMethodInSuperclass(metadataReader, metadataReaderFactory, visitedClasses)
                || hasListenerMethodInInterfaces(metadataReader, metadataReaderFactory, visitedClasses);
    }

    private boolean hasListenerMethodInSuperclass(
            MetadataReader metadataReader,
            MetadataReaderFactory metadataReaderFactory,
            Set<String> visitedClasses
    ) throws IOException {
        String superClassName = metadataReader.getClassMetadata().getSuperClassName();
        if (superClassName == null || Object.class.getName().equals(superClassName)) {
            return false;
        }
        return hasMorkEventListenerMethod(
                metadataReaderFactory.getMetadataReader(superClassName),
                metadataReaderFactory,
                visitedClasses
        );
    }

    private boolean hasListenerMethodInInterfaces(
            MetadataReader metadataReader,
            MetadataReaderFactory metadataReaderFactory,
            Set<String> visitedClasses
    ) throws IOException {
        for (String interfaceName : metadataReader.getClassMetadata().getInterfaceNames()) {
            if (hasMorkEventListenerMethod(
                    metadataReaderFactory.getMetadataReader(interfaceName),
                    metadataReaderFactory,
                    visitedClasses
            )) {
                return true;
            }
        }
        return false;
    }
}
