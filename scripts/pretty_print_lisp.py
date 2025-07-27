import re

def tokenize_lisp(code):
    # Add spaces around parentheses for easier tokenizing
    code = re.sub(r'([\(\)])', r' \1 ', code)
    return code.split()

def pretty_print_lisp(tokens, indent='  '):
    output = ''
    level = 0
    i = 0

    while i < len(tokens):
        token = tokens[i]

        if token == '(':
            if output and not output.endswith('\n'):
                output += '\n' + indent * level
            output += '('
            level += 1
            i += 1
            first = True
            while i < len(tokens) and tokens[i] != ')':
                if tokens[i] == '(':
                    output += '\n' + indent * level
                    sub_expr, delta = pretty_print_lisp(tokens[i:], indent)
                    output += sub_expr
                    i += delta
                else:
                    if not first:
                        output += ' '
                    output += tokens[i]
                    i += 1
                    first = False
            output += ')'
            level -= 1
            i += 1
        elif token == ')':
            return output, i
        else:
            output += token
            i += 1

    return output, i

def beautify_lisp(code):
    tokens = tokenize_lisp(code)
    result, _ = pretty_print_lisp(tokens)
    return result

# Example usage
if __name__ == '__main__':
    import argparse
    parser = argparse.ArgumentParser(description="Pretty print a Lisp file.")
    parser.add_argument("input", help="Path to the input Lisp file")
    args = parser.parse_args()
    
    with open(args.input, 'r') as f:
        content = f.read()
        
    beautified = beautify_lisp(content)
    print(beautified)