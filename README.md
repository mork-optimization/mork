# MORK: Metaheuristic Optimization framewoRK 


[![DOI](https://zenodo.org/badge/223169907.svg)](https://zenodo.org/badge/latestdoi/223169907)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/es.urjc.etsii.grafo/mork/badge.svg?style=square)](https://search.maven.org/artifact/es.urjc.etsii.grafo/mork) 
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=rmartinsanta_mork&metric=bugs)](https://sonarcloud.io/dashboard?id=rmartinsanta_mork) [![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=rmartinsanta_mork&metric=vulnerabilities)](https://sonarcloud.io/dashboard?id=rmartinsanta_mork)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=rmartinsanta_mork&metric=coverage)](https://sonarcloud.io/dashboard?id=rmartinsanta_mork) [![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=rmartinsanta_mork&metric=duplicated_lines_density)](https://sonarcloud.io/dashboard?id=rmartinsanta_mork)

## What
Mork is a framework for developing approaches for NP-Hard problems using the JVM. 
It is currently under heavy development.

## Why use it
The idea of the project is to provide both high quality and tested componentes that can be used as is, and a developing framework to create new metaheuristic approaches for different kind of problems. A non extensive list of its current main benefits are:
- Automatic experiment paralelization
- Automatic results report generation
- Guaranteed reproducibility, even in high concurrency environments, by using the provided RandomManager.
- Can execute anywhere (at least, anywhere where Java and Docker can!). Easily build Docker containers that can execute almost anywhere.
- Automatic benchmarking and optional timings adjustment.
- Nice web interface to visualize solution quality and experiment progress.

https://user-images.githubusercontent.com/55482385/140910473-1fa14244-5ef9-4ec5-9cf6-1139578f4151.mov

- And more!



## Getting started

TLDR: Automatically generate a project using [https://rmartinsanta.github.io/mork/](https://rmartinsanta.github.io/mork/), 
import in your favourite IDE and start working!

See [Getting started page](https://mork-optimization.readthedocs.io/en/latest/quickstart/starting/) in the [Official Documentation](https://mork-optimization.readthedocs.io/en/latest/) for more details.


What functionality do you need? Whether you want everything or just a specific Mork functionality, take a look at the table below and determine what you need. 

The current development version (0.15) is a Maven project structured in the following modules:

| Module     | Description                                                                                        |
|------------|----------------------------------------------------------------------------------------------------|
| core       | This is where the magic happens: parallelization, report generation, web interface, and much more! |
| common     | Basic and fundamental mork classics: algorithms, solutions, instances, and so on.                  |
| autoconfig | Automatic configuration procedures that allow you to easily tune algorithms.                       |


Determine what your needs are and easily add/remove your dependencies in the `pom.xml` file.
