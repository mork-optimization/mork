# Irace Integration

## What is IRACE?

In short, Irace is a software package that implements a number of automatic configuration procedures, that allows us to easily tune our algorithms when manually testing each possible configuration is not viable.

Irace is integrated in Mork, so tuning your algorithms is extremely easy.

See the official documentation for more information about Irace.
https://cran.r-project.org/web/packages/irace/vignettes/irace-package.pdf


# Requirements

## Activating IRACE mode

To execute MORK in IRACE tuning mode, use the `--irace` or `--autoconfig` command-line argument when running your application:

```bash
# Run with irace mode
java -jar your-application.jar --irace

# Or use the autoconfig alias
java -jar your-application.jar --autoconfig
```

To start only the follower process that exposes the execution API and waits for external commands, run:

```bash
java -jar your-application.jar --follower
```

When running in IRACE tuning mode, user defined experiments are ignored, and a special tuning experiment is executed using the user provided scenario. In follower mode, MORK starts the execution controller and waits for incoming requests instead of launching tuning locally.

## Configuring R integration

Local IRACE tuning runs R scripts through the `Rscript` executable. Install
[R for your environment](https://cran.r-project.org/bin/) and ensure both `R`
and `Rscript` are available on `PATH` before starting Mork with `--irace` or
`--autoconfig`.

Verify the installation from your terminal:

```bash
Rscript --version
```

# How to use it
Mork supports two irace workflows:

- Automatic configuration generation, where Mork discovers annotated components and generates the irace parameter space. See [Autoconfig](autoconfig.md).
- Manual configuration, where you implement an `AlgorithmBuilder` and maintain `parameters.txt` yourself.

This doc page documents the second use case. For the automatic configuration generation method, see the [Autoconfig](autoconfig.md) doc page.
There are three main things that have to be done in order to use Irace manually.
1. Configuring dynamic algorithm generation.
2. Defining algorithm parameters to test.
3. Adjusting scenario options.

Please read the complete set of steps before proceeding.

## Configuring dynamic algorithm generation

In a normal experiment, algorithm configurations are defined inside an experiment. When using Irace, algorithms must be dynamically built to match the different configurations Irace wants to test.

 1. Create a new Java class that extends `AlgorithmBuilder`.
 2. Override the method`buildFromConfig`. Note that this method returns a single `Algorithm` object, unlike the other experiments where a list of experiments is returned.
 3.  The `buildFromConfig` method receives an `AlgorithmConfiguration` object as an input parameter. This parameter is used to determine the configuration of the algorithm to be tuned. To access the value of each of the parameters that configure the algorithm the method `getValue` is used.
Note that this method returns an Optional<String> if a default value is not provided, given the fact that Irace may supply a given parameter only in certain configurations (Conditional parameters can be defined inside the parameters.txt file, for example only provide an alpha value if a GRASP like constructive is used).

**Tip**: If you are certain a given parameter is always present, use `configuration.get("parameterName").orElseThrow()`. If for whatever reason the parameter is NOT present, an Exception will be thrown.



## Defining algorithm parameters to test
 The parameters of the target algorithm are defined by a parameter file `parameters.txt` located in `src/main/resources/irace/parameters.txt`.

Each target parameter has an associated type that defines its domain and the way Irace handles them internally.  The four basic types supported by irace are: *Real*, *Integer*, *Categorical* and *Ordinal*. The parameter file format follows a table like scheme, where each row is defined as:

    <name> <label> <type> <range> [| <condition>]

 - The `name` of the parameter as an unquoted alphanumeric string.
 - A `label` for this parameter. This label will be later used in the `getValue` method of the `IraceConfiguration` object. Unless you have a reason not to, it is a good idea to match the name plus the equals sign. (i.e:  if the parameter name is `alpha`, use `alpha=` as the label value.
 - The `type` of the parameter, either integer, real, ordinal or categorical, given as **a single letter**: `i`, `r`, `o` or `c`.
 - The `range` or set of values of the parameter delimited by parentheses. e.g., (0,1) or (a,b,c,d).
 - An optional `condition` that determines whether the parameter is enabled or disabled, thus making the parameter conditional. If the condition evaluates to false, then no value is assigned to this parameter, and neither the parameter value nor the corresponding label are passed to algorithm. **The condition must be a valid R logical expression**.

## Adjusting scenario options
The scenario allows specifying a text file that contains an initial set of configurations to start the execution of Irace. Particularly, this configuration is defined in `scenario.txt` file located in `src/main/resources/irace/scenario.txt` .


## More info
Check full parameter.txt and scenario.txt documentation in [the official Irace manual](https://cran.r-project.org/web/packages/irace/vignettes/irace-package.pdf).

More information in the guidelines provided in the published article: ["The irace package: Iterated racing for automatic algorithm configuration"](https://doi.org/10.1016/j.orp.2016.09.002), or in the *irace* package documentation: ["The irace Package: User Guide"](https://cran.r-project.org/web/packages/irace/vignettes/irace-package.pdf).

**Reminder!**:  When running MORK with `--irace`, `--autoconfig`, or `--follower`, no other user defined experiments will execute.
