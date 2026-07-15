# General guidelines for developing in Mork

- When choosing between the Stream API and simple for loops, prefer simple for loops. 
- When refactoring existing methods, avoid creating compatibility layers, refactor callers too. 
- Simplify implementations whenever possible. KISS.
- Breaking changes are encouraged when they substantially simplify and improve the existing code.
