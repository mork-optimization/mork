<p align="center">
<img src="https://user-images.githubusercontent.com/55482385/233611563-4f5c91f2-af36-4437-a4b5-572b6655487a.svg" width="300" align="center" style="margin: 0 auto"/>
</p>

# MORK: Metaheuristic Optimization framewoRK 

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/es.urjc.etsii.grafo/mork/badge.svg?style=square)](https://search.maven.org/artifact/es.urjc.etsii.grafo/mork) 
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=rmartinsanta_mork&metric=bugs)](https://sonarcloud.io/dashboard?id=rmartinsanta_mork) [![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=rmartinsanta_mork&metric=vulnerabilities)](https://sonarcloud.io/dashboard?id=rmartinsanta_mork)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=rmartinsanta_mork&metric=coverage)](https://sonarcloud.io/dashboard?id=rmartinsanta_mork) [![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=rmartinsanta_mork&metric=duplicated_lines_density)](https://sonarcloud.io/dashboard?id=rmartinsanta_mork)


## What
Mork is a framework for developing approaches for NP-Hard problems using the JVM. 
It is currently under heavy development.

## Why use it
The idea of the project is to provide both high quality and tested componentes that can be used as is, and a developing framework to create new metaheuristic approaches for different kind of problems. A non extensive list of its current main benefits are:

- Automatic experiment parallelization
- Automatic results report generation
- Guaranteed reproducibility, even in high concurrency environments, by using the provided RandomManager.
- Can execute anywhere (at least, anywhere where Java and Docker can!). Easily build Docker containers that can execute almost anywhere.
- Automatic benchmarking and optional timings adjustment.
- Nice web interface to visualize solution quality and experiment progress.

https://user-images.githubusercontent.com/55482385/140910473-1fa14244-5ef9-4ec5-9cf6-1139578f4151.mov

- And more!


## Getting started

TLDR: Automatically generate a project using [https://generator.mork-optimization.com/](https://generator.mork-optimization.com/), 
import in your favourite IDE and start working!

See [Getting started page](https://mork-optimization.readthedocs.io/en/latest/quickstart/starting/) in the [Official Documentation](https://mork-optimization.readthedocs.io/en/latest/) for more details.

## Citing

If this project is useful for any research, please consider citing the original paper
https://doi.org/10.1162/evco_a_00317

### Bibtext
```bib 
@article{10.1162/evco_a_00317,
    author = {Martín-Santamaría, Raúl and Cavero, Sergio and Herrán, Alberto and Duarte, Abraham and Colmenar, J. Manuel},
    title = "{A practical methodology for reproducible experimentation: an application to the Double-row Facility Layout Problem}",
    journal = {Evolutionary Computation},
    pages = {1-35},
    year = {2022},
    month = {11},
    abstract = "{Reproducibility of experiments is a complex task in stochastic methods such as evolutionary algorithms or metaheuristics in general. Many works from the literature give general guidelines to favor reproducibility. However, none of them provide both a practical set of steps and also software tools to help on this process. In this paper, we propose a practical methodology to favor reproducibility in optimization problems tackled with stochastic methods. This methodology is divided into three main steps, where the researcher is assisted by software tools which implement state-of-theart techniques related to this process. The methodology has been applied to study the Double Row Facility Layout Problem, where we propose a new algorithm able to obtain better results than the state-of-the-art methods. To this aim, we have also replicated the previous methods in order to complete the study with a new set of larger instances. All the produced artifacts related to the methodology and the study of the target problem are available in Zenodo.}",
    issn = {1063-6560},
    doi = {10.1162/evco_a_00317},
    url = {https://doi.org/10.1162/evco\_a\_00317},
    eprint = {https://direct.mit.edu/evco/article-pdf/doi/10.1162/evco\_a\_00317/2057545/evco\_a\_00317.pdf},
}
```

### Citing artifacts
Artifacts, docs, and source code are automatically archived in Zenodo with each release. See:
[![DOI](https://zenodo.org/badge/223169907.svg)](https://zenodo.org/badge/latestdoi/223169907)
 for more information.

## Contributing

Issues, suggestions or any contribution in general are welcome! 
Before doing any mayor refactor / contribution, get in touch!

The current development version is a Maven project structured in the following modules:

| Module            | Description                                                                                                                           |
|-------------------|---------------------------------------------------------------------------------------------------------------------------------------|
| common            | Basic and fundamental Mork classes / building blocks: algorithms, solutions, instances, helper methods, etc.                          |
| core              | Mork engine: this is where the magic happens. Parallelization, report generation, web interface, dependency injection, and much more! |
| autoconfig        | Automatic configuration procedures that allow you to easily tune algorithms.                                                          |
| template          | Base project used by the web generator to create the project zip files.                                                               |
| example-tsp       | Example project implementation using Mork, the classic Travelling Salesman Problem.                                                   |
| integration-tests | Integration tests used by Mork developers to validate the whole framework interactions and behaviour.                                 |


Remember depending on your needs when developing approaches using Mork you may easily add/remove module dependencies in the `pom.xml` file.


## Powered by Mork

Below is a list of repositories leveraging Mork to tackle optimization problems.

- [An Efficient Fixed Set Search for the Covering Location with Interconnected Facilities Problem (I. Lozano-Osorio, J. Sánchez-Oro, A. Martínez-Gavara, AD. López-Sánchez, and A. Duarte, 2023)](https://grafo.etsii.urjc.es/CPIF-MIC). [Link to the published paper](https://doi.org/10.1007/978-3-031-26504-4_37).
- Multi-Round Influence Maximization: A Variable Neighborhood Search Approach (I. Lozano-Osorio, J. Sánchez-Oro, and A. Duarte, 2023). [Link to the published paper](https://doi.org/10.1007/978-3-031-34500-5_9).
- [Dynamic Path Relinking for the Target Set Selection problem (I. Lozano-Osorio, A. Oliva-García, and J. Sánchez-Oro, 2023)](https://grafo.etsii.urjc.es/TSS). [Link to the published paper](https://doi.org/10.1016/j.knosys.2023.110827).
- [An efficient and effective GRASP algorithm for the Budget Influence Maximization Problem (I. Lozano-Osorio, J. Sánchez-Oro, and A. Duarte 2023)](https://grafo.etsii.urjc.es/BIMP). [Link to the published paper](https://doi.org/10.1007/s12652-023-04680-z).   
- [Strategic oscillation tabu search for improved hierarchical graph drawing (S. Cavero, E. G. Pardo, F. Glover, and R. Martí, 2024)](https://github.com/scaverod/SOS-TS-GraphDrawing). [Link to the published paper](https://doi.org/10.1016/j.eswa.2023.122668). 
