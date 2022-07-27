package es.urjc.etsii.grafo.solver.services;

import es.urjc.etsii.grafo.solver.Mork;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ClassUtilTest {

    @Test
    public void objectClassTest(){
        Assertions.assertTrue(ClassUtil.isObjectClass(Object.class));
        Assertions.assertFalse(ClassUtil.isObjectClass(List.class));
        Assertions.assertFalse(ClassUtil.isObjectClass(Mork.class));
    }
}
