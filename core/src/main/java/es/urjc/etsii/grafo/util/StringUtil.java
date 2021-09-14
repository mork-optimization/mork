package es.urjc.etsii.grafo.util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class StringUtil {
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    public static String b64decode(String s){
        return b64decode(s, DEFAULT_CHARSET);
    }

    public static String b64decode(String s, Charset charset){
        var decoder = Base64.getDecoder();
        byte[] encodedBytes = s.getBytes(charset);
        var result = decoder.decode(encodedBytes);
        return new String(result, charset);
    }

    public static String b64encode(String s){
        return b64encode(s, DEFAULT_CHARSET);
    }

    public static String b64encode(String s, Charset charset){
        var encoder = Base64.getEncoder();
        byte[] bytes = s.getBytes(charset);
        var result = encoder.encode(bytes);
        return new String(result, charset);
    }
}
