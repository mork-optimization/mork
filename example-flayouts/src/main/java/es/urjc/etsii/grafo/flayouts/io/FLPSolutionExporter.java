package es.urjc.etsii.grafo.flayouts.io;

import es.urjc.etsii.grafo.executors.WorkUnitResult;
import es.urjc.etsii.grafo.flayouts.model.FLPInstance;
import es.urjc.etsii.grafo.flayouts.model.FLPSolution;
import es.urjc.etsii.grafo.io.serializers.SolutionSerializer;

import java.io.BufferedWriter;
import java.io.IOException;

/**
 * Serialize solutions using a custom format
 * Each row represents a facility row, with positive numbers being the facility id, and negative numbers a fake facility and its width.
 * Example: 1 3 5 -2 --> Facility 1, 3 and 5, and a fake facility with width 2.
 */
public class FLPSolutionExporter extends SolutionSerializer<FLPSolution, FLPInstance> {

    /**
     * Create a new solution serializer with the given config
     *
     * @param config
     */
    public FLPSolutionExporter(FLPSolutionSerializerConfig config) {
        super(config);
    }

    @Override
    public void export(BufferedWriter writer, WorkUnitResult<FLPSolution, FLPInstance> result) throws IOException {
        var solution = result.solution();
        var rows = solution.getRows();
        StringBuilder sb = new StringBuilder();
        for (int row = 0; row < solution.nRows(); row++) {
            for (int pos = 0; pos < solution.rowSize(row); pos++) {
                int f = rows[row][pos];
                if(f == FLPSolution.FREE_SPACE){
                    throw new IllegalStateException("Invalid solution: free space found in row %s, pos %s. Data: %s".formatted(row, pos, rows));
                }
                sb.append(f).append(" ");
            }
            if(!sb.isEmpty()){
                sb.setCharAt(sb.length() - 1, '\n');
            } else {
                sb.append('\n');
            }
        }
        writer.write(sb.toString());
    }
}
