package es.urjc.etsii.grafo.autoconfig.autollm;

import java.io.*;
import java.nio.*;
import java.nio.charset.*;
import java.nio.file.*;

/** Bounded byte cursors never rescan a growing log or split a UTF-8 character. */
public final class AutoLlmLogUtil {
    public record Page(long cursor, long nextCursor, String text, boolean more, boolean reset) {}
    private AutoLlmLogUtil() {}
    public static Page read(Path file, long cursor, int maximumBytes) {
        if (cursor < 0 || maximumBytes < 4 || maximumBytes > 262144) throw new IllegalArgumentException("Invalid log cursor/page size");
        if (!Files.exists(file)) return new Page(cursor, cursor, "", false, false);
        try (var input = new RandomAccessFile(file.toFile(), "r")) {
            boolean reset = cursor > input.length(); if (reset) cursor = 0;
            input.seek(cursor); byte[] bytes = new byte[(int)Math.min(maximumBytes, input.length() - cursor)]; input.readFully(bytes);
            var buffer = ByteBuffer.wrap(bytes); var characters = CharBuffer.allocate(bytes.length);
            var decoder = StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPLACE);
            decoder.decode(buffer, characters, false); characters.flip();
            long next = cursor + buffer.position();
            return new Page(cursor, next, characters.toString(), next > cursor && next < input.length(), reset);
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }
}
