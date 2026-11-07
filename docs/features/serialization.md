# Results and serialization

Experiment results and solution data can be exported to multiple formats. By default, Mork provides implementations 
to export experiment results to CSV/TSV and XLSX (Microsoft Excel 2007+) and solution data to JSON. 
More formats can be added by the user. 
If you implement a format that may be useful for other people, consider submitting a Pull Request!

Result serialization is invoked directly and synchronously by the execution workflow; it is not performed by an event listener. Serializers configured with `PER_INSTANCE` run after an instance has been processed and before its `InstanceProcessingEndedEvent` is published. Serializers configured with `EXPERIMENT_END` run after experiment execution and before `ExperimentEndedEvent` is published. A serialization failure is reported as an execution error but does not prevent the corresponding final events or lifecycle finalization.

## Result lifetime and access

`ResultStore` keeps full solutions strongly reachable through the final experiment serialization attempt, allowing all result serializers and customizers to inspect or manipulate them. This guarantee also applies when a serializer fails. After that attempt, strong references are always released: the stored `WorkUnitResult` becomes a solutionless copy that retains its cached objective values, custom properties, metrics, and other metadata, while the solution itself is kept in a `SoftReference`.

Use `ResultStore.findSolution(UUID)` when the solution is needed after release. The lookup is best-effort at that point and may return an empty `Optional` if memory pressure has reclaimed the solution. `getResultsForExperiment(String)` returns an immutable, insertion-ordered `List<WorkUnitResult<...>>` snapshot.

## CSV serialization

Experiment data can be serialized to CSV like formats, using the separator defined in the configuration, which defaults to ','.
For example, by changing the separator to a tab '\t', the format would be TSV.

## Excel serialization

Experiment data can be exported to XLSX with these sheets:

- **Raw Results** contains successful executions and reference measurements in `RawResultsTable`.
- **Pivot Table** summarizes those rows. If there are no successful executions, it explains why no summary is available.
- **Failures** lists unsuccessful executions when present.
- **Execution Summary** lists total, successful and failed execution counts and the success rate for each algorithm. Reference measurements are excluded.
- **Other** contains execution counts and benchmark metadata; the optional **Instances** sheet contains instance properties.

Missing reference scores and times are blank and do not contribute numeric measurements to summaries.
Best-score calculations exclude missing scores. Relative deviation is `ABS(score - best) / ABS(best)`,
displayed as a percentage. When the best score is zero, deviation is zero for a zero score and blank for
any other score because relative deviation is undefined. Missing scores also have blank deviation and best-match indicators.

The pivot table and comparison formulas reference `RawResultsTable`. To add results manually, paste rows with
the same columns into the first row directly below the raw results table, then refresh the pivot table using
**Data > Refresh All**. Excel expands the table automatically, so the source range does not need to be edited.
The first raw-data header contains the same instructions as a note. Excel recalculates formulas on opening.

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

### Customizing the Excel report

Create a public class extending `ExcelCustomizer`. Dependencies can be supplied through
its constructor, like other Mork components.

Mork calls `customize(workbook, results)` for each Excel export, after generating the default sheets and
before saving the file. The hook lets you add charts and custom sheets, and modify the editable default
sheets:

| Sheet | What you can do inside `customize` |
| --- | --- |
| **Pivot Table** | Edit cells, formatting and pivot settings. An export without successful executions has an explanatory message instead of a pivot table. |
| **Other** | Edit metadata cells or append your own metadata. |
| Sheets you create | Create and edit cells, formulas, formatting, charts and tables using Apache POI. |
| **Raw Results**, **Failures**, **Instances**, **Execution Summary** | Their rows are unavailable to the hook. Do not recreate or overwrite these sheets or their rows. |

`XSSFWorkbook` supports editing. The restriction on the last four sheets comes from how Mork writes
large reports: those rows are streamed separately and merged into the saved file after the hook returns.
Reading them through the supplied workbook may return no rows even though the saved report contains data.
The hook does not reopen the completed file for full-workbook editing.

Use the supplied `results` list to create your own views of the data:

- It contains this report's successful and failed executions, in input order. Reference-provider rows are not included.
- Check `result.success()` before using objective values. Failed executions may have no objectives.
- Read `result.objectives()`, `result.solutionProperties()` and the execution metadata directly. The list is read-only.
- `result.solution()` may be null, particularly for offline AutoLLM reports. Using cached values makes a customizer work for both normal and offline exports.
- Formula cells have not been recalculated yet. Use the supplied results for calculations in Java; Excel recalculates workbook formulas when the report is opened.

The following implementation creates a sheet containing successful executions and appends a note to
**Other**. Put it in `MyExcelCustomizer.java`, adjusting the package to match your application:

