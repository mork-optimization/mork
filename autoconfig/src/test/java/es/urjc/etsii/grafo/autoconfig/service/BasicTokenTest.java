package es.urjc.etsii.grafo.autoconfig.service;

import es.urjc.etsii.grafo.autoconfig.builder.AlgorithmBuilderService;
import es.urjc.etsii.grafo.autoconfig.exception.AlgorithmParsingException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class BasicTokenTest {

    @ParameterizedTest
    @ValueSource(strings = {"1.234", "0.0", "-145.93e10", "5.1e10"})
    void basicParseFloat(String value){
        var parser = AlgorithmBuilderService.getParser(value);
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

    @ParameterizedTest
    @ValueSource(strings = {"-94", "0", "12395"})
    void basicParseInteger(String value){
        var parser = AlgorithmBuilderService.getParser(value);
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


    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void basicParseBoolean(String value){
        var parser = AlgorithmBuilderService.getParser(value);
        var ctx = parser.propertyValue();
        Assertions.assertNull(ctx.component());
        var l = ctx.literal();

        Assertions.assertNotNull(l);
        Assertions.assertNull(l.NullLiteral());
        Assertions.assertNull(l.CharacterLiteral());
        Assertions.assertNull(l.FloatingPointLiteral());
        Assertions.assertNull(l.IntegerLiteral());
        Assertions.assertNull(l.StringLiteral());
        Assertions.assertNull(l.arrayLiteral());
        Assertions.assertNotNull(l.BooleanLiteral());

        Assertions.assertEquals(value, l.BooleanLiteral().getSymbol().getText());
    }

    @ParameterizedTest
    @ValueSource(strings = {"\"\"", "\"a\"", "\"long string with spaces\"", "\"\\u0020\""})
    void basicParseString(String value){
        var parser = AlgorithmBuilderService.getParser(value);
        var ctx = parser.propertyValue();
        Assertions.assertNull(ctx.component());
        var l = ctx.literal();

        Assertions.assertNotNull(l);
        Assertions.assertNull(l.NullLiteral());
        Assertions.assertNull(l.BooleanLiteral());
        Assertions.assertNull(l.CharacterLiteral());
        Assertions.assertNull(l.FloatingPointLiteral());
        Assertions.assertNull(l.IntegerLiteral());
        Assertions.assertNotNull(l.StringLiteral());
        Assertions.assertNull(l.arrayLiteral());

        Assertions.assertEquals(value, l.StringLiteral().getSymbol().getText());
    }

    @ParameterizedTest
    @ValueSource(strings = {"'\u0020'", "'a'", "'1'", "'\\\\'"})
    void basicParseCharacter(String value){
        var parser = AlgorithmBuilderService.getParser(value);
        var ctx = parser.propertyValue();
        Assertions.assertNull(ctx.component());
        var l = ctx.literal();

        Assertions.assertNotNull(l);
        Assertions.assertNull(l.NullLiteral());
        Assertions.assertNull(l.BooleanLiteral());
        Assertions.assertNotNull(l.CharacterLiteral());
        Assertions.assertNull(l.FloatingPointLiteral());
        Assertions.assertNull(l.IntegerLiteral());
        Assertions.assertNull(l.StringLiteral());
        Assertions.assertNull(l.arrayLiteral());

        Assertions.assertEquals(value, l.CharacterLiteral().getSymbol().getText());
    }

    @Test
    void basicParseNull(){
        String value = "null";
        var parser = AlgorithmBuilderService.getParser(value);
        var ctx = parser.propertyValue();
        Assertions.assertNull(ctx.component());
        var l = ctx.literal();

        Assertions.assertNotNull(l);
        Assertions.assertNotNull(l.NullLiteral());
        Assertions.assertNull(l.BooleanLiteral());
        Assertions.assertNull(l.CharacterLiteral());
        Assertions.assertNull(l.FloatingPointLiteral());
        Assertions.assertNull(l.IntegerLiteral());
        Assertions.assertNull(l.StringLiteral());
        Assertions.assertNull(l.arrayLiteral());

        Assertions.assertEquals(value, l.NullLiteral().getSymbol().getText());
    }

    @ParameterizedTest
    @ValueSource(strings = {"True", "False", "z", "whatever", "'multichar'", "'", "\"", "''", "'\\'"})
    void failBasicParse(String value){
        var parser = AlgorithmBuilderService.getParser(value);
        Assertions.assertThrows(AlgorithmParsingException.class, parser::propertyValue);
    }
}
