# Framework concepts

Mork models any optimization method as a small set of **algorithm components** that cooperate to
build and refine solutions. Understanding these building blocks — and how they fit together — is the
key to using the framework effectively and to extending it with your own strategies.

This section introduces those concepts and links to the detailed documentation for each component
family.

## Where to start

- [Algorithm components](algorithm-components/intro.md): the core idea behind Mork — what a
  component is, the roles they can play (algorithm, constructive, improver, shake), and how to
  implement your own.
- [List of algorithm components](algorithm-components/components.md): a catalogue of every component
  bundled with the framework, grouped by role.

## Component families

| Family | Role | Documentation |
|--------|------|---------------|
| **Metaheuristics** | High-level strategies that drive the search: VNS, Simulated Annealing, Iterated Greedy, Scatter Search, Multi-Start, VND | [Metaheuristics](algorithm-components/metaheuristics/vns.md) |
| **Constructors** | Build feasible solutions from scratch: random, greedy, GRASP | [Constructors](algorithm-components/constructors/constructive.md) |
| **Improvers** | Refine a solution without ever worsening it: local search, VND | [Improvers](algorithm-components/improvers/improver.md) |
| **Shakes & Perturbations** | Escape local optima by perturbing a solution: random moves, destructive/reconstructive | [Shakes](algorithm-components/shakes/shake.md) |

!!! tip
    New to Mork? Follow the [TSP step-by-step example](../examples/TSP.md) to see all these concepts
    working together on a real optimization problem.
