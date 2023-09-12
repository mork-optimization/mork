package es.urjc.etsii.grafo.autoconfigtests.model;

import es.urjc.etsii.grafo.io.Instance;

public class ACInstance extends Instance{

    private final String content;
    /**
     * Creates a new instance
     *
     * @param id      instance id or instance name
     * @param content
     */
    protected ACInstance(String id, String content) {
        super(id);
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public int length() {
        return content.length();
    }
}
