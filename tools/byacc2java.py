#!/usr/bin/env python3
"""
byacc2java.py - Convert byacc-j (.y) files to Java Grammar definitions

This script parses byacc/yacc grammar specification files and generates equivalent
Java code using the com.github.aaditmshah.Grammar class.

YACC/Bison format:
    %{
      // Prologue code
    %}
    
    %token TOKEN1 TOKEN2
    %left '+' '-'
    %right '*' '/'
    
    %%
    
    rule
        : SYMBOL1 SYMBOL2 { action }
        | SYMBOL3         { action }
        ;
    
    %%
    
    // Epilogue code

Usage:
    python byacc2java.py input.y [-o output.java] [--class ClassName]
"""

import re
import sys
import argparse
from typing import List, Tuple, Optional, Dict, Set


class Production:
    """Represents a single grammar production rule"""
    def __init__(self, lhs: str, rhs: List[str], action: str):
        self.lhs = lhs      # Left-hand side (non-terminal)
        self.rhs = rhs      # Right-hand side symbols
        self.action = action  # Semantic action
    
    def __repr__(self):
        return f"Production({self.lhs} -> {' '.join(self.rhs)}, action={self.action!r})"


class YaccParser:
    """Parser for yacc/bison grammar specification files"""
    
    def __init__(self):
        self.class_name = "GeneratedGrammar"
        self.package_name: Optional[str] = None
        self.prologue: str = ""
        self.epilogue: str = ""
        
        self.tokens: Set[str] = set()
        self.left_assoc: Dict[int, List[str]] = {}   # precedence -> tokens
        self.right_assoc: Dict[int, List[str]] = {}
        self.nonassoc: Dict[int, List[str]] = {}
        
        self.start_symbol: Optional[str] = None
        self.productions: List[Production] = []
        self.type_declarations: Dict[str, str] = {}  # symbol -> type
        
        self._current_precedence = 0
    
    def parse_file(self, filename: str):
        """Parse a yacc file"""
        with open(filename, 'r', encoding='utf-8') as f:
            content = f.read()
        self.parse(content)
    
    def parse(self, content: str):
        """Parse yacc content"""
        # Remove comments
        content = self._remove_comments(content)
        
        # Split into sections by %%
        sections = re.split(r'^%%\s*$', content, flags=re.MULTILINE)
        
        if len(sections) >= 1:
            self._parse_declarations(sections[0])
        
        if len(sections) >= 2:
            self._parse_rules(sections[1])
        
        if len(sections) >= 3:
            self.epilogue = sections[2].strip()
    
    def _remove_comments(self, content: str) -> str:
        """Remove C-style comments"""
        # Remove /* */ comments
        content = re.sub(r'/\*.*?\*/', '', content, flags=re.DOTALL)
        # Keep // comments for now as they might be in actions
        return content
    
    def _parse_declarations(self, section: str):
        """Parse the declarations section"""
        lines = section.split('\n')
        i = 0
        in_code_block = False
        code_block = []
        
        while i < len(lines):
            line = lines[i]
            stripped = line.strip()
            
            # Handle %{ ... %} code blocks
            if stripped.startswith('%{'):
                in_code_block = True
                code_block = []
                # Check if %{ has content on same line
                rest = stripped[2:].strip()
                if rest:
                    code_block.append(rest)
                i += 1
                continue
            elif stripped.startswith('%}'):
                in_code_block = False
                self.prologue = '\n'.join(code_block)
                i += 1
                continue
            
            if in_code_block:
                code_block.append(line)
                i += 1
                continue
            
            # Handle directives
            if stripped.startswith('%token'):
                tokens = self._parse_token_list(stripped[6:])
                self.tokens.update(tokens)
            elif stripped.startswith('%left'):
                self._current_precedence += 1
                tokens = self._parse_token_list(stripped[5:])
                self.left_assoc[self._current_precedence] = tokens
                self.tokens.update(tokens)
            elif stripped.startswith('%right'):
                self._current_precedence += 1
                tokens = self._parse_token_list(stripped[6:])
                self.right_assoc[self._current_precedence] = tokens
                self.tokens.update(tokens)
            elif stripped.startswith('%nonassoc'):
                self._current_precedence += 1
                tokens = self._parse_token_list(stripped[9:])
                self.nonassoc[self._current_precedence] = tokens
                self.tokens.update(tokens)
            elif stripped.startswith('%start'):
                match = re.match(r'%start\s+(\w+)', stripped)
                if match:
                    self.start_symbol = match.group(1)
            elif stripped.startswith('%type'):
                # %type <type> symbol1 symbol2 ...
                match = re.match(r'%type\s*<(\w+)>\s+(.+)', stripped)
                if match:
                    type_name = match.group(1)
                    symbols = match.group(2).split()
                    for sym in symbols:
                        self.type_declarations[sym] = type_name
            
            i += 1
    
    def _parse_token_list(self, text: str) -> List[str]:
        """Parse a list of token names"""
        tokens = []
        # Handle both NAME and 'c' style tokens
        for match in re.finditer(r"(\w+)|'(.)'", text):
            if match.group(1):
                tokens.append(match.group(1))
            elif match.group(2):
                # Character literal - convert to token name
                char = match.group(2)
                token_name = self._char_to_token_name(char)
                tokens.append(token_name)
        return tokens
    
    def _char_to_token_name(self, char: str) -> str:
        """Convert a character to a token name"""
        char_names = {
            '+': 'PLUS', '-': 'MINUS', '*': 'STAR', '/': 'SLASH',
            '(': 'LPAREN', ')': 'RPAREN', '[': 'LBRACKET', ']': 'RBRACKET',
            '{': 'LBRACE', '}': 'RBRACE', '<': 'LT', '>': 'GT',
            '=': 'EQ', '!': 'BANG', '&': 'AMP', '|': 'PIPE',
            ',': 'COMMA', '.': 'DOT', ':': 'COLON', ';': 'SEMI',
            '?': 'QUESTION', '^': 'CARET', '%': 'PERCENT', '#': 'HASH',
            '@': 'AT', '~': 'TILDE', '`': 'BACKTICK',
        }
        return char_names.get(char, f'CHAR_{ord(char)}')
    
    def _parse_rules(self, section: str):
        """Parse the rules section"""
        # Combine all lines and parse rules
        text = section.strip()
        
        # Split by rule (look for NAME: or NAME\n:)
        rule_pattern = re.compile(r'(\w+)\s*:\s*')
        
        current_lhs = None
        current_alt = []
        current_action = ""
        
        i = 0
        while i < len(text):
            # Check for rule start
            match = rule_pattern.match(text, i)
            if match:
                # Save previous production if exists
                if current_lhs and (current_alt or current_action):
                    self.productions.append(Production(current_lhs, current_alt, current_action))
                
                current_lhs = match.group(1)
                current_alt = []
                current_action = ""
                i = match.end()
                continue
            
            # Check for alternative separator |
            if text[i] == '|':
                if current_lhs:
                    self.productions.append(Production(current_lhs, current_alt, current_action))
                current_alt = []
                current_action = ""
                i += 1
                continue
            
            # Check for rule end ;
            if text[i] == ';':
                if current_lhs and (current_alt or current_action):
                    self.productions.append(Production(current_lhs, current_alt, current_action))
                current_lhs = None
                current_alt = []
                current_action = ""
                i += 1
                continue
            
            # Check for action { ... }
            if text[i] == '{':
                action, end = self._extract_action(text, i)
                current_action = action
                i = end
                continue
            
            # Check for symbol (word or 'char')
            symbol_match = re.match(r"(\w+)|'(.)'", text[i:])
            if symbol_match:
                if symbol_match.group(1):
                    sym = symbol_match.group(1)
                    # Skip 'error' special token
                    if sym != 'error':
                        current_alt.append(sym)
                elif symbol_match.group(2):
                    char = symbol_match.group(2)
                    current_alt.append(self._char_to_token_name(char))
                i += symbol_match.end()
                continue
            
            # Skip whitespace
            if text[i].isspace():
                i += 1
                continue
            
            # Skip unknown character
            i += 1
    
    def _extract_action(self, text: str, start: int) -> Tuple[str, int]:
        """Extract an action block { ... }"""
        if text[start] != '{':
            return "", start
        
        brace_count = 1
        i = start + 1
        while i < len(text) and brace_count > 0:
            if text[i] == '{':
                brace_count += 1
            elif text[i] == '}':
                brace_count -= 1
            i += 1
        
        action = text[start + 1:i - 1].strip()
        return action, i


