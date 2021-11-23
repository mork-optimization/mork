package es.urjc.etsii.grafo.solver.services;

/**
 * Result of validating a solution
 */
public class ValidationResult {
    private final boolean isValid;
    private final String reasonFailed;

    /**
     * Is the solution valid?
     *
     * @return true if the solution passed all validations, false if any failed
     */
    public boolean isValid() {
        return isValid;
    }

    /**
     * If the validation failed, returns the cause.
     *
     * @return cause if validation failure
     */
    public String getReasonFailed() {
        return reasonFailed;
    }

    private ValidationResult(boolean isValid, String reasonFailed) {
        this.isValid = isValid;
        this.reasonFailed = reasonFailed;
    }

    /**
     * Validation passed
     *
     * @return ValidationResult
     */
    public static ValidationResult ok(){
        return new ValidationResult(true, "");
    }

    /**
     * Validation failed
     *
     * @param reason reason why the validation failed
     * @return ValidationResult
     */
    public static ValidationResult fail(String reason){
        return new ValidationResult(false, reason);
    }
}
