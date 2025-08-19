package es.urjc.etsii.grafo.VRPOD.model.instance;

public class PolarCoordinates {
    public final double vector, tetha;

    public PolarCoordinates(double vector, double tetha) {
        this.vector = vector;
        this.tetha = tetha;
    }

    public Point2D toCartesian(){
        return new Point2D(vector * Math.cos(tetha), vector * Math.sin(tetha));
    }

    public Point2D toCartesian(Point2D reference){
        return toCartesian().translate(reference);
    }
}
