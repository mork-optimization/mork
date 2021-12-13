package es.urjc.etsii.grafo.util;

/**
 * Print messages only when assertions are enabled
 */
public class Debug {

    /**
     * Print debugging message
     *
     * @param s String to print
     */
    public static void debug(String s){
        assert _print(s);
    }

    /**
     * Print debugging message
     *
     * @param s String to print, can contain formatters
     * @param o Objects to format
     */
    public static void debug(String s, Object... o){
        assert _print(s, o);
    }

    private static boolean _print(String s, Object... o){
        System.out.println(String.format(s, o));
        return true;
    }
}
