package es.urjc.etsii.grafo.autoconfigtests.model;

import es.urjc.etsii.grafo.io.Instance;

public class ACInstance extends Instance{

    private final int length;
    /**
     * Creates a new instance
     *
     * @param id      instance id or instance name
     * @param length content length, N
     */
    protected ACInstance(String id, int length) {
        super(id);
        this.length = length;
        setProperty("length", length);
        setProperty("sqrtlength", Math.sqrt(length));
        setProperty("quartlength", length/4.0);
        setProperty("loglength", Math.log(length));
        setProperty("c", 7);
    }

    public int length() {
        return length;
    }
}
