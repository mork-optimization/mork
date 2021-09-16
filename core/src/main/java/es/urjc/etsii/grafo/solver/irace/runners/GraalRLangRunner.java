package es.urjc.etsii.grafo.solver.irace.runners;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnExpression("!${irace.shell}")
public class GraalRLangRunner implements RLangRunner {
    private static final String R_LANG = "R";
    public void execute(String rCode){
        Context polyglot = Context.newBuilder(R_LANG).option("R.PrintErrorStacktracesToFile", "true").allowAllAccess(true).build();
        Value v = polyglot.eval(R_LANG, rCode);
    }
}
