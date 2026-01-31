package com.github.aaditmshah;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.example.Util;
import com.example.sharp.CString;
import com.example.sharp.Delegates;
import com.example.sharp.Dictionary;
import com.example.sharp.Tracer;

/**
 * A flex-like Lexer for Java that allows defining lexical rules using regex patterns and lambda actions.
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * Lexer lexer = new Lexer();
 * lexer.addRule("\\d+", (lex, match) -> {
 *     return "NUMBER";
 * });
 * lexer.addRule("[a-zA-Z_][a-zA-Z0-9_]*", (lex, match) -> {
 *     return "IDENTIFIER";
 * });
 * lexer.addRule("\\s+", (lex, match) -> {
 *     return null; // skip whitespace
 * });
 * 
 * lexer.setInput("hello 123 world");
 * Token token;
 * while ((token = lexer.nextToken()) != null) {
 *     System.out.println(token);
 * }
 * }</pre>
 */
public class Lexer {
	
	public static final int EOF=0;
	public static final int STATE_INITIAL=0;
	public static final int STATE_ANY=-1;
	public static final String RULE_EOF="<<EOF>>";
	
	/**
	 * Token class representing a lexical token with type and value
	 */
	public static class Token {
		public final String type;
		public final String value;
		public final int start;
		public final int end;
		public final int line;
		public final int column;
		
		public Token(String type, String value, int start, int end, int line, int column) {
			this.type = type;
			this.value = value;
			this.start = start;
			this.end = end;
			this.line = line;
			this.column = column;
		}
		
		@Override
		public String toString() {
			return "Token{type='" + type + "', value='" + value + "', line=" + line + ", column=" + column + "}";
		}
	}
	
	public static class State{
		public String name;
		public boolean exclusive;
		public State(String name,boolean exclusive) {
			this.name=name;
			this.exclusive=exclusive;
		}
	}
	
	private String input = "";
	private int index = 0;
	private int line = 1;
	private int column = 1;
	private int state = Lexer.STATE_INITIAL;
	private boolean error = false;
	private boolean debugEnabled = false;
	private boolean ignoreCase = false;
	
	private Vector<String> stateStack = new Vector<>();
	private Dictionary<String, String> definitions = new Dictionary<>();
	private Dictionary<String, State> states = new Dictionary<>();
	private Vector<RuleObject> rules = new Vector<>();
	
	Delegates.Func1<Character, String> defunctAction;
	
	// Current match information
	private String currentMatch = "";
	private Token lastToken = null;
	
	public Lexer() {
	}
	
	/**
	 * Reset the lexer state for reuse
	 */
	public void reset() {
		index = 0;
		line = 1;
		column = 1;
		state = Lexer.STATE_INITIAL;
		error = false;
		stateStack = new Vector<>();
		currentMatch = "";
		lastToken = null;
	}
	
	/**
	 * Clear all rules and reset state
	 */
	public void clear() {
		states = new Dictionary<>();
		definitions = new Dictionary<>();
		rules = new Vector<>();
		this.ignoreCase = false;
		this.debugEnabled = false;
		this.reset();
	}
	
	/**
	 * Handle unexpected character
	 */
	public String defunct(char chr) {
		if (defunctAction != null) {
			try {
				return defunctAction.Invoke(chr);
			} catch (Exception ee) {
				Tracer.D(ee);
			}
		}
		Tracer.D("Unexpected character at index " + (this.index - 1) + ": " + chr);
		error = true;
		return null;
	}
	
	/**
	 * Set the defunct action handler for unexpected characters
	 */
	public Lexer setDefunctAction(Delegates.Func1<Character, String> action) {
		this.defunctAction = action;
		return this;
	}
	
	public void setIgnoreCase(boolean ignoreCase) {
		this.ignoreCase = ignoreCase;
	}
	
	public void setDebugEnabled(boolean debugEnabled) {
		this.debugEnabled = debugEnabled;
	}
	
	public void addState(String name, boolean exclusive) {
		this.states.set(name, new State(name, exclusive));
	}
	
	public void addDefinition(String name, String expression) {
		this.definitions.set(name, expression);
	}
	
	/**
	 * Add a lexer rule with pattern, action, and optional start states
	 * 
	 * @param pattern Regular expression pattern
	 * @param action Lambda action: (lexer, match) -> tokenType
	 * @param start Optional start states
	 * @return this lexer for chaining
	 */
	public Lexer addRule(String pattern, Delegates.Func2<Lexer, MatchResult, String> action, Vector<Integer> start) {
		int flags = 0;
		if (ignoreCase) {
			flags |= Pattern.CASE_INSENSITIVE;
		}
		PatternObject compiledPattern = new PatternObject(pattern, flags);
		if (start == null) {
			start = Delegates.toVector(new Integer[] { STATE_INITIAL });
		}
		rules.add(new RuleObject(compiledPattern, pattern, action, start));
		return this;
	}
	
	/**
	 * Add a lexer rule with pattern and action (default start state)
	 * 
	 * @param pattern Regular expression pattern
	 * @param action Lambda action: (lexer, match) -> tokenType
	 * @return this lexer for chaining
	 */
	public Lexer addRule(String pattern, Delegates.Func2<Lexer, MatchResult, String> action) {
		Vector<Integer> defaultStart = Delegates.toVector(new Integer[] { STATE_INITIAL });
		return addRule(pattern, action, defaultStart);
	}
	
	/**
	 * Set input string and reset position
	 * 
	 * @param input Input string to tokenize
	 * @return this lexer for chaining
	 */
	public Lexer setInput(String input) {
		this.input = input;
		this.index = 0;
		this.line = 1;
		this.column = 1;
		this.error = false;
		this.lastToken = null;
		return this;
	}
	
	/**
	 * Get the current input string
	 */
	public String getInput() {
		return input;
	}
	
	/**
	 * Get the current position in the input
	 */
	public int getIndex() {
		return index;
	}
	
	/**
	 * Get the current line number
	 */
	public int getLine() {
		return line;
	}
	
	/**
	 * Get the current column number
	 */
	public int getColumn() {
		return column;
	}
	
	/**
	 * Check if lexer has encountered an error
	 */
	public boolean hasError() {
		return error;
	}
	
	/**
	 * Get the last matched text
	 */
	public String getMatch() {
		return currentMatch;
	}
	
	/**
	 * Get the last token
	 */
	public Token getLastToken() {
		return lastToken;
	}
	
	/**
	 * Push current state and switch to new state
	 */
	public void pushState(int newState) {
		stateStack.add(String.valueOf(state));
		state = newState;
	}
	
	/**
	 * Pop and restore previous state
	 */
	public void popState() {
		if (!stateStack.isEmpty()) {
			state = Integer.parseInt(stateStack.remove(stateStack.size() - 1));
		}
	}
	
	/**
	 * Set the current state
	 */
	public void setState(int newState) {
		state = newState;
	}
	
	/**
	 * Get the next token from the input
	 * 
	 * @return Token or null if end of input or error
	 */
	public Token nextToken() {
		while (index < input.length()) {
			if (error) {
				return null;
			}
			
			int startIndex = index;
			int startLine = line;
			int startColumn = column;
			
			// Find the best matching rule (longest match)
			MatchResultObject bestMatch = null;
			
			for (RuleObject rule : rules) {
				Vector<Integer> startStates = rule.start;
				
				// Check if rule applies in current state
				boolean applies = false;
				if (startStates.isEmpty() || startStates.contains(STATE_ANY)) {
					applies = true;
				} else if (startStates.contains(state)) {
					applies = true;
				} else if (startStates.contains(STATE_INITIAL) && state == STATE_INITIAL) {
					applies = true;
				}
				
				if (applies) {
					Matcher matcher = rule.pattern.pattern.matcher(input);
					matcher.region(index, input.length());
					
					if (matcher.lookingAt()) {
						int matchLength = matcher.end() - matcher.start();
						if (bestMatch == null || matchLength > bestMatch.length) {
							bestMatch = new MatchResultObject(matcher.toMatchResult(), rule.action, matchLength);
						}
					}
				}
			}
			
			if (bestMatch != null) {
				currentMatch = bestMatch.result.group();
				index = bestMatch.result.end();
				
				// Update line and column
				updatePosition(currentMatch);
				
				// Execute the action
				String tokenType = null;
				try {
					tokenType = bestMatch.action.Invoke(this, bestMatch.result);
				} catch (Exception e) {
					Tracer.D(e);
					error = true;
					return null;
				}
				
				if (tokenType != null) {
					lastToken = new Token(tokenType, currentMatch, startIndex, index, startLine, startColumn);
					return lastToken;
				}
				// If tokenType is null, skip this match and continue
			} else {
				// No rule matched, call defunct handler
				char c = input.charAt(index);
				index++;
				updatePosition(String.valueOf(c));
				String tokenType = defunct(c);
				if (tokenType != null) {
					lastToken = new Token(tokenType, String.valueOf(c), startIndex, index, startLine, startColumn);
					return lastToken;
				}
				if (error) {
					return null;
				}
			}
		}
		
		return null; // End of input
	}
	
	/**
	 * Legacy lex() method for backward compatibility
	 */
	public String lex() {
		Token token = nextToken();
		return token != null ? token.type : null;
	}
	
	/**
	 * Tokenize the entire input and return a list of tokens
	 * 
	 * @return List of tokens
	 */
	public List<Token> tokenize() {
		List<Token> tokens = new ArrayList<>();
		Token token;
		while ((token = nextToken()) != null) {
			tokens.add(token);
		}
		return tokens;
	}
	
	/**
	 * Update line and column position based on matched text
	 */
	private void updatePosition(String text) {
		for (char c : text.toCharArray()) {
			if (c == '\n') {
				line++;
				column = 1;
			} else {
				column++;
			}
		}
	}
	
	/**
	 * Peek at the next character without consuming
	 */
	public char peek() {
		if (index < input.length()) {
			return input.charAt(index);
		}
		return '\0';
	}
	
	/**
	 * Check if there is more input to process
	 */
	public boolean hasMore() {
		return index < input.length();
	}
}

class PatternObject {
	public Pattern pattern;
	public int lastIndex;
	public String global;
	
	public PatternObject(String regex) {
		this.pattern = Pattern.compile(regex);
		this.global = regex;
	}
	
	public PatternObject(String regex, int flags) {
		this.pattern = Pattern.compile(regex, flags);
		this.global = regex;
	}
	
	public PatternObject(Pattern pattern, String global) {
		this.pattern = pattern;
		this.global = global;
	}
	
	public PatternObject() {
	}
}

class MatchResultObject {
	public MatchResult result;
	public Delegates.Func2<Lexer, MatchResult, String> action;
	public int length;
	
	public MatchResultObject() {
	}
	
	public MatchResultObject(MatchResult result, Delegates.Func2<Lexer, MatchResult, String> action, int length) {
		this.result = result;
		this.action = action;
		this.length = length;
	}
}

class RuleObject {
	public PatternObject pattern;
	public String global;
	public Delegates.Func2<Lexer, MatchResult, String> action;
	public Vector<Integer> start;
	
	public RuleObject(PatternObject pattern, String global, Delegates.Func2<Lexer, MatchResult, String> action, Vector<Integer> start) {
		this.pattern = pattern;
		this.global = global;
		this.action = action;
		this.start = start;
	}
	
	public RuleObject() {
	}
}
