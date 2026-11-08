package es.urjc.etsii.grafo.autoconfig.autollm;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.ToIntFunction;

/** Small, dependency-free SVG figures. All evidence text is escaped before entering markup. */
final class AutoLlmReportCharts {
    private AutoLlmReportCharts() {}
    private static final int LEFT = 110, WIDTH = 660, RIGHT = 790;
    private static final String INPUT = "#1565c0", OUTPUT = "#e65100", WIN = "#2e7d32", TIE = "#546e7a", LOSS = "#b3261e";
    private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("MM-dd HH:mm:ss").withZone(ZoneOffset.UTC);

    static String stageLabel(String stage) {
        return switch (stage) {
            case "GENERATION" -> "Generation";
            case "REPAIR" -> "Generation (repair)";
            case "LOCAL_PREPARATION" -> "Local preparation";
            case "PREPARATION" -> "Preparation";
            case "TUNING" -> "Tuning";
            case "WINNER_EXPORT" -> "Winner export";
            case "COMPARISON" -> "Comparison";
            default -> stage;
        };
    }
    private static String color(String stage) {
        return switch (stage) {
            case "GENERATION", "REPAIR" -> INPUT;
            case "LOCAL_PREPARATION" -> "#6a1b9a";
            case "PREPARATION" -> "#00838f";
            case "TUNING" -> OUTPUT;
            case "WINNER_EXPORT" -> TIE;
            case "COMPARISON" -> WIN;
            default -> "#777777";
        };
    }

    static void timelines(StringBuilder out, List<AutoLlmPortableReport.Trial> trials) {
        var rows = new ArrayList<AutoLlmReportMetrics.Timeline>();
        long origin = Long.MAX_VALUE, end = Long.MIN_VALUE, elapsed = 0;
        for (var trial : trials) {
            var row = AutoLlmReportMetrics.timeline(trial); rows.add(row);
            if (row.start() != null) { origin = Math.min(origin, row.start()); end = Math.max(end, row.end()); elapsed = Math.max(elapsed, row.end() - row.start()); }
        }
        if (origin == Long.MAX_VALUE) { origin = 0; end = 0; }
        for (boolean clock : List.of(false, true)) {
            String kind = clock ? "timeline-clock" : "timeline-elapsed";
            String title = clock ? "Campaign clock timeline (UTC)" : "Trial duration timeline";
            out.append("<h3>").append(title).append("</h3>");
            var legend = new LinkedHashMap<String, String>();
            for (String stage : List.of("GENERATION", "LOCAL_PREPARATION", "PREPARATION", "TUNING", "WINNER_EXPORT", "COMPARISON")) legend.put(stageLabel(stage), color(stage));
            legend.put("Unclassified gap", "#e0e0e0");
            out.append("<p>Dashed borders identify repair attempts; dark borders identify other unfinished or unsuccessful attempts. Overlaps use separate tracks within a trial. Gaps are unclassified, not measured idle CPU time.</p>");
            int block = 0;
            for (var page : pages(rows, AutoLlmReportCharts::timelineHeight)) {
                int height = 0; for (var row : page) height += timelineHeight(row);
                var svg = new Figure(out, kind, ++block, title, height);
                double scale = Math.max(1, clock ? end - origin : elapsed);
                for (int i = 0; i <= 4; i++) {
                    long value = (long) (scale * i / 4);
                    svg.tick(LEFT + WIDTH * i / 4.0, clock ? CLOCK.format(Instant.ofEpochMilli(origin + value)) : AutoLlmReportHtml.time(value));
                }
                int y = 34;
                for (var row : page) {
                    int rowHeight = timelineHeight(row); svg.row(row.trial().saved().number, y, rowHeight);
                    if (row.start() == null) svg.text(LEFT, y + 18, "Timing unavailable");
                    else {
                        long base = clock ? origin : row.start();
                        for (var gap : row.gaps()) svg.rect(position(gap.start() - base, scale), y + 3,
                                WIDTH * (gap.end() - gap.start()) / scale, 14 * row.lanes(), "#e0e0e0", "", "Unclassified gap · " + AutoLlmReportHtml.time(gap.end() - gap.start()));
                        for (var span : row.spans()) {
                            var attempt = span.attempt().saved();
                            String border = attempt.stage == AutoLlmCampaign.AttemptStage.REPAIR ? " stroke=\"#172c39\" stroke-width=\"2\" stroke-dasharray=\"4 2\""
                                    : attempt.status != AutoLlmCampaign.AttemptStatus.SUCCEEDED ? " stroke=\"#172c39\" stroke-width=\"2\"" : "";
                            svg.rect(position(span.start() - base, scale), y + 3 + span.lane() * 16,
                                    WIDTH * (span.end() - span.start()) / scale, 12, color(attempt.stage.name()), border,
                                    stageLabel(attempt.stage.name()) + " · attempt " + attempt.number + " · " + attempt.status + " · " + AutoLlmReportHtml.time(span.attempt().millis())
                                            + " · " + Instant.ofEpochMilli(span.start()) + " → " + Instant.ofEpochMilli(span.end()));
                        }
                    }
                    svg.text(RIGHT, y + 15, AutoLlmReportHtml.time(row.trial().elapsed()) + " · " + row.trial().saved().status);
                    if (row.trial().unknownDurations() > 0) svg.text(RIGHT, y + 31, row.trial().unknownDurations() + " duration(s) unavailable");
                    svg.endRow(); y += rowHeight;
                }
                svg.finish(clock ? "Campaign clock (UTC)" : "Elapsed since first valid attempt timestamp", legend);
            }
        }
    }
    private static int timelineHeight(AutoLlmReportMetrics.Timeline row) { return Math.max(46, row.lanes() * 16 + 20); }
    private static double position(double value, double maximum) { return LEFT + WIDTH * value / maximum; }

