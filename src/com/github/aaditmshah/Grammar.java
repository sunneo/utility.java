package com.github.aaditmshah;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.example.sharp.Delegates;

/**
 * A Parsec-like parser combinator library for Java using lambdas.
 * 
 * <p>This class provides combinators to build parsers that can parse token streams
 * produced by the Lexer and construct AST nodes.</p>
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * Grammar grammar = new Grammar();
 * 
 * // Define parsers using combinators
 * Parser<String> number = grammar.token("NUMBER");
 * Parser<String> plus = grammar.token("PLUS");
 * 
 * Parser<ASTNode> expr = grammar.sequence(
 *     number,
 *     plus, 
 *     number
 * ).map(results -> new AddNode(results.get(0), results.get(2)));
 * 
 * // Parse tokens
 * List<Lexer.Token> tokens = lexer.tokenize();
 * ParseResult<ASTNode> result = expr.parse(new ParseState(tokens));
 * }</pre>
 */
public class Grammar {
	
	/**
	 * Result of a parse operation
	 */
	public static class ParseResult<T> {
		private final T value;
		private final ParseState state;
		private final boolean success;
		private final String error;
		
		private ParseResult(T value, ParseState state, boolean success, String error) {
			this.value = value;
			this.state = state;
			this.success = success;
			this.error = error;
		}
		
		public static <T> ParseResult<T> success(T value, ParseState state) {
			return new ParseResult<>(value, state, true, null);
		}
		
		public static <T> ParseResult<T> failure(String error, ParseState state) {
			return new ParseResult<>(null, state, false, error);
		}
		
		public boolean isSuccess() {
			return success;
		}
		
		public boolean isFailure() {
			return !success;
		}
		
		public T getValue() {
			return value;
		}
		
		public ParseState getState() {
			return state;
		}
		
		public String getError() {
			return error;
		}
		
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
	 * Parser functional interface
	 */
	@FunctionalInterface
	public interface Parser<T> {
		ParseResult<T> parse(ParseState state);
		
		/**
		 * Map the result of this parser to a new type
		 */
		default <R> Parser<R> map(Function<T, R> mapper) {
			return state -> {
				ParseResult<T> result = this.parse(state);
				if (result.isSuccess()) {
					return ParseResult.success(mapper.apply(result.getValue()), result.getState());
				}
				return ParseResult.failure(result.getError(), result.getState());
			};
		}
		
		/**
		 * Chain this parser with another parser that depends on the result
		 */
		default <R> Parser<R> flatMap(Function<T, Parser<R>> mapper) {
			return state -> {
				ParseResult<T> result = this.parse(state);
				if (result.isSuccess()) {
					return mapper.apply(result.getValue()).parse(result.getState());
				}
				return ParseResult.failure(result.getError(), result.getState());
			};
		}
		
		/**
		 * Try this parser, or if it fails, try the alternative
		 */
		default Parser<T> or(Parser<T> alternative) {
			return state -> {
				ParseResult<T> result = this.parse(state);
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
				ParseResult<T> result = this.parse(state);
				if (result.isSuccess()) {
					return result;
				}
				return ParseResult.success(null, state);
			};
		}
	}
	
