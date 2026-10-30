package es.urjc.etsii.grafo.autoconfig.irace;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Precision of automatically generated irace domains, seeds and probe configurations. */
public final class IracePrecision {
    public static final int DIGITS = 2;
    private IracePrecision() {}

    public static BigDecimal exact(double value) {
        return BigDecimal.valueOf(value).setScale(DIGITS, RoundingMode.UNNECESSARY);
    }

    public static String domainError(double minimum, double maximum) {
        if (!Double.isFinite(minimum) || !Double.isFinite(maximum)) return "Bounds must be finite";
        if (minimum >= maximum) return "Lower bound must be strictly smaller than upper bound";
        int required = Math.max(Math.max(0, BigDecimal.valueOf(minimum).stripTrailingZeros().scale()),
                Math.max(0, BigDecimal.valueOf(maximum).stripTrailingZeros().scale()));
        return required > DIGITS ? "Bounds require digits=" + required + "; generated irace domains use digits=" + DIGITS : null;
    }
}
