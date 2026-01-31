#!/usr/bin/env python3
"""
jflex2java.py - Convert JFlex (.flex/.jflex) files to Java Lexer definitions

This script parses JFlex lexer specification files and generates equivalent
Java code using the com.github.aaditmshah.Lexer class.

JFlex format:
    %{
      // Java code for class members
    %}
    
    %class ClassName
    %unicode
    %line
    %column
    
    %%
    
    PATTERN    { return TOKEN; }
    
    %%

Usage:
    python jflex2java.py input.flex [-o output.java] [--class ClassName]
"""

import re
import sys
import argparse
from typing import List, Tuple, Optional, Dict


class JFlexRule:
    """Represents a single lexer rule from a JFlex file"""
    def __init__(self, pattern: str, action: str, state: Optional[str] = None):
        self.pattern = pattern
        self.action = action
        self.state = state  # Start condition (state name)
    
    def __repr__(self):
        return f"JFlexRule(pattern={self.pattern!r}, action={self.action!r}, state={self.state!r})"


class JFlexParser:
    """Parser for JFlex lexer specification files"""
    
    def __init__(self):
        self.class_name = "GeneratedLexer"
        self.package_name: Optional[str] = None
        self.imports: List[str] = []
        self.user_code: str = ""
        self.rules: List[JFlexRule] = []
        self.macros: Dict[str, str] = {}
        self.states: List[str] = []
        self.unicode = False
        self.line = False
        self.column = False
    
    def parse_file(self, filename: str):
        """Parse a JFlex file"""
        with open(filename, 'r', encoding='utf-8') as f:
            content = f.read()
        self.parse(content)
    
    def parse(self, content: str):
        """Parse JFlex content"""
        # Split into sections by %%
        sections = re.split(r'^%%\s*$', content, flags=re.MULTILINE)
        
        if len(sections) >= 1:
            self._parse_definitions(sections[0])
        
        if len(sections) >= 2:
            self._parse_rules(sections[1])
        
        if len(sections) >= 3:
            self.user_code = sections[2].strip()
    
    def _parse_definitions(self, section: str):
        """Parse the definitions section"""
        lines = section.split('\n')
        i = 0
        in_code_block = False
        code_block = []
        
        while i < len(lines):
            line = lines[i]
            stripped = line.strip()
            
            # Handle code blocks
            if stripped.startswith('%{'):
                in_code_block = True
                code_block = []
                i += 1
                continue
            elif stripped.startswith('%}'):
                in_code_block = False
                self.imports.extend(code_block)
                i += 1
                continue
            
            if in_code_block:
                code_block.append(line)
                i += 1
                continue
            
            # Handle directives
            if stripped.startswith('%class'):
                match = re.match(r'%class\s+(\w+)', stripped)
                if match:
                    self.class_name = match.group(1)
            elif stripped.startswith('%package'):
                match = re.match(r'%package\s+([\w.]+)', stripped)
                if match:
                    self.package_name = match.group(1)
            elif stripped == '%unicode':
                self.unicode = True
            elif stripped == '%line':
                self.line = True
            elif stripped == '%column':
                self.column = True
            elif stripped.startswith('%state') or stripped.startswith('%xstate'):
                match = re.match(r'%x?state\s+(.+)', stripped)
                if match:
                    states = match.group(1).split()
                    self.states.extend(states)
            elif '=' in stripped and not stripped.startswith('%'):
                # Macro definition: NAME = PATTERN
                match = re.match(r'(\w+)\s*=\s*(.+)', stripped)
                if match:
                    name, pattern = match.groups()
                    self.macros[name] = pattern.strip()
            
            i += 1
    
    def _parse_rules(self, section: str):
        """Parse the rules section"""
        lines = section.split('\n')
        i = 0
        
        while i < len(lines):
            line = lines[i].strip()
            
            if not line or line.startswith('//'):
                i += 1
                continue
            
            # Handle state-specific rules: <STATE>pattern { action }
            state = None
            if line.startswith('<'):
                match = re.match(r'<(\w+)>', line)
                if match:
                    state = match.group(1)
                    line = line[match.end():].strip()
            
            # Handle pattern { action } format
            # Pattern can contain {macro} references and regex
            action_match = re.search(r'\{([^{}]*(?:\{[^{}]*\}[^{}]*)*)\}\s*$', line)
            
            if action_match:
                pattern = line[:action_match.start()].strip()
                action = action_match.group(1).strip()
                
                # Expand macros in pattern
                pattern = self._expand_macros(pattern)
                
                # Handle multi-line actions
                brace_count = 1
                while brace_count > 0 and i + 1 < len(lines):
                    open_count = action.count('{')
                    close_count = action.count('}')
                    brace_count = open_count - close_count
                    if brace_count > 0:
                        i += 1
                        action += '\n' + lines[i]
                
                self.rules.append(JFlexRule(pattern, action, state))
            
            i += 1
    
    def _expand_macros(self, pattern: str) -> str:
        """Expand macro references in a pattern"""
        for name, value in self.macros.items():
            pattern = pattern.replace('{' + name + '}', '(' + value + ')')
        return pattern