```java
package es.urjc.etsii.myproject;

import es.urjc.etsii.grafo.executors.WorkUnitResult;
import es.urjc.etsii.grafo.io.serializers.excel.ExcelCustomizer;
import es.urjc.etsii.grafo.io.serializers.excel.ExcelSerializer;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.List;

public class MyExcelCustomizer extends ExcelCustomizer {
    @Override
    public void customize(XSSFWorkbook workbook, List<? extends WorkUnitResult<?, ?>> results) {
        var sheet = workbook.createSheet("My Results");
        var header = sheet.createRow(0);
        header.createCell(0).setCellValue("Instance");
        header.createCell(1).setCellValue("Algorithm");
        header.createCell(2).setCellValue("Main objective");

        int rowIndex = 1;
        for (var result : results) {
            if (!result.success()) continue;
            var row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(result.instanceId());
            row.createCell(1).setCellValue(result.algorithm().getName());
            row.createCell(2).setCellValue(result.mainObjectiveValue());
        }
        sheet.createFreezePane(0, 1);
        sheet.setColumnWidth(0, 30 * 256);
        sheet.setColumnWidth(1, 30 * 256);
        sheet.setColumnWidth(2, 20 * 256);

        var metadata = workbook.getSheet(ExcelSerializer.OTHER_DATA_SHEET);
        var note = metadata.createRow(metadata.getLastRowNum() + 1);
        note.createCell(0).setCellValue("My Results contents");
        note.createCell(1).setCellValue("Successful executions only; reference measurements excluded");
    }
}
```

Modify the supplied workbook in place and return. Mork owns saving and closing it; do not call `write()`
or `close()` on it, and do not keep it for background work. An exception from the hook fails that export;
the previous report, if present, is preserved. Custom sheets are held in memory, so large additional
sheets increase the memory required to export the report.

To add columns to **Raw Results**, define them through your solution's `customProperties()` method
before execution. Mork captures these values and exports them automatically. Values must be finite
numbers, strings or booleans; property names must not duplicate the built-in column names.

If you need arbitrary edits to the streamed sheets, open the **completed** `.xlsx` file with
`XSSFWorkbook` after export and save an edited copy, or edit it in Excel. This is a separate step after
Mork finishes saving, not code to run inside `customize`. Reopening the file loads the full workbook
into memory. Editing cells in the saved file does not change Mork's stored execution results.

Mork uses Apache POI 5 to create the XLSX files, see the [official Javadoc](https://poi.apache.org/apidocs/index.html) for more information on how to
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
public class YourProblemSolutionSerializer extends SolutionSerializer<YourSolutionType, YourInstanceType> {

    /**
     * Create a new solution serializer with the given config
     * @param config
     */
    public YourProblemSolutionSerializer(YourProblemSolutionSerializerConfig config) {
        super(config);
    }

    @Override
    public void export(BufferedWriter writer, WorkUnitResult<YourSolutionType, YourInstanceType> result) throws IOException {
        var solution = result.solution();
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

`WorkUnitResult` is a record. Use record accessors such as `resultId()`, `solution()`, `executionTime()`, `objectives()`, and `solutionProperties()`, plus the derived `mainObjectiveValue()` method. Objective values and custom properties are evaluated and cached when the result is created, so later solution mutation does not change serialized result metadata.

Note: If the method `export(BufferedWriter writer, WorkUnitResult<YourSolutionType, YourInstanceType> result)` does not provide enough flexibility, 
for example if you want to export the solution as an image, you may leave it empty and override 
`List<Path> export(String folder, String suggestedFilename, WorkUnitResult<YourSolutionType, YourInstanceType> result)` instead. Return every successfully written file path (or an empty list when intentionally skipped) so the monitoring artifact registry can expose it. Throw on export failure; do not return a path that was not written. The standard `BufferedWriter` override is unchanged; its default wrapper returns the written path automatically. Export warnings appear in the normal monitor without being counted as failed computations.

## Metrics

Mork is able to gather different metrics along the execution of an algorithm.

A predefined metric in all Mork available algorithms is `BestObjective`, which records the evolution of the 
objective function on each run. This way, once an algorithm has finished and produced a solution, it is possible
to analyze how the objective function has changed during the execution of any given algorithm. 
The default JSON serializer saves the metrics along the exported solution if enabled. When using a custom serializer, you control which metrics are saved if any.
All metrics data can be accessed from the export method inside your serializer class, using the provided `WorkUnitResult` as an argument.

Remember that metrics may not be enabled by default, the corresponding configuration parameter can be found in the application.yml file:
```yml
solver:

  #[...]

  # Enable or disable metrics tracking. Force enabled if using autoconfig.
  metrics: true

  #[...]

```

If using the default JSON serializer, once the execution has finished, the JSON file containing the solution description will also include values in the metrics section. For each defined metric, it will show pairs of (time, value). All time values are in nanoseconds, and represent the ellapsed time since the experiment started in nanoseconds. The default metric is `BestObjective`, which watches the objective function value of the best solution. An example of this report follows:

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

All algorithms implemented in Mork register the changes to the objective function. If you create new algorithm components, and want to add new datapoints to the BestObjective metric, or in general, to any metric, an statement similar to the following can be included where appropiate, for example each time the best solution is updated:
```java
Metrics.add(BestObjective.class, solution.getScore());
```
where `solution` is the best solution managed by the algorithm.
