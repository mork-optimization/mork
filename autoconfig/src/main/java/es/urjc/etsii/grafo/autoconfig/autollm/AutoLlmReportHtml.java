package es.urjc.etsii.grafo.autoconfig.autollm;

import tools.jackson.databind.JsonNode;
import java.util.*;

/** Standalone HTML with no external resources or executable evidence content. */
public final class AutoLlmReportHtml {
    private AutoLlmReportHtml() {}
    static String escape(Object text) {
        return Objects.toString(text, "Unavailable").replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }
    static String time(Long millis) {
        if (millis == null) return "Unavailable";
        long seconds = millis / 1000;
        return "%dh %02dm %02ds".formatted(seconds / 3600, seconds / 60 % 60, seconds % 60);
    }
    private static String number(Double value) { return value == null || !Double.isFinite(value) ? "Unavailable" : String.format(Locale.ROOT, "%.3f", value == 0 ? 0 : value); }
    private static String gap(AutoLlmComparisonReport.Run run) { var value = AutoLlmReportMetrics.gap(run); return value == null ? "Unavailable" : number(value) + "%"; }
    private static void row(StringBuilder out, Object... cells) {
        out.append("<tr>"); for (Object cell : cells) out.append("<td>").append(escape(cell)).append("</td>"); out.append("</tr>");
    }
    private static void table(StringBuilder out, String... headers) {
        out.append("<div class=table-wrap><table><thead><tr>"); for (String header : headers) out.append("<th scope=col>").append(escape(header)).append("</th>"); out.append("</tr></thead><tbody>");
    }
    private static void endTable(StringBuilder out) { out.append("</tbody></table></div>"); }
    private static void paragraph(StringBuilder out, Object text) { out.append("<p>").append(escape(text)).append("</p>"); }
    private static void json(StringBuilder out, Object value) {
        if (value == null) { paragraph(out, "Unavailable in saved evidence"); return; }
        out.append("<pre>").append(escape(AutoLlmFileUtil.JSON.writerWithDefaultPrettyPrinter().writeValueAsString(value))).append("</pre>");
    }
    private static void usage(StringBuilder out, JsonNode summary) {
        if (summary == null || summary.isMissingNode() || summary.isNull()) { paragraph(out, "Token usage unavailable"); return; }
        paragraph(out, "Usage completeness: " + summary.path("status").asString("UNAVAILABLE"));
        paragraph(out, "Attempt coverage: " + summary.path("completeAttempts").asInt(0) + "/" + summary.path("attempts").asInt(0)
                + " complete; " + summary.path("partialAttempts").asInt(0) + " partial; " + summary.path("unavailableAttempts").asInt(0) + " unavailable");
        var tokens = summary.path("tokens");
        table(out, "Input", "Cached input¹", "Cache-write input", "Output", "Reasoning output¹", "Total");
        var values = new ArrayList<String>();
        for (String key : List.of("inputTokens", "cachedInputTokens", "cacheWriteInputTokens", "outputTokens", "reasoningOutputTokens", "totalTokens"))
            values.add(tokens.path(key).isNumber() ? tokens.path(key).asString() : "Unavailable");
        row(out, values.toArray()); endTable(out);
        paragraph(out, "¹ Cached input is part of input; reasoning output is part of output. Partial totals include known counts only. No monetary cost is calculated.");
    }
    static String approach(Object winner) {
        if (winner == null) return "Not available";
        var node = AutoLlmFileUtil.JSON.valueToTree(winner);
        return node.path("$component").asString(node.path("component").asString("See winner specification"));
    }
    public static String render(AutoLlmPortableReport.Snapshot report) {
        var c = report.campaign(); var out = new StringBuilder(32768);
        out.append("""
                <!doctype html><html lang="en"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
                <title>AutoLLM campaign report</title><style>
                :root{color-scheme:light;--ink:#172c39;--muted:#506572;--line:#d9e2e7;--accent:#166b71}
                *{box-sizing:border-box}body{margin:0;background:#f1f5f7;color:var(--ink);font:15px/1.55 system-ui,sans-serif}
                main{max-width:1440px;margin:auto;padding:40px 32px}header{border-top:6px solid var(--accent);padding:24px 0}
                h1{font-size:34px;margin:8px 0}h2{font-size:24px;margin:0 0 18px}h3{margin:26px 0 10px}h4{margin:18px 0 8px}
                .eyebrow{font-size:12px;letter-spacing:.13em;text-transform:uppercase;color:var(--accent);font-weight:700}
                .muted,small{color:var(--muted)}section{padding:26px;background:white;border:1px solid var(--line);border-radius:10px;margin:22px 0}
                .cards{display:flex;flex-wrap:wrap;gap:16px}.card{flex:1;min-width:160px;padding:16px;background:#f2f7f8;border-radius:8px}.card strong{display:block;font-size:25px}
                a{color:var(--accent)}nav{display:flex;gap:18px;flex-wrap:wrap}.table-wrap{overflow-x:auto}table{width:100%;border-collapse:collapse;font-size:13px}
                th,td{padding:10px 12px;text-align:left;vertical-align:top;border-bottom:1px solid var(--line);overflow-wrap:anywhere}th{background:#ecf3f5;color:#284953}
                tr:nth-child(even){background:#fafcfd}pre{white-space:pre-wrap;overflow-wrap:anywhere;background:#f3f6f7;padding:16px;font-size:12px;border-radius:6px}
                .notice{border-left:4px solid #a96a11;padding:12px 16px;background:#fff8e9}.component{border-left:3px solid var(--accent);padding:0 16px;margin:20px 0}
                .trial{scroll-margin-top:20px}.attempt{border-top:1px solid var(--line);margin-top:20px;padding-top:8px}code{overflow-wrap:anywhere}p{overflow-wrap:anywhere}
                .report-chart{margin:22px 0;break-inside:avoid}.report-chart figcaption{font-weight:600;margin:0 0 12px;overflow-wrap:anywhere}.report-chart svg{display:block;width:100%;height:auto;font:12px system-ui,sans-serif;color:var(--ink)}.report-chart text{fill:currentColor}
                @media(max-width:700px){main{padding:16px}section{padding:16px}h1{font-size:28px}}
                @media print{@page{size:A4 landscape;margin:12mm}body{background:white;font-size:10pt;print-color-adjust:exact;-webkit-print-color-adjust:exact}main{padding:0;max-width:none}section{border:0;border-radius:0;padding:12px 0}.trial{break-before:page}thead{display:table-header-group}tr,.component,.card{break-inside:avoid}.table-wrap{overflow:visible}table{font-size:8pt}th,td{padding:5px}pre{font-size:8pt;break-inside:avoid}nav{display:none}a{color:inherit;text-decoration:none}h2,h3,h4{break-after:avoid}}
                </style></head><body><main><header><div class=eyebrow>Mork · AutoLLM</div><h1>Campaign report</h1>
                """);
        paragraph(out, c.id + " · " + c.status + " · Snapshot " + report.at());
        if (!c.status.completed()) out.append("<p class=notice>Partial campaign snapshot. Unfinished or failed trials remain visible; only complete successful comparisons contribute to headline performance.</p>");
        out.append("<nav aria-label=Contents><a href=#overview>Overview</a><a href=#trials>Trial summary</a><a href=#timing>Timelines</a><a href=#tokens>Tokens</a><a href=#performance>Performance</a><a href=#failures>Failures and retries</a><a href=#evidence>Configuration and evidence</a></nav></header>");
        out.append("<section id=overview><h2>Experiment overview</h2><div class=cards>");
        card(out, "Trials", c.trials.size()); card(out, "Successful trials", report.statuses().getOrDefault("SUCCEEDED",0));
        card(out, "Failed trials", report.statuses().getOrDefault("FAILED",0)); card(out, "Complete comparisons", report.includedTrials());
        card(out, "Wall-clock duration", time(report.elapsed())); out.append("</div>");
        paragraph(out, report.durationBasis());
        paragraph(out, "Frozen dataset sizes · training: " + Objects.toString(report.datasetSizes().get("training"), "Unavailable")
                + " · testing: " + Objects.toString(report.datasetSizes().get("comparison"), "Unavailable")
                + " · overlap: " + Objects.toString(report.datasetSizes().get("overlap"), "Unavailable"));
        paragraph(out, "Summed recorded attempt durations: " + time(report.attemptMillis()) + ". Parallel and nested attempts may overlap; this is not wall-clock time or CPU time.");
        table(out, "Trial state", "Count"); for (var status : report.statuses().entrySet()) row(out, status.getKey(), status.getValue()); endTable(out);
        out.append("<h3>Host-matched performance</h3>");
        paragraph(out, "Objective: " + Objects.toString(report.selection().objective(), "not selected") + " · Direction: " + Objects.toString(report.selection().mode(), "unavailable"));
        out.append("<div class=cards>"); card(out, "Instance wins", report.wins()); card(out, "Instance ties", report.ties()); card(out, "Instance losses", report.losses()); out.append("</div>");
        paragraph(out, "Counts are trial–instance comparisons, not trial outcomes. Each instance uses the mean across repetitions. Gap is 100 × (proposal − SOTA) / |SOTA| for minimization, with reversed numerator for maximization. Negative means better. Instances have equal weight; zero references make percentage gap undefined. Tie tolerance: 1e-9 × max(1, |SOTA|, |proposal|). No cross-host ranking is implied.");
        for (String warning : report.warnings()) paragraph(out, warning);
        out.append("<h3>Campaign token usage</h3>"); usage(out, AutoLlmFileUtil.JSON.valueToTree(c.generationUsage)); out.append("</section>");
        out.append("<section id=trials><h2>Trial summary</h2>");
        table(out, "Trial", "Status / host", "Elapsed", "Total tokens", "Average SOTA gap", "Wins", "Ties", "Losses", "Generated components", "Irace winner approach");
        for (var trial : report.trials()) {
            var t = trial.saved(); var names = new ArrayList<String>(); for (var component : trial.components()) names.add(component.name());
            var comparison = trial.comparison();
            out.append("<tr><td><a href=#trial-").append(t.number).append(">Trial ").append(t.number).append("</a></td>");
            for (Object cell : List.of(t.status + " / " + Objects.toString(t.targetId,"unassigned"), time(trial.elapsed()), AutoLlmReportMetrics.total(t.generationUsage), gap(comparison),
                    comparison == null ? "—" : comparison.wins(), comparison == null ? "—" : comparison.ties(), comparison == null ? "—" : comparison.losses(),
                    names.isEmpty() ? "Unavailable" : String.join(", ", names), approach(trial.winner()))) out.append("<td>").append(escape(cell)).append("</td>");
            out.append("</tr>");
        }
        endTable(out); paragraph(out, "Unavailable gaps and dashes are not zero results. Exclusions and partial measurements appear in each trial."); out.append("</section>");
        out.append("<section id=timing><h2>Trial timing</h2>");
        paragraph(out, "Every trial is included. Running intervals end at snapshot " + report.at() + ". Summed stage durations may overlap; elapsed time uses the span of valid timestamps.");
        AutoLlmReportCharts.timelines(out, report.trials()); out.append("</section>");
        out.append("<section id=tokens><h2>Token usage per trial</h2>"); AutoLlmReportCharts.tokens(out, report.trials());
        table(out, "Trial", "Input", "Output", "Total / completeness", "Attempt coverage");
        for (var trial : report.trials()) {
            var usage = trial.saved().generationUsage; var tokens = usage == null ? null : usage.tokens();
            out.append("<tr>"); trialLink(out, trial.saved().number);
            for (Object value : List.of(tokens == null ? "Unavailable" : Objects.toString(tokens.inputTokens(), "Unavailable"),
                    tokens == null ? "Unavailable" : Objects.toString(tokens.outputTokens(), "Unavailable"), AutoLlmReportMetrics.total(usage), AutoLlmReportMetrics.coverage(usage)))
                out.append("<td>").append(escape(value)).append("</td>");
            out.append("</tr>");
        }
        endTable(out); paragraph(out, "Campaign totals deduplicate responses across all trials; do not recompute them by summing displayed trial totals."); out.append("</section>");
        out.append("<section id=performance><h2>Performance by execution host</h2>");
        paragraph(out, "Only complete successful comparisons are plotted. Trials are ordered by number within each host, with host-specific scales. Negative gap means better than SOTA; no cross-host ranking is implied.");
        AutoLlmReportCharts.performance(out, report.trials());
        table(out, "Trial", "Excluded / undefined gap reason");
        for (var trial : report.trials()) if (AutoLlmReportMetrics.gap(trial.comparison()) == null) {
            out.append("<tr>"); trialLink(out, trial.saved().number);
            out.append("<td>").append(escape(trial.exclusion() == null ? "Percentage gap undefined for the saved reference values" : trial.exclusion())).append("</td></tr>");
        }
        endTable(out); out.append("</section>");
        failures(out, report.trials());
        for (var trial : report.trials()) trial(out, trial);
        out.append("<section id=evidence><h2>Configuration and evidence</h2>");
        if (report.datasetOrigin() != null && !report.datasetOrigin().isEmpty()) {
            paragraph(out, "Dataset selection provenance. Screening measurements below are historical and refer to their original baseline and host.");
            out.append("<details><summary>Dataset selection and historical screening</summary><pre>").append(escape(report.datasetOrigin().toPrettyString())).append("</pre></details>");
        }
        paragraph(out, "Baseline commit: " + Objects.toString(c.baselineCommit, "Unavailable"));
        paragraph(out, "Campaign created: " + c.createdAt + " · execution started: " + Objects.toString(c.startedAt,"Unavailable") + " · stopped: " + Objects.toString(c.stoppedAt,"Unavailable"));
        if (c.config != null) {
            paragraph(out, "Generation agent: " + c.config.generation().agent() + " · model: " + c.config.generation().model() + " · reasoning effort: " + Objects.toString(c.config.generation().reasoningEffort(), "agent default"));
            out.append("<h3>Budgets and execution settings</h3>");
            json(out, Map.of("tuning", c.config.tuning(), "comparison", c.config.comparison(), "preparation", c.config.preparation(), "execution", c.config.execution()));
        }
        out.append("<h3>Execution hosts</h3>");
        for (var target : c.targets.entrySet()) {
            paragraph(out, target.getKey() + " · " + target.getValue().status);
            json(out, target.getValue().host);
        }
        out.append("<h3>Toolchain versions</h3>"); json(out, c.versions);
        paragraph(out, "This report is a portable snapshot. Original journals, source snapshots, solution exports, build logs and transcripts remain separate campaign artifacts. Generated descriptions record the model’s claimed mechanism, not evidence of individual component contribution.");
        out.append("</section></main></body></html>"); return out.toString();
    }
    private static void card(StringBuilder out, String label, Object value) { out.append("<div class=card>").append(escape(label)).append("<strong>").append(escape(value)).append("</strong></div>"); }
    private static void trialLink(StringBuilder out, int number) { out.append("<td><a href=#trial-").append(number).append(">Trial ").append(number).append("</a></td>"); }
    private static void failures(StringBuilder out, List<AutoLlmPortableReport.Trial> trials) {
        out.append("<section id=failures><h2>Failures, interruptions and retries</h2>");
        paragraph(out, "Historical attempt outcomes are counted separately from current trial problems. A recovered trial can contain failed attempts. Quota/capacity limits and interruptions are not counted as failed attempts.");
        var groups = AutoLlmReportMetrics.failures(trials);
        if (groups.isEmpty()) paragraph(out, "No unsuccessful attempt outcomes recorded.");
        else {
            table(out, "Stage", "Outcome", "Category", "Code", "Attempts", "Affected trials");
            for (var group : groups) {
                out.append("<tr>"); var key = group.key();
                for (Object value : List.of(AutoLlmReportCharts.stageLabel(key.stage()), key.outcome(), key.category(), Objects.toString(key.code(), "UNKNOWN"), group.attempts())) out.append("<td>").append(escape(value)).append("</td>");
                out.append("<td>"); for (int trial : group.trials()) out.append("<a href=#trial-").append(trial).append(">Trial ").append(trial).append("</a> "); out.append("</td></tr>");
            }
            endTable(out);
        }
        out.append("<h3>Current problems and recorded retry counters</h3>");
        paragraph(out, "These are saved counters, not reconstructed lifetime totals. Infrastructure retry counters can reset when a trial resumes. Charged repair turns differ from the number of recorded repair attempts.");
        table(out, "Trial", "Current status / problem", "Charged repair turns", "Infrastructure retry counter", "Connection retry counters by job");
        for (var trial : trials) {
            var t = trial.saved(); out.append("<tr>"); trialLink(out, t.number);
            for (Object value : List.of(t.status + " · " + (t.problem != null ? t.problem.code() + ": " + t.problem.message() : Objects.toString(t.failure, "No current problem recorded")),
                    t.repairTurns, t.infrastructureRetries, t.connectionRetries.isEmpty() ? "None recorded" : t.connectionRetries)) out.append("<td>").append(escape(value)).append("</td>");
            out.append("</tr>");
        }
        endTable(out); out.append("</section>");
    }
    private static void trial(StringBuilder out, AutoLlmPortableReport.Trial trial) {
        var t = trial.saved(); out.append("<section class=trial id=trial-").append(t.number).append("><div class=eyebrow>Trial ").append(t.number).append("</div><h2>").append(escape(t.status)).append("</h2>");
        paragraph(out, "Host: " + Objects.toString(t.targetId,"Unassigned") + " · assignment: " + Objects.toString(t.activeAssignmentId,"Unavailable") + " · repairs: " + t.repairTurns + " · infrastructure retries: " + t.infrastructureRetries);
        paragraph(out, "Pilot seed: " + t.pilotSeed + " · tuning seed: " + t.tuningSeed + " · comparison seed: " + t.comparisonSeed);
        if (trial.exclusion() != null) out.append("<p class=notice>").append(escape(trial.exclusion())).append("</p>");
        if (t.problem != null) paragraph(out, t.problem.message()); else if (t.failure != null) paragraph(out, t.failure);
        for (String warning : trial.warnings()) paragraph(out, warning);
        out.append("<h3>Stage timing</h3>"); table(out, "Stage (all attempts)", "Duration");
        for (var stage : trial.stages()) row(out, AutoLlmReportCharts.stageLabel(stage.name()), time(stage.millis())); endTable(out);
        paragraph(out, "Elapsed trial span: " + time(trial.elapsed()) + " · summed known attempts: " + time(trial.attemptMillis()));
        out.append("<h3>Generated components</h3>");
        if (trial.components().isEmpty()) paragraph(out, "Structured component inventory unavailable. Saved generation notes appear below.");
        for (var component : trial.components()) {
            out.append("<div class=component><h4>").append(escape(component.name())).append("</h4>");
            paragraph(out, component.role() + " · " + (component.verified() ? "Verified final inventory" : "Manifest only; final verification unavailable") + (component.selected() ? " · Used by winner" : ""));
            paragraph(out, component.description()); out.append("</div>");
        }
        out.append("<h3>Winner composition and tuned parameters</h3>"); paragraph(out, approach(trial.winner())); json(out, trial.winner());
        out.append("<h3>").append(trial.comparison() == null ? "Partial comparison evidence" : "Complete comparison").append("</h3>");
        table(out, "Instance", "SOTA mean", "Proposal mean", "SOTA gap %", "Outcome", "Successful repetitions (SOTA / proposal)", "Evaluation outcomes");
        for (var instance : trial.partial()) {
            var sota = instance.algorithms().get("SOTA"); var proposal = instance.algorithms().get("IraceProposal");
            AutoLlmComparisonReport.Row complete = null;
            if (trial.comparison() != null) for (var candidate : trial.comparison().instances()) if (candidate.instanceId().equals(instance.instanceId())) complete = candidate;
            row(out, instance.instance(), sota == null ? "Unavailable" : number(sota.objective().mean()), proposal == null ? "Unavailable" : number(proposal.objective().mean()),
                    complete == null || complete.improvementPercent() == null ? "Unavailable" : number(-complete.improvementPercent()), complete == null ? "Excluded" : complete.outcome(),
                    (sota == null ? 0 : sota.objective().count()) + " / " + (proposal == null ? 0 : proposal.objective().count()), instance.counts());
        }
        endTable(out); out.append("<h3>Trial token usage</h3>"); usage(out, AutoLlmFileUtil.JSON.valueToTree(t.generationUsage));
        out.append("<h3>Generation and repair overhead</h3>");
        table(out, "Kind", "Recorded attempts", "Input", "Output", "Total / completeness", "Attempt coverage", "Summed attempt duration");
        for (var stage : AutoLlmReportMetrics.usageStages(trial)) {
            var usage = stage.usage(); var tokens = usage.tokens();
            row(out, stage.name(), usage.attempts(), tokens.inputTokens(), tokens.outputTokens(), AutoLlmReportMetrics.total(usage), AutoLlmReportMetrics.coverage(usage),
                    usage.attempts() == 0 ? "Not started" : time(stage.knownMillis()) + (stage.unknownDurations() == 0 ? "" : " known; " + stage.unknownDurations() + " duration(s) unavailable"));
        }
        endTable(out); paragraph(out, "Responses are attributed to the first recorded attempt containing them; repeated response IDs add no further tokens. Counts include failed and interrupted attempts. Charged repair turns: " + t.repairTurns + ". Durations can overlap and are not wall-clock or CPU time.");
        out.append("<h3>Attempt details</h3>");
        for (var attempt : trial.attempts()) {
            var a = attempt.saved(); out.append("<article class=attempt><h4>").append(escape("Attempt " + a.number + " · " + a.stage + " · " + a.status)).append("</h4>");
            paragraph(out, a.startedAt + " → " + Objects.toString(a.finishedAt,"Snapshot cutoff / unavailable") + " · " + time(attempt.millis()));
            if (a.problem != null) paragraph(out, a.problem.message());
            if (!attempt.substages().isEmpty()) {
                table(out, "Preparation substage", "Observed duration"); for (var interval : attempt.substages()) row(out, interval.name(), time(interval.millis())); endTable(out);
                paragraph(out, "Substage intervals are controller-observed and can include transfers and waits.");
            } else if (a.stage == AutoLlmCampaign.AttemptStage.PREPARATION || a.stage == AutoLlmCampaign.AttemptStage.LOCAL_PREPARATION) paragraph(out, "Preparation substage timing unavailable");
            if (a.stage.generation()) {
                usage(out, attempt.usage() == null ? null : attempt.usage().path("summary"));
                if (attempt.model() != null) { out.append("<h4>Model selection</h4>"); json(out, attempt.model()); }
                if (attempt.manifest() != null) {
                    for (String field : List.of("inventory", "metaheuristics", "components", "tests", "limitations")) {
                        out.append("<h4>").append(escape(field)).append("</h4><ul>");
                        for (var value : attempt.manifest().path(field)) out.append("<li>").append(escape(value.asString())).append("</li>"); out.append("</ul>");
                    }
                }
            }
            paragraph(out, "Evidence: " + a.directory); out.append("</article>");
        }
        out.append("<h3>Evidence locations</h3><ul>"); for (String file : trial.evidence()) out.append("<li><code>").append(escape(file)).append("</code></li>");
        out.append("</ul><a href=#trials>Back to trial summary</a></section>");
    }
}
