package es.urjc.etsii.grafo.patches;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Block Java API methods configuration
 */
@Configuration
@ConfigurationProperties(prefix = "advanced.block")
public class BlockConfig {

    /**
     * Should we block the method Collections.shuffle(List)?
     */
    private boolean blockCollectionsShuffle = false;

    /**
     * Should we block the method Math.random()?
     */
    private boolean blockMathRandom = false;

    /**
     * Return true if method Collections.shuffle(LIST) should be blocked.
     * Use CollectionUtil.shuffle() instead.
     *
     * @return true if method should be blocked, false otherwise
     */
    public boolean isBlockCollectionsShuffle() {
        return blockCollectionsShuffle;
    }

    /**
     * NOOP after method has been blocked. Only used as a setter when deserializing the configuration file.
     *
     * @param blockCollectionsShuffle true if method should be blocked, false otherwise
     */
    public void setBlockCollectionsShuffle(boolean blockCollectionsShuffle) {
        this.blockCollectionsShuffle = blockCollectionsShuffle;
    }


    /**
     * Return true if method Math.random() should be blocked.
     * Use RandomManager instead.
     *
     * @return true if method should be blocked, false otherwise
     */
    public boolean isBlockMathRandom() {
        return blockMathRandom;
    }

    /**
     * NOOP after method has been blocked. Only used as a setter when deserializing the configuration file.
     *
     * @param blockMathRandom true if method should be blocked, false otherwise
     */
    public void setBlockMathRandom(boolean blockMathRandom) {
        this.blockMathRandom = blockMathRandom;
    }
}
