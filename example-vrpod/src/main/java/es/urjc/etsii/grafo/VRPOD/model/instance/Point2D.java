package es.urjc.etsii.grafo.VRPOD.model.instance;

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
}
