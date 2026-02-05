package es.urjc.etsii.grafo.autoconfig.generator;

import es.urjc.etsii.grafo.autoconfig.inventory.BlacklistInventoryFilter;
import es.urjc.etsii.grafo.autoconfig.inventory.DefaultInventoryFilter;
import es.urjc.etsii.grafo.autoconfig.inventory.WhitelistInventoryFilter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

public class FilterConfigTest {

    private FilterConfig filterConfig;

    @BeforeEach
    void setup() {
        filterConfig = new FilterConfig();
        filterConfig.pkgs = "es.urjc.etsii.grafo.autoconfig.generator"; // scan our own package for test filters
        filterConfig.whitelistParam = "";
        filterConfig.blacklistParam = "";
    }

    @Test
    void testDefaultFilter() {
        var filter = filterConfig.getDefaultFilterStrategy();
        Assertions.assertTrue(filter instanceof DefaultInventoryFilter);
    }

    @Test
    void testBothParamsProvided() {
        filterConfig.whitelistParam = "true";
        filterConfig.blacklistParam = "true";
        Assertions.assertThrows(IllegalArgumentException.class, () -> filterConfig.getDefaultFilterStrategy());
    }

    @Test
    void testWhitelistFlagAmbiguous() {
        filterConfig.whitelistParam = "true";
        Assertions.assertThrows(IllegalArgumentException.class, () -> filterConfig.getDefaultFilterStrategy());
    }

    @Test
    void testWhitelistSimpleName() {
        filterConfig.whitelistParam = "TestWhitelistFilter";
        var filter = filterConfig.getDefaultFilterStrategy();
        Assertions.assertTrue(filter instanceof TestWhitelistFilter);
    }

    @Test
    void testWhitelistFQN() {
        filterConfig.whitelistParam = "es.urjc.etsii.grafo.autoconfig.generator.FilterConfigTest$TestWhitelistFilter";
        var filter = filterConfig.getDefaultFilterStrategy();
        Assertions.assertTrue(filter instanceof TestWhitelistFilter);
    }

    @Test
    void testBlacklistFlag() {
        filterConfig.blacklistParam = "true";
        var filter = filterConfig.getDefaultFilterStrategy();
        Assertions.assertTrue(filter instanceof TestBlacklistFilter);
    }

    @Test
    void testBlacklistSimpleName() {
        filterConfig.blacklistParam = "TestBlacklistFilter";
        var filter = filterConfig.getDefaultFilterStrategy();
        Assertions.assertTrue(filter instanceof TestBlacklistFilter);
    }

    @Test
    void testBlacklistFQN() {
        filterConfig.blacklistParam = "es.urjc.etsii.grafo.autoconfig.generator.FilterConfigTest$TestBlacklistFilter";
        var filter = filterConfig.getDefaultFilterStrategy();
        Assertions.assertTrue(filter instanceof TestBlacklistFilter);
    }

    @Test
    void testClassNotFound() {
        filterConfig.whitelistParam = "NonExistentFilter";
        Assertions.assertThrows(IllegalArgumentException.class, () -> filterConfig.getDefaultFilterStrategy());
    }

    @Test
    void testWrongType() {
        // Trying to use a blacklist filter when whitelist is expected
        filterConfig.whitelistParam = "TestBlacklistFilter";
        Assertions.assertThrows(IllegalArgumentException.class, () -> filterConfig.getDefaultFilterStrategy());
    }

    @Test
    void testPrivateConstructorAllowed() {
        filterConfig.whitelistParam = "es.urjc.etsii.grafo.autoconfig.generator.FilterConfigTest$PrivateConstructorFilter";
        var filter = Assertions.assertDoesNotThrow(() -> filterConfig.getDefaultFilterStrategy());
        Assertions.assertTrue(filter instanceof PrivateConstructorFilter);
    }

    @Test
    void testAmbiguousSimpleName() {
        filterConfig.whitelistParam = "TestAmbiguous";
        Assertions.assertThrows(IllegalArgumentException.class, () -> filterConfig.getDefaultFilterStrategy());
    }

    public static class TestWhitelistFilter extends WhitelistInventoryFilter {
        @Override
        public Set<Class<?>> getWhitelist() {
            return Set.of();
        }
    }

    public static class TestBlacklistFilter extends BlacklistInventoryFilter {
        @Override
        public Set<Class<?>> getBlacklist() {
            return Set.of();
        }
    }

    public static class AnotherWhitelistFilter extends WhitelistInventoryFilter {
        @Override
        public Set<Class<?>> getWhitelist() {
            return Set.of();
        }
    }

    public static class TestAmbiguous extends WhitelistInventoryFilter {
        @Override
        public Set<Class<?>> getWhitelist() {
            return Set.of();
        }
    }

    public static class AnotherTestAmbiguous extends WhitelistInventoryFilter {
        @Override
        public Set<Class<?>> getWhitelist() {
            return Set.of();
        }

        public static class TestAmbiguous extends WhitelistInventoryFilter {
            @Override
            public Set<Class<?>> getWhitelist() {
                return Set.of();
            }
        }
    }

    public static class PrivateConstructorFilter extends WhitelistInventoryFilter {
        private PrivateConstructorFilter() {}

        @Override
        public Set<Class<?>> getWhitelist() {
            return Set.of();
        }
    }
}
