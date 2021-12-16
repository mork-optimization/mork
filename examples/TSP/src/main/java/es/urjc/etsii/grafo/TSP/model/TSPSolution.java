package es.urjc.etsii.grafo.TSP.model;

import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.util.ArrayUtil;
import es.urjc.etsii.grafo.util.DoubleComparator;

import java.util.Arrays;

/**
 * This class represents a solution of the problem.
 * A solution is represented by an array of size n, being n the number of locations (cities, facilities, etc.) of the problem.
 * The index represent the order in which the location contained at that position of the array will be visited.
 * <p>
 * Example: an instance with 5 locations, each location with an id [0,4] and a route 3 -> 1 -> 4 -> 0 -> 2 (-> 3) is represented as follows:
 * Index    0    1    2    3    4
 * Value  [ 3 ][ 1 ][ 4 ][ 0 ][ 2 ]
 * <p>
 * The total distance is considered the objective function value of the solution.
 */
public class TSPSolution extends Solution<TSPSolution, TSPInstance> {

    /**
     * Total length of the route
     */
    private double routeLength;

    /**
     * Circular route represented by an integer array:
     * Index        ->     Value
     * Position to visit ->     Location
     */
    private final int[] route;

    /**
     * Initialize solution from instance
     *
     * @param ins instance of the problem
     */
    public TSPSolution(TSPInstance ins) {
        super(ins);
        this.route = new int[ins.numberOfLocations()];
        Arrays.fill(route, -1);
    }

    /**
     * Clone constructor
     *
     * @param s Solution to clone
     */
    public TSPSolution(TSPSolution s) {
        super(s);
        this.route = s.route.clone();
        this.routeLength = s.routeLength;
    }


    @Override
    public TSPSolution cloneSolution() {
        return new TSPSolution(this);
    }

    /**
     * Is the current solution strictly better than the solution given as a parameter?
     * @param other solution we are comparing against
     * @return true if strictly better, false if equals or worse.
     */
    @Override
    protected boolean _isBetterThan(TSPSolution other) {
        return DoubleComparator.isLessThan(this.routeLength, other.routeLength);
    }

    /**
     * Get the current solution score.
     * The difference between this method and recalculateScore is that
     * this result can be a property of the solution, or cached,
     * it does not have to be calculated each time this method is called
     *
     * @return current solution score as double
     */
    @Override
    public double getScore() {
        return this.routeLength;
    }

    /**
     * Set the current solution score.
     */
    public void setScore(double distance) {
        this.routeLength = distance;
    }

    /**
     * Recalculate solution score and validate current solution state
     * You must check that no constraints are broken, and that all costs are valid
     * The difference between this method and getScore is that we must recalculate the score from scratch,
     * without using any cache/shortcuts.
     * DO NOT UPDATE CACHES / MAKE SURE THIS METHOD DOES NOT HAVE SIDE EFFECTS
     *
     * @return current solution score as double
     */
    @Override
    public double recalculateScore() {
        double distance = 0;
        for (int i = 0; i < this.route.length; i++) {
            var j = (i + 1) % this.route.length;
            distance += this.getInstance().getDistance(route[i], route[j]);
        }
        return distance;
    }

    /**
     * Generate a string representation of this solution. Used when printing progress to console,
     * show as minimal info as possible
     *
     * @return Small string representing the current solution
     */
    @Override
    public String toString() {
        return Arrays.toString(this.route) + "\n" + "Score: " + this.routeLength;
    }


    /**
     * Sets in which position (or order) a location will be visited.
     *
     * @param order    position in which the location will be visited
     * @param location location
     */
    public void setOrderOfLocation(int order, int location) {
        this.route[order] = location;
    }

    /**
     * Shuffle route
     */
    public void shuffleRoute() {
        ArrayUtil.shuffle(route);
    }


    /**
     * Swap classical move:
     * Swap the position in the route of two locations, given its actual positions.
     * Example: actual route : [a,b,c,d,e,f], pi = 0,  pj= 1, resultant route= [b,a,c,d,e,f]
     * Example: actual route : [a,b,c,d,e,f], pi = 1,  pj= 4, resultant route= [a,e,c,d,b,f]
     * When the operation is performed, the objective function (this.distance) is updated
     *
     * @param pi actual position of the location
     * @param pj desired position
     */
    public void swapLocationOrder(int pi, int pj) {
        var i = this.route[pi];
        var j = this.route[pj];
        this.routeLength = this.routeLength - getDistanceContribution(pi) - getDistanceContribution(pj);
        this.route[pi] = j;
        this.route[pj] = i;
        this.routeLength = this.routeLength + getDistanceContribution(pi) + getDistanceContribution(pj);
    }


    /**
     * Insert classical move:
     * Deletes a location from and array (given its position) and inserts it in the specified position.
     * Example: actual route : [a,b,c,d,e,f], pi = 0,  pj= 1, resultant route= [b,a,c,d,e,f]
     * Example: actual route : [a,b,c,d,e,f], pi = 1,  pj= 4, resultant route=[a,c,d,e,b,f]
     * Example: actual route : [a,b,c,d,e,f], pi = 5   pj= 3, resultant route= [a,b,c,f,d,e]
     * When the operation is performed, the objective function (this.distance) is updated
     *
     * @param pi actual position of the location
     * @param pj desired position
     */
    public void insertLocationAtPiInPj(int pi, int pj) {
        ArrayUtil.deleteAndInsert(this.route, pi, pj);
       this.routeLength = this.recalculateScore();
    }


    /**
     * Get the position what is visited in a given position of the route
     *
     * @param position position of the route
     * @return location id
     */
    public int getLocation(int position) {
        return this.route[position];
    }


    /**
     * Get the contribution (i.e. the distance to the previous and next location on the route) of a location (given its position).
     * The location is not inserted at that position, it only calculates and returns the value.
     *
     * @param pos position of the route
     * @return the distance
     */
    public double getDistanceContribution(int pos) {
        return getDistanceContributionToPreviousLocation(pos) + getDistanceContributionToNextLocation(pos);
    }

    /**
     * Get the contribution (i.e. the distance to the previous and next location on the route) if it was placed at a specific position on the route.
     * The location is not inserted at that position, it only calculates and returns the value.
     *
     * @param pos position of the route
     * @param loc location to calculate the distance to next location
     * @return the distance
     */
    public double getDistanceContribution(int pos, int loc) {
        return getDistanceContributionToPreviousLocation(pos, loc) + getDistanceContributionToNextLocation(pos, loc);
    }


    /**
     * Get the distance to the previous location
     *
     * @param pos position of the route
     * @return the distance
     */
    public double getDistanceContributionToPreviousLocation(int pos) {
        return this.getDistanceContributionToPreviousLocation(pos, route[pos]);
    }

    /**
     * Get the distance to the previous location if it was placed at a specific position on the route.
     * The location is not inserted at that position, it only calculates and returns the value.
     *
     * @param pos position of the route
     * @param loc location to calculate the distance to next location
     * @return the distance
     */
    public double getDistanceContributionToPreviousLocation(int pos, int loc) {
        var previous = (pos - 1 + this.getInstance().numberOfLocations()) % this.getInstance().numberOfLocations();
        return this.getInstance().getDistance(route[previous], loc);
    }


    /**
     * Get the distance to the next location
     *
     * @param pos position of the route
     * @return the distance
     */
    public double getDistanceContributionToNextLocation(int pos) {
        return this.getDistanceContributionToNextLocation(pos, route[pos]);
    }


    /**
     * Get the distance to the next location if it was placed at a specific position on the route.
     * The location is not inserted at that position, it only calculates and returns the value.
     *
     * @param pos position of the route
     * @param loc location to calculate the distance to next location
     * @return the distance
     */
    public double getDistanceContributionToNextLocation(int pos, int loc) {
        var next = (pos + 1) % this.getInstance().numberOfLocations();
        return this.getInstance().getDistance(loc, route[next]);
    }
}
