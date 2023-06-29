package es.urjc.etsii.grafo.autoconfig.antlr;// Generated from AlgorithmParser.g4 by ANTLR 4.13.0
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link AlgorithmParser}.
 */
public interface AlgorithmParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link AlgorithmParser#init}.
	 * @param ctx the parse tree
	 */
	void enterInit(AlgorithmParser.InitContext ctx);
	/**
	 * Exit a parse tree produced by {@link AlgorithmParser#init}.
	 * @param ctx the parse tree
	 */
	void exitInit(AlgorithmParser.InitContext ctx);
	/**
	 * Enter a parse tree produced by {@link AlgorithmParser#component}.
	 * @param ctx the parse tree
	 */
	void enterComponent(AlgorithmParser.ComponentContext ctx);
	/**
	 * Exit a parse tree produced by {@link AlgorithmParser#component}.
	 * @param ctx the parse tree
	 */
	void exitComponent(AlgorithmParser.ComponentContext ctx);
	/**
	 * Enter a parse tree produced by {@link AlgorithmParser#properties}.
	 * @param ctx the parse tree
	 */
	void enterProperties(AlgorithmParser.PropertiesContext ctx);
	/**
	 * Exit a parse tree produced by {@link AlgorithmParser#properties}.
	 * @param ctx the parse tree
	 */
	void exitProperties(AlgorithmParser.PropertiesContext ctx);
	/**
	 * Enter a parse tree produced by {@link AlgorithmParser#property}.
	 * @param ctx the parse tree
	 */
	void enterProperty(AlgorithmParser.PropertyContext ctx);
	/**
	 * Exit a parse tree produced by {@link AlgorithmParser#property}.
	 * @param ctx the parse tree
	 */
	void exitProperty(AlgorithmParser.PropertyContext ctx);
	/**
	 * Enter a parse tree produced by {@link AlgorithmParser#propertyValue}.
	 * @param ctx the parse tree
	 */
	void enterPropertyValue(AlgorithmParser.PropertyValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link AlgorithmParser#propertyValue}.
	 * @param ctx the parse tree
	 */
	void exitPropertyValue(AlgorithmParser.PropertyValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link AlgorithmParser#literal}.
	 * @param ctx the parse tree
	 */
	void enterLiteral(AlgorithmParser.LiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link AlgorithmParser#literal}.
	 * @param ctx the parse tree
	 */
	void exitLiteral(AlgorithmParser.LiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link AlgorithmParser#arrayLiteral}.
	 * @param ctx the parse tree
	 */
	void enterArrayLiteral(AlgorithmParser.ArrayLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link AlgorithmParser#arrayLiteral}.
	 * @param ctx the parse tree
	 */
	void exitArrayLiteral(AlgorithmParser.ArrayLiteralContext ctx);
}