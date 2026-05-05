grammar LycoScript;

// ========================================================================
// PARSER RULES (Syntax Definition)
// ========================================================================

/**
 * The root of our LycoScript program.
 * A program is a sequence of declarations ending with the End Of File (EOF).
 */
program: declaration* EOF;

declaration
    : constantDecl
    | variableDecl
    | functionDecl
    ;

/**
 * Constants are resolved at compile-time and map to Assembly Equates.
 * Example: const pointer SCREEN = $2000;
 */
constantDecl: 'const' type IDENTIFIER '=' expression ';';

/**
 * Variables are 8-bit or 16-bit memory locations (typically Zero Page).
 * Example: byte color = $05;
 */
variableDecl: type IDENTIFIER ('=' expression)? ';';

/**
 * Functions support parameters and a code block.
 * Example: void main() { ... }
 */
functionDecl: type IDENTIFIER '(' parameterList? ')' block;

parameterList: parameter (',' parameter)*;
parameter: type IDENTIFIER;

/**
 * Supported data types.
 * byte: 8-bit unsigned integer (0-255).
 * pointer: 16-bit memory address.
 * void: Used only for function return types.
 */
type: 'byte' | 'pointer' | 'void';

/**
 * A block of code enclosed in braces. Defines a new variable scope.
 */
block: '{' statement* '}';

statement
    : variableDecl
    | assignmentStatement
    | ifStatement
    | whileStatement
    | functionCallStatement
    | returnStatement
    | block
    ;

/**
 * Assignments handle direct variables and pointer dereferencing.
 * Direct: a = 5;
 * Pointer: SCREEN[0] = $FF;
 */
assignmentStatement
    : IDENTIFIER '=' expression ';'
    | IDENTIFIER '[' expression ']' '=' expression ';'
    ;

functionCallStatement: functionCall ';';

ifStatement: 'if' '(' expression ')' block ('else' block)?;

whileStatement: 'while' '(' expression ')' block;

returnStatement: 'return' expression? ';';

/**
 * Expressions with operator precedence.
 * ANTLR4 implicitly handles precedence based on the order of rules (top is highest).
 */
expression
    : IDENTIFIER '[' expression ']'                                  # PointerAccessExpr
    | functionCall                                                   # FunctionCallExpr
    | ('-' | '!') expression                                         # UnaryExpr
    | expression ('*' | '/') expression                              # MulDivExpr
    | expression ('+' | '-') expression                              # AddSubExpr
    | expression ('==' | '!=' | '<' | '>' | '<=' | '>=') expression  # RelationalExpr
    | expression ('&' | '|' | '^') expression                        # BitwiseExpr
    | IDENTIFIER                                                     # IdExpr
    | NUMBER                                                         # NumberExpr
    | HEX_NUMBER                                                     # HexExpr
    | CHAR_LITERAL                                                   # CharExpr
    | STRING_LITERAL                                                 # StringExpr
    | '(' expression ')'                                             # ParenExpr
    ;

functionCall: IDENTIFIER '(' argumentList? ')';
argumentList: expression (',' expression)*;

// ========================================================================
// LEXER RULES (Tokenization)
// ========================================================================

// Keywords
CONST: 'const';
BYTE: 'byte';
POINTER: 'pointer';
VOID: 'void';
IF: 'if';
ELSE: 'else';
WHILE: 'while';
RETURN: 'return';

// Identifiers (Variables and Function names)
IDENTIFIER: [a-zA-Z_][a-zA-Z0-9_]*;

// Numeric and String Literals
NUMBER: [0-9]+;
HEX_NUMBER: '$' [0-9a-fA-F]+;
CHAR_LITERAL: '\'' . '\'';
STRING_LITERAL: '"' ~["]* '"';

// Whitespace and Comments (Ignored by the parser)
WS: [ \t\r\n]+ -> channel(HIDDEN);
LINE_COMMENT: '//' ~[\r\n]* -> channel(HIDDEN);
BLOCK_COMMENT: '/*' .*? '*/' -> channel(HIDDEN);