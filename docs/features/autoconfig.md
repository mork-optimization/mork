# Autoconfig

Autoconfig builds an irace search space automatically from the algorithm components available in the application. It discovers components, reads one annotated constructor per component, generates conditional irace parameters, and later rebuilds the selected algorithm from the configuration returned by irace.

Use this page when you want Mork to propose component combinations and parameter domains for you. If you want to write `parameters.txt` manually, see the [irace integration](irace.md) page instead.

## Runtime Flow

1. Mork scans the packages configured in `advanced.scan-pkgs` for algorithm components.
2. Public classes annotated with `@AlgorithmComponent`, or extending a class annotated with it, are registered in the component inventory.
3. Autoconfig starts from discovered `Algorithm` implementations and recursively explores their component dependencies.
4. Each explored component must either have a registered `AlgorithmComponentFactory` or exactly one constructor annotated with `@AutoconfigConstructor`.
5. Constructor parameter annotations define the irace parameter domain.
6. Unannotated constructor parameters whose type is a known algorithm component are treated as recursive component choices.
7. Parameters annotated with `@ProvidedParam` are ignored by irace and filled at runtime by a matching `ParameterProvider`.

Runtime string construction is separate from automatic proposal generation. A component can still be built from a string if Mork can infer a matching constructor from parameter names and types, even when it does not have `@AutoconfigConstructor`.

## Minimal Example

```java
public class MyAlgorithm<S extends Solution<S, I>, I extends Instance> extends Algorithm<S, I> {

    @AutoconfigConstructor
    public MyAlgorithm(
            @ProvidedParam String algorithmName,
            @IntegerParam(min = 10, max = 1000) int iterations,
            Constructive<S, I> constructive,
            Improver<S, I> improver
    ) {
        super(algorithmName);
        // Store constructor parameters as final fields.
    }
}
```

In this example, irace chooses `iterations`, `constructive`, and `improver`. Mork generates `algorithmName` automatically.

## Annotation Reference

| Annotation | Target | Purpose |
|------------|--------|---------|
| `@AlgorithmComponent` | Class | Marks a class hierarchy as discoverable algorithm components. Inherited by subclasses, not interfaces. |
| `@InheritedComponent` | Class | Marks framework extension points whose implementations should be discovered as Spring components, such as factories and parameter providers. |
| `@AutoconfigConstructor` | Constructor | Selects the constructor used for automatic candidate generation. Use at most one per class. |
| `@IntegerParam` | Parameter | Creates an integer irace parameter. Use on integer-compatible Java types. |
| `@RealParam` | Parameter | Creates a real-valued irace parameter. Use on `float`, `double`, their wrappers, or raw `String` values. |
| `@CategoricalParam` | Parameter | Creates a categorical irace parameter from a non-empty string list. Values are converted to the constructor type when possible. |
| `@OrdinalParam` | Parameter | Creates an ordinal irace parameter from a non-empty ordered string list. |
| `@ComponentParam` | Parameter | Adds restrictions to recursive component resolution, such as excluding a component class and its subclasses. |
| `@ProvidedParam` | Parameter | Marks a value that is supplied at runtime by exactly one matching `ParameterProvider`. |

## Constructor Rules

Constructor parameter names are part of the autoconfig API. They become irace parameter names and are also used for runtime constructor matching. Projects built with the Mork parent already compile with `-parameters`; custom builds must keep Java parameter metadata enabled.

Parameter and component names must match this pattern:

```text
[a-zA-Z][a-zA-Z0-9]*
```

Use exactly one autoconfig parameter annotation per constructor parameter. Component dependencies normally do not need an annotation:

```java
public MyAlgorithm(Constructive<S, I> constructive, Improver<S, I> improver) { ... }
```

Autoconfig treats both parameters as recursive choices because `Constructive` and `Improver` are known component types.

## Component Restrictions

Use `@ComponentParam` when the default recursive search should exclude some implementations.

```java
@AutoconfigConstructor
public VND(
        @ComponentParam(disallowed = VND.class) Improver<S, I> improver1,
        @ComponentParam(disallowed = VND.class) Improver<S, I> improver2,
        @ComponentParam(disallowed = VND.class) Improver<S, I> improver3
) { ... }
```

Every disallowed class must be assignable to the annotated parameter type. If a class is disallowed, its subclasses are disallowed too.

## Provided Parameters

Use `@ProvidedParam` for values that should not be tuned by irace.

Built-in providers include:

| Parameter | Type | Provider |
|-----------|------|----------|
| `algorithmName`, `name`, `componentName` | `String` | Generated algorithm/component name |
| `objective` | `Objective` | Main objective from `Context` |

Custom values can be supplied by extending `ParameterProvider`. Provider matching uses both parameter type and name. If more than one provider matches the same parameter, autoconfig fails instead of choosing one by list order.

## Factories

Use `AlgorithmComponentFactory` when a component cannot be described cleanly with one constructor. Factories declare their required `ComponentParameter` list directly and build the component from the resolved parameter map.

Factories are useful for legacy components, aliases, or builders whose public constructor does not match the desired tuning surface.

## Configuration

The main autoconfig controls live under `solver`:

```yaml
solver:
  tree-depth: 1000
  max-derivation-repetition: 1
  experiments-per-parameter: 200
  minimum-number-of-experiments: 10000
  ignore-initial-millis: 10000
  interval-duration-millis: 50000
  log-scale-area: true
```

Use `--irace` or `--autoconfig` to launch tuning. Use `--follower` to start only the execution controller.

## Troubleshooting

If a component is missing from the generated space, check that it is public, under `advanced.scan-pkgs`, and either annotated with `@AlgorithmComponent` or extends an annotated component base class.

If a component is discovered but ignored, check that it has exactly one `@AutoconfigConstructor` or a registered factory.

If constructor matching fails, check parameter names, Java parameter metadata, and whether every `@ProvidedParam` has exactly one matching provider.
