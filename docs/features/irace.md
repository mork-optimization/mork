# Irace Integration

## What is IRACE?

In short, Irace is a software package that implements a number of automatic configuration procedures, that allows us to easily tune our algorithms when manually testing each possible configuration is not viable.

Irace is integrated in Mork, so tuning your algorithms is extremely easy.

See the official documentation for more information about Irace.
https://cran.r-project.org/web/packages/irace/vignettes/irace-package.pdf


# Requirements

Inside the default `application.yml` you may see a section similar to this:
```YML
# Enable irace integration? Check IRACE Wiki section before enabling
irace:
  enabled: false

  # False: use GraalVM implementation, does not need R installed locally
  # True: Use shell to execute R scripts, R/Rscript need to be locally installed and in path
  shell: true
```

When `irace.enabled` is true, user defined experiments are ignored, and a special tuning experiment is executed using the user provided scenario.

In order to use Irace, you need to either use GraalVM (and set `irace.shell` to `false`) or install R/Rscript locally (and set `irace.shell` to `true`).

## Option A: Using GraalVM
- Install and configure the GraalVM, the recommended way is to use [sdkman](https://sdkman.io/).
- Follow the instructions in https://www.graalvm.org/reference-manual/r/
- Set `irace.shell` to `false`.
```bash
# Example installation instructions
sdk install java 21.3.0.r17-grl # Use sdk list java to see latest available GraalVM version
gu install R
```

## Option B: Using native R
- Install and configure R for your environment: https://cran.r-project.org/bin/
- Test that R and Rscript are available as commands in your favorite console.
- Set `irace.shell` to `true`

# How to use it
There are three main things that have to be done in order to use Irace.
1. Configuring dynamic algorithm generation.
2. Defining algorithm parameters to test.
3. Adjusting scenario options.

Please read the complete set of steps before proceeding.

## Configuring dynamic algorithm generation

In a normal experiment, algorithm configurations are defined inside an experiment. When using Irace, algorithms must be build dynamically to match the different configurations Irace wants to test.

 1. Create a new Java class that extends `IraceAlgorithmGenerator`.
 2. Override the method`buildAlgorithm`. Note that this method returns a single `Algorithm` object, unlike the other experiments where a list of experiments is returned.
 3.  The `buildAlgorithm` method receives an `IraceConfiguration` object as an input parameter. This parameter is used to determine the configuration of the algorithm to be tuned. To access the value of each of the parameters that configure the algorithm the method `getValue` is used.
Note that this method returns an Optional<String> if a default value is not provided, given the fact that Irace may supply a given parameter only in certain configurations (Conditional parameters can be defined inside the parameters.txt file, for example only provide an alpha value if a GRASP like constructive is used).

**Tip**: If you are certain a given parameter is always present, use `configuration.get("parameterName").orElseThrow()`. If for whatever reason the parameter is NOT present, an Exception will be thrown.



## Defining algorithm parameters to test
 The parameters of the target algorithm are defined by a parameter file `parameters.txt` located in `src/main/resources/irace/parameters.txt`.

Each target parameter has an associated type that defines its domain and the way Irace handles them internally.  The four basic types supported by irace are: *Real*, *Integer*, *Categorical* and *Ordinal*. The parameter file format follows a table like scheme, where each row is defined as:

    <name> <label> <type> <range> [| <condition>]

 - The `name` of the parameter as an unquoted alphanumeric string.
 - A `label` for this parameter. This label will be later used in the `getValue` method of the `IraceConfiguration` object. Unless you have a reason not to, it is a good idea to match the name plus the equals sign. (i.e:  if the parameter name is `alpha`, use `alpha=` as the label value.
 - The `type` of the parameter, either integer, real, ordinal or categorical, given as **a single letter**: ‘i’, ‘r’, ‘o’ or ‘c’.
 - The `range` or set of values of the parameter delimited by parentheses. e.g., (0,1) or (a,b,c,d).
 - An optional `condition` that determines whether the parameter is enabled or disabled, thus making the parameter conditional. If the condition evaluates to false, then no value is assigned to this parameter, and neither the parameter value nor the corresponding label are passed to algorithm. **The condition must be a valid R logical expression**.

## Adjusting scenario options
The scenario allows specifying a text file that contains an initial set of configurations to start the execution of Irace. Particularly, this configuration is defined in `scenario.txt` file located in `src/main/resources/irace/scenario.txt` .


## More info
Check full parameter.txt and scenario.txt documentation in [the official Irace manual](https://cran.r-project.org/web/packages/irace/vignettes/irace-package.pdf).

More information in the guidelines provided in the published article: ["The irace package: Iterated racing for automatic algorithm configuration"](https://doi.org/10.1016/j.orp.2016.09.002), or in the *irace* package documentation: ["The irace Package: User Guide"](https://cran.r-project.org/web/packages/irace/vignettes/irace-package.pdf).

**Reminder!**:  When the irace option is activated ( `irace.enabled = true` ), no other user defined experiments will execute.
