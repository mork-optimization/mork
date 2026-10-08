# JSON algorithm descriptions

Mork can build algorithms and individual algorithm components from JSON. This format is useful for storing a
manually selected algorithm, reproducing an autoconfig result, or sending a component description through an
external tool.

Autoconfig does not serialize and parse JSON internally. It creates the same `ComponentSpec` model directly from
the IRACE configuration and passes that model to the component builder.

## Format

Every component is a JSON object with a required `$component` property. Constructor parameters are fields in the
same object:

```json
{
  "$component": "VND",
  "improvers": [
    {
      "$component": "LocalSearchBestImprovement",
      "neighborhood": {
        "$component": "SwapNeighborhood"
      }
    },
    {
      "$component": "LocalSearchFirstImprovement",
      "neighborhood": {
        "$component": "InsertNeighborhood"
      }
    }
  ]
}
```

`$component` may contain a discovered class name, a registered alias, or a registered
`AlgorithmComponentFactory` name. Component resolution always uses Mork's component inventory; JSON class names
are never used for unrestricted Java deserialization.

All names beginning with `$` are reserved for format metadata. `$component` is currently the only supported
metadata property. Constructor parameter names must match `[a-zA-Z][a-zA-Z0-9]*`.

## Values

Parameters may contain:

- strings, booleans, integer numbers, real numbers, or `null`;
- another component object;
- an array of values, normally used for a `List<T>`, `T[]`, or varargs component parameter.

JSON strings are converted to enums, objectives, booleans, numeric types, and other supported constructor types
by the component builder. For a `char` or `Character` parameter, use a string containing exactly one Java
character.

Integer JSON values are represented as `Integer` when possible and otherwise as `Long`. Values outside the
signed 64-bit range are rejected. Real values are represented as `Double` and must be finite.

An empty component combination is an empty JSON array:

```json
{
  "$component": "VND",
  "improvers": []
}
```

Items in a manual JSON array are built in order. Restrictions used to generate the autoconfig search space, such
as combination repetition and blocked components, do not reject an explicitly supplied JSON description.
Normal Java constructor type checking still applies.

## Building descriptions

Use `AlgorithmBuilderService` when the JSON describes either a complete algorithm or another component:

```java
Algorithm<?, ?> algorithm = algorithmBuilderService.buildAlgorithmFromJson(json);
Object component = algorithmBuilderService.buildAlgorithmComponentFromJson(json);
```

The structured Java representation is `ComponentSpec`. It can be created and built without serializing it:

```java
var spec = new ComponentSpec(
        "VND",
        Map.of(
                "improvers",
                List.of(
                        new ComponentSpec("MyFirstLocalSearch"),
                        new ComponentSpec("MySecondLocalSearch")
                )
        )
);

Object component = algorithmBuilderService.buildAlgorithmComponent(spec);
String formattedJson = algorithmBuilderService.toJson(spec);
```

Use `buildAlgorithm(spec)` instead when the root specification names an `Algorithm`.

`AutomaticAlgorithmBuilder.buildFromJson(...)` provides the typed equivalent for application code that already
uses the automatic builder.

Parameters annotated with `@ProvidedParam` should normally be omitted. The matching `ParameterProvider` supplies
them while the component is built.

## Validation and errors

Descriptions use strict JSON:

- comments, duplicate properties, trailing documents, and trailing commas are rejected;
- single-quoted strings, Java character literals, hexadecimal numbers, numeric suffixes, `undefined`, `NaN`, and
  infinity are not valid;
- every object must describe a component and therefore contain `$component`;
- arbitrary map-valued constructor parameters are not supported;
- unknown components and constructor mismatches fail during component construction.

Syntax errors report their line and column. Structural errors report a JSON-style path such as
`$/improvers/1/$component`.

## Migrating the previous format

The former custom language has been removed. Migrate descriptions as follows:

| Previous description | JSON |
|---|---|
| `VND{}` | `{"$component":"VND"}` |
| `Component{size=10}` | `{"$component":"Component","size":10}` |
| `Component{name="test"}` | `{"$component":"Component","name":"test"}` |
| `Component{separator=';'}` | `{"$component":"Component","separator":";"}` |
| `Component{value=null}` | `{"$component":"Component","value":null}` |
| `Outer{child=Inner{}}` | `{"$component":"Outer","child":{"$component":"Inner"}}` |
| `VND{improvers=[A{},B{}]}` | `{"$component":"VND","improvers":[{"$component":"A"},{"$component":"B"}]}` |

Use normal JSON escaping for quotes, backslashes, control characters, and Unicode. The removed
`buildAlgorithmFromString(...)`, `buildAlgorithmComponentFromString(...)`, and
`AutomaticAlgorithmBuilder.buildFromStringDescription(...)` APIs have no compatibility aliases.
