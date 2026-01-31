/* Sample JFlex lexer specification for a simple calculator */

%%

%class CalcLexer
%unicode
%line
%column

/* Macros */
DIGIT   = [0-9]
NUMBER  = {DIGIT}+

%%

/* Rules */
{NUMBER}        { return NUMBER; }
"+"             { return PLUS; }
"-"             { return MINUS; }
"*"             { return MULTIPLY; }
"/"             { return DIVIDE; }
"("             { return LPAREN; }
")"             { return RPAREN; }
[ \t\n\r]+      { /* skip whitespace */ }

%%
