grammar LycoScript;

// =============================================================================
// PARSER RULES (Syntax)
// These rules define the logical structure of the code.
// Rule names must begin with a lowercase letter.
// =============================================================================

// The entry point of a LycoScript file.
// A program is a sequence of zero or more statements, followed by the End Of File.
program: importStatement* declaration* EOF;

importStatement: 'import' STRING_LITERAL ';' ;

declaration
    : structDeclaration
    | functionDeclaration
    | globalVariableDeclaration
    ;

structDeclaration
    : 'struct' identifier '{' structField* '}' ';'          # StructDef
    ;

structField
    : type identifier ';'                                   # StructSimpleField
    | type identifier '[' NUMBER ']' ';'                    # StructArrayField
    ;

functionDeclaration
    : 'native' (type | 'void') identifier '(' paramList? ')' ';'           # NativeFuncDef
    | (type | 'void') identifier '(' paramList? ')' '{' statement* '}'     # FuncDef
    ;

paramList
    : type identifier (',' type identifier)*
    ;

globalVariableDeclaration
    : 'const'? type identifier '=' expression ';'           # GlobalVarDecl
    | type identifier '[' NUMBER ']' ';'                    # GlobalArrayDecl
    ;

statement
    // Local Variables
    : 'const'? type identifier '=' expression ';'           # LocalVarDecl
    | type identifier '[' NUMBER ']' ';'                    # LocalArrayDecl

    // Assignments
    | identifier '=' expression ';'                         # VarAssignment
    | identifier '[' expression ']' '=' expression ';'      # ArrayAssignment
    | identifier '.' identifier '=' expression ';'          # StructFieldAssignment
    | '*' identifier '=' expression ';'                     # PointerAssignment

    // Control Flow
    | 'while' '(' expression ')' '{' statement* '}'                                 # WhileLoop
        | 'for' '(' forInit? ';' expression? ';' forUpdate? ')' '{' statement* '}'  # ForLoop
        | 'for' '(' type identifier ':' identifier ')' '{' statement* '}'           # ForEachLoop
    | 'if' '(' expression ')' '{' statement* '}' ('else' '{' statement* '}')?       # IfElseBlock

    // Function calls and returns
    | identifier '(' argList? ')' ';'                       # FunctionCallStmt
    | 'return' expression? ';'                              # ReturnStmt
    ;

forInit
    : type identifier '=' expression
    | identifier '=' expression
    ;

forUpdate
    : identifier '=' expression
    ;

argList
    : expression (',' expression)*
    ;

expression
    // Function Call in Expression (e.g., x = getRandom();)
    : identifier '(' argList? ')'                           # FunctionCallExpr

    // Struct and Array Access
    | identifier '.' identifier                             # StructFieldAccessExpr
    | identifier '[' expression ']'                         # ArrayAccessExpr

    // Unary Operators (Pointers & Logic)
    | '&' identifier                                        # AddressOfExpr
    | '*' identifier                                        # DereferenceExpr
    | '!' expression                                        # LogicalNotExpr
    | '~' expression                                        # BitwiseNotExpr

    // Math Operations (Precedence rules apply top to bottom)
    | left=expression op=('*' | '/') right=expression       # MathMulDivExpr
    | left=expression op=('+' | '-') right=expression       # MathAddSubExpr

    // Bitwise Shift
    | left=expression op=('<<' | '>>') right=expression     # BitwiseShiftExpr

    // Relational (Comparison)
    | left=expression op=('<' | '<=' | '>' | '>=') right=expression # RelationalExpr
    | left=expression op=('==' | '!=') right=expression     # EqualityExpr

    // Bitwise Logic
    | left=expression '&' right=expression                  # BitwiseAndExpr
    | left=expression '^' right=expression                  # BitwiseXorExpr
    | left=expression '|' right=expression                  # BitwiseOrExpr

    // Literals and Variables
    | NUMBER                                                # NumberExpr
    | HEX_NUMBER                                            # HexNumberExpr
    | STRING_LITERAL                                        # StringExpr
    | CHAR_LITERAL                                          # CharExpr
    | TRUE                                                  # BooleanTrueExpr
    | FALSE                                                 # BooleanFalseExpr
    | identifier                                            # IdentifierExpr
    | '(' expression ')'                                    # ParenthesisExpr
    ;

// Supported data types. For a 6502 CPU, an 8-bit 'byte' is highly recommended.
type: 'int' | 'byte' | 'boolean' | 'char' | 'string' | type '*';

identifier: ID;

// =============================================================================
// LEXER RULES (Tokens)
// These rules define the vocabulary of the language.
// Rule names must begin with an UPPERCASE letter.
// =============================================================================

ID: [a-zA-Z_] [a-zA-Z0-9_]*;

TRUE: 'true';
FALSE: 'false';

STRING_LITERAL: '"' (~["\r\n\\] | '\\' .)* '"';
CHAR_LITERAL: '\'' . '\'';

HEX_NUMBER: '0' [xX] [0-9a-fA-F]+;
NUMBER: [0-9]+;

WS: [ \t\r\n]+ -> channel(HIDDEN);
LINE_COMMENT: '//' ~[\r\n]* -> channel(HIDDEN);
BLOCK_COMMENT: '/*' .*? '*/' -> channel(HIDDEN);
ANY_OTHER: . ;