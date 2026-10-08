package es.urjc.etsii.grafo.autoconfig.service;

import es.urjc.etsii.grafo.autoconfig.builder.ComponentSpec;
import es.urjc.etsii.grafo.autoconfig.builder.ComponentSpecJsonCodec;
import es.urjc.etsii.grafo.autoconfig.exception.AlgorithmParsingException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComponentSpecJsonCodecTest {

    private final ComponentSpecJsonCodec codec = new ComponentSpecJsonCodec();

    @Test
    void parsesAllSupportedValues() {
        var spec = codec.parse("""
                {
                  "$component": "Root",
                  "text": "escaped \\" text and unicode \\u20ac",
                  "enabled": true,
                  "nothing": null,
                  "small": 12,
                  "large": 2147483648,
                  "real": -145.93e10,
                  "child": {"$component": "Child"},
                  "items": [
                    {"$component": "First"},
                    null,
                    "value"
                  ]
                }
                """);

        assertEquals("Root", spec.component());
        assertEquals("escaped \" text and unicode €", spec.parameters().get("text"));
        assertEquals(true, spec.parameters().get("enabled"));
        assertTrue(spec.parameters().containsKey("nothing"));
        assertNull(spec.parameters().get("nothing"));
        assertInstanceOf(Integer.class, spec.parameters().get("small"));
        assertInstanceOf(Long.class, spec.parameters().get("large"));
        assertInstanceOf(Double.class, spec.parameters().get("real"));
        assertEquals("Child", assertInstanceOf(ComponentSpec.class, spec.parameters().get("child")).component());

        var items = assertInstanceOf(List.class, spec.parameters().get("items"));
        assertEquals("First", assertInstanceOf(ComponentSpec.class, items.get(0)).component());
        assertNull(items.get(1));
        assertEquals("value", items.get(2));
    }

    @Test
    void serializesFlattenedComponentSpecsInStableOrder() {
        var spec = new ComponentSpec("Root", Map.of(
                "child", new ComponentSpec("Child"),
                "items", List.of(new ComponentSpec("First"), new ComponentSpec("Second"))
        ));

        String json = codec.toJson(spec);
        var reparsed = codec.parse(json);

        assertEquals(spec.component(), reparsed.component());
        assertTrue(json.indexOf("\"$component\"") < json.indexOf("\"child\""));
        assertEquals("Child", assertInstanceOf(ComponentSpec.class, reparsed.parameters().get("child")).component());
    }

    @Test
    void supportsEmptyArrays() {
        var spec = codec.parse("""
                {"$component": "Root", "items": []}
                """);

        assertEquals(List.of(), spec.parameters().get("items"));
    }

    @Test
    void componentSpecsDefensivelyCopyCollections() {
        var items = new ArrayList<Object>();
        items.add(new ComponentSpec("First"));
        var spec = new ComponentSpec("Root", Map.of("items", items));
        items.add(new ComponentSpec("Second"));

        var copiedItems = assertInstanceOf(List.class, spec.parameters().get("items"));
        assertEquals(1, copiedItems.size());
        assertThrows(UnsupportedOperationException.class, () -> copiedItems.add(new ComponentSpec("Third")));
        assertThrows(UnsupportedOperationException.class, () -> spec.parameters().put("value", 1));
    }

    @Test
    void rejectsInvalidRootAndComponentMetadata() {
        assertInvalid("[]", "$");
        assertInvalid("{}", "missing required property");
        assertInvalid("{\"$component\": 1}", "/$component");
        assertInvalid("{\"$component\": \"  \"}", "cannot be blank");
        assertInvalid("{\"$component\": \"Root\", \"$id\": \"x\"}", "unknown reserved property");
    }

    @Test
    void rejectsInvalidParametersAndArbitraryObjects() {
        assertInvalid("{\"$component\": \"Root\", \"bad-name\": 1}", "Invalid component parameter name");
        assertInvalid(
                "{\"$component\": \"Root\", \"value\": {\"key\": 1}}",
                "$/value"
        );
    }

    @Test
    void rejectsDuplicateKeysTrailingContentAndMalformedJson() {
        assertInvalid(
                "{\"$component\":\"Root\",\"value\":1,\"value\":2}",
                "Duplicate Object property"
        );
        assertInvalid("{\"$component\":\"Root\"} {}", "Trailing token");
        assertInvalid("{\"$component\":\"Root\",}", "Invalid algorithm JSON");
    }

    @Test
    void rejectsUnsupportedNumbers() {
        assertInvalid(
                "{\"$component\":\"Root\",\"value\":9223372036854775808}",
                "signed 64-bit"
        );
        assertInvalid("{\"$component\":\"Root\",\"value\":NaN}", "Invalid algorithm JSON");
        assertInvalid("{\"$component\":\"Root\",\"value\":Infinity}", "Invalid algorithm JSON");
    }

    @Test
    void rejectsUnsupportedProgrammaticValues() {
        var spec = new ComponentSpec("Root", Map.of("value", Map.of("key", "value")));

        assertThrows(AlgorithmParsingException.class, () -> codec.toJson(spec));
    }

    private void assertInvalid(String json, String messagePart) {
        var exception = assertThrows(AlgorithmParsingException.class, () -> codec.parse(json));
        assertTrue(
                exception.getMessage().contains(messagePart),
                () -> "Expected <%s> in <%s>".formatted(messagePart, exception.getMessage())
        );
    }
}
