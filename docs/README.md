# MORK: Metaheuristic Optimization framewoRK

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/es.urjc.etsii.grafo/mork/badge.svg?style=square)](https://search.maven.org/artifact/es.urjc.etsii.grafo/mork) [![Bugs](https://sonarcloud.io/api/project_badges/measure?project=rmartinsanta_mork&metric=bugs)](https://sonarcloud.io/dashboard?id=rmartinsanta_mork) [![Coverage](https://sonarcloud.io/api/project_badges/measure?project=rmartinsanta_mork&metric=coverage)](https://sonarcloud.io/dashboard?id=rmartinsanta_mork) [![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=rmartinsanta_mork&metric=vulnerabilities)](https://sonarcloud.io/dashboard?id=rmartinsanta_mork) [![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=rmartinsanta_mork&metric=duplicated_lines_density)](https://sonarcloud.io/dashboard?id=rmartinsanta_mork)

## What
Mork is a framework for developing approaches for optimisation problems using the JVM, specially those considered challenging for traditional and exact methods, called [NP Hard Problems](https://en.wikipedia.org/wiki/NP-hardness).

Example problems: Traveling Salesman Problem (TSP), Subset Sum Problem, Scheduling Problems, Facility Distribution Problems, etc.


## Why use it
The idea of the project is twofold: provide both high quality and tested componentes that can be used as is, and a developing framework to create new metaheuristic approaches for different kind of problems. A non extensive list of its current main benefits are:

- Automatic experiment parallelization
- Automatic results report generation
- Guaranteed reproducibility, even in high concurrency environments, by using the provided RandomManager.
- Can execute anywhere (at least, anywhere where Java and Docker can!). Easily build Docker containers that can execute almost anywhere.
- Automatic benchmarking and optional timings adjustment.
- Nice web interface to visualize solution quality and experiment progress.

<video controls="true" width="100%">
  <source src="https://user-images.githubusercontent.com/55482385/140910473-1fa14244-5ef9-4ec5-9cf6-1139578f4151.mov" type="video/quicktime">
</video>

- And more!

## Getting started

TLDR: Go to [https://rmartinsanta.github.io/mork/](https://rmartinsanta.github.io/mork/), import in your favourite IDE (IntelliJ recommended) and start working.

See Getting started page in the Wiki for more details.
