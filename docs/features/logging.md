# Logging system

## Introduction

Commonly you may need to print any information to see your algorithm progress or to debug it.
Although you may be tempted to use `System.out`, or `System.err`, it should be avoided, using a `Logger` object instead. 
Loggers have multiple advantages, among them:

- You can configure the level of information to print, so you can print more information when debugging and less when running the algorithm. Forget about commenting and uncommenting `System.out.println` lines!
- Performance: `Logger` objects are optimized to be used in production environments, so they are faster than `println` statements.
- You can configure the output format, so you can print the information in a more readable way. For example, by default console output is colored!
- You can configure the output destination, both by log origin, and by level. For example, you can redirect an experiment log to a file, while keeping general information in the console.

For this purpose, Mork provides a logging system based on [SLF4J](https://www.slf4j.org/). Stop using `System.out` today!

## How to use it

### Declaring a logger
In any file, you can declare a logger as follows:

```java
private static final Logger logger = LoggerFactory.getLogger(MyClass.class);
```

Where `MyClass` is the name of the class where you are declaring the logger. This is important, as it will be used to identify the origin of the log message.

!!! warning

    When importing the Logger and LoggerFactory classes, make sure to use the ones from the `org.slf4j` package. Specifically, avoid using `java.util.logging` classes, as their usage convention is completely different.


### Logging messages

In any method, you can log messages using the logger object as follows:

```java
public int improve(Solution s, List<Improver<Solution, Instance>> improvers) {
    double scoreBeforeAll = s.getScore();
    double scoreBefore = scoreBeforeAll;
    for (var : improvers) {
        s = improver.improve(s);
        logger.trace("Improver {}: {} --> {}", improver.getClass().getSimpleName(), scoreBefore, s.getScore());
        scoreBefore = s.getScore();
    }
    logger.debug("Total improvement: {}", scoreBeforeAll - s.getScore());
}
```

Formatting strings is an expensive operation, that should not be done if the given log level is not enabled. 
Avoid concatenating concatenating strings, or using `String::format`. As seen in the previous example, 
use a fixed string, with `{}` as placeholders, and then pass the objects to be formatted as arguments to the logger call.

In more complex cases, you may need to invoke some methods to generate some debugging information, and doing everything in the logger call may be unreadable.
In this cases, you can guard the expression using `Logger::isLevelEnabled`, and then calling the expensive methods and the log call only if the level is enabled. Example:

```java
if (logger.isDebugEnabled()) {
    var expensiveResult = someExpensiveMethod();
    logger.debug("The current solution is: {} -> {}", s, expensiveResult);
}
```

### Log levels
In Mork, the log levels are used as follows:

- **ERROR**: Used for anything that keeps us from continuing. For example, missing mandatory components, or invalid configurations.
- **WARN**: Used for anything that may cause problems if not managed, or unexpected things that although not strictly invalid, it may not be what the user wants, or missing best practices. For example, using a deprecated method, or a configuration that is known to cause performance issues.
- **INFO**: Used for anything that may be interesting for the user to know, but can be safely ignored. For example, the current experiment status, number of iterations remaining, etc.
- **DEBUG**: Using to print internal information about each component. For example, algorithm components will track their progress and log it using a DEBUG level. Very useful for debugging purposes.
- **TRACE**: Used to print very detailed information about a component. Each operations is logged. For example, each movement applied to the solution in an improvement method. Note that enabling TRACE level may produce a huge amount of information, and may have a big performance impact, so it should be used carefully.

## Logging configuration

Configuration is managed by the logging framework implementation, in this case [Logback](https://logback.qos.ch/).
Inside the `resources` folder, there is a `logback.xml` file, which contains the default configuration. You can modify it to suit your needs.
The most relevant sections of the file are detailed next.

### Log levels
Each package and class can be configured to use a different log level.
For example, you may want to print all the information about the experiment progress,
but only print warnings or errors in your own classes. 
To configure the log level of a package, use the `logger` tag, specifying a log level for the given package. For example:

```xml
<logger name="es.urjc.etsii.grafo.myProblem" level="DEBUG"/>
```

Would configure the log level of all loggers in the `es.urjc.etsii.grafo.myProblem` package to `DEBUG`. 
Log levels are inherited by default, but can be overriden. For example, the following configuration:

```xml
<logger name="es.urjc.etsii.grafo.myProblem" level="INFO"/>
<logger name="es.urjc.etsii.grafo.myProblem.localsearch" level="TRACE"/>
```

Would configure the log level of all classes inside the `es.urjc.etsii.grafo.myProblem` package to `INFO`,
and all classes inside the `es.urjc.etsii.grafo.myProblem.localsearch` package to `TRACE`.

### Appenders
Appenders are the log destinations, where the messages will be sent. 
By default, two appenders are configured, a console appender, which prints log messages to the console, and a file appender, which prints them to the configured file.
For the full list of available appenders and their configurable parameters, see the reference [Logback documentation](https://logback.qos.ch/manual/appenders.html).

Appenders can declare their own filters, to filter the messages that are sent to them. For example, the Console appender is configured to ignore all messages below the `DEBUG` level, so `TRACE` messages are not printed to the console.
[Reference filters documentation](https://logback.qos.ch/manual/filters.html).

If adding a new appender, remember to add it to the root logger, so it is used. For example, a want to create a custom appender that filters ERROR messages and sends them to a file:

```xml
<appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>errors.txt</file>
    <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
        <fileNamePattern>errors-%d{yyyy-MM-dd}.%i}.gz</fileNamePattern>
        <maxFileSize>${LOGBACK_ROLLINGPOLICY_MAX_FILE_SIZE:-10MB}</maxFileSize>
    </rollingPolicy>
    <encoder>
        <pattern>
            %clr([%d{HH:mm:ss}]){faint} %clr(%-5level) %clr(%-26.26logger{25}){cyan} %clr(:){faint} %m%n
        </pattern>
    </encoder>
    <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
        <level>ERROR</level>
    </filter>
</appender>
```

and add it to the root logger

```xml

<root level="info">
    <appender-ref ref="STDOUT"/>
    <appender-ref ref="FILE"/>
    <appender-ref ref="ERROR2FILE"/>
</root>
```

### Example configuration
!!! tip

    This configuration is provided as an example, and may diverge from the current recommended configuration. 
    The latest recommended logging configuration can always be found in the [Mork repository](https://github.com/mork-optimization/mork/blob/master/template/src/main/resources/logback.xml)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <!-- Each logger level can be independently configured here -->
    <!-- Set our own log level to INFO by default -->
    <logger name="es.urjc.etsii.grafo" level="INFO"/>

    <!-- Override info level for the executor package, to print in real time information about the experiment progress -->
    <logger name="es.urjc.etsii.grafo.executors" level="DEBUG"/>

    <!-- If any algorithm component does not behave as expected, you can set the log level to DEBUG to see what is going on -->
    <!-- TRACE log level is reserved for generating very detailed information, for example in an improver method it will
     dump each operation performed for all solutions. -->

    <!--    <logger name="es.urjc.etsii.grafo.improve" level="DEBUG"/>-->
    <!--    <logger name="es.urjc.etsii.grafo.algorithms" level="DEBUG"/>-->


    <!-- Reduce the noise generated by the following packages -->
    <logger name="org.apache.poi.util.XMLHelper" level="ERROR"/>
    <logger name="org.apache.catalina" level="WARN"/>
    <logger name="org.springframework" level="WARN"/>

    <!-- Console appender, to write logs to the console -->
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <!-- The pattern deletes the current line using ANSI scape sequences, so the progress bar is not printed multiple times -->
            <pattern>\\u001b[2K\r%clr([%d{HH:mm:ss}]){faint} %clr(%-5level) %clr(%-26.26logger{25}){cyan} %clr(:){faint} %m%n</pattern>
        </encoder>
        <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
            <!-- We can filter the logs printed to the console, for example to print only INFO
            and above to the console but keep DEBUG or TRACE to the file. By default filters TRACE logs -->
            <level>DEBUG</level>
        </filter>
    </appender>

    <!-- Write all logs to a file, with a rolling policy to avoid filling the disk.
    Each time the file size reaches the limit, or the current day changes, it is compressed and a new file is created. -->
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>log.txt</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>log-%d{yyyy-MM-dd}.%i}.gz</fileNamePattern>
            <maxFileSize>${LOGBACK_ROLLINGPOLICY_MAX_FILE_SIZE:-10MB}</maxFileSize>
        </rollingPolicy>
        <encoder>
            <pattern>
                %clr([%d{HH:mm:ss}]){faint} %clr(%-5level) %clr(%-26.26logger{25}){cyan} %clr(:){faint} %m%n
            </pattern>
        </encoder>
    </appender>

    <root level="info">
        <appender-ref ref="STDOUT" />
        <appender-ref ref="FILE"/>
    </root>
    <include resource="org/springframework/boot/logging/logback/defaults.xml" />
</configuration>
```