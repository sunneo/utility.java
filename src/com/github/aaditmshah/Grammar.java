package com.github.aaditmshah;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;

import com.example.sharp.Delegates;

/**
 * A bison/byacc-like grammar parser for Java that allows defining grammar rules
 * with semantic actions using lambdas.
 * 
 * <p>This class provides a way to define grammar rules similar to bison/yacc:</p>
 * <ul>
 *   <li>%token declarations for terminal symbols</li>
 *   <li>%left, %right, %nonassoc for operator precedence</li>
 *   <li>Production rules with semantic actions</li>
 * </ul>
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * Grammar grammar = new Grammar();
 * 
 * // Define tokens (terminals)
 * grammar.token("NUMBER", "PLUS", "MINUS", "MULTIPLY", "DIVIDE", "LPAREN", "RPAREN");
 * 
 * // Define precedence (lower = lower precedence)
 * grammar.left(1, "PLUS", "MINUS");
 * grammar.left(2, "MULTIPLY", "DIVIDE");
 * 
 * // Define grammar rules with semantic actions
 * grammar.rule("expr", symbols("expr", "PLUS", "expr"), (vals) -> {
 *     return ((Integer)vals[0]) + ((Integer)vals[2]);
 * });
 * 
 * grammar.rule("expr", symbols("expr", "MINUS", "expr"), (vals) -> {
 *     return ((Integer)vals[0]) - ((Integer)vals[2]);
 * });
 * 
 * grammar.rule("expr", symbols("NUMBER"), (vals) -> {
 *     Lexer.Token tok = (Lexer.Token) vals[0];
 *     return Integer.parseInt(tok.value);
 * });
 * 
 * // Set start symbol and parse
 * grammar.start("expr");
 * Object result = grammar.parse(tokens);
 * }</pre>
 */
public class Grammar {
	
	/**
	 * Associativity types for operator precedence
	 */
	public enum Associativity {
		LEFT,
		RIGHT,
		NONASSOC
	}
	
	/**
	 * Represents a grammar symbol (terminal or non-terminal)
	 */
	public static class Symbol {
		public final String name;
		public final boolean isTerminal;
		
		public Symbol(String name, boolean isTerminal) {
			this.name = name;
			this.isTerminal = isTerminal;
		}
		
		@Override
		public String toString() {
			return name;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Symbol) {
				return name.equals(((Symbol) obj).name);
			}
			return false;
		}
		
