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
  if (is.null(configurations) || nrow(configurations) == 0) {
    return(list())
  }

  configuration_ids <- if (".ID." %in% names(configurations)) {
    configurations[[".ID."]]
  } else {
    rownames(configurations)
  }
  parameter_names <- names(configurations)[!startsWith(names(configurations), ".")]

  lapply(seq_len(nrow(configurations)), function(row) {
    parameters <- list()
    for (parameter_name in parameter_names) {
      value <- configurations[[parameter_name]][[row]]
      parameters[[parameter_name]] <- configuration_value(value)
    }
    list(
      configurationId = as.character(configuration_ids[[row]]),
      parameters = parameters
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
    if (http_error(response)) {
      warning(paste("Could not publish IRACE progress:", http_status(response)$message))
    }
  }, error = function(error) {
    warning(paste("Could not publish IRACE progress:", conditionMessage(error)))
  })
}

write_final_elites <- function(configurations) {
  target <- "autoconfig-final-elites.json"
  temporary <- paste0(target, ".tmp")
  jsonlite::write_json(
    list(elites = configuration_list(configurations)),
    temporary,
    auto_unbox = TRUE,
    pretty = TRUE
  )
  if (file.exists(target)) {
    unlink(target)
  }
  if (!file.rename(temporary, target)) {
    stop(paste("Could not publish final IRACE elites to", target))
  }
}

scenario <- readScenario(filename = "scenario.txt", scenario = defaultScenario())
if ("iterationCallback" %in% names(scenario)) {
  scenario$iterationCallback <- report_progress
} else {
  warning("Installed IRACE does not support iterationCallback; live elite updates are disabled")
}
options(mork.irace.preflight = TRUE)
checkIraceScenario(scenario = scenario)
options(mork.irace.preflight = FALSE)
elite_configurations <- irace_main(scenario = scenario)
write_final_elites(elite_configurations)
options(mork.irace.preflight = TRUE)
ablation_log <- ablation_cmdline(c("-l", "irace.Rdata", "-s", "scenario.txt"))
plotAblation(ablation_log, pdf_file = "plots.pdf", height = 20, type = c("rank", "boxplot"))
report("irace.Rdata")