	/**
	 * Create a parser that matches a specific token type
	 */
	public Parser<Lexer.Token> token(String type) {
		return state -> {
			if (state.hasMore()) {
				Lexer.Token token = state.current();
				if (token.type.equals(type)) {
					return ParseResult.success(token, state.advance());
				}
				return ParseResult.failure("Expected " + type + " but got " + token.type + " at " + state.getLocationInfo(), state);
			}
			return ParseResult.failure("Expected " + type + " but reached end of input", state);
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
					return ParseResult.success(token, state.advance());
				}
				return ParseResult.failure("Expected " + type + "(" + value + ") but got " + token.type + "(" + token.value + ") at " + state.getLocationInfo(), state);
			}
			return ParseResult.failure("Expected " + type + "(" + value + ") but reached end of input", state);
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
						return ParseResult.success(token, state.advance());
					}
				}
				return ParseResult.failure("Expected one of " + String.join(", ", types) + " but got " + token.type + " at " + state.getLocationInfo(), state);
			}
			return ParseResult.failure("Expected one of " + String.join(", ", types) + " but reached end of input", state);
		};
	}
	
	/**
	 * Create a parser that always succeeds with the given value
	 */
	public <T> Parser<T> pure(T value) {
		return state -> ParseResult.success(value, state);
	}
	
	/**
	 * Create a parser that always fails with the given message
	 */
	public <T> Parser<T> fail(String message) {
		return state -> ParseResult.failure(message, state);
	}
	
	/**
	 * Sequence two parsers and return both results as a pair
	 */
	public <A, B> Parser<List<Object>> sequence(Parser<A> first, Parser<B> second) {
		return state -> {
			ParseResult<A> result1 = first.parse(state);
			if (result1.isFailure()) {
				return ParseResult.failure(result1.getError(), result1.getState());
			}
			ParseResult<B> result2 = second.parse(result1.getState());
			if (result2.isFailure()) {
				return ParseResult.failure(result2.getError(), result2.getState());
			}
			List<Object> list = new ArrayList<>();
			list.add(result1.getValue());
			list.add(result2.getValue());
			return ParseResult.success(list, result2.getState());
		};
	}
	
	/**
	 * Sequence three parsers
	 */
	public <A, B, C> Parser<List<Object>> sequence(Parser<A> p1, Parser<B> p2, Parser<C> p3) {
		return state -> {
			ParseResult<A> r1 = p1.parse(state);
			if (r1.isFailure()) return ParseResult.failure(r1.getError(), r1.getState());
			
			ParseResult<B> r2 = p2.parse(r1.getState());
			if (r2.isFailure()) return ParseResult.failure(r2.getError(), r2.getState());
			
			ParseResult<C> r3 = p3.parse(r2.getState());
			if (r3.isFailure()) return ParseResult.failure(r3.getError(), r3.getState());
			
			List<Object> list = new ArrayList<>();
			list.add(r1.getValue());
			list.add(r2.getValue());
			list.add(r3.getValue());
			return ParseResult.success(list, r3.getState());
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
				ParseResult<?> result = parser.parse(currentState);
				if (result.isFailure()) {
					return ParseResult.failure(result.getError(), result.getState());
				}
				results.add(result.getValue());
				currentState = result.getState();
			}
			
			return ParseResult.success(results, currentState);
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
				ParseResult<T> result = parser.parse(state);
				if (result.isSuccess()) {
					return result;
				}
				lastError = result.getError();
			}
			return ParseResult.failure(lastError, state);
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
				ParseResult<T> result = parser.parse(currentState);
				if (result.isFailure()) {
					break;
				}
				results.add(result.getValue());
				currentState = result.getState();
			}
			
			return ParseResult.success(results, currentState);
		};
	}
	
	/**
	 * Parse one or more occurrences
	 */
	public <T> Parser<List<T>> many1(Parser<T> parser) {
		return state -> {
			ParseResult<T> first = parser.parse(state);
			if (first.isFailure()) {
				return ParseResult.failure(first.getError(), first.getState());
			}
			
			List<T> results = new ArrayList<>();
			results.add(first.getValue());
			ParseState currentState = first.getState();
			
			while (true) {
				ParseResult<T> result = parser.parse(currentState);
				if (result.isFailure()) {
					break;
				}
				results.add(result.getValue());
				currentState = result.getState();
			}
			
			return ParseResult.success(results, currentState);
		};
	}
	
	/**
	 * Parse items separated by a separator
	 */
	public <T, S> Parser<List<T>> sepBy(Parser<T> item, Parser<S> separator) {
		return state -> {
			List<T> results = new ArrayList<>();
			
			ParseResult<T> first = item.parse(state);
			if (first.isFailure()) {
				return ParseResult.success(results, state); // Empty list is OK
			}
			
			results.add(first.getValue());
			ParseState currentState = first.getState();
			
			while (true) {
				ParseResult<S> sepResult = separator.parse(currentState);
				if (sepResult.isFailure()) {
					break;
				}
				
				ParseResult<T> itemResult = item.parse(sepResult.getState());
				if (itemResult.isFailure()) {
					break;
				}
				
				results.add(itemResult.getValue());
				currentState = itemResult.getState();
			}
			
			return ParseResult.success(results, currentState);
		};
	}
	
	/**
	 * Parse items separated by a separator, requiring at least one item
	 */
	public <T, S> Parser<List<T>> sepBy1(Parser<T> item, Parser<S> separator) {
		return state -> {
			List<T> results = new ArrayList<>();
			
			ParseResult<T> first = item.parse(state);
			if (first.isFailure()) {
				return ParseResult.failure(first.getError(), first.getState());
			}
			
			results.add(first.getValue());
			ParseState currentState = first.getState();
			
			while (true) {
				ParseResult<S> sepResult = separator.parse(currentState);
				if (sepResult.isFailure()) {
					break;
				}
				
				ParseResult<T> itemResult = item.parse(sepResult.getState());
				if (itemResult.isFailure()) {
					break;
				}
				
				results.add(itemResult.getValue());
				currentState = itemResult.getState();
			}
			
			return ParseResult.success(results, currentState);
		};
	}
	
	/**
	 * Parse content between delimiters
	 */
	public <L, T, R> Parser<T> between(Parser<L> left, Parser<T> content, Parser<R> right) {
		return state -> {
			ParseResult<L> leftResult = left.parse(state);
			if (leftResult.isFailure()) {
				return ParseResult.failure(leftResult.getError(), leftResult.getState());
			}
			
			ParseResult<T> contentResult = content.parse(leftResult.getState());
			if (contentResult.isFailure()) {
				return ParseResult.failure(contentResult.getError(), contentResult.getState());
			}
			
			ParseResult<R> rightResult = right.parse(contentResult.getState());
			if (rightResult.isFailure()) {
				return ParseResult.failure(rightResult.getError(), rightResult.getState());
			}
			
			return ParseResult.success(contentResult.getValue(), rightResult.getState());
		};
	}
	
	/**
	 * Optionally parse, returning default value on failure
	 */
	public <T> Parser<T> optional(Parser<T> parser, T defaultValue) {
		return state -> {
			ParseResult<T> result = parser.parse(state);
			if (result.isSuccess()) {
				return result;
			}
			return ParseResult.success(defaultValue, state);
		};
	}
	
	/**
	 * Look ahead without consuming input
	 */
	public <T> Parser<T> lookAhead(Parser<T> parser) {
		return state -> {
			ParseResult<T> result = parser.parse(state);
			if (result.isSuccess()) {
				return ParseResult.success(result.getValue(), state); // Don't advance
			}
			return result;
		};
	}
	
	/**
	 * Negative look ahead - succeed if parser fails, fail if it succeeds
	 */
	public <T> Parser<Void> notFollowedBy(Parser<T> parser) {
		return state -> {
			ParseResult<T> result = parser.parse(state);
			if (result.isSuccess()) {
				return ParseResult.failure("Unexpected match", state);
			}
			return ParseResult.success(null, state);
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
				return ParseResult.success(null, state);
			}
			return ParseResult.failure("Expected end of input but got " + state.current().type, state);
		};
	}
	
	/**
	 * Label a parser with an expected name for better error messages
	 */
	public <T> Parser<T> label(Parser<T> parser, String expected) {
		return state -> {
			ParseResult<T> result = parser.parse(state);
			if (result.isFailure()) {
				return ParseResult.failure("Expected " + expected + " at " + state.getLocationInfo(), state);
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
			ParseResult<T> r1 = first.parse(state);
			if (r1.isFailure()) return ParseResult.failure(r1.getError(), r1.getState());
			
			ParseResult<S> r2 = second.parse(r1.getState());
			if (r2.isFailure()) return ParseResult.failure(r2.getError(), r2.getState());
			
			return ParseResult.success(r1.getValue(), r2.getState());
		};
	}
	
	/**
	 * Skip first, parse second (use second result)
	 */
	public <T, S> Parser<S> right(Parser<T> first, Parser<S> second) {
		return state -> {
			ParseResult<T> r1 = first.parse(state);
			if (r1.isFailure()) return ParseResult.failure(r1.getError(), r1.getState());
			
			ParseResult<S> r2 = second.parse(r1.getState());
			if (r2.isFailure()) return ParseResult.failure(r2.getError(), r2.getState());
			
			return ParseResult.success(r2.getValue(), r2.getState());
		};
	}
}
