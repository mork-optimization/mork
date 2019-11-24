package solution;

/**
 * All neighborhood movements should be represented by different instances of this class.
 * As they are in the same package as the Solution, they can efficiently manipulate it
 */
public abstract class Move implements Comparable<Move> {

    protected final long solutionVersion;

    protected Solution s;

    public Move(Solution s) {
        this.s = s;
        this.solutionVersion = s.version;
    }

    /**
     * Does the solution improve if the current movement is applied
     * @return True if the solution improves, false otherwise
     */
    public abstract boolean improves();

    /**
     * Executes the proposed movement
     */
    public void execute(){
        if(this.solutionVersion != s.version){
            throw new AssertionError(String.format("Solution state changed (%s), cannot execute movement (%s)", s.version, this.solutionVersion));
        }
        _execute();
        s.version++;
    }

    /**
     * Executes the proposed movement, NO VALIDATION
     */
    protected abstract void _execute();

    /**
     *
     * @return the next movement in this generator sequence
     */
    protected abstract Move next();
}
