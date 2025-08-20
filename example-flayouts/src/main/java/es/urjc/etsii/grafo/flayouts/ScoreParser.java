//package es.urjc.etsii.grafo.CAP;
//
//import com.fasterxml.jackson.core.JsonGenerator;
//import com.fasterxml.jackson.databind.DeserializationFeature;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import es.urjc.etsii.grafo.metrics.TimeValue;
//import me.tongfei.progressbar.ProgressBar;
//import me.tongfei.progressbar.ProgressBarBuilder;
//import me.tongfei.progressbar.ProgressBarStyle;
//
//import java.io.File;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.TreeSet;
//import java.util.regex.Pattern;
//import java.util.stream.Collectors;
//import java.util.stream.Stream;
//
//public class ScoreParser {
//
//    private static final Pattern FILE_PATTERN = Pattern.compile("([^_]+)_(.+)_([^_]+)_([^_]+)_\\.json");
//    private static final ObjectMapper jsonParser = new ObjectMapper()
//            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
//            .enable(JsonGenerator.Feature.IGNORE_UNKNOWN);
//
//    private static final Map<String, List<TreeSet<TimeValue>>> data = new HashMap<>();
//
////    public static void main(String[] args) throws IOException {_main(args);}
//    public static void _main(String[] args) throws IOException {
//        if (args.length != 2) {
//            System.out.println("Usage: java -jar file.jar solutionsFolder instancePattern");
//            System.exit(-1);
//        }
//
//        long count;
//        try (var files = getFilteredFiles(args[0], args[1])) {
//            count = files.count(); // consumes the stream
//        }
//
//        try (var files = getFilteredFiles(args[0], args[1])) {
//            var taskResult = ProgressBar.wrap(files, progressBar(count, "Parsing data"))
//                    .map(Path::toFile)
//                    .map(ScoreParser::parseWorkUnit)
//                    .map(ScoreParser::extract)
//                    .collect(Collectors.joining("\n"));
//            System.out.println(taskResult);
//        }
//    }
//
//    private static ProgressBarBuilder progressBar(long count, String taskName) {
//        return new ProgressBarBuilder()
//                .setInitialMax(count)
//                .setTaskName(taskName)
//                .continuousUpdate()
//                .setUnit(" files", 1)
//                .setStyle(ProgressBarStyle.COLORFUL_UNICODE_BLOCK);
//    }
//
//    private static Stream<Path> getFilteredFiles(String arg, String arg1) throws IOException {
//        return Files.list(Path.of(arg))
//                .filter(f -> f.toString().contains(arg1));
//    }
//
//    private static RPDFile parseWorkUnit(File f) {
//        try {
//            var data = jsonParser.readValue(f, RPDData.class);
//            return new RPDFile(data, f.getName());
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    private static String extract(RPDFile f) {
//        var m = FILE_PATTERN.matcher(f.filename);
//        if(!m.matches()){
//            throw new IllegalArgumentException("Invalid filename: " + f.filename);
//        }
//        if (f.data.iteration().equals("bestiter")) {
//            return ""; // Should have been already filtered but just in case, append nothing
//        }
//        if(!m.group(4).equals(f.data.iteration())){
//            throw new IllegalArgumentException("Iteration mismatch: %s != %s".formatted(m.group(4), f.data.iteration()));
//        }
//
//        // instanceName, algorithmName, iteration, score
//        return "%s\t%s\t%s\t%s".formatted(m.group(2), m.group(3), f.data.iteration(), f.data.score());
//    }
//
//    record AlgName(String shortName, String name) {
//        @Override
//        public String name() {
//            if(shortName != null && !shortName.isBlank()){
//                return shortName;
//            }
//            if(name != null && !name.isBlank()){
//                return name;
//            }
//            throw new IllegalArgumentException("Invalid algorithm name: " + this);
//        }
//    }
//
//    record RPDData(AlgName algorithm, String iteration, SolutionScore solution) {
//        public double score(){
//            return solution.score();
//        }
//
//        public String algorithmName(){
//            return algorithm.name();
//        }
//    }
//
//    record RPDFile(RPDData data, String filename){}
//    record SolutionScore(double score){}
//}
