package es.urjc.etsii.grafo.events;

import es.urjc.etsii.grafo.events.types.PingEvent;
import es.urjc.etsii.grafo.events.types.SolutionGeneratedEvent;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class EventEnvelopeJsonTest {

    private final JsonMapper mapper = new JsonMapper();

    @Test
    void envelopeOwnsTransportMetadata() throws Exception {
        var envelope = new EventEnvelope(7, "PingEvent", 123L, "producer", new PingEvent());

        assertEquals(
                "{\"eventId\":7,\"type\":\"PingEvent\",\"timestamp\":123,\"workerName\":\"producer\",\"payload\":{}}",
                mapper.writeValueAsString(envelope)
        );
        var payload = mapper.readTree(mapper.writeValueAsString(envelope)).get("payload");
        assertFalse(payload.has("type"));
        assertFalse(payload.has("workerName"));
    }

    @Test
    void resultUuidIsSerializedAsAJsonString() throws Exception {
        UUID id = UUID.randomUUID();
        var payload = new SolutionGeneratedEvent(
                id, true, "experiment", "instance", "algorithm", "1",
                Map.of("score", 4.2), 4.2, 10L, 5L
        );
        var json = mapper.readTree(mapper.writeValueAsString(new EventEnvelope(
                0, "SolutionGeneratedEvent", 1L, "worker", payload
        )));

        assertEquals(id.toString(), json.get("payload").get("resultId").asText());
    }
}
