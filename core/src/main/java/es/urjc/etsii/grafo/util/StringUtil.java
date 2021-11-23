package es.urjc.etsii.grafo.util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * String Utils
 *
 */
public class StringUtil {
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    /**
     * Base 64 decode a string. Uses UTF-8
     *
     * @param s String to decode
     * @return decoded string
     */
    public static String b64decode(String s){
        return b64decode(s, DEFAULT_CHARSET);
    }

    /**
     * Base 64 decode a string
     *
     * @param s String to decode
     * @param charset Charset to use
     * @return decoded string
     */
    public static String b64decode(String s, Charset charset){
        var decoder = Base64.getDecoder();
        byte[] encodedBytes = s.getBytes(charset);
        var result = decoder.decode(encodedBytes);
        return new String(result, charset);
    }

    /**
     * Base 64 encode a string. Uses UTF-8
     *
     * @param s String to encode
     * @return encoded string
     */
    public static String b64encode(String s){
        return b64encode(s, DEFAULT_CHARSET);
    }

    /**
     * Base 64 encode a string
     *
     * @param s String to encode
     * @param charset Charset to use
     * @return encoded string
     */
    public static String b64encode(String s, Charset charset){
        byte[] bytes = s.getBytes(charset);
        return b64encode(bytes);
    }

    /**
     * Base 64 encode a byte array
     *
     * @param bytes bytes to encode
     * @return encoded string
     */
    public static String b64encode(byte[] bytes){
        var encoder = Base64.getEncoder();
        var result = encoder.encode(bytes);
        return new String(result);
    }

    /**
     * Generate secret using a SecureRandom
     *
     * @param size size in bytes.
     * @return Base64 encoded secret.
     */
    public static String generateSecret(int size) {
        SecureRandom sr = new SecureRandom();
        if(size<=0) {
            throw new IllegalArgumentException("Secret size must be greater than 0");
        }

        byte[] arr = new byte[size];
        sr.nextBytes(arr);
        return b64encode(arr);
    }

    /**
     * Generate secret using a SecureRandom. Size defaults to 16 bytes.
     *
     * @return Base64 encoded secret.
     */
    public static String generateSecret() {
        return generateSecret(16);
    }
}
