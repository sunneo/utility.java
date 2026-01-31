package com.github.aaditmshah.json;

import java.util.List;

import com.github.aaditmshah.BisonGrammar;
import com.github.aaditmshah.Grammar;
import com.github.aaditmshah.Lexer;
import com.github.aaditmshah.ParsecGrammar;

import static com.github.aaditmshah.BisonGrammar.symbols;

/**
 * Example demonstrating the usage of Lexer and Grammar to parse JSON into AST.
 * 
 * <p>This example shows:</p>
 * <ul>
 *   <li>Creating a flex-like Lexer with addRule() and lambda handlers</li>
 *   <li>Creating a bison-like Grammar with production rules and semantic actions</li>
 *   <li>Creating a Parsec-like Grammar with parser combinators</li>
 *   <li>Both grammar styles implement the same Grammar interface</li>
 *   <li>Parsing JSON strings into AST nodes</li>
 *   <li>Navigating and manipulating the AST</li>
 * </ul>
 */
public class JsonParserExample {

public static void main(String[] args) {
System.out.println("=== JSON Parser Example using Lexer and Grammar ===\n");

// Example 1: Basic JSON parsing
example1_BasicParsing();

// Example 2: Lexer demonstration (flex-like)
example2_LexerDemo();

// Example 3: Bison-style Grammar
example3_BisonGrammarDemo();

// Example 4: Parsec-style Grammar
example4_ParsecGrammarDemo();

// Example 5: Complex JSON
example5_ComplexJson();

// Example 6: Show generated grammar in bison format
example6_ShowGrammar();
}

/**
 * Example 1: Basic JSON parsing
 */
private static void example1_BasicParsing() {
System.out.println("--- Example 1: Basic JSON Parsing ---");

String json = "{\"name\": \"John Doe\", \"age\": 30, \"active\": true}";
System.out.println("Input JSON: " + json);

try {
JsonParser parser = new JsonParser();
JsonNode ast = parser.parse(json);

System.out.println("Parsed AST type: " + ast.getType());

// Navigate the AST
JsonNode.JsonObject obj = ast.asObject();
System.out.println("Name: " + obj.get("name").asString().getValue());
System.out.println("Age: " + obj.get("age").asNumber().intValue());
System.out.println("Active: " + obj.get("active").asBoolean().getValue());

// Convert back to JSON
System.out.println("Reconstructed JSON: " + ast.toJson());
System.out.println("Pretty printed:\n" + ast.toJson(2));

} catch (JsonParser.JsonParseException e) {
System.err.println("Parse error: " + e.getMessage());
}

System.out.println();
}

/**
 * Example 2: Lexer demonstration (flex-like)
 */
private static void example2_LexerDemo() {
System.out.println("--- Example 2: Lexer Demonstration (flex-like) ---");

// Create a custom lexer for arithmetic expressions
Lexer lexer = new Lexer();

// Define token rules using lambda expressions (flex-like pattern)
// This is similar to flex rules:
//   [0-9]+    { return NUMBER; }
//   "+"       { return PLUS; }
//   etc.
lexer.addRule("\\d+", (lex, match) -> "NUMBER")
     .addRule("\\+", (lex, match) -> "PLUS")
     .addRule("-", (lex, match) -> "MINUS")
     .addRule("\\*", (lex, match) -> "MULTIPLY")
     .addRule("/", (lex, match) -> "DIVIDE")
     .addRule("\\(", (lex, match) -> "LPAREN")
     .addRule("\\)", (lex, match) -> "RPAREN")
     .addRule("\\s+", (lex, match) -> null); // Skip whitespace

String expression = "3 + 4 * (2 - 1)";
System.out.println("Tokenizing: " + expression);

lexer.setInput(expression);
List<Lexer.Token> tokens = lexer.tokenize();

System.out.println("Tokens:");
for (Lexer.Token token : tokens) {
System.out.println("  " + token);
}

System.out.println();
}

/**
 * Example 3: Bison-style Grammar with production rules
 * 
 * This is similar to writing yacc/bison:
 * 
 *   %token NUMBER PLUS MINUS
 *   %%
 *   expr : expr PLUS term   { $$ = $1 + $3; }
 *        | expr MINUS term  { $$ = $1 - $3; }
 *        | term             { $$ = $1; }
 *        ;
 *   term : NUMBER           { $$ = atoi($1); }
 *        ;
 */
private static void example3_BisonGrammarDemo() {
System.out.println("--- Example 3: Bison-style Grammar ---");

// Create lexer
Lexer lexer = new Lexer();
lexer.addRule("\\d+", (lex, match) -> "NUMBER")
     .addRule("\\+", (lex, match) -> "PLUS")
     .addRule("-", (lex, match) -> "MINUS")
     .addRule("\\s+", (lex, match) -> null);

// Create Bison-style grammar with recursive rules
BisonGrammar grammar = new BisonGrammar();

// %token NUMBER PLUS MINUS
grammar.token("NUMBER", "PLUS", "MINUS");

// %start expr
grammar.start("expr");

// expr : expr PLUS term { $$ = $1 + $3; }
grammar.rule("expr", symbols("expr", "PLUS", "term"), vals -> {
int left = (Integer) vals[0];
int right = (Integer) vals[2];
System.out.println("  Reducing: " + left + " + " + right);
return left + right;
});

// expr : expr MINUS term { $$ = $1 - $3; }
grammar.rule("expr", symbols("expr", "MINUS", "term"), vals -> {
int left = (Integer) vals[0];
int right = (Integer) vals[2];
System.out.println("  Reducing: " + left + " - " + right);
return left - right;
});

// expr : term { $$ = $1; }
grammar.rule("expr", symbols("term"), vals -> vals[0]);

// term : NUMBER { $$ = atoi($1); }
grammar.rule("term", symbols("NUMBER"), vals -> {
Lexer.Token tok = (Lexer.Token) vals[0];
int value = Integer.parseInt(tok.value);
System.out.println("  Reading number: " + value);
return value;
});

// Parse expression
String input = "3 + 4 - 2";
System.out.println("Parsing (Bison-style): " + input);

lexer.setInput(input);
List<Lexer.Token> tokens = lexer.tokenize();

Grammar.ParseResult result = grammar.parse(tokens);

System.out.println("Grammar type: " + grammar.getGrammarType());
if (result.isSuccess()) {
System.out.println("Result: " + result.getValue());
} else {
System.out.println("Parse failed: " + result.getError());
}

// Print the grammar in bison format
System.out.println("\nGenerated grammar in Bison format:");
System.out.println(grammar.toGrammarString());

System.out.println();
}

/**
 * Example 4: Parsec-style Grammar with parser combinators
 * 
 * This is similar to Haskell's Parsec:
 * 
 *   expr = do
 *     left <- term
 *     rest left
 *   where
 *     rest acc = (do
 *       op <- plus <|> minus
 *       right <- term
 *       rest (op acc right))
 *       <|> return acc
 */
private static void example4_ParsecGrammarDemo() {
System.out.println("--- Example 4: Parsec-style Grammar ---");

// Create lexer
Lexer lexer = new Lexer();
lexer.addRule("\\d+", (lex, match) -> "NUMBER")
     .addRule("\\+", (lex, match) -> "PLUS")
     .addRule("-", (lex, match) -> "MINUS")
     .addRule("\\s+", (lex, match) -> null);

// Create Parsec-style grammar with combinators
ParsecGrammar grammar = new ParsecGrammar();
grammar.setDescription("Simple arithmetic expression parser");

// Define token parsers
ParsecGrammar.Parser<Lexer.Token> number = grammar.token("NUMBER");

// term : NUMBER
ParsecGrammar.Parser<Integer> term = number.map(tok -> {
int value = Integer.parseInt(tok.value);
System.out.println("  Reading number: " + value);
return value;
});

// expr : term ((PLUS | MINUS) term)*
// Using left-associative parsing
ParsecGrammar.Parser<Integer> expr = state -> {
// Parse first term
ParsecGrammar.ParserResult<Integer> firstResult = term.parse(state);
if (firstResult.isFailure()) {
return firstResult;
}

int accumulator = firstResult.getValue();
ParsecGrammar.ParseState currentState = firstResult.getState();

// Parse remaining (op term)* 
while (true) {
// Try to parse operator
ParsecGrammar.ParserResult<Lexer.Token> opResult = 
grammar.oneOf("PLUS", "MINUS").parse(currentState);

if (opResult.isFailure()) {
break; // No more operators
}

// Parse right operand
ParsecGrammar.ParserResult<Integer> rightResult = term.parse(opResult.getState());
if (rightResult.isFailure()) {
break; // No right operand
}

// Apply operation
String op = opResult.getValue().type;
int right = rightResult.getValue();

if (op.equals("PLUS")) {
System.out.println("  Reducing: " + accumulator + " + " + right);
accumulator = accumulator + right;
} else {
System.out.println("  Reducing: " + accumulator + " - " + right);
accumulator = accumulator - right;
}

currentState = rightResult.getState();
}

return ParsecGrammar.ParserResult.success(accumulator, currentState);
};

// Set main parser
grammar.setMainParser(expr);

// Parse expression
String input = "3 + 4 - 2";
System.out.println("Parsing (Parsec-style): " + input);

lexer.setInput(input);
List<Lexer.Token> tokens = lexer.tokenize();

Grammar.ParseResult result = grammar.parse(tokens);

System.out.println("Grammar type: " + grammar.getGrammarType());
if (result.isSuccess()) {
System.out.println("Result: " + result.getValue());
} else {
System.out.println("Parse failed: " + result.getError());
}

System.out.println();
}

/**
 * Example 5: Complex JSON parsing
 */
private static void example5_ComplexJson() {
System.out.println("--- Example 5: Complex JSON Parsing ---");

String json = "{\n" +
"  \"users\": [\n" +
"    {\"id\": 1, \"name\": \"Alice\", \"email\": \"alice@example.com\"},\n" +
"    {\"id\": 2, \"name\": \"Bob\", \"email\": \"bob@example.com\"}\n" +
"  ],\n" +
"  \"total\": 2,\n" +
"  \"page\": 1,\n" +
"  \"hasMore\": false,\n" +
"  \"metadata\": null\n" +
"}";

System.out.println("Input JSON:");
System.out.println(json);
System.out.println();

try {
JsonParser parser = new JsonParser();
JsonNode ast = parser.parse(json);

// Navigate complex structure
JsonNode.JsonObject root = ast.asObject();
JsonNode.JsonArray users = root.get("users").asArray();

System.out.println("Number of users: " + users.size());

for (int i = 0; i < users.size(); i++) {
JsonNode.JsonObject user = users.get(i).asObject();
System.out.println("User " + (i + 1) + ":");
System.out.println("  ID: " + user.get("id").asNumber().intValue());
System.out.println("  Name: " + user.get("name").asString().getValue());
System.out.println("  Email: " + user.get("email").asString().getValue());
}

System.out.println("Total: " + root.get("total").asNumber().intValue());
System.out.println("Has more: " + root.get("hasMore").asBoolean().getValue());
System.out.println("Metadata is null: " + root.get("metadata").isNull());

} catch (JsonParser.JsonParseException e) {
System.err.println("Parse error: " + e.getMessage());
}

System.out.println();
}

/**
 * Example 6: Show generated grammar in bison format
 */
private static void example6_ShowGrammar() {
System.out.println("--- Example 6: JSON Grammar in Bison Format ---");

JsonParser parser = new JsonParser();
System.out.println(parser.getGrammarString());
}
}
