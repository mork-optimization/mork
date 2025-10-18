package es.urjc.etsii.grafo.vrpod.model.solution;

import es.urjc.etsii.grafo.vrpod.model.instance.VRPODInstance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.util.DoubleComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class VRPODSolution extends Solution<VRPODSolution, VRPODInstance> {

    private static final Logger log = LoggerFactory.getLogger(VRPODSolution.class);

    // TODO Utilizar streams para permitir utilizar first LS de forma eficiente como alternativa a best LS
    public static final int NOT_ASSIGNED = -1;

    // TODO integrate this
//    public static final MovementsProvider[] movementsProviders = new MovementsProvider[]{VRPODSolution::getOdToRouteMovements, VRPODSolution::getRouteToODMovements, VRPODSolution::get2OptMovements, VRPODSolution::getInsertRouteMovements, VRPODSolution::getSwapRouteMovements};

    public VRPODInstance ins;

    /**
     * Delivery type for each customer
     */
    DeliveryType[] attendedByType;

    /**
     * Who will attend each client?
     * attendedByID[3] --> 5 means client 3 is attended by driver 5.
     * Look attendedByType to know the driver type
     */
    int[] attendedByID;

    /**
      Normal drivers, each position representents a route, with the order in which customers are going to be iterated
     */
    List<Route> normalDrivers;

    public List<Route> getNormalDrivers() {
        return Collections.unmodifiableList(normalDrivers);
    }

    /**
     * Returns which customer a given OD will attend, NOT_ASSIGNED if not assigned
     */
    int[] ODattends;

    Set<Integer> unassignedClients;

    Set<Integer> usedOds;
    /**
     * Current value of the objective function
     */
    double objValue = 0d;

    public Map<String, Object> otherData = new HashMap<>();

    boolean hasBeenCleared = false;


    private VRPODSolution(VRPODSolution solution){
        super(solution.getInstance());
        this.ins = solution.ins;
        this.hasBeenCleared = solution.hasBeenCleared;
        this.unassignedClients = new HashSet<>(solution.unassignedClients);
        this.ODattends = Arrays.copyOf(solution.ODattends, solution.ODattends.length);
        this.attendedByID = Arrays.copyOf(solution.attendedByID, solution.attendedByID.length);
        this.attendedByType = Arrays.copyOf(solution.attendedByType, solution.attendedByType.length);
        this.usedOds = new HashSet<>(solution.usedOds);
        this.otherData = new HashMap<>(solution.otherData);
        this.objValue = solution.objValue;
        this.normalDrivers = new ArrayList<>(solution.normalDrivers.size());
        for (Route normalDriver : solution.normalDrivers) {
            this.normalDrivers.add(normalDriver == null ? null : normalDriver.clone());
        }
    }
    
    public VRPODSolution(VRPODInstance ins) {
        super(ins);
        this.ins = ins;
        this.attendedByType = new DeliveryType[ins.getNumberOfDestinations()];
        for (int client = 1; client < this.attendedByType.length; client++) {
            this.attendedByType[client] = DeliveryType.NOT_ASSIGNED_YET;
        }

        this.normalDrivers = new ArrayList<>();
        // Posicion 0 nula useless asdadsa
        this.normalDrivers.add(null);
        attendedByID = new int[ins.getNumberOfDestinations()];
        Arrays.fill(attendedByID, NOT_ASSIGNED);
        ODattends = new int[ins.getNumberOfDestinations()];
        Arrays.fill(ODattends, NOT_ASSIGNED);
        unassignedClients = new HashSet<>();
        for (int i = 1; i < ins.getNumberOfDestinations(); i++) {
            unassignedClients.add(i);
        }

        usedOds = new HashSet<>(ins.getNumOccasionalDrivers());
    }

    public Set<Integer> getUnassignedClients(){
        return Collections.unmodifiableSet(this.unassignedClients);
    }

    /**
     * Check if a normal driver, or a normal route, can allocate the given customer
     * @return True if the given driver can carry the packet for the given customer
     */
    public boolean canCarry(int driver, int customer) {

        assert driver >= 1 && driver < this.normalDrivers.size();

        // Current vehicle weight + packet weigth must be less than the maximum charge allowed
        return this.normalDrivers.get(driver).capacityLeft - this.ins.getPacketSize(customer) >= 0;
    }

    /**
     * Returns the possible store or normal drivers for a given customer
     * @param customer id of the customer
     * @return Set of Normal Drivers that can attent the given client
     */
    public Set<Integer> getAvailableNormalDriversFor(int customer){
        Set<Integer> availableDrivers = new HashSet<>();
        boolean atLeastOneEmpty = false;
        for (int i = 1; i < this.normalDrivers.size(); i++) {
            if(this.normalDrivers.get(i).nodes.isEmpty())
                atLeastOneEmpty = true;
            if(canCarry(i, customer))
                availableDrivers.add(i);
        }
        // There is always the possibility to create a new driver, assume that the new driver can carry the packet
        if(!atLeastOneEmpty) availableDrivers.add(normalDrivers.size());
        return availableDrivers;
    }

    public List<Integer> getAvailableODDriversFor(int client) {

        var todos = ins.getOdsOfClient(client);
        var usados = getUsedOds();
        List<Integer> result = new ArrayList<>(todos.size());  // En el peor caso ninguno esta usado
        for (var od : todos) {
            if(!usados.contains(od))
                result.add(od);
        }
        return result;
    }

    /**
     * Get the current optimal value
     *
     * @return the current optimal value
     */
    public double getOptimalValue() {
        assert this.hasBeenCleared || this.objValue == Double.MAX_VALUE || DoubleComparator.equals(this._recalculateCurrentValue(), this.objValue):
                String.format("current value failed validation, expected %s, got cached %s", this._recalculateCurrentValue(), this.objValue);

        return this.objValue;
    }

    private boolean validateODs() {
        var usedOds = new HashSet<Integer>();
        for (int i = 0; i < ODattends.length; i++) {
            int customer = ODattends[i];
            if(customer != NOT_ASSIGNED){
                usedOds.add(i);
                // el array inverso debe coincidir con el ID actual, y el tipo debe ser el correcto
                if(attendedByID[customer] != i){
                    log.debug("Ocassional driver {} attends customer {} but customer {} is attended by {}", i, customer, customer, attendedByID[customer]);
                    return false;
                }
                if(attendedByType[customer] != DeliveryType.OCCASIONAL_DRIVER){
                    log.debug("Ocassional driver {} attends customer {} but customer {} status is {}", i, customer, customer, attendedByType[customer]);
                    return false;
                }
            }
        }

        int count = 0;
        for (DeliveryType deliveryType : this.attendedByType) {
            if (deliveryType == DeliveryType.OCCASIONAL_DRIVER)
                count++;
        }
        if(count != usedOds.size()) {
            log.debug("Number of used ods does not match sum of attendedByType");
            return false;
        }

        if(usedOds.size() != this.usedOds.size()){
            log.debug("Number of used ods does not match cached set");
            return false;
        }
        usedOds.removeAll(this.usedOds);
        if(!usedOds.isEmpty()){
            log.debug("Missing used ods from cached set");
            return false;
        }
        return true;
    }

    private double _recalculateCurrentValue(){
        assert this.attendedByID.length == this.attendedByType.length: "Who and which type arrays should be of equal length";
        assert validateODs() : "ODs validation failed";

        double normalDriversCost = 0d;
        for (int route = 1; route < this.normalDrivers.size(); route++) {
            Route r = this.normalDrivers.get(route);

            if (r.nodes.size() == 0) continue;
            for (int i = 1; i < r.nodes.size(); i++) {
                normalDriversCost += ins.getDistance(r.nodes.get(i - 1), r.nodes.get(i));
            }
            normalDriversCost += ins.getDistance(0, r.nodes.get(0));
            normalDriversCost += ins.getDistance(0, r.nodes.get(r.nodes.size() - 1));
        }

        double ocassionalDriversCost = 0;
        for (int i = 1; i < this.attendedByID.length; i++) {
            if(this.attendedByType[i] != DeliveryType.OCCASIONAL_DRIVER) continue;
            ocassionalDriversCost += this.ins.getCost(attendedByID[i], i);
        }
        return normalDriversCost + ocassionalDriversCost;
    }


    /**
     * How expensive is assigning a customer to the given driver. Adds customer to the end of the route
     * @param driver id of the driver
     * @param customer id of the customer
     * @return Cost of assigning a customer to a given driver, in the last position of the driver route
     */
    public double getAssignCostNormal(int driver, int customer) {
        // Verify that the driver either is new or is in the array
        assert driver <= this.normalDrivers.size();

        // If it is a new driver
        if(driver == this.normalDrivers.size() || this.normalDrivers.get(driver).nodes.isEmpty()){
            return this.ins.getDistance(customer, 0) * 2;
        }

        // If it is not a new driver, check that the client is not already in the list
        assert !this.normalDrivers.get(driver).nodes.contains(customer);

        Route r = this.normalDrivers.get(driver);

        // A ---> B if we insert a middle point C: A ---> C ---> B the new cost = oldCost + d(A,C) + d(C,B) - d(A,B)
        // We only return the difference, customer is the middle point

        int last = r.nodes.size()-1;
        double cost =  ins.getDistance(r.nodes.get(last), customer) + ins.getDistance(customer, 0) - ins.getDistance(r.nodes.get(last), 0);
        assert DoubleComparator.isPositiveOrZero(cost): "Negative Cost????";

        return cost;
    }

    public double getAssignCostOD(int driver, int customer) {
        assert this.ODattends.length > customer;
        assert this.getAvailableODDriversFor(customer).contains(driver): String.format("OD Driver %s cannot attend customer %s", driver, customer);
        assert this.ODattends[driver] != customer: String.format("OD %s already attending that customer %s", driver, customer);
        assert this.ODattends[driver] == NOT_ASSIGNED: String.format("OD %s attending different customer %s - %s", driver, this.ODattends[driver], customer);

        double cost = _getAssignCostOD(driver, customer);
        assert cost >= 0: "Negative cost???";

        return cost;
    }

    double _getAssignCostOD(int driver, int customer){
        return this.ins.getCost(driver, customer);
    }

    public void assignNormal(int driver, int position, int customer, double cost) {
        assert this.attendedByType[customer] == DeliveryType.NOT_ASSIGNED_YET;
        assert this.attendedByID[customer] == NOT_ASSIGNED;
        assert DoubleComparator.isPositiveOrZero(cost): "Assign decreases obj value???";
        assert driver <= this.normalDrivers.size();

        this.unassignedClients.remove(customer);

        this.attendedByType[customer] = DeliveryType.STORE_DRIVER;
        this.attendedByID[customer] = driver;

        if(normalDrivers.size() == driver){
            normalDrivers.add(new Route(driver, ins.getCapacity(), getInstance().getNumberOfClients()));
        }
        Route r = this.normalDrivers.get(driver);
        assert r.capacityLeft >= this.ins.getPacketSize(customer): "Package is bigger than remaining route space";
        r.nodes.add(position, customer);
        r.cost += cost;
        r.capacityLeft -= this.ins.getPacketSize(customer);
        this.objValue += cost;
    }

    public void revertAssignNormal(int driver, int customer, double cost) {
        assert this.attendedByType[customer] == DeliveryType.STORE_DRIVER;
        assert this.attendedByID[customer] == driver;
        assert DoubleComparator.isNegativeOrZero(cost): "Revert cannot increase obj value";
        assert driver < this.normalDrivers.size();

        this.unassignedClients.add(customer);

        this.attendedByType[customer] = DeliveryType.NOT_ASSIGNED_YET;
        this.attendedByID[customer] = NOT_ASSIGNED;

        Route r = this.normalDrivers.get(driver);
        assert r.nodes.contains(customer);
        r.nodes.remove((Integer) customer);
        r.cost += cost;
        r.capacityLeft += this.ins.getPacketSize(customer);
        this.objValue += cost;
    }

    public void assignOD(int driver, int customer) {
        this.assignOD(driver, customer, getAssignCostOD(driver, customer));
    }

    public void assignOD(int driver, int customer, double cost) {
        //assert debug("Trying to assign OD %s to customer %s ", driver, customer);
        assert this.attendedByType[customer] == DeliveryType.NOT_ASSIGNED_YET;
        assert this.getAvailableODDriversFor(customer).contains(driver): String.format("OD Driver %s cannot attend customer %s", driver, customer);
        assert this.attendedByID[customer] == NOT_ASSIGNED;
        assert this.ODattends[driver] == NOT_ASSIGNED;
        assert DoubleComparator.isPositiveOrZero(cost): "Assign decreases obj value???";
        assert !getUsedOds().contains(driver);

        this.unassignedClients.remove(customer);
        this.usedOds.add(driver);

        this.attendedByType[customer] = DeliveryType.OCCASIONAL_DRIVER;
        this.attendedByID[customer] = driver;
        this.ODattends[driver] = customer;

        this.objValue += cost;
    }

    public void revertAssignOD(int driver, int client, double cost) {
        //assert debug("Trying to revert OD %s to client %s ", driver, client);

        assert this.attendedByType[client] == DeliveryType.OCCASIONAL_DRIVER;
        assert this.attendedByID[client] == driver;
        assert this.ODattends[driver] == client;
        assert DoubleComparator.isNegativeOrZero(cost): "Revert cannot increase obj value";
        assert getUsedOds().contains(driver);

        this.unassignedClients.add(client);
        this.usedOds.remove(driver);

        this.attendedByType[client] = DeliveryType.NOT_ASSIGNED_YET;
        this.attendedByID[client] = NOT_ASSIGNED;
        this.ODattends[driver] = NOT_ASSIGNED;

        this.objValue += cost;
    }

    public boolean isAssigned(int customer){
        return this.attendedByType[customer] != DeliveryType.NOT_ASSIGNED_YET;
    }

    public int getNumberOfNormalDrivers(){ return this.normalDrivers.size();}

    public long getNumberOfOdUsed(){
        return Arrays.stream(this.attendedByType).filter(i -> i == DeliveryType.OCCASIONAL_DRIVER).count();
    }

    public Set<Integer> getUsedOds(){
        return Collections.unmodifiableSet(this.usedOds);
    }

    List<ODToRouteMove> moveCostFromOdToNormalDriver(int od){
        assert od >= 0 && od < this.ODattends.length: "Out of range";
        int customer = this.ODattends[od];
        assert customer != NOT_ASSIGNED;
        assert this.attendedByID[customer] == od;
        assert this.attendedByType[customer] == DeliveryType.OCCASIONAL_DRIVER;

        List<ODToRouteMove> movements = new ArrayList<>();
        double odCost = -this.ins.getCost(od, customer);

        for (int i = 1; i < normalDrivers.size(); i++) {
            Route r = normalDrivers.get(i);
            if(r.capacityLeft < this.ins.getPacketSize(customer)){
                continue;
            }
            int position = -1;
            double normalCost = Double.MAX_VALUE;
            for (int pos = 0; pos <= r.nodes.size(); pos++) {
                int prev = (pos == 0) ? 0 : r.nodes.get(pos - 1);
                int next = (pos == r.nodes.size()) ? 0 : r.nodes.get(pos);

                // (Coste de insertar en la ruta) - Coste de que lo lleve el OD
                double _normalCost = ins.getDistance(prev, customer) + ins.getDistance(customer, next) - ins.getDistance(prev, next);
                if (_normalCost < normalCost) {
                    position = pos;
                    normalCost = _normalCost;
                }
                assert position != -1;
            }

            movements.add(new ODToRouteMove(this, od, customer, r.driver, position, odCost, normalCost));
        }
        return movements;
    }


    public double getRemoveCost(int driver, int position, int customer){
        assert driver < this.normalDrivers.size();
        assert position < this.normalDrivers.get(driver).nodes.size();
        assert this.normalDrivers.get(driver).nodes.get(position) == customer;
        assert this.normalDrivers.get(driver).nodes.get(position) == customer;

        double cost = _getRemoveCost(driver, position, customer);

        assert DoubleComparator.isNegativeOrZero(cost): "POSITIVE COST WHEN REMOVING????";

        return cost;
    }

    private double _getRemoveCost(int driver, int position, int customer){
        Route r = this.normalDrivers.get(driver);

        // 4 Possible cases, there is only one person, we are removing one in the middle, we are removing the last, we are removing the first
        if(r.nodes.size() == 1){
            assert position == 0;
            return -2 * ins.getDistance(0, customer);
        }

        // El previo del primero es el warehouse. El siguiente del ultimo es el warehouse
        int prev = position == 0? 0 : r.nodes.get(position - 1);
        int next = position == r.nodes.size() - 1 ? 0 : r.nodes.get(position + 1);
        // A ---> B if we insert a middle point C: A ---> C ---> B the new cost = oldCost + d(A,C) + d(C,B) - d(A,B)
        // We only return the difference, customer is the middle point

        return -ins.getDistance(prev, customer) - ins.getDistance(customer, next) + ins.getDistance(prev, next);
    }


    public double getInsertCost(int driver, int position, int customer){
        assert driver <= this.normalDrivers.size();

        // todo al ejecutar este return no se pasan el resto de validaciones
        if(driver == this.normalDrivers.size() && position == 0){
            return 2 * ins.getDistance(0, customer);
        }

        assert position <= this.normalDrivers.get(driver).nodes.size();

        Route r = this.normalDrivers.get(driver);

        double cost = _getInsertCost(r, position, customer);

        assert r.capacityLeft >= ins.getPacketSize(customer);
        assert DoubleComparator.isPositiveOrZero(cost): "NEGATIVE OR ZERO COST WHEN INSERTING????";

        return cost;
    }

    double _getReplaceCost(Route r, int position, int newCustomer){

        // El previo del primero es el warehouse. El siguiente del ultimo es el warehouse
        int prev = position == 0? 0: r.nodes.get(position - 1);
        int next = position >= r.nodes.size() -1 ? 0 : r.nodes.get(position+1);
        int oldCustomer = r.nodes.get(position);
        // We only return the difference, if we swap the customer in the given position with a new customer

        return  - ins.getDistance(prev, oldCustomer) - ins.getDistance(oldCustomer, next)
                + ins.getDistance(prev, newCustomer) + ins.getDistance(newCustomer, next);
    }
    double _getInsertCost(Route r, int position, int customer) {
        // If the driver does not exist, the driver is new --> the total distance is warehouse -> customer -> warehouse
        // => 2 * d(warehouse, client)
        if(r.nodes.size() == 0){
            return 2 * ins.getDistance(0, customer);
        }

        // El previo del primero es el warehouse. El siguiente del ultimo es el warehouse
        int prev = position == 0? 0: r.nodes.get(position - 1);
        int next = position >= r.nodes.size() ? 0 : r.nodes.get(position);
        // A ---> B if we insert a middle point C: A ---> C ---> B the new cost = oldCost + d(A,C) + d(C,B) - d(A,B)
        // We only return the difference, customer is the middle point

        return ins.getDistance(prev, customer) + ins.getDistance(customer, next) - ins.getDistance(prev, next);
    }

    public void deassign(int client){
        if(this.attendedByType[client] == DeliveryType.NOT_ASSIGNED_YET) {
            throw new IllegalStateException("Cannot deassign a client that has not been assigned");
        }

        int driver = attendedByID[client];
        if(this.attendedByType[client] == DeliveryType.STORE_DRIVER){
            Route r = normalDrivers.get(driver);
            r.capacityLeft += ins.getPacketSize(client);
            double cost = this._getRemoveCost(driver, r.nodes.indexOf(client), client);
            r.cost += cost;
            this.objValue += cost;
            r.nodes.remove((Integer) client);
        } else {
            this.objValue -= ins.getCost(driver, client);
            this.ODattends[driver] = NOT_ASSIGNED;
            this.usedOds.remove(driver);
        }
        this.attendedByType[client] = DeliveryType.NOT_ASSIGNED_YET;
        this.unassignedClients.add(client);
        this.attendedByID[client] = VRPODSolution.NOT_ASSIGNED;

        this.getOptimalValue();
    }

    void validate(Route r){
        assert r.capacityLeft >= 0: "Negative capacity";
        assert r.nodes.stream().mapToInt(i -> ins.getPacketSize(i)).sum() == ins.getCapacity() - r.capacityLeft;
        assert DoubleComparator.isPositiveOrZero(r.cost): "Negative cost";
        assert DoubleComparator.equals(this._recalculateCurrentValue(),this.objValue): "f.o failed to validate";
    }

    String isValid(Route r){
        if(r.capacityLeft < 0){
            return "Negative capacity";
        }
        if(r.nodes.stream().mapToInt(i -> ins.getPacketSize(i)).sum() != ins.getCapacity() - r.capacityLeft){
            return "Packet size mismatch %s != %s".formatted(r.nodes.stream().mapToInt(i -> ins.getPacketSize(i)).sum(), ins.getCapacity() - r.capacityLeft);
        }
        if(DoubleComparator.isNegative(r.cost)){
            return "Negative cost";
        }
        if(!DoubleComparator.equals(this._recalculateCurrentValue(),this.objValue)){
            return "f.o failed to validate";
        }
        return "";
    }

    void validate(Route r1, Route r2){
        validate(r1);
        validate(r2);
    }

    /**
     * Metodo chapucero para ahorrar memoria cuando stackeamos miles de soluciones
     * TODO un fix de verdad
     */
    public void saveRAM(){
        if(hasBeenCleared) return;

        this.hasBeenCleared = true;
        this.usedOds = null;
        this.normalDrivers = null;
        this.attendedByType = null;
        this.attendedByID = null;
        this.ODattends = null;
        this.unassignedClients = null;
    }

    public VRPODSolution cloneSolution(){
        return new VRPODSolution(this);
    }

    public double getScore() {
        return this.objValue;
    }

    public double recalculateScore() {
        return this._recalculateCurrentValue();
    }

    @Override
    public String toString() {
        return "%s".formatted(this.objValue);
    }


    public enum DeliveryType {
        STORE_DRIVER,
        OCCASIONAL_DRIVER,
        NOT_ASSIGNED_YET
    }

    public class Route {
        int driver;
        int capacityLeft;
        double cost;
        List<Integer> nodes;

        private Route(){}

        public Route(int driver, int capacity, int numberOfClients) {
            this.driver = driver;
            this.capacityLeft = capacity;
            this.nodes = new ArrayList<>(numberOfClients);
        }

        public Route clone(){
            Route r = new Route(driver, ins.getCapacity(), getInstance().getNumberOfClients());
            r.driver = this.driver;
            r.capacityLeft = this.capacityLeft;
            r.cost = this.cost;
            r.nodes = new ArrayList<>(this.nodes);
            return r;
        }

        public int getDriver() {
            return driver;
        }

        public int getCapacityLeft() {
            return capacityLeft;
        }

        public double getCost() {
            return cost;
        }

        public int getNumberOfClients() {
            return nodes.size();
        }

        public List<Integer> getNodes() {
            return Collections.unmodifiableList(nodes);
        }
    }
}
