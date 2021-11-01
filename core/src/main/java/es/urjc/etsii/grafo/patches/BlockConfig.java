package es.urjc.etsii.grafo.patches;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "advanced.block")
public class BlockConfig {
    private boolean blockCollectionsShuffle;
    private boolean blockMathRandom;

    public boolean isBlockCollectionsShuffle() {
        return blockCollectionsShuffle;
    }

    public void setBlockCollectionsShuffle(boolean blockCollectionsShuffle) {
        this.blockCollectionsShuffle = blockCollectionsShuffle;
    }

    public boolean isBlockMathRandom() {
        return blockMathRandom;
    }

    public void setBlockMathRandom(boolean blockMathRandom) {
        this.blockMathRandom = blockMathRandom;
    }
}
