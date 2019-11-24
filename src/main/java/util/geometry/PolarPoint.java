package util.geometry;

/**
 * Represents a 2D point using Polar coordinates (Vector, tetha)
 */
public class PolarPoint {
    public final double vector, tetha;

    public PolarPoint(double vector, double tetha) {
        this.vector = vector;
        this.tetha = tetha;
    }

    public CartesianPoint toCartesian(){
        return new CartesianPoint(vector * Math.cos(tetha), vector * Math.sin(tetha));
    }

    public CartesianPoint toCartesian(CartesianPoint reference){
        return toCartesian().translate(reference);
    }
}
