package es.urjc.etsii.grafo.VRPOD.model.instance;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.util.DoubleComparator;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public abstract class VRPODInstance extends Instance {

    private static final int AUTOMATIC_EXECUTION_RATIO = 10;
    protected static final Pattern FILE_PATTERN = Pattern.compile("od(\\d+)rho(\\d\\.\\d+)zeta(1\\.\\d+)_(.*)\\.txt");

    protected static final Comparator<VRPODInstance> INSTANCE_COMPARATOR = Comparator
            .comparing(VRPODInstance::getNumOccasionalDrivers)
            .thenComparing(VRPODInstance::getRho)
            .thenComparing(VRPODInstance::getZeta)
            .thenComparing(VRPODInstance::getGenerator);

    protected final int numOcassionalDrivers;

    /**
     * Rho value, extracted from the filename. 0.2, 0.05, 1.2, etc.
     */
    protected final double rho;

    /**
     * Zeta value, extracted from the filename. 1.1, 1.2, etc
     */
    protected final double zeta;

    /**
     * Generator name, extracted from the filename. C101, C102... RC201
     */
    protected final String generator;

    /**
     * Instance name
     */
    protected final String name;

    /**
     * Number of destinations where we have to make a delivery +1, the origin counts as a destination
     */
    protected int numberOfDestinations;

    /**
     * Capacity of each vehicle used, each destination has an associated cost, the vehicle route must: {@code capacity < sum(each destination cost)}
     */
    protected int capacity;

    /**
     * Distances between all destinations, _dist[0][2] returns the distance between the vertex 0 (the warehouse) and vertex 2 (a customer).
     */
    protected double[][] _dist;

    protected Location[] locations;

    protected Location[] getLocations() {
        return locations;
    }

    /**
     * Weigth of the packet to deliver
     */
    protected int[] packetSizes;

    /**
     * Occasional driver destination coordinates
     */
    protected Point2D[] odsCoordinates;

    /**
     * Each occasional driver can potentially deliver a packet to a set of customers, as long as the total distance does not exceed a preconfigured margin
     */
    protected Set<Integer>[] ods2Clients;

    protected Set<Integer>[] clients2Ods;

    public VRPODInstance(String name, int numOccasionalDrivers, int capacity, int numberOfDestinations){
        super(name);
        // There is no occasional driver #0. Begins in 1.
        odsCoordinates = new Point2D[numOccasionalDrivers + 1];
        ods2Clients = new HashSet[numOccasionalDrivers +1];
        this.capacity = capacity;
        this.numberOfDestinations = numberOfDestinations;
        this.numOcassionalDrivers = numOccasionalDrivers;

        this.name = name;
        var matcher = FILE_PATTERN.matcher(name);
        if(!matcher.matches()){
            throw new IllegalArgumentException("Invalid filename, cannot parse required arguments. Make sure the filename conforms to the following RegEX: " + FILE_PATTERN.pattern());
        }
        var filenameOds = Integer.parseInt(matcher.group(1));
        if(filenameOds != numOccasionalDrivers){
            throw new IllegalArgumentException(String.format("Invalid filename, number of file ods (%s) does not match filename (%s)", numOcassionalDrivers, filenameOds));
        }
        this.rho = Double.parseDouble(matcher.group(2));
        this.zeta = Double.parseDouble(matcher.group(3));
        this.generator = matcher.group(4);
    }

    /**
     * Computation of Euclidean distances among all vertices.
     */
    protected void computeDistances() {
        _dist = new double[numberOfDestinations][numberOfDestinations];
        // Customer 0 is the depot, and distance to it has to be also computed.
        for (int i = 0; i< numberOfDestinations - 1; i++) {
            for (int j = i+1; j< numberOfDestinations; j++) {
                double d = distanceBetween(locations[i].point, locations[j].point);
                _dist[i][j] = d;
                _dist[j][i] = d;
            }
        }
    }

    /**
     * Calculate the Euclidean distance between two points
     * @param a First point, array of coordinates
     * @param b Second point, array of coordinates
     * @return The Euclidean distance between the two points
     * @throws AssertionError If any point is null or the points have different number of dimensions
     */
    protected double distanceBetween(Point2D a, Point2D b) {
        return Math.sqrt(
                Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2)
        );
    }

    protected void initializeClients(Point2D[] clientsCoordinates) {
        var depot = clientsCoordinates[0];

        this.locations = new Location[clientsCoordinates.length];
        for (int i = 0; i < clientsCoordinates.length; i++) {
            this.locations[i] = new Location(i, clientsCoordinates[i], clientsCoordinates[i].toPolar(depot));
        }
    }

    protected void inverseOds2Customer() {
        clients2Ods = new HashSet[numberOfDestinations ];
        // Starts in 1, client 0 does not exist
        for (int i = 1; i < this.clients2Ods.length; i++) {
            this.clients2Ods[i] = new HashSet<>();
        }

        for (int od = 1; od < this.ods2Clients.length; od++) {
            for(int client: this.ods2Clients[od]){
                clients2Ods[client].add(od);
            }
        }
    }


    /**
     * Returns the total number of nodes (including the depot)
     * @return
     */
    public int getNumberOfDestinations() {
        return numberOfDestinations;
    }

    /**
     * Returns the number of locations
     * @return Number of locations
     */
    public int getNumberOfClients(){
        return getNumberOfDestinations() - 1;
    }

    /**
     * Returns the number of occasional drivers that are available in this instance.
     * @return
     */
    public int getNumOccasionalDrivers() {
        return this.numOcassionalDrivers;
    }

    /**
     * Returns the set of locations that an ocassional driver can attend
     * @param driver
     */
    public Set<Integer> getClientsOfOccasionalDriver(int driver) {
        assert driver >= 1;
        return Collections.unmodifiableSet(ods2Clients[driver]);
    }

    /**
     * Returns the set of ocasional drivers that can attend a given customer
     * @param client
     */
    public Set<Integer> getOdsOfClient(int client) {
        assert client >= 1 && client <= this.numberOfDestinations;
        return Collections.unmodifiableSet(clients2Ods[client]);
    }


    /**
     * Returns the packetSizes of the given customer id.
     * @param client
     * @return
     */
    public int getPacketSize(int client) {
        assert client >= 1 && client <= this.numberOfDestinations;
        return this.packetSizes[client];
    }

    /**
     * Returns the capacity of the regular vehicles.
     * @return
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * Set the profit if a given od delivers a packet to the given customer
     * @param od The ocassional driver id
     * @param customer The customer id
     * @param value The profit
     */
    abstract void setProfit(int od, int customer, double value);

    /**
     * Get the profit an OD would get if he(she delivers the packet to the given customer
     * @param od The ocassional driver id
     * @param customer The customer id
     * @return The profit
     */
    public abstract double getCost(int od, int customer);

    /**
     * Returns the distance between the given elements (vertices)
     *
     * @param a
     * @param b
     * @return
     */
    public double getDistance(int a, int b) {
        assert DoubleComparator.equals(_dist[a][b], _dist[b][a]);
        return _dist[a][b];
    }

    public String getName() {
        return name;
    }

    public double getRho() {
        return rho;
    }

    public double getZeta() {
        return zeta;
    }

    public String getGenerator() {
        return generator;
    }


    public int getRecommendedNumberOfShakes(){
        return Math.max(getNumOccasionalDrivers() * AUTOMATIC_EXECUTION_RATIO, 1);
    }

    /**
     * Sort instances in this order: by od, rho, zeta, generator type
     */
    @Override
    public int compareTo(Instance o) {
        return INSTANCE_COMPARATOR.compare(this, (VRPODInstance) o);
    }

    public int generatorType(){
        if(generator.startsWith("C")){
            return 1; // clustered
        } else if(generator.startsWith("R")){
            return 2; // random
        } else if(generator.startsWith("RC")){
            return 3; // hybrid
        } else {
            throw new IllegalArgumentException("Unknown generator type: " + generator);
        }
    }

}
