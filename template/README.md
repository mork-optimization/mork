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

Example: running experiments with a new set of instances organized inside the `new` folder, disabling parallelization:
```
java -jar __RNAME__.jar --instances.path.default=new --solver.parallelExecutor=false 
```

For a full list of configurable parameters, see the [configuration section of the Mork documentation](https://docs.mork-optimization.com/en/latest/features/config/).


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
