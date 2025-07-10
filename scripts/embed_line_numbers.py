import re
import sys

def embed_line_numbers_plaintext(input_path, output_path):
    tag_pattern = re.compile(r'<\s*(/)?\s*([a-zA-Z_][\w\-.]*)([^<>]*)>')

    with open(input_path, 'r', encoding='utf-8') as infile, \
         open(output_path, 'w', encoding='utf-8') as outfile:

        for lineno, line in enumerate(infile, start=1):
            def insert_line_attr(match):
                is_closing = match.group(1)
                tag_name = match.group(2)
                rest = match.group(3).strip()

                if is_closing:
                    return match.group(0)  # Don't modify closing tags like </tag>
                
                # Skip over these lines
                if "<s:NCTE>" in line or "<s:pc>" in line:
                    mytest = match.group(0)
                    return match.group(0)
                
                # Check if line attr already exists
                if re.search(r'\bline\s*=', rest):
                    return match.group(0)

                # Insert line attribute after tag name
                if rest:
                    return f'<{tag_name} line="{lineno}" {rest}>'
                else:
                    return f'<{tag_name} line="{lineno}">'

            new_line = tag_pattern.sub(insert_line_attr, line)
            outfile.write(new_line)

if __name__ == "__main__":

    if len(sys.argv) < 3:
        print("Usage: python embed_line_numbers.py <input_file> <output_file>")
        sys.exit(1)

    input_file = sys.argv[1]
    output_file = sys.argv[2]

    embed_line_numbers_plaintext(input_file, output_file)