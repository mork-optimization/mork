# Step-by-step example for the Travelling Salesman Problem (TSP) with MorK

## What is the Travelling Salesman Problem?

The traveling salesman problem (commonly denoted as TSP) asks the following question: "Given a list of cities and the
distances between each pair of cities, what is the shortest possible route that visits each city exactly once and
returns to the origin city?" It is an NP-hard problem in combinatorial optimization, important in theoretical computer
science and operations research.

<p align="center">
    <img src="https://miro.medium.com/max/1838/1*by3MgdkmamEAxlCaIH68Xg.jpeg"  width="500"  alt="Example of the TSP problem"/>
</p>

Obtained
from *[The Trials And Tribulations Of The Traveling Salesman](https://medium.com/basecs/the-trials-and-tribulations-of-the-traveling-salesman-56048d6709d)*
.

## 1. Environment set up

### 1.1 Prerequisites

- Java 11 or more recent is required to run. Please download and install a recent JDK.
- [Maven](https://maven.apache.org/)
- This is not really a prerequisite, but we recommend using an IDE, such as [IntelliJ](https://www.jetbrains.com/idea/)
  😉. (It's the IDE used in the development of Mork)

> 💡 *Tip:* Have you ever use SDKMAN to easily manage your JDKs and SDKs?. Have a look to the official *[web page](https://sdkman.io/)*.

### 1.2 Download problem  instances

In order to test the proposed algorithms for the TSP we will use the standard instances for the problem, that can be
easily obtained from *[TSPLIB](http://elib.zib.de/pub/mp-testdata/tsp/tsplib/tsplib.html)*.
*[TSPLIB](http://elib.zib.de/pub/mp-testdata/tsp/tsplib/tsplib.html)* is a library of sample instances for the TSP
(and related problems) from various sources and of various types. Particularly, we will use
the *[TSPLIB Symmetric Traveling Salesman Problem Instances](http://elib.zib.de/pub/mp-testdata/tsp/tsplib/tsp/index.html)*
. For the moment, you will only need to download the following
files: ([berlin52](http://elib.zib.de/pub/mp-testdata/tsp/tsplib/tsp/berlin52.tsp)
, [eil101](http://elib.zib.de/pub/mp-testdata/tsp/tsplib/tsp/eil101.tsp),
[ch130](http://elib.zib.de/pub/mp-testdata/tsp/tsplib/tsp/ch130.tsp),
[st70](http://elib.zib.de/pub/mp-testdata/tsp/tsplib/tsp/st70.tsp),
and [a280](http://elib.zib.de/pub/mp-testdata/tsp/tsplib/tsp/a280.tsp). Have a quick look at the structure of the files.

### 1.3 Using the quick start project generator

Go to the *[https://rmartinsanta.github.io/mork/](https://rmartinsanta.github.io/mork/)*, set "TSP" as the Project Name
and generate your project. After less than a minute you will be able to download your project.  
Then, extract the zip file and import it your favorite IDE.


> 💡 *Tip:* Some IDEs, allow you to select the `pom.xml` file when you select the option to import an existing project. If such a possibility exists, we highly recommend **importing the project as a maven project**. Indeed, most of them have it. Some examples of how to import a [Maven](https://maven.apache.org/) project in the most important editors can be found in: [Eclipse](https://www.eclipse.org/m2e/), [IntelliJ](https://www.jetbrains.com/help/idea/maven-importing.html), or [NetBeans](https://netbeans.apache.org/wiki/MavenBestPractices.asciidoc).



For your future project, remember to choose a good name, i.e., check that it starts with an Uppercase letter followed by
any alphanumeric characters or underscores, without spaces.

### 1.4 A quick look

The project is organized in the following folders

- 📁 **.run**

If you are using  [IntelliJ](https://www.jetbrains.com/idea/), you might have noticed that there are two default
configuration files: `Performance.run.xml` and `Validation.run.xml`. On the one hand, the performance run configuration
corresponds to a normal execution of the framework. On the other hand, the validation run configuration has the
assertion enables (don't you know what assertion or assert is? Have a look to [Testing in MorK section]()). In this
case, as soon as an assertion is not true, an exception will be thrown. Anyway, to configure your own run configuration,
the main class of MorK is located at `es.urjc.etsii.grafo.TSP.Main`.

- 📁 **docker** :
- 📁 **instances**

This folder should contain everything related with the instances of the problem. In this case, since we are tackling the
TSP, this folder might contain TSP instances. Therefore, you should locate the download instances (*.tsp files) at
*[TSPLIB Symmetric Traveling Salesman Problem Instances](http://elib.zib.de/pub/mp-testdata/tsp/tsplib/tsp/index.html)*
in this folder.

- 📁 **src/main**:
    - 📁 **java/es.urjc.etsii.grafo.TSP**
        - 📁 **algorithms**: contains constructive, local search, metaheuristic, among other procedures.
        - 📁 **experiments**: contains the experiments carried out to test the proposed algorithms and strategies.
        - 📁 **model**: contains the basic elements of the studied problem: solution, instance, etc.
    - 📁 **resources**
        - 📁 **irace**: irace is a software package that implements a number of automatic configuration procedures.
        - 📁 **static**: contains files to generate a localhost web page which allow the researcher to see the
          solution-quality convergence or the best solution found.
        - 📝 application.yml: this file contains the global configuration of the project, such as which experiment
          should be executed, which instances should be used, among others.
- 📝 .gitignore: this file tells Git which files to ignore when committing your project to
  the [GitHub](https://github.com/) repository.
- 📝 pom.xml: contains information of project and configuration information for the maven to build the project such as
  dependencies, build directory, source directory...

## 2. Our first step: reading instances

This MorK project aims to approach the Traveling Salesman Problem (TSP). Given a set of points, (that can be considered
as locations or cities), the TSP consist of find a roundtrip of minimal total length visiting each node exactly once. In
this section you will learn, what an instance is, how to define an instance of the problem, and how to read an instance
from a file.

An instance of a problem is all the inputs needed to compute a solution to the problem. Focusing on the problem at hand,
what is an instance? An instance represents a map of cities or locations, all of them connected to each other. In this
particular problem, all locations are defined by x/y cooridnates.

At this point, you should have downloaded the instance
files ([berlin52.tsp](http://elib.zib.de/pub/mp-testdata/tsp/tsplib/tsp/berlin52.tsp)
, [eil101](http://elib.zib.de/pub/mp-testdata/tsp/tsplib/tsp/eil101.tsp),
[ch130](http://elib.zib.de/pub/mp-testdata/tsp/tsplib/tsp/ch130.tsp),
[st70](http://elib.zib.de/pub/mp-testdata/tsp/tsplib/tsp/st70.tsp),
and [a280](http://elib.zib.de/pub/mp-testdata/tsp/tsplib/tsp/a280.tsp)
and place it at the instance folder of the project.

Have a look to any of those four files. The structure is the same for each of them. Particularly, this files have the
following` <keyword>:<value>` structure. where `<keyword>` denotes an alphanumerical keyword and `<value>` denotes
alphanumerical or numerical data:

- NAME : `<string>` // Identifies the data file.
- TYPE : `<string>` // Specifies the type of the data. In this case will be TSP.
- COMMENT :` <string>` // Additional comments.
- DIMENSION : `<integer>` // Number of its nodes (cities, locations, etc.)
- EDGE WEIGHT TYPE : `<string>` // Specifies how the edge weights (or distances) are given.
- NODE COORD SECTION :   `<integer> <real> <real>` // Node coordinates are given in this section.

Notice that the five instances selected have EDGE WEIGHT TYPE = EUC_2D, which means that the distance between two points
_i_ and _j_ is computed as follows: [_√[(ix – jx)2 + (iy – jy)2]_](https://en.wikipedia.org/wiki/Euclidean_distance).

If you are interested in a deep description of the instances to test the proposed algorithm with other type of instance,
have a look to the [TSPLIB documentation](http://comopt.ifi.uni-heidelberg.de/software/TSPLIB95/tsp95.pdf).

Then, open file `TSPInstance.java` located in `src/main/java/es/urjc/etsii/grafo/TSP/model`. This class will represent
an instance of the problem, i.e., a list of x/y coordinates. Therefore, we will carry out the following task:

1. Define an structure represent a 2D coordinate.
2. Define a parameter  (in `TSPInstance` class) that represent list of locations.
3. Update the constructive to `TSPInstance(String name, Coordinate[] locations)`.

Try yourself, and compare with our code (obviously more than one implementation is possible, everyone thinks different).

The resultant class will be the following:

```
public class TSPInstance extends Instance {

  /**
    * List of coordinates
    */ 
  private final Coordinate[] locations;

  /**
    * Constructor
    *
    * @param name name of the instance
    * @param locations list of coordiantes
    */ 
  protected TSPInstance(String name, Coordinate[] locations) { 
    super(name); 
    this.locations = locations; 
  }

  /**
    * Get the list of locations
    *
    * @return list of locations
    */ 
  public Coordinate[] getLocations() { return locations; }

  /**
    * 2D coordinate
    */ 
  public record Coordinate(double x, double y) { }
  
}
```

> 💡 _Tip_: is this the first time you have come across record? You don't know what you're missing!! Record classes, which are a special kind of class, help to model plain data aggregates with less ceremony than normal classes. Have a look to the [Java documentation](https://docs.oracle.com/en/java/javase/16/language/records.html) abut record classes.

---

**⚠️IMPORTANT : after calling the constructor of the instance, i.e., the instance is defined and generated, it MUST BE
IMMUTABLE.**

---


Next, lets move on to the `TSPInstanceImporter.java` file. This class aims to generate an instance of the problem given
a text file. To this end, we will need to implement the method: `importInstance(BufferedReader reader, String filename)`
. This method receives as input parameters the buffer reader, managed by the framework and filename. Moreover, it
returns the constructed instance. Considering the file instance structure, we will need to read line by line the file,
storing the list of coordinates, and finally, construct the instance.

### Testing in MorK

#### Asserts

I'm sure you've spent hours in front of your code trying to find that 🤬 bug. For that reason, we consider that it is
important that any operation must be validated, and check that the implemented procedures perform the desired behavior.
And how can this be done in MorK? There are many ways, testing is one way (go to section XXX for an example of Test
implementation in MorK), but in this case, we are talking about **asserts**.

The keyword or reserved word **assert** is used to state that at a certain point in the code a certain condition must be
true. For example, if you write a method that calculates the speed of a particle, you might assert that the calculated
speed is less than the speed of light. Not using it yet? Take a look at
the [official documentation](https://docs.oracle.com/javase/7/docs/technotes/guides/language/assert.html) and start
using it right now. Experience has shown that writing assertions while programming is one of the quickest and most
effective ways to detect and correct bugs. As an added benefit, assertions serve to document the inner workings of your
program, enhancing maintainability. By default, assertions are disabled at runtime. To enable assertions use
the `-enableassertions`, or `-ea`, as a program argument.

