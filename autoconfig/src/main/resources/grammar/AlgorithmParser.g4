// Example: MULTISTART{iter=100, alg=SANNEAL{initialTemp=400, constructive=GRASP{alpha=0.5}}}
parser grammar AlgorithmParser;
options
{
   tokenVocab = AlgorithmLexer;
}

init: component;

component: IDENT LBRCE properties? RBRCE;

properties: property (COMMA property)*;

property: IDENT EQ propertyValue;

// Allow optional {} after literal values to simplify algorithm reconstruction
propertyValue: literal (LBRCE RBRCE)? | component | arrayLiteral;

literal: NullLiteral | BooleanLiteral | FloatingPointLiteral | IntegerLiteral | StringLiteral | CharacterLiteral;


arrayLiteral: LBRCK (propertyValue (COMMA propertyValue)*)? RBRCK;
