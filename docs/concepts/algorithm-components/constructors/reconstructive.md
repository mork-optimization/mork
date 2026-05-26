# Reconstructive

Reconstructive methods are specialized constructive methods designed to rebuild partially destroyed solutions. They are commonly used in destruction-reconstruction metaheuristics like Iterated Greedy.

## Overview

Unlike regular constructive methods that start from empty solutions, reconstructive methods:
- Take **partially complete** solutions as input
- **Rebuild** the missing parts
- Preserve the existing structure where possible

All reconstructive components are valid constructive methods, but not constructives that can work on partially valid solutions can work as a reconstructive method.

## Algorithm Outline

```
Reconstruct(partialSolution):
    while (partialSolution not feasible) {
        candidates = getUnassignedElements(partialSolution)
        selected = selectElement(candidates, partialSolution)
        add(selected, partialSolution)
    }
    
    return partialSolution
```
