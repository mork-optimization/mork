package es.urjc.etsii.grafo.util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
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
        byte[] bytes = s.getBytes(charset);
        return b64encode(bytes, charset);
    }

    public static String b64encode(byte[] bytes, Charset charset){
        var encoder = Base64.getEncoder();
        var result = encoder.encode(bytes);
        return new String(result, charset);
    }

    public static String generateSecret(int size) {
        SecureRandom sr = new SecureRandom();
        if(size<=0) {
            throw new IllegalArgumentException("Secret size must be greater than 0");
        }

        byte[] arr = new byte[size];
        sr.nextBytes(arr);
        return b64encode(arr, Charset.defaultCharset());
    }

    public static String generateSecret() {
        return generateSecret(16);
    }
}
