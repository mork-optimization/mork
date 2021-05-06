package es.urjc.etsii.grafo.util.geometry;

/**
 * Represents a 2D point using cartesian coordinates (X, Y)
 */
public class CartesianPoint {
    public final double x, y;

    public CartesianPoint(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public PolarPoint toPolar(){
        return new PolarPoint(Math.sqrt(x * x + y * y), Math.atan2(y, x));
    }

    public PolarPoint toPolar(CartesianPoint reference){
        double x = this.x - reference.x;
        double y = this.y - reference.y;
        return new PolarPoint(Math.sqrt(x * x + y * y), Math.atan2(y, x));
    }

    public CartesianPoint translate(CartesianPoint p){
        return new CartesianPoint(this.x + p.x, this.y + p.y);
    }

    /**
     * Calculate the Euclidean distance between two points
     * @param a First point, array of coordinates
     * @param b Second point, array of coordinates
     * @return The Euclidean distance between the two points
     * @throws AssertionError If any point is null or the points have different number of dimensions
     */
    public static double distanceBetween(CartesianPoint a, CartesianPoint b) {
        return Math.sqrt(
                Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2)
        );
    }

    public double distanceTo(CartesianPoint a){
        return distanceBetween(this, a);
    }
}
