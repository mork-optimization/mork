package es.urjc.etsii.grafo.util;

/**
 * Assertion utils
 * http://www.cs.um.edu.mt/gordon.pace/Teaching/DiscreteMaths/Laws.pdf
 */
public class AssertionUtil {

    /**
     * Does the supposition "if and only if" hold?
     * {@code P <--> Q = (P & Q) | (!P & !Q)}
     *
     * @param p P
     * @param q Q
     * @return true if valid supposition, false otherwise
     */
    public static boolean biimplication(boolean p, boolean q){
        return (p & q) || (!p && !q);
    }
    /**
     * Does the supposition "if then" hold?
     * {@code P --> Q = !P || Q}
     *
     * @param p P
     * @param q Q
     * @return true if valid supposition, false otherwise
     */
    public static boolean implication(boolean p, boolean q){
        return !p || q;
    }

}
