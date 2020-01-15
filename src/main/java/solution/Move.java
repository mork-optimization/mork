package solution;

/**
 * All neighborhood moves should be represented by different instances of this class.
 * As they are in the same package as the Solution, they can efficiently manipulate it
 */
public abstract class Move {

    protected final long solutionVersion;

    protected Solution s;

    public Move(Solution s) {
        this.s = s;
        this.solutionVersion = s.version;
    }

    /**
     * Does the solution improve if the current move is applied
     * @return True if the solution improves, false otherwise
     */
    public abstract boolean improves();

    /**
     * Get the best move between two candidates
     * @param o The other move
     * @return Returns the best move
     */
    public abstract <T extends Move> T getBestMove(T o);

    /**
     * Executes the proposed move
     */
    public final void execute(){
        if(this.solutionVersion != s.version){
            throw new AssertionError(String.format("Solution state changed (%s), cannot execute move (%s)", s.version, this.solutionVersion));
        }
        _execute();
        s.version++;
    }

    /**
     * Executes the proposed move,
     * to be implemented by each move type
     */
    protected abstract void _execute();

    /**
     * Get next move in this sequence.
     * @return the next move in this generator sequence if there is a next move, null otherwise
     */
    protected abstract Move next();
}
