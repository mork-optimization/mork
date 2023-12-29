# [__RNAME__](https://doi.org/XXXXX)

## Abstract
Paper abstract or summary of the article submitted.

## Authors
Authors involved in this work and their respective contributions:
- Person1.

## Datasets

Instances are categorized in different datasets inside the 'instances' folder. All instances are from the [UCI Machine Learning Repository](https://archive.ics.uci.edu/ml/index.php) or reference to paper where they are from, etc.

## Compiling

You can easily compile and build an executable artifact of this project using Maven and a recent version of Java (17+):
```text
mvn clean package
```

## Executing

You can just run the generated jar file in target. For easy of use there is an already compiled JAR inside the target folder.
To review a full list of configurable parameters, either using an application.yml in the same folder as the executable, or using command line parameters, see the Mork documentation, section configuration.
Example: execute the IteratedGreedyExperiment using a new set of instances located inside the `newinstances` folder.

```text
java -jar target/__RNAME__.jar --instances.path.default=newinstances --solver.experiment=IteratedGreedyExperiment
```

```
java -server -jar __RNAME__.jar indexfile
```

Example: running with only new proposed instances:
```
java -server -jar __RNAME__.jar instances/new/index
```

## Instance format

Explain instance format so other users may easily use them even if not using your code.


## Cite

Consider citing our paper if used in your own work:

### DOI
https://doi.org/XXXXXXX

### Bibtex
```bibtex
@article{
...
}
```

## Powered by MORK (Metaheuristic Optimization framewoRK)
| ![mork logo](https://user-images.githubusercontent.com/55482385/233611563-4f5c91f2-af36-4437-a4b5-572b6655487a.svg) | Mork is a Java framework for solving easily hard optimization problems. You can [create a project](https://generator.mork-optimization.com/) fastly right now or just visit the [documentation](https://docs.mork-optimization.com/en/latest/) or the [repository](https://github.com/mork-optimization/mork). |
|--|--|
