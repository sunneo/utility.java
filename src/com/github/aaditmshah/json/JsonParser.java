package com.github.aaditmshah.json;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.github.aaditmshah.Grammar;
import com.github.aaditmshah.Grammar.ParseResult;
import com.github.aaditmshah.Grammar.ParseState;
import com.github.aaditmshah.Grammar.Parser;
import com.github.aaditmshah.Lexer;

/**
 * JSON Parser that uses the Lexer and Grammar to parse JSON and build an AST.
 * 
 * <p>This is an example of using the flex-like Lexer and Parsec-like Grammar
 * to implement a complete parser for JSON.</p>
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * JsonParser parser = new JsonParser();
 * 
 * String json = "{\"name\": \"John\", \"age\": 30}";
 * JsonNode ast = parser.parse(json);
 * 
 * // Access AST
 * JsonNode.JsonObject obj = ast.asObject();
 * String name = obj.get("name").asString().getValue();
 * int age = obj.get("age").asNumber().intValue();
 * 
 * // Convert back to JSON
 * String output = ast.toJson(2); // Pretty print with 2-space indent
 * }</pre>
 */
public class JsonParser {
	
	// Token types
	public static final String T_LBRACE = "LBRACE";       // {
	public static final String T_RBRACE = "RBRACE";       // }
	public static final String T_LBRACKET = "LBRACKET";   // [
	public static final String T_RBRACKET = "RBRACKET";   // ]
	public static final String T_COLON = "COLON";         // :
	public static final String T_COMMA = "COMMA";         // ,
	public static final String T_STRING = "STRING";       // "..."
	public static final String T_NUMBER = "NUMBER";       // 123, 1.5, -3.14e10
	public static final String T_TRUE = "TRUE";           // true
	public static final String T_FALSE = "FALSE";         // false
	public static final String T_NULL = "NULL";           // null
	
	private final Lexer lexer;
	private final Grammar grammar;
	
	// Parsers
	private final Parser<JsonNode> valueParser;
	
	public JsonParser() {
		lexer = createLexer();
		grammar = new Grammar();
		valueParser = createGrammar();
	}
	
	/**
	 * Create the JSON lexer with all token rules
	 */
	private Lexer createLexer() {
		Lexer lex = new Lexer();
		
		// Skip whitespace
		lex.addRule("\\s+", (l, m) -> null);
		
		// Structural characters
		lex.addRule("\\{", (l, m) -> T_LBRACE);
		lex.addRule("\\}", (l, m) -> T_RBRACE);
		lex.addRule("\\[", (l, m) -> T_LBRACKET);
		lex.addRule("\\]", (l, m) -> T_RBRACKET);
		lex.addRule(":", (l, m) -> T_COLON);
		lex.addRule(",", (l, m) -> T_COMMA);
		
		// Keywords
		lex.addRule("true", (l, m) -> T_TRUE);
		lex.addRule("false", (l, m) -> T_FALSE);
		lex.addRule("null", (l, m) -> T_NULL);
		
		// Number: integer or floating point, with optional exponent
		// Regex: -?(0|[1-9][0-9]*)(\.[0-9]+)?([eE][+-]?[0-9]+)?
		lex.addRule("-?(0|[1-9][0-9]*)(\\.[0-9]+)?([eE][+-]?[0-9]+)?", (l, m) -> T_NUMBER);
		
		// String: double-quoted with escape sequences
		// This regex handles most common escape sequences
		lex.addRule("\"([^\"\\\\]|\\\\[\"\\\\bfnrt/]|\\\\u[0-9a-fA-F]{4})*\"", (l, m) -> T_STRING);
		
		return lex;
	}
	
