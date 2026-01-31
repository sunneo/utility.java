package com.github.aaditmshah;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.example.sharp.Delegates;

/**
 * Parsec-style parser combinator grammar for Java using lambdas.
 * 
 * <p>This class provides combinators to build parsers that can parse token streams
 * produced by the Lexer and construct AST nodes. It implements the {@link Grammar}
 * interface for interoperability with other grammar styles.</p>
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * ParsecGrammar grammar = new ParsecGrammar();
 * 
 * // Define parsers using combinators
 * Parser<Lexer.Token> number = grammar.token("NUMBER");
 * Parser<Lexer.Token> plus = grammar.token("PLUS");
 * 
 * // Build complex parsers from simpler ones
 * Parser<Integer> expr = grammar.lazy(() ->
 *     grammar.choice(
 *         grammar.sequence(number, plus, expr)
 *             .map(results -> {
 *                 int left = Integer.parseInt(((Lexer.Token)results.get(0)).value);
 *                 int right = (Integer) results.get(2);
 *                 return left + right;
 *             }),
 *         number.map(t -> Integer.parseInt(t.value))
 *     )
 * );
 * 
 * // Set the main parser and parse
 * grammar.setMainParser(expr);
 * Grammar.ParseResult result = grammar.parse(tokens);
 * }</pre>
 */
public class ParsecGrammar implements Grammar {
	
	/**
	 * Parse state tracking position in token stream
	 */
	public static class ParseState {
		private final List<Lexer.Token> tokens;
		private final int position;
		
		public ParseState(List<Lexer.Token> tokens) {
			this(tokens, 0);
		}
		
		public ParseState(List<Lexer.Token> tokens, int position) {
			this.tokens = tokens;
			this.position = position;
		}
		
		public boolean hasMore() {
			return position < tokens.size();
		}
		
		public Lexer.Token current() {
			if (hasMore()) {
				return tokens.get(position);
			}
			return null;
		}
		
		public Lexer.Token peek(int offset) {
			int idx = position + offset;
			if (idx >= 0 && idx < tokens.size()) {
				return tokens.get(idx);
			}
			return null;
		}
		
		public ParseState advance() {
			return new ParseState(tokens, position + 1);
		}
		
		public ParseState advance(int count) {
			return new ParseState(tokens, position + count);
		}
		
		public int getPosition() {
			return position;
		}
		
		public List<Lexer.Token> getTokens() {
			return tokens;
		}
		
		public String getLocationInfo() {
			Lexer.Token token = current();
			if (token != null) {
				return "line " + token.line + ", column " + token.column;
			}
			return "end of input";
		}
	}
	
	/**
	 * Internal parse result for combinators
	 */
	public static class ParserResult<T> {
		private final T value;
		private final ParseState state;
		private final boolean success;
		private final String error;
		
		private ParserResult(T value, ParseState state, boolean success, String error) {
			this.value = value;
			this.state = state;
			this.success = success;
			this.error = error;
		}
		
		public static <T> ParserResult<T> success(T value, ParseState state) {
			return new ParserResult<>(value, state, true, null);
		}
		
		public static <T> ParserResult<T> failure(String error, ParseState state) {
			return new ParserResult<>(null, state, false, error);
		}
		
		public boolean isSuccess() { return success; }
		public boolean isFailure() { return !success; }
		public T getValue() { return value; }
		public ParseState getState() { return state; }
		public String getError() { return error; }
		
		@Override
		public String toString() {
			if (success) {
				return "Success(" + value + ")";
			} else {
				return "Failure(" + error + ")";
			}
		}
	}
	
	/**
	 * Parser functional interface for building parser combinators
	 */
	@FunctionalInterface
	public interface Parser<T> {
		ParserResult<T> parse(ParseState state);
		
		/**
		 * Map the result of this parser to a new type
		 */
		default <R> Parser<R> map(Function<T, R> mapper) {
			return state -> {
				ParserResult<T> result = this.parse(state);
				if (result.isSuccess()) {
					return ParserResult.success(mapper.apply(result.getValue()), result.getState());
				}
				return ParserResult.failure(result.getError(), result.getState());
			};
		}
		
		/**
		 * Chain this parser with another parser that depends on the result
		 */
		default <R> Parser<R> flatMap(Function<T, Parser<R>> mapper) {
			return state -> {
				ParserResult<T> result = this.parse(state);
				if (result.isSuccess()) {
					return mapper.apply(result.getValue()).parse(result.getState());
				}
				return ParserResult.failure(result.getError(), result.getState());
			};
		}
		