		@Override
		public int hashCode() {
			return name.hashCode();
		}
	}
	
	/**
	 * Represents a production rule with semantic action
	 */
	public static class Production {
		public final String lhs;           // Left-hand side (non-terminal)
		public final String[] rhs;         // Right-hand side symbols
		public final Delegates.Func1<Object[], Object> action;  // Semantic action
		public final int precedence;       // Precedence level (0 = default)
		
		public Production(String lhs, String[] rhs, Delegates.Func1<Object[], Object> action) {
			this(lhs, rhs, action, 0);
		}
		
		public Production(String lhs, String[] rhs, Delegates.Func1<Object[], Object> action, int precedence) {
			this.lhs = lhs;
			this.rhs = rhs;
			this.action = action;
			this.precedence = precedence;
		}
		
		@Override
		public String toString() {
			return lhs + " -> " + String.join(" ", rhs);
		}
	}
	
	/**
	 * Represents precedence information for a token
	 */
	public static class PrecedenceInfo {
		public final int level;
		public final Associativity associativity;
		
		public PrecedenceInfo(int level, Associativity associativity) {
			this.level = level;
			this.associativity = associativity;
		}
	}
	
	/**
	 * Parse result containing value or error
	 */
	public static class ParseResult {
		private final Object value;
		private final boolean success;
		private final String error;
		private final int errorPosition;
		
		private ParseResult(Object value, boolean success, String error, int errorPosition) {
			this.value = value;
			this.success = success;
			this.error = error;
			this.errorPosition = errorPosition;
		}
		
		public static ParseResult success(Object value) {
			return new ParseResult(value, true, null, -1);
		}
		
		public static ParseResult failure(String error, int position) {
			return new ParseResult(null, false, error, position);
		}
		
		public boolean isSuccess() { return success; }
		public boolean isFailure() { return !success; }
		public Object getValue() { return value; }
		public String getError() { return error; }
		public int getErrorPosition() { return errorPosition; }
		
		@Override
		public String toString() {
			if (success) {
				return "Success(" + value + ")";
			} else {
				return "Failure(" + error + " at position " + errorPosition + ")";
			}
		}
	}
	
	// Grammar definitions
	private final Map<String, Boolean> terminals = new HashMap<>();
	private final Map<String, PrecedenceInfo> precedence = new HashMap<>();
	private final List<Production> productions = new ArrayList<>();
	private final Map<String, List<Production>> productionsByLhs = new HashMap<>();
	private String startSymbol = null;
	
	// Parser state
	private List<Lexer.Token> tokens;
	private int position;
	private final Map<String, Map<Integer, Object>> memoTable = new HashMap<>();
	
	public Grammar() {
	}
	
	/**
	 * Declare terminal symbols (tokens)
	 * Equivalent to: %token TOKEN1 TOKEN2 ...
	 */
	public Grammar token(String... tokenNames) {
		for (String name : tokenNames) {
			terminals.put(name, true);
		}
		return this;
	}
	
	/**
	 * Declare left-associative operators with precedence level
	 * Equivalent to: %left TOKEN1 TOKEN2 ...
	 */
	public Grammar left(int level, String... tokenNames) {
		for (String name : tokenNames) {
			precedence.put(name, new PrecedenceInfo(level, Associativity.LEFT));
		}
		return this;
	}
	
	/**
	 * Declare right-associative operators with precedence level
	 * Equivalent to: %right TOKEN1 TOKEN2 ...
	 */
	public Grammar right(int level, String... tokenNames) {
		for (String name : tokenNames) {
			precedence.put(name, new PrecedenceInfo(level, Associativity.RIGHT));
		}
		return this;
	}
	
	/**
	 * Declare non-associative operators with precedence level
	 * Equivalent to: %nonassoc TOKEN1 TOKEN2 ...
	 */
	public Grammar nonassoc(int level, String... tokenNames) {
		for (String name : tokenNames) {
			precedence.put(name, new PrecedenceInfo(level, Associativity.NONASSOC));
		}
		return this;
	}
	
	/**
	 * Helper method to create symbol array for rule definition
	 */
	public static String[] symbols(String... symbols) {
		return symbols;
	}
	
	/**
	 * Add a production rule with semantic action
	 * Equivalent to: lhs : rhs1 rhs2 ... { action }
	 * 
	 * @param lhs Left-hand side non-terminal
	 * @param rhs Right-hand side symbols
	 * @param action Semantic action: (Object[] values) -> result
	 * @return this grammar for chaining
	 */
	public Grammar rule(String lhs, String[] rhs, Delegates.Func1<Object[], Object> action) {
		Production prod = new Production(lhs, rhs, action);
		productions.add(prod);
		productionsByLhs.computeIfAbsent(lhs, k -> new ArrayList<>()).add(prod);
		return this;
	}
	
	/**
	 * Add a production rule without semantic action (returns first symbol value)
	 */
	public Grammar rule(String lhs, String[] rhs) {
		return rule(lhs, rhs, vals -> vals.length > 0 ? vals[0] : null);
	}
	
	/**
	 * Add an empty production (epsilon rule)
	 * Equivalent to: lhs : /* empty */
	 */
	public Grammar empty(String lhs, Delegates.Func1<Object[], Object> action) {
		return rule(lhs, new String[0], action);
	}
	
	/**
	 * Add an empty production that returns null
	 */
	public Grammar empty(String lhs) {
		return empty(lhs, vals -> null);
	}
	
	/**
	 * Set the start symbol
	 * Equivalent to: %start symbol
	 */
	public Grammar start(String symbol) {
		this.startSymbol = symbol;
		return this;
	}
	
	/**
	 * Check if a symbol is a terminal
	 */
	public boolean isTerminal(String symbol) {
		return terminals.containsKey(symbol);
	}
	
	/**
	 * Get the precedence of a symbol
	 */
	public PrecedenceInfo getPrecedence(String symbol) {
		return precedence.get(symbol);
	}
	
	/**
	 * Parse the token stream using the defined grammar
	 * Uses a recursive descent parser with memoization (Packrat parsing)
	 * 
	 * @param tokenList List of tokens from the lexer
	 * @return Parse result
	 */
	public ParseResult parse(List<Lexer.Token> tokenList) {
		if (startSymbol == null) {
			if (productionsByLhs.isEmpty()) {
				return ParseResult.failure("No grammar rules defined", 0);
			}
			// Use first production's LHS as start symbol
			startSymbol = productions.get(0).lhs;
		}
		
		this.tokens = tokenList;
		this.position = 0;
		this.memoTable.clear();
		
		try {
			Object result = parseSymbol(startSymbol);
			
			// Check if all input consumed
			if (position < tokens.size()) {
				Lexer.Token remaining = tokens.get(position);
				return ParseResult.failure("Unexpected token: " + remaining.type + " at line " + remaining.line, position);
			}
			
			return ParseResult.success(result);
		} catch (ParseException e) {
			return ParseResult.failure(e.getMessage(), e.position);
		}
	}
	
	/**
	 * Parse a symbol (terminal or non-terminal)
	 */
	private Object parseSymbol(String symbol) throws ParseException {
		if (isTerminal(symbol)) {
			return parseTerminal(symbol);
		} else {
			return parseNonTerminal(symbol);
		}
	}
	
	/**
	 * Parse a terminal symbol (match a token)
	 */
	private Lexer.Token parseTerminal(String symbol) throws ParseException {
		if (position >= tokens.size()) {
			throw new ParseException("Expected " + symbol + " but reached end of input", position);
		}
		
		Lexer.Token token = tokens.get(position);
		if (token.type.equals(symbol)) {
			position++;
			return token;
		}
		
		throw new ParseException("Expected " + symbol + " but got " + token.type + " at line " + token.line, position);
	}
	
	/**
	 * Parse a non-terminal symbol by trying all applicable productions
	 */
	private Object parseNonTerminal(String symbol) throws ParseException {
		List<Production> prods = productionsByLhs.get(symbol);
		if (prods == null || prods.isEmpty()) {
			throw new ParseException("No production rules for: " + symbol, position);
		}
		
		// Check memoization
		String memoKey = symbol;
		if (memoTable.containsKey(memoKey) && memoTable.get(memoKey).containsKey(position)) {
			Object cached = memoTable.get(memoKey).get(position);
			if (cached instanceof MemoFailure) {
				throw new ParseException(((MemoFailure) cached).message, position);
			}
			return cached;
		}
		
		int startPos = position;
		ParseException lastError = null;
		
		// Try each production in order
		for (Production prod : prods) {
			position = startPos;  // Reset position for each alternative
			
			try {
				Object result = tryProduction(prod);
				
				// Memoize success
				memoTable.computeIfAbsent(memoKey, k -> new HashMap<>()).put(startPos, result);
				return result;
			} catch (ParseException e) {
				lastError = e;
				// Continue to next alternative
			}
		}
		
		// All alternatives failed
		if (lastError != null) {
			// Memoize failure
			memoTable.computeIfAbsent(memoKey, k -> new HashMap<>())
				.put(startPos, new MemoFailure(lastError.getMessage()));
			throw lastError;
		}
		
		throw new ParseException("Failed to parse: " + symbol, position);
	}
	
	/**
	 * Try to apply a single production rule
	 */
	private Object tryProduction(Production prod) throws ParseException {
		Object[] values = new Object[prod.rhs.length];
		
		// Parse each symbol in the RHS
		for (int i = 0; i < prod.rhs.length; i++) {
			values[i] = parseSymbol(prod.rhs[i]);
		}
		
		// Execute semantic action
		if (prod.action != null) {
			try {
				return prod.action.Invoke(values);
			} catch (Exception e) {
				throw new ParseException("Semantic action error: " + e.getMessage(), position);
			}
		}
		
		return values.length > 0 ? values[0] : null;
	}
	
	/**
	 * Get all productions for a non-terminal
	 */
	public List<Production> getProductions(String lhs) {
		return productionsByLhs.getOrDefault(lhs, new ArrayList<>());
	}
	
	/**
	 * Get all productions
	 */
	public List<Production> getAllProductions() {
		return new ArrayList<>(productions);
	}
	
	/**
	 * Get all terminal symbols
	 */
	public List<String> getTerminals() {
		return new ArrayList<>(terminals.keySet());
	}
	
	/**
	 * Get all non-terminal symbols
	 */
	public List<String> getNonTerminals() {
		return new ArrayList<>(productionsByLhs.keySet());
	}
	
	/**
	 * Generate a string representation of the grammar
	 */
	public String toGrammarString() {
		StringBuilder sb = new StringBuilder();
		
		// Tokens
		if (!terminals.isEmpty()) {
			sb.append("%token ");
			sb.append(String.join(" ", terminals.keySet()));
			sb.append("\n\n");
		}
		
		// Precedence
		Map<Integer, List<String>> leftPrec = new HashMap<>();
		Map<Integer, List<String>> rightPrec = new HashMap<>();
		Map<Integer, List<String>> nonassocPrec = new HashMap<>();
		
		for (Map.Entry<String, PrecedenceInfo> entry : precedence.entrySet()) {
			PrecedenceInfo info = entry.getValue();
			Map<Integer, List<String>> target;
			switch (info.associativity) {
				case LEFT: target = leftPrec; break;
				case RIGHT: target = rightPrec; break;
				default: target = nonassocPrec; break;
			}
			target.computeIfAbsent(info.level, k -> new ArrayList<>()).add(entry.getKey());
		}
		
		for (int level : leftPrec.keySet()) {
			sb.append("%left ");
			sb.append(String.join(" ", leftPrec.get(level)));
			sb.append("\n");
		}
		for (int level : rightPrec.keySet()) {
			sb.append("%right ");
			sb.append(String.join(" ", rightPrec.get(level)));
			sb.append("\n");
		}
		for (int level : nonassocPrec.keySet()) {
			sb.append("%nonassoc ");
			sb.append(String.join(" ", nonassocPrec.get(level)));
			sb.append("\n");
		}
		
		if (!precedence.isEmpty()) {
			sb.append("\n");
		}
		
		// Start symbol
		if (startSymbol != null) {
			sb.append("%start ").append(startSymbol).append("\n\n");
		}
		
		// Productions
		sb.append("%%\n\n");
		
		for (String lhs : productionsByLhs.keySet()) {
			List<Production> prods = productionsByLhs.get(lhs);
			sb.append(lhs).append("\n");
			
			for (int i = 0; i < prods.size(); i++) {
				Production prod = prods.get(i);
				if (i == 0) {
					sb.append("    : ");
				} else {
					sb.append("    | ");
				}
				
				if (prod.rhs.length == 0) {
					sb.append("/* empty */");
				} else {
					sb.append(String.join(" ", prod.rhs));
				}
				
				sb.append(" { /* action */ }");
				sb.append("\n");
			}
			sb.append("    ;\n\n");
		}
		
		sb.append("%%\n");
		
		return sb.toString();
	}
	
	/**
	 * Internal exception for parse errors
	 */
	private static class ParseException extends Exception {
		final int position;
		
		ParseException(String message, int position) {
			super(message);
			this.position = position;
		}
	}
	
	/**
	 * Internal class for memoization of failures
	 */
	private static class MemoFailure {
		final String message;
		
		MemoFailure(String message) {
			this.message = message;
		}
	}
}
