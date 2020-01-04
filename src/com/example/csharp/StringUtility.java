package com.example.csharp;

public class StringUtility {
	public static boolean IsNullOrEmpty(String s) {
		if (s == null)
			return true;
		if (s.trim().length() == 0)
			return true;
		return false;
	}

	public static boolean IsNullOrWhiteSpace(String s) {
		return s == null || s.trim().length() == 0;
	}
}
