//// Configuration

// Max number of charts rendered. Oldest charts get removed when limit is reached.
const max_charts = 10;

// Redraw cooldown in milliseconds.
// Example: If 100 changes are done to a chart in a second,
// and cooldown is 500, charts are only redrawn twice instead of 100 times.
const redraw_cooldown = 2000;

// Event batch download size
const event_batch_size = 1000;

//// End configuration

//// Start Chart data
let convergence_chart_series;
let convergence_chart;
let minimum_algorithm;

let current_chart_series;
let current_chart;

let bestValue = NaN;

let last_redraw = new Date() - 1000;

let progress_chart;
let nInstances;
let currentInstances;
let nAlgorithms;
let currentAlgorithms;
let nRepetitions;
let currentRepetitions;
//// End chart data

// Event handlers, see Github wiki page for more information about the event system.
// Each handler is called when an event from the corresponding type is received. Usual event order is:

// ExecutionStartedEvent --> ExperimentStartedEvent --> InstanceProcessingStarted --> SolutionGeneratedEvent(1 to N) --> InstanceProcessingEnded --> ExperimentEndedEvent --> ExecutionEndedEvent
//                                      A                            A                                                                 |                       |
//                                      |                             \________________________________________________________________/                       |
//                                      |                                                                                                                      |
//                                      \______________________________________________________________________________________________________________________/

//// EVENT HANDLERS
function onExecutionStart(event) {
    createProgressChart();
}

function onExecutionEnd(event) {
    $('#running-status').text('FINISHED');
}

function onExperimentStart(event) {
    nInstances = event.instanceNames.length;
    $('#experiment-name').text(event.experimentName);
    $('#experiment-start').text(new Date(event.timestamp).toLocaleString());
    currentInstances = 0;
}

function onExperimentEnd(event) {
    $('#experiment-name').text('[Waiting]');
}

function onInstanceProcessingStart(event) {
    currentAlgorithms = 0;
    nAlgorithms = event.algorithms.length;
    currentRepetitions = 0;
    nRepetitions = event.repetitions;
    updateStatusChart();

    const instanceName = event.instanceName.replace(/\..+/g, "");
    // Convergence chart configuration
    $(".convergence-charts").prepend("<div id='chart-convergence-" + instanceName + "'></div>");
    convergence_chart_series = {};
    minimum_algorithm = {};
    convergence_chart = new Highcharts.Chart('chart-convergence-' + instanceName, {
        chart: {
            zoomType: 'x'
        },
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
        },
        legend: {
            maxHeight: 60
        },
        credits: {
            enabled: false
        }
    });

    // Current solution chart
    $('.current-charts').prepend("<div id='chart-current-" + instanceName + "'></div>");
    current_chart_series = {};
    current_chart = new Highcharts.Chart('chart-current-' + instanceName, {
        chart: {
            zoomType: 'x'
        },
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
        },
        legend: {
            maxHeight: 60
        },
        credits: {
            enabled: false
        }
    });

    // Draw best solution found
    $('.best-solutions').prepend("<div id='best-solution-" + instanceName + "' class='text-center box-rendered-solution'></div>");
    bestValue = NaN;
    current_best_sol = $('#best-solution-' + instanceName);

    // Add reference value plot line
    // Style of the plot line. Default to solid. See https://jsfiddle.net/gh/get/library/pure/highcharts/highcharts/tree/master/samples/highcharts/plotoptions/series-dashstyle-all/
    convergence_chart.yAxis[0].addPlotLine({
        color: 'red',
        dashStyle: 'longdash',
        value: event.referenceValue,
        width: 2,
        label: {
            align: 'left',
            y: 16
        }
    });
    current_chart.yAxis[0].addPlotLine({
        color: 'red',
        dashStyle: 'longdash',
        value: event.referenceValue,
        width: 2,
        label: {
            align: 'left',
            y: 16
        }
    });
}

function onInstanceProcessingEnd(event) {
    // Each time an instance finishes executing redraw its charts one last time and delete oldest ones
    convergence_chart.redraw();
    removeExtraCharts('.convergence-charts');

    current_chart.redraw();
    removeExtraCharts('.current-charts');

    // current_solution_chart.redraw();
    // removeExtraCharts('.best-solutions');

    currentInstances++;
    currentAlgorithms = nAlgorithms;
    currentRepetitions = nRepetitions;
    updateStatusChart(true);

    convergence_chart_series = []
    minimum_algorithm = []
}

