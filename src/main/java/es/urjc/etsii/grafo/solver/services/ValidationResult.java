package es.urjc.etsii.grafo.solver.services;

public class ValidationResult {
    private final boolean isValid;
    private final String reasonFailed;

    public boolean isValid() {
        return isValid;
    }

    public String getReasonFailed() {
        return reasonFailed;
    }

    private ValidationResult(boolean isValid, String reasonFailed) {
        this.isValid = isValid;
        this.reasonFailed = reasonFailed;
    }

    public static ValidationResult ok(){
        return new ValidationResult(true, "");
    }

    public static ValidationResult fail(String reason){
        return new ValidationResult(false, reason);
    }
}
