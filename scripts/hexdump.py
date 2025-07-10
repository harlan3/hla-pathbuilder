import sys

def hexdump(filename, bytes_per_line=16):
    """
    Generates a hex dump of a file.

    Args:
        filename (str): The path to the file to dump.
        bytes_per_line (int, optional): Number of bytes to display per line. Defaults to 16.
    """
    try:
        with open(filename, 'rb') as f:
            offset = 0
            while True:
                chunk = f.read(bytes_per_line)
                if not chunk:
                    break

                hex_values = ' '.join(f'{byte:02x}' for byte in chunk)
                ascii_values = ''.join(chr(byte) if 32 <= byte <= 126 else '.' for byte in chunk)

                print(f'{offset:08x}  {hex_values:<{3 * bytes_per_line - 1}}  |{ascii_values}|')
                offset += bytes_per_line
    except FileNotFoundError:
        print(f"Error: File not found: {filename}")
    except Exception as e:
        print(f"An error occurred: {e}")
        
hexdump(sys.argv[1])
