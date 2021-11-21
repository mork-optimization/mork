package es.urjc.etsii.grafo.TSP.model;

import es.urjc.etsii.grafo.io.Instance;

public class TSPInstance extends Instance {

    /**
     * List of coordinates
     */
    private final Coordinate[] locations;


    /**
     * Constructor
     *
     * @param name      name of the instance
     * @param locations list of coordiantes
     */
    protected TSPInstance(String name, Coordinate[] locations) {
        super(name);
        this.locations = locations;
    }

    /**
     * Get the list of locations
     *
     * @return list of locations
     */
    public Coordinate[] getLocations() {
        return locations;
    }

    /**
     * Get the number of locations of the instance
     *
     * @return number of locations
     */
    public int numberOfLocations() {
        return locations.length;
    }

    /**
     * 2D coordinate
     */
    public record Coordinate(double x, double y) {
    }
}
