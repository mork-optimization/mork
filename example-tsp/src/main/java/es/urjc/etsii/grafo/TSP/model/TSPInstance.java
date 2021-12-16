package es.urjc.etsii.grafo.TSP.model;

import es.urjc.etsii.grafo.io.Instance;

public class TSPInstance extends Instance {

    /**
     * List of coordinates
     */
    private final Coordinate[] locations;


    /**
     * Distance between all coordinates
     */
    private final double[][] distances;

    /**
     * Constructor
     *
     * @param name      name of the instance
     * @param locations list of coordiantes
     */
    protected TSPInstance(String name, Coordinate[] locations, double[][] distances) {
        super(name);
        this.locations = locations;
        this.distances = distances;
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

        public double[] toList() {
            return new double[]{x, y};
        }
    }

    /**
     * Get coordinate of a specific location (that represents a city, place, facility...)
     *
     * @param id of the location
     * @return the location coordinate
     */
    public Coordinate getCoordinate(int id) {
        return this.locations[id];
    }


    /**
     * Return the euclidean distance between two locations i and j.
     *
     * @param i first location
     * @param j second location
     * @return the euclidean distance
     */
    public double getDistance(int i, int j) {
        return this.distances[i][j];
    }
}