	/**
	 * Create the JSON grammar with parser combinators
	 */
	private Parser<JsonNode> createGrammar() {
		// Token parsers
		Parser<Lexer.Token> lbrace = grammar.token(T_LBRACE);
		Parser<Lexer.Token> rbrace = grammar.token(T_RBRACE);
		Parser<Lexer.Token> lbracket = grammar.token(T_LBRACKET);
		Parser<Lexer.Token> rbracket = grammar.token(T_RBRACKET);
		Parser<Lexer.Token> colon = grammar.token(T_COLON);
		Parser<Lexer.Token> comma = grammar.token(T_COMMA);
		Parser<Lexer.Token> stringToken = grammar.token(T_STRING);
		Parser<Lexer.Token> numberToken = grammar.token(T_NUMBER);
		Parser<Lexer.Token> trueToken = grammar.token(T_TRUE);
		Parser<Lexer.Token> falseToken = grammar.token(T_FALSE);
		Parser<Lexer.Token> nullToken = grammar.token(T_NULL);
		
		// Value parsers (primitives)
		Parser<JsonNode> stringValue = stringToken.map(t -> 
			new JsonNode.JsonString(parseStringLiteral(t.value))
		);
		
		Parser<JsonNode> numberValue = numberToken.map(t -> 
			new JsonNode.JsonNumber(t.value)
		);
		
		Parser<JsonNode> trueValue = trueToken.map(t -> 
			JsonNode.JsonBoolean.TRUE
		);
		
		Parser<JsonNode> falseValue = falseToken.map(t -> 
			JsonNode.JsonBoolean.FALSE
		);
		
		Parser<JsonNode> nullValue = nullToken.map(t -> 
			JsonNode.JsonNull.INSTANCE
		);
		
		// Use lazy evaluation for recursive grammar
		Parser<JsonNode> value = grammar.lazy(() -> 
			grammar.choice(
				objectParser(),
				arrayParser(),
				stringValue,
				numberValue,
				trueValue,
				falseValue,
				nullValue
			)
		);
		
		return value;
	}
	
	/**
	 * Create the object parser
	 */
	private Parser<JsonNode> objectParser() {
		Parser<Lexer.Token> lbrace = grammar.token(T_LBRACE);
		Parser<Lexer.Token> rbrace = grammar.token(T_RBRACE);
		Parser<Lexer.Token> colon = grammar.token(T_COLON);
		Parser<Lexer.Token> comma = grammar.token(T_COMMA);
		Parser<Lexer.Token> stringToken = grammar.token(T_STRING);
		
		// Member: string : value
		Parser<Map.Entry<String, JsonNode>> member = state -> {
			ParseResult<Lexer.Token> keyResult = stringToken.parse(state);
			if (keyResult.isFailure()) {
				return ParseResult.failure(keyResult.getError(), keyResult.getState());
			}
			
			ParseResult<Lexer.Token> colonResult = colon.parse(keyResult.getState());
			if (colonResult.isFailure()) {
				return ParseResult.failure(colonResult.getError(), colonResult.getState());
			}
			
			ParseResult<JsonNode> valueResult = valueParser.parse(colonResult.getState());
			if (valueResult.isFailure()) {
				return ParseResult.failure(valueResult.getError(), valueResult.getState());
			}
			
			String key = parseStringLiteral(keyResult.getValue().value);
			Map.Entry<String, JsonNode> entry = new java.util.AbstractMap.SimpleEntry<>(key, valueResult.getValue());
			return ParseResult.success(entry, valueResult.getState());
		};
		
		// Members: member (, member)*
		Parser<List<Map.Entry<String, JsonNode>>> members = grammar.sepBy(member, comma);
		
		// Object: { members? }
		return state -> {
			ParseResult<Lexer.Token> lbraceResult = lbrace.parse(state);
			if (lbraceResult.isFailure()) {
				return ParseResult.failure(lbraceResult.getError(), lbraceResult.getState());
			}
			
			ParseResult<List<Map.Entry<String, JsonNode>>> membersResult = members.parse(lbraceResult.getState());
			// members can be empty, so we check the state
			
			ParseResult<Lexer.Token> rbraceResult = rbrace.parse(membersResult.getState());
			if (rbraceResult.isFailure()) {
				return ParseResult.failure(rbraceResult.getError(), rbraceResult.getState());
			}
			
			Map<String, JsonNode> map = new LinkedHashMap<>();
			for (Map.Entry<String, JsonNode> entry : membersResult.getValue()) {
				map.put(entry.getKey(), entry.getValue());
			}
			
			return ParseResult.success(new JsonNode.JsonObject(map), rbraceResult.getState());
		};
	}
	