    static void tokens(StringBuilder out, List<AutoLlmPortableReport.Trial> trials) {
        var legend = new LinkedHashMap<String, String>(); legend.put("Input", INPUT); legend.put("Output", OUTPUT);
        out.append("<p>Bars show known input and output counts. Labels show reported totals and completeness; these can differ from the bar sum when counters are incomplete. Cached input and reasoning output are already included. Failed and interrupted attempts are included.</p>");
        double maximum = 1;
        for (var trial : trials) maximum = Math.max(maximum, tokenBar(trial.saved().generationUsage));
        int block = 0;
        for (var page : pages(trials, ignored -> 48)) {
            var svg = new Figure(out, "tokens", ++block, "Token usage per trial", page.size() * 48);
            for (int i = 0; i <= 4; i++) svg.tick(LEFT + WIDTH * i / 4.0, String.format(Locale.ROOT, "%,.0f", maximum * i / 4));
            int y = 34;
            for (var trial : page) {
                var usage = trial.saved().generationUsage; var tokens = usage == null ? null : usage.tokens();
                svg.row(trial.saved().number, y, 48);
                if (tokens == null || tokens.inputTokens() == null && tokens.outputTokens() == null) svg.text(LEFT, y + 18, "Input/output unavailable");
                else {
                    double input = known(tokens.inputTokens()), output = known(tokens.outputTokens());
                    if (tokens.inputTokens() != null) svg.rect(LEFT, y + 3, WIDTH * input / maximum, 16, INPUT, "", "Input: " + tokens.inputTokens());
                    if (tokens.outputTokens() != null) svg.rect(position(input, maximum), y + 3, WIDTH * output / maximum, 16, OUTPUT, "", "Output: " + tokens.outputTokens());
                    svg.text(LEFT, y + 36, "Input " + Objects.toString(tokens.inputTokens(), "unavailable") + " · output " + Objects.toString(tokens.outputTokens(), "unavailable"));
                }
                svg.text(RIGHT, y + 17, AutoLlmReportMetrics.total(usage));
                svg.endRow(); y += 48;
            }
            svg.finish("Known input + output tokens", legend);
        }
    }
    private static double known(Long value) { return value == null ? 0 : value.doubleValue(); }
    private static double tokenBar(GenerationEvidence.Summary usage) {
        return usage == null || usage.tokens() == null ? 0 : known(usage.tokens().inputTokens()) + known(usage.tokens().outputTokens());
    }

