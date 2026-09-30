package es.urjc.etsii.grafo.flayouts.io;

import es.urjc.etsii.grafo.executors.WorkUnitResult;
import es.urjc.etsii.grafo.flayouts.model.FLPInstance;
import es.urjc.etsii.grafo.flayouts.model.FLPSolution;
import es.urjc.etsii.grafo.io.serializers.SolutionSerializer;

import java.io.BufferedWriter;
import java.io.IOException;

/**
 * Serialize a space-free layout as one line per row of zero-based facility IDs.
 * Facilities appear in their layout order, packed consecutively from coordinate zero.
 * Example: 0 3 5 represents facilities 0, 3 and 5 in one row.
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