		/**
		 * Try this parser, or if it fails, try the alternative
		 */
		default Parser<T> or(Parser<T> alternative) {
			return state -> {
				ParserResult<T> result = this.parse(state);
				if (result.isSuccess()) {
					return result;
				}
				return alternative.parse(state);
			};
		}
		
		/**
		 * Make this parser optional, returning null if it fails
		 */
		default Parser<T> optional() {
			return state -> {
				ParserResult<T> result = this.parse(state);
				if (result.isSuccess()) {
					return result;
				}
				return ParserResult.success(null, state);
			};
		}
	}
	
	// Main parser for the grammar
	private Parser<?> mainParser = null;
	private String grammarDescription = "Parsec-style grammar";
	
	public ParsecGrammar() {
	}
	
	/**
	 * Set the main parser to use for parsing
	 */
	public <T> ParsecGrammar setMainParser(Parser<T> parser) {
		this.mainParser = parser;
		return this;
	}
	
	/**
	 * Set a description for this grammar
	 */
	public ParsecGrammar setDescription(String description) {
		this.grammarDescription = description;
		return this;
	}
	
	/**
	 * Create a parser that matches a specific token type
	 */
	public Parser<Lexer.Token> token(String type) {
		return state -> {
			if (state.hasMore()) {
				Lexer.Token token = state.current();
				if (token.type.equals(type)) {
					return ParserResult.success(token, state.advance());
				}
				return ParserResult.failure("Expected " + type + " but got " + token.type + " at " + state.getLocationInfo(), state);
			}
			return ParserResult.failure("Expected " + type + " but reached end of input", state);
		};
	}
	
	/**
	 * Create a parser that matches a token with specific type and value
	 */
	public Parser<Lexer.Token> token(String type, String value) {
		return state -> {
			if (state.hasMore()) {
				Lexer.Token token = state.current();
				if (token.type.equals(type) && token.value.equals(value)) {
					return ParserResult.success(token, state.advance());
				}
				return ParserResult.failure("Expected " + type + "(" + value + ") but got " + token.type + "(" + token.value + ") at " + state.getLocationInfo(), state);
			}
			return ParserResult.failure("Expected " + type + "(" + value + ") but reached end of input", state);
		};
	}
	
	/**
	 * Create a parser that matches any of the given token types
	 */
	public Parser<Lexer.Token> oneOf(String... types) {
		return state -> {
			if (state.hasMore()) {
				Lexer.Token token = state.current();
				for (String type : types) {
					if (token.type.equals(type)) {
						return ParserResult.success(token, state.advance());
					}
				}
				return ParserResult.failure("Expected one of " + String.join(", ", types) + " but got " + token.type + " at " + state.getLocationInfo(), state);
			}
			return ParserResult.failure("Expected one of " + String.join(", ", types) + " but reached end of input", state);
		};
	}
	
	/**
	 * Create a parser that always succeeds with the given value
	 */
	public <T> Parser<T> pure(T value) {
		return state -> ParserResult.success(value, state);
	}
	
	/**
	 * Create a parser that always fails with the given message
	 */
	public <T> Parser<T> fail(String message) {
		return state -> ParserResult.failure(message, state);
	}
	
	/**
	 * Sequence two parsers and return both results as a pair
	 */
	public <A, B> Parser<List<Object>> sequence(Parser<A> first, Parser<B> second) {
		return state -> {
			ParserResult<A> result1 = first.parse(state);
			if (result1.isFailure()) {
				return ParserResult.failure(result1.getError(), result1.getState());
			}
			ParserResult<B> result2 = second.parse(result1.getState());
			if (result2.isFailure()) {
				return ParserResult.failure(result2.getError(), result2.getState());
			}
			List<Object> list = new ArrayList<>();
			list.add(result1.getValue());
			list.add(result2.getValue());
			return ParserResult.success(list, result2.getState());
		};
	}
	
	/**
	 * Sequence three parsers
	 */
	public <A, B, C> Parser<List<Object>> sequence(Parser<A> p1, Parser<B> p2, Parser<C> p3) {
		return state -> {
			ParserResult<A> r1 = p1.parse(state);
			if (r1.isFailure()) return ParserResult.failure(r1.getError(), r1.getState());
			
			ParserResult<B> r2 = p2.parse(r1.getState());
			if (r2.isFailure()) return ParserResult.failure(r2.getError(), r2.getState());
			
			ParserResult<C> r3 = p3.parse(r2.getState());
			if (r3.isFailure()) return ParserResult.failure(r3.getError(), r3.getState());
			
			List<Object> list = new ArrayList<>();
			list.add(r1.getValue());
			list.add(r2.getValue());
			list.add(r3.getValue());
			return ParserResult.success(list, r3.getState());
		};
	}
	
