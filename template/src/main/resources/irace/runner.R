if(!require("irace")){
    install.packages("irace", type="source", repos = "http://cran.us.r-project.org")
    library("irace")
}

if(!require("curl")){
    install.packages("curl", type="source", repos = "http://cran.us.r-project.org")
    library("curl")
}

if(!require("base64enc")){
    install.packages("base64enc", type="source", repos = "http://cran.us.r-project.org")
    library("base64enc")
}

scenario <- readScenario(filename = "scenario.txt", scenario = defaultScenario())
checkIraceScenario(scenario = scenario)
irace.main(scenario = scenario)
