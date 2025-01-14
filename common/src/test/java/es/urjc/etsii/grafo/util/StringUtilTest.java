package es.urjc.etsii.grafo.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StringUtilTest {

    @Test
    void testLevenshteinStr() {
        assertEquals(0, StringUtil.levenshtein("abcdef", "abcdef"));
        assertEquals(0, StringUtil.levenshtein("", ""));

        assertEquals(6, StringUtil.levenshtein("", "abcdef"));
        assertEquals(6, StringUtil.levenshtein("abcdef", ""));

        assertEquals(2, StringUtil.levenshtein("abef", "abcdef"));
        assertEquals(2, StringUtil.levenshtein("abcdef", "abef"));

        assertEquals(1, StringUtil.levenshtein("abcdZf", "abcdef"));
        assertEquals(1, StringUtil.levenshtein("abcdef", "abcdeZ"));
        assertEquals(1, StringUtil.levenshtein("abcdef", "Zbcdef"));

        assertEquals(1, StringUtil.levenshtein("abcdef", "zabcdef"));
        assertEquals(1, StringUtil.levenshtein("zabcdef", "abcdef"));

        assertThrows(NullPointerException.class, () -> StringUtil.levenshtein("abcdef", null));
        assertThrows(NullPointerException.class, () -> StringUtil.levenshtein(null, "abcdef"));
    }

    @Test
    void testLevenshteinInts() {
        assertEquals(0, StringUtil.levenshtein(new int[]{0,1,2,3,4,5}, new int[]{0,1,2,3,4,5}));
        assertEquals(0, StringUtil.levenshtein(new int[]{}, new int[]{}));

        assertEquals(6, StringUtil.levenshtein(new int[]{}, new int[]{0,1,2,3,4,5}));
        assertEquals(6, StringUtil.levenshtein(new int[]{0,1,2,3,4,5}, new int[]{}));

        assertEquals(2, StringUtil.levenshtein(new int[]{0,1,4,5}, new int[]{0,1,2,3,4,5}));
        assertEquals(2, StringUtil.levenshtein(new int[]{0,1,2,3,4,5},  new int[]{0,1,4,5}));

        assertEquals(1, StringUtil.levenshtein( new int[]{0,1,2,3,-1,5},  new int[]{0,1,2,3,4,5}));
        assertEquals(1, StringUtil.levenshtein( new int[]{0,1,2,3,-1,5},  new int[]{0,1,2,3,-2,5}));
        assertEquals(1, StringUtil.levenshtein( new int[]{0,1,2,3,4,5}, new int[]{0,1,2,3,4,-1}));
        assertEquals(1, StringUtil.levenshtein( new int[]{0,1,2,3,4,5},  new int[]{-1,1,2,3,4,5}));

        assertEquals(1, StringUtil.levenshtein( new int[]{0,1,2,3,4,5},  new int[]{-1,0,1,2,3,4,5}));
        assertEquals(1, StringUtil.levenshtein( new int[]{-1,0,1,2,3,4,5},  new int[]{0,1,2,3,4,5}));

        assertThrows(NullPointerException.class, () -> StringUtil.levenshtein( new int[]{0,1,2,3,4,5}, null));
        assertThrows(NullPointerException.class, () -> StringUtil.levenshtein(null,  new int[]{0,1,2,3,4,5}));

        assertThrows(IllegalArgumentException.class, () -> StringUtil.levenshtein(new int[]{0,1,2,3,4,5}, -1, new int[]{0,1,2,3,4,5},  0));
        assertThrows(IllegalArgumentException.class, () -> StringUtil.levenshtein(new int[]{0,1,2,3,4,5}, 0, new int[]{0,1,2,3,4,5}, -1));
    }

    @Test
    void testName(){
        String algName = StringUtil.randomAlgorithmName();
        assertNotNull(algName);
        assertTrue(algName.length() > 0);
        assertTrue(algName.length() < 100);
    }

    @Test
    void nameInvalidChars(){
        for (int i = 0; i < 1_000; i++) {
            String algName = StringUtil.randomAlgorithmName();
            assertFalse(algName.contains("/"));
            assertFalse(algName.contains("+"));
        }
    }

    @Test
    void testSecret(){
        String secret = StringUtil.generateSecret();
        assertNotNull(secret);
        assertTrue(secret.length() > 0);
        assertTrue(secret.length() < 100);
    }

    @Test
    void testSecretN(){
        String secret = StringUtil.generateSecret(1);
        assertNotNull(secret);
        assertEquals(4, secret.length()); // Base64 encoded 1 byte length == 4

        secret = StringUtil.generateSecret(10);
        assertNotNull(secret);
        assertEquals(16, secret.length()); // Base64 encoded 10 bytes is length 16

        secret = StringUtil.generateSecret(12);
        assertNotNull(secret);
        assertEquals(16, secret.length()); // Base64 encoded 10 bytes is length 16
    }

    @Test
    void testb64(){
        String test = "hola";
        var b64 = StringUtil.b64encode(test);
        assertEquals("aG9sYQ==", b64);
        var decoded = StringUtil.b64decode(b64);
        assertEquals(test, decoded);
    }

    @Test
    void testReverse(){
        assertEquals("", StringUtil.reverse(""));
        assertEquals("cba", StringUtil.reverse("abc"));
        assertEquals("etnaparred anipla otom im", StringUtil.reverse("mi moto alpina derrapante"));
        assertEquals("1", StringUtil.reverse("1"));
        assertEquals("12/21", StringUtil.reverse("12/21"));
    }

    @Test
    void testLCP(){
        assertEquals("", StringUtil.longestCommonPrefix(new String[]{}));
        assertEquals("", StringUtil.longestCommonPrefix(new String[]{""}));
        assertEquals("", StringUtil.longestCommonPrefix(new String[]{"abcdef"}));
        assertEquals("ab.cd.", StringUtil.longestCommonPrefix(new String[]{"ab.cd.eg", "ab.cd.efgasd"}));
        assertEquals("ab.cd.", StringUtil.longestCommonPrefix(new String[]{"ab.cd.ef", "ab.cd.ef.g.asd"}));
        assertEquals("", StringUtil.longestCommonPrefix(new String[]{"abcdefasd", "abcdefgasd"}));
        assertEquals("...abc/", StringUtil.longestCommonPrefix(new String[]{"...abc/abcdefasd", "...abc/.abcdefgasd"}));
        assertEquals("/a/", StringUtil.longestCommonPrefix(new String[]{"/a/i/r/", "/a/m/b/u/l/a/c/e/", "/a/b/l/a/t/i/o/n/", "/a/e/r/o/p/l/a/n/e/"}));
        assertEquals("test_", StringUtil.longestCommonPrefix(new String[]{"test_15.json", "test_5.json", "test_40.json"}));
    }

    @Test
    void testLCS(){
        assertEquals("", StringUtil.longestCommonSuffix(new String[]{}));
        assertEquals("", StringUtil.longestCommonSuffix(new String[]{""}));
        assertEquals("", StringUtil.longestCommonSuffix(new String[]{"abcdef"}));
        assertEquals("", StringUtil.longestCommonSuffix(new String[]{"abcdef", "abcdef"}));
        assertEquals("", StringUtil.longestCommonSuffix(new String[]{"abcdef", "abcdefgasd"}));
        assertEquals(".txt", StringUtil.longestCommonSuffix(new String[]{"whatever.txt", "file2.txt"}));
        assertEquals("", StringUtil.longestCommonSuffix(new String[]{"abcdefgasd", "/abcdefgasd"}));
        assertEquals("/abcdefgasd.txt", StringUtil.longestCommonSuffix(new String[]{"abc/abcdefgasd.txt", "cba/abcdefgasd.txt"}));
        assertEquals(".json", StringUtil.longestCommonSuffix(new String[]{"/a/test_15.json", "/a/test_5.json", "/a/test_40.json"}));
    }

}