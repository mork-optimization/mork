package es.urjc.etsii.grafo.terminal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class ProgressAwareTerminalTest {

    private ByteArrayOutputStream output;
    private ProgressAwareProgressBarConsumer consumer;

    @BeforeEach
    void setUp() {
        output = new ByteArrayOutputStream();
        ProgressAwareTerminal.resetForTesting(output);
        consumer = new ProgressAwareProgressBarConsumer(120);
    }

    @AfterEach
    void tearDown() {
        ProgressAwareTerminal.resetForTesting(System.out);
    }

    @Test
    void logMessageIsWrittenAboveActiveProgressLine() throws IOException {
        consumer.accept("Experiment 50%");

        ProgressAwareTerminal.writeLog("hello\n".getBytes(StandardCharsets.UTF_8), 0, "hello\n".length());

        String terminalOutput = terminalOutput();
        assertTrue(terminalOutput.contains("hello\n"));
        assertTrue(terminalOutput.contains("Experiment 50%\r" + " ".repeat("Experiment 50%".length()) + "\rhello\n"));
        assertTrue(terminalOutput.endsWith("\rExperiment 50%"));
        assertEquals(2, countOccurrences(terminalOutput, "Experiment 50%"));
    }

    @Test
    void multilineLogMessageIsPreservedBeforeProgressRedraw() throws IOException {
        consumer.accept("Experiment 75%");

        byte[] logBytes = "line 1\nline 2\n".getBytes(StandardCharsets.UTF_8);
        ProgressAwareTerminal.writeLog(logBytes, 0, logBytes.length);

        String terminalOutput = terminalOutput();
        assertTrue(terminalOutput.contains("line 1\nline 2\n"));
        assertTrue(terminalOutput.endsWith("\rExperiment 75%"));
    }

    @Test
    void logWithoutTrailingLineBreakGetsSeparatedFromProgressRedraw() throws IOException {
        consumer.accept("Experiment 25%");

        byte[] logBytes = "partial log".getBytes(StandardCharsets.UTF_8);
        ProgressAwareTerminal.writeLog(logBytes, 0, logBytes.length);

        assertTrue(terminalOutput().contains("partial log" + System.lineSeparator() + "\rExperiment 25%"));
    }

    @Test
    void progressIsOverwrittenBeforeWritingTabbedLogMessage() throws IOException {
        String progress = "progress text remains here";
        consumer.accept(progress);

        byte[] logBytes = "\t6.\tT(s): 37.919\n".getBytes(StandardCharsets.UTF_8);
        ProgressAwareTerminal.writeLog(logBytes, 0, logBytes.length);

        assertTrue(terminalOutput().contains(progress + "\r" + " ".repeat(progress.length()) + "\r\t6.\tT(s): 37.919\n"));
    }

    @Test
    void shorterProgressOverwritesFullPreviousProgressLine() {
        consumer.accept("Long progress line");

        consumer.accept("Short");

        assertTrue(terminalOutput().contains("Long progress line\r" + " ".repeat("Long progress line".length()) + "\rShort"));
    }

    @Test
    void ansiSequencesDoNotAddToProgressDisplayLength() {
        String coloredProgress = "\u001B[33mProgress\u001B[0m";
        consumer.accept(coloredProgress);

        consumer.clear();

        assertTrue(terminalOutput().endsWith(coloredProgress + "\r" + " ".repeat("Progress".length()) + "\r"));
    }

    @Test
    void progressConsumerDoesNotEmitCursorMovementSequences() throws IOException {
        consumer.accept("Experiment 10%");
        ProgressAwareTerminal.writeLog("log\n".getBytes(StandardCharsets.UTF_8), 0, "log\n".length());

        String terminalOutput = terminalOutput();
        assertFalse(terminalOutput.contains("\u001B[1A"));
        assertFalse(terminalOutput.contains("\u001B[1B"));
    }

    @Test
    void closingProgressStopsFutureLogRedraws() throws IOException {
        consumer.accept("Experiment 100%");
        consumer.close();

        byte[] logBytes = "after\n".getBytes(StandardCharsets.UTF_8);
        ProgressAwareTerminal.writeLog(logBytes, 0, logBytes.length);

        String terminalOutput = terminalOutput();
        assertTrue(terminalOutput.contains("Experiment 100%" + System.lineSeparator() + "after\n"));
        assertEquals(1, countOccurrences(terminalOutput, "Experiment 100%"));
    }

    private String terminalOutput() {
        return output.toString(StandardCharsets.UTF_8);
    }

    private static int countOccurrences(String value, String pattern) {
        int count = 0;
        int index = 0;
        while ((index = value.indexOf(pattern, index)) != -1) {
            count++;
            index += pattern.length();
        }
        return count;
    }
}
