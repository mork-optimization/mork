package es.urjc.etsii.grafo.autoconfig.irace;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IraceConfigTest {

    @Test
    void absentLegacyPropertyUsesRScript() {
        var config = new IraceConfig();

        assertDoesNotThrow(config::validateRScriptExecution);
    }

    @Test
    @SuppressWarnings("removal")
    void legacyTrueUsesRScript() {
        var config = new IraceConfig();
        config.setShell(true);

        assertDoesNotThrow(config::validateRScriptExecution);
    }

    @Test
    @SuppressWarnings("removal")
    void legacyFalseProducesMigrationError() {
        var config = new IraceConfig();
        config.setShell(false);

        var failure = assertThrows(IllegalStateException.class, config::validateRScriptExecution);

        assertTrue(failure.getMessage().contains("irace.shell=false"));
        assertTrue(failure.getMessage().contains("Install GNU R"));
    }
}
