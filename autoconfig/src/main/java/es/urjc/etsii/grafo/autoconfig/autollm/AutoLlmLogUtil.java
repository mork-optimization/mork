package es.urjc.etsii.grafo.autoconfig.autollm;

import java.io.*;
import java.nio.*;
import java.nio.charset.*;
import java.nio.file.*;

/** Bounded UTF-8 log pages and file-tail previews. */
public final class AutoLlmLogUtil {
    public static final int MAXIMUM_PREVIEW_BYTES = 262144;
    public record Page(long cursor, long nextCursor, String text, boolean more, boolean reset) {}
    public record Preview(String text, int lineCount, long sizeBytes, boolean truncated, boolean byteLimited, long nextCursor) {}
    private AutoLlmLogUtil() {}

    /** Reads only a bounded suffix, even when a file is several gigabytes or has an enormous single line. */
    public static Preview tail(Path file, int maximumLines) {
        if (maximumLines < 1 || maximumLines > 5000) throw new IllegalArgumentException("Preview lines must be between 1 and 5000");
        try (var input = new RandomAccessFile(file.toFile(), "r")) {
            long size = input.length(), offset = Math.max(0, size - MAXIMUM_PREVIEW_BYTES);
            input.seek(Math.max(0, offset - 1));
            int previous = offset > 0 ? input.read() : '\n';
            byte[] bytes = new byte[(int)(size - offset)];
            input.readFully(bytes);
            int begin = bytes.length, separators = 0;
            while (begin > 0) {
                begin--;
                if (bytes[begin] == '\n' && begin < bytes.length - 1 && ++separators == maximumLines) { begin++; break; }
            }
            boolean reachedByteLimit = offset > 0 && begin == 0;
            boolean partialLine = reachedByteLimit && previous != '\n';
            if (partialLine) {
                // Discard a partial leading line when complete later lines are available.
                for (int i = 0; i < bytes.length - 1; i++) {
                    if (bytes[i] == '\n') { begin = i + 1; partialLine = false; break; }
                }
                // A single oversized line is shown as a bounded fragment, starting at a UTF-8 boundary.
                while (begin < bytes.length && (bytes[begin] & 0xc0) == 0x80) begin++;
            }
            for (int i = begin; i < bytes.length; i++) {
                if (bytes[i] == 0) throw new IllegalArgumentException("Binary files cannot be previewed; download the full file");
            }
            var buffer = ByteBuffer.wrap(bytes, begin, bytes.length - begin);
            String text = decode(buffer);
            int lineCount = text.isEmpty() ? 0 : 1;
            for (int i = 0; i < text.length() - 1; i++) if (text.charAt(i) == '\n') lineCount++;
            return new Preview(text, lineCount, size, offset + begin > 0,
                    reachedByteLimit && (partialLine || lineCount < maximumLines), offset + buffer.position());
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }

    public static Page read(Path file, long cursor, int maximumBytes) {
        if (cursor < 0 || maximumBytes < 4 || maximumBytes > 262144) throw new IllegalArgumentException("Invalid log cursor/page size");
        if (!Files.exists(file)) return new Page(cursor, cursor, "", false, false);
        try (var input = new RandomAccessFile(file.toFile(), "r")) {
            boolean reset = cursor > input.length(); if (reset) cursor = 0;
            input.seek(cursor); byte[] bytes = new byte[(int)Math.min(maximumBytes, input.length() - cursor)]; input.readFully(bytes);
            var buffer = ByteBuffer.wrap(bytes); String text = decode(buffer);
            long next = cursor + buffer.position();
            return new Page(cursor, next, text, next > cursor && next < input.length(), reset);
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }

    private static String decode(ByteBuffer buffer) {
        var characters = CharBuffer.allocate(buffer.remaining());
        var decoder = StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPLACE);
        decoder.decode(buffer, characters, false); characters.flip();
        return characters.toString();
    }
}
