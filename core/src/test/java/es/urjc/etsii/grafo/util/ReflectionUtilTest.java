package es.urjc.etsii.grafo.util;

import es.urjc.etsii.grafo.annotations.InheritedComponent;
import es.urjc.etsii.grafo.solver.Mork;
import es.urjc.etsii.grafo.util.test.findtypes.FindMe;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ReflectionUtilTest {

    @Test
    void findByAnnotation(){
        var list = ReflectionUtil.findTypesByAnnotation("es.urjc.etsii.grafo.util.test", InheritedComponent.class);
        assertEquals(1, list.size());
        assertEquals(list.get(0), FindMe.class);
    }

    @Test
    void objectClassTest(){
        assertTrue(ReflectionUtil.isObjectClass(Object.class));
        assertFalse(ReflectionUtil.isObjectClass(List.class));
        assertFalse(ReflectionUtil.isObjectClass(Mork.class));
    }

    @Test
    void hierarchyContainsTest(){
        assertTrue(ReflectionUtil.hierarchyContainsAny(ArrayList.class, Set.of(List.class)));
        assertFalse(ReflectionUtil.hierarchyContainsAny(ArrayList.class, Set.of(Set.class)));
        assertTrue(ReflectionUtil.hierarchyContainsAny(ArrayList.class, Set.of(ArrayList.class)));
        assertFalse(ReflectionUtil.hierarchyContainsAny(List.class, Set.of(ArrayList.class)));
        assertTrue(ReflectionUtil.hierarchyContainsAny(ArrayList.class, Set.of(Set.class, List.class)));
    }
}
