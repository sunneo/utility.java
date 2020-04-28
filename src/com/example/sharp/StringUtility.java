package com.example.sharp;

public class StringUtility {

    public static final Delegates.Func1<Character,Boolean> SpaceFilter = (c)-> {
        return Character.isSpaceChar(c);
    };

    public static String Join(String delimiter,Iterable<String> strings) {
        StringBuilder ret = new StringBuilder();
        if(strings != null) {
            Iterator<String> iter = strings.iterator();
            while(iter.hasNext()) {
                ret.append(iter.next());
                if(iter.hasNext()) {
                    ret.append(delimiter);
                }
            }
        }
        return ret.toString();
    }

    public static String Join(String delimiter, String... strings) {
        return Join(delimiter, Delegates.forall(strings));
    }

    public static boolean IsNullOrWhiteSpace(String s) {
        return s == null || s.trim().length() == 0;
    }

    public static boolean IsNullOrEmpty(String s) {
        return s == null || s.length() == 0;
    }

    public static String TrimStart(String s) {
        return TrimStart(s,(ch)->Character.isWhitespace(ch));
    }

    public static String TrimStart(String s,  char c) {
        return TrimStart(s,(ch)->ch==c);
    }
    public static Delegates.Func1<Character, Boolean> IsCharInSetDelegate(char[] chars) {
        return (ch)-> {
            for(char c:chars) {
                if(ch == c) {
                    return true;
                }
            }
            return false;
        };
    }
    public static String Trim(String s) {
        return TrimStart(TrimEnd(s));
    }
    public static String Trim(String s,char c) {
        return TrimStart(TrimEnd(s,c),c);
    }
    public static String Trim(String s,char...chars) {
        return TrimStart(TrimEnd(s,chars),chars);
    }
    public static String Trim(String s,Delegates.Func1<Character, Boolean> acceptTestFunc) {
        return TrimStart(TrimEnd(s,acceptTestFunc),acceptTestFunc);
    }
    public static String TrimStart(String s,  char... chars) {
        return TrimStart(s,IsCharInSetDelegate(chars));
    }
    public static String TrimStart(String s, Delegates.Func1<Character, Boolean> acceptTestFunc) {
        int len = s.length();
        for(int i=0; i<len; ++i) {
            if (!acceptTestFunc.Invoke(s.charAt(i))) {
                return s.substring(i);
            }
        }
        return s;
    }
    public static String TrimEnd(String s) {
        return TrimEnd(s,(ch)->Character.isWhitespace(ch));
    }
    public static String TrimEnd(String s, char... chars) {
        return TrimEnd(s,IsCharInSetDelegate(chars));
    }
    public static String TrimEnd(String s, char c) {
        return TrimEnd(s,(ch)->ch==c);
    }
    public static String TrimEnd(String s, Delegates.Func1<Character, Boolean> acceptTestFunc) {
        int len = s.length();
        for(int i=len-1; i>=0; --i) {
            if (!acceptTestFunc.Invoke(s.charAt(i))) {
                String ret = s.substring(0,i+1);
                return ret;
            }
        }
        return s;
    }
}
