package es.urjc.etsii.grafo.autoconfig.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public final class ParserTest {


    @Test
    public void parseBasicTypes() {
        String alg = "false";
        //var algorithm = algComponent.buildFromString(alg);
    }

    @Test
    public void checkFailsParsing(){

    }

    @ParameterizedTest()
    @ValueSource(strings = {"1.234", "0.0", "-145.93e10", "5.1e10"})
    public void basicParseFloat(String value){
        var parser = AlgComponentService.getParser(value);
        var ctx = parser.propertyValue();
        Assertions.assertNull(ctx.component());
        var l = ctx.literal();

        Assertions.assertNotNull(l);
        Assertions.assertNull(l.NullLiteral());
        Assertions.assertNull(l.BooleanLiteral());
        Assertions.assertNull(l.CharacterLiteral());
        Assertions.assertNotNull(l.FloatingPointLiteral());
        Assertions.assertNull(l.IntegerLiteral());
        Assertions.assertNull(l.StringLiteral());
        Assertions.assertNull(l.arrayLiteral());

        Assertions.assertEquals(value, l.FloatingPointLiteral().getSymbol().getText());
    }

    @ParameterizedTest()
    @ValueSource(strings = {"-94", "0", "12395"})
    public void basicParseInteger(String value){
        var parser = AlgComponentService.getParser(value);
        var ctx = parser.propertyValue();
        Assertions.assertNull(ctx.component());
        var l = ctx.literal();

        Assertions.assertNotNull(l);
        Assertions.assertNull(l.NullLiteral());
        Assertions.assertNull(l.BooleanLiteral());
        Assertions.assertNull(l.CharacterLiteral());
        Assertions.assertNull(l.FloatingPointLiteral());
        Assertions.assertNotNull(l.IntegerLiteral());
        Assertions.assertNull(l.StringLiteral());
        Assertions.assertNull(l.arrayLiteral());

        Assertions.assertEquals(value, l.IntegerLiteral().getSymbol().getText());
    }
}
