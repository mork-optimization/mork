# Algorithm components

## Definition
An algorithm component is any Java class that is related to the optimization progress, ie, is going to modify or create solutions, and it can be either an algorithm or part of one.
Examples of algorithm components types are algorithms themselves, constructive methods, local searches, perturbation methods, etc. All algorithm components must extend a type, for example `GRASPConstructive` extends the `Constructive` class, `BestImprovementLocalSearch` extends `Improver`, etc.

> Tip: You can create as many algorithm component types as you need, without using the predefined ones, if you wish. See the advanced section at the end.

## Component types
All components, except indicated otherwise, are available in the `common` module. In this section, we introduce the list of ready to use algorithm components.

Common algorithm components available in the framework are classified in the following roles, depending on the functionality that they contain:
- Algorithms: receive an instance and return a feasible solution
- Improver: 


### Algorithm

### Constructive methods

### Perturbation / Shake methods

### Improvement methods

## Advanced

One of the core design principles of the Mork framework is its flexibility. In this section, we will explain how to create custom components, and some Mork internals and design decisions.
The only requirement to integrate with the framework, is using the algorithm base class.

### Creating a custom algorithm

### Autodetect components
Any Java class is automatically detected by the Mork engine as an algorithm component if it is either marked with the annotation `@AlgorithmComponent`, or any superclass is annotated with it (does not include interfaces).
Unless you have a reason to use interfaces, always use class inheritance to define algorithm components.

> Note: Because the Algorithm, Improver, Constructive, etc. classes are marked with `@AlgorithmComponent`, any child class is automatically considered an algorithm component automatically.

