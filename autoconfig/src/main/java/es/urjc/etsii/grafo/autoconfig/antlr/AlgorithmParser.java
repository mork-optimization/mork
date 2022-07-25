package es.urjc.etsii.grafo.autoconfig.antlr;// Generated from resources/grammar/AlgorithmParser.g4 by ANTLR 4.10.1
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class AlgorithmParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.10.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		LBRCE=1, RBRCE=2, LBRCK=3, RBRCK=4, EQ=5, COMMA=6, IDENT=7, WS=8, IntegerLiteral=9, 
		FloatingPointLiteral=10, BooleanLiteral=11, CharacterLiteral=12, StringLiteral=13, 
		NullLiteral=14;
	public static final int
		RULE_init = 0, RULE_component = 1, RULE_properties = 2, RULE_property = 3, 
		RULE_propertyValue = 4, RULE_literal = 5, RULE_arrayLiteral = 6;
	private static String[] makeRuleNames() {
		return new String[] {
			"init", "component", "properties", "property", "propertyValue", "literal", 
			"arrayLiteral"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'{'", "'}'", "'['", "']'", "'='", "','", null, null, null, null, 
			null, null, null, "'null'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "LBRCE", "RBRCE", "LBRCK", "RBRCK", "EQ", "COMMA", "IDENT", "WS", 
			"IntegerLiteral", "FloatingPointLiteral", "BooleanLiteral", "CharacterLiteral", 
			"StringLiteral", "NullLiteral"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "AlgorithmParser.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public AlgorithmParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class InitContext extends ParserRuleContext {
		public ComponentContext component() {
			return getRuleContext(ComponentContext.class,0);
		}
		public InitContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_init; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AlgorithmParserListener) ((AlgorithmParserListener)listener).enterInit(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AlgorithmParserListener) ((AlgorithmParserListener)listener).exitInit(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AlgorithmParserVisitor) return ((AlgorithmParserVisitor<? extends T>)visitor).visitInit(this);
			else return visitor.visitChildren(this);
		}
	}

	public final InitContext init() throws RecognitionException {
		InitContext _localctx = new InitContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_init);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(14);
			component();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ComponentContext extends ParserRuleContext {
		public TerminalNode IDENT() { return getToken(AlgorithmParser.IDENT, 0); }
		public TerminalNode LBRCE() { return getToken(AlgorithmParser.LBRCE, 0); }
		public TerminalNode RBRCE() { return getToken(AlgorithmParser.RBRCE, 0); }
		public PropertiesContext properties() {
			return getRuleContext(PropertiesContext.class,0);
		}
		public ComponentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_component; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AlgorithmParserListener) ((AlgorithmParserListener)listener).enterComponent(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AlgorithmParserListener) ((AlgorithmParserListener)listener).exitComponent(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AlgorithmParserVisitor) return ((AlgorithmParserVisitor<? extends T>)visitor).visitComponent(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ComponentContext component() throws RecognitionException {
		ComponentContext _localctx = new ComponentContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_component);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(16);
			match(IDENT);
			setState(17);
			match(LBRCE);
			setState(19);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==IDENT) {
				{
				setState(18);
				properties();
				}
			}

			setState(21);
			match(RBRCE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PropertiesContext extends ParserRuleContext {
		public List<PropertyContext> property() {
			return getRuleContexts(PropertyContext.class);
		}
		public PropertyContext property(int i) {
			return getRuleContext(PropertyContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(AlgorithmParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(AlgorithmParser.COMMA, i);
		}
		public PropertiesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_properties; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AlgorithmParserListener) ((AlgorithmParserListener)listener).enterProperties(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AlgorithmParserListener) ((AlgorithmParserListener)listener).exitProperties(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AlgorithmParserVisitor) return ((AlgorithmParserVisitor<? extends T>)visitor).visitProperties(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PropertiesContext properties() throws RecognitionException {
		PropertiesContext _localctx = new PropertiesContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_properties);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(23);
			property();
			setState(28);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(24);
				match(COMMA);
				setState(25);
				property();
				}
				}
				setState(30);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PropertyContext extends ParserRuleContext {
		public TerminalNode IDENT() { return getToken(AlgorithmParser.IDENT, 0); }
		public TerminalNode EQ() { return getToken(AlgorithmParser.EQ, 0); }
		public PropertyValueContext propertyValue() {
			return getRuleContext(PropertyValueContext.class,0);
		}
		public PropertyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_property; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AlgorithmParserListener) ((AlgorithmParserListener)listener).enterProperty(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AlgorithmParserListener) ((AlgorithmParserListener)listener).exitProperty(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AlgorithmParserVisitor) return ((AlgorithmParserVisitor<? extends T>)visitor).visitProperty(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PropertyContext property() throws RecognitionException {
		PropertyContext _localctx = new PropertyContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_property);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(31);
			match(IDENT);
			setState(32);
			match(EQ);
			setState(33);
			propertyValue();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PropertyValueContext extends ParserRuleContext {
		public LiteralContext literal() {
			return getRuleContext(LiteralContext.class,0);
		}
		public ComponentContext component() {
			return getRuleContext(ComponentContext.class,0);
		}
		public PropertyValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_propertyValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AlgorithmParserListener) ((AlgorithmParserListener)listener).enterPropertyValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AlgorithmParserListener) ((AlgorithmParserListener)listener).exitPropertyValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AlgorithmParserVisitor) return ((AlgorithmParserVisitor<? extends T>)visitor).visitPropertyValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PropertyValueContext propertyValue() throws RecognitionException {
		PropertyValueContext _localctx = new PropertyValueContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_propertyValue);
		try {
			setState(37);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LBRCK:
			case IntegerLiteral:
			case FloatingPointLiteral:
			case BooleanLiteral:
			case CharacterLiteral:
			case StringLiteral:
			case NullLiteral:
				enterOuterAlt(_localctx, 1);
				{
				setState(35);
				literal();
				}
				break;
			case IDENT:
				enterOuterAlt(_localctx, 2);
				{
				setState(36);
				component();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class LiteralContext extends ParserRuleContext {
		public TerminalNode FloatingPointLiteral() { return getToken(AlgorithmParser.FloatingPointLiteral, 0); }
		public TerminalNode IntegerLiteral() { return getToken(AlgorithmParser.IntegerLiteral, 0); }
		public TerminalNode BooleanLiteral() { return getToken(AlgorithmParser.BooleanLiteral, 0); }
		public TerminalNode NullLiteral() { return getToken(AlgorithmParser.NullLiteral, 0); }
		public TerminalNode StringLiteral() { return getToken(AlgorithmParser.StringLiteral, 0); }
		public TerminalNode CharacterLiteral() { return getToken(AlgorithmParser.CharacterLiteral, 0); }
		public ArrayLiteralContext arrayLiteral() {
			return getRuleContext(ArrayLiteralContext.class,0);
		}
		public LiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_literal; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AlgorithmParserListener) ((AlgorithmParserListener)listener).enterLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AlgorithmParserListener) ((AlgorithmParserListener)listener).exitLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AlgorithmParserVisitor) return ((AlgorithmParserVisitor<? extends T>)visitor).visitLiteral(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LiteralContext literal() throws RecognitionException {
		LiteralContext _localctx = new LiteralContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_literal);
		try {
			setState(46);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case FloatingPointLiteral:
				enterOuterAlt(_localctx, 1);
				{
				setState(39);
				match(FloatingPointLiteral);
				}
				break;
			case IntegerLiteral:
				enterOuterAlt(_localctx, 2);
				{
				setState(40);
				match(IntegerLiteral);
				}
				break;
			case BooleanLiteral:
				enterOuterAlt(_localctx, 3);
				{
				setState(41);
				match(BooleanLiteral);
				}
				break;
			case NullLiteral:
				enterOuterAlt(_localctx, 4);
				{
				setState(42);
				match(NullLiteral);
				}
				break;
			case StringLiteral:
				enterOuterAlt(_localctx, 5);
				{
				setState(43);
				match(StringLiteral);
				}
				break;
			case CharacterLiteral:
				enterOuterAlt(_localctx, 6);
				{
				setState(44);
				match(CharacterLiteral);
				}
				break;
			case LBRCK:
				enterOuterAlt(_localctx, 7);
				{
				setState(45);
				arrayLiteral();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ArrayLiteralContext extends ParserRuleContext {
		public TerminalNode LBRCK() { return getToken(AlgorithmParser.LBRCK, 0); }
		public TerminalNode RBRCK() { return getToken(AlgorithmParser.RBRCK, 0); }
		public List<TerminalNode> IntegerLiteral() { return getTokens(AlgorithmParser.IntegerLiteral); }
		public TerminalNode IntegerLiteral(int i) {
			return getToken(AlgorithmParser.IntegerLiteral, i);
		}
		public List<TerminalNode> COMMA() { return getTokens(AlgorithmParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(AlgorithmParser.COMMA, i);
		}
		public List<TerminalNode> FloatingPointLiteral() { return getTokens(AlgorithmParser.FloatingPointLiteral); }
		public TerminalNode FloatingPointLiteral(int i) {
			return getToken(AlgorithmParser.FloatingPointLiteral, i);
		}
		public List<TerminalNode> BooleanLiteral() { return getTokens(AlgorithmParser.BooleanLiteral); }
		public TerminalNode BooleanLiteral(int i) {
			return getToken(AlgorithmParser.BooleanLiteral, i);
		}
		public List<TerminalNode> CharacterLiteral() { return getTokens(AlgorithmParser.CharacterLiteral); }
		public TerminalNode CharacterLiteral(int i) {
			return getToken(AlgorithmParser.CharacterLiteral, i);
		}
		public List<TerminalNode> StringLiteral() { return getTokens(AlgorithmParser.StringLiteral); }
		public TerminalNode StringLiteral(int i) {
			return getToken(AlgorithmParser.StringLiteral, i);
		}
		public ArrayLiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_arrayLiteral; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AlgorithmParserListener) ((AlgorithmParserListener)listener).enterArrayLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AlgorithmParserListener) ((AlgorithmParserListener)listener).exitArrayLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AlgorithmParserVisitor) return ((AlgorithmParserVisitor<? extends T>)visitor).visitArrayLiteral(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ArrayLiteralContext arrayLiteral() throws RecognitionException {
		ArrayLiteralContext _localctx = new ArrayLiteralContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_arrayLiteral);
		int _la;
		try {
			setState(100);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,9,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(48);
				match(LBRCK);
				setState(49);
				match(RBRCK);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(50);
				match(LBRCK);
				setState(51);
				match(IntegerLiteral);
				setState(56);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(52);
					match(COMMA);
					setState(53);
					match(IntegerLiteral);
					}
					}
					setState(58);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(59);
				match(RBRCK);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(60);
				match(LBRCK);
				setState(61);
				match(FloatingPointLiteral);
				setState(66);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(62);
					match(COMMA);
					setState(63);
					match(FloatingPointLiteral);
					}
					}
					setState(68);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(69);
				match(RBRCK);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(70);
				match(LBRCK);
				setState(71);
				match(BooleanLiteral);
				setState(76);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(72);
					match(COMMA);
					setState(73);
					match(BooleanLiteral);
					}
					}
					setState(78);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(79);
				match(RBRCK);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(80);
				match(LBRCK);
				setState(81);
				match(CharacterLiteral);
				setState(86);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(82);
					match(COMMA);
					setState(83);
					match(CharacterLiteral);
					}
					}
					setState(88);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(89);
				match(RBRCK);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(90);
				match(LBRCK);
				setState(91);
				match(StringLiteral);
				setState(96);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(92);
					match(COMMA);
					setState(93);
					match(StringLiteral);
					}
					}
					setState(98);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(99);
				match(RBRCK);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\u0004\u0001\u000eg\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0001\u0000\u0001\u0000\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0003\u0001\u0014\b\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0002\u0001\u0002\u0001\u0002\u0005\u0002\u001b\b\u0002\n"+
		"\u0002\f\u0002\u001e\t\u0002\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0004\u0001\u0004\u0003\u0004&\b\u0004\u0001\u0005\u0001"+
		"\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0003"+
		"\u0005/\b\u0005\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001"+
		"\u0006\u0001\u0006\u0005\u00067\b\u0006\n\u0006\f\u0006:\t\u0006\u0001"+
		"\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0005\u0006A\b"+
		"\u0006\n\u0006\f\u0006D\t\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001"+
		"\u0006\u0001\u0006\u0005\u0006K\b\u0006\n\u0006\f\u0006N\t\u0006\u0001"+
		"\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0005\u0006U\b"+
		"\u0006\n\u0006\f\u0006X\t\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001"+
		"\u0006\u0001\u0006\u0005\u0006_\b\u0006\n\u0006\f\u0006b\t\u0006\u0001"+
		"\u0006\u0003\u0006e\b\u0006\u0001\u0006\u0000\u0000\u0007\u0000\u0002"+
		"\u0004\u0006\b\n\f\u0000\u0000r\u0000\u000e\u0001\u0000\u0000\u0000\u0002"+
		"\u0010\u0001\u0000\u0000\u0000\u0004\u0017\u0001\u0000\u0000\u0000\u0006"+
		"\u001f\u0001\u0000\u0000\u0000\b%\u0001\u0000\u0000\u0000\n.\u0001\u0000"+
		"\u0000\u0000\fd\u0001\u0000\u0000\u0000\u000e\u000f\u0003\u0002\u0001"+
		"\u0000\u000f\u0001\u0001\u0000\u0000\u0000\u0010\u0011\u0005\u0007\u0000"+
		"\u0000\u0011\u0013\u0005\u0001\u0000\u0000\u0012\u0014\u0003\u0004\u0002"+
		"\u0000\u0013\u0012\u0001\u0000\u0000\u0000\u0013\u0014\u0001\u0000\u0000"+
		"\u0000\u0014\u0015\u0001\u0000\u0000\u0000\u0015\u0016\u0005\u0002\u0000"+
		"\u0000\u0016\u0003\u0001\u0000\u0000\u0000\u0017\u001c\u0003\u0006\u0003"+
		"\u0000\u0018\u0019\u0005\u0006\u0000\u0000\u0019\u001b\u0003\u0006\u0003"+
		"\u0000\u001a\u0018\u0001\u0000\u0000\u0000\u001b\u001e\u0001\u0000\u0000"+
		"\u0000\u001c\u001a\u0001\u0000\u0000\u0000\u001c\u001d\u0001\u0000\u0000"+
		"\u0000\u001d\u0005\u0001\u0000\u0000\u0000\u001e\u001c\u0001\u0000\u0000"+
		"\u0000\u001f \u0005\u0007\u0000\u0000 !\u0005\u0005\u0000\u0000!\"\u0003"+
		"\b\u0004\u0000\"\u0007\u0001\u0000\u0000\u0000#&\u0003\n\u0005\u0000$"+
		"&\u0003\u0002\u0001\u0000%#\u0001\u0000\u0000\u0000%$\u0001\u0000\u0000"+
		"\u0000&\t\u0001\u0000\u0000\u0000\'/\u0005\n\u0000\u0000(/\u0005\t\u0000"+
		"\u0000)/\u0005\u000b\u0000\u0000*/\u0005\u000e\u0000\u0000+/\u0005\r\u0000"+
		"\u0000,/\u0005\f\u0000\u0000-/\u0003\f\u0006\u0000.\'\u0001\u0000\u0000"+
		"\u0000.(\u0001\u0000\u0000\u0000.)\u0001\u0000\u0000\u0000.*\u0001\u0000"+
		"\u0000\u0000.+\u0001\u0000\u0000\u0000.,\u0001\u0000\u0000\u0000.-\u0001"+
		"\u0000\u0000\u0000/\u000b\u0001\u0000\u0000\u000001\u0005\u0003\u0000"+
		"\u00001e\u0005\u0004\u0000\u000023\u0005\u0003\u0000\u000038\u0005\t\u0000"+
		"\u000045\u0005\u0006\u0000\u000057\u0005\t\u0000\u000064\u0001\u0000\u0000"+
		"\u00007:\u0001\u0000\u0000\u000086\u0001\u0000\u0000\u000089\u0001\u0000"+
		"\u0000\u00009;\u0001\u0000\u0000\u0000:8\u0001\u0000\u0000\u0000;e\u0005"+
		"\u0004\u0000\u0000<=\u0005\u0003\u0000\u0000=B\u0005\n\u0000\u0000>?\u0005"+
		"\u0006\u0000\u0000?A\u0005\n\u0000\u0000@>\u0001\u0000\u0000\u0000AD\u0001"+
		"\u0000\u0000\u0000B@\u0001\u0000\u0000\u0000BC\u0001\u0000\u0000\u0000"+
		"CE\u0001\u0000\u0000\u0000DB\u0001\u0000\u0000\u0000Ee\u0005\u0004\u0000"+
		"\u0000FG\u0005\u0003\u0000\u0000GL\u0005\u000b\u0000\u0000HI\u0005\u0006"+
		"\u0000\u0000IK\u0005\u000b\u0000\u0000JH\u0001\u0000\u0000\u0000KN\u0001"+
		"\u0000\u0000\u0000LJ\u0001\u0000\u0000\u0000LM\u0001\u0000\u0000\u0000"+
		"MO\u0001\u0000\u0000\u0000NL\u0001\u0000\u0000\u0000Oe\u0005\u0004\u0000"+
		"\u0000PQ\u0005\u0003\u0000\u0000QV\u0005\f\u0000\u0000RS\u0005\u0006\u0000"+
		"\u0000SU\u0005\f\u0000\u0000TR\u0001\u0000\u0000\u0000UX\u0001\u0000\u0000"+
		"\u0000VT\u0001\u0000\u0000\u0000VW\u0001\u0000\u0000\u0000WY\u0001\u0000"+
		"\u0000\u0000XV\u0001\u0000\u0000\u0000Ye\u0005\u0004\u0000\u0000Z[\u0005"+
		"\u0003\u0000\u0000[`\u0005\r\u0000\u0000\\]\u0005\u0006\u0000\u0000]_"+
		"\u0005\r\u0000\u0000^\\\u0001\u0000\u0000\u0000_b\u0001\u0000\u0000\u0000"+
		"`^\u0001\u0000\u0000\u0000`a\u0001\u0000\u0000\u0000ac\u0001\u0000\u0000"+
		"\u0000b`\u0001\u0000\u0000\u0000ce\u0005\u0004\u0000\u0000d0\u0001\u0000"+
		"\u0000\u0000d2\u0001\u0000\u0000\u0000d<\u0001\u0000\u0000\u0000dF\u0001"+
		"\u0000\u0000\u0000dP\u0001\u0000\u0000\u0000dZ\u0001\u0000\u0000\u0000"+
		"e\r\u0001\u0000\u0000\u0000\n\u0013\u001c%.8BLV`d";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}