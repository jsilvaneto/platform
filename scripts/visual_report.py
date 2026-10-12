#!/usr/bin/env python3
"""Gera build/visual-report/index.html com todas as telas lado a lado (tela x tema) e exibição de diffs."""
import html
import os
import pathlib
import re
import shutil
import sys

def generate_report():
    src_dir = pathlib.Path(sys.argv[1] if len(sys.argv) > 1 else "app/src/test/snapshots/images")
    out_dir = pathlib.Path("build/visual-report")
    out_images_dir = out_dir / "images"
    out_images_dir.mkdir(parents=True, exist_ok=True)

    base_pat = re.compile(r"^(?P<screen>.+)_(?P<theme>amoled|dark|light)_font(?P<font>[\d.]+)\.png$")
    diff_pat = re.compile(r"^(?P<screen>.+)_(?P<theme>amoled|dark|light)_font(?P<font>[\d.]+)_(?P<type>actual|compare)\.png$")

    # Coleta arquivos base e comparações
    groups = {}
    diffs = {}

    if src_dir.exists():
        for p in sorted(src_dir.glob("*.png")):
            # Copia imagem para a pasta do relatório tornando-o portátil
            dest_img = out_images_dir / p.name
            shutil.copy2(p, dest_img)

            dm = diff_pat.match(p.name)
            if dm:
                key = (dm["screen"], dm["font"], dm["theme"])
                diffs.setdefault(key, {})[dm["type"]] = dest_img.name
                continue

            bm = base_pat.match(p.name)
            if bm:
                key = (bm["screen"], bm["font"])
                groups.setdefault(key, {})[bm["theme"]] = dest_img.name

    has_changes = len(diffs) > 0
    changed_keys = set((k[0], k[1]) for k in diffs.keys())

    changed_sections = []
    unchanged_sections = []

    for (screen, font), themes in sorted(groups.items()):
        is_changed = (screen, font) in changed_keys

        if is_changed:
            # Exibe Comparação: Antes, Depois, Diferença
            diff_cells = []
            for t in ("amoled", "dark", "light"):
                diff_info = diffs.get((screen, font, t), {})
                actual_img = diff_info.get("actual")
                compare_img = diff_info.get("compare")
                base_img = themes.get(t)

                if compare_img and actual_img:
                    diff_cells.append(
                        f'<div class="diff-card">'
                        f'  <h3>Tema: {t.upper()}</h3>'
                        f'  <div class="row">'
                        f'    <figure><figcaption>Antes (Golden)</figcaption><img src="images/{base_img}"></figure>'
                        f'    <figure><figcaption>Depois (Atual)</figcaption><img src="images/{actual_img}"></figure>'
                        f'    <figure class="highlight-diff"><figcaption>Diferença (Diff)</figcaption><img src="images/{compare_img}"></figure>'
                        f'  </div>'
                        f'</div>'
                    )
            section = (
                f'<section class="changed-screen">'
                f'  <div class="badge-changed">ALTERADA</div>'
                f'  <h2>{html.escape(screen)} · fonte {font}x</h2>'
                f'  {"".join(diff_cells)}'
                f'</section>'
            )
            changed_sections.append(section)
        else:
            cells = "".join(
                f'<figure><figcaption>{t}</figcaption><img src="images/{themes[t]}"></figure>'
                for t in ("amoled", "dark", "light") if t in themes
            )
            section = (
                f'<div class="screen-block">'
                f'  <h3>{html.escape(screen)} · fonte {font}x</h3>'
                f'  <div class="row">{cells}</div>'
                f'</div>'
            )
            unchanged_sections.append(section)

    style = """
    body { background: #0c0d0e; color: #f1f5f9; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; padding: 24px; line-height: 1.5; }
    h1 { font-size: 24px; font-weight: 700; margin-bottom: 8px; color: #fff; }
    .subtitle { color: #94a3b8; font-size: 14px; margin-bottom: 24px; }
    .status-badge { display: inline-block; padding: 4px 10px; border-radius: 9999px; font-size: 12px; font-weight: 600; margin-bottom: 20px; }
    .status-ok { background: #064e3b; color: #34d399; border: 1px solid #059669; }
    .status-diff { background: #7c2d12; color: #fb923c; border: 1px solid #ea580c; }
    .badge-changed { display: inline-block; padding: 2px 8px; background: #dc2626; color: white; border-radius: 4px; font-size: 11px; font-weight: bold; margin-bottom: 6px; }
    .changed-screen { background: #1e1b18; border: 1px solid #b45309; border-radius: 8px; padding: 16px; margin-bottom: 24px; }
    .diff-card { margin-top: 12px; padding: 12px; background: #171717; border-radius: 6px; }
    .diff-card h3 { font-size: 13px; color: #fb923c; margin-top: 0; text-transform: uppercase; }
    .highlight-diff img { border: 2px solid #ef4444 !important; }
    .row { display: flex; gap: 16px; overflow-x: auto; padding-bottom: 8px; }
    figure { margin: 0; display: flex; flex-direction: column; align-items: center; }
    figcaption { font-size: 12px; color: #94a3b8; margin-bottom: 6px; font-weight: 600; text-transform: uppercase; }
    img { width: 310px; border: 1px solid #334155; border-radius: 8px; background: #000; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.3); }
    details { background: #13171f; border: 1px solid #1e293b; border-radius: 8px; padding: 14px; margin-top: 20px; }
    summary { font-size: 15px; font-weight: 600; cursor: pointer; color: #cbd5e1; outline: none; }
    summary:hover { color: #fff; }
    .screen-block { margin-top: 20px; padding-bottom: 16px; border-bottom: 1px solid #1e293b; }
    .screen-block:last-child { border-bottom: none; }
    .screen-block h3 { font-size: 15px; margin: 0 0 10px 0; color: #e2e8f0; }
    """

    header_html = (
        f"<h1>Relatório Visual Único · Platform Android</h1>"
        f'<div class="subtitle">Auditoria de telas por tema e escala de fonte</div>'
    )

    if has_changes:
        status_html = f'<div class="status-badge status-diff">⚠️ {len(changed_keys)} tela(s) com alterações visuais detectadas</div>'
    else:
        status_html = f'<div class="status-badge status-ok">✓ Todas as telas em conformidade com as baselines ({len(groups)} conjuntos)</div>'

    body_html = [header_html, status_html]

    if changed_sections:
        body_html.append("".join(changed_sections))

    if unchanged_sections:
        if has_changes:
            body_html.append(f'<details><summary>Telas sem alterações ({len(unchanged_sections)})</summary>')
            body_html.append("".join(unchanged_sections))
            body_html.append("</details>")
        else:
            body_html.append("".join(unchanged_sections))

    full_html = f"<!DOCTYPE html><html lang='pt-BR'><head><meta charset='utf-8'><title>Relatório Visual</title><style>{style}</style></head><body>{''.join(body_html)}</body></html>"

    report_file = out_dir / "index.html"
    report_file.write_text(full_html, encoding="utf-8")
    print(f"Relatório gerado em: {report_file.resolve()}")

if __name__ == "__main__":
    generate_report()
