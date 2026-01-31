package com.github.aaditmshah.json;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Abstract base class for JSON AST nodes
 */
public abstract class JsonNode {
	
	/**
	 * Get the type of this JSON node
	 */
	public abstract JsonType getType();
	
	/**
	 * Check if this node is a specific type
	 */
	public boolean is(JsonType type) {
		return getType() == type;
	}
	
	/**
	 * Safe cast to JsonObject
	 */
	public JsonObject asObject() {
		if (this instanceof JsonObject) {
			return (JsonObject) this;
		}
		throw new ClassCastException("Cannot cast " + getType() + " to object");
	}
	
	/**
	 * Safe cast to JsonArray
	 */
	public JsonArray asArray() {
		if (this instanceof JsonArray) {
			return (JsonArray) this;
		}
		throw new ClassCastException("Cannot cast " + getType() + " to array");
	}
	
	/**
	 * Safe cast to JsonString
	 */
	public JsonString asString() {
		if (this instanceof JsonString) {
			return (JsonString) this;
		}
		throw new ClassCastException("Cannot cast " + getType() + " to string");
	}
	
	/**
	 * Safe cast to JsonNumber
	 */
	public JsonNumber asNumber() {
		if (this instanceof JsonNumber) {
			return (JsonNumber) this;
		}
		throw new ClassCastException("Cannot cast " + getType() + " to number");
	}
	
	/**
	 * Safe cast to JsonBoolean
	 */
	public JsonBoolean asBoolean() {
		if (this instanceof JsonBoolean) {
			return (JsonBoolean) this;
		}
		throw new ClassCastException("Cannot cast " + getType() + " to boolean");
	}
	
	/**
	 * Check if this is a null node
	 */
	public boolean isNull() {
		return this instanceof JsonNull;
	}
	
	/**
	 * Convert to pretty-printed JSON string
	 */
	public abstract String toJson();
	
	/**
	 * Convert to pretty-printed JSON string with indentation
	 */
	public String toJson(int indent) {
		return toJsonIndented(indent, 0);
	}
	
	protected abstract String toJsonIndented(int indent, int level);
	
