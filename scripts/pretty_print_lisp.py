def pretty_print_lisp(input_file, output_file=None, indent_width=2):
    with open(input_file, 'r') as f:
        content = f.read()

    # Remove comments (optional: only remove inline ';' comments)
    lines = content.splitlines()
    stripped = []
    for line in lines:
        clean_line = line.split(';')[0]  # remove anything after ';'
        stripped.append(clean_line.strip())

    content = ' '.join(stripped)
    formatted = []
    indent = 0
    token = ''
    in_string = False

    for c in content:
        if c == '"':
            token += c
            in_string = not in_string
        elif in_string:
            token += c
        elif c == '(':
            if token.strip():
                formatted.append(' ' * indent + token.strip())
                token = ''
            formatted.append(' ' * indent + '(')
            indent += indent_width
        elif c == ')':
            if token.strip():
                formatted.append(' ' * indent + token.strip())
                token = ''
            indent -= indent_width
            formatted.append(' ' * indent + ')')
        elif c.isspace():
            if token.endswith(' '):
                continue
            token += ' '
        else:
            token += c

    if token.strip():
        formatted.append(' ' * indent + token.strip())

    result = '\n'.join(formatted)

    if output_file:
        with open(output_file, 'w') as f:
            f.write(result)
    else:
        print(result)


# Example usage
if __name__ == "__main__":
    import argparse
    parser = argparse.ArgumentParser(description="Pretty print a Lisp file.")
    parser.add_argument("input", help="Path to the input Lisp file")
    parser.add_argument("-o", "--output", help="Path to the output file (optional)")
    args = parser.parse_args()

    pretty_print_lisp(args.input, args.output)