"""
Crafts 4 clean optical connected-glass panes (zero repeating dot glints) and the emissive
reactor_pulse_glow sprite live in Pontilha, saves .pontilha sources, exports 16x16 PNGs,
and generates _contact_sheet_v7.png.
Run: python art-src/textures/build_textures_v5.py
"""
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent))
import build_textures_v4 as bt4  # noqa: E402

ART_DIR = bt4.ART_DIR
BLOCK_DIR = bt4.BLOCK_DIR


def make_clean_pane(tint):
    return (["tttttttttttttttt"] * 16, {"t": tint})


def make_pulse_glow():
    # Bright near-white emissive core fading gently at left/right edges for crisp vertical light bands
    row = "0123344444433210"
    return (
        [row] * 16,
        {
            "0": "#c8ffbb",
            "1": "#d8ffd0",
            "2": "#e6ffe0",
            "3": "#f0ffea",
            "4": "#f8fff0",
        },
    )


V5_TEXTURES = {
    "reactor_glass_pane": make_clean_pane("#b8e4ff16"),
    "turbine_glass_pane": make_clean_pane("#c8ecff16"),
    "heat_exchanger_glass_pane": make_clean_pane("#ffe4cc16"),
    "battery_glass_pane": make_clean_pane("#c8ffe016"),
    "reactor_pulse_glow": make_pulse_glow(),
}


if __name__ == "__main__":
    exported = []
    for name, (rows, legend) in V5_TEXTURES.items():
        assert len(rows) == 16 and all(len(r) == 16 for r in rows), name
        bt4.pontilha_run("doc.new", {"width": 16, "height": 16, "name": name})
        bt4.pontilha_run("draw.pattern", {"x": 0, "y": 0, "rows": rows, "legend": legend})
        bt4.pontilha_cmd(["save", str(ART_DIR / f"{name}.pontilha")])
        out_png = BLOCK_DIR / f"{name}.png"
        bt4.pontilha_cmd(["export", str(out_png), "1"])
        print("crafted & exported", name)
        exported.append((name, out_png))
    bt4.build_contact_sheet(exported, ART_DIR / "_contact_sheet_v7.png", scale=6, cols=5)
