package es.urjc.etsii.grafo.autoconfig.controller.dto;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * An elite IRACE configuration reported by the R process.
 *
 * @param configurationId IRACE configuration identifier
 * @param parameters flattened IRACE parameter values
 */
public record EliteConfiguration(String configurationId, Map<String, String> parameters) {

    public EliteConfiguration {
        Objects.requireNonNull(configurationId, "Configuration ID cannot be null");
        if (configurationId.isBlank()) {
            throw new IllegalArgumentException("Configuration ID cannot be blank");
        }
        Objects.requireNonNull(parameters, "Elite parameters cannot be null");
        parameters = Collections.unmodifiableMap(new TreeMap<>(parameters));
    }
}
