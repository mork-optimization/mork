//package es.urjc.etsii.grafo.__RNAME__.io;
//
//import es.urjc.etsii.grafo.__RNAME__.model.__RNAME__Instance;
//import es.urjc.etsii.grafo.__RNAME__.model.__RNAME__Solution;
//import es.urjc.etsii.grafo.io.serializers.SolutionSerializer;
//
//import java.io.BufferedWriter;
//import java.io.IOException;
//
///**
// * Provides a custom implementation for exporting solutions to disk.
// */
//public class __RNAME__SolutionExporter extends SolutionSerializer<__RNAME__Solution, __RNAME__Instance> {
//
//    /**
//     * Create a new solution serializer with the given config
//     *
//     * @param config Common solution serializer configuration
//     */
//    protected __RNAME__SolutionExporter(__RNAME__SolutionExporterConfig config) {
//        super(config);
//    }
//
//    @Override
//    public void export(BufferedWriter writer, __RNAME__Solution solution) throws IOException {
//        // Export data to the BufferedWriter. If more control is desired,
//        // for example if you want to create multiple files for each solution, ot write binary data
//        // ignore this method (throw new UnsupportedOperationException()) and override this one instead:
//        // public void export(String folder, String suggestedFilename, __RNAME__Solution solution)
//    }
//}
