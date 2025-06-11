package es.urjc.etsii.grafo.util;

public record TimeStatsEvent(boolean enter, long when, String clazz, String method) {}
