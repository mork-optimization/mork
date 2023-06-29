package es.urjc.etsii.grafo.autoconfig.antlr;// Generated from AlgorithmParser.g4 by ANTLR 4.13.0

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.ParserATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.List;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue"})
public class AlgorithmParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.13.0", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		LBRCE=1, RBRCE=2, LBRCK=3, RBRCK=4, EQ=5, COMMA=6, MINUS=7, BooleanLiteral=8, 
		NullLiteral=9, IDENT=10, WS=11, IntegerLiteral=12, FloatingPointLiteral=13, 
		CharacterLiteral=14, StringLiteral=15;
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
			null, "'{'", "'}'", "'['", "']'", "'='", "','", "'-'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "LBRCE", "RBRCE", "LBRCK", "RBRCK", "EQ", "COMMA", "MINUS", "BooleanLiteral", 
			"NullLiteral", "IDENT", "WS", "IntegerLiteral", "FloatingPointLiteral", 
			"CharacterLiteral", "StringLiteral"
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

	@SuppressWarnings("CheckReturnValue")
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
			if ( listener instanceof AlgorithmParserListener ) ((AlgorithmParserListener)listener).enterInit(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AlgorithmParserListener ) ((AlgorithmParserListener)listener).exitInit(this);
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

	@SuppressWarnings("CheckReturnValue")
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
			if ( listener instanceof AlgorithmParserListener ) ((AlgorithmParserListener)listener).enterComponent(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AlgorithmParserListener ) ((AlgorithmParserListener)listener).exitComponent(this);
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

	@SuppressWarnings("CheckReturnValue")
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
			if ( listener instanceof AlgorithmParserListener ) ((AlgorithmParserListener)listener).enterProperties(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AlgorithmParserListener ) ((AlgorithmParserListener)listener).exitProperties(this);
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

	@SuppressWarnings("CheckReturnValue")
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
			if ( listener instanceof AlgorithmParserListener ) ((AlgorithmParserListener)listener).enterProperty(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AlgorithmParserListener ) ((AlgorithmParserListener)listener).exitProperty(this);
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

	@SuppressWarnings("CheckReturnValue")
	public static class PropertyValueContext extends ParserRuleContext {
		public LiteralContext literal() {
			return getRuleContext(LiteralContext.class,0);
		}
		public TerminalNode LBRCE() { return getToken(AlgorithmParser.LBRCE, 0); }
		public TerminalNode RBRCE() { return getToken(AlgorithmParser.RBRCE, 0); }
		public ComponentContext component() {
			return getRuleContext(ComponentContext.class,0);
		}
		public PropertyValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_propertyValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AlgorithmParserListener ) ((AlgorithmParserListener)listener).enterPropertyValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AlgorithmParserListener ) ((AlgorithmParserListener)listener).exitPropertyValue(this);
		}
	}

	public final PropertyValueContext propertyValue() throws RecognitionException {
		PropertyValueContext _localctx = new PropertyValueContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_propertyValue);
		int _la;
		try {
			setState(41);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LBRCK:
			case BooleanLiteral:
			case NullLiteral:
			case IntegerLiteral:
			case FloatingPointLiteral:
			case CharacterLiteral:
			case StringLiteral:
				enterOuterAlt(_localctx, 1);
				{
				setState(35);
				literal();
				setState(38);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LBRCE) {
					{
					setState(36);
					match(LBRCE);
					setState(37);
					match(RBRCE);
					}
				}

				}
				break;
			case IDENT:
				enterOuterAlt(_localctx, 2);
				{
				setState(40);
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

	@SuppressWarnings("CheckReturnValue")
	public static class LiteralContext extends ParserRuleContext {
		public TerminalNode NullLiteral() { return getToken(AlgorithmParser.NullLiteral, 0); }
		public TerminalNode BooleanLiteral() { return getToken(AlgorithmParser.BooleanLiteral, 0); }
		public TerminalNode FloatingPointLiteral() { return getToken(AlgorithmParser.FloatingPointLiteral, 0); }
		public TerminalNode IntegerLiteral() { return getToken(AlgorithmParser.IntegerLiteral, 0); }
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
			if ( listener instanceof AlgorithmParserListener ) ((AlgorithmParserListener)listener).enterLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AlgorithmParserListener ) ((AlgorithmParserListener)listener).exitLiteral(this);
		}
	}

	public final LiteralContext literal() throws RecognitionException {
		LiteralContext _localctx = new LiteralContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_literal);
		try {
			setState(50);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NullLiteral:
				enterOuterAlt(_localctx, 1);
				{
				setState(43);
				match(NullLiteral);
				}
				break;
			case BooleanLiteral:
				enterOuterAlt(_localctx, 2);
				{
				setState(44);
				match(BooleanLiteral);
				}
				break;
			case FloatingPointLiteral:
				enterOuterAlt(_localctx, 3);
				{
				setState(45);
				match(FloatingPointLiteral);
				}
				break;
			case IntegerLiteral:
				enterOuterAlt(_localctx, 4);
				{
				setState(46);
				match(IntegerLiteral);
				}
				break;
			case StringLiteral:
				enterOuterAlt(_localctx, 5);
				{
				setState(47);
				match(StringLiteral);
				}
				break;
			case CharacterLiteral:
				enterOuterAlt(_localctx, 6);
				{
				setState(48);
				match(CharacterLiteral);
				}
				break;
			case LBRCK:
				enterOuterAlt(_localctx, 7);
				{
				setState(49);
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

	@SuppressWarnings("CheckReturnValue")
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
			if ( listener instanceof AlgorithmParserListener ) ((AlgorithmParserListener)listener).enterArrayLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AlgorithmParserListener ) ((AlgorithmParserListener)listener).exitArrayLiteral(this);
		}
	}

	public final ArrayLiteralContext arrayLiteral() throws RecognitionException {
		ArrayLiteralContext _localctx = new ArrayLiteralContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_arrayLiteral);
		int _la;
		try {
			setState(104);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,10,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(52);
				match(LBRCK);
				setState(53);
				match(RBRCK);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(54);
				match(LBRCK);
				setState(55);
				match(IntegerLiteral);
				setState(60);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(56);
					match(COMMA);
					setState(57);
					match(IntegerLiteral);
					}
					}
					setState(62);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(63);
				match(RBRCK);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(64);
				match(LBRCK);
				setState(65);
				match(FloatingPointLiteral);
				setState(70);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(66);
					match(COMMA);
					setState(67);
					match(FloatingPointLiteral);
					}
					}
					setState(72);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(73);
				match(RBRCK);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(74);
				match(LBRCK);
				setState(75);
				match(BooleanLiteral);
				setState(80);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(76);
					match(COMMA);
					setState(77);
					match(BooleanLiteral);
					}
					}
					setState(82);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(83);
				match(RBRCK);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(84);
				match(LBRCK);
				setState(85);
				match(CharacterLiteral);
				setState(90);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(86);
					match(COMMA);
					setState(87);
					match(CharacterLiteral);
					}
					}
					setState(92);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(93);
				match(RBRCK);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(94);
				match(LBRCK);
				setState(95);
				match(StringLiteral);
				setState(100);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(96);
					match(COMMA);
					setState(97);
					match(StringLiteral);
					}
					}
					setState(102);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(103);
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
		"\u0004\u0001\u000fk\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0001\u0000\u0001\u0000\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0003\u0001\u0014\b\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0002\u0001\u0002\u0001\u0002\u0005\u0002\u001b\b\u0002\n"+
		"\u0002\f\u0002\u001e\t\u0002\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0004\u0001\u0004\u0001\u0004\u0003\u0004\'\b\u0004\u0001"+
		"\u0004\u0003\u0004*\b\u0004\u0001\u0005\u0001\u0005\u0001\u0005\u0001"+
		"\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0003\u00053\b\u0005\u0001"+
		"\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0005"+
		"\u0006;\b\u0006\n\u0006\f\u0006>\t\u0006\u0001\u0006\u0001\u0006\u0001"+
		"\u0006\u0001\u0006\u0001\u0006\u0005\u0006E\b\u0006\n\u0006\f\u0006H\t"+
		"\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0005"+
		"\u0006O\b\u0006\n\u0006\f\u0006R\t\u0006\u0001\u0006\u0001\u0006\u0001"+
		"\u0006\u0001\u0006\u0001\u0006\u0005\u0006Y\b\u0006\n\u0006\f\u0006\\"+
		"\t\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0005"+
		"\u0006c\b\u0006\n\u0006\f\u0006f\t\u0006\u0001\u0006\u0003\u0006i\b\u0006"+
		"\u0001\u0006\u0000\u0000\u0007\u0000\u0002\u0004\u0006\b\n\f\u0000\u0000"+
		"w\u0000\u000e\u0001\u0000\u0000\u0000\u0002\u0010\u0001\u0000\u0000\u0000"+
		"\u0004\u0017\u0001\u0000\u0000\u0000\u0006\u001f\u0001\u0000\u0000\u0000"+
		"\b)\u0001\u0000\u0000\u0000\n2\u0001\u0000\u0000\u0000\fh\u0001\u0000"+
		"\u0000\u0000\u000e\u000f\u0003\u0002\u0001\u0000\u000f\u0001\u0001\u0000"+
		"\u0000\u0000\u0010\u0011\u0005\n\u0000\u0000\u0011\u0013\u0005\u0001\u0000"+
		"\u0000\u0012\u0014\u0003\u0004\u0002\u0000\u0013\u0012\u0001\u0000\u0000"+
		"\u0000\u0013\u0014\u0001\u0000\u0000\u0000\u0014\u0015\u0001\u0000\u0000"+
		"\u0000\u0015\u0016\u0005\u0002\u0000\u0000\u0016\u0003\u0001\u0000\u0000"+
		"\u0000\u0017\u001c\u0003\u0006\u0003\u0000\u0018\u0019\u0005\u0006\u0000"+
		"\u0000\u0019\u001b\u0003\u0006\u0003\u0000\u001a\u0018\u0001\u0000\u0000"+
		"\u0000\u001b\u001e\u0001\u0000\u0000\u0000\u001c\u001a\u0001\u0000\u0000"+
		"\u0000\u001c\u001d\u0001\u0000\u0000\u0000\u001d\u0005\u0001\u0000\u0000"+
		"\u0000\u001e\u001c\u0001\u0000\u0000\u0000\u001f \u0005\n\u0000\u0000"+
		" !\u0005\u0005\u0000\u0000!\"\u0003\b\u0004\u0000\"\u0007\u0001\u0000"+
		"\u0000\u0000#&\u0003\n\u0005\u0000$%\u0005\u0001\u0000\u0000%\'\u0005"+
		"\u0002\u0000\u0000&$\u0001\u0000\u0000\u0000&\'\u0001\u0000\u0000\u0000"+
		"\'*\u0001\u0000\u0000\u0000(*\u0003\u0002\u0001\u0000)#\u0001\u0000\u0000"+
		"\u0000)(\u0001\u0000\u0000\u0000*\t\u0001\u0000\u0000\u0000+3\u0005\t"+
		"\u0000\u0000,3\u0005\b\u0000\u0000-3\u0005\r\u0000\u0000.3\u0005\f\u0000"+
		"\u0000/3\u0005\u000f\u0000\u000003\u0005\u000e\u0000\u000013\u0003\f\u0006"+
		"\u00002+\u0001\u0000\u0000\u00002,\u0001\u0000\u0000\u00002-\u0001\u0000"+
		"\u0000\u00002.\u0001\u0000\u0000\u00002/\u0001\u0000\u0000\u000020\u0001"+
		"\u0000\u0000\u000021\u0001\u0000\u0000\u00003\u000b\u0001\u0000\u0000"+
		"\u000045\u0005\u0003\u0000\u00005i\u0005\u0004\u0000\u000067\u0005\u0003"+
		"\u0000\u00007<\u0005\f\u0000\u000089\u0005\u0006\u0000\u00009;\u0005\f"+
		"\u0000\u0000:8\u0001\u0000\u0000\u0000;>\u0001\u0000\u0000\u0000<:\u0001"+
		"\u0000\u0000\u0000<=\u0001\u0000\u0000\u0000=?\u0001\u0000\u0000\u0000"+
		"><\u0001\u0000\u0000\u0000?i\u0005\u0004\u0000\u0000@A\u0005\u0003\u0000"+
		"\u0000AF\u0005\r\u0000\u0000BC\u0005\u0006\u0000\u0000CE\u0005\r\u0000"+
		"\u0000DB\u0001\u0000\u0000\u0000EH\u0001\u0000\u0000\u0000FD\u0001\u0000"+
		"\u0000\u0000FG\u0001\u0000\u0000\u0000GI\u0001\u0000\u0000\u0000HF\u0001"+
		"\u0000\u0000\u0000Ii\u0005\u0004\u0000\u0000JK\u0005\u0003\u0000\u0000"+
		"KP\u0005\b\u0000\u0000LM\u0005\u0006\u0000\u0000MO\u0005\b\u0000\u0000"+
		"NL\u0001\u0000\u0000\u0000OR\u0001\u0000\u0000\u0000PN\u0001\u0000\u0000"+
		"\u0000PQ\u0001\u0000\u0000\u0000QS\u0001\u0000\u0000\u0000RP\u0001\u0000"+
		"\u0000\u0000Si\u0005\u0004\u0000\u0000TU\u0005\u0003\u0000\u0000UZ\u0005"+
		"\u000e\u0000\u0000VW\u0005\u0006\u0000\u0000WY\u0005\u000e\u0000\u0000"+
		"XV\u0001\u0000\u0000\u0000Y\\\u0001\u0000\u0000\u0000ZX\u0001\u0000\u0000"+
		"\u0000Z[\u0001\u0000\u0000\u0000[]\u0001\u0000\u0000\u0000\\Z\u0001\u0000"+
		"\u0000\u0000]i\u0005\u0004\u0000\u0000^_\u0005\u0003\u0000\u0000_d\u0005"+
		"\u000f\u0000\u0000`a\u0005\u0006\u0000\u0000ac\u0005\u000f\u0000\u0000"+
		"b`\u0001\u0000\u0000\u0000cf\u0001\u0000\u0000\u0000db\u0001\u0000\u0000"+
		"\u0000de\u0001\u0000\u0000\u0000eg\u0001\u0000\u0000\u0000fd\u0001\u0000"+
		"\u0000\u0000gi\u0005\u0004\u0000\u0000h4\u0001\u0000\u0000\u0000h6\u0001"+
		"\u0000\u0000\u0000h@\u0001\u0000\u0000\u0000hJ\u0001\u0000\u0000\u0000"+
		"hT\u0001\u0000\u0000\u0000h^\u0001\u0000\u0000\u0000i\r\u0001\u0000\u0000"+
		"\u0000\u000b\u0013\u001c&)2<FPZdh";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}