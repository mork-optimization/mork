# App configuration

The app can be configured in multiple ways. See the application.yml

## How to provide or change configuration at runtime
Several ways to override the embedded configuration file are provided, from more priority to less.

### Using the command line
```bash
java -jar myproject.jar --seed=1235 --instances.path.default=/results
```
or
```
java -Dseed=1235 -Dinstances.path.default=/results -jar myproject.jar
```

> Remember: command line properties always take precedence over any configuration file.

### Using environment variables
```bash
export INSTANCES_PATH_DEFAULT="/results"
java -jar myproject.jar
```
Note that the equivalent environment variable for a property is calculated as: `-` are removed, `.` replaced with `_`, and the string is uppercased.

### Using a configuration file
Place an `application.yml` on the same folder as the jar. Any property or configuration parameter in this `application.yml` overrides the corresponding value of the packaged `application.yml` inside the jar.

## Extending the configuration or adding custom values

You may add and use any custom config property inside your implementation. The value for the custom config parameter can be provided with any of the methods explained above. In order to retrieve the config value for any given key at runtime, use the static methods inside the `ConfigService` class.

## Advanced

The app configuration system is inherited from Spring Boot. Everything that can be done as specified in the Spring Boot docs can be done in Mork. See https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config for a full reference.

## Reference configuration
The following is a reference configuration as can be found in the generated project. All properties should be properly documented, open an issue or a PR if something is not clear to modify the default configuration file.

```yml
# Random seed. The algorithms should generate the exact same results as long as the seed does not change.
seed: 1234

instances:
  path:
    # Default instance path for all experiments, can be overridden.
    default: 'instances'

    # Override default instance path only for the experiment declared in file PreliminarExperiment.java
    # If an experiment does not have a specific path the default will be used. Example:
    PreliminarExperiment: './instances/preliminar'

solutions:
  # Path where solutions will be exported (Only if a solution serializer is enabled, see below)
  path:
    out: 'solutions'
  dateformat: 'yyyy-MM-dd_HH-mm-ss'

errors:
  # Path where all errors or exceptions encountered during experiment execution will be exported
  # Experiment execution DOES NOT (usually) END if
  # an uncontrolled exception is propagated, the error is logged, and we try to keep solving
  path: 'errors'

serializers:
  sol-json:
    # Enable default JSON serializer for solutions. Exports best solution of each algorithm to JSON.
    # If you want to export solutions to a custom format, extend SolutionSerializer,
    # NOTE: the default JSON solution serializer is automatically disabled when the SolutionSerializer class is extended.
    enabled: false
    # If enabled, pretty print JSON (indentation + new lines)
    pretty: true

  csv:
    # Export results to CSV, set to false to skip serializing results to CSV
    enabled: false

    # Can use commas, semicolons, \t (tabs) or any other character to separate columns when exporting the results to CSV
    # Note: Use only a single character
    separator: ','

    # Results folder
    folder: 'results'

    # Filename format, replacements are applied as follows
    # yyyy: replaced with current year, ex 2020
    # MM, dd, HH, mm, ss: replaced by month, day, hour, minute and seconds
    # any letters [a-zA-Z] can be part of the filename as long as they are between single quotes
    format: "'Results'_yyyy-MM-dd_HH-mm-ss.'csv'"

  xlsx:
    # Enable XLSX results serializer, set to false to skip serializing results to Excel 2007+
    enabled: true

    # When generating the pivot table, should algorithms be in rows or columns?
    # True: Instances per row, algorithms in columns
    # False: Algorithms in rows, instances in columns
    algorithmsInColumns: true

    # Results folder
    folder: 'results'

    # Filename format, replacements are applied as follows
    # yyyy: replaced with current year, ex 2020
    # MM, dd, HH, mm, ss: replaced by month, day, hour, minute and seconds
    # letters [a-zA-Z] can be part of the filename as long as they are between single quotes
    format: "'Results'_yyyy-MM-dd_HH-mm-ss.'xlsx'"

solver:

  # Which experiments should be executed? .* --> All.
  # Experiment names default to the class name in which they are declared unless overridden.
  # Tip, you may use a Regex, example: Preeliminar.*
  experiments: '.*'

  # Maximize or minimize objective function? True --> Maximizing, False --> Minimizing
  maximizing: true

  # How many times should each experiment be repeated. Recommended a minimum of 30
  repetitions: 100

  # Use parallel executor DISABLE IF THE ALGORITHM IS ALREADY PARALLELIZED
  # Valid Values: true, false
  parallelExecutor: true

  # Number of workers to use if parallelExecutor is enabled
  # any number between 1 and MAX_INT, or -1 to automatically decide at runtime (available threads / 2)
  nWorkers: -1

  # Execute benchmark before starting solver?
  benchmark: true

# Advanced configuration, do not change unless you know what you are doing!
advanced:
  # Block Java API methods
  block:
    # Collections.shuffle(RandomManager.getRandom()) should be used instead of Collections.shuffle()
    collections-shuffle: true
    # Block Math.random(), use RandomManager.getRandom().nextDouble()
    math-random: true

# Logging configuration
logging:
  level:
    # Default logging level, do not modify unless you know what you are doing
    root: 'info'
    es:
      urjc:
        etsii:
          # Use:
          # - INFO:  Print only important messages
          # - DEBUG: Show debug logs for each algorithm
          # - TRACE: Print all debug messages, and print ALL movements when they are applied to any solution.
          #          Note: Enabling trace mode has a big performance impact.
          #grafo: 'DEBUG'
          grafo: 'INFO'

  # Save logs to file 'log.txt'
  file:
    name: 'log.txt'

  # Logging messages format
  pattern.console: '%clr([%d{${LOG_DATEFORMAT_PATTERN:yyyy/MM/dd HH:mm:ss}}]){faint} %clr(${LOG_LEVEL_PATTERN:--%5p}) %clr(--> %-40.40logger{39}){cyan} %clr(:){faint} %m%n${LOG_EXCEPTION_CONVERSION_WORD:%wEx}'
```