	/**
	 * Create the array parser
	 */
	private Parser<JsonNode> arrayParser() {
		Parser<Lexer.Token> lbracket = grammar.token(T_LBRACKET);
		Parser<Lexer.Token> rbracket = grammar.token(T_RBRACKET);
		Parser<Lexer.Token> comma = grammar.token(T_COMMA);
		
		// Elements: value (, value)*
		Parser<List<JsonNode>> elements = grammar.sepBy(valueParser, comma);
		
		// Array: [ elements? ]
		return state -> {
			ParseResult<Lexer.Token> lbracketResult = lbracket.parse(state);
			if (lbracketResult.isFailure()) {
				return ParseResult.failure(lbracketResult.getError(), lbracketResult.getState());
			}
			
			ParseResult<List<JsonNode>> elementsResult = elements.parse(lbracketResult.getState());
			
			ParseResult<Lexer.Token> rbracketResult = rbracket.parse(elementsResult.getState());
			if (rbracketResult.isFailure()) {
				return ParseResult.failure(rbracketResult.getError(), rbracketResult.getState());
			}
			
			return ParseResult.success(new JsonNode.JsonArray(elementsResult.getValue()), rbracketResult.getState());
		};
	}
	
	/**
	 * Parse a JSON string
	 * 
	 * @param json The JSON string to parse
	 * @return The parsed JSON AST
	 * @throws JsonParseException if parsing fails
	 */
	public JsonNode parse(String json) throws JsonParseException {
		// Tokenize
		lexer.setInput(json);
		List<Lexer.Token> tokens = lexer.tokenize();
		
		if (lexer.hasError()) {
			throw new JsonParseException("Lexer error: unexpected character at position " + lexer.getIndex());
		}
		
		if (tokens.isEmpty()) {
			throw new JsonParseException("Empty input");
		}
		
		// Parse
		ParseState state = new ParseState(tokens);
		ParseResult<JsonNode> result = valueParser.parse(state);
		
		if (result.isFailure()) {
			throw new JsonParseException(result.getError());
		}
		
		// Check for trailing tokens
		if (result.getState().hasMore()) {
			Lexer.Token extra = result.getState().current();
			throw new JsonParseException("Unexpected token after JSON value: " + extra.type + " at line " + extra.line);
		}
		
		return result.getValue();
	}
	
	/**
	 * Parse a JSON string literal (removes quotes and processes escape sequences)
	 */
	private static String parseStringLiteral(String literal) {
		// Remove surrounding quotes
		String content = literal.substring(1, literal.length() - 1);
		
		StringBuilder sb = new StringBuilder();
		int i = 0;
		while (i < content.length()) {
			char c = content.charAt(i);
			if (c == '\\' && i + 1 < content.length()) {
				char next = content.charAt(i + 1);
				switch (next) {
					case '"': sb.append('"'); i += 2; break;
					case '\\': sb.append('\\'); i += 2; break;
					case '/': sb.append('/'); i += 2; break;
					case 'b': sb.append('\b'); i += 2; break;
					case 'f': sb.append('\f'); i += 2; break;
					case 'n': sb.append('\n'); i += 2; break;
					case 'r': sb.append('\r'); i += 2; break;
					case 't': sb.append('\t'); i += 2; break;
					case 'u':
						if (i + 6 <= content.length()) {
							String hex = content.substring(i + 2, i + 6);
							sb.append((char) Integer.parseInt(hex, 16));
							i += 6;
						} else {
							sb.append(c);
							i++;
						}
						break;
					default:
						sb.append(c);
						i++;
				}
			} else {
				sb.append(c);
				i++;
			}
		}
		
		return sb.toString();
	}
	
	/**
	 * Exception thrown when JSON parsing fails
	 */
	public static class JsonParseException extends Exception {
		public JsonParseException(String message) {
			super(message);
		}
	}
}
