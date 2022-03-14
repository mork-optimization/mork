package es.urjc.etsii.grafo.io;

/**
 * Base instance class. All instance subclasses must be immutable after construction
 * Order defaults to instance name, can be changed by user
 * (for example, to order by instance size or any other instance property)
 */
public abstract class Instance implements Comparable<Instance>{
    private final String id;
    private String path;

    /**
     * Creates a new instance
     * @param id instance id or instance name
     */
    protected Instance(String id) {
        this.id = id;
    }

    /**
     * {@inheritDoc}
     *
     * Sort order will determine in which order the instances will be solved
     * Sort defaults to instance name
     */
    @Override
    public int compareTo(Instance o) {
        return this.id.compareTo(o.id);
    }

    /**
     * Returns the Instance name
     *
     * @return instance name
     */
    public String getId() {
        return id;
    }

    /**
     * Set instance path, used by the framework
     * @param path instance absolute path where it was loaded from
     */
    void setPath(String path) {
        this.path = path;
    }

    /**
     * Get instance absolute path
     * @return instance path where it was first loaded from
     */
    public String getPath() {
        return path;
    }
}
