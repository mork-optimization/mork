// Example: MULTISTART{iter=100, alg=SANNEAL{initialTemp=400, constructive=GRASP{alpha=0.5}}}
parser grammar AlgorithmParser;
options
{
   tokenVocab = AlgorithmLexer;
}

init: component;

component: IDENT LBRCE properties? RBRCE;

properties: IDENT EQ propertyValue (COMMA IDENT EQ propertyValue)*;

propertyValue: literal | component;

literal: FloatingPointLiteral | IntegerLiteral | BooleanLiteral | NullLiteral | StringLiteral | CharacterLiteral | arrayLiteral;


// Only allow arrays as literals of the same type value
arrayLiteral:
LBRCK RBRCK |
LBRCK IntegerLiteral (COMMA IntegerLiteral)* RBRCK |
LBRCK FloatingPointLiteral (COMMA FloatingPointLiteral)* RBRCK |
LBRCK BooleanLiteral (COMMA BooleanLiteral)* RBRCK |
LBRCK CharacterLiteral (COMMA CharacterLiteral)* RBRCK |
LBRCK StringLiteral (COMMA StringLiteral)* RBRCK;
