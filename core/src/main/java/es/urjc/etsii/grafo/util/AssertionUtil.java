package es.urjc.etsii.grafo.util;

/**
 * Assertion utils
 * http://www.cs.um.edu.mt/gordon.pace/Teaching/DiscreteMaths/Laws.pdf
 */
public class AssertionUtil {

    // P <--> Q : (P & Q) | (!P & !Q)
    public static boolean biimplication(boolean p, boolean q){
        return (p & q) || (!p && !q);
    }

    public static boolean implication(boolean p, boolean q){
        return !p || q;
    }

}
