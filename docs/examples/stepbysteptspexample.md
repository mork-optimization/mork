# Step-by-step example for the Travelling Salesman Problem (TSP) with MorK


## What is the Travelling Salesman Problem?
The traveling salesman problem (commonly denoted as TSP) asks the following question: "Given a list of cities and the 
distances between each pair of cities, what is the shortest possible route that visits each city exactly once and returns 
to the origin city?" It is an NP-hard problem in combinatorial optimization, important in theoretical computer science
and operations research.

<p align="center">
    <img src="https://miro.medium.com/max/1838/1*by3MgdkmamEAxlCaIH68Xg.jpeg"  width="500"  alt="Example of the TSP problem"/>
</p>

Obtained from *[The Trials And Tribulations Of The Traveling Salesman](https://medium.com/basecs/the-trials-and-tribulations-of-the-traveling-salesman-56048d6709d)*.




## 0. Environment set up


### Prerequisites


### Problem  instances
To test the algorithm the instance from *[TSPLIB](http://elib.zib.de/pub/mp-testdata/tsp/tsplib/tsplib.html)* will be used.
*[TSPLIB](http://elib.zib.de/pub/mp-testdata/tsp/tsplib/tsplib.html)* is a library of sample instances for the TSP 
(and related problems) from various sources and of various types. Particularly, we will use the *[TSPLIB Symmetric Traveling Salesman Problem Instances](http://elib.zib.de/pub/mp-testdata/tsp/tsplib/tsp/index.html)*. 




## 1. Using the quick start project generator

Visit the following website, fill the gaps and click Download project.
https://rmartinsanta.github.io/mork/

Extract the zip and import in your favorite IDE.

## Manual

1. Create a new empty Maven project
2. Configure pom.xml, use the following as a template:
```XML
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <!-- Project name and current version, replace -->
    <artifactId>projectname</artifactId>
    <version>1.0-SNAPSHOT</version>

    <!-- Automatic configuration, individual properties can be overridden -->
    <parent>
        <groupId>es.urjc.etsii.grafo</groupId>
        <artifactId>mork-parent</artifactId>
        <version>0.1</version> <!-- Use latest available release if possible! -->
    </parent>
</project>
```

3. Create your Instance and Solution classes, implement mandatory methods.
> Tip: IDE code generation features can be used to create toString(), equals() and hashcode() methods when required.

4. Create an Instance importer or use one of the existing ones. Example:
```java

```

5. Create and run your first experiment!

Check algorithms documentation on the following wiki page: TODO.


(OPTIONAL) Implement a solution validator: A solution validator will be used to verify that all solutions are valid after the algorithm finishes executing. Can be really useful to catch invalid solutions due to bugs / implementation mistakes.

(OPTIONAL) Implement a solution exporter: If enabled in the configuration file, the custom solution exporter will be used to write best solutions to disk using a custom format. Check exporting solution data wiki page: TODO.

(OPTIONAL) Implement a ReferenceResultsProvider: If there are known previous values for instances, you may provide them, no matter if they are optimal values or not. Values, if provided, are used when calculating several metrics (%dev to best result, number of best results, etc.) The frontend may use it too for several graphs as a reference point to compare different approaches.
