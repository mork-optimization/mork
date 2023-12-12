# Results and serialization

Experiment results and solution data can be exported to multiple formats. By default, Mork provides implementations 
to export experiment results to CSV/TSV and XLSX (Microsoft Excel 2007+) and solution data to JSON. 
More formats can be added by the user. 
If you implement a format that may be useful for other people, consider submitting a Pull Request!

## CSV serialization

Experiment data can be serialized to CSV like formats, using the separator defined in the configuration, which defaults to ','.
For example, by changing the separator to a tab '\t', the format would be TSV.

## Excel serialization

Experiment data can be exported to the XLSX format, which allows us to create calculated fields with formulas 
and interactive data automatically without intervention. By default, the Excel file contains two sheets, 
one with the raw data and another one with an interactive pivot table that allows us to quickly filter and 
analyze the results.

### Configuring pivot table fields

Although the fields in the pivot table can be configured in Excel, in order to save time we can optionally configure them directly
inside the application.yml file.

```yml
# [seralizers.xlsx section]

    # Show best (min or max) score in pivot table
    bestScoreEnabled: true

    # Show average score in pivot table
    avgScoreEnabled: false

    # Show standard deviation of solution score in pivot table. Uses Excel STD_DEVP function
    stdScoreEnabled: false

    # Show variance of score in pivot table. Uses Excel VARP function
    varScoreEnabled: false

    # Show average time in seconds per iteration in pivot table.
    avgTimeEnabled: false

    # Show total time in seconds for a given (algorithm, instance) in pivot table.
    totalTimeEnabled: true

    # Show average time to the best solution in seconds in pivot table.
    avgTTBEnabled: false

    # Show total time to the best solution in seconds in pivot table.
    totalTTBEnabled: false

    # Show number of times a given algorithm reaches the best known solution.
    sumBestKnownEnabled: false

    # Show 1 if a given algorithm reaches the best solution for an instance, 0 otherwise.
    hasBestKnownEnabled: true

    # Show average percentage deviation to best known solution in pivot table.
    avgDevToBestKnownEnabled: false

    # Show minimum percentage deviation to best known solution in pivot table.
    minDevToBestKnownEnabled: true

    # Show generated grand total in pivot table
    rowGrandTotal: false
    columnGrandTotal: false
```

### Adding custom data

You may create charts, new sheets, tables, etc from within your code by extending the `ExcelCustomizer` class, 
using the method `customize(XSSFWorkbook excelBook)`. The `XSSFWorkbook` is 
the current opened Excel file.
Any dependency can be declared in the constructor of your implementation, and it will be automatically be provided at runtime. Example:
```java
public class MyExcelCustomizer extends ExcelCustomizer {

    private final AbstractEventStorage<MySolution, MyInstance> events;
    public MyExcelCustomizer(AbstractEventStorage<MySolution, MyInstance> events) {
        this.events = events;
    }

    @Override
    public void customize(XSSFWorkbook excelBook) {
        var sheet = excelBook.createSheet("MyCustomSheet");

        var allSolutions = this.events.getEventsByType(SolutionGeneratedEvent.class).toList();
        for(var event: allSolutions){
            double score = event.getScore();
            long execTime = event.getExecutionTime();
            
            // Do some processing and write it to the excel file!
            sheet.createRow(); 
            // [...]
        }
    }
}
```
See [event docs](events.md) for more information about how the event system works. All events generated during the execution are available using an `AbstractEventStorage`.
It is guaranteed that the customize method will be invoked **after** the default Excel sheets are generated, so you may always modify the existing data and adapt it to your needs.