class JavaGrammarGenerator:
    """Generate Java Grammar code from parsed yacc definitions"""
    
    def __init__(self, parser: YaccParser):
        self.parser = parser
    
    def generate(self) -> str:
        """Generate Java code for the Grammar"""
        lines = []
        
        # Package declaration
        if self.parser.package_name:
            lines.append(f"package {self.parser.package_name};")
            lines.append("")
        
        # Imports
        lines.append("import java.util.List;")
        lines.append("import com.github.aaditmshah.BisonGrammar;")
        lines.append("import com.github.aaditmshah.Grammar;")
        lines.append("import com.github.aaditmshah.Lexer;")
        lines.append("import static com.github.aaditmshah.BisonGrammar.symbols;")
        lines.append("")
        
        # Class declaration
        lines.append("/**")
        lines.append(" * Generated from YACC/Bison grammar specification")
        lines.append(" * @see byacc2java.py")
        lines.append(" */")
        lines.append(f"public class {self.parser.class_name} {{")
        lines.append("")
        
        # Token constants
        lines.append("    // Token types (terminals)")
        for token in sorted(self.parser.tokens):
            lines.append(f'    public static final String {token} = "{token}";')
        lines.append("")
        
        # Grammar instance
        lines.append("    private final BisonGrammar grammar;")
        lines.append("")
        
        # Constructor
        lines.append(f"    public {self.parser.class_name}() {{")
        lines.append("        grammar = new BisonGrammar();")
        lines.append("        initGrammar();")
        lines.append("    }")
        lines.append("")
        
        # Create Grammar method
        lines.append("    /**")
        lines.append("     * Initialize the grammar with all rules")
        lines.append("     */")
        lines.append("    @SuppressWarnings(\"unchecked\")")
        lines.append("    private void initGrammar() {")
        
        # Declare tokens
        token_list = ', '.join(sorted(self.parser.tokens))
        lines.append(f"        // Declare terminals")
        lines.append(f"        grammar.token({token_list});")
        lines.append("")
        
        # Set start symbol
        if self.parser.start_symbol:
            lines.append(f"        // Start symbol")
            lines.append(f'        grammar.start("{self.parser.start_symbol}");')
            lines.append("")
        
        # Add precedence declarations
        has_precedence = (self.parser.left_assoc or self.parser.right_assoc or self.parser.nonassoc)
        if has_precedence:
            lines.append("        // Operator precedence")
            for level, tokens in sorted(self.parser.left_assoc.items()):
                token_args = ', '.join(tokens)
                lines.append(f"        grammar.left({level}, {token_args});")
            for level, tokens in sorted(self.parser.right_assoc.items()):
                token_args = ', '.join(tokens)
                lines.append(f"        grammar.right({level}, {token_args});")
            for level, tokens in sorted(self.parser.nonassoc.items()):
                token_args = ', '.join(tokens)
                lines.append(f"        grammar.nonassoc({level}, {token_args});")
            lines.append("")
        
        # Add production rules
        lines.append("        // Production rules")
        for prod in self.parser.productions:
            lhs = prod.lhs
            rhs_list = ', '.join(f'"{s}"' for s in prod.rhs) if prod.rhs else ''
            
            if prod.action:
                action_code = self._convert_action(prod.action, prod.rhs)
                if rhs_list:
                    lines.append(f'        grammar.rule("{lhs}", symbols({rhs_list}), vals -> {{')
                else:
                    lines.append(f'        grammar.rule("{lhs}", new String[0], vals -> {{')
                lines.append(f'            {action_code}')
                lines.append(f'        }});')
            else:
                # Default action: return first symbol value
                if rhs_list:
                    lines.append(f'        grammar.rule("{lhs}", symbols({rhs_list}));')
                else:
                    lines.append(f'        grammar.empty("{lhs}");')
            lines.append("")
        
        lines.append("    }")
        lines.append("")
        
        # getGrammar method
        lines.append("    /**")
        lines.append("     * Get the configured Grammar instance")
        lines.append("     */")
        lines.append("    public Grammar getGrammar() {")
        lines.append("        return grammar;")
        lines.append("    }")
        lines.append("")
        
        # parse method
        lines.append("    /**")
        lines.append("     * Parse a list of tokens")
        lines.append("     */")
        lines.append("    public Grammar.ParseResult parse(List<Lexer.Token> tokens) {")
        lines.append("        return grammar.parse(tokens);")
        lines.append("    }")
        lines.append("")
        
        # toGrammarString method
        lines.append("    /**")
        lines.append("     * Get the grammar in bison/yacc format")
        lines.append("     */")
        lines.append("    public String toGrammarString() {")
        lines.append("        return grammar.toGrammarString();")
        lines.append("    }")
        
        # Close class
        lines.append("}")
        
        return '\n'.join(lines)
    
    def _convert_action(self, action: str, rhs: List[str]) -> str:
        """Convert a yacc action to Java code"""
        result = action.strip()
        
        # Replace $$ with return
        if '$$' in result:
            result = re.sub(r'\$\$\s*=\s*', 'return ', result)
        
        # Replace $1, $2, etc. with vals[0], vals[1], etc.
        for i, sym in enumerate(rhs):
            pattern = rf'\${i + 1}'
            if self.parser.tokens and sym in self.parser.tokens:
                # Terminal - it's a Lexer.Token
                replacement = f'((Lexer.Token)vals[{i}])'
            else:
                # Non-terminal - generic Object
                replacement = f'vals[{i}]'
            result = re.sub(pattern, replacement, result)
        
        # Handle @1, @2 location references (simplified)
        result = re.sub(r'@(\d+)', r'/* location \1 */', result)
        
        # If no return statement, add one
        if 'return' not in result:
            result = f'return {result}'
        
        # Ensure it ends with semicolon
        if not result.rstrip().endswith(';'):
            result = result.rstrip() + ';'
        
        return result


def main():
    parser = argparse.ArgumentParser(
        description='Convert byacc-j (.y) files to Java Grammar definitions'
    )
    parser.add_argument('input', help='Input yacc/bison file')
    parser.add_argument('-o', '--output', help='Output Java file')
    parser.add_argument('--class', dest='class_name', help='Override class name')
    parser.add_argument('--package', help='Override package name')
    
    args = parser.parse_args()
    
    # Parse yacc file
    yacc_parser = YaccParser()
    yacc_parser.parse_file(args.input)
    
    # Override settings if provided
    if args.class_name:
        yacc_parser.class_name = args.class_name
    if args.package:
        yacc_parser.package_name = args.package
    
    # Generate Java code
    generator = JavaGrammarGenerator(yacc_parser)
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
