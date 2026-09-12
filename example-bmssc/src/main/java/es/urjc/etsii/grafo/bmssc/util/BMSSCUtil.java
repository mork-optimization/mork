package es.urjc.etsii.grafo.bmssc.util;

import es.urjc.etsii.grafo.util.Context;

import java.util.function.Supplier;

public final class BMSSCUtil {
    private BMSSCUtil() {}

    public static double contribution(double pairSum, int size) {
        return size < 2 ? 0 : pairSum / size;
    }

    /** Keep delta assertions active while temporarily allowing partial or unbalanced solutions. */
    public static <T> T withPartialSolution(Supplier<T> action) {
        boolean validationEnabled = Context.Configurator.isValidationEnabled();
        try (var ignored = Context.suspendObjectiveTracking()) {
            Context.Configurator.disableValidation();
            return action.get();
        } finally {
            if (validationEnabled) Context.Configurator.enableValidation();
            else Context.Configurator.disableValidation();
        }
    }
}
