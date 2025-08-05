//package es.urjc.etsii.grafo.MST.io;
//
//import es.urjc.etsii.grafo.MST.model.MSTInstance;
//import es.urjc.etsii.grafo.MST.model.MSTSolution;
//import es.urjc.etsii.grafo.executors.WorkUnitResult;
//import es.urjc.etsii.grafo.io.serializers.SolutionSerializer;
//
//import java.io.BufferedWriter;
//import java.io.IOException;
//
///**
// * Provides a custom implementation for exporting solutions to disk.
// */
//public class MSTSolutionExporter extends SolutionSerializer<MSTSolution, MSTInstance> {
//
//    /**
//     * Create a new solution serializer with the given config
//     *
//     * @param config Common solution serializer configuration
//     */
//    protected MSTSolutionExporter(MSTSolutionExporterConfig config) {
//        super(config);
//    }
//
//    @Override
//    public void export(BufferedWriter writer, WorkUnitResult<MSTSolution, MSTInstance> result) throws IOException {
//        // Export data to the BufferedWriter. If more control is desired,
//        // for example if you want to create multiple files for each solution, ot write binary data
//        // ignore this method (throw new UnsupportedOperationException()) and override this one instead:
//        // public void export(String folder, String suggestedFilename, WorkUnitResult<MSTSolution, MSTInstance> solution)
//    }
//}