	/**
	 * Sequence multiple parsers and return all results as a list
	 */
	@SafeVarargs
	public final Parser<List<Object>> sequence(Parser<?>... parsers) {
		return state -> {
			List<Object> results = new ArrayList<>();
			ParseState currentState = state;
			
			for (Parser<?> parser : parsers) {
				ParserResult<?> result = parser.parse(currentState);
				if (result.isFailure()) {
					return ParserResult.failure(result.getError(), result.getState());
				}
				results.add(result.getValue());
				currentState = result.getState();
			}
			
			return ParserResult.success(results, currentState);
		};
	}
	
	/**
	 * Try multiple parsers in order and return the first successful result
	 */
	@SafeVarargs
	public final <T> Parser<T> choice(Parser<T>... parsers) {
		return state -> {
			String lastError = "No parsers provided";
			for (Parser<T> parser : parsers) {
				ParserResult<T> result = parser.parse(state);
				if (result.isSuccess()) {
					return result;
				}
				lastError = result.getError();
			}
			return ParserResult.failure(lastError, state);
		};
	}
	
	/**
	 * Parse zero or more occurrences
	 */
	public <T> Parser<List<T>> many(Parser<T> parser) {
		return state -> {
			List<T> results = new ArrayList<>();
			ParseState currentState = state;
			
			while (true) {
				ParserResult<T> result = parser.parse(currentState);
				if (result.isFailure()) {
					break;
				}
				results.add(result.getValue());
				currentState = result.getState();
			}
			
			return ParserResult.success(results, currentState);
		};
	}
	
	/**
	 * Parse one or more occurrences
	 */
	public <T> Parser<List<T>> many1(Parser<T> parser) {
		return state -> {
			ParserResult<T> first = parser.parse(state);
			if (first.isFailure()) {
				return ParserResult.failure(first.getError(), first.getState());
			}
			
			List<T> results = new ArrayList<>();
			results.add(first.getValue());
			ParseState currentState = first.getState();
			
			while (true) {
				ParserResult<T> result = parser.parse(currentState);
				if (result.isFailure()) {
					break;
				}
				results.add(result.getValue());
				currentState = result.getState();
			}
			
			return ParserResult.success(results, currentState);
		};
	}
	
	/**
	 * Parse items separated by a separator
	 */
	public <T, S> Parser<List<T>> sepBy(Parser<T> item, Parser<S> separator) {
		return state -> {
			List<T> results = new ArrayList<>();
			
			ParserResult<T> first = item.parse(state);
			if (first.isFailure()) {
				return ParserResult.success(results, state); // Empty list is OK
			}
			
			results.add(first.getValue());
			ParseState currentState = first.getState();
			
			while (true) {
				ParserResult<S> sepResult = separator.parse(currentState);
				if (sepResult.isFailure()) {
					break;
				}
				
				ParserResult<T> itemResult = item.parse(sepResult.getState());
				if (itemResult.isFailure()) {
					break;
				}
				
				results.add(itemResult.getValue());
				currentState = itemResult.getState();
			}
			
			return ParserResult.success(results, currentState);
		};
	}
	
	/**
	 * Parse items separated by a separator, requiring at least one item
	 */
	public <T, S> Parser<List<T>> sepBy1(Parser<T> item, Parser<S> separator) {
		return state -> {
			List<T> results = new ArrayList<>();
			
			ParserResult<T> first = item.parse(state);
			if (first.isFailure()) {
				return ParserResult.failure(first.getError(), first.getState());
			}
			
			results.add(first.getValue());
			ParseState currentState = first.getState();
			
			while (true) {
				ParserResult<S> sepResult = separator.parse(currentState);
				if (sepResult.isFailure()) {
					break;
				}
				
				ParserResult<T> itemResult = item.parse(sepResult.getState());
				if (itemResult.isFailure()) {
					break;
				}
				
				results.add(itemResult.getValue());
				currentState = itemResult.getState();
			}
			
			return ParserResult.success(results, currentState);
		};
	}
	
	/**
	 * Parse content between delimiters
	 */
	public <L, T, R> Parser<T> between(Parser<L> left, Parser<T> content, Parser<R> right) {
		return state -> {
			ParserResult<L> leftResult = left.parse(state);
			if (leftResult.isFailure()) {
				return ParserResult.failure(leftResult.getError(), leftResult.getState());
			}
			
			ParserResult<T> contentResult = content.parse(leftResult.getState());
			if (contentResult.isFailure()) {
				return ParserResult.failure(contentResult.getError(), contentResult.getState());
			}
			
			ParserResult<R> rightResult = right.parse(contentResult.getState());
			if (rightResult.isFailure()) {
				return ParserResult.failure(rightResult.getError(), rightResult.getState());
			}
			
			return ParserResult.success(contentResult.getValue(), rightResult.getState());
		};
	}
	
