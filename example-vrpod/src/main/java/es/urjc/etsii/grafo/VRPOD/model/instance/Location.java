package es.urjc.etsii.grafo.VRPOD.model.instance;

public class Location {
    public final Point2D point;

    /**
     * Polar coordinates are relative to the depot
     */
    public final PolarCoordinates polar;
    public final int id;

    public Location(int id, Point2D point, PolarCoordinates polar) {
        this.point = point;
        this.polar = polar;
        this.id = id;
    }

    public boolean isDepot(){
        return this.id == 0;
    }

    public double getPolarAngle(){
        return this.polar.tetha;
    }
    public double getPolarVector(){
        return this.polar.vector;
    }
}