Mork uses Apache POI 5.0 to create the XLSX files, see the [official Javadoc](https://poi.apache.org/apidocs/index.html) for more information on how to
do common operations such as creating new sheets and setting cell values.

## Solution serialization 

By default, solutions can be exported to file using [the JSON format](https://developer.mozilla.org/en-US/docs/Learn/JavaScript/Objects/JSON). 
See next section if you want to implement a custom export format.

## Custom Solution Serializer
You may define a custom solution serializer to export solutions to any given format.

### Creating a configuration class
The following class will store your serializer configuration. You may add any custom property needed, or you can leave it empty.
All properties will be automatically be filled at runtime with the configuration available from the environment, 
the application.yml file and the command line parameters. For more information about the configuration system see [here](config.md).

```java
@Configuration
@ConfigurationProperties(prefix = "serializers.solution-yourproblem")
public class YourProblemSolutionSerializerConfig extends AbstractSerializerConfig {
    private boolean enableXFeature = false; // Default value, will be overridden if defined in the application.yml or any other source

    public boolean isEnableXFeature(){
        return this.enableXFeature;
    }
    
    //[...]
}
```

### Mapping YourProblemSolutionSerializerConfig to configuration properties
After defining the configuration class, you may create a new section inside the application.yml. The properties `enabled`, `folder` and `format` are 
common in all serializers.
Note that the section name must match the prefix specified in `ConfigurationProperties` so configuration properties are correctly mapped:
```yml
serializers:
  solution-yourproblem:
    # Enable this serializer.
    enabled: true

    # Path where solutions created by this serializer will be exported
    folder: 'solutions'

    # Filename format, replacements are applied as follows
    # yyyy: replaced with current year, ex 2020
    # MM, dd, HH, mm, ss: replaced by month, day, hour, minute and seconds
    # any letters [a-zA-Z] can be part of the filename as long as they are between single quotes
    # Always prepends ExperimentName, InstanceName and algorithm name to prevent name collisions
    format: "'.txt'"

    # Enable my custom feature
    enableXFeature: true
    
    # [Other additional config parameters]
```

### Creating the Solution Serializer
The last step is implementing the serializer by extending the AbstractSerializer.
```java
public class YourProblemSolutionSerializerConfig extends SolutionSerializer<YourSolutionType, YourInstanceType> {

    /**
     * Create a new solution serializer with the given config
     * @param config
     */
    public YourProblemSolutionSeralizer(YourProblemSolutionSerializerConfig config) {
        super(config);
    }

    @Override
    public void export(BufferedWriter writer, YourSolutionType solution) throws IOException {
        var data = solution.getSolutionData();
        StringBuilder sb = new StringBuilder();
        for(var row: data){
            for(var f: row){
                sb.append(f.facility.id);
                sb.append(" ");
            }
            sb.append('\n');
        }
        writer.write(sb.toString());
    }
}
```

Note: If the method `export(BufferedWriter writer, YourSolutionType solution)` does not provide enough flexibility, 
for example if you want to export the solution as an image, you may leave it empty and override 
`export(File f, DRFPSolution drfpSolution)` instead.

## Metrics

Mork is able to gather the evolution of the objective function on each execution. This way, once an algorithm has finished and
produced a solution, it is possible to analyze how the objective function has changed in the execution.

This feature requieres the JSON serializer to be enabled.

In order to activate the metrics, the corresponding parameter has to be enabled in the application.yml file:
```yml
solver:

  #[...]

  # Enable or disable metrics tracking. Force enabled if using autoconfig.
  metrics: true

  #[...]

```

Once the execution has finished, the JSON file containing the solution description will also include values in the metrics section. For each defined metric, it will show the timestamp and the new value. The default metric is `BestObjective`, which watches the objective function value of the best solution. An example of this report follows:
```json
"executionTime" : 9670583917,
  "timeToTarget" : 9572068250,
  "metrics" : {
    "metrics" : {
      "BestObjective" : {
        "values" : [ {
          "instant" : 180894167,
          "value" : 44704.0
        }, {
          "instant" : 4650119875,
          "value" : 447.0
        },  {
          "instant" : 8874177375,
          "value" : 280.0
        }, {
          "instant" : 9006580125,
          "value" : 277.0
        }, {
          "instant" : 9572100250,
          "value" : 251.0
        } ],
        "name" : "BestObjective"
      }
    },
    "referenceNanoTime" : 821070987037541
  }
```
