package es.urjc.etsii.grafo.io;

/**
 * Base instance class. All instance subclasses must be immutable after construction
 * Order defaults to instance name, can be changed by user
 * (for example, to order by instance size or any other instance property)
 */
public abstract class Instance implements Comparable<Instance>{
    private final String name;

    /**
     * Creates a new instance
     *
     * @param name instance name
     */
    protected Instance(String name) {
        this.name = name;
    }

    /**
     * {@inheritDoc}
     *
     * Sort order will determine in which order the instances will be solved
     * Sort defaults to instance name
     */
    @Override
    public int compareTo(Instance o) {
        return this.name.compareTo(o.name);
    }

    /**
     * Returns the Instance name
     *
     * @return instance name
     */
    public String getName() {
        return name;
    }
}