	protected String getIndent(int indent, int level) {
		if (indent <= 0) return "";
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < indent * level; i++) {
			sb.append(' ');
		}
		return sb.toString();
	}
	
	/**
	 * JSON value types
	 */
	public enum JsonType {
		OBJECT,
		ARRAY,
		STRING,
		NUMBER,
		BOOLEAN,
		NULL
	}
	
	/**
	 * JSON Object node
	 */
	public static class JsonObject extends JsonNode {
		private final Map<String, JsonNode> members;
		
		public JsonObject(Map<String, JsonNode> members) {
			this.members = members;
		}
		
		@Override
		public JsonType getType() {
			return JsonType.OBJECT;
		}
		
		public Map<String, JsonNode> getMembers() {
			return members;
		}
		
		public JsonNode get(String key) {
			return members.get(key);
		}
		
		public boolean has(String key) {
			return members.containsKey(key);
		}
		
		public int size() {
			return members.size();
		}
		
		@Override
		public String toJson() {
			if (members.isEmpty()) {
				return "{}";
			}
			StringBuilder sb = new StringBuilder();
			sb.append("{");
			boolean first = true;
			for (Map.Entry<String, JsonNode> entry : members.entrySet()) {
				if (!first) sb.append(",");
				sb.append("\"").append(escapeString(entry.getKey())).append("\":");
				sb.append(entry.getValue().toJson());
				first = false;
			}
			sb.append("}");
			return sb.toString();
		}
		
		@Override
		protected String toJsonIndented(int indent, int level) {
			if (members.isEmpty()) {
				return "{}";
			}
			StringBuilder sb = new StringBuilder();
			sb.append("{\n");
			boolean first = true;
			for (Map.Entry<String, JsonNode> entry : members.entrySet()) {
				if (!first) sb.append(",\n");
				sb.append(getIndent(indent, level + 1));
				sb.append("\"").append(escapeString(entry.getKey())).append("\": ");
				sb.append(entry.getValue().toJsonIndented(indent, level + 1));
				first = false;
			}
			sb.append("\n").append(getIndent(indent, level)).append("}");
			return sb.toString();
		}
		
		@Override
		public String toString() {
			return "JsonObject{" + members.size() + " members}";
		}
	}
	
	/**
	 * JSON Array node
	 */
	public static class JsonArray extends JsonNode {
		private final List<JsonNode> elements;
		
		public JsonArray(List<JsonNode> elements) {
			this.elements = elements;
		}
		
		@Override
		public JsonType getType() {
			return JsonType.ARRAY;
		}
		
		public List<JsonNode> getElements() {
			return elements;
		}
		
		public JsonNode get(int index) {
			return elements.get(index);
		}
		
		public int size() {
			return elements.size();
		}
		
		@Override
		public String toJson() {
			if (elements.isEmpty()) {
				return "[]";
			}
			StringBuilder sb = new StringBuilder();
			sb.append("[");
			boolean first = true;
			for (JsonNode element : elements) {
				if (!first) sb.append(",");
				sb.append(element.toJson());
				first = false;
			}
			sb.append("]");
			return sb.toString();
		}
		
		@Override
		protected String toJsonIndented(int indent, int level) {
			if (elements.isEmpty()) {
				return "[]";
			}
			StringBuilder sb = new StringBuilder();
			sb.append("[\n");
			boolean first = true;
			for (JsonNode element : elements) {
				if (!first) sb.append(",\n");
				sb.append(getIndent(indent, level + 1));
				sb.append(element.toJsonIndented(indent, level + 1));
				first = false;
			}
			sb.append("\n").append(getIndent(indent, level)).append("]");
			return sb.toString();
		}
		
		@Override
		public String toString() {
			return "JsonArray{" + elements.size() + " elements}";
		}
	}
	
	/**
	 * JSON String node
	 */
	public static class JsonString extends JsonNode {
		private final String value;
		
		public JsonString(String value) {
			this.value = value;
		}
		
		@Override
		public JsonType getType() {
			return JsonType.STRING;
		}
		
		public String getValue() {
			return value;
		}
		
		@Override
		public String toJson() {
			return "\"" + escapeString(value) + "\"";
		}
		
		@Override
		protected String toJsonIndented(int indent, int level) {
			return toJson();
		}
		
		@Override
		public String toString() {
			return "JsonString{\"" + value + "\"}";
		}
	}
	
	/**
	 * JSON Number node
	 */
	public static class JsonNumber extends JsonNode {
		private final Number value;
		private final String rawValue;
		
		public JsonNumber(Number value) {
			this.value = value;
			this.rawValue = value.toString();
		}
		
		public JsonNumber(String rawValue) {
			this.rawValue = rawValue;
			// Parse number
			Number parsedValue;
			if (rawValue.contains(".") || rawValue.contains("e") || rawValue.contains("E")) {
				parsedValue = Double.parseDouble(rawValue);
			} else {
				try {
					parsedValue = Long.parseLong(rawValue);
				} catch (NumberFormatException e) {
					parsedValue = Double.parseDouble(rawValue);
				}
			}
			this.value = parsedValue;
		}
		
		@Override
		public JsonType getType() {
			return JsonType.NUMBER;
		}
		
		public Number getValue() {
			return value;
		}
		
		public int intValue() {
			return value.intValue();
		}
		
		public long longValue() {
			return value.longValue();
		}
		
		public double doubleValue() {
			return value.doubleValue();
		}
		
		@Override
		public String toJson() {
			return rawValue;
		}
		
		@Override
		protected String toJsonIndented(int indent, int level) {
			return toJson();
		}
		
		@Override
		public String toString() {
			return "JsonNumber{" + rawValue + "}";
		}
	}
	
	/**
	 * JSON Boolean node
	 */
	public static class JsonBoolean extends JsonNode {
		private final boolean value;
		
		public static final JsonBoolean TRUE = new JsonBoolean(true);
		public static final JsonBoolean FALSE = new JsonBoolean(false);
		
		private JsonBoolean(boolean value) {
			this.value = value;
		}
		
		public static JsonBoolean of(boolean value) {
			return value ? TRUE : FALSE;
		}
		
		@Override
		public JsonType getType() {
			return JsonType.BOOLEAN;
		}
		
		public boolean getValue() {
			return value;
		}
		
		@Override
		public String toJson() {
			return value ? "true" : "false";
		}
		
		@Override
		protected String toJsonIndented(int indent, int level) {
			return toJson();
		}
		
		@Override
		public String toString() {
			return "JsonBoolean{" + value + "}";
		}
	}
	
	/**
	 * JSON Null node
	 */
	public static class JsonNull extends JsonNode {
		public static final JsonNull INSTANCE = new JsonNull();
		
		private JsonNull() {}
		
		@Override
		public JsonType getType() {
			return JsonType.NULL;
		}
		
		@Override
		public String toJson() {
			return "null";
		}
		
		@Override
		protected String toJsonIndented(int indent, int level) {
			return toJson();
		}
		
		@Override
		public String toString() {
			return "JsonNull";
		}
	}
	
	/**
	 * Escape special characters in JSON strings
	 */
	protected static String escapeString(String s) {
		StringBuilder sb = new StringBuilder();
		for (char c : s.toCharArray()) {
			switch (c) {
				case '"': sb.append("\\\""); break;
				case '\\': sb.append("\\\\"); break;
				case '\b': sb.append("\\b"); break;
				case '\f': sb.append("\\f"); break;
				case '\n': sb.append("\\n"); break;
				case '\r': sb.append("\\r"); break;
				case '\t': sb.append("\\t"); break;
				default:
					if (c < 32) {
						sb.append(String.format("\\u%04x", (int) c));
					} else {
						sb.append(c);
					}
			}
		}
		return sb.toString();
	}
}
