package com.example.sharp;

import java.util.Iterator;
import java.util.Vector;

public class CString {
		
    public static final Delegates.Func1<Character,Boolean> SpaceFilter = (c)->{
        return Character.isSpaceChar(c);
    };
    public static Vector<String> tokenize(String str, String delimiter) {
		if (CString.IsNullOrEmpty(str)) {
			return new Vector<>();
		}
		Vector<String> tokens;
		tokens = new Vector<>();
		int len = str.length();
		int idx = 0;
		String val = str;
		int delimLen = delimiter.length();
		while(idx < len) {
			int tok = val.indexOf(delimiter);
			if(tok < 0) {
				tokens.add(val);
				break;
			}
			String left = val.substring(0,tok);
			tokens.add(left);
			idx = tok+delimLen;
			val = val.substring(tok+delimLen);
		}
		return tokens;
	}
    @SafeVarargs
    public static String[] Extract(String source,Tuples.Tuple2<Integer,Integer>... pairs) {
        Vector<String> vec = new Vector<>();
        StringBuilder strb = new StringBuilder();
        strb.append(source);
        for(int i=0; i<pairs.length; ++i) {
            Tuples.Tuple2<Integer,Integer> splitPoint=pairs[i];
            char[] targetBuf=new char[splitPoint.item2-splitPoint.item1];
            strb.getChars(splitPoint.item1, splitPoint.item2, targetBuf, 0);
            vec.add(new String(targetBuf));
        }
        return Delegates.toArray(vec);
    }
    @SuppressWarnings("unchecked")
    @SafeVarargs
    public static String[] Extract(String source,int... pairs) {
        Object[] tuples=new Object[pairs.length/2];
        for(int i=0; i<pairs.length; i+=2) {
            int item1=pairs[i];
            int item2=pairs[i+1];
            tuples[i/2] = Tuples.tuple(item1,item2);
        }
        return Extract(source,(Tuples.Tuple2<Integer, Integer>[])tuples);
    }
    public static String[] Split(String source,int... splitPoints) {
        Vector<String> vec = new Vector<>();
        int currentIdx=0;
        StringBuilder strb = new StringBuilder();
        strb.append(source);
        int len = strb.length();
        for(int i=0; i<splitPoints.length; ++i) {
            int splitPoint=splitPoints[i];
            if(currentIdx >= splitPoint) continue;
            if(splitPoint >= len) break; 
            char[] targetBuf=new char[splitPoint-currentIdx];
            strb.getChars(currentIdx, splitPoint, targetBuf, 0);
            vec.add(new String(targetBuf));
            currentIdx=splitPoint;
        }
        if(currentIdx < len) {
            char[] targetBuf=new char[len-currentIdx+1];
            strb.getChars(currentIdx, len, targetBuf, 0);
            vec.add(new String(targetBuf));
        }
        return Delegates.toArray(vec);
    }
    public static String Join(String delimiter,Iterable<String> strings) {
        return Join(delimiter,strings.iterator());
    }
    public static String Join(String delimiter,Iterator<String> strings) {
        StringBuilder ret = new StringBuilder();
        if(strings != null) {
            Iterator<String> iter = strings;
            while(iter.hasNext()) {
                ret.append(iter.next());
                if(iter.hasNext()) {
                    ret.append(delimiter);
                }
            }
        }
        return ret.toString();
    }
    public static boolean ContainsUpperCase(String s) {
        char[] chs = s.toCharArray();
        for(int i=0; i<chs.length; ++i) {
           char ch = chs[i];
           if(Character.isAlphabetic(ch) && !Character.isLowerCase(ch)) {
               return true;
           }
        }
        return false;
    }
    public static boolean ContainsLowerCase(String s) {
        char[] chs = s.toCharArray();
        for(int i=0; i<chs.length; ++i) {
           char ch = chs[i];
           if(Character.isAlphabetic(ch) && !Character.isUpperCase(ch)) {
               return true;
           }
        }
        return false;
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
        return (ch)->{
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
        return Trim(s,IsCharInSetDelegate(chars)); 
    }
    public static String Trim(String s,Delegates.Func1<Character, Boolean> acceptTestFunc) {
        char[] chars = s.toCharArray();
        String ret = s;
        int len = chars.length;
        int startIdx=-1;
        for(int i=0; i<len; ++i) {
            if (!acceptTestFunc.Invoke(chars[i])) {
                startIdx=i;
                break;
            }
        }
        int endIdx=-1;
        for(int i=len-1; i>=0; --i) {
            if (!acceptTestFunc.Invoke(chars[i])) {
                endIdx=i;
                break;
            }
        }
        if(startIdx == -1 || endIdx == -1) {
            ret = "";
        } else {
            ret = s.substring(startIdx,endIdx+1);
        }
        return ret;
    }
    public static String TrimStart(String s,  char... chars) {
        return TrimStart(s,IsCharInSetDelegate(chars));
    }
    public static String TrimStart(String s, Delegates.Func1<Character, Boolean> acceptTestFunc) {
        char[] chars = s.toCharArray();
        for(int i=0; i<chars.length; ++i) {
            if (!acceptTestFunc.Invoke(chars[i])) {
                String ret = s.substring(i);
                return ret;
            }
        }
        return "";
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
        char[] chars = s.toCharArray();
        for(int i=chars.length-1; i>=0; --i) {
            if (!acceptTestFunc.Invoke(chars[i])) {
                String ret = s.substring(0,i+1);
                return ret;
            }
        }
        return "";
    }
}