function onSolutionGenerated(event) {
    if (currentRepetitions >= nRepetitions) {
        currentRepetitions = 0;
        currentAlgorithms++;
    }
    currentRepetitions++;
    updateStatusChart();

    if (!convergence_chart || !current_chart) {
        console.log("Skipping onSolutionGenerated due to missing charts, probably a bug!!");
        return;
    }

    const currentTime = new Date();
    const ellapsedTime = currentTime - last_redraw;
    let redraw = false;
    if (ellapsedTime > redraw_cooldown) {
        last_redraw = currentTime;
        redraw = true;
        console.log("Redrawing: " + ellapsedTime + ", eventId: " + event.eventId);
    } else {
        console.log("Skipping redraw: " + ellapsedTime + ", eventId: " + event.eventId);
    }

    if (!convergence_chart_series.hasOwnProperty(event.algorithmName)) {
        convergence_chart_series[event.algorithmName] = convergence_chart.addSeries({
            name: event.algorithmName,
            data: [],
            lineWidth: 1,
            animation: false
        });
        minimum_algorithm[event.algorithmName] = event.score;
    }
    minimum_algorithm[event.algorithmName] = Math.min(minimum_algorithm[event.algorithmName], event.score);
    // BUGFIX: Reorder iterations for convergence chart as they arrive
    convergence_chart_series[event.algorithmName].addPoint([currentRepetitions, minimum_algorithm[event.algorithmName]], redraw)


    // Update current value graph
    if (!current_chart) {
        //console.log("Skipping due to missing char");
        return;
    }
    if (!current_chart_series.hasOwnProperty(event.algorithmName)) {
        current_chart_series[event.algorithmName] = current_chart.addSeries({
            name: event.algorithmName,
            data: [],
            lineWidth: 1,
            animation: false
        });
    }
    current_chart_series[event.algorithmName].addPoint([event.iteration, event.score], redraw);

    // Change to > if maximizing
    if (isNaN(bestValue) || event.score < bestValue) {
        const chart_to_update = current_best_sol;
        bestValue = event.score;
        $.get("/api/generategraph/" + event.eventId, (response) => {
            chart_to_update.html(` <p class="text-center"> best solution is ${event.score}</p>` +
                `<img class="rendered-solution" src="data:image/png;base64,${response}" />`
            );
        });


    }


}

function onAnyEvent(event) {
    // Update last event id
    $("#event-count").text(event.eventId);
}

//// END EVENT HANDLERS
// do not change anything under this line unless you know what you are doing


function onMessage(event) {
    // Dispatch to event handlers
    switch (event.type) {
        case "ExecutionStartedEvent":
            onExecutionStart(event);
            break;

        case "ExecutionEndedEvent":
            onExecutionEnd(event);
            break;

        case "ExperimentStartedEvent":
            onExperimentStart(event);
            break;

        case "ExperimentEndedEvent":
            onExperimentEnd(event);
            break;

        case "InstanceProcessingStartedEvent":
            onInstanceProcessingStart(event);
            break;

        case "InstanceProcessingEndedEvent":
            onInstanceProcessingEnd(event);
            break;

        case "SolutionGeneratedEvent":
            onSolutionGenerated(event);
            break;
    }
    onAnyEvent(event);
}

var stompClient = null;

