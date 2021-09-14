package es.urjc.etsii.grafo.solver.irace;

import org.graalvm.polyglot.*;
import org.springframework.stereotype.Service;

@Service
public class RLangRunner {
    public void execute(String rCode){
        Context polyglot = Context.newBuilder("R").allowAllAccess(true).build();
        Value v = polyglot.eval("R", rCode);
    }
}
