package com.github.aaditmshah.json;

import java.util.List;

import com.github.aaditmshah.Grammar;
import com.github.aaditmshah.Grammar.ParseResult;
import com.github.aaditmshah.Lexer;

import static com.github.aaditmshah.Grammar.symbols;

/**
 * Example demonstrating the usage of Lexer and Grammar to parse JSON into AST.
 * 
 * <p>This example shows:</p>
 * <ul>
 *   <li>Creating a flex-like Lexer with addRule() and lambda handlers</li>
 *   <li>Creating a bison-like Grammar with production rules and semantic actions</li>
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
		
		// Example 3: Grammar with production rules (bison-like)
		example3_GrammarDemo();
		
		// Example 4: Complex JSON
		example4_ComplexJson();
		
		// Example 5: Error handling
		example5_ErrorHandling();
		
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
	 * Example 3: Grammar with production rules (bison-like)
	 */
	private static void example3_GrammarDemo() {
		System.out.println("--- Example 3: Grammar with Production Rules (bison-like) ---");
		
		// Create lexer for simple calculator
		Lexer lexer = new Lexer();
		lexer.addRule("\\d+", (lex, match) -> "NUMBER")
		     .addRule("\\+", (lex, match) -> "PLUS")
		     .addRule("-", (lex, match) -> "MINUS")
		     .addRule("\\s+", (lex, match) -> null);
		
		// Create grammar using bison-style rules
		// This is similar to:
		//   %token NUMBER PLUS MINUS
		//   %%
		//   expr : expr PLUS term  { $$ = $1 + $3; }
		//        | term            { $$ = $1; }
		//        ;
		//   term : NUMBER          { $$ = atoi($1); }
		//        ;
		
		Grammar grammar = new Grammar();
		
		// Declare terminals
		grammar.token("NUMBER", "PLUS", "MINUS");
		
		// Set start symbol
		grammar.start("expr");
		
		// expr : expr PLUS term { $$ = $1 + $3; }
		grammar.rule("expr", symbols("expr", "PLUS", "term"), vals -> {
			int left = (Integer) vals[0];
			int right = (Integer) vals[2];
			return left + right;
		});
		
		// expr : expr MINUS term { $$ = $1 - $3; }
		grammar.rule("expr", symbols("expr", "MINUS", "term"), vals -> {
			int left = (Integer) vals[0];
			int right = (Integer) vals[2];
			return left - right;
		});
		
		// expr : term { $$ = $1; }
		grammar.rule("expr", symbols("term"), vals -> vals[0]);
		
		// term : NUMBER { $$ = atoi($1); }
		grammar.rule("term", symbols("NUMBER"), vals -> {
			Lexer.Token tok = (Lexer.Token) vals[0];
			return Integer.parseInt(tok.value);
		});
		
		// Parse expression
		String input = "3 + 4 - 2";
		System.out.println("Parsing: " + input);
		
		lexer.setInput(input);
		List<Lexer.Token> tokens = lexer.tokenize();
		
		ParseResult result = grammar.parse(tokens);
		
		if (result.isSuccess()) {
			System.out.println("Result: " + result.getValue());
		} else {
			System.out.println("Parse failed: " + result.getError());
		}
		
		// Print the grammar in bison format
		System.out.println("\nGenerated grammar:");
		System.out.println(grammar.toGrammarString());
		
		System.out.println();
	}
	
	/**
	 * Example 4: Complex JSON parsing
	 */
	private static void example4_ComplexJson() {
		System.out.println("--- Example 4: Complex JSON Parsing ---");
		
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
	 * Example 5: Error handling
	 */
	private static void example5_ErrorHandling() {
		System.out.println("--- Example 5: Error Handling ---");
		
		String[] badJsons = {
			"{\"name\": }",           // Missing value
			"{\"name\" \"John\"}",    // Missing colon
			"[1, 2, 3",               // Missing bracket
			"{key: \"value\"}",       // Unquoted key
		};
		
		JsonParser parser = new JsonParser();
		
		for (String json : badJsons) {
			System.out.println("Parsing: " + json);
			try {
				parser.parse(json);
				System.out.println("  Unexpected success!");
			} catch (JsonParser.JsonParseException e) {
				System.out.println("  Error: " + e.getMessage());
			}
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
