import xml.dom.minidom
import xml.etree.ElementTree as ET
import re
import sys

def remove_illegal_chars(lisp_str):
    
    #remove all occurences of /'
    result  = re.sub(r"\\'", "'", lisp_str)

    #remove all occurences of /"
    result  = re.sub(r'\\"', '', result)

    #replace [66,70]) with )
    result = re.sub(r"\[.+\]\)", ")", result)

    #replace & with and
    result = re.sub(r"&", "and", result)

    #replace <text> with text
    result = re.sub(r"<(.*?)>", r"\1", result)

    #remove all the Notes because they have frequent illegal characters
    result = re.sub(r'\(Note \(NoteNumber 1\).*', '', result, flags=re.DOTALL)

    #add the final parenthesis that was removed with Notes
    result = result + ")"

    #print(result)

    return result

def lisp_to_xml(lisp_str):
    def tokenize(s):
        s = s.replace('(', ' ( ').replace(')', ' ) ').replace('"', ' " ')
        tokens = []
        current = ''
        in_quotes = False
        for char in s:
            if char == '"':
                in_quotes = not in_quotes
                if not in_quotes and current:
                    tokens.append(current)
                    current = ''
                continue
            if in_quotes:
                current += char
            elif char.isspace():
                if current:
                    tokens.append(current)
                    current = ''
            else:
                current += char
        if current:
            tokens.append(current)
        return tokens

    def parse(tokens):
        if not tokens:
            return None
        token = tokens.pop(0)
        if token == '(':
            lst = []
            while tokens and tokens[0] != ')':
                lst.append(parse(tokens))
            if tokens:
                tokens.pop(0)  # Remove ')'
            return lst
        return token

    def to_xml(lst, indent=0):
        if not lst:
            return ''
        if isinstance(lst, str):
            return lst
        tag = lst[0]
        content = []
        for item in lst[1:]:
            if isinstance(item, list):
                content.append(to_xml(item, indent + 2))
            else:
                content.append(item)
        indent_str = ' ' * indent
        if content:
            return (f'{indent_str}<{tag}>\n' +
                    '\n'.join(c for c in content if c) +
                    f'\n{indent_str}</{tag}>')
        return f'{indent_str}<{tag}/>'

    tokens = tokenize(lisp_str)
    parsed = parse(tokens)
    return to_xml(parsed)

def trim_element_text(element):
    """Recursively trim whitespace from the text of an element and its children."""
    if element.text is not None:
        element.text = element.text.strip()
    for subelement in element:
        trim_element_text(subelement)
        if subelement.tail:
            subelement.tail = subelement.tail.strip()

def trim_xml_string(xml_string):
    """Trim whitespace from an XML string."""
    #print(xml_string)
    root = ET.fromstring(xml_string)
    trim_element_text(root)
    return ET.tostring(root, encoding='unicode')

if len(sys.argv) > 1:
    input_file = sys.argv[1]

with open(input_file, 'r') as file:
    lisp_input = file.read()

lisp_clean = remove_illegal_chars(lisp_input)
xml_output = trim_xml_string(lisp_to_xml(lisp_clean))
dom = xml.dom.minidom.parseString(xml_output)
pretty_xml = dom.toprettyxml(indent="   ")

print(pretty_xml)