class JavaLexerGenerator:
    """Generate Java Lexer code from parsed JFlex definitions"""
    
    def __init__(self, parser: JFlexParser):
        self.parser = parser
    
    def generate(self) -> str:
        """Generate Java code for the Lexer"""
        lines = []
        
        # Package declaration
        if self.parser.package_name:
            lines.append(f"package {self.parser.package_name};")
            lines.append("")
        
        # Imports
        lines.append("import java.util.regex.MatchResult;")
        lines.append("import com.github.aaditmshah.Lexer;")
        lines.append("")
        
        # Additional imports from user code
        for imp in self.parser.imports:
            if imp.strip().startswith('import'):
                lines.append(imp.strip())
        if self.parser.imports:
            lines.append("")
        
        # Class declaration
        lines.append("/**")
        lines.append(" * Generated from JFlex specification")
        lines.append(" * @see jflex2java.py")
        lines.append(" */")
        lines.append(f"public class {self.parser.class_name} {{")
        lines.append("")
        
        # Token constants
        lines.append("    // Token types")
        token_names = set()
        for rule in self.parser.rules:
            # Extract return token name from action
            match = re.search(r'return\s+(\w+)\s*;', rule.action)
            if match:
                token = match.group(1)
                if token != 'null' and token not in token_names:
                    token_names.add(token)
                    lines.append(f'    public static final String {token} = "{token}";')
        lines.append("")
        
        # State constants
        if self.parser.states:
            lines.append("    // Lexer states")
            for i, state in enumerate(self.parser.states):
                lines.append(f"    public static final int STATE_{state} = {i + 1};")
            lines.append("")
        
        # Lexer instance
        lines.append("    private final Lexer lexer;")
        lines.append("")
        
        # Constructor
        lines.append(f"    public {self.parser.class_name}() {{")
        lines.append("        lexer = new Lexer();")
        lines.append("        initRules();")
        lines.append("    }")
        lines.append("")
        
        # Create Lexer method
        lines.append("    /**")
        lines.append("     * Create and configure the Lexer with all rules")
        lines.append("     */")
        lines.append("    private void initRules() {")
        
        for rule in self.parser.rules:
            java_pattern = self._escape_pattern(rule.pattern)
            java_action = self._convert_action(rule.action)
            
            if rule.state:
                state_const = f"STATE_{rule.state}"
                lines.append(f'        lexer.addRule("{java_pattern}", (lex, match) -> {java_action}, {state_const});')
            else:
                lines.append(f'        lexer.addRule("{java_pattern}", (lex, match) -> {java_action});')
        
        lines.append("    }")
        lines.append("")
        
        # getLexer method
        lines.append("    /**")
        lines.append("     * Get the configured Lexer instance")
        lines.append("     */")
        lines.append("    public Lexer getLexer() {")
        lines.append("        return lexer;")
        lines.append("    }")
        lines.append("")
        
        # setInput method
        lines.append("    /**")
        lines.append("     * Set the input string to tokenize")
        lines.append("     */")
        lines.append(f"    public {self.parser.class_name} setInput(String input) {{")
        lines.append("        lexer.setInput(input);")
        lines.append("        return this;")
        lines.append("    }")
        lines.append("")
        
        # nextToken method
        lines.append("    /**")
        lines.append("     * Get the next token from the input")
        lines.append("     */")
        lines.append("    public Lexer.Token nextToken() {")
        lines.append("        return lexer.nextToken();")
        lines.append("    }")
        lines.append("")
        
        # tokenize method
        lines.append("    /**")
        lines.append("     * Tokenize the entire input")
        lines.append("     */")
        lines.append("    public java.util.List<Lexer.Token> tokenize() {")
        lines.append("        return lexer.tokenize();")
        lines.append("    }")
        
        # Close class
        lines.append("}")
        
        return '\n'.join(lines)
    
    def _escape_pattern(self, pattern: str) -> str:
        """Escape a pattern for use in Java string literal"""
        # Remove surrounding quotes if present
        if pattern.startswith('"') and pattern.endswith('"'):
            pattern = pattern[1:-1]
        
        # Escape backslashes (must be first!)
        pattern = pattern.replace('\\', '\\\\')
        
        # Escape quotes
        pattern = pattern.replace('"', '\\"')
        
        return pattern
    
    def _convert_action(self, action: str) -> str:
        """Convert a JFlex action to Java lambda return"""
        action = action.strip()
        
        # Handle /* skip */ or empty action
        if not action or action == '/* skip */' or action == '/* ignore */':
            return 'null'
        
        # Handle simple return statements
        match = re.match(r'return\s+(\w+)\s*;?$', action)
        if match:
            token = match.group(1)
            if token == 'null':
                return 'null'
            return token
        
        # Handle yytext() or match.group()
        if 'yytext()' in action:
            action = action.replace('yytext()', 'match.group()')
        
        # Handle complex actions - wrap in block
        if '{' in action or ';' in action:
            return '{ ' + action + ' }'
        
        return action


def main():
    parser = argparse.ArgumentParser(
        description='Convert JFlex (.flex/.jflex) files to Java Lexer definitions'
    )
    parser.add_argument('input', help='Input JFlex file')
    parser.add_argument('-o', '--output', help='Output Java file')
    parser.add_argument('--class', dest='class_name', help='Override class name')
    parser.add_argument('--package', help='Override package name')
    
    args = parser.parse_args()
    
    # Parse JFlex file
    jflex_parser = JFlexParser()
    jflex_parser.parse_file(args.input)
    
    # Override settings if provided
    if args.class_name:
        jflex_parser.class_name = args.class_name
    if args.package:
        jflex_parser.package_name = args.package
    
    # Generate Java code
    generator = JavaLexerGenerator(jflex_parser)
    java_code = generator.generate()
    
    # Write output
    if args.output:
        with open(args.output, 'w', encoding='utf-8') as f:
            f.write(java_code)
        print(f"Generated: {args.output}")
    else:
        print(java_code)


if __name__ == '__main__':
    main()
