//package es.urjc.etsii.grafo.CAP;
//
//import com.fasterxml.jackson.core.JsonGenerator;
//import com.fasterxml.jackson.databind.DeserializationFeature;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import es.urjc.etsii.grafo.metrics.TimeValue;
//import es.urjc.etsii.grafo.util.TimeUtil;
//import me.tongfei.progressbar.ProgressBar;
//import me.tongfei.progressbar.ProgressBarBuilder;
//import me.tongfei.progressbar.ProgressBarStyle;
//
//import java.io.File;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.util.*;
//import java.util.regex.Pattern;
//import java.util.stream.Stream;
//
//import static java.lang.Double.NaN;
//
//public class RPDParser {
//
//    private static final Pattern FILE_PATTERN = Pattern.compile("([^_]+)_(.+)_([^_]+)_([^_]+)_\\.json");
//    private static final Set<String> allowedAlgs = null; // Set.of("");
//    private static final ObjectMapper jsonParser = new ObjectMapper()
//            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
//            .enable(JsonGenerator.Feature.IGNORE_UNKNOWN);
//
//    private static final Map<String, List<TreeSet<TimeValue>>> data = new HashMap<>();
//    private static final Map<String, Double> minV = new HashMap<>();
//
//
////    public static void main(String[] args) throws IOException{_main(args);}
//
//    public static void _main(String[] args) throws IOException {
//        if (args.length != 3) {
//            System.out.println("Usage: java -jar file.jar solutionsFolder instanceName bestKnownValuesFile");
//            System.exit(-1);
//        }
//
//        loadMinimumValues(args[2]);
//        long count;
//        try (var files = getFilteredFiles(args[0], args[1])) {
//            count = files.count(); // consumes the stream
//        }
//        try (var files = getFilteredFiles(args[0], args[1])) {
//            ProgressBar.wrap(files, progressBar(count, "Parsing data"))
//                    .map(Path::toFile)
//                    .map(RPDParser::parseWorkUnit)
//                    .filter(e -> allowedAlgs == null || allowedAlgs.contains(e.data().algorithm.name()))
//                    .forEach(RPDParser::extract);
//        }
//
//
//        long nanoInterval = 500_000_000L;
//        long maxTime = 120_000_000_000L;
//        long skipFirst = 0L;
//
//        long nDatapoints = (maxTime - skipFirst) / nanoInterval;
//        boolean remainder = (maxTime - skipFirst) % nanoInterval != 0;
//        if(remainder) nDatapoints++;
//        String[][] result = new String[(int) (nDatapoints + 1)][data.size() * 2];
//
//        int col = 0;
//        for (var e : ProgressBar.wrap(data.entrySet(), progressBar(data.size(), "Generating output"))) {
//            result[0][col] = e.getKey();
//            result[0][col + 1] = "";
//            int row = 1;
//            for (long time = skipFirst; time < maxTime; time += nanoInterval) {
//                double total = 0;
//                var trees = e.getValue();
//                int nFails = 0;
//                for (var tree : trees) {
//                    var search = new TimeValue(time, -1);
//                    var v = tree.floor(search);
//                    if (v == null) {
//                        nFails++;
//                    } else {
//                        total += v.value();
//                    }
//                }
//                double avg = NaN;
//                // Only compute average if at least 50% of the datapoints are present
//                if(nFails < trees.size() / 2){
//                    avg = total / (trees.size() - nFails);
//                }
//                result[row][col] = String.valueOf((double) time / TimeUtil.NANOS_IN_SECOND);
//                result[row][col + 1] = String.format("%.4f", avg);
//                row++;
//            }
//            col += 2;
//        }
//
//        StringBuilder sb = new StringBuilder();
//        for (String[] strings : result) {
//            for (String string : strings) {
//                sb.append(string).append("\t");
//            }
//            sb.append("\n");
//        }
//        System.out.println(sb);
//    }
//
//    private static ProgressBarBuilder progressBar(long count, String Parsing_data) {
//        return new ProgressBarBuilder()
//                .setInitialMax(count)
//                .setTaskName(Parsing_data)
//                .continuousUpdate()
//                .setUnit(" files", 1)
//                .setStyle(ProgressBarStyle.COLORFUL_UNICODE_BLOCK);
//    }
//
//    private static void loadMinimumValues(String path) throws IOException {
//        try (var lines = Files.lines(Path.of(path))) {
//            lines.map(l -> l.split("\t"))
//                    .forEach(parts -> {
//                        minV.put(parts[0], Double.parseDouble(parts[1]));
//                    });
//        }
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
//
//    private static void extract(RPDFile f) {
//        var wu = f.data;
//        if (wu.iteration.equals("bestiter")) {
//            return;
//        }
//        String algorithmName = wu.algorithm().name();
//
//        data.putIfAbsent(algorithmName, new ArrayList<>());
//        var relativeTree = relativize(f.filename, wu.metrics().metrics().get("Default").values());
//        data.get(algorithmName).add(relativeTree);
//    }
//
//    private static TreeSet<TimeValue> relativize(String filename, TreeSet<TimeValue> values){
//        var m = FILE_PATTERN.matcher(filename);
//        if(!m.matches()){
//            throw new IllegalArgumentException("Invalid filename: " + filename);
//        }
//        var instanceName = m.group(2);
//        double referenceValue = minV.get(instanceName);
//        var newTree = new TreeSet<TimeValue>();
//        for(var tv: values){
//            var newValue = (tv.value() - referenceValue) / referenceValue;
//            newTree.add(new TimeValue(tv.instant(), newValue));
//        }
//        return newTree;
//    }
//
//    record Metric(String name, TreeSet<TimeValue> values) {
//    }
//
//    record Metrics(long referenceNanoTime, Map<String, Metric> metrics) {
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
//    record RPDData(AlgName algorithm, String iteration, Metrics metrics) { }
//
//    record RPDFile(RPDData data, String filename){}
//}
