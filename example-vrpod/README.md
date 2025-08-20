# [Autoconfig VRPOD](https://doi.org/XXXXX)

## Abstract
Metaheuristic algorithms have become one of the preferred approaches for
solving optimization problems. Finding the best metaheuristic for a given
problem is often difficult due to the large number of available approaches
and possible algorithmic designs. Moreover, high-performing metaheuristics
often combine general-purpose and problem-specific algorithmic components.
We propose here an approach for automatically designing metaheuristics using
a flexible framework of algorithmic components, from which algorithms
are instantiated and evaluated by an automatic configuration method. The
rules for composing algorithmic components are defined implicitly by the
properties of each algorithm component, in contrast to previous proposals,
which require a handwritten algorithmic template or grammar. As a result,
extending our framework with additional components, even problem-specific
or user-defined ones, automatically updates the design space. We provide
an implementation of our proposal and demonstrate its benefits by outperforming
earlier research in three distinct problems from completely different
families: a facility layout problem, a vehicle routing problem and a clustering
problem.

## Links to Github repositories
- https://github.com/rmartinsanta/ac-VRPOD
- https://github.com/rmartinsanta/ac-BMSSC
- https://github.com/rmartinsanta/ac-SFMRFLP


## Authors of the Autoconfig proposal
Authors involved in this work and their respective contributions:
- Raúl Martín Santamaría
- Manuel López-Ibáñez
- Thomas Stützle
- José Manuel Colmenar Verdugo

## Authors of the original work
- Raúl Martín Santamaría
- Ana Dolores López Sánchez
- María Luisa Delgado-Jalón
- José Manuel Colmenar Verdugo


## Datasets
Instances are categorized in different datasets inside the 'instances' folder.

## Instance format
See the VRPODInstanceImporter class for full details about how to parse the instance files.
The provided format is not trivial.

## Properties used for instance classification and selection
- Number of client destinations
- Number of occasional drivers
- Rho
- Zeta
- Instance generator type (Clustered, Random, Hybrid), numbered as 1, 2 and 3 respectively
- Route vehicle capacity
- (min, max, avg, std) distance to depot for all clients
- (min, max, avg, std) Available ODs per clients
- (min, max, avg, std) Package size


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
java -jar target/VRPOD.jar --instances.path.default=instances
```

## Cite

Consider citing our paper if used in your own work:

### DOI VRPOD
https://doi.org/10.3390/math9050509

### Bibtex VRPOD
```bibtex
@Article{math9050509,
AUTHOR = {Martín-Santamaría , Raúl and López-Sánchez , Ana D. and Delgado-Jalón , María Luisa and Colmenar , J. Manuel},
TITLE = {An Efficient Algorithm for Crowd Logistics Optimization},
JOURNAL = {Mathematics},
VOLUME = {9},
YEAR = {2021},
NUMBER = {5},
ARTICLE-NUMBER = {509},
URL = {https://www.mdpi.com/2227-7390/9/5/509},
ISSN = {2227-7390},
DOI = {10.3390/math9050509}
}
```


### DOI Autoconfig
Pending review

### Bibtex Autoconfig
Pending review