    static void performance(StringBuilder out, List<AutoLlmPortableReport.Trial> trials) {
        var hosts = new TreeMap<String, List<AutoLlmPortableReport.Trial>>();
        for (var trial : trials) hosts.computeIfAbsent(Objects.toString(trial.saved().targetId, "Unassigned"), ignored -> new ArrayList<>()).add(trial);
        int block = 0;
        for (var host : hosts.entrySet()) {
            out.append("<h3>Host: ").append(AutoLlmReportHtml.escape(host.getKey())).append("</h3>");
            double maximumGap = 1, maximumCount = 1;
            for (var trial : host.getValue()) if (trial.comparison() != null) {
                var comparison = trial.comparison();
                var value = AutoLlmReportMetrics.gap(comparison);
                if (value != null) maximumGap = Math.max(maximumGap, Math.abs(value));
                maximumCount = Math.max(maximumCount, (double) comparison.wins() + comparison.ties() + comparison.losses());
            }
            for (boolean gap : List.of(true, false)) {
                var legend = new LinkedHashMap<String, String>();
                if (!gap) { legend.put("Wins", WIN); legend.put("Ties", TIE); legend.put("Losses", LOSS); }
                for (var page : pages(host.getValue(), ignored -> 40)) {
                    var svg = new Figure(out, gap ? "gap" : "outcomes", ++block, (gap ? "Average SOTA gap" : "Instance outcomes") + " · " + host.getKey(), page.size() * 40);
                    for (int i = 0; i <= 4; i++) svg.tick(LEFT + WIDTH * i / 4.0, gap
                            ? percent(maximumGap * (i / 2.0 - 1), 2)
                            : String.format(Locale.ROOT, "%.1f", maximumCount * i / 4));
                    if (gap) svg.line(LEFT + WIDTH / 2.0, 28, LEFT + WIDTH / 2.0, 34 + page.size() * 40, "#172c39");
                    int y = 34;
                    for (var trial : page) {
                        svg.row(trial.saved().number, y, 40); var comparison = trial.comparison();
                        if (comparison == null) svg.text(LEFT, y + 18, "Excluded · see trial details");
                        else if (gap) {
                            var value = AutoLlmReportMetrics.gap(comparison);
                            if (value == null) svg.text(LEFT, y + 18, "Percentage gap unavailable");
                            else {
                                double x = LEFT + WIDTH / 2.0 + WIDTH / 2.0 * (value / maximumGap);
                                svg.line(LEFT + WIDTH / 2.0, y + 12, x, y + 12, value <= 0 ? WIN : LOSS);
                                svg.circle(x, y + 12, value <= 0 ? WIN : LOSS);
                                svg.text(RIGHT, y + 17, percent(value, 3));
                            }
                        } else {
                            double x = LEFT;
                            int[] counts = {comparison.wins(), comparison.ties(), comparison.losses()}; String[] colors = {WIN, TIE, LOSS};
                            for (int i = 0; i < counts.length; i++) { double width = WIDTH * counts[i] / maximumCount; svg.rect(x, y + 3, width, 16, colors[i], "", Integer.toString(counts[i])); x += width; }
                            svg.text(RIGHT, y + 17, comparison.wins() + " wins · " + comparison.ties() + " ties · " + comparison.losses() + " losses");
                        }
                        svg.endRow(); y += 40;
                    }
                    svg.finish(gap ? "Average gap against host-matched SOTA · negative is better" : "Trial–instance comparisons", legend);
                }
            }
        }
    }

    private static String percent(double value, int decimals) {
        return String.format(Locale.ROOT, Math.abs(value) >= 10000 ? "%.3g%%" : "%." + decimals + "f%%", value == 0 ? 0 : value);
    }

    private static <T> List<List<T>> pages(List<T> rows, ToIntFunction<T> height) {
        var result = new ArrayList<List<T>>(); var page = new ArrayList<T>(); int used = 0;
        for (var row : rows) {
            if (!page.isEmpty() && (page.size() == 12 || used + height.applyAsInt(row) > 576)) { result.add(page); page = new ArrayList<>(); used = 0; }
            page.add(row); used += height.applyAsInt(row);
        }
        if (!page.isEmpty()) result.add(page);
        return result;
    }
    private static final class Figure {
        final StringBuilder out;
        final int height;
        Figure(StringBuilder out, String kind, int block, String title, int rowsHeight) {
            this.out = out; this.height = rowsHeight + 112;
            String id = kind + "-" + block;
            out.append("<figure class=report-chart data-chart=\"").append(kind).append("\"><figcaption>").append(AutoLlmReportHtml.escape(title)).append(" · figure ").append(block).append("</figcaption>");
            out.append("<svg xmlns=\"http://www.w3.org/2000/svg\" role=\"img\" aria-labelledby=\"").append(id).append("-title ").append(id).append("-desc\" viewBox=\"0 0 1100 ").append(height).append("\">");
            out.append("<title id=\"").append(id).append("-title\">").append(AutoLlmReportHtml.escape(title)).append("</title><desc id=\"").append(id).append("-desc\">One row per trial. Exact values and missing-evidence explanations are provided in the accompanying tables and trial details.</desc>");
        }
        void row(int trial, int y, int height) {
            out.append("<g data-trial=\"").append(trial).append("\">");
            line(LEFT, y + height - 5, 1080, y + height - 5, "#edf1f3");
            out.append("<a href=\"#trial-").append(trial).append("\">"); text(6, y + 17, "Trial " + trial); out.append("</a>");
        }
        void endRow() { out.append("</g>"); }
        void tick(double x, String label) {
            line(x, 28, x, height - 72, "#d9e2e7");
            out.append("<text x=\"").append(n(x)).append("\" y=\"18\" text-anchor=\"middle\">").append(AutoLlmReportHtml.escape(label)).append("</text>");
        }
        void text(double x, double y, String text) {
            out.append("<text x=\"").append(n(x)).append("\" y=\"").append(n(y)).append("\">").append(AutoLlmReportHtml.escape(text)).append("</text>");
        }
        void rect(double x, double y, double width, double height, String color, String attributes, String title) {
            if (width <= 0) return;
            out.append("<rect class=\"chart-bar\" x=\"").append(n(x)).append("\" y=\"").append(n(y)).append("\" width=\"").append(n(Math.max(.5, width))).append("\" height=\"").append(n(height)).append("\" fill=\"").append(color).append("\"").append(attributes).append("><title>").append(AutoLlmReportHtml.escape(title)).append("</title></rect>");
        }
        void line(double x1, double y1, double x2, double y2, String color) {
            out.append("<line x1=\"").append(n(x1)).append("\" y1=\"").append(n(y1)).append("\" x2=\"").append(n(x2)).append("\" y2=\"").append(n(y2)).append("\" stroke=\"").append(color).append("\"/>");
        }
        void circle(double x, double y, String color) { out.append("<circle cx=\"").append(n(x)).append("\" cy=\"").append(n(y)).append("\" r=\"4\" fill=\"").append(color).append("\"/>"); }
        void finish(String axis, Map<String, String> legend) {
            text(LEFT, height - 40, axis); int x = 6;
            for (var entry : legend.entrySet()) {
                out.append("<rect x=\"").append(x).append("\" y=\"").append(height - 24).append("\" width=\"12\" height=\"10\" fill=\"").append(entry.getValue()).append("\"/>");
                text(x + 18, height - 14, entry.getKey()); x += 30 + entry.getKey().length() * 7;
            }
            out.append("</svg></figure>");
        }
        private static String n(double value) { return String.format(Locale.ROOT, "%.2f", value); }
    }
}
