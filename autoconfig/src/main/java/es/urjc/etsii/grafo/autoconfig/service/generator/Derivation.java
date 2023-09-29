package es.urjc.etsii.grafo.autoconfig.service.generator;

/**
 * Represents a derivation rule in the autoconfig grammar
 * @param type a base algorithm component type, usually a generic abstract class. For example, a Constructive.
 * @param target a concrete implementation of the given component type. For example, a RandomConstructive.
 */
public record Derivation(String type, String target) {
    public Derivation(Class<?> type, Class<?> target) {
        this(type.getName(), target.getName());
    }
}