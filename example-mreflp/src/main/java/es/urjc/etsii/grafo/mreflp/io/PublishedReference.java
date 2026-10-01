package es.urjc.etsii.grafo.mreflp.io;

public record PublishedReference(String caseId, String method, Double cost, Double averageCost,
                                 Double timeToBestSeconds, boolean optimal, String sheet, String cell, boolean flagged) {}
