personal_lib_path <- Sys.getenv("R_LIBS_USER")
if (!dir.exists(personal_lib_path)) {
  dir.create(personal_lib_path, recursive = TRUE)
}
.libPaths(personal_lib_path, .libPaths())

if (!require(curl)) {
  remotes::install_version("curl", version="5.2.3", lib=personal_lib_path, repos = "http://cran.us.r-project.org")
  library(curl)
}

if (!require(remotes)) {
  install.packages("remotes", type = "source", lib=personal_lib_path, repos = "http://cran.us.r-project.org")
  library(remotes)
}

if (!require(irace)) {
  remotes::install_github("mork-optimization/irace", upgrade=FALSE)
  library(irace)
}

if(!require(iraceplot)){
    remotes::install_github("mork-optimization/iraceplot", upgrade=FALSE)
    library(iraceplot)
}

if (!require(httr)) {
  install.packages("httr", type = "source", lib=personal_lib_path, repos = "http://cran.us.r-project.org")
  library(httr)
}

if (!require(rjson)) {
  install.packages("rjson", type = "source", lib=personal_lib_path, repos = "http://cran.us.r-project.org")
  library(rjson)
}

if (!require(base64enc)) {
  install.packages("base64enc", type = "source", lib=personal_lib_path, repos = "http://cran.us.r-project.org")
  library(base64enc)
}

scenario <- readScenario(filename = "scenario.txt", scenario = defaultScenario())
checkIraceScenario(scenario = scenario)
irace_main(scenario = scenario)
ablation_cmdline(c("-l", "irace.Rdata", "-s", "scenario.txt", "-p", "plots.pdf", "-O", "rank,boxplot"))
report("irace.Rdata")

