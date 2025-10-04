import os
from collections import Counter

def analyze_file(file_path):
    try:
        with open(file_path, 'r', encoding='utf-8') as file:
            lines = file.readlines()

        line_count = len(lines)
        char_count = sum(len(line) for line in lines)
        empty_line_count = sum(1 for line in lines if line.strip() == '')
        char_freq = Counter(''.join(lines))

        return {
            'line_count': line_count,
            'char_count': char_count,
            'empty_line_count': empty_line_count,
            'char_freq': char_freq
        }
    except FileNotFoundError:
        print(f"Ошибка: Файл '{file_path}' не найден.")
        return None
    except Exception as e:
        print(f"Произошла ошибка: {e}")
        return None

def print_results(analysis, options):
    if '1' in options:
        print(f"Количество строк: {analysis['line_count']}")
    if '2' in options:
        print(f"Количество символов: {analysis['char_count']}")
    if '3' in options:
        print(f"Количество пустых строк: {analysis['empty_line_count']}")
    if '4' in options:
        print("Частотный словарь символов:")
        for char, freq in analysis['char_freq'].items():
            if char.isspace():
                char_display = repr(char)[1:-1]
            else:
                char_display = char
            print(f"'{char_display}': {freq}")

def main():
    file_path = input("")

    print("1. Количество строк")
    print("2. Количество символов")
    print("3. Количество пустых строк")
    print("4. Частотный словарь символов")

    options = input().split()
    analysis = analyze_file(file_path)
    if analysis:
        print_results(analysis, options)

if __name__ == "__main__":
    main()