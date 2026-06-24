package es.urjc.etsii.grafo.terminal;

import me.tongfei.progressbar.ProgressBarConsumer;

/**
 * Progress bar consumer that cooperates with Logback console output.
 */
public class ProgressAwareProgressBarConsumer implements ProgressBarConsumer {

    private final int maxRenderedLength;

    public ProgressAwareProgressBarConsumer() {
        this(-1);
    }

    public ProgressAwareProgressBarConsumer(int maxRenderedLength) {
        this.maxRenderedLength = maxRenderedLength;
    }

    @Override
    public int getMaxRenderedLength() {
        return ProgressAwareTerminal.getMaxRenderedLength(maxRenderedLength);
    }

    @Override
    public void accept(String renderedProgress) {
        ProgressAwareTerminal.writeProgress(renderedProgress, getMaxRenderedLength());
    }

    @Override
    public void clear() {
        ProgressAwareTerminal.clearProgress();
    }

    @Override
    public void close() {
        ProgressAwareTerminal.closeProgress();
    }
}
