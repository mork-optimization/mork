package es.urjc.etsii.grafo.autoconfig.testutil.findautoconfig;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;

public class Has2 {
    private int n;

    @AutoconfigConstructor
    public Has2(int n) {
        this.n = n;
    }

    @AutoconfigConstructor
    public Has2() {
        this.n = 0;
    }
}
