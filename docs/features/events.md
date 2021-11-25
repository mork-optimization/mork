# Event system

## What
Events are actions or occurrences in any part of the application, that may or may not be handled by listeners. Events are propagated to event listeners asynchronously when certain actions happen (an instance is loaded, a solution is generated, an experiment ends, etc.)

## Why
Events allow users to easily extend the framework functionality without directly modifying it.


## Event lifecycle
```
 ExecutionStartedEvent --> ExperimentStartedEvent --> InstanceProcessingStarted --> SolutionGeneratedEvent(1 to N) --> InstanceProcessingEnded --> ExperimentEndedEvent --> ExecutionEndedEvent
                                      A                            A                                                                 |                       |
                                      |                             \________________________________________________________________/                       |
                                      |                                                                                                                      |
                                      \______________________________________________________________________________________________________________________/

```


# Event types list
Most event names are self-explanatory, in case not:

| Event name | Explanation |
| --- | --- |
| `ExecutionStartedEvent` | Fired once when solver is ready to start generating solutions |
| `ExperimentStartedEvent` | Fired when starting each experiment |
| `InstanceProcessingStartedEvent` | An instance has been loaded and is going to be solved by different algorithms |
| `SolutionGeneratedEvent` | A solution has been generated for the tuple (Instance, AlgorithmConfig, Iteration) |
| `InstanceProcessingEndedEvent` | An instance has been solved with all algorithm configurations and is no longer needed |
| `ExperimentEndedEvent` | Experiment finalized, if there are no more experiments queued end |
| `ExecutionEndedEvent` | All experiments done, fired before solver shutdowns |


# Implementing an event listener

## Backend
Extend `AbstractEventListener` and create methods annotated by `@MorkEventListener.` For example, the telegram bot is implemented using a listener as follows:

```Java

public class TelegramEventListener extends AbstractEventListener {

    private final TelegramService telegramService;
    private volatile boolean errorNotified = false;

    public TelegramEventListener(TelegramService telegramService) {
        this.telegramService = telegramService;
    }

    // Notify user when experiment ends
    @MorkEventListener
    public void onExperimentEnd(ExperimentEndedEvent event) {
        if (!telegramService.ready()) return;
        telegramService.sendMessage(String.format("Experiment %s ended. Execution time: %s seconds", event.getExperimentName(), event.getExecutionTime() / 1_000_000_000));
    }

    // Notify user of the first error
    @MorkEventListener
    public void onError(ErrorEvent event) {
        if (!telegramService.ready()) return;
        // Only notify first error to prevent spamming
        if (!errorNotified) {
            errorNotified = true;
            var t = event.getThrowable();
            telegramService.sendMessage(String.format("Execution Error: %s. Further errors will NOT be notified.", t));
        }
    }

    // Stop Telegram bot when execution ends
    @MorkEventListener
    public void onExecutionEnd(ExecutionEndedEvent event) {
        telegramService.stop();
    }
}
```

If you want to listen to all framework events, use the superclass `MorkEvent`.

## Frontend
Implement the methods `onEventName()`. See `app.js` file in template project for example implementations.

If you want to listen to all framework events use the method `onAnyEvent()`.


# Using custom events
Triggering custom events is extremely easy, for example in custom algorithms such as genetic algorithms.

Just extend `MorkEvent` class, fill with data from any part of your code, and propagate to the framework using `EventPublisher.publishEvent(event)`.
Remember that all events are processed asynchronously, and they MUST be immutable (i.e do not implement any setter).
