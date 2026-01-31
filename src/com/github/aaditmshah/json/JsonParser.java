package com.github.aaditmshah.json;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.github.aaditmshah.BisonGrammar;
import com.github.aaditmshah.Grammar;
import com.github.aaditmshah.Lexer;

import static com.github.aaditmshah.BisonGrammar.symbols;

/**
 * JSON Parser that uses the Lexer and bison-style Grammar to parse JSON and build an AST.
 * 
 * <p>This is an example of using the flex-like Lexer and bison-like Grammar
 * to implement a complete parser for JSON.</p>
 * 
 * <p>The grammar for JSON in bison/yacc format:</p>
 * <pre>
 * %token STRING NUMBER TRUE FALSE NULL
 * %token LBRACE RBRACE LBRACKET RBRACKET COLON COMMA
 * 
 * %%
 * 
 * value
 *     : object
 *     | array
 *     | STRING    { $$ = new JsonString($1); }
 *     | NUMBER    { $$ = new JsonNumber($1); }
 *     | TRUE      { $$ = JsonBoolean.TRUE; }
 *     | FALSE     { $$ = JsonBoolean.FALSE; }
 *     | NULL      { $$ = JsonNull.INSTANCE; }
 *     ;
 * 
 * object
 *     : LBRACE RBRACE                    { $$ = new JsonObject({}); }
 *     | LBRACE members RBRACE            { $$ = new JsonObject($2); }
 *     ;
 * 
 * members
 *     : member                           { $$ = [$1]; }
 *     | members COMMA member             { $$ = $1.add($3); }
 *     ;
 * 
 * member
 *     : STRING COLON value               { $$ = pair($1, $3); }
 *     ;
 * 
 * array
 *     : LBRACKET RBRACKET                { $$ = new JsonArray([]); }
 *     | LBRACKET elements RBRACKET       { $$ = new JsonArray($2); }
 *     ;
 * 
 * elements
 *     : value                            { $$ = [$1]; }
 *     | elements COMMA value             { $$ = $1.add($3); }
 *     ;
 * 
 * %%
 * </pre>
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
	
	// Token types (terminals)
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
	
	public JsonParser() {
		lexer = createLexer();
		grammar = createGrammar();
	}
	
	/**
	 * Create the JSON lexer with all token rules (flex-like)
	 */
	private Lexer createLexer() {
		Lexer lex = new Lexer();
		
		// Skip whitespace (return null to skip)
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
		lex.addRule("-?(0|[1-9][0-9]*)(\\.[0-9]+)?([eE][+-]?[0-9]+)?", (l, m) -> T_NUMBER);
		
		// String: double-quoted with escape sequences
		lex.addRule("\"([^\"\\\\]|\\\\[\"\\\\bfnrt/]|\\\\u[0-9a-fA-F]{4})*\"", (l, m) -> T_STRING);
		
		return lex;
	}
	
	/**
	 * Create the JSON grammar with production rules (bison-like)
	 */
	@SuppressWarnings("unchecked")
	private BisonGrammar createGrammar() {
		BisonGrammar g = new BisonGrammar();
		
		// Declare terminal symbols (%token)
		g.token(T_LBRACE, T_RBRACE, T_LBRACKET, T_RBRACKET, T_COLON, T_COMMA,
		        T_STRING, T_NUMBER, T_TRUE, T_FALSE, T_NULL);
		
		// Set start symbol
		g.start("value");
		
		// value : object
		g.rule("value", symbols("object"), vals -> vals[0]);
		
		// value : array
		g.rule("value", symbols("array"), vals -> vals[0]);
		
		// value : STRING  { $$ = new JsonString($1); }
		g.rule("value", symbols(T_STRING), vals -> {
			Lexer.Token tok = (Lexer.Token) vals[0];
			return new JsonNode.JsonString(parseStringLiteral(tok.value));
		});
		
		// value : NUMBER  { $$ = new JsonNumber($1); }
		g.rule("value", symbols(T_NUMBER), vals -> {
			Lexer.Token tok = (Lexer.Token) vals[0];
			return new JsonNode.JsonNumber(tok.value);
		});
		
		// value : TRUE    { $$ = JsonBoolean.TRUE; }
		g.rule("value", symbols(T_TRUE), vals -> JsonNode.JsonBoolean.TRUE);
		
		// value : FALSE   { $$ = JsonBoolean.FALSE; }
		g.rule("value", symbols(T_FALSE), vals -> JsonNode.JsonBoolean.FALSE);
		
		// value : NULL    { $$ = JsonNull.INSTANCE; }
		g.rule("value", symbols(T_NULL), vals -> JsonNode.JsonNull.INSTANCE);
		
		// object : LBRACE RBRACE  { $$ = new JsonObject({}); }
		g.rule("object", symbols(T_LBRACE, T_RBRACE), vals -> {
			return new JsonNode.JsonObject(new LinkedHashMap<>());
		});
		
		// object : LBRACE members RBRACE  { $$ = new JsonObject($2); }
		g.rule("object", symbols(T_LBRACE, "members", T_RBRACE), vals -> {
			List<Map.Entry<String, JsonNode>> members = (List<Map.Entry<String, JsonNode>>) vals[1];
			Map<String, JsonNode> map = new LinkedHashMap<>();
			for (Map.Entry<String, JsonNode> entry : members) {
				map.put(entry.getKey(), entry.getValue());
			}
			return new JsonNode.JsonObject(map);
		});
		
		// members : member  { $$ = [$1]; }
		g.rule("members", symbols("member"), vals -> {
			List<Map.Entry<String, JsonNode>> list = new ArrayList<>();
			list.add((Map.Entry<String, JsonNode>) vals[0]);
			return list;
		});
		
		// members : members COMMA member  { $1.add($3); $$ = $1; }
		g.rule("members", symbols("members", T_COMMA, "member"), vals -> {
			List<Map.Entry<String, JsonNode>> list = (List<Map.Entry<String, JsonNode>>) vals[0];
			list.add((Map.Entry<String, JsonNode>) vals[2]);
			return list;
		});
		
		// member : STRING COLON value  { $$ = pair($1, $3); }
		g.rule("member", symbols(T_STRING, T_COLON, "value"), vals -> {
			Lexer.Token keyToken = (Lexer.Token) vals[0];
			JsonNode value = (JsonNode) vals[2];
			String key = parseStringLiteral(keyToken.value);
			return new java.util.AbstractMap.SimpleEntry<>(key, value);
		});
		
		// array : LBRACKET RBRACKET  { $$ = new JsonArray([]); }
		g.rule("array", symbols(T_LBRACKET, T_RBRACKET), vals -> {
			return new JsonNode.JsonArray(new ArrayList<>());
		});
		
		// array : LBRACKET elements RBRACKET  { $$ = new JsonArray($2); }
		g.rule("array", symbols(T_LBRACKET, "elements", T_RBRACKET), vals -> {
			List<JsonNode> elements = (List<JsonNode>) vals[1];
			return new JsonNode.JsonArray(elements);
		});
		
		// elements : value  { $$ = [$1]; }
		g.rule("elements", symbols("value"), vals -> {
			List<JsonNode> list = new ArrayList<>();
			list.add((JsonNode) vals[0]);
			return list;
		});
		
		// elements : elements COMMA value  { $1.add($3); $$ = $1; }
		g.rule("elements", symbols("elements", T_COMMA, "value"), vals -> {
			List<JsonNode> list = (List<JsonNode>) vals[0];
			list.add((JsonNode) vals[2]);
			return list;
		});
		
		return g;
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
		
		// Parse using the grammar
		Grammar.ParseResult result = grammar.parse(tokens);
		
		if (result.isFailure()) {
			throw new JsonParseException(result.getError());
		}
		
		Object value = result.getValue();
		if (!(value instanceof JsonNode)) {
			throw new JsonParseException("Grammar returned unexpected type: " + 
				(value == null ? "null" : value.getClass().getName()));
		}
		return (JsonNode) value;
	}
	
	/**
	 * Get the grammar in bison/yacc format string
	 */
	public String getGrammarString() {
		return grammar.toGrammarString();
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
