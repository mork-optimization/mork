package es.urjc.etsii.grafo.autoconfig.antlr;// Generated from resources/grammar/AlgorithmParser.g4 by ANTLR 4.10.1
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link AlgorithmParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface AlgorithmParserVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link AlgorithmParser#init}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInit(AlgorithmParser.InitContext ctx);
	/**
	 * Visit a parse tree produced by {@link AlgorithmParser#component}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComponent(AlgorithmParser.ComponentContext ctx);
	/**
	 * Visit a parse tree produced by {@link AlgorithmParser#properties}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProperties(AlgorithmParser.PropertiesContext ctx);
	/**
	 * Visit a parse tree produced by {@link AlgorithmParser#property}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProperty(AlgorithmParser.PropertyContext ctx);
	/**
	 * Visit a parse tree produced by {@link AlgorithmParser#propertyValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPropertyValue(AlgorithmParser.PropertyValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link AlgorithmParser#literal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLiteral(AlgorithmParser.LiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link AlgorithmParser#arrayLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArrayLiteral(AlgorithmParser.ArrayLiteralContext ctx);
}