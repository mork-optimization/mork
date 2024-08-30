personal_lib_path <- Sys.getenv("R_LIBS_USER")
if (!dir.exists(personal_lib_path)) {
  dir.create(personal_lib_path, recursive = TRUE)
}
.libPaths(personal_lib_path)

if (!require(remotes)) {
  install.packages("remotes", type = "source", repos = "http://cran.us.r-project.org")
  library(remotes)
}

if (!require(irace)) {
  # install.packages("irace", type = "source", repos = "http://cran.us.r-project.org")
  remotes::install_github("mork-optimization/irace", upgrade=FALSE)
  library(irace)
}

if(!require(iraceplot)){
    remotes::install_github("mork-optimization/iraceplot", upgrade=FALSE)
    library(iraceplot)
}

if (!require(httr)) {
  install.packages("httr", type = "source", repos = "http://cran.us.r-project.org")
  library(httr)
}

if (!require(rjson)) {
  install.packages("rjson", type = "source", repos = "http://cran.us.r-project.org")
  library(rjson)
}

if (!require(base64enc)) {
  install.packages("base64enc", type = "source", repos = "http://cran.us.r-project.org")
  library(base64enc)
}

scenario <- readScenario(filename = "scenario.txt", scenario = defaultScenario())
checkIraceScenario(scenario = scenario)
irace_main(scenario = scenario)
ablation_cmdline(c("-l", "irace.Rdata", "-s", "scenario.txt", "-p", "plots.pdf", "-O", "rank,boxplot"))
report("irace.Rdata")

