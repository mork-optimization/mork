package solution;

public abstract class GRASPMove extends Move {
    public GRASPMove(Solution s) {
        super(s);
    }

    protected double value;

    public double getValue() {
        return value;
    }
}
