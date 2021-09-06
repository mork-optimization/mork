# v0.6
- (BREAKING) Two types of neighborhoods, normal and lazy. Lazy generated movements under demand, while eager returns a collection of movements.
- New event type: ErrorEvent, when an unhandled exception is propagated inside an Executor.
- Telegram bot, if enabled, sends message onErrorEvent.

# v0.5
- Added Telegram integration, see Wiki for more information and how to use.
- Fixed bug in IteratedGreedy
- Added option to allow users to decide if the backend should stop and the application be killed after all experiments are finished.

