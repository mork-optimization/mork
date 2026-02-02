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
public class DRFPSolutionIO extends SolutionSerializer<FLPSolution, FLPInstance> {

    /**
     * Create a new solution serializer with the given config
     *
     * @param config
     */
    public DRFPSolutionIO(DRFPSolutionSerializerConfig config) {
        super(config);
    }

    @Override
    public void export(BufferedWriter writer, WorkUnitResult<FLPSolution, FLPInstance> result) throws IOException {
        var solution = result.solution();
        var data = solution.getRows();
        StringBuilder sb = new StringBuilder();
        for(var row: data){
            for(var f: row){
                if(f == null){
                    continue;
                }
                if(f.facility.fake){
                    sb.append(-f.width());
                } else {
                    sb.append(f.id());
                }
                sb.append(" ");
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
