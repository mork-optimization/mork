cran_repo <- "https://cloud.r-project.org"

personal_lib_path <- Sys.getenv("R_LIBS_USER")
if (!dir.exists(personal_lib_path)) {
  dir.create(personal_lib_path, recursive = TRUE)
}
.libPaths(c(personal_lib_path, .libPaths()))

if (!requireNamespace("remotes", quietly = TRUE)) {
  install.packages("remotes", type = "source", repos = cran_repo)
}
library(remotes)

if (!requireNamespace("curl", quietly = TRUE)) {
  install.packages("curl", repos = cran_repo)
}
library(curl)

if (!requireNamespace("irace", quietly = TRUE)) {
  remotes::install_github("mork-optimization/irace", upgrade=FALSE)
}
library(irace)

if(!requireNamespace("iraceplot", quietly = TRUE)){
    remotes::install_github("mork-optimization/iraceplot", upgrade=FALSE)
}
library(iraceplot)

if (!requireNamespace("httr", quietly = TRUE)) {
  install.packages("httr", type = "source", repos = cran_repo)
}
library(httr)

if (!requireNamespace("jsonlite", quietly = TRUE)) {
  install.packages("jsonlite", repos = cran_repo)
}

integration_key <- "__INTEGRATION_KEY__"
run_id <- "__RUN_ID__"

configuration_value <- function(value) {
  if (is.na(value)) {
    return("NA")
  }
  value <- as.character(value)
  if (nchar(value) >= 2 && startsWith(value, "'") && endsWith(value, "'")) {
    return(substr(value, 2, nchar(value) - 1))
  }
  value
}

configuration_list <- function(configurations) {
  parameter_names <- names(configurations)[!startsWith(names(configurations), ".")]

  lapply(seq_len(nrow(configurations)), function(row) {
    list(
      configurationId = as.character(configurations[[".ID."]][[row]]),
      parameters = lapply(
        configurations[row, parameter_names, drop = FALSE],
        configuration_value
      )
    )
  })
}

report_progress <- function(iteration, elites, ...) {
  tryCatch({
    response <- POST(
      "http://127.0.0.1:__PORT__/internal/autoconfig/irace/progress",
      timeout(30),
      body = list(
        key = integration_key,
        runId = run_id,
        iteration = iteration,
        elites = configuration_list(elites)
      ),
      encode = "json"
    )
    stop_for_status(response)
  }, error = function(error) {
    warning("Could not publish IRACE progress: ", conditionMessage(error))
  })
}

write_final_elites <- function(configurations) {
  jsonlite::write_json(
    list(elites = configuration_list(configurations)),
    "autoconfig-final-elites.json",
    auto_unbox = TRUE
  )
}

scenario <- readScenario(filename = "scenario.txt", scenario = defaultScenario())
if (!"iterationCallback" %in% names(scenario)) {
  stop("Installed IRACE does not support iterationCallback; please upgrade IRACE")
}
scenario$iterationCallback <- report_progress
options(mork.irace.preflight = TRUE)
checkIraceScenario(scenario = scenario)
options(mork.irace.preflight = FALSE)
elite_configurations <- irace_main(scenario = scenario)
write_final_elites(elite_configurations)
options(mork.irace.preflight = TRUE)
ablation_log <- ablation_cmdline(c("-l", "irace.Rdata", "-s", "scenario.txt"))
plotAblation(ablation_log, pdf_file = "plots.pdf", height = 20, type = c("rank", "boxplot"))
report("irace.Rdata")
