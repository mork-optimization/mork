package es.urjc.etsii.grafo.autoconfig.testutil.findautoconfig;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;

public class Has1 {

    private int n;

    @AutoconfigConstructor

    public Has1(int n) {
        this.n = n;
    }

    public Has1() {
        this.n = 0;
    }
}
