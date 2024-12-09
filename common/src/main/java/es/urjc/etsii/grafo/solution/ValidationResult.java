package es.urjc.etsii.grafo.solution;

import es.urjc.etsii.grafo.exception.InvalidSolutionException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Result of validating a solution
 */
public class ValidationResult {
    private final List<String> reasonFailed = new ArrayList<>();

    /**
     * Is the solution valid?
     *
     * @return true if the solution passed all validations, false if any failed
     */
    public boolean isValid() {
        return reasonFailed.isEmpty();
    }

    /**
     * If the validation failed, returns the cause.
     *
     * @return cause if validation failure
     */
    public String getReasonFailed() {
        if(this.reasonFailed.isEmpty()){
            throw new IllegalStateException("Validation passed, no reason to get cause");
        }
        if(this.reasonFailed.size() == 1){
            return this.reasonFailed.getFirst();
        }
        return "Multiple failures: " + String.join("; ", this.reasonFailed);
    }

    /**
     * Number of failed validations
     * @return number of failed validations
     */
    public int getFailCount() {
        return this.reasonFailed.size();
    }

    /**
     * Get all reasons why the validation failed
     * @return list of reasons
     */
    public List<String> getReasonsFailed() {
        return Collections.unmodifiableList(this.reasonFailed);
    }

    private ValidationResult() {}

    public void throwIfFail(){
        if(!this.isValid()){
            throw new InvalidSolutionException(getReasonFailed());
        }
    }

    /**
     * Validation passed
     *
     * @return ValidationResult
     */
    public static ValidationResult ok(){
        return new ValidationResult();
    }

    /**
     * Validation failed
     *
     * @param reason reason why the validation failed
     * @return ValidationResult
     */
    public static ValidationResult fail(String reason){
        var vr = new ValidationResult();
        return vr.addFailure(reason);
    }

    public ValidationResult addFailure(String reason) {
        if(reason != null && !reason.isBlank()){
            this.reasonFailed.add(reason);
        }
        return this;
    }
}
