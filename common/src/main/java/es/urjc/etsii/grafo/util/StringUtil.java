package es.urjc.etsii.grafo.util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;

/**
 * String Utils
 */
public class StringUtil {
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    /**
     * Base 64 decode a string. Uses UTF-8
     *
     * @param s String to decode
     * @return decoded string
     */
    public static String b64decode(String s) {
        return b64decode(s, DEFAULT_CHARSET);
    }

    /**
     * Base 64 decode a string
     *
     * @param s       String to decode
     * @param charset Charset to use
     * @return decoded string
     */
    public static String b64decode(String s, Charset charset) {
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
    public static String b64encode(String s) {
        return b64encode(s, DEFAULT_CHARSET);
    }

    /**
     * Base 64 encode a string
     *
     * @param s       String to encode
     * @param charset Charset to use
     * @return encoded string
     */
    public static String b64encode(String s, Charset charset) {
        byte[] bytes = s.getBytes(charset);
        return b64encode(bytes);
    }

    /**
     * Base 64 encode a byte array
     *
     * @param bytes bytes to encode
     * @return encoded string
     */
    public static String b64encode(byte[] bytes) {
        var encoder = Base64.getEncoder();
        var result = encoder.encode(bytes);
        return new String(result, DEFAULT_CHARSET);
    }

    /**
     * Generate secret using a SecureRandom
     *
     * @param size size in bytes.
     * @return Base64 encoded secret.
     */
    public static String generateSecret(int size) {
        SecureRandom sr = new SecureRandom();
        if (size <= 0) {
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

    public static String randomAlgorithmName() {
        return generateSecret(6)
                .replace("+", "-")
                .replace("/", "_");
    }

    /**
     * Calculate the minimum number of operations required to transform one string into another.
     * In Levenshtein distance, valid operations are insert, remove and swap/replace.
     * Adapted from: <a href="https://en.wikibooks.org/wiki/Algorithm_Implementation/Strings/Levenshtein_distance#Java">Wikibooks</a>
     *
     * @param lhs first string
     * @param rhs second string
     * @return minimum number of operations required to transform the first string into the second.
     */
    public static int levenshtein(CharSequence lhs, CharSequence rhs) {
        Objects.requireNonNull(lhs, "First string is null");
        Objects.requireNonNull(rhs, "Second string is null");

        int len0 = lhs.length() + 1;
        int len1 = rhs.length() + 1;

        // the array of distances
        int[] cost = new int[len0];
        int[] newcost = new int[len0];

        // initial cost of skipping prefix in String s0
        for (int i = 0; i < len0; i++) cost[i] = i;

        // dynamically computing the array of distances

        // transformation cost for each letter in s1
        for (int j = 1; j < len1; j++) {
            // initial cost of skipping prefix in String s1
            newcost[0] = j;

            // transformation cost for each letter in s0
            for (int i = 1; i < len0; i++) {
                // matching current letters in both strings
                int match = (lhs.charAt(i - 1) == rhs.charAt(j - 1)) ? 0 : 1;

                // computing cost for each transformation
                int costReplace = cost[i - 1] + match;
                int costInsert = cost[i] + 1;
                int costDelete = newcost[i - 1] + 1;

                // keep minimum cost
                newcost[i] = Math.min(Math.min(costInsert, costDelete), costReplace);
            }

            // swap cost/newcost arrays
            int[] swap = cost;
            cost = newcost;
            newcost = swap;
        }

        // the distance is the cost for transforming all letters in both strings
        return cost[len0 - 1];
    }

    /**
     * Calculate the minimum number of operations required to transform one array into another.
     * In Levenshtein distance, valid operations are insert, remove and swap/replace.
     * Adapted from: <a href="https://en.wikibooks.org/wiki/Algorithm_Implementation/Strings/Levenshtein_distance#Java">Wikibooks</a>
     *
     * @param lhs first string
     * @param rhs second string
     * @return minimum number of operations required to transform the first string into the second.
     */
    public static int levenshtein(int[] lhs, int[] rhs) {
        return levenshtein(lhs, lhs.length, rhs, rhs.length);
    }

    /**
     * Calculate the minimum number of operations required to transform one array into another.
     * In Levenshtein distance, valid operations are insert, remove and swap/replace.
     * Adapted from: <a href="https://en.wikibooks.org/wiki/Algorithm_Implementation/Strings/Levenshtein_distance#Java">Wikibooks</a>
     *
     * @param lhs first string
     * @param rhs second string
     * @return minimum number of operations required to transform the first string into the second.
     */
    public static int levenshtein(int[] lhs, int limitlhs, int[] rhs, int limitrhs) {
        Objects.requireNonNull(lhs, "First string is null");
        Objects.requireNonNull(rhs, "Second string is null");
        if (limitlhs < 0 || limitlhs > lhs.length) {
            throw new IllegalArgumentException("limitlhs index %s out of range [0, %s]".formatted(limitlhs, lhs.length));
        }

        if (limitrhs < 0 || limitrhs > rhs.length) {
            throw new IllegalArgumentException("limitrhs index %s out of range [0, %s]".formatted(limitrhs, rhs.length));
        }

        int len0 = lhs.length + 1;
        int len1 = rhs.length + 1;

        // the array of distances
        int[] cost = new int[len0];
        int[] newcost = new int[len0];

        // initial cost of skipping prefix in String s0
        for (int i = 0; i < len0; i++) cost[i] = i;

        // dynamically computing the array of distances

        // transformation cost for each letter in s1
        for (int j = 1; j < len1; j++) {
            // initial cost of skipping prefix in String s1
            newcost[0] = j;

            // transformation cost for each letter in s0
            for (int i = 1; i < len0; i++) {
                // matching current letters in both strings
                int match = (lhs[i - 1] == rhs[j - 1]) ? 0 : 1;

                // computing cost for each transformation
                int costReplace = cost[i - 1] + match;
                int costInsert = cost[i] + 1;
                int costDelete = newcost[i - 1] + 1;

                // keep minimum cost
                newcost[i] = Math.min(Math.min(costInsert, costDelete), costReplace);
            }

            // swap cost/newcost arrays
            int[] swap = cost;
            cost = newcost;
            newcost = swap;
        }

        // the distance is the cost for transforming all letters in both strings
        return cost[len0 - 1];
    }

    /**
     * Find the longest common prefix given a list of strings.
     * WARNING: The method will sort the array given as an argument, modifying it.
     * Clone it before calling this method if this behaviour is not desired.
     *
     * @param data Array of strings used to calculate common prefix
     * @return Longest common prefix for all strings given as argument
     */
    public static String longestCommonPrefix(String[] data) {
        if (data.length <= 1) {
            return "";
        }
        String[][] tokens = new String[data.length][];
        for (int i = 0; i < data.length; i++) {
            tokens[i] = data[i].split("[\\W_]");
        }
        int commonTokens = 0;
        var first = tokens[0];
        out:
        for (int i = 0; i < first.length-1; i++) {
            for (int j = 1; j < tokens.length; j++) {
                if (tokens[j].length <= i || !tokens[j][i].equals(first[i])) {
                    break out;
                }
            }
            commonTokens++;
        }
        if(commonTokens == 0){
            return "";
        }
        int prefixLength = 0;
        for (int i = 0; i < commonTokens; i++) {
            prefixLength += tokens[0][i].length() + 1;
        }

        return data[0].substring(0, prefixLength);
    }

    /**
     * Find the longest common suffix given a list of strings.
     *
     * @param data Array of strings used to calculate common prefix
     * @return Longest common prefix for all strings given as argument
     */
    public static String longestCommonSuffix(String[] data) {
        String[] reversed = new String[data.length];
        for (int i = 0; i < data.length; i++) {
            reversed[i] = reverse(data[i]);
        }
        String prefix = longestCommonPrefix(reversed);
        return reverse(prefix);
    }

    /**
     * Reverse string, ie, "abc" becomes "cba"
     *
     * @param s string to reverse
     * @return reversed string
     */
    public static String reverse(String s) {
        char[] chars = new char[s.length()];
        for (int i = s.length() - 1, j = 0; i >= 0; i--, j++) {
            chars[j] = s.charAt(i);
        }
        return new String(chars);
    }

}
