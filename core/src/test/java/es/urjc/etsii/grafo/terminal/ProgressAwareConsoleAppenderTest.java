package es.urjc.etsii.grafo.terminal;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.encoder.EncoderBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ProgressAwareConsoleAppenderTest {

    @AfterEach
    void tearDown() {
        ProgressAwareTerminal.resetForTesting(System.out);
    }

    @Test
    void appenderUsesConfiguredEncoderAndProgressAwareTerminal() {
        var output = new ByteArrayOutputStream();
        var context = new LoggerContext();
        context.start();
        var encoder = new TestEncoder();
        encoder.setContext(context);
        encoder.start();

        var appender = new TestProgressAwareConsoleAppender(output);
        appender.setContext(context);
        appender.setName("TEST");
        appender.setEncoder(encoder);
        appender.start();
        assertTrue(appender.isStarted(), context.getStatusManager().getCopyOfStatusList().toString());

        var consumer = new ProgressAwareProgressBarConsumer(120);
        consumer.accept("Experiment 50%");

        appender.doAppend("hello");
        assertTrue(appender.wasAppendCalled, "Appender append method was not called");
        assertTrue(appender.wasStartedAtAppend, "Appender was not started inside append");
        assertTrue(encoder.wasEncodeCalled, "Encoder encode method was not called");

        String terminalOutput = output.toString(StandardCharsets.UTF_8);
        assertTrue(terminalOutput.contains("INFO hello\n"), sanitized(terminalOutput));
        assertTrue(terminalOutput.endsWith("\rExperiment 50%"), sanitized(terminalOutput));

        appender.stop();
        context.stop();
    }

    private static class TestProgressAwareConsoleAppender extends ProgressAwareConsoleAppender<String> {
        private final OutputStream targetStream;
        private boolean wasAppendCalled;
        private boolean wasStartedAtAppend;

        private TestProgressAwareConsoleAppender(OutputStream targetStream) {
            this.targetStream = targetStream;
        }

        @Override
        protected OutputStream resolveTargetStream() {
            return targetStream;
        }

        @Override
        protected void append(String eventObject) {
            wasAppendCalled = true;
            wasStartedAtAppend = isStarted();
            super.append(eventObject);
        }
    }

    private static class TestEncoder extends EncoderBase<String> {
        private boolean wasEncodeCalled;

        @Override
        public byte[] headerBytes() {
            return new byte[0];
        }

        @Override
        public byte[] encode(String event) {
            wasEncodeCalled = true;
            return ("INFO " + event + "\n").getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public byte[] footerBytes() {
            return new byte[0];
        }
    }

    private static String sanitized(String terminalOutput) {
        return terminalOutput
                .replace("\u001B", "\\u001B")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }
}
