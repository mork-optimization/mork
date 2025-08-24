//package es.urjc.etsii.grafo.TSPTW.io;
//
//import es.urjc.etsii.grafo.TSPTW.model.TSPTWInstance;
//import es.urjc.etsii.grafo.TSPTW.model.TSPTWSolution;
//import es.urjc.etsii.grafo.executors.WorkUnitResult;
//import es.urjc.etsii.grafo.io.serializers.SolutionSerializer;
//
//import java.io.BufferedWriter;
//import java.io.IOException;
//
///**
// * Provides a custom implementation for exporting solutions to disk.
// */
//public class TSPTWSolutionExporter extends SolutionSerializer<TSPTWSolution, TSPTWInstance> {
//
//    /**
//     * Create a new solution serializer with the given config
//     *
//     * @param config Common solution serializer configuration
//     */
//    protected TSPTWSolutionExporter(TSPTWSolutionExporterConfig config) {
//        super(config);
//    }
//
//    @Override
//    public void export(BufferedWriter writer, WorkUnitResult<TSPTWSolution, TSPTWInstance> result) throws IOException {
//        // Export data to the BufferedWriter. If more control is desired,
//        // for example if you want to create multiple files for each solution, ot write binary data
//        // ignore this method (throw new UnsupportedOperationException()) and override this one instead:
//        // public void export(String folder, String suggestedFilename, WorkUnitResult<TSPTWSolution, TSPTWInstance> solution)
//    }
//}
