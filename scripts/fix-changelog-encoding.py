#!/usr/bin/env python3
"""
Script de correção de codificação e sanitização de formatação do CHANGELOG.md.
- Reverte mojibake progressivo decodificando recursivamente até estabilizar.
- Substitui notações de fórmulas LaTeX por texto simples legível.
- Regrava em UTF-8 estrito sem BOM.
"""
import os
import re
import sys

def cp1252_flexible_encode(text: str) -> bytes:
    res = bytearray()
    for c in text:
        try:
            res.extend(c.encode("cp1252"))
        except UnicodeEncodeError:
            code = ord(c)
            if 0x80 <= code <= 0xFF:
                res.append(code)
            else:
                raise
    return bytes(res)

def decode_mojibake(line: str) -> str:
    cur = line
    while True:
        try:
            b = cp1252_flexible_encode(cur)
            nxt = b.decode("utf-8")
            if nxt == cur:
                break
            cur = nxt
        except Exception:
            break
    return cur

def sanitize_latex(text: str) -> str:
    # Mapeamentos pontuais de fórmulas e símbolos LaTeX encontrados
    replacements = [
        (r"\$\\rightarrow\$", "->"),
        (r"\$\\le\s*3\$", "<= 3"),
        (r"\$\\le\s*7\$", "<= 7"),
        (r"\$\\le\s*4\$", "<= 4"),
        (r"\$\\le\s*1\$", "<= 1"),
        (r"\$\\le\s*60\\%\$", "<= 60%"),
        (r"\$\\le\s*50\\%\$", "<= 50%"),
        (r"\$\\le\s*10\\%\$", "<= 10%"),
        (r"\$\\ge\s*15\\%\$", ">= 15%"),
        (r"\$10\\%\$", "10%"),
        (r"\$35\\%\$", "35%"),
        (r"\$\\text\{Total Orçado\}\s*=\s*\\text\{Total Contas do Mês\}\s*\+\s*\\text\{Aportes em Metas\}\$",
         "Total Orçado = Total Contas do Mês + Aportes em Metas"),
    ]
    for pattern, repl in replacements:
        text = re.sub(pattern, repl, text)
    return text

def main():
    root_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    target_file = os.path.join(root_dir, "CHANGELOG.md")

    if not os.path.exists(target_file):
        print(f"Erro: {target_file} não encontrado.")
        sys.exit(1)

    print(f"Lendo {target_file}...")
    with open(target_file, "r", encoding="utf-8") as f:
        lines = f.readlines()

    fixed_lines = []
    decoded_count = 0
    for l in lines:
        fixed = decode_mojibake(l)
        if fixed != l:
            decoded_count += 1
        fixed_lines.append(fixed)

    content = "".join(fixed_lines)
    sanitized_content = sanitize_latex(content)

    # Escrever em UTF-8 puro sem BOM
    with open(target_file, "w", encoding="utf-8", newline="\n") as f:
        f.write(sanitized_content)

    print(f"Sucesso: {decoded_count} linha(s) com mojibake revertida(s).")
    print(f"LaTeX substituído por texto simples.")
    print(f"Arquivo gravado em UTF-8 sem BOM: {target_file}")

if __name__ == "__main__":
    main()
