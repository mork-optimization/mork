package es.urjc.etsii.grafo.terminal;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Coordinates progress bar redraws and console logs through a single terminal writer.
 */
final class ProgressAwareTerminal {

    private static final String CARRIAGE_RETURN = "\r";
    private static final int DEFAULT_TERMINAL_WIDTH = 80;
    private static final int CONSOLE_RIGHT_MARGIN = 1;
    private static final ReentrantLock LOCK = new ReentrantLock();

    private static OutputStream outputStream = System.out;
    private static String currentProgressLine;
    private static int currentProgressDisplayLength;
    private static boolean progressVisible;

    private ProgressAwareTerminal() {
        // Utility class
    }

    static void setOutputStream(OutputStream target) {
        LOCK.lock();
        try {
            outputStream = target;
        } finally {
            LOCK.unlock();
        }
    }

    static int getMaxRenderedLength(int configuredMaxRenderedLength) {
        if (configuredMaxRenderedLength > 0) {
            return configuredMaxRenderedLength;
        }
        return Math.max(1, getTerminalWidth() - CONSOLE_RIGHT_MARGIN);
    }

    static int getTerminalWidth() {
        String columns = System.getenv("COLUMNS");
        if (columns != null && !columns.isBlank()) {
            try {
                int parsed = Integer.parseInt(columns);
                if (parsed >= 10) {
                    return parsed;
                }
            } catch (NumberFormatException ignored) {
                // Use the default width below.
            }
        }
        return DEFAULT_TERMINAL_WIDTH;
    }

    static void writeProgress(String progressLine, int maxRenderedLength) {
        LOCK.lock();
        try {
            RenderedLine renderedLine = trimDisplayLength(progressLine, maxRenderedLength);
            if (progressVisible) {
                overwriteCurrentProgressLine();
            } else {
                writeString(CARRIAGE_RETURN);
            }
            currentProgressLine = renderedLine.value();
            currentProgressDisplayLength = renderedLine.displayLength();
            progressVisible = true;
            writeString(currentProgressLine);
            flush();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot write progress bar to console", e);
        } finally {
            LOCK.unlock();
        }
    }

    static void clearProgress() {
        LOCK.lock();
        try {
            if (progressVisible) {
                overwriteCurrentProgressLine();
                flush();
            }
            progressVisible = false;
            currentProgressLine = null;
            currentProgressDisplayLength = 0;
        } catch (IOException e) {
            throw new IllegalStateException("Cannot clear progress bar from console", e);
        } finally {
            LOCK.unlock();
        }
    }

    static void closeProgress() {
        LOCK.lock();
        try {
            if (progressVisible) {
                writeString(System.lineSeparator());
                flush();
            }
            progressVisible = false;
            currentProgressLine = null;
            currentProgressDisplayLength = 0;
        } catch (IOException e) {
            throw new IllegalStateException("Cannot close progress bar console writer", e);
        } finally {
            LOCK.unlock();
        }
    }

    static void writeLog(byte[] bytes, int offset, int length) throws IOException {
        LOCK.lock();
        try {
            boolean shouldRedrawProgress = progressVisible && currentProgressLine != null;
            if (shouldRedrawProgress) {
                overwriteCurrentProgressLine();
            }

            outputStream.write(bytes, offset, length);

            if (shouldRedrawProgress) {
                if (length > 0 && !endsWithLineBreak(bytes, offset, length)) {
                    writeString(System.lineSeparator());
                }
                writeString(CARRIAGE_RETURN);
                writeString(currentProgressLine);
            }
            flush();
        } finally {
            LOCK.unlock();
        }
    }

    private static boolean endsWithLineBreak(byte[] bytes, int offset, int length) {
        byte lastByte = bytes[offset + length - 1];
        return lastByte == '\n' || lastByte == '\r';
    }

    private static void overwriteCurrentProgressLine() throws IOException {
        writeString(CARRIAGE_RETURN);
        writeString(" ".repeat(currentProgressDisplayLength));
        writeString(CARRIAGE_RETURN);
    }

    private static void writeString(String value) throws IOException {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        outputStream.write(bytes);
    }

    private static void flush() throws IOException {
        outputStream.flush();
    }

    private static RenderedLine trimDisplayLength(String value, int maxDisplayLength) {
        if (maxDisplayLength <= 0 || value == null || value.isEmpty()) {
            return new RenderedLine("", 0);
        }

        int displayLength = 0;
        StringBuilder result = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); ) {
            char c = value.charAt(i);
            if (c == '\u001B') {
                int sequenceEnd = findAnsiSequenceEnd(value, i);
                result.append(value, i, sequenceEnd);
                i = sequenceEnd;
                continue;
            }

            int charCount = Character.charCount(value.codePointAt(i));
            int charDisplayLength = Character.isISOControl(c) ? 0 : 1;
            if (displayLength + charDisplayLength > maxDisplayLength) {
                break;
            }
            result.append(value, i, i + charCount);
            displayLength += charDisplayLength;
            i += charCount;
        }
        return new RenderedLine(result.toString(), displayLength);
    }

    private static int findAnsiSequenceEnd(String value, int start) {
        int i = start + 1;
        if (i >= value.length()) {
            return i;
        }
        if (value.charAt(i) == '[') {
            i++;
        }
        while (i < value.length()) {
            char c = value.charAt(i++);
            if (c >= '@' && c <= '~') {
                return i;
            }
        }
        return i;
    }

    static void resetForTesting(OutputStream target) {
        LOCK.lock();
        try {
            outputStream = target;
            currentProgressLine = null;
            currentProgressDisplayLength = 0;
            progressVisible = false;
        } finally {
            LOCK.unlock();
        }
    }

    private record RenderedLine(String value, int displayLength) {}
}
