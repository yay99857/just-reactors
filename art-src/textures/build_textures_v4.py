"""
Crafts 15 unified trim & connected-glass textures for Turbine, Heat Exchanger, and Battery
live in Pontilha, saves .pontilha sources in art-src/textures/, exports 16x16 PNGs,
and builds _contact_sheet_v6.png over a dark checkerboard for visual inspection.
Run: python art-src/textures/build_textures_v4.py
"""
import json
import os
import shutil
import subprocess
import sys
from pathlib import Path

from PIL import Image, ImageDraw

sys.path.insert(0, str(Path(__file__).parent))
import build_textures as bt  # noqa: E402
import build_textures_v2 as bt2  # noqa: E402

ART_DIR = bt.ART_DIR
BLOCK_DIR = bt.BLOCK_DIR
PONTILHA_EXE = shutil.which("pontilha") or str(
    Path(os.environ.get("LOCALAPPDATA", "")) / "Programs" / "Pontilha" / "pontilha.exe"
)


def make_base(pal, trim, stripe_hi, stripe_lo, stripe_dark):
    return (
        [
            "khhhhhhhhhhhhhhd",
            "hnnnnmmmmmmmnnmd",
            "hnrmmmmmmmmmmrmd",
            "hmmmmmnnnmmmmnmd",
            "hmmmnnmmmmmmmmmd",
            "hmmmmmmmmnnmmmmd",
            "hmnnnmmmmmmmmnmd",
            "hmmmmmmmmmmmmnmd",
            "hmmnnmmmmnnmmmmd",
            "hmmmmmmmmmmmmnmd",
            "hmmmmnnmmmmnmmmd",
            "dmmmmmmmmmmmmmmd",
            "TTTTTTTTTTTTTTTT",
            "ABCCABCCABCCABCC",
            "CABCCABCCABCCABC",
            "CCABCCABCCABCCAB",
        ],
        {**pal, "T": trim, "A": stripe_hi, "B": stripe_lo, "C": stripe_dark},
    )


def make_pillar(pal, flange_a, flange_b, bolt):
    return (
        [
            "kbaahllllllhaabk",
            "kbaahnnmmnnhaabk",
            "kBaaBmmnnmmBaaBk",
            "kbaahmmmmmmhaabk",
            "kbaahmnnnmmhaabk",
            "kbaahmmmmmmhaabk",
            "kBaaBmmnnmmBaaBk",
            "kbaahnnmmmmhaabk",
            "kbaahmmmmmmhaabk",
            "kbaahmmnnmmhaabk",
            "kBaaBmmmmmmBaaBk",
            "kbaahmnnnmmhaabk",
            "kbaahmmmmmmhaabk",
            "kBaaBmmnnmmBaaBk",
            "kbaahmmmmmmhaabk",
            "kbaaddddddddaabk",
        ],
        {**pal, "a": flange_a, "b": flange_b, "B": bolt},
    )


def make_top_edge(pal, vent_mid, vent_dark):
    return (
        [
            "kllllllllllllllk",
            "lhhhhhhhhhhhhhhd",
            "hnnmmmmmmmmmmnmd",
            "hnuuuuuuuuuuuund",
            "hnvvvvvvvvvvvvnd",
            "hnhhhhhhhhhhhhnd",
            "hnvvvvvvvvvvvvnd",
            "hnhhhhhhhhhhhhnd",
            "dmmmmmmmmmmmmmmd",
            "kddddddddddddddk",
            "hnnnnmmmmmmmnnmd",
            "hnrmmmmmmmmmmrmd",
            "hmmmmmnnnmmmmnmd",
            "hmmmnnmmmmmmmmmd",
            "dmmmmmmmmmmmmmmd",
            "kddddddddddddddk",
        ],
        {**pal, "u": vent_mid, "v": vent_dark},
    )


def make_panel(pal, p_dark, p_mid, p_light):
    return (
        [
            "kddddddddddddddk",
            "dssssssssssssssd",
            "dsqqqqqqqqqqqqsd",
            "dsqccccppccccpsd",
            "dsqcrccccccrcpsd",
            "dsqccccccccccpsd",
            "dsqccppccccccpsd",
            "dsqccccccppccpsd",
            "dsqccccccccccpsd",
            "dsqcppcccccccpsd",
            "dsqccccccppccpsd",
            "dsqcccrcccrccpsd",
            "dsqpppppppppppsd",
            "dshhhhhhhhhhhhsd",
            "dmmmmmmmmmmmmmmd",
            "kddddddddddddddk",
        ],
        {**pal, "q": p_dark, "c": p_mid, "p": p_light},
    )


def make_pane(tint, glint_soft, glint_bright):
    return (
        [
            "tttttttttttttttt",
            "twsttttttttttttt",
            "tssttttttttttttt",
            "tttttttttttttttt",
            "tttttttttttttttt",
            "tttttttttttttttt",
            "tttttttttttttttt",
            "tttttttttttttttt",
            "tttttttttttttttt",
            "tttttttttttttttt",
            "tttttttttttttttt",
            "tttttttttttttttt",
            "tttttttttttttttt",
            "tttttttttttttsst",
            "tttttttttttttswt",
            "tttttttttttttttt",
        ],
        {"t": tint, "s": glint_soft, "w": glint_bright},
    )


