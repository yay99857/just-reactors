"""
Crafts 9 refined trim & corner cap textures live in Pontilha:
- 4 clean armored corner caps (*_casing_corner) so top corners close cleanly without louvers
- 3 sober machine-specific bases (removing diagonal hazard stripes from Turbine, Exchanger, Battery)
- 2 streamlined top edges (removing radiator louvers from Turbine and Battery)
Run: python art-src/textures/build_textures_v6.py
"""
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent))
import build_textures as bt  # noqa: E402
import build_textures_v2 as bt2  # noqa: E402
import build_textures_v4 as bt4  # noqa: E402

ART_DIR = bt4.ART_DIR
BLOCK_DIR = bt4.BLOCK_DIR


def make_corner(pal, flange_a, flange_b, bolt):
    return (
        [
            "kllllllllllllllk",
            "lhhhhhhhhhhhhhhd",
            "kbaahllllllhaabk",
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
            "kbaahnnmmnnhaabk",
            "kbaaddddddddaabk",
        ],
        {**pal, "a": flange_a, "b": flange_b, "B": bolt},
    )


def make_sober_base(pal, seam, p_hi, p_mid, p_dark, rivet):
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
            "SSSSSSSSSSSSSSSS",
            "0111111111111110",
            "12R2222222222R21",
            "0000000000000000",
        ],
        {**pal, "S": seam, "0": p_dark, "1": p_hi, "2": p_mid, "R": rivet},
    )


def make_streamlined_top(pal, groove, bolt):
    return (
        [
            "kllllllllllllllk",
            "lhhhhhhhhhhhhhhd",
            "hnnnnmmmmmmmnnmd",
            "hnBmmmmmmmmmmBnd",
            "hmmmmmnnnmmmmnmd",
            "GGGGGGGGGGGGGGGG",
            "hnnnnmmmmmmmnnmd",
            "hmmmmmmmmmmmmmmd",
            "dmmmmmmmmmmmmmmd",
            "kddddddddddddddk",
            "hnnnnmmmmmmmnnmd",
            "hnrmmmmmmmmmmrmd",
            "hmmmmmnnnmmmmnmd",
            "hmmmnnmmmmmmmmmd",
            "dmmmmmmmmmmmmmmd",
            "kddddddddddddddk",
        ],
        {**pal, "G": groove, "B": bolt},
    )


V6_TEXTURES = {
    "reactor_casing_corner": make_corner(bt.R_PAL, "#2b313a", "#3a414d", "#22272f"),
    "turbine_casing_corner": make_corner(bt.T_PAL, "#343f4d", "#455364", "#26303d"),
    "heat_exchanger_casing_corner": make_corner(bt2.X_PAL, "#312520", "#45352e", "#241b17"),
    "battery_casing_corner": make_corner(bt2.B_PAL, "#232b28", "#313c38", "#19201d"),
    "turbine_casing_base": make_sober_base(bt.T_PAL, "#26303d", "#4c5c70", "#3a4757", "#27313d", "#8ea3ba"),
    "heat_exchanger_casing_base": make_sober_base(bt2.X_PAL, "#291e18", "#594338", "#44332b", "#2c211b", "#a3806c"),
    "battery_casing_base": make_sober_base(bt2.B_PAL, "#1a211e", "#36453f", "#27332e", "#19211e", "#71857d"),
    "turbine_casing_top_edge": make_streamlined_top(bt.T_PAL, "#3a4654", "#2f3a47"),
    "battery_casing_top_edge": make_streamlined_top(bt2.B_PAL, "#1f2724", "#1b221f"),
}


if __name__ == "__main__":
    exported = []
    for name, (rows, legend) in V6_TEXTURES.items():
        assert len(rows) == 16 and all(len(r) == 16 for r in rows), name
        bt4.pontilha_run("doc.new", {"width": 16, "height": 16, "name": name})
        bt4.pontilha_run("draw.pattern", {"x": 0, "y": 0, "rows": rows, "legend": legend})
        bt4.pontilha_cmd(["save", str(ART_DIR / f"{name}.pontilha")])
        out_png = BLOCK_DIR / f"{name}.png"
        bt4.pontilha_cmd(["export", str(out_png), "1"])
        print("crafted & exported", name)
        exported.append((name.replace("heat_exchanger_", "hx_").replace("_casing_", "_"), out_png))
    bt4.build_contact_sheet(exported, ART_DIR / "_contact_sheet_v8.png", scale=6, cols=5)
