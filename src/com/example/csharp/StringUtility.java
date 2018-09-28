package com.example.csharp;

public class StringUtility {
    public static boolean IsNullOrEmpty(String s){
        if(s == null) return true;
        if(s.trim().length() == 0) return true;
        return false;
    }
}
