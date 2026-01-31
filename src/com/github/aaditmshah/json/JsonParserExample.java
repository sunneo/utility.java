package com.github.aaditmshah.json;

import java.util.List;

import com.github.aaditmshah.Grammar;
import com.github.aaditmshah.Grammar.ParseResult;
import com.github.aaditmshah.Grammar.ParseState;
import com.github.aaditmshah.Grammar.Parser;
import com.github.aaditmshah.Lexer;

/**
 * Example demonstrating the usage of Lexer and Grammar to parse JSON into AST.
 * 
 * <p>This example shows:</p>
 * <ul>
 *   <li>Creating a flex-like Lexer with addRule() and lambda handlers</li>
 *   <li>Creating a Parsec-like Grammar with parser combinators</li>
 *   <li>Parsing JSON strings into AST nodes</li>
 *   <li>Navigating and manipulating the AST</li>
 * </ul>
 */
public class JsonParserExample {
	
	public static void main(String[] args) {
		System.out.println("=== JSON Parser Example using Lexer and Grammar ===\n");
		
		// Example 1: Basic JSON parsing
		example1_BasicParsing();
		
		// Example 2: Lexer demonstration
		example2_LexerDemo();
		
		// Example 3: Grammar combinators
		example3_GrammarDemo();
		
		// Example 4: Complex JSON
		example4_ComplexJson();
		
		// Example 5: Error handling
		example5_ErrorHandling();
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
	 * Example 2: Lexer demonstration
	 */
	private static void example2_LexerDemo() {
		System.out.println("--- Example 2: Lexer Demonstration ---");
		
		// Create a custom lexer for arithmetic expressions
		Lexer lexer = new Lexer();
		
		// Define token rules using lambda expressions (flex-like pattern)
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
	 * Example 3: Grammar combinators demonstration
	 */
	private static void example3_GrammarDemo() {
		System.out.println("--- Example 3: Grammar Combinators ---");
		
		// Create lexer for simple key-value pairs: key = value
		Lexer lexer = new Lexer();
		lexer.addRule("[a-zA-Z_][a-zA-Z0-9_]*", (lex, match) -> "ID")
		     .addRule("=", (lex, match) -> "EQUALS")
		     .addRule("\"[^\"]*\"", (lex, match) -> "STRING")
		     .addRule("\\d+", (lex, match) -> "NUMBER")
		     .addRule("\\s+", (lex, match) -> null);
		
		String input = "name = \"Alice\"";
		System.out.println("Parsing: " + input);
		
		lexer.setInput(input);
		List<Lexer.Token> tokens = lexer.tokenize();
		
		// Create grammar
		Grammar grammar = new Grammar();
		
		// Define parsers using combinators (Parsec-like pattern)
		Parser<Lexer.Token> idParser = grammar.token("ID");
		Parser<Lexer.Token> equalsParser = grammar.token("EQUALS");
		Parser<Lexer.Token> valueParser = grammar.oneOf("STRING", "NUMBER");
		
		// Combine parsers: ID = VALUE
		Parser<String> assignmentParser = grammar.sequence(idParser, equalsParser, valueParser)
			.map(results -> {
				Lexer.Token id = (Lexer.Token) results.get(0);
				Lexer.Token value = (Lexer.Token) results.get(2);
				return id.value + " -> " + value.value;
			});
		
		// Parse
		ParseResult<String> result = assignmentParser.parse(new ParseState(tokens));
		
		if (result.isSuccess()) {
			System.out.println("Parse result: " + result.getValue());
		} else {
			System.out.println("Parse failed: " + result.getError());
		}
		
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
}