V4_TEXTURES = {
    # Turbine (T_PAL, aerospace steel-blue)
    "turbine_casing_base": make_base(bt.T_PAL, "#26303d", "#3b8ec4", "#28648f", "#232d38"),
    "turbine_casing_pillar": make_pillar(bt.T_PAL, "#343f4d", "#455364", "#26303d"),
    "turbine_casing_top_edge": make_top_edge(bt.T_PAL, "#343f4d", "#242c36"),
    "turbine_casing_panel": make_panel(bt.T_PAL, "#566475", "#617083", "#69798d"),
    "turbine_glass_pane": make_pane("#c8ecff18", "#e8f8ff55", "#f6fcff70"),
    # Heat Exchanger (X_PAL, warm copper-bronze)
    "heat_exchanger_casing_base": make_base(bt2.X_PAL, "#291e18", "#d97834", "#a6501c", "#2a1e18"),
    "heat_exchanger_casing_pillar": make_pillar(bt2.X_PAL, "#312520", "#45352e", "#241b17"),
    "heat_exchanger_casing_top_edge": make_top_edge(bt2.X_PAL, "#332721", "#231a16"),
    "heat_exchanger_casing_panel": make_panel(bt2.X_PAL, "#5b473e", "#675045", "#71584c"),
    "heat_exchanger_glass_pane": make_pane("#ffe4cc18", "#fff2e255", "#fff9f070"),
    # Battery (B_PAL, dark slate-green)
    "battery_casing_base": make_base(bt2.B_PAL, "#1a211e", "#4fb865", "#2f7a40", "#1d2421"),
    "battery_casing_pillar": make_pillar(bt2.B_PAL, "#232b28", "#313c38", "#19201d"),
    "battery_casing_top_edge": make_top_edge(bt2.B_PAL, "#232b28", "#171d1b"),
    "battery_casing_panel": make_panel(bt2.B_PAL, "#3b4944", "#44544e", "#4b5c56"),
    "battery_glass_pane": make_pane("#c8ffe018", "#e6fff055", "#f4fff870"),
}


def pontilha_run(cmd_id, params):
    res = subprocess.run(
        [PONTILHA_EXE, "run", cmd_id, json.dumps(params)],
        capture_output=True,
        text=True,
    )
    if res.returncode != 0:
        raise RuntimeError(f"pontilha run {cmd_id} failed: {res.stderr or res.stdout}")


def pontilha_cmd(args):
    res = subprocess.run([PONTILHA_EXE, *args], capture_output=True, text=True)
    if res.returncode != 0:
        raise RuntimeError(f"pontilha {' '.join(args)} failed: {res.stderr or res.stdout}")


def build_contact_sheet(entries, out_path, scale=6, cols=5):
    cell = 16 * scale
    cell_w = cell + 16
    cell_h = cell + 26
    rows_n = (len(entries) + cols - 1) // cols
    img = Image.new("RGBA", (cols * cell_w + 16, rows_n * cell_h + 16), (24, 27, 33, 255))
    draw = ImageDraw.Draw(img)
    for idx, (label, path) in enumerate(entries):
        r, c = divmod(idx, cols)
        x0, y0 = 8 + c * cell_w, 8 + r * cell_h
        for cy in range(0, cell, 8):
            for cx in range(0, cell, 8):
                col = (44, 48, 56, 255) if ((cx // 8 + cy // 8) % 2 == 0) else (36, 39, 46, 255)
                draw.rectangle([x0 + 8 + cx, y0 + 4 + cy, x0 + 8 + cx + 7, y0 + 4 + cy + 7], fill=col)
        im = Image.open(path).convert("RGBA")
        im_s = im.resize((cell, cell), Image.NEAREST)
        img.alpha_composite(im_s, (x0 + 8, y0 + 4))
        draw.text((x0 + 6, y0 + cell + 8), label[:16], fill=(220, 226, 236, 255))
    img.save(out_path)
    print("saved", out_path.name)


if __name__ == "__main__":
    exported = []
    for name, (rows, legend) in V4_TEXTURES.items():
        assert len(rows) == 16 and all(len(r) == 16 for r in rows), name
        pontilha_run("doc.new", {"width": 16, "height": 16, "name": name})
        pontilha_run("draw.pattern", {"x": 0, "y": 0, "rows": rows, "legend": legend})
        pontilha_cmd(["save", str(ART_DIR / f"{name}.pontilha")])
        out_png = BLOCK_DIR / f"{name}.png"
        pontilha_cmd(["export", str(out_png), "1"])
        print("crafted & exported", name)
        exported.append((name.replace("heat_exchanger_", "hx_").replace("_casing_", "_"), out_png))
    build_contact_sheet(exported, ART_DIR / "_contact_sheet_v6.png")
