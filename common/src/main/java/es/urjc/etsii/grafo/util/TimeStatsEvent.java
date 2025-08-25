package es.urjc.etsii.grafo.util;

public record TimeStatsEvent(String clazz, String method, long enter, long exit) {
    public TimeStatsEvent {
        assert enter < exit;
    }
}
