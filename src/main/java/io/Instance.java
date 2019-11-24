package io;

/**
 * Base instance class. All instance subclasses must be inmutable after construction
 * Order defaults to instance name
 */
public abstract class Instance implements Comparable<Instance>{
    private final String name;

    protected Instance(String name) {
        this.name = name;
    }

    @Override
    public int compareTo(Instance o) {
        return this.name.compareTo(o.name);
    }

    public String getName() {
        return name;
    }
}
