# State-of-the-Art comparison

Usually, we have a set of reference results for a given set of instances, maybe because the optimal value is known, or maybe a previous heuristic approach.

We can feed this existing data to Mork so it is automatically taken into account when comparing different algorithmic approaches, for example, when the Excel files are generated, best values and deviation to best known values are calculated taking into account this existing data.
In the Mork context, this values, which may include objective function values, execution times, or time to best, are called Reference Results.

## Programmatically feeding reference data

In order to feed this data to the framework, extend the class `ReferenceResultProvider`, and implement the method `ReferenceResult getValueFor(String instanceName)`. Example:

```java
/**
 * Example: Loads reference results as reported in http://dx.doi.org/10.10...
 */
public abstract class MyProblemReferenceResults extends ReferenceResultProvider {
    
    Map<String, ReferenceResult> sotaResults = new HashedMap<>();

    /**
     * Load reference values from TSV or CSV file
     * The file format in this example is "instanceName,functionValue,executionTime",
     * so after the split by ',', instance name is in position 0, f.o value in position 1, and lastly execution time in position 2.
     */
    public MyProblemReferenceResults() {
        Files.lines(Path.of("sota.csv"))
            //.skip(1) // Remember to skip the first line if the file has headers
            .forEach(l -> {
                var parts = l.split(","); // Separator may be one of ';', ',' or '\t'.
                var referenceResult = new ReferenceResult();
                referenceResult.setScore(parts[1]);
                referenceResult.setTimeInSeconds(parts[2]);
                sotaResults.put(parts[0], referenceResult);
        });
    }

    /**
     * It is 
     * @param instanceName Get ReferenceResult for the given instance
     * @return Reference value for the instance if known, or empty reference result if not. 
     * It is perfectly valid to not have all values for all instances.
     */
    @Override
    public ReferenceResult getValueFor(String instanceName) {
        return this.sotaResults.getOrDefault(instanceName, new ReferenceResult());
    }

    /**
     * Where are this reference result from? Used to disambiguate if there are multiple providers.
     * @return Provider name, can be the name of the SOTA algorithm, the authors, or any other id that clearly identifies the source.
     */
    @Override
    public String getProviderName() {
        return "RaulEtAl2019";
    }
}
```


If there are multiple reference results for the given instance (for example, when there are multiple previous works using different approaches), we can just extend several times the class `ReferenceResultProvider` as long as the `getProviderName()` method does not collide between implementations.