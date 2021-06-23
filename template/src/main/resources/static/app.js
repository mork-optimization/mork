let convergence_chart_series;
let convergence_chart;
let minimum_algorithm;

let current_chart_series;
let current_chart;

function onMessage(message){
    // Replace with custom implementation
    console.log(message);
    const logArea = $("#log");
    //logArea.val(logArea.val() + JSON.stringify(message));
    switch (message.type){
        // Execute actions on different event types
        case "InstanceProcessingStartedEvent":
            const instanceName = message.instanceName.replaceAll("\..+", "");
            // Convergence chart configuration
            $(".convergence-charts").prepend("<div id='chart-convergence-" + instanceName + "'></div>");
            convergence_chart_series = {};
            minimum_algorithm = {};
            convergence_chart = new Highcharts.Chart('chart-convergence-'+instanceName, {
                // chart: {
                //     zoomType: 'x'
                // },
                title: {
                    text: 'Convergence chart'
                },
                subtitle: {
                    text: 'Instance: ' + instanceName
                },
                tooltip: {
                    valueDecimals: 2
                },
                xAxis: {
                    type: 'linear',
                    title: 'Iteration'
                },
                yAxis: {
                    type: 'linear',
                    title: 'Score'
                }
            });

            // Current solution chart
            $(".current-charts").prepend("<div id='chart-current-" + instanceName + "'></div>");
            current_chart_series = {};
            current_chart = new Highcharts.Chart('chart-current-'+instanceName, {
                // chart: {
                //     zoomType: 'x'
                // },
                title: {
                    text: 'Actual value chart'
                },
                subtitle: {
                    text: 'Instance: ' + instanceName
                },
                tooltip: {
                    valueDecimals: 2
                },
                xAxis: {
                    type: 'linear',
                    title: 'Iteration'
                },
                yAxis: {
                    type: 'linear',
                    title: 'Score'
                }
            });

            break;

        case "SolutionGeneratedEvent":
            // Update convergence graph
            if(!convergence_chart) {
                //console.log("Skipping due to missing char");
                return;
            }
            if(!convergence_chart_series.hasOwnProperty(message.algorithmName)){
                convergence_chart_series[message.algorithmName] = convergence_chart.addSeries({
                    name: message.algorithmName,
                    data: [],
                    lineWidth: 0.5
                });
                minimum_algorithm[message.algorithmName] = message.score;
            }
            minimum_algorithm[message.algorithmName] = Math.min(minimum_algorithm[message.algorithmName], message.score);
            convergence_chart_series[message.algorithmName].addPoint([message.iteration, minimum_algorithm[message.algorithmName]])


            // Update current value graph
            if(!current_chart) {
                //console.log("Skipping due to missing char");
                return;
            }
            if(!current_chart_series.hasOwnProperty(message.algorithmName)){
                current_chart_series[message.algorithmName] = current_chart.addSeries({
                    name: message.algorithmName,
                    data: [],
                    lineWidth: 0.5
                });
            }
            current_chart_series[message.algorithmName].addPoint([message.iteration, message.score])
            break;
    }
}


// do not change anything under this line unless you know what you are doing
var stompClient = null;
function setConnected(connected) {
    // $("#connect").prop("disabled", connected);
    // $("#disconnect").prop("disabled", !connected);
    // if (connected) {
    //     $("#conversation").show();
    // }
    // else {
    //     $("#conversation").hide();
    // }
    // $("#greetings").html("");
}

function connectAndSubscribe(callback) {
    const brokerURL = "ws://" + window.location.host + "/websocket";
    stompClient = new StompJs.Client({brokerURL: brokerURL});
    stompClient.onConnect = function() {
        // Subscribe to live event feed from Mork
        const subscription = stompClient.subscribe('/topic/events', function (event) {
            const payload = JSON.parse(event.body);
            callback(payload);
        });
        console.log("STOMP connected");
        setConnected(true);
    }

    stompClient.onStompError = function (frame) {
        // Will be invoked in case of error encountered at Broker
        // Bad login/passcode typically will cause an error
        // Complaint brokers will set `message` header with a brief message. Body may contain details.
        // Compliant brokers will terminate the connection after any error
        console.log('Broker reported error: ' + frame.headers['message']);
        console.log('Additional details: ' + frame.body);
    };

    stompClient.activate();
}


$(() => {
    console.log("Connecting...");
    connectAndSubscribe(onMessage);
})