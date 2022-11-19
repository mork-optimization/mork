# [__RNAME__](https://doi.org/XXXXX)

## Abstract
Paper abstract or summary of the article submitted.

## Authors
Authors involved in this work and their respective contributions:
- Person1.

## Datasets

Instances are categorized in different datasets inside the 'resources/instances' folder. All instances are from the [UCI Machine Learning Repository](https://archive.ics.uci.edu/ml/index.php)

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
java -jar target/MyProblem.jar --instances.path.default=newinstances --solver.experiment=IteratedGreedyExperiment
```

```
java -server -jar BMSSC.jar indexfile
```

Example: running with only new proposed instances:
```
java -server -jar BMSSC.jar resources/instances/new/index
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
