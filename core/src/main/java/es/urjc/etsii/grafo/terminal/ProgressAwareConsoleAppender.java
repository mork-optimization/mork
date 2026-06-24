package es.urjc.etsii.grafo.terminal;

import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.encoder.Encoder;
import ch.qos.logback.core.joran.spi.ConsoleTarget;
import ch.qos.logback.core.spi.DeferredProcessingAware;
import ch.qos.logback.core.status.ErrorStatus;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.WarnStatus;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * Logback console appender that preserves the active progress bar line.
 *
 * @param <E> log event type
 */
public class ProgressAwareConsoleAppender<E> extends UnsynchronizedAppenderBase<E> {

    private ConsoleTarget target = ConsoleTarget.SystemOut;
    private Encoder<E> encoder;

    /**
     * Sets the console target. Recognized values are System.out and System.err.
     *
     * @param value console target name
     */
    public void setTarget(String value) {
        ConsoleTarget configuredTarget = ConsoleTarget.findByName(value.trim());
        if (configuredTarget == null) {
            Status status = new WarnStatus("[" + value + "] should be one of " + Arrays.toString(ConsoleTarget.values()), this);
            status.add(new WarnStatus("Using previously set target, System.out by default.", this));
            addStatus(status);
            return;
        }
        target = configuredTarget;
    }

    /**
     * Returns the current console target.
     *
     * @return target name
     */
    public String getTarget() {
        return target.getName();
    }

    @Override
    public void start() {
        int errors = 0;
        if (encoder == null) {
            addStatus(new ErrorStatus("No encoder set for the appender named \"" + name + "\".", this));
            errors++;
        }
        if (errors > 0) {
            return;
        }

        ProgressAwareTerminal.setOutputStream(resolveTargetStream());
        super.start();
        writeHeader();
    }

    protected OutputStream resolveTargetStream() {
        return target.getStream();
    }

    @Override
    protected void append(E eventObject) {
        try {
            if (eventObject instanceof DeferredProcessingAware deferredProcessingAware) {
                deferredProcessingAware.prepareForDeferredProcessing();
            }
            writeBytes(encoder.encode(eventObject));
        } catch (IOException e) {
            this.started = false;
            addStatus(new ErrorStatus("IO failure in appender", this, e));
        }
    }

    @Override
    public void stop() {
        if (!isStarted()) {
            return;
        }
        writeFooter();
        super.stop();
    }

    public Encoder<E> getEncoder() {
        return encoder;
    }

    public void setEncoder(Encoder<E> encoder) {
        this.encoder = encoder;
    }

    private void writeHeader() {
        try {
            writeBytes(encoder.headerBytes());
        } catch (IOException e) {
            this.started = false;
            addStatus(new ErrorStatus("Failed to initialize encoder for appender named [" + name + "].", this, e));
        }
    }

    private void writeFooter() {
        try {
            writeBytes(encoder.footerBytes());
        } catch (IOException e) {
            addStatus(new ErrorStatus("Failed to write footer for appender named [" + name + "].", this, e));
        }
    }

    private void writeBytes(byte[] bytes) throws IOException {
        if (bytes == null || bytes.length == 0) {
            return;
        }
        ProgressAwareTerminal.writeLog(bytes, 0, bytes.length);
    }
}