	/**
	 * Optionally parse, returning default value on failure
	 */
	public <T> Parser<T> optional(Parser<T> parser, T defaultValue) {
		return state -> {
			ParserResult<T> result = parser.parse(state);
			if (result.isSuccess()) {
				return result;
			}
			return ParserResult.success(defaultValue, state);
		};
	}
	
	/**
	 * Look ahead without consuming input
	 */
	public <T> Parser<T> lookAhead(Parser<T> parser) {
		return state -> {
			ParserResult<T> result = parser.parse(state);
			if (result.isSuccess()) {
				return ParserResult.success(result.getValue(), state); // Don't advance
			}
			return result;
		};
	}
	
	/**
	 * Negative look ahead - succeed if parser fails, fail if it succeeds
	 */
	public <T> Parser<Void> notFollowedBy(Parser<T> parser) {
		return state -> {
			ParserResult<T> result = parser.parse(state);
			if (result.isSuccess()) {
				return ParserResult.failure("Unexpected match", state);
			}
			return ParserResult.success(null, state);
		};
	}
	
	/**
	 * Parse and apply a semantic action
	 */
	public <T, R> Parser<R> action(Parser<T> parser, Delegates.Func1<T, R> handler) {
		return parser.map(handler::Invoke);
	}
	
	/**
	 * Create a lazy parser (for recursive grammars)
	 * This is essential for defining recursive rules in Parsec-style
	 */
	public <T> Parser<T> lazy(Delegates.Func<Parser<T>> parserSupplier) {
		return state -> parserSupplier.Invoke().parse(state);
	}
	
	/**
	 * End of input parser
	 */
	public Parser<Void> eof() {
		return state -> {
			if (!state.hasMore()) {
				return ParserResult.success(null, state);
			}
			return ParserResult.failure("Expected end of input but got " + state.current().type, state);
		};
	}
	
	/**
	 * Label a parser with an expected name for better error messages
	 */
	public <T> Parser<T> label(Parser<T> parser, String expected) {
		return state -> {
			ParserResult<T> result = parser.parse(state);
			if (result.isFailure()) {
				return ParserResult.failure("Expected " + expected + " at " + state.getLocationInfo(), state);
			}
			return result;
		};
	}
	
	/**
	 * Skip parser - parse but return null
	 */
	public <T> Parser<Void> skip(Parser<T> parser) {
		return parser.map(v -> null);
	}
	
	/**
	 * Parse first, skip second (use first result)
	 */
	public <T, S> Parser<T> left(Parser<T> first, Parser<S> second) {
		return state -> {
			ParserResult<T> r1 = first.parse(state);
			if (r1.isFailure()) return ParserResult.failure(r1.getError(), r1.getState());
			
			ParserResult<S> r2 = second.parse(r1.getState());
			if (r2.isFailure()) return ParserResult.failure(r2.getError(), r2.getState());
			
			return ParserResult.success(r1.getValue(), r2.getState());
		};
	}
	
	/**
	 * Skip first, parse second (use second result)
	 */
	public <T, S> Parser<S> right(Parser<T> first, Parser<S> second) {
		return state -> {
			ParserResult<T> r1 = first.parse(state);
			if (r1.isFailure()) return ParserResult.failure(r1.getError(), r1.getState());
			
			ParserResult<S> r2 = second.parse(r1.getState());
			if (r2.isFailure()) return ParserResult.failure(r2.getError(), r2.getState());
			
			return ParserResult.success(r2.getValue(), r2.getState());
		};
	}
	
	// ==================== Grammar Interface Implementation ====================
	
	@Override
	public ParseResult parse(List<Lexer.Token> tokens) {
		if (mainParser == null) {
			return ParseResult.failure("No main parser set. Call setMainParser() first.", 0);
		}
		
		ParseState state = new ParseState(tokens);
		ParserResult<?> result = mainParser.parse(state);
		
		if (result.isFailure()) {
			return ParseResult.failure(result.getError(), result.getState().getPosition());
		}
		
		// Check if all input consumed
		if (result.getState().hasMore()) {
			Lexer.Token remaining = result.getState().current();
			return ParseResult.failure("Unexpected token: " + remaining.type + " at line " + remaining.line, 
				result.getState().getPosition());
		}
		
		return ParseResult.success(result.getValue());
	}
	
	@Override
	public String toGrammarString() {
		return "/* Parsec-style Grammar */\n" +
		       "/* " + grammarDescription + " */\n" +
		       "/* Parser combinators are defined programmatically */\n";
	}
	
	@Override
	public String getGrammarType() {
		return "Parsec";
	}
}
