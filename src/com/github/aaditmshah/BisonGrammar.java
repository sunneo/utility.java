package com.github.aaditmshah;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.sharp.Delegates;

/**
 * Bison/yacc-style grammar parser for Java that allows defining grammar rules
 * with semantic actions using lambdas.
 * 
 * <p>This class provides a way to define grammar rules similar to bison/yacc:</p>
 * <ul>
 *   <li>%token declarations for terminal symbols</li>
 *   <li>%left, %right, %nonassoc for operator precedence</li>
 *   <li>Production rules with semantic actions</li>
 *   <li>Recursive rule definitions</li>
 * </ul>
 * 
 * <p>It implements the {@link Grammar} interface for interoperability with 
 * other grammar styles like {@link ParsecGrammar}.</p>
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * BisonGrammar grammar = new BisonGrammar();
 * 
 * // Define tokens (terminals) - equivalent to: %token NUMBER PLUS MINUS
 * grammar.token("NUMBER", "PLUS", "MINUS", "MULTIPLY", "DIVIDE", "LPAREN", "RPAREN");
 * 
 * // Define precedence (lower = lower precedence)
 * // Equivalent to: %left PLUS MINUS
 * //                %left MULTIPLY DIVIDE
 * grammar.left(1, "PLUS", "MINUS");
 * grammar.left(2, "MULTIPLY", "DIVIDE");
 * 
 * // Define grammar rules with semantic actions
 * // Equivalent to:
 * //   expr : expr PLUS term   { $$ = $1 + $3; }
 * //        | expr MINUS term  { $$ = $1 - $3; }
 * //        | term             { $$ = $1; }
 * //        ;
 * grammar.rule("expr", symbols("expr", "PLUS", "term"), (vals) -> {
 *     return ((Integer)vals[0]) + ((Integer)vals[2]);
 * });
 * 
 * grammar.rule("expr", symbols("expr", "MINUS", "term"), (vals) -> {
 *     return ((Integer)vals[0]) - ((Integer)vals[2]);
 * });
 * 
 * grammar.rule("expr", symbols("term"), (vals) -> vals[0]);
 * 
 * grammar.rule("term", symbols("NUMBER"), (vals) -> {
 *     Lexer.Token tok = (Lexer.Token) vals[0];
 *     return Integer.parseInt(tok.value);
 * });
 * 
 * // Set start symbol and parse
 * grammar.start("expr");
 * Grammar.ParseResult result = grammar.parse(tokens);
 * }</pre>
 */
public class BisonGrammar implements Grammar {
	
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
	
	public BisonGrammar() {
	}
	
	/**
	 * Declare terminal symbols (tokens)
	 * Equivalent to: %token TOKEN1 TOKEN2 ...
	 */
	public BisonGrammar token(String... tokenNames) {
		for (String name : tokenNames) {
			terminals.put(name, true);
		}
		return this;
	}
	
	/**
	 * Declare left-associative operators with precedence level
	 * Equivalent to: %left TOKEN1 TOKEN2 ...
	 */
	public BisonGrammar left(int level, String... tokenNames) {
		for (String name : tokenNames) {
			precedence.put(name, new PrecedenceInfo(level, Associativity.LEFT));
		}
		return this;
	}
	
	/**
	 * Declare right-associative operators with precedence level
	 * Equivalent to: %right TOKEN1 TOKEN2 ...
	 */
	public BisonGrammar right(int level, String... tokenNames) {
		for (String name : tokenNames) {
			precedence.put(name, new PrecedenceInfo(level, Associativity.RIGHT));
		}
		return this;
	}
	
	/**
	 * Declare non-associative operators with precedence level
	 * Equivalent to: %nonassoc TOKEN1 TOKEN2 ...
	 */
	public BisonGrammar nonassoc(int level, String... tokenNames) {
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
	public BisonGrammar rule(String lhs, String[] rhs, Delegates.Func1<Object[], Object> action) {
		Production prod = new Production(lhs, rhs, action);
		productions.add(prod);
		productionsByLhs.computeIfAbsent(lhs, k -> new ArrayList<>()).add(prod);
		return this;
	}
	
	/**
	 * Add a production rule without semantic action (returns first symbol value)
	 */
	public BisonGrammar rule(String lhs, String[] rhs) {
		return rule(lhs, rhs, vals -> vals.length > 0 ? vals[0] : null);
	}
	
	/**
	 * Add an empty production (epsilon rule)
	 * Equivalent to: lhs : (empty) { action }
	 */
	public BisonGrammar empty(String lhs, Delegates.Func1<Object[], Object> action) {
		return rule(lhs, new String[0], action);
	}
	
	/**
	 * Add an empty production that returns null
	 */
	public BisonGrammar empty(String lhs) {
		return empty(lhs, vals -> null);
	}
	
	/**
	 * Set the start symbol
	 * Equivalent to: %start symbol
	 */
	public BisonGrammar start(String symbol) {
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
	
	// ==================== Grammar Interface Implementation ====================
	
	@Override
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
	 * Parse a non-terminal symbol by trying all applicable productions.
	 * Handles left recursion using seed-and-grow approach.
	 */
	private Object parseNonTerminal(String symbol) throws ParseException {
		List<Production> prods = productionsByLhs.get(symbol);
		if (prods == null || prods.isEmpty()) {
			throw new ParseException("No production rules for: " + symbol, position);
		}
		
		int startPos = position;
		String memoKey = symbol + "@" + startPos;
		
		// Check if we're in a left-recursive call
		if (memoTable.containsKey(symbol) && memoTable.get(symbol).containsKey(startPos)) {
			Object cached = memoTable.get(symbol).get(startPos);
			if (cached instanceof MemoFailure) {
				throw new ParseException(((MemoFailure) cached).message, position);
			}
			if (cached instanceof InProgress) {
				// We're in a left-recursive loop - return failure to break the recursion
				throw new ParseException("Left recursion detected for: " + symbol, position);
			}
			// Return memoized result and advance position
			MemoSuccess success = (MemoSuccess) cached;
			position = success.endPosition;
			return success.value;
		}
		
		// Mark as in-progress to detect left recursion
		memoTable.computeIfAbsent(symbol, k -> new HashMap<>()).put(startPos, InProgress.INSTANCE);
		
		// Separate left-recursive and non-left-recursive productions
		List<Production> nonLeftRec = new ArrayList<>();
		List<Production> leftRec = new ArrayList<>();
		
		for (Production prod : prods) {
			if (prod.rhs.length > 0 && prod.rhs[0].equals(symbol)) {
				leftRec.add(prod);
			} else {
				nonLeftRec.add(prod);
			}
		}
		
		// First, try non-left-recursive productions to get a seed
		Object seed = null;
		int seedEndPos = startPos;
		ParseException lastError = null;
		
		for (Production prod : nonLeftRec) {
			position = startPos;
			try {
				seed = tryProduction(prod);
				seedEndPos = position;
				break; // Found a seed
			} catch (ParseException e) {
				lastError = e;
			}
		}
		
		if (seed == null) {
			// No seed found, all alternatives failed
			memoTable.get(symbol).put(startPos, new MemoFailure(lastError != null ? lastError.getMessage() : "No match"));
			throw lastError != null ? lastError : new ParseException("Failed to parse: " + symbol, position);
		}
		
		// If there are no left-recursive rules, just return the seed
		if (leftRec.isEmpty()) {
			memoTable.get(symbol).put(startPos, new MemoSuccess(seed, seedEndPos));
			return seed;
		}
		
		// Grow the seed using left-recursive rules
		Object result = seed;
		int resultEndPos = seedEndPos;
		
		while (true) {
			// Memoize current result so left-recursive calls can find it
			memoTable.get(symbol).put(startPos, new MemoSuccess(result, resultEndPos));
			
			boolean grew = false;
			for (Production prod : leftRec) {
				position = startPos;
				try {
					Object newResult = tryProduction(prod);
					if (position > resultEndPos) {
						// We grew the parse
						result = newResult;
						resultEndPos = position;
						grew = true;
						break; // Start over with left-recursive rules
					}
				} catch (ParseException e) {
					// This left-recursive rule didn't match, try next
				}
			}
			
			if (!grew) {
				break; // No more growth possible
			}
		}
		
		// Final memoization
		memoTable.get(symbol).put(startPos, new MemoSuccess(result, resultEndPos));
		position = resultEndPos;
		return result;
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
	
	@Override
	public String toGrammarString() {
		StringBuilder sb = new StringBuilder();
		
		// Header
		sb.append("/* Bison-style Grammar */\n\n");
		
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
	
	@Override
	public String getGrammarType() {
		return "Bison";
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
	
	/**
	 * Internal class for memoization of successful parses
	 */
	private static class MemoSuccess {
		final Object value;
		final int endPosition;
		
		MemoSuccess(Object value, int endPosition) {
			this.value = value;
			this.endPosition = endPosition;
		}
	}
	
	/**
	 * Internal marker for in-progress parsing (to detect left recursion)
	 */
	private static class InProgress {
		static final InProgress INSTANCE = new InProgress();
		private InProgress() {}
	}
}
