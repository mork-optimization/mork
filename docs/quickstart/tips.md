# Tips & Tricks

## Managing Java installations

Manually installation, updating, and removing old Java versions can get cumbersome. Managing Java versions, 
or more in general, Software Development Kits, can be simplified using a tool like [SdkMan](https://sdkman.io/install).
For example, listing available Java SDKs is as simple as:
```
sdk list java
================================================================================
Available Java Versions for Linux 64bit
================================================================================
 Vendor        | Use | Version      | Dist    | Status     | Identifier
--------------------------------------------------------------------------------
 Corretto      |     | 25.0.1       | amzn    |            | 25.0.1-amzn         
               |     | 25           | amzn    |            | 25-amzn             
               |     | 24.0.2       | amzn    |            | 24.0.2-amzn         
               |     | 23.0.2       | amzn    |            | 23.0.2-amzn         
               |     | 21.0.9       | amzn    |            | 21.0.9-amzn         
               |     | 21.0.8       | amzn    |            | 21.0.8-amzn         
               |     | 17.0.17      | amzn    |            | 17.0.17-amzn        
               |     | 17.0.16      | amzn    |            | 17.0.16-amzn        
               |     | 11.0.29      | amzn    |            | 11.0.29-amzn        
               |     | 11.0.28      | amzn    |            | 11.0.28-amzn        
               |     | 8.0.472      | amzn    |            | 8.0.472-amzn        
               |     | 8.0.462      | amzn    |            | 8.0.462-amzn        
 Dragonwell    |     | 21.0.8       | albba   |            | 21.0.8-albba        
               |     | 17.0.16      | albba   |            | 17.0.16-albba       
               |     | 11.0.28      | albba   |            | 11.0.28-albba       
               |     | 8.0.462      | albba   |            | 8.0.462-albba       
 Gluon         |     | 22.1.0.1.r17 | gln     |            | 22.1.0.1.r17-gln    
               |     | 22.1.0.1.r11 | gln     |            | 22.1.0.1.r11-gln    
 GraalVM CE    |     | 25.0.1       | graalce |            | 25.0.1-graalce      
               |     | 25           | graalce |            | 25-graalce          
               |     | 24.0.2       | graalce |            | 24.0.2-graalce      
               |     | 23.0.2       | graalce |            | 23.0.2-graalce      
               |     | 21.0.2       | graalce |            | 21.0.2-graalce      
               |     | 17.0.9       | graalce |            | 17.0.9-graalce      
 GraalVM Oracle|     | 26.ea.13     | graal   |            | 26.ea.13-graal      
               |     | 25.0.1       | graal   |            | 25.0.1-graal        
               |     | 25           | graal   |            | 25-graal            
               |     | 24.0.2       | graal   |            | 24.0.2-graal        
               |     | 23.0.2       | graal   |            | 23.0.2-graal        
               |     | 21.0.9       | graal   |            | 21.0.9-graal
```

And installing it:
```
sdk install java 25.0.1-amzn
Downloading: java 25.0.1-amzn
In progress...
################################################################################################################################################################################################################################# 100.0%
Repackaging Java 25.0.1-amzn...
Done repackaging...
Installing: java 25.0.1-amzn
Done installing!
Do you want java 25.0.1-amzn to be set as default? (Y/n): n
```

!!! note
    Setting a Java SDK as default during installation with SDKMan may only affect new shells, 
    so you may need to close and open a new one. Moreover, you may need to manually choose in IntelliJ
    for each project which Java version should be used.

For a more detailed explanation, and full usage details see the official [SdkMan documentation](https://sdkman.io/usage).

## Initialize collections with fixed size

When creating collections like `ArrayList`, `HashMap`, or other dynamic data structures, always initialize them with a capacity that matches or exceeds your expected size. This prevents the collection from having to resize internally, which involves creating new arrays and copying elements—an expensive operation that triggers garbage collection.

**Bad approach** - No initial capacity:
```java
List<Integer> list = new ArrayList<>();  // Default capacity is 10
for (int i = 0; i < 1_000_000; i++) {
    list.add(i);  // Multiple resizes as the list grows
}
```

**Good approach** - Fixed capacity:
```java
List<Integer> list = new ArrayList<>(1_000_000);  // Pre-allocate space
for (int i = 0; i < 1_000_000; i++) {
    list.add(i);  // No resizing needed
}
```

The same applies to `HashMap` and other collections:
```java
// Bad
Map<String, Integer> map = new HashMap<>();

// Good
Map<String, Integer> map = new HashMap<>(expectedSize);
```

By pre-allocating the correct size, you avoid the overhead of multiple array copies and reduce garbage collection pressure. This is especially important in performance-critical code sections with large datasets.

For more information on collection performance, see the [Java Collections Framework documentation](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/doc-files/coll-reference.html) and [ArrayList capacity management](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/ArrayList.html).

## Minimize object creation and reuse instances

Creating new objects in tight loops can significantly impact performance. Every time an object is created and discarded, the garbage collector must eventually clean it up, which consumes CPU cycles and can cause pause times in your application.

**Bad approach** - Creating new objects repeatedly:
```java
for (int i = 0; i < 1_000_000_000; i++) {
    List<Integer> tempList = new ArrayList<>(3);
    tempList.add(i);
    tempList.add(i + 1);
    tempList.add(i + 2);
    // List is discarded, triggering garbage collection
}
```

**Good approach** - Reusing the same object with `clear()`:
```java
List<Integer> reuseList = new ArrayList<>(3);

for (int i = 0; i < 1_000_000_000; i++) {
    reuseList.clear();  // Reuse the same list
    reuseList.add(i);
    reuseList.add(i + 1);
    reuseList.add(i + 2);
}
```

The difference is substantial. In the first approach, with 1 billion iterations creating new objects and immediately becoming eligible for garbage collection, it takes approximately 7977 ms. By reusing objects and calling `clear()` instead, you reduce memory pressure and allow the garbage collector to run less frequently, resulting in approximately 6009 ms. This represents a significant performance improvement of about 25% by simply reusing objects.

## Prefer arrays or BitSet over Map/Set for lookups

When you need to check if a value exists in a collection frequently, using a `Map` or `Set` might not be optimal. Static arrays or `BitSet` can offer significantly faster lookup times, especially with a fixed range of values.

**Benchmark comparison** - 1 element vs 1000000 elements:
```java
// Map lookup performance
Map<Integer, Boolean> map = new HashMap<>(1000);
map.put(0, true);
map.containsKey(0);  // O(1) average case, but with hashing overhead

// Array lookup performance
boolean[] array = new boolean[1000];
array[0] = true;
array[0];  // O(1) with minimal overhead

// BitSet lookup performance
BitSet bitSet = new BitSet(1000);
bitSet.set(0);
bitSet.get(0);  // O(1) with minimal overhead
```

For a practical benchmark with 10 million lookups:

**With 1 element:**
- Map lookup: ~8 ms
- Boolean array lookup: ~0 ms
- BitSet lookup: ~1 ms

**With 1000000 elements:**
- Map lookup: ~43 ms (consistent)
- Boolean array lookup: ~1 ms (consistent)
- BitSet lookup: ~2 ms (consistent)

The `BitSet` is particularly efficient because it uses individual bits to represent boolean values, using approximately 8 times less memory than storing Boolean objects in a Map, and providing extremely fast bitwise operations for lookups. Use `BitSet` or `Boolean array` when you need to track a fixed range of integer values, and prefer static arrays when the data is known at compile time.
