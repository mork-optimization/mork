# [Autoconfig: BMSSC](https://doi.org/XXXXXX)

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
- Jesus Sánchez-Oro Calvo
- Sergio Pérez Peló
- Abraham Duarte Muñoz

## Datasets

All instances are from the [UCI Machine Learning Repository](https://archive.ics.uci.edu/ml/index.php)

## Instance format
First line contains 3 numbers (nPoints,nDimensions,nClusters), which corresponds, respectively,
with the number of points in the instance, the point dimensionality and the number of clusters in which to classify the points.
The next N lines, contains the point data for each dimension, separated by commas.

## Properties used for instance classification and selection
3 instances have been removed because they are too complex to be solved in less than 1 minute by all approaches: 

- n: number of points
- k: number of clusters
- d: number of dimensions
- Number of clusters with size != n/k, only happens when N is not divisible by k
- (min, max, avg, std) distance between points

## Cite
Consider citing our original BMSSC paper if used in your own work, or the Autoconfig proposal

### DOI BMSSC
https://doi.org/10.1016/j.ins.2021.11.048

### Bibtex BMSSC
```bibtex
@article{MARTINSANTAMARIA2022529,
title = {Strategic oscillation for the balanced minimum sum-of-squares clustering problem},
journal = {Information Sciences},
volume = {585},
pages = {529-542},
year = {2022},
issn = {0020-0255},
doi = {https://doi.org/10.1016/j.ins.2021.11.048},
url = {https://www.sciencedirect.com/science/article/pii/S0020025521011701},
author = {R. Martín-Santamaría and J. Sánchez-Oro and S. Pérez-Peló and A. Duarte},
keywords = {Balanced clustering, Metaheuristics, Strategic oscillation, GRASP, Infeasibility},
abstract = {In the age of connectivity, every person is constantly producing large amounts of data every minute: social networks, information about trips, work connections, etc. These data will only become useful information if we are able to analyze and extract the most relevant features from it, which depends on the field of analysis. This task is usually performed by clustering data into similar groups with the aim of finding similarities and differences among them. However, the vast amount of data available makes traditional analysis obsolete for real-life datasets. This paper addresses the problem of dividing a set of elements into a predefined number of equally-sized clusters. In order to do so, we propose a Strategic Oscillation approach combined with a Greedy Randomized Adaptive Search Procedure. The computational experiments section firstly tunes the parameters of the algorithm and studies the influence of the proposed strategies. Then, the best variant is compared with the current state-of-the-art method over the same set of instances. The obtained results show the superiority of the proposal using two different clustering metrics: MSE (Mean Square Error) and Davies-Bouldin index.}
}
```

### DOI Autoconfig
Pending review

### Bibtex Autoconfig
Pending review