package com.github.aaditmshah;

import java.util.List;

/**
 * Grammar interface - abstraction layer for different parsing styles.
 * 
 * <p>This interface can be implemented by different grammar/parser styles:</p>
 * <ul>
 *   <li>{@link ParsecGrammar} - Parsec-style parser combinators</li>
 *   <li>{@link BisonGrammar} - Bison/yacc-style production rules</li>
 * </ul>
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * // Using Parsec-style
 * Grammar parsecGrammar = new ParsecGrammar();
 * // ... define parsers using combinators
 * 
 * // Using Bison-style
 * Grammar bisonGrammar = new BisonGrammar();
 * // ... define production rules
 * 
 * // Both can be used interchangeably
 * Grammar.ParseResult result = grammar.parse(tokens);
 * }</pre>
 */
public interface Grammar {
	
	/**
	 * Parse result containing value or error
	 */
	public static class ParseResult {
		private final Object value;
		private final boolean success;
		private final String error;
		private final int errorPosition;
		
		protected ParseResult(Object value, boolean success, String error, int errorPosition) {
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
	
	/**
	 * Parse a list of tokens using this grammar
	 * 
	 * @param tokens List of tokens from the lexer
	 * @return Parse result containing the parsed value or error
	 */
	ParseResult parse(List<Lexer.Token> tokens);
	
	/**
	 * Get the grammar description in a human-readable format
	 * 
	 * @return Grammar description string
	 */
	String toGrammarString();
	
	/**
	 * Get the name/type of this grammar implementation
	 * 
	 * @return Grammar type name (e.g., "Parsec", "Bison")
	 */
	String getGrammarType();
}
