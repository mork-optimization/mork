package es.urjc.etsii.grafo.util;

import es.urjc.etsii.grafo.solver.Mork;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

class ClassUtilTest {

    @Test
    void objectClassTest(){
        Assertions.assertTrue(ClassUtil.isObjectClass(Object.class));
        Assertions.assertFalse(ClassUtil.isObjectClass(List.class));
        Assertions.assertFalse(ClassUtil.isObjectClass(Mork.class));
    }

    @Test
    void hierarchyContainsTest(){
        Assertions.assertTrue(ClassUtil.hierarchyContainsAny(ArrayList.class, Set.of(List.class)));
        Assertions.assertFalse(ClassUtil.hierarchyContainsAny(ArrayList.class, Set.of(Set.class)));
        Assertions.assertTrue(ClassUtil.hierarchyContainsAny(ArrayList.class, Set.of(ArrayList.class)));
        Assertions.assertFalse(ClassUtil.hierarchyContainsAny(List.class, Set.of(ArrayList.class)));
        Assertions.assertTrue(ClassUtil.hierarchyContainsAny(ArrayList.class, Set.of(Set.class, List.class)));
    }
}
