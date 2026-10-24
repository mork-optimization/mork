package es.urjc.etsii.grafo.autoconfig.autollm;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;

class AutoLlmLogUtilTest {
    @TempDir Path temp;

    @Test void selectsLastLinesWithOrWithoutFinalNewlineAndHandlesEmptyLines() throws Exception {
        Path file = temp.resolve("stdout.log");
        for (String ending : new String[]{"", "\n", "\r\n"}) {
            Files.writeString(file, "first\nsecond\nthird" + ending);
            var preview = AutoLlmLogUtil.tail(file, 2);
            assertEquals("second\nthird" + ending, preview.text());
            assertEquals(2, preview.lineCount()); assertTrue(preview.truncated()); assertFalse(preview.byteLimited());
        }
        Files.writeString(file, "first\n\n\n");
        assertEquals("\n\n", AutoLlmLogUtil.tail(file, 2).text());
        Files.writeString(file, "");
        var empty = AutoLlmLogUtil.tail(file, 500);
        assertEquals("", empty.text()); assertEquals(0, empty.lineCount()); assertFalse(empty.truncated());
    }

    @Test void showsTheWholeSmallFileWithoutCallingItTruncated() throws Exception {
        Path file = temp.resolve("config.json"); Files.writeString(file, "{\n  \"key\": \"α€\"\n}\n");
        var preview = AutoLlmLogUtil.tail(file, 500);
        assertEquals(Files.readString(file), preview.text()); assertEquals(3, preview.lineCount());
        assertEquals(Files.size(file), preview.sizeBytes()); assertFalse(preview.truncated()); assertFalse(preview.byteLimited());
    }

    @Test void readsASparseEightGigabyteFileFromTheEndAndTracksAppendAndTruncation() throws Exception {
        Path file = temp.resolve("large.log");
        long size = 8L * 1024 * 1024 * 1024;
        byte[] suffix = "\nlast one\nlast two\n".getBytes(StandardCharsets.UTF_8);
        try (var output = new RandomAccessFile(file.toFile(), "rw")) {
            output.setLength(size); output.seek(size - suffix.length); output.write(suffix);
        }
        var preview = AutoLlmLogUtil.tail(file, 2);
        assertEquals("last one\nlast two\n", preview.text()); assertEquals(size, preview.sizeBytes());
        assertTrue(preview.truncated()); assertFalse(preview.byteLimited());
        Files.writeString(file, "appended\n", StandardOpenOption.APPEND);
        assertEquals("last two\nappended\n", AutoLlmLogUtil.tail(file, 2).text());
        Files.writeString(file, "new file\n");
        assertEquals("new file\n", AutoLlmLogUtil.tail(file, 2).text());
    }

    @Test void capsAnOversizedUnicodeLineWithoutBrokenCharacters() throws Exception {
        Path file = temp.resolve("long.jsonl"); Files.writeString(file, "€".repeat(200_000));
        var preview = AutoLlmLogUtil.tail(file, 1);
        assertTrue(preview.text().getBytes(StandardCharsets.UTF_8).length <= AutoLlmLogUtil.MAXIMUM_PREVIEW_BYTES);
        assertFalse(preview.text().contains("�")); assertTrue(preview.text().endsWith("€"));
        assertEquals(1, preview.lineCount()); assertTrue(preview.truncated()); assertTrue(preview.byteLimited());
    }

    @Test void dropsAPartialLeadingLineWhenLaterCompleteLinesAreAvailable() throws Exception {
        Path file = temp.resolve("long.log"); Files.writeString(file, "x".repeat(300_000) + "\ncomplete\nlast\n");
        var preview = AutoLlmLogUtil.tail(file, 500);
        assertEquals("complete\nlast\n", preview.text()); assertEquals(2, preview.lineCount());
        assertTrue(preview.byteLimited());
        Files.writeString(file, "x".repeat(300_000) + "\n" + "y\n".repeat(AutoLlmLogUtil.MAXIMUM_PREVIEW_BYTES / 2));
        var exact = AutoLlmLogUtil.tail(file, 5000);
        assertEquals(5000, exact.lineCount()); assertFalse(exact.byteLimited());
    }

    @Test void waitsForTheLastUtf8CharacterToFinish() throws Exception {
        Path file = temp.resolve("unicode.log"); Files.write(file, new byte[]{'a', (byte)0xe2, (byte)0x82});
        assertEquals("a", AutoLlmLogUtil.tail(file, 1).text());
        assertEquals(1, AutoLlmLogUtil.tail(file, 1).nextCursor());
        Files.write(file, new byte[]{(byte)0xac}, StandardOpenOption.APPEND);
        assertEquals("a€", AutoLlmLogUtil.tail(file, 1).text());
        assertEquals("€", AutoLlmLogUtil.read(file, 1, 65536).text());
    }

    @Test void rejectsUnboundedLineRequestsAndBinaryFiles() throws Exception {
        Path file = temp.resolve("file.bin"); Files.write(file, new byte[]{1, 0, 3});
        assertThrows(IllegalArgumentException.class, () -> AutoLlmLogUtil.tail(file, 0));
        assertThrows(IllegalArgumentException.class, () -> AutoLlmLogUtil.tail(file, 5001));
        assertThrows(IllegalArgumentException.class, () -> AutoLlmLogUtil.tail(file, 500));
    }
}
