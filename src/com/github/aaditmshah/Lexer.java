package com.github.aaditmshah;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import com.example.Util;
import com.example.sharp.CString;
import com.example.sharp.Delegates;
import com.example.sharp.Dictionary;
import com.example.sharp.Tracer;

public class Lexer {
	
	private Pattern idRegExp = Pattern.compile("[a-z_][a-z0-9_-]*");
	public static final int EOF=0;
	public static final int STATE_INITIAL=0;
	public static final int STATE_ANY=-1;
	public static final String RULE_EOF="<<EOF>>";
	public static class State{
		public String name;
		public boolean exclusive;
		public State(String name,boolean exclusive) {
			this.name=name;
			this.exclusive=exclusive;
		}
	}
	String source="";
	int index=0;
	String text="";
	int state=Lexer.STATE_INITIAL;
	int ruleIndex=-1;
	boolean readMore=false;
	Vector<String> stateStack=new Vector<>();
	Vector<String> rejectedRules=new Vector<>();
	Dictionary<String,String> definitions = new Dictionary<>();
	Dictionary<String,State> states = new Dictionary<>();
	Vector<RuleObject> rules = new Vector<>();
	boolean ignoreCase=false;
	boolean debugEnabled=false;
	private int remove;
	private Vector<String> tokens = new Vector<>();
	private String input;
	private boolean error=false;
	Delegates.Func1<Character, String> defunctAction;
	
	public void reset() {
		source="";
		index=0;
		text="";
		state=Lexer.STATE_INITIAL;
		ruleIndex=-1;
		readMore=false;
		stateStack=new Vector<>();
		rejectedRules=new Vector<>();
	}
	public String defunct(char chr) {
		if(defunctAction != null) {
			try {
				return defunctAction.Invoke(chr);
			}catch(Exception ee) {
				Tracer.D(ee);
			}
		}
		Tracer.D("Unexpected character at index " + (this.index - 1) + ": " + chr);
		error=true;
		return null;
	}
	public void clear() {
		states = new Dictionary<>();
		definitions = new Dictionary<>();
		rules = new Vector<>();
		this.ignoreCase=false;
		this.debugEnabled=false;
		// this.addState(Lexer.STATE_INITIAL);
		this.reset();
	}
	public void setIgnoreCase(boolean ignoreCase) {
		this.ignoreCase=ignoreCase;
	}
	public void setDebugEnabled(boolean debugEnabled) {
		this.debugEnabled=debugEnabled;
	}
	public void addState(String name,boolean exclusive) {
		this.states.set(name, new State(name,exclusive));
	}
	public void addDefinition(String name, String expression) {
		this.definitions.set(name, expression);
	}
	public Lexer addRule(String pattern, Delegates.Func2<Lexer, MatchResult, String>  action, Vector<Integer> start) {
        
        PatternObject compiledPattern=new PatternObject(pattern);
        if(start==null) {
        	start=Delegates.toVector(new Integer[] {0});
        }
        rules.add(new RuleObject(compiledPattern,pattern,action,start));
        return this;
    }
    public Lexer  setInput(String input) {
        this.remove = 0;
        this.state =0;
        this.index = 0;
        this.tokens.clear();
        this.input = input;
        return this;
    }
    Iterator<String> tokenIter;
	private boolean reject;
    public String lex() {
        if (tokenIter!= null && tokenIter.hasNext()) return tokenIter.next();

        this.reject = true;

        while (this.index <= this.input.length()) {
            Vector<MatchResultObject> matches = Delegates.toVector(Util.splice(scan(),remove));
            int index = this.index;

            Iterator<MatchResultObject> matchesIter=matches.iterator();
            while (matchesIter.hasNext()) {
                if (this.reject) {
                	MatchResultObject match = matchesIter.next();
                    MatchResult result = match.result;
                    int length = match.length;
                    this.index += length;
                    this.reject = false;
                    remove++;

                    String token = match.action.Invoke(this, result);
                    if (this.reject) this.index = result.start();
                    else if (token !=null) {
                    	if(token.getClass().isArray()) {
                    		int len = Array.getLength(token);
                    		for(int ii=1;ii<len; ++ii) {
                    			String subToken=(String)Array.get(token, ii);
                    			tokens.add(subToken);
                    		}
                    		token = tokens.get(0);
                    	} else {
                    		if(length > 0) {
                    			remove = 0;
                    		}
                    		return token;
                    	}
                    }
                } else break;
            }

            String input = this.input;

            if (index < input.length()) {
                if (this.reject) {
                    remove = 0;
                    String token = defunct(input.charAt(this.index++));
                    if (token != null) {
                    	if(token.getClass().isArray()) {
                    		int len = Array.getLength(token);
                    		for(int ii=1;ii<len; ++ii) {
                    			String subToken=(String)Array.get(token, ii);
                    			tokens.add(subToken);
                    		}
                    		token = tokens.get(0);
                    	} else {
                    		return token;
                    	}
                    }
                } else {
                    if (this.index != index) remove = 0;
                    this.reject = true;
                }
            } else if (matches.size() > 0)
                this.reject = true;
            else break;
        }
        return null;
    }
    public Vector<MatchResultObject> scan() {
        Vector<MatchResultObject> matches = new Vector<>();
        int index = 0;

        int state = this.state;
        int lastIndex = this.index;
        String input = this.input;

        for (int i = 0, length = rules.size(); i < length; i++) {
            RuleObject rule = rules.get(i);
            Vector<Integer> start = rule.start;
            int states = start.size();

            if ((states==0 || start.indexOf(state) >= 0) ||
                ((state % 2) != 0 && states == 1 && start.get(0)==0)) {
                PatternObject pattern = rule.pattern;
                pattern.lastIndex = lastIndex;
                MatchResult result = pattern.pattern.matcher(input).toMatchResult();

                if (result!=null && result.start() == lastIndex) {
                    matches.add(new MatchResultObject(
                    		result,
                            rule.action,
                            result.group(0).length()
                    ));
                    int j = matches.size()-1;

                    if (!CString.IsNullOrEmpty(rule.global)) index = j;

                    while (--j > index) {
                        var k = j - 1;
                        
                        if (matches.get(j).length > matches.get(k).length) {
                        	Util.swap(matches, j, k);
                        }
                    }
                }
            }
        }

        return matches;
    }

}
class PatternObject{
	public Pattern pattern;
	public int lastIndex;
	public String global;
	public PatternObject(String global) {
		this.pattern=Pattern.compile(global);
		this.global=global;
	}
	public PatternObject(Pattern pattern, String global) {
		this.pattern=pattern;
		this.global=global;
	}
	public PatternObject() {
		
	}
}
class MatchResultObject{
	public MatchResult result;
	public Delegates.Func2<Lexer, MatchResult, String>  action;
	public int length;
	public MatchResultObject() {
		
	}
	public MatchResultObject(MatchResult result,Delegates.Func2<Lexer, MatchResult, String>  action,int length) {
		this.result=result;
		this.action=action;
		this.length=length;
	}
}
class RuleObject{
	public PatternObject pattern;
	public String global;
	public Delegates.Func2<Lexer, MatchResult, String>  action;
	public Vector<Integer> start;
	public RuleObject(PatternObject pattern, String global, Delegates.Func2<Lexer, MatchResult, String>  action,Vector<Integer> start) {
		this.pattern=pattern;
		this.global=global;
		this.action=action;
		this.start=start;
	}
	public RuleObject() {
		
	}
}
