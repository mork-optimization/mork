# Upgrading from old versions

In this page, you will find instructions and tips for upgrading from older versions of Mork to the latest version. 
Note that instructions are provided for upgrading from one mayor version (por example, v0.19), to the next one (v0.20), and so on.
If you want to upgrade from a version that is not the immediate previous one, you will need to follow the instructions for each intermediate version.

## Upgrading from v0.19 to v0.20

### Objectives refactor
Before, we declared if we were maximizing or minimizing using a parameter when starting Mork, for example:
```java
public static void main(String[] args) {
    Mork.start(args, FMode.MINIMIZE);
}
```
Or using the `FMode.MAXIMIZE` constant.
```java
public static void main(String[] args) {
    Mork.start(args, FMode.MAXIMIZE);
}
```
Now, we can declare our problem objectives using the static methods in the `Objective` class, for example:
```java
public static void main(String[] args) {
    Mork.start(args,
            Objective.ofMinimizing("Distance", TSPSolution::getDistance, TSPMove::getDistanceDelta)
    );
}
```

This way, methods `getValue()`, `recalculateScore()`, `getScore()`, etc. are no longer mandatory, and you do not need to override them.
You can still use them if you want, or rename them to whatever name is representative to your problem. In the case of multi objective optimization,
objectives can be easily declared as follows:
```java
public static void main(String[] args) {
    Mork.start(args,
            Objective.ofMinimizing("Distance", TSPSolution::getDistance, TSPMove::getDistanceDelta),
            Objective.ofMaximizing("Profit", TSPSolution::getProfit, TSPMove::getProfitDelta)
    );
}
```

Note that you can also use lambda expressions instead of method references, for example:
```java
public static void main(String[] args) {
    Mork.start(args,
            Objective.ofMinimizing("Distance", solution -> solution.getMetrics().distance, move -> move.calculateXYZ() * 4)
    );
}
```

