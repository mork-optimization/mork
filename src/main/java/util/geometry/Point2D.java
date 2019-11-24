package util.geometry;

public class Point2D {
    public final double x, y;

    public Point2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public PolarCoordinates toPolar(){
        return new PolarCoordinates(Math.sqrt(x * x + y * y), Math.atan2(y, x));
    }

    public PolarCoordinates toPolar(Point2D reference){
        double x = this.x - reference.x;
        double y = this.y - reference.y;
        return new PolarCoordinates(Math.sqrt(x * x + y * y), Math.atan2(y, x));
    }

    public Point2D translate(Point2D p){
        return new Point2D(this.x + p.x, this.y + p.y);
    }

    /**
     * Calculate the Euclidean distance between two points
     * @param a First point, array of coordinates
     * @param b Second point, array of coordinates
     * @return The Euclidean distance between the two points
     * @throws AssertionError If any point is null or the points have different number of dimensions
     */
    public static double distanceBetween(Point2D a, Point2D b) {
        return Math.sqrt(
                Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2)
        );
    }

    public double distanceTo(Point2D a){
        return distanceBetween(this, a);
    }
}
