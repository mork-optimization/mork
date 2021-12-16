if(!require(irace)){
    install.packages("irace", type="source", repos = "http://cran.us.r-project.org")
    library(irace)
}

scenario <- readScenario(filename = "scenario.txt", scenario = defaultScenario())
checkIraceScenario(scenario = scenario)
irace.main(scenario = scenario)
