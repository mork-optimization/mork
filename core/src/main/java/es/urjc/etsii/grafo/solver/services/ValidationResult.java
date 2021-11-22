package es.urjc.etsii.grafo.solver.services;

/**
 * <p>ValidationResult class.</p>
 *
 */
public class ValidationResult {
    private final boolean isValid;
    private final String reasonFailed;

    /**
     * <p>isValid.</p>
     *
     * @return a boolean.
     */
    public boolean isValid() {
        return isValid;
    }

    /**
     * <p>Getter for the field <code>reasonFailed</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getReasonFailed() {
        return reasonFailed;
    }

    private ValidationResult(boolean isValid, String reasonFailed) {
        this.isValid = isValid;
        this.reasonFailed = reasonFailed;
    }

    /**
     * <p>ok.</p>
     *
     * @return a {@link es.urjc.etsii.grafo.solver.services.ValidationResult} object.
     */
    public static ValidationResult ok(){
        return new ValidationResult(true, "");
    }

    /**
     * <p>fail.</p>
     *
     * @param reason a {@link java.lang.String} object.
     * @return a {@link es.urjc.etsii.grafo.solver.services.ValidationResult} object.
     */
    public static ValidationResult fail(String reason){
        return new ValidationResult(false, reason);
    }
}
