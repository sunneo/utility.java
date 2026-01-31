/* Sample yacc/bison grammar specification for a simple calculator */

%{
/* Prologue code */
%}

%token NUMBER
%token PLUS MINUS MULTIPLY DIVIDE
%token LPAREN RPAREN

%left PLUS MINUS
%left MULTIPLY DIVIDE

%start expr

%%

expr
    : expr PLUS term      { $$ = ((Integer)$1) + ((Integer)$3); }
    | expr MINUS term     { $$ = ((Integer)$1) - ((Integer)$3); }
    | term                { $$ = $1; }
    ;

term
    : term MULTIPLY factor { $$ = ((Integer)$1) * ((Integer)$3); }
    | term DIVIDE factor   { $$ = ((Integer)$1) / ((Integer)$3); }
    | factor               { $$ = $1; }
    ;

factor
    : LPAREN expr RPAREN  { $$ = $2; }
    | NUMBER              { $$ = Integer.parseInt(((Lexer.Token)$1).value); }
    ;

%%
