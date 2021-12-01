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
*[TSPLIB](http://elib.zib.de/pub/mp-testdata/tsp/tsplib/tsplib.html)* is a library of sample instances for the TSP  (and
related problems) from various sources and of various types. Particularly, we will use
the *[TSPLIB Symmetric Traveling Salesman Problem Instances](http://elib.zib.de/pub/mp-testdata/tsp/tsplib/tsp/index.html)*
. For the moment, you will only need to download the following
files: [berlin52](http://elib.zib.de/pub/mp-testdata/tsp/tsplib/tsp/berlin52.tsp)
, [eil101](http://elib.zib.de/pub/mp-testdata/tsp/tsplib/tsp/eil101.tsp)
,  [ch130](http://elib.zib.de/pub/mp-testdata/tsp/tsplib/tsp/ch130.tsp)
, [st70](http://elib.zib.de/pub/mp-testdata/tsp/tsplib/tsp/st70.tsp),
and [a280](http://elib.zib.de/pub/mp-testdata/tsp/tsplib/tsp/a280.tsp). Have a quick look at the structure of the files.

### 1.3 Using the quick start project generator

Go to the *[https://rmartinsanta.github.io/mork/](https://rmartinsanta.github.io/mork/)*, set "TSP" as the ProjectName
and generate your project. After less than a minute you will be able to download your project. Then, extract the zip
file and import it your favorite IDE.


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

This folder should contain everything related with the instances of the problem. In this case, since we are tackling
the  
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
    - 📝 application.yml: this file contains the global configuration of the project, such as which experiment should be
      executed, which instances should be used, among others.
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
particular problem, all locations are defined by x/y coordinates.

At this point, you should have downloaded the instance
files ([berlin52.tsp](http://elib.zib.de/pub/mp-testdata/tsp/tsplib/tsp/berlin52.tsp)
, [eil101](http://elib.zib.de/pub/mp-testdata/tsp/tsplib/tsp/eil101.tsp)
, [ch130](http://elib.zib.de/pub/mp-testdata/tsp/tsplib/tsp/ch130.tsp)
, [st70](http://elib.zib.de/pub/mp-testdata/tsp/tsplib/tsp/st70.tsp),and [a280](http://elib.zib.de/pub/mp-testdata/tsp/tsplib/tsp/a280.tsp)
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

Notice that the five instances selected have EDGE WEIGHT TYPE = EUC_2D, which means that the distance between two
points  
_i_ and _j_ is computed as follows: [_√[(ix – jx)2 + (iy – jy)2]_](https://en.wikipedia.org/wiki/Euclidean_distance).

If you are interested in a deep description of the instances to test the proposed algorithm with other type of
instance,  
have a look to the [TSPLIB documentation](http://comopt.ifi.uni-heidelberg.de/software/TSPLIB95/tsp95.pdf).

Then, open file `TSPInstance.java` located in `src/main/java/es/urjc/etsii/grafo/TSP/model`. This class will represent  
an instance of the problem, i.e., a list of x/y coordinates. Therefore, we will carry out the following task:

- Define an structure represent a 2D coordinate.
- Define an attribute (in `TSPInstance` class) that represent list of locations and the distance of locations.
- Implement the class constructor and getter methods.

Try yourself, and compare with our code (obviously more than one implementation is possible, everyone thinks different).

The resultant class will be the following:

```  
public class TSPInstance extends Instance {  
  
  /**  
   * List of coordinates 
   */  
   private final Coordinate[] locations;  
  
  
  /**  
   * Distance between all coordinates 
   */  
   private final double[][] distances;  
  
  /**  
   * Constructor 
   * @param name name of the instance  
   * @param locations list of coordiantes  
   */  
   protected TSPInstance(String name, Coordinate[] locations, double[][] distances) {  
      super(name);  
      this.locations = locations;  
      this.distances = distances;  
  }  
   
  /**  
   * Get the list of locations 
   * @return list of locations  
   */  
   public Coordinate[] getLocations() {  
      return locations;  
  }  
  
  /**  
   * Get the number of locations of the instance 
   * @return number of locations  
   */  
   public int numberOfLocations() {  
      return locations.length;  
   }  
  
  /**  
   * 2D coordinate 
   */  
   public record Coordinate(double x, double y) {}  
  
  
  /**  
   * Get coordinate of a specific location (that represents a city, place, facility...) * * @param id of the location  
   * @return the location coordinate  
   */  
   public Coordinate getCoordinate(int id) {  
      return this.locations[id];  
  }  
  
  
  /**  
   * Return the euclidean distance between two locations i and j. * * @param i first location  
   * @param j second location  
   * @return the euclidean distance  
   */
   public double getDistance(int i, int j) {  
      return this.distances[i][j];  
   }  
   
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
storing the list of coordinates, and distance between each pair of coordinates, and finally, construct the instance. The
resultant class will be the following:

```
public class TSPInstanceImporter extends InstanceImporter<TSPInstance> {

    @Override
    public TSPInstance importInstance(BufferedReader reader, String filename) throws IOException {
        Scanner sc = new Scanner(reader).useLocale(Locale.US);
        String name = sc.nextLine().split(":")[1].trim();
        String type = sc.nextLine().split(":")[1];
        String comment = sc.nextLine().split(":")[1];
        int dimension = Integer.parseInt(sc.nextLine().split(":")[1].trim());
        String edgeWeightType = sc.nextLine().split(":")[1];
        String nodeCoordSection = sc.nextLine();
        TSPInstance.Coordinate[] locations = new TSPInstance.Coordinate[dimension];
        while (!sc.hasNext("EOF")) {
            int id = sc.nextInt() - 1;
            double x = sc.nextDouble();
            double y = sc.nextDouble();
            locations[id] = new TSPInstance.Coordinate(x, y);
        }
        double[][] distances = getMatrixOfDistances(locations);
        return new TSPInstance(name, locations, distances);
    }


    /**
     * Calculate all euclidean distances between all locations
     *
     * @param locations list of locations
     * @return a matrix of distances
     */
    private double[][] getMatrixOfDistances(TSPInstance.Coordinate[] locations) {
        var dimension = locations.length;
        double[][] distances = new double[dimension][dimension];
        for (int i = 0; i < dimension; i++) {
            for (int j = i+1; j < dimension; j++) {
                var distance = this.calculateEuclideanDistance(locations[i], locations[j]);
                distances[i][j] = distance;
                distances[j][i] = distance;
            }
        }
       return distances;
    }


    /**
     * Calculate the Euclidian distance of two given coordinates
     *
     * @param i first coordinate
     * @param j second coordinate
     * @return the euclidean distance between two coordiantes
     */
    public double calculateEuclideanDistance(TSPInstance.Coordinate i, TSPInstance.Coordinate j) {
        var di = i.x() - j.x();
        var dj = i.y() - j.y();
        return Math.sqrt((di * di) + (dj * dj));
    }
}
```

## 3. Defining a solution of the problem

The next task is to define a TSP solution. In this case, a solution corresponds to a circular path passing through each
of the instance locations. To define a solution in Mork we must edit the `TSPSolution` class. The objects of this class
represent the candidate solutions that are handled throughout the optimization algorithm developed. The simplest
implementation of a circular path is through an array of integers, where the index represents the order (or position) in
the path of the location (identified by the ID) referenced by that array index. It should be noted that an array type
structure is very appropriate for this purpose, since it allows direct access to its components. An efficient
implementation must take this type of details into account. In addition to this array, we will define another class
attribute that represents the objective function value of the solution, i.e., the distance of the route.

    private double distance;  
 
    private final int[] route;

The main methods of the `TSPSolution` class are the following:

### Constructors

By default, two constructors must be implemented. The first one, initialize a solution given an instance. The second one
initialize a solution given another solution. For example:

```
public TSPSolution(TSPInstance ins) {  
  super(ins);  
  this.route = new int[ins.numberOfLocations()];  
  Arrays.fill(route, -1);  
}
    
public TSPSolution(TSPSolution s) {  
   super(s);  
   this.route = s.route.clone();  
   this.distance = s.distance;  
}
```

### Objective function methods

Therefore, it will also needed a method to calculate the objective function of the solution. In Mork, there are to main
procedures to manage the objective function of the solution:

- `public double getScore()` : get the objective function of the solution. This procedure is like a getter method, it
  does not perform any calculation.
- `public double recalculateScore()`: recalculate solution objective function. **MAKE SURE THIS METHOD DOES NOT HAVE
  SIDE EFFECTS**

```  
public double getScore() {  
   return this.distance;  
}

public double recalculateScore() {  
   double distance = 0;  
   for (int i = 0; i < this.route.length; i++) {  
      var j = (i + 1) % this.route.length;  
      distance += this.getInstance().getDistance(route[i], route[j]);  
  }  
  return distance;  
}
 ```

### Moving methods

The solution class must have the necessary methods to operate and edit a solution. In this case, since it is a path,
methods to exchange the order of two locations (classic interchange movement), to establish a position in the path of a
position (classic interchange movement), or even to randomize a path make sense for the TSP.

```    
   /**  
    * Shuffle route 
    */
    public void shuffleRoute() {  
       ArrayUtil.shuffle(route);  
    }  
      
      
    /**  
     * Swap classical move: 
     * Swap the position in the route of two locations, given its actual positions. 
     * Example: actual route : [a,b,c,d,e,f], pi = 0,  pj= 1, resultant route= [b,a,c,d,e,f] 
     * Example: actual route : [a,b,c,d,e,f], pi = 1,  pj= 4, resultant route= [a,e,c,d,b,f] 
     * When the operation is performed, the objective function (this.distance) is updated
     * @param pi actual position of the location  
     * @param pj desired position  
     */
     public void swapLocationOrder(int pi, int pj) {  
        var i = this.route[pi];  
        var j = this.route[pj];  
        this.distance = this.distance - getDistanceContribution(pi) - getDistanceContribution(pj);  
        this.route[pi] = j;  
        this.route[pj] = i;  
        this.distance = this.distance + getDistanceContribution(pi) + getDistanceContribution(pj);  
    }  
      
      
    /**  
     * Insert classical move: 
     * Deletes a location from and array (given its position) and inserts it in the specified position. 
     * Example: actual route : [a,b,c,d,e,f], pi = 0,  pj= 1, resultant route= [b,a,c,d,e,f] 
     * Example: actual route : [a,b,c,d,e,f], pi = 1,  pj= 4, resultant route=[a,c,d,e,b,f] 
     * Example: actual route : [a,b,c,d,e,f], pi = 5   pj= 3, resultant route= [a,b,c,f,d,e] 
     * When the operation is performed, the objective function (this.distance) is updated * 
     * @param pi actual position of the location  
     * @param pj desired position  
     */
     public void insertLocationAtPiInPj(int pi, int pj) {  
        ArrayUtil.deleteAndInsert(this.route, pi, pj);  
        this.distance = this.recalculateScore();  
    }
```

When working with arrays we strongly recommend having a look at the class `ArrayUtil` that contains a wide variety of
efficient procedures.

## 4. Our first algorithms and experiments

In this section we will generate our first solutions for the TSP. To do so, we will perform the following tasks:

1. Implement a constructive that generates random solutions.
2. Define an experiment.
3. Run MorK: understanding the application.yml, the web interface and results.

### Constructive procedures

Constructive procedures are methods that generate solutions to a problem. To implement a constructive we are going to
use as an example the constructive procedure located in the 'constructives' folder. Every constructive proposed for the
TSP must extend the `Constructive<TSPSolution, TSPInstance>`.

The simplest implementation of a randomized construct is shown below:

### Define an experiment

Once the construct has been defined, let's define an experiment. Each of the experiments to be executed for the TSP must
be located in the 'experiments' folder and must extend the `AbstractExperiment<TSPSolution, TSPInstance>` class. To
define an experiment it is necessary to implement the method `getAlgorithms()`; which returns a list of algorithms. In
this case we are only interested in testing a simple algorithm, a constructive procedure. Therefore, the resulting
experiment would look like this:

### Run MorK

To run MorK it is necessary to configure previously the run parameters. To do so, go to application.yml file, located at
src/main/resources/. This file contains a list of well-documented properties. In this case we are going to focus just on
some of them:

- instances: in this property the path of instances should be indicated. It is possible to indicate a path for each
  experiment. In this case we set `default: 'instances'`.
- maximizing: since the TSP is a minimization optimization problem, maximizing is set to false:  ` maximizing: false`.
- experiments: this property determines which experiment or experiments should be executed. To do so, you should define
  a regex expression. However, for a single experiment execution, just specify the class
  name:   `experiments: 'ConstructiveExperiment'`. Have a look to the rest of configuration parameters and feel free to
  change whatever you want.

Then, you are able to run MorK. You will see a lot of text and numbers in the console, don't worry, you can analyze them
carefully when the program finishes, it is not difficult to understand. While the algorithm is running, go
to: *[http://localhost:8080/](http://localhost:8080/)*. In that website, you will be able to visualize the convergence
chart and the actual value chart for each of the instance executed. In addition, you will be able to visualize the best
solution found (as soon as you learn how to do it).

> 💡 _Tip_: when all experiments finish, the web server stops. Therefore, to maintain the server alive, set: `event->webserver-> stopOnExecutionEnd: false ` in the application.yml file.

When the execution ends, go to the result folder and check that an excel file (*.xlxs) has been correctly generated. The excel file contains two sheets: a summary of the results and raw results. 
Particularly, the summary file should report the following data (exactly the same, MorKs' experiments are fully reproducible)

|                     | ScTSPRandomConstructivei |                |         |                |
|---------------------|--------------------------|----------------|---------|----------------|
| Etiquetas   de fila | Min. score               | Sum Total T(s) | hasBest | Min. %Dev2Best |
| a280                | 31810.10976              | 0.0286772      | 1       | 0              |
| berlin52            | 25944.86163              | 0.002271       | 1       | 0              |
| ch130               | 42393.82045              | 0.0022626      | 1       | 0              |
| eil101              | 3094.175908              | 0.0024924      | 1       | 0              |
| st70                | 3285.619063              | 0.0015071      | 1       | 0              |

###  Local Searches
In this section you will be able to implement local search procedures and define more complex experiments.
A local search algorithm starts from a candidate solution and then iteratively moves to a neighbor solution.  As an example, we will define to classical neighborhood based on the swap and insert movement. To this end, we will perform the following tasks:

1. Implement a neighborhood structure.
2. Implement the insert/swap operator.
3. Define a Local Search experiment.


####  Implement a neighborhood structure.
A neighborhoods represents all potential solutions that can be reached for a given solution applying a movement.
In MorK there are two types of neighborhoods:
1. Eager Neighborhood: Movements in this neighborhood are generated at once, using List<> of EagerMoves.
2. Lazy Neighborhood:  Movements in this neighborhood are generated lazily under demand using Streams with LazyMoves.


**Eager Neighborhood**
To explain  Eager Neighborhoods we are going to use the Insert classical move as an example. The insert operator consist in removing a location from the route and insert it between other two locations (i.e., insert it at a specific position).

Have a look to the example depicted in the figure above. The location with ID=7 has been removed from the route, and it is wanted to insert it between locations 2 and 3. The resultant route after the insertion is shown in the second array.
![insert](https://images.saymedia-content.com/.image/c_limit,cs_srgb,q_auto:eco,w_609/MTc0NDYxNTczNzExMDEzMjI0/c-standard-list-insert-examples.webp)

Given the insert operator, the neighborhood is defined as all possible insertions of all locations in any position of the route. To this end, we first create a class named: `InsertNeighborhood` that must extend `EagerNeighborhood<InsertNeighborhood.InsertMove, TSPSolution, TSPInstance>`, and where `InsertNeighborhood.InsertMove`is the insert move operator we also have to define.
Once the header of the class has been defined, next task will be to implement the method `public List<InsertMove> getMovements(TSPSolution solution)`, This procedure will generate all possible insert moves given a solution (i.e., insert all location in each of the positions of the route).  A straightforward implementation is shown below:

    public List<InsertMove> getMovements(TSPSolution solution) {  
      List<InsertMove> list = new ArrayList<>();  
     for (int i = 0; i < solution.getInstance().numberOfLocations(); i++) {  
      for (int j = 0; j < solution.getInstance().numberOfLocations(); j++) {  
      list.add(new InsertMove(solution, i, j));  
      }  
     }  return list;  
    }

Next task is to implement the Insert move: `public static class InsertMove extends EagerMove<TSPSolution, TSPInstance>`. Notice that this class has been nested in `InsertNeighborhood` class. As you may have noticed, the constructor of an insert move receive tree parameters: the solution and two integers: the position in the route of the location to insert in a desired position.
Regardless of the type of movement intended (Eager or Lazy), the following methods have to be implemented:
- `boolean isValid()`: true if the solution obtained after the move is feasible
- `void execute()`: execute the move, the procedure changes the solution
-  `double getValue()`: this procedure calculates the difference between the value of the solution that would be obtained if the movement were carried out, and the value of the current target solution. This method does NOT perform the movement, the solution (and its structures) do not change.
-  `boolean improves()` : returns true if applying the move results in a better solution than the current one.

The easiest implementation of this class is depicted below.

    public InsertMove(TSPSolution solution, int pi, int pj) {  
      super(solution);  
     this.pi = pi;  
     this.pj = pj;  
    }  
      
    public boolean isValid() {  
      return true;  
    }  
      
    protected void _execute() {  
      this.getSolution().insertLocationAtPiInPj(pi, pj);  
    }  
      
    public double getValue() {  
      var s = this.getSolution().cloneSolution();  
      s.insertLocationAtPiInPj(pi, pj);  
     return s.getScore() - this.getSolution().getScore();  
    }
    
    public boolean improves() {  
      return DoubleComparator.isLessThan(this.getValue(), 0);  
    }

In this example, `getValue()` performed the insert move in a cloned solution of the current one. Then it returns the difference in the objective function value between the cloned one (the neighbor solution) and the current one. This procedure is extremely inefficient. An efficient way to perform this calculation will evaluate just the part of the solution that has changed after the move. We depict a more efficient approach in the swap move example.

**Lazy Neighborhood**
Movements in this neighborhood are generated lazily under demand using [`Streams`](https://docs.oracle.com/javase/8/docs/api/java/util/stream/Stream.html) with `LazyMoves`. In this neighborhood we will need to build an exhaustive stream to iterate over it.
We will use the classical swap move operator to define a Lazy Neighborhood. This move, exchange the position in the route of two locations, and can be easily explained thru the following picture.

![enter image description here](https://raw.githubusercontent.com/nickbalestra/nickbalestra.github.io/master/assets/images/swap-in-place.png)

The main difference between this neighborhood and the previous one is the way in which the movements are defined. In this case, instead of being a list of movements, it is a Stream. The idea of this neighborhood is that given a movement a next movement can be generated (if it exists). In this way, movements are only generated if they are needed. How to do it?
First, we generate the Swap Neighborhood class (`SwapNeighborhood extends LazyNeighborhood<SwapNeighborhood.SwapMove, TSPSolution, TSPInstance>`) and implement the stream method. This  method generate an initial `SwapMove` object.

    public Stream<SwapMove> stream(TSPSolution solution) {  
      int initialVertex = RandomManager.getRandom().nextInt(solution.getInstance().numberOfLocations());  
     return buildStream(new SwapMove(solution, initialVertex, initialVertex, (initialVertex + 1) % solution.getInstance().numberOfLocations()));  
    }

Again, the `SwapMove` class is a nested class in `LazyNeighborhood` class. The main difference between Lazy moves and Eager moves is that in Lazy Moves the method `LazyMove<TSPSolution, TSPInstance> next()` must be implemented (in addition to all previous detailed methods). This method is in charge of generate next move in the stream sequence. Given a move, it generates the next move if exists, or null otherwise.
In this particular example we would like to swap pair of locations of the instance. Notice that the swap between a location A and B is equal to the swap between B and A. Therefore, this procedure should avoid generating already visited moves.  Our proposed procedure is depicted next:

    public LazyMove<TSPSolution, TSPInstance> next() {  
      var nextPj = (pj + 1) % s.getInstance().numberOfLocations();  
     var nextPi = pi;  
     if (nextPj == initialPi) {  
      nextPi = (nextPi + 1) % s.getInstance().numberOfLocations();  
     if (nextPi == (initialPi -1 + s.getInstance().numberOfLocations())/ + s.getInstance().numberOfLocations()) {  
      return null;  
      }  
      nextPj = (nextPi + 1) % s.getInstance().numberOfLocations();  
      }  
      return new SwapMove(s, initialPi, nextPi, nextPj);  
    }

An example of the stream generated by this procedure, given an instance with locations A, B, C, D and E, starting with the swap A <->B, will be the following: A <-> B, A <-> C, A <-> D, A <-> E, B <-> C, B <-> D, B <-> E, C <-> D, C <-> E, D <-> E, and finally, `null`.

#### Define a Local Search experiment
Define a local search experiment is as easy as define a constructive experiment. Copy the `ConstructiveExperiment` class in the same folder and rename it to `LocalSearchExperiment`.  In Mork, you could use to defined Local Searches: `LocalSearchFirstImprovement` and `LocalSearchBestImprovement`. The first one follows a first improvement strategy, i.e., as soon as it finds a move that results on an improve, it is executed. The second one follows a best improvement strategy, it explores all solutions of a neighborhood and execute the best possible move, the move that results in the best solution of the neighborhood.
In this experiment we are going to define 5 algorithms:
- Random constructive:
- Insert Neighborhood following a first and best improvement strategy
- Swap Neighborhood following a first and best improvement strategy

        public List<Algorithm<TSPSolution, TSPInstance>> getAlgorithms() {  
          
          var algorithms = new ArrayList<Algorithm<TSPSolution, TSPInstance>>();  
          
          
          algorithms.add(new SimpleAlgorithm<>(new TSPRandomConstructive()));  
          algorithms.add(new SimpleAlgorithm<>(new TSPRandomConstructive(),  
         new LocalSearchFirstImprovement<>(super.isMaximizing(), new InsertNeighborhood())));  
          algorithms.add(new SimpleAlgorithm<>(new TSPRandomConstructive(),  
         new LocalSearchBestImprovement<>(super.isMaximizing(), new InsertNeighborhood())));  
          algorithms.add(new SimpleAlgorithm<>(new TSPRandomConstructive(),  
         new LocalSearchFirstImprovement<>(super.isMaximizing(), new SwapNeighborhood())));  
          algorithms.add(new SimpleAlgorithm<>(new TSPRandomConstructive(),  
         new LocalSearchBestImprovement<>(super.isMaximizing(), new SwapNeighborhood())));  
          
         return algorithms;  

  }

Now is the moment to run this new experiment. Change the experiment property in the `application.yml` file and run it! Remember to look to the interactive dashboard run in [localhost](http://localhost:8080/). Which is the best algorithm?
### Testing in MorK

#### Asserts

I'm sure you've spent hours in front of your code trying to find that 🤬 bug. For that reason, we consider that it is
important that any operation must be validated, and check that the implemented procedures perform the desired
behavior.  
And how can this be done in MorK? There are many ways, testing is one way (go to section XXX for an example of Test
implementation in MorK), but in this case, we are talking about **asserts**. The keyword or reserved word **assert** is
used to state that at a certain point in the code a certain condition must be true. For example, if you write a method
that calculates the speed of a particle, you might assert that the calculated speed is less than the speed of light. Not
using it yet? Take a look at
the [official documentation](https://docs.oracle.com/javase/7/docs/technotes/guides/language/assert.html) and start
using it right now. Experience has shown that writing assertions while programming is one of the quickest and most
effective ways to detect and correct bugs. As an added benefit, assertions serve to document the inner workings of your
program, enhancing maintainability. By default, assertions are disabled at runtime. To enable assertions use
the `-enableassertions`, or `-ea`, as a program argument.



