"""
Reactor visual overhaul textures (v3 additions), crafted live in Pontilha.
Records all 7 new/updated 16x16 reactor textures and builds a 6x contact sheet (_contact_sheet_v5.png).
Run: python art-src/textures/build_textures_v3.py
"""
import sys
from pathlib import Path

from PIL import Image

sys.path.insert(0, str(Path(__file__).parent))
import build_textures as bt  # noqa: E402
import build_textures_v2 as bt2  # noqa: E402

BLOCK_DIR = bt.BLOCK_DIR
R_PAL = bt.R_PAL

V3_TEXTURES = {
    "reactor_fuel_rod_copper": (
        [
            "0111111111111110",
            "1444333223344321",
            "13b3222pq2222b21",
            "1222222112222210",
            "0111111111111110",
            "1443333223334321",
            "23344432233432b1",
            "23b3332pq2232210",
            "2223322112222210",
            "1222222112222b10",
            "0111111111111110",
            "1343222222234321",
            "12b22pq222222b21",
            "1222211222222210",
            "1111111111111110",
            "0000000000000000",
        ],
        {
            "0": "#332018", "1": "#7a3f22", "2": "#a85b32", "3": "#cf7e4c", "4": "#ebac7c",
            "p": "#4b8f7a", "q": "#6ab59b", "b": "#252a33",
        },
    ),
    "reactor_fuel_rod": (
        [
            "klllllllllllllld",
            "kddddddddddddddk",
            "kdrg01123210grdk",
            "kdrg11233321grdk",
            "kdsg01122210gsdk",
            "kdrg00011100grdk",
            "kdrg01123210grdk",
            "kdsg11233321gsdk",
            "kdrg01122210grdk",
            "kdrg00011100grdk",
            "kdsg01123210gsdk",
            "kdrg11233321grdk",
            "kdrg01122210grdk",
            "kdsg00011100gsdk",
            "lhhhhhhhhhhhhhhd",
            "kddddddddddddddk",
        ],
        {**R_PAL, "g": "#26333b", "0": "#3d591b", "1": "#5d8529", "2": "#85b842", "3": "#aed96a"},
    ),
    "reactor_casing_base": (
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
            "yYKKyYKKyYKKyYKK",
            "KyYKKyYKKyYKKyYK",
            "KKyYKKyYKKyYKKyY",
        ],
        {**R_PAL, "T": "#252a33", "K": "#23272e", "y": "#c99a28", "Y": "#a3771a"},
    ),
    "reactor_casing_pillar": (
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
        {**R_PAL, "a": "#2b313a", "b": "#3a414d", "B": "#22272f"},
    ),
    "reactor_casing_top_edge": (
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
        {**R_PAL, "u": "#2b323d", "v": "#1e232b"},
    ),
    "reactor_casing_panel": (
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
        {**R_PAL, "q": "#434b57", "c": "#4c5563", "p": "#525b6a"},
    ),
    "reactor_glass_pane": (
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
        {"t": "#b8e4ff18", "s": "#e2f6ff55", "w": "#f4fbff70"},
    ),
}


if __name__ == "__main__":
    for name, (rows, legend) in V3_TEXTURES.items():
        out = BLOCK_DIR / f"{name}.png"
        bt2.render(rows, legend).save(out)
        print("wrote", out.name)
    sheet = Image.new("RGBA", (len(V3_TEXTURES) * 100, 100), (40, 40, 40, 255))
    for i, (name, (rows, legend)) in enumerate(V3_TEXTURES.items()):
        sheet.paste(bt2.render(rows, legend).resize((96, 96), Image.NEAREST), (i * 100 + 2, 2))
    out_sheet = Path(__file__).parent / "_contact_sheet_v5.png"
    sheet.save(out_sheet)
    print("saved", out_sheet.name)
