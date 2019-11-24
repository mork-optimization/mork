package util;

public class Debug {

    public static boolean debug(String s){
        System.out.println(s);
        return true;
    }

    public static boolean debug(String s, Object... o){
        System.out.println(String.format(s, o));
        return true;
    }
}
