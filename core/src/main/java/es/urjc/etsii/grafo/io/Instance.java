package es.urjc.etsii.grafo.io;

import java.util.*;

/**
 * Base instance class. All instance subclasses must be immutable after construction
 * Order defaults to instance name, can be changed by user
 * (for example, to order by instance size or any other instance property)
 */
public abstract class Instance implements Comparable<Instance>{
    private final String id;
    private String path;

    private static final Set<String> uniqueProperties = new HashSet<>();
    private final Map<String, Object> properties = new HashMap<>();


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
    protected void setPath(String path) {
        this.path = path;
    }

    /**
     * Get instance absolute path
     * @return instance path where it was first loaded from
     */
    public String getPath() {
        return path;
    }

    /**
     * Get custom instance property.  For instance, if instance is a graph: number of nodes, number of edges, etc.
     * Properties in this map will be used by the framework to provide additional context in some operations, for example when serializing results.
     * @param key Property name. Must be the same for all instances
     * @return Value if property name exists
     * @throws IllegalArgumentException if key/property name is not defined for the current instance
     */
    public Object getProperty(String key){
        Object property = this.properties.get(key);
        if(property == null){
            throw new IllegalArgumentException("Invalid property %s, check that the property has been set on instance load");
        }
        return property;
    }

    /**
     * Get custom instance property.  For instance, if instance is a graph: number of nodes, number of edges, etc.
     * Properties in this map will be used by the framework to provide additional context in some operations, for example when serializing results.
     * @param key Property name. Must be the same for all instances
     * @param defaultValue Value to return if key does not exist
     * @return Value if property name exists, default value if not
     */
    public Object getPropertyOrDefault(String key, Object defaultValue){
        return this.properties.getOrDefault(key, defaultValue);
    }

    /**
     * Set custom instance property. For instance, if instance is a graph: number of nodes, number of edges, etc.
     * @param key Property name, must be common for all instances.
     * @param property Property value.
     */
    public void setProperty(String key, Object property){
        if(key == null){
            throw new IllegalArgumentException("Null property key");
        }
        if(property == null){
            throw new IllegalArgumentException("Null property value");
        }

        Instance.uniqueProperties.add(key);
        this.properties.put(key, property);
    }

    /**
     * Get all property keys used so far for all instances
     * @return Set of all known property keys
     */
    public static Set<String> getUniquePropertiesKeys(){
        return Collections.unmodifiableSet(Instance.uniqueProperties);
    }

    /**
     * Destroy global memorized property names
     */
    public static void resetProperties(){
        Instance.uniqueProperties.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Instance instance = (Instance) o;
        return id.equals(instance.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