function connectAndSubscribe(callback) {
    const brokerURL = "ws://" + window.location.host + "/websocket";
    stompClient = new StompJs.Client({brokerURL: brokerURL});
    stompClient.reconnectDelay = 1000;
    stompClient.onConnect = function () {
        // Subscribe to live event feed from Mork
        const subscription = stompClient.subscribe('/topic/events', function (event) {
            const payload = JSON.parse(event.body);
            callback(payload);
        });
        $('#running-status').text('WAITING');
        console.log("STOMP connected. Waiting for the latest event to synchronize state...");
        $.getJSON( "/lastevent", function(event) {
            if (!event_queue) {
                event_queue = [];
                console.log("Recieved first event with id: " + event.eventId);
                downloadOldEventData(0, event.eventId + 1); // [0, eventId]
            } else {
                console.log("ERROR: Event queue already created, impossible?")
            }
        });
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

let event_queue;
let downloaded_events = [];
let isUpToDate = false;

function downloadOldEventData(from, to) {
    $('#running-status').text('RUNNING, SYNCING');
    const limit = Math.min(to, from + event_batch_size);
    $.get(`/events?from=${from}&to=${limit}`, (old_events) => {
        console.log(`API /events returned  [${from}, ${limit}): ` + old_events.length);
        downloaded_events = downloaded_events.concat(old_events);
        if (old_events.length < event_batch_size) {
            event_queue = downloaded_events.concat(event_queue);
            downloaded_events = [];
            const catchUp = (i) => {
                if (i === event_queue.length) {
                    // force redraw
                    // Each time an instance finishes executing redraw its charts one last time and delete oldest ones
                    convergence_chart.redraw();
                    current_chart.redraw();

                    event_queue = [];
                    isUpToDate = true;
                    $('#running-status').text('RUNNING, REAL TIME');
                    console.log("Up to date!");
                } else {
                    $('#running-status').text('RUNNING, REPLAYING');
                    onMessage(event_queue[i]);
                    setTimeout(() => catchUp(i + 1), 0);
                }
            };
            setTimeout(() => catchUp(0), 0);
        } else {
            setTimeout(() => downloadOldEventData(from + event_batch_size, to), 0);
        }
    });
}

// Store events in queue until previous events have been processed
function interceptor(event) {
    if (isUpToDate) {
        onMessage(event);
    } else {
        event_queue.push(event);
    }
}

function createProgressChart() {
    if (progress_chart) {
        progress_chart.destroy();
    }
    progress_chart = Highcharts.chart('progress-chart', {
        chart: {
            type: 'solidgauge',
            height: '110%'
        },
        title: {
            text: '',
            style: {
                fontSize: '12px'
            }
        },

        exporting: {
            enabled: false
        },

        credits: {
            enabled: false
        },
        tooltip: {
            borderWidth: 0,
            backgroundColor: 'none',
            shadow: false,
            style: {
                fontSize: '10px'
            },
            valueSuffix: '%',
            pointFormat: '{series.name}<br><span style="font-size:2em; color: {point.color}; font-weight: bold">{point.y}</span>',
            positioner: function (labelWidth) {
                return {
                    x: (this.chart.chartWidth - labelWidth) / 2,
                    y: (this.chart.plotHeight / 2) - 15
                };
            }
        },

        pane: {
            startAngle: 0,
            endAngle: 360,
            background: [{ // Track for Iterations
                outerRadius: '112%',
                innerRadius: '88%',
                backgroundColor: Highcharts.color(Highcharts.getOptions().colors[0])
                    .setOpacity(0.3)
                    .get(),
                borderWidth: 0
            }, { // Track for Algorithms
                outerRadius: '87%',
                innerRadius: '63%',
                backgroundColor: Highcharts.color(Highcharts.getOptions().colors[1])
                    .setOpacity(0.3)
                    .get(),
                borderWidth: 0
            }, { // Track for Instances
                outerRadius: '62%',
                innerRadius: '38%',
                backgroundColor: Highcharts.color(Highcharts.getOptions().colors[2])
                    .setOpacity(0.3)
                    .get(),
                borderWidth: 0
            }]
        },

        yAxis: {
            min: 0,
            max: 100,
            lineWidth: 0,
            tickPositions: []
        },

        plotOptions: {
            solidgauge: {
                dataLabels: {
                    enabled: false
                },
                linecap: 'round',
                stickyTracking: false,
                rounded: true
            }
        },

        series: [{
            name: 'Iteration',
            data: [{
                id: 'Iteration',
                color: Highcharts.getOptions().colors[0],
                radius: '112%',
                innerRadius: '88%',
                y: 80
            }]
        }, {
            name: 'Algorithm',
            data: [{
                id: 'Algorithm',
                color: Highcharts.getOptions().colors[1],
                radius: '87%',
                innerRadius: '63%',
                y: 65
            }]
        }, {
            name: 'Instance',
            data: [{
                id: 'Instance',
                color: Highcharts.getOptions().colors[2],
                radius: '62%',
                innerRadius: '38%',
                y: 50
            }]
        }]
    });
}

function updateStatusChart(force = false) {
    const ellapsedTime = new Date() - last_redraw;
    let redraw = force || ellapsedTime > redraw_cooldown;
    const iteration = progress_chart.get('Iteration'),
        algorithm = progress_chart.get('Algorithm'),
        instance = progress_chart.get('Instance');

    iteration.update(Math.round(currentRepetitions / nRepetitions * 100), redraw);
    algorithm.update(Math.round(currentAlgorithms / nAlgorithms * 100), redraw);
    instance.update(Math.round(currentInstances / nInstances * 100), redraw);
}


function getRandomColor(id) {
    id *= 3;
    return `rgb(${random(id)}, ${random(id + 1)}, ${random(id + 2)})`;
}

// Random from 0 to 255
function random(seed) {
    var x = Math.sin(seed) * 10000;
    return Math.floor((x - Math.floor(x)) * 256);
}

function removeExtraCharts(selector) {
    $(selector).children().slice(max_charts - 1).each(function () {
        const c = $(this);
        try {
            console.log("Deleting chart:");
            console.log(c);
            c.highcharts().destroy();
            c.remove();
        } catch (e) {
            console.log(e);
        }
    });
}

$(() => {
    console.log("Connecting...");
    $('#running-status').text('CONNECTING');
    connectAndSubscribe(interceptor);
})