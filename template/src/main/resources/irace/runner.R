if (!require("irace")) {
  install.packages("irace", type = "source", repos = "http://cran.us.r-project.org")
  library("irace")
}

if (!require("httr")) {
  install.packages("httr", type = "source", repos = "http://cran.us.r-project.org")
  library("httr")
}

if (!require("rjson")) {
  install.packages("rjson", type = "source", repos = "http://cran.us.r-project.org")
  library("rjson")
}

if (!require("base64enc")) {
  install.packages("base64enc", type = "source", repos = "http://cran.us.r-project.org")
  library("base64enc")
}

scenario <- readScenario(filename = "scenario.txt", scenario = defaultScenario())
checkIraceScenario(scenario = scenario)
irace.main(scenario = scenario)
