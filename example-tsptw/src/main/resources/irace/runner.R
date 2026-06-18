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


scenario <- readScenario(filename = "scenario.txt", scenario = defaultScenario())
checkIraceScenario(scenario = scenario)
irace_main(scenario = scenario)
ablation_cmdline(c("-l", "irace.Rdata", "-s", "scenario.txt", "-p", "plots.pdf", "-O", "rank,boxplot"))
report("irace.Rdata")
