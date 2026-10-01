package es.urjc.etsii.grafo.mreflp;

import es.urjc.etsii.grafo.mreflp.alg.LMLSVariant;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "mreflp")
public class MREFLPConfig {
    private LMLSVariant variant = LMLSVariant.LMLS;
    private double timeLimitSeconds = 600;
    private int maxRestarts = 0;
    private String protocol = "pilot";
    public LMLSVariant getVariant() { return variant; }
    public void setVariant(LMLSVariant variant) { this.variant = variant; }
    public double getTimeLimitSeconds() { return timeLimitSeconds; }
    public void setTimeLimitSeconds(double seconds) {
        if (!Double.isFinite(seconds) || seconds <= 0 || seconds > Long.MAX_VALUE / 1000.0) throw new IllegalArgumentException("Invalid time budget");
        timeLimitSeconds = seconds;
    }
    public int getMaxRestarts() { return maxRestarts; }
    public void setMaxRestarts(int maxRestarts) {
        if (maxRestarts < 0) throw new IllegalArgumentException("Negative restart limit");
        this.maxRestarts = maxRestarts;
    }
    public String getProtocol() { return protocol; }
    public void setProtocol(String protocol) { this.protocol = protocol; }
}
