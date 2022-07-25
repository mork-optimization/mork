//package es.urjc.etsii.grafo.autoconfig;
//
//import es.urjc.etsii.grafo.autoconfig.antlr.AlgorithmParser;
//import es.urjc.etsii.grafo.autoconfig.antlr.AlgorithmParserBaseVisitor;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Objects;
//
//public class AlgorithmVisitor extends AlgorithmParserBaseVisitor<Map<String, Object>> {
//    private static final Map<String, Object> EMPTY = Map.of();
//
//    @Override
//    protected Map<String, Object> defaultResult() {
//        return EMPTY;
//    }
//
//    @Override
//    protected Map<String, Object> aggregateResult(Map<String, Object> aggregate, Map<String, Object> nextResult) {
//        var map = new HashMap<String, Object>(aggregate.size() + nextResult.size());
//        map.putAll(aggregate);
//        map.putAll(nextResult);
//        return map;
//    }
//
//    @Override
//    public Map<String, Object> visitProperty(AlgorithmParser.PropertyContext ctx) {
//        var visited = super.visitProperty(ctx);
//        String ident = ctx.IDENT().getSymbol().getText();
//    }
//
//    @Override
//    public Map<String, Object> visitLiteral(AlgorithmParser.LiteralContext ctx) {
//        if(ctx.arrayLiteral().)
//        return Map.of("value", Objects.requireNonNull(value));
//        //return super.visitLiteral(ctx);
//    }
//
//    public Object getValue(AlgorithmParser.LiteralContext ctx){
//        if(ctx.BooleanLiteral() != null){
//            return Boolean.valueOf(ctx.BooleanLiteral().getText());
//        }
//        if(ctx.CharacterLiteral() != null){
//            return ctx.CharacterLiteral().getText();
//        }
//        if(ctx.IntegerLiteral() != null){
//            return Integer.parseInt(ctx.IntegerLiteral().getText());
//        }
//        if(ctx.FloatingPointLiteral() != null){
//            return Double.parseDouble(ctx.FloatingPointLiteral().getText());
//        }
//        if(ctx.StringLiteral() != null){
//            return ctx.StringLiteral().getText();
//        }
//        if(ctx.NullLiteral() != null){
//            return null;
//        }
//        throw new UnsupportedOperationException("failed to process LiteralContext" + ctx);
//    }
//
//}
