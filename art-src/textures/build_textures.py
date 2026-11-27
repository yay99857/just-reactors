"""
Crafts community-standard Minecraft tech-mod pixel-art textures for Just Reactors
via Pontilha (inspired by AE2, Mekanism/MekanismGenerators, and Vanilla Jappa standards),
saves .pontilha native sources, exports native PNGs, and builds two 6x visual inspection sheets:
- _contact_sheet_v3.png: all 38 Just Reactors textures + Voltaic cables
- _comparison_v3.png: side-by-side comparison vs AE2/Mekanism/Vanilla refs + 3x3 multiblock wall mockups
"""
import json
import subprocess
from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(r"C:\Users\yay\www\just-reactors")
ART_DIR = ROOT / "art-src" / "textures"
BLOCK_DIR = ROOT / "src" / "main" / "resources" / "assets" / "justreactors" / "textures" / "block"
ITEM_DIR = ROOT / "src" / "main" / "resources" / "assets" / "justreactors" / "textures" / "item"
LOGO_PATH = ROOT / "src" / "main" / "resources" / "justreactors_logo.png"

VOLTAIC_BLOCK = Path(r"C:\Users\yay\www\voltaic-dynamics\src\main\resources\assets\voltaicdynamics\textures\block")
REF_ROOT = Path(r"C:\Users\yay\.gemini\antigravity-cli\brain\bfa9055c-adcb-4446-bf66-b97fcfbd36a6\scratch\refs")
MEK_TEX = REF_ROOT / "mekanism" / "src" / "main" / "resources" / "assets" / "mekanism" / "textures"
MEKG_TEX = REF_ROOT / "mekanism" / "src" / "generators" / "resources" / "assets" / "mekanismgenerators" / "textures"
AE2_TEX = REF_ROOT / "ae2" / "src" / "main" / "resources" / "assets" / "ae2" / "textures"
VANILLA_TEX = REF_ROOT / "vanilla_extract"

ART_DIR.mkdir(parents=True, exist_ok=True)
BLOCK_DIR.mkdir(parents=True, exist_ok=True)
ITEM_DIR.mkdir(parents=True, exist_ok=True)

# Reactor casing: readable medium-dark brushed titanium-gunmetal (Mekanism Fission Reactor inspired)
R_PAL = {
    "k": "#323842",  # outer 1px panel seam / shadow
    "d": "#444c58",  # bottom-right bevel & deep steel
    "s": "#3a404b",  # muted fastener recess
    "r": "#4d5563",  # muted corner bolt (never harsh white dots)
    "m": "#58616f",  # brushed steel base mid
    "n": "#626c7b",  # subtle brushed steel highlight streak
    "h": "#768192",  # top-left inner bevel
    "l": "#8c98aa",  # top-left bright chamfer edge
}

# Turbine casing: lighter aerospace brushed steel-blue (Mekanism Turbine inspired)
T_PAL = {
    "k": "#424e5c",
    "d": "#576576",
    "s": "#4b5868",
    "r": "#5e6d80",
    "m": "#708094",
    "n": "#7c8da3",
    "h": "#92a4b8",
    "l": "#aabbd0",
}

def make_industrial_casing(pal):
    """
    Calm, cohesive brushed titanium/steel panel (Mekanism Fission/Turbine casing weight):
    1px soft border seam + 1px bevel + smooth horizontal-brushed plate interior so
    3x3 / 5x5 multiblock walls tile cleanly without chunky grid borders.
    """
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
            "hmmnmmmmmmmmmnmd",
            "hmmmmmmmnnmmmmmd",
            "hnrmmmmmmmmmmrmd",
            "dmmmmmmmmmmmmmmd",
            "kddddddddddddddk",
        ],
        pal,
    )

def make_framed(pal, center_rows, extra_legend):
    """
    16x16 machined casing border (3px beveled plate matching industrial_casing) + 10x10 recessed module.
    Uses ONLY uppercase/digit keys in extra_legend so it never collides with lowercase pal keys.
    """
    base = [
        list("khhhhhhhhhhhhhhd"),
        list("hnnnnmmmmmmmnnmd"),
        list("hnrmmmmmmmmmmrmd"),
    ]
    for i in range(10):
        c_row = center_rows[i]
        assert len(c_row) == 10, f"Center row {i} != 10: {c_row}"
        base.append(list("hmm" + c_row + "mmd"))
    base.extend([
        list("hnrmmmmmmmmmmrmd"),
        list("dmmmmmmmmmmmmmmd"),
        list("kddddddddddddddk"),
    ])
    rows = ["".join(r) for r in base]
    leg = dict(pal)
    leg.update(extra_legend)
    return rows, leg

def make_ingot(p0, p1, p2, p3, p4, p5):
    """
    Canonical Minecraft / Mekanism 3D isometric ingot brick silhouette with
    sculpted metallic sheen streaks and hue-shifted outline.
    """
    rows = [
        "................",
        "................",
        "..........11....",
        ".......111442...",
        "....1114544532..",
        ".11145544455432.",
        "1544544455444542",
        "1445443444433320",
        "1444333333333220",
        "1334432222322320",
        "133332222222200.",
        ".133422222200...",
        "..123222000.....",
        "...11000........",
        "................",
        "................",
    ]
    return rows, {"0": p0, "1": p1, "2": p2, "3": p3, "4": p4, "5": p5}

def make_plated_storage_block(p0, p1, p2, p3, p4):
    """Heavy stacked forged horizontal plates with diagonal sheen (inspired by Mekanism uranium/lead blocks)."""
    rows = [
        "1444444444444430",
        "4333344333333321",
        "4334433333322321",
        "4333333322222221",
        "1222222222222210",
        "0111111111111110",
        "1444444444444430",
        "4333344333333321",
        "4334433333332221",
        "4333333332222221",
        "1222222222222210",
        "0111111111111110",
        "1444444444444430",
        "4334433333333221",
        "4322222222222210",
        "0111111111111110",
    ]
    return rows, {"0": p0, "1": p1, "2": p2, "3": p3, "4": p4}

def make_brushed_storage_block(p0, p1, p2, p3, p4):
    """Chamfered brushed alloy block (inspired by Mekanism steel/osmium blocks)."""
    rows = [
        "1444444444444430",
        "4333344333333320",
        "4133443333433120",
        "4334433334433320",
        "4344333344332320",
        "4333333443322320",
        "4333334433223320",
        "4333333332233320",
        "4333443322333320",
        "4334433333332320",
        "4344333333322320",
        "4333333344223320",
        "4333333443333320",
        "4133334433322120",
        "3222222222222210",
        "0000000000000000",
    ]
    return rows, {"0": p0, "1": p1, "2": p2, "3": p3, "4": p4}

def make_ore(stone_type, v0, v1, v2, v3):
    """
    Authentic Jappa/Mekanism stratified stone & deepslate with 5 distinct
    asymmetric crystal veins of varying sizes and under-vein rock crevice AO ('O').
    """
    if stone_type == "stone":
        bg = [
            "CDDDCCCBBBCCCBCC",
            "BBCCCCDDBBAAACBB",
            "AABBCCCDDCCBBAAA",
            "CCBBBAAAABBBCCCD",
            "DDDDCCBBBAAACCCB",
            "CCBBBAACCDDDDCCB",
            "AABCCDDDCCCBBBAA",
            "BBCCBBBAAAABCCDD",
            "CDDDCCCBBBBCCCCB",
            "ABBBCCCDDDCCBBAA",
            "CCBBAAAABBBBCCCD",
            "DDDDCCBBBCCCDDCC",
            "BBCCCAACCDDDCCBB",
            "AABCCDDDCCCBBBAA",
            "CCBBBBAAACCCDDDD",
            "DDCCCBBBBCCCCBBA",
        ]
        leg = {"A": "#6b6b6b", "B": "#767676", "C": "#828282", "D": "#8f8f8f", "O": "#4c4c4c"}
    else:
        bg = [
            "ABBBCCCCDDDCCCBB",
            "CCDDDDCCCCBBBBAA",
            "AAAAABBBBBCCCDDD",
            "CDDDCCCCBBBAABBC",
            "BBBAAACCCDDDDCCB",
            "AABBCCCCBBBBAAAA",
            "DDDDCCCBBBAACCCD",
            "CCBBAAAABBBCCCCB",
            "ABBBCCCDDDDCCCBB",
            "CCCCBBBAAAABBBBB",
            "AAABBBBCCCDDDDAA",
            "CCDDDDCCCBBBBCCC",
            "BBBBAAACCCDDDDAB",
            "AAAABBBBCCCCBBBA",
            "CDDDDCCCBBBAAACC",
            "BBCCCCBBBBAAAABB",
        ]
        leg = {"A": "#2b2b31", "B": "#36363c", "C": "#434349", "D": "#515158", "O": "#1d1d22"}

    # Asymmetric shards + streaks + small flecks, with bottom-edge AO ('O')
    veins = [
        "................",
        "....32..........",
        "...3210....23...",
        "....10O....102..",
        ".....O......OO..",
        "................",
        "................",
        ".......320......",
        "..23...1321.....",
        "..1O....OO10....",
        "...........O....",
        "............23..",
        "...321.....3210.",
        "..2310......1OO.",
        "...OOO..........",
        "................",
    ]
    merged = []
    for y in range(16):
        row = []
        for x in range(16):
            ch = veins[y][x]
            row.append(ch if ch != "." else bg[y][x])
        merged.append("".join(row))
    leg.update({"0": v0, "1": v1, "2": v2, "3": v3})
    return merged, leg

TEXTURES = {}

# --- REACTOR MULTIBLOCK FAMILY ---
TEXTURES[("block", "reactor_casing")] = make_industrial_casing(R_PAL)

TEXTURES[("block", "reactor_glass")] = (
    [
        "khhhhhhhhhhhhhhd",
        "hnn..........nmd",
        "hn.GG.........md",
        "hm.G..........md",
        "hm............md",
        "hm............md",
        "hm............md",
        "hm............md",
        "hm............md",
        "hm............md",
        "hm............md",
        "hm............md",
        "hm..........G.md",
        "hm.........GGnmd",
        "dmmmmmmmmmmmmmmd",
        "kddddddddddddddk",
    ],
    {**R_PAL, "G": "#d8f0ffbb"},
)

TEXTURES[("block", "reactor_controller_off")] = make_framed(
    R_PAL,
    [
        "KDDDDDDDDK",
        "DPPPPPPPPD",
        "DPQQ11QQPD",
        "DPQQQQQQPD",
        "DPQ2222QPD",
        "DPQQQQQQPD",
        "DPQ33Q4QPD",
        "DPPPPPPPPD",
        "KMMMMMMMMK",
        "KKKKKKKKKK",
    ],
    {
        "K": "#252a33", "D": "#383f4a", "M": "#4c5463",
        "P": "#181d24", "Q": "#202730",
        "1": "#4f2525", "2": "#354557", "3": "#3d4654", "4": "#8c2b2b",
    },
)

TEXTURES[("block", "reactor_controller_on")] = make_framed(
    R_PAL,
    [
        "KDDDDDDDDK",
        "DPPPPPPPPD",
        "DPQ151QQPD",
        "DPQQQQQQPD",
        "DPQ2622QPD",
        "DPQQQQQQPD",
        "DPQ33Q4QPD",
        "DPPPPPPPPD",
        "KMMMMMMMMK",
        "KKKKKKKKKK",
    ],
    {
        "K": "#252a33", "D": "#383f4a", "M": "#4c5463",
        "P": "#162028", "Q": "#1c2c36",
        "1": "#d9b938", "5": "#fff49c", "2": "#34c77b", "6": "#82f5b6", "3": "#489ed4", "4": "#42e88a",
    },
)

TEXTURES[("block", "reactor_fuel_rod")] = (
    [
        "klllllllllllllld",
        "kddddddddddddddk",
        "..kmgg12321ggdk.",
        "..kmgg12432ggdk.",
        "..kmgg12321ggdk.",
        "..kmgg01110ggdk.",
        "..kmgg12321ggdk.",
        "..kmgg12432ggdk.",
        "..kmgg12321ggdk.",
        "..kmgg01110ggdk.",
        "..kmgg12321ggdk.",
        "..kmgg12432ggdk.",
        "..kmgg12321ggdk.",
        "..kmgg01110ggdk.",
        "lhhhhhhhhhhhhhhd",
        "kddddddddddddddk",
    ],
    {
        **R_PAL,
        "g": "#2b3e47",
        "0": "#4b6b22",
        "1": "#76a336",
        "2": "#a6d455",
        "3": "#d1f086",
        "4": "#eefac7",
    },
)

TEXTURES[("block", "reactor_fuel_rod_end")] = (
    [
        "................",
        "................",
        "..kllllllllllk..",
        "..lhhhhhhhhhhd..",
        "..lhmdd11ddmhd..",
        "..lhmd12321dmd..",
        "..lhmd23432dmd..",
        "..lhmd23432dmd..",
        "..lhmd12321dmd..",
        "..lhmdd11ddmhd..",
        "..lhmddddddmhd..",
        "..lhhhhhhhhhhd..",
        "..dmmmmmmmmmmd..",
        "..kddddddddddk..",
        "................",
        "................",
    ],
    {**R_PAL, "1": "#60882b", "2": "#92bf44", "3": "#c4e872", "4": "#ebfab8"},
)

TEXTURES[("block", "reactor_control_rod")] = make_framed(
    R_PAL,
    [
        "MMMMMMMMMM",
        "MKKDDDDKKM",
        "MKDRLLLRDK",
        "MDRMMMMRDM",
        "MDMKKKKMDM",
        "MDMKSSKMDM",
        "MDMKSSKMDM",
        "MDMKKKKMDM",
        "MKRMMMMRKM",
        "KKDDDDDDKK",
    ],
    {"K": "#262b33", "D": "#3c434f", "M": "#555f6e", "R": "#7c8799", "L": "#a2aec2", "S": "#1d2129"},
)

def make_port(pal, c_hi, c_mid, c_sh, bore_dark, bore_mid, bore_hi):
    """Shaded industrial port collar around a dark recessed mechanical bore (Mekanism port style)."""
    return make_framed(
        pal,
        [
            "KDDDDDDDDK",
            "DLLLLLLMMD",
            "DL111111MD",
            "DL1KAAK1MD",
            "DL1ABBA1MD",
            "DL1ABCA1MD",
            "DL1KAAK1MD",
            "DL222222MD",
            "DMMMMMMMMD",
            "KKKKKKKKKK",
        ],
        {
            "K": "#252a33", "D": "#3c4450", "M": "#5c6675", "L": "#8a96a8",
            "1": c_hi, "2": c_sh, "A": bore_dark, "B": bore_mid, "C": bore_hi,
        },
    )

# Wait: make_port uses '1' (top) and '2' (bottom); let's also add side collar pixels cleanly
def make_collar_port(pal, c_hi, c_mid, c_sh, b0, b1, b2):
    return make_framed(
        pal,
        [
            "KDDDDDDDDK",
            "DL111111MD",
            "D11KAAK2MD",
            "D1KABBAK2M",
            "D1KABCBAK2",
            "D1KABBAK2M",
            "D1KAAKK2MD",
            "D2222222MD",
            "DMMMMMMMMD",
            "KKKKKKKKKK",
        ],
        {
            "K": "#242932", "D": "#3a424e", "M": "#5b6575", "L": "#8793a6",
            "1": c_hi, "2": c_sh, "A": b0, "B": b1, "C": b2,
        },
    )

def make_sym_collar_port(pal, c_hi, c_mid, c_sh, b0, b1, b2):
    return make_framed(
        pal,
        [
            "KDDDDDDDDK",
            "DLL1111MMD",
            "DL11KK11MD",
            "D1KAABAK2M",
            "D1KABCAK2M",
            "D1KABBAK2M",
            "D1KAAAAK2M",
            "DM22KK22MD",
            "DMM2222MMD",
            "KKKKKKKKKK",
        ],
        {
            "K": "#242932", "D": "#3a424e", "M": "#596373", "L": "#8793a6",
            "1": c_hi, "2": c_sh, "A": b0, "B": b1, "C": b2,
        },
    )

TEXTURES[("block", "reactor_power_tap")] = make_framed(
    R_PAL,
    [
        "KDDDDDDDDK",
        "DL111111MD",
        "D1KCCCCK2M",
        "D1KCEECK2M",
        "D1KEFFEK2M",
        "D1KEFFEK2M",
        "D1KCEECK2M",
        "D2KCCCCK2M",
        "DM222222MD",
        "KKKKKKKKKK",
    ],
    {
        "K": "#242932", "D": "#3a424e", "M": "#596373", "L": "#8793a6",
        "1": "#d48d4e", "2": "#7d4825", "C": "#24303a", "E": "#2a8c5c", "F": "#64d99b",
    },
)

TEXTURES[("block", "reactor_access_port_inlet")] = make_sym_collar_port(
    R_PAL, "#e3b352", "#ba852e", "#7a5319", "#1e222b", "#3a4352", "#637187"
)
TEXTURES[("block", "reactor_access_port_outlet")] = make_sym_collar_port(
    R_PAL, "#64c4eb", "#3490b8", "#1f5e7a", "#1e222b", "#3a4352", "#637187"
)
TEXTURES[("block", "reactor_coolant_port_inlet")] = make_sym_collar_port(
    R_PAL, "#5cb4f2", "#2d7ec2", "#1c5080", "#16293d", "#24578a", "#52a3e8"
)
TEXTURES[("block", "reactor_coolant_port_outlet")] = make_sym_collar_port(
    R_PAL, "#d2e5f5", "#94b0c7", "#5d7385", "#293542", "#597185", "#c2daed"
)

TEXTURES[("block", "reactor_redstone_port_off")] = make_framed(
    R_PAL,
    [
        "KDDDDDDDDK",
        "DLLLLLLMMD",
        "DLKKKKKKMD",
        "DLK1111KMD",
        "DLK1221KMD",
        "DLK1221KMD",
        "DLK1111KMD",
        "DLKKKKKKMD",
        "DMMMMMMMMD",
        "KKKKKKKKKK",
    ],
    {"K": "#242932", "D": "#3a424e", "M": "#596373", "L": "#8793a6", "1": "#45191d", "2": "#73272f"},
)

TEXTURES[("block", "reactor_redstone_port_on")] = make_framed(
    R_PAL,
    [
        "KDDDDDDDDK",
        "DL111111MD",
        "D1KKKKKK1M",
        "D1K2222K1M",
        "D1K2342K1M",
        "D1K2432K1M",
        "D1K2222K1M",
        "D1KKKKKK1M",
        "DM111111MD",
        "KKKKKKKKKK",
    ],
    {"K": "#242932", "D": "#3a424e", "M": "#596373", "L": "#8793a6", "1": "#7d2020", "2": "#bf2e2e", "3": "#f05b5b", "4": "#ffb5b5"},
)

# --- TURBINE MULTIBLOCK FAMILY ---
TEXTURES[("block", "turbine_casing")] = make_industrial_casing(T_PAL)

TEXTURES[("block", "turbine_glass")] = (
    [
        "khhhhhhhhhhhhhhd",
        "hnn..........nmd",
        "hn.GG.........md",
        "hm.G..........md",
        "hm............md",
        "hm............md",
        "hm............md",
        "hm............md",
        "hm............md",
        "hm............md",
        "hm............md",
        "hm............md",
        "hm..........G.md",
        "hm.........GGnmd",
        "dmmmmmmmmmmmmmmd",
        "kdddddddddddddak".replace("a", "d"),
    ],
    {**T_PAL, "G": "#dcf2ffbb"},
)

TEXTURES[("block", "turbine_controller_off")] = make_framed(
    T_PAL,
    [
        "KDDDDDDDDK",
        "DPPPPPPPPD",
        "DPQQ11QQPD",
        "DPQ1QQ1QPD",
        "DPQ1Q21QPD",
        "DPQQQQQQPD",
        "DPQ3333QPD",
        "DPPPPPPPPD",
        "KMMMMMMMMK",
        "KKKKKKKKKK",
    ],
    {
        "K": "#2d3745", "D": "#465466", "M": "#64768c",
        "P": "#18212b", "Q": "#212e3d",
        "1": "#405873", "2": "#8c3535", "3": "#354961",
    },
)

TEXTURES[("block", "turbine_controller_on")] = make_framed(
    T_PAL,
    [
        "KDDDDDDDDK",
        "DPPPPPPPPD",
        "DPQQ15QQPD",
        "DPQ1QQ5QPD",
        "DPQ1Q25QPD",
        "DPQQQQQQPD",
        "DPQ3364QPD",
        "DPPPPPPPPD",
        "KMMMMMMMMK",
        "KKKKKKKKKK",
    ],
    {
        "K": "#2d3745", "D": "#465466", "M": "#64768c",
        "P": "#182636", "Q": "#1e3347",
        "1": "#47b8e8", "5": "#96ebff", "2": "#e8f7ff", "3": "#34c77b", "6": "#e3c542", "4": "#42e88a",
    },
)

TEXTURES[("block", "turbine_rotor_bearing")] = make_framed(
    T_PAL,
    [
        "KDDDDDDDDK",
        "DLLGGGGMMD",
        "DLGYYYYGMD",
        "DGYKAAKYGM",
        "DGYABBCYGM",
        "DGYABBCYGM",
        "DGYKAAKYGM",
        "DMGYYYYGMD",
        "DMMGGGGMMD",
        "KKKKKKKKKK",
    ],
    {
        "K": "#2d3745", "D": "#465466", "M": "#64768c", "L": "#96a8bd",
        "G": "#8f6928", "Y": "#cf9f44", "A": "#2a3340", "B": "#59687a", "C": "#99abc2",
    },
)

TEXTURES[("block", "turbine_rotor_shaft")] = (
    [
        "....khlmmddk....",
        "....khlmmddk....",
        "....kllhhmdk....",
        "....kddmmddk....",
        "....khlmmddk....",
        "....khlmmddk....",
        "....khlmmddk....",
        "....kddmmddk....",
        "....khlmmddk....",
        "....khlmmddk....",
        "....khlmmddk....",
        "....kddmmddk....",
        "....khlmmddk....",
        "....kllhhmdk....",
        "....khlmmddk....",
        "....kddddddk....",
    ],
    {"k": "#2b3542", "d": "#515f73", "m": "#74869e", "h": "#9eb0c7", "l": "#cad8e8"},
)

TEXTURES[("block", "turbine_rotor_blade")] = (
    [
        "klllllllllllllld",
        "lhhhhhhhhhhhhhhd",
        "lhmmmmmmmmmmmmmd",
        "lhmbbbbbbbbbbmmd",
        "lhmBbbbbbbbbBmmd",
        "lhmmmmmmmmmmmmmd",
        "lhmddddddddddmmd",
        "lhmmmmmmmmmmmmmd",
        "lhmmmmmmmmmmmmmd",
        "lhmddddddddddmmd",
        "lhmBbbbbbbbbBmmd",
        "lhmbbbbbbbbbbmmd",
        "lhmmmmmmmmmmmmmd",
        "lhmmmmmmmmmmmmmd",
        "dhhhhhhhhhhhhhhd",
        "kddddddddddddddk",
    ],
    {"k": "#303a47", "d": "#526173", "m": "#76879c", "h": "#9fb1c7", "l": "#cad8e8", "b": "#2d6d8f", "B": "#4fa6cf"},
)

TEXTURES[("block", "turbine_fluid_port_inlet")] = make_sym_collar_port(
    T_PAL, "#d2e5f5", "#94b0c7", "#5d7385", "#293542", "#597185", "#c2daed"
)
TEXTURES[("block", "turbine_fluid_port_outlet")] = make_sym_collar_port(
    T_PAL, "#5cb4f2", "#2d7ec2", "#1c5080", "#16293d", "#24578a", "#52a3e8"
)
TEXTURES[("block", "turbine_power_tap")] = make_framed(
    T_PAL,
    [
        "KDDDDDDDDK",
        "DL111111MD",
        "D1KCCCCK2M",
        "D1KCEECK2M",
        "D1KEFFEK2M",
        "D1KEFFEK2M",
        "D1KCEECK2M",
        "D2KCCCCK2M",
        "DM222222MD",
        "KKKKKKKKKK",
    ],
    {
        "K": "#2d3745", "D": "#465466", "M": "#64768c", "L": "#96a8bd",
        "1": "#d48d4e", "2": "#7d4825", "C": "#24303a", "E": "#2a8c5c", "F": "#64d99b",
    },
)

TEXTURES[("block", "turbine_redstone_port_off")] = make_framed(
    T_PAL,
    [
        "KDDDDDDDDK",
        "DLLLLLLMMD",
        "DLKKKKKKMD",
        "DLK1111KMD",
        "DLK1221KMD",
        "DLK1221KMD",
        "DLK1111KMD",
        "DLKKKKKKMD",
        "DMMMMMMMMD",
        "KKKKKKKKKK",
    ],
    {"K": "#2d3745", "D": "#465466", "M": "#64768c", "L": "#96a8bd", "1": "#45191d", "2": "#73272f"},
)

TEXTURES[("block", "turbine_redstone_port_on")] = make_framed(
    T_PAL,
    [
        "KDDDDDDDDK",
        "DL111111MD",
        "D1KKKKKK1M",
        "D1K2222K1M",
        "D1K2342K1M",
        "D1K2432K1M",
        "D1K2222K1M",
        "D1KKKKKK1M",
        "DM111111MD",
        "KKKKKKKKKK",
    ],
    {"K": "#2d3745", "D": "#465466", "M": "#64768c", "L": "#96a8bd", "1": "#7d2020", "2": "#bf2e2e", "3": "#f05b5b", "4": "#ffb5b5"},
)

# --- STANDALONE REPROCESSOR ---
TEXTURES[("block", "cyanite_reprocessor_off")] = make_framed(
    R_PAL,
    [
        "KDDDDDDDDK",
        "DLLKPPKMDD",
        "DLBKPPKBMD",
        "DLBK11KBMD",
        "DLBK12KBMD",
        "DLBK11KBMD",
        "DLBKPPKBMD",
        "DMMMMMMMMD",
        "KMD33MDMMK",
        "KKKKKKKKKK",
    ],
    {
        "K": "#242932", "D": "#3a424e", "M": "#596373", "L": "#8793a6",
        "B": "#385a73", "P": "#1f192b", "1": "#442870", "2": "#653ca3", "3": "#9c3333",
    },
)

TEXTURES[("block", "cyanite_reprocessor_on")] = make_framed(
    R_PAL,
    [
        "KDDDDDDDDK",
        "DLLKPPKMDD",
        "DLBKPPKBMD",
        "DLBK11KBMD",
        "DLBK24KBMD",
        "DLBK11KBMD",
        "DLBKPPKBMD",
        "DMMMMMMMMD",
        "KMD33MDMMK",
        "KKKKKKKKKK",
    ],
    {
        "K": "#242932", "D": "#3a424e", "M": "#596373", "L": "#8793a6",
        "B": "#3a89b8", "P": "#261a3d", "1": "#6b38b8", "2": "#a363e8", "4": "#e3c7ff", "3": "#3cd480",
    },
)

# --- ORES & STORAGE BLOCKS ---
TEXTURES[("block", "uranium_ore")] = make_ore("stone", "#385c1e", "#5b912d", "#84c241", "#b5db6c")
TEXTURES[("block", "deepslate_uranium_ore")] = make_ore("deepslate", "#385c1e", "#5b912d", "#84c241", "#b5db6c")

TEXTURES[("block", "uranium_block")] = make_plated_storage_block("#344d1d", "#4f732c", "#74a142", "#9ec965", "#c8eb98")
TEXTURES[("block", "graphite_block")] = make_brushed_storage_block("#1c2026", "#292e38", "#3a414f", "#4d5668", "#657187")
TEXTURES[("block", "cyanite_block")] = make_brushed_storage_block("#1b4157", "#2b6787", "#4290b8", "#69badb", "#9fe0f7")
TEXTURES[("block", "blutonium_block")] = make_brushed_storage_block("#231742", "#3a276b", "#5a3e9e", "#825fd1", "#b896f5")

# --- ITEMS (Vanilla / Mekanism Silhouettes) ---
TEXTURES[("item", "uranium_ingot")] = make_ingot("#2b3d16", "#466323", "#6e9934", "#99c94f", "#c5eb7f", "#e8fab9")
TEXTURES[("item", "graphite_ingot")] = make_ingot("#181b20", "#292e38", "#414854", "#5c6575", "#7a8599", "#a4b0c4")
TEXTURES[("item", "cyanite_ingot")] = make_ingot("#153445", "#235670", "#377ea1", "#59a8cc", "#8ad0eb", "#c7f0ff")
TEXTURES[("item", "blutonium_ingot")] = make_ingot("#1f153b", "#372566", "#583c9c", "#7e5ccf", "#af8cf2", "#dfd0ff")

# Original craggy crystalline uranium ore clump (compact rocky cluster with sharp facets)
TEXTURES[("item", "raw_uranium")] = (
    [
        "................",
        "................",
        "......1111......",
        "....11454311....",
        "...1455433221...",
        "..145433222110..",
        "..134322245420..",
        "..123321355430..",
        "...12211343220..",
        "....110123220...",
        ".....1001210....",
        ".........00.....",
        "................",
        "................",
        "................",
        "................",
    ],
    {"0": "#223012", "1": "#364d1c", "2": "#587a2a", "3": "#82b03e", "4": "#b5e064", "5": "#e4f79c"},
)

# Canonical centered Vanilla bucket silhouette (x=2..13, y=1..14) filled with pressurized steam
TEXTURES[("item", "steam_bucket")] = (
    [
        "......w...s.....",
        ".....111111.....",
        "...1133ssww11...",
        "..1322swwcssc1..",
        "..1233wwccssc0..",
        "..11122sscc110..",
        "..154111111320..",
        "..155554433320..",
        "..155554433330..",
        "..145554433320..",
        "..135554433320..",
        "...1555443320...",
        "...1455433320...",
        "....14543320....",
        ".....100000.....",
        "................",
    ],
    {
        "0": "#222730",
        "1": "#3b424f",
        "2": "#6b7482",
        "3": "#9da6b5",
        "4": "#cfd7e3",
        "5": "#edf2f7",
        "c": "#8ea3b3",
        "s": "#c5d8e6",
        "w": "#f2f8fc",
    },
)

# --- 32x32 MOD LOGO (refined titanium-gunmetal medallion matching new palette) ---
LOGO_ROWS = [
    "................................",
    "...........kkkkkkkkkk...........",
    "........kkkllllllllllkkk........",
    "......kkllhhhhhhhhhhhhllkk......",
    ".....klhhmmmmmmmmmmmmmmhhlk.....",
    "....klhmddddddddddddddddmhlk....",
    "...klhmddyyyyyyyyyyyyyyddmhlk...",
    "..klhmdyYYYYYYYYYYYYYYYYydmhlk..",
    "..klhmdyYYkkkkkkkkkkkkYYydmhlk..",
    ".klhmdyYYkkmmmmmmmmmkkkYYydmhlk.",
    ".klhmdyYYkm1111111111mkYYydmhlk.",
    "klhmdyYYkm122222222221mkYYydmhlk",
    "klhmdyYYkm123333333321mkYYydmhlk",
    "klhmdyYYkm123444444321mkYYydmhlk",
    "klhmdyYYkm123455554321mkYYydmhlk",
    "klhmdyYYkm123455554321mkYYydmhlk",
    "klhmdyYYkm123455554321mkYYydmhlk",
    "klhmdyYYkm123455554321mkYYydmhlk",
    "klhmdyYYkm123444444321mkYYydmhlk",
    "klhmdyYYkm123333333321mkYYydmhlk",
    "klhmdyYYkm122222222221mkYYydmhlk",
    ".klhmdyYYkm1111111111mkYYydmhlk.",
    ".klhmdyYYkkkkkkkkkkkkkkYYydmhlk.",
    "..klhmdyYYYYYYYYYYYYYYYYydmhlk..",
    "..klhmdyYYYYYYYYYYYYYYYYydmhlk..",
    "...klhmddyyyyyyyyyyyyyyddmhlk...",
    "....klhmddddddddddddddddmhlk....",
    ".....klhhmmmmmmmmmmmmmmhhlk.....",
    "......kkllhhhhhhhhhhhhllkk......",
    "........kkkddddddddddkkk........",
    "...........kkkkkkkkkk...........",
    "................................",
]
LOGO_LEGEND = {
    **R_PAL,
    "y": "#8a6d24",
    "Y": "#c99f38",
    "1": "#4b6b22",
    "2": "#76a336",
    "3": "#a6d455",
    "4": "#d1f086",
    "5": "#eefac7",
}

def pontilha_run(cmd_id, params):
    res = subprocess.run(["pontilha", "run", cmd_id, json.dumps(params)], capture_output=True, text=True)
    if res.returncode != 0:
        raise RuntimeError(f"pontilha run {cmd_id} failed: {res.stderr or res.stdout}")

def pontilha_cmd(args):
    res = subprocess.run(["pontilha", *args], capture_output=True, text=True)
    if res.returncode != 0:
        raise RuntimeError(f"pontilha {' '.join(args)} failed: {res.stderr or res.stdout}")

def craft_in_pontilha(name, rows, legend, export_path, size=16):
    assert len(rows) == size, f"{name}: expected {size} rows, got {len(rows)}"
    for i, r in enumerate(rows):
        assert len(r) == size, f"{name} row {i}: expected {size} cols, got {len(r)}"
    pontilha_run("doc.new", {"width": size, "height": size, "name": name, "discard": True})
    pontilha_run("draw.pattern", {"x": 0, "y": 0, "rows": rows, "legend": legend})
    pontilha_cmd(["save", str(ART_DIR / f"{name}.pontilha")])
    pontilha_cmd(["export", str(export_path), "1"])

def build_contact_sheet_v3(entries, out_path, scale=6, cols=8):
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
        s = max(1, min(cell // max(im.width, 1), cell // max(im.height, 1)))
        im_s = im.resize((im.width * s, im.height * s), Image.NEAREST)
        img.alpha_composite(im_s, (x0 + 8, y0 + 4))
        draw.text((x0 + 6, y0 + cell + 8), label[:16], fill=(220, 226, 236, 255))
    img.save(out_path)
    print(f"Saved {out_path}")

def build_comparison_v3(out_path):
    """Side-by-side comparison vs AE2 / Mekanism / Vanilla refs + 3x3 Reactor & Turbine walls."""
    scale = 6
    cell = 16 * scale
    cell_w, cell_h = 118, 128
    pairs = [
        ("jr:uranium_ing", ITEM_DIR / "uranium_ingot.png"),
        ("mek:ingot_uran", MEK_TEX / "item" / "ingot_uranium.png"),
        ("jr:graphite_ing", ITEM_DIR / "graphite_ingot.png"),
        ("mek:ingot_steel", MEK_TEX / "item" / "ingot_steel.png"),
        ("jr:raw_uranium", ITEM_DIR / "raw_uranium.png"),
        ("mek:raw_uranium", MEK_TEX / "item" / "raw_uranium.png"),
        ("jr:steam_bucket", ITEM_DIR / "steam_bucket.png"),
        ("mc:water_bucket", VANILLA_TEX / "water_bucket.png"),
        ("jr:uranium_ore", BLOCK_DIR / "uranium_ore.png"),
        ("mek:uranium_ore", MEK_TEX / "block" / "uranium_ore.png"),
        ("jr:ds_uran_ore", BLOCK_DIR / "deepslate_uranium_ore.png"),
        ("mek:ds_uran_ore", MEK_TEX / "block" / "deepslate_uranium_ore.png"),
        ("jr:uranium_blk", BLOCK_DIR / "uranium_block.png"),
        ("mek:blk_uranium", MEK_TEX / "block" / "block_uranium.png"),
        ("jr:cyanite_blk", BLOCK_DIR / "cyanite_block.png"),
        ("mek:blk_osmium", MEK_TEX / "block" / "block_osmium.png"),
        ("jr:r_casing", BLOCK_DIR / "reactor_casing.png"),
        ("mekg:fission_cs", MEKG_TEX / "block" / "fission_reactor_casing.png"),
        ("jr:t_casing", BLOCK_DIR / "turbine_casing.png"),
        ("mekg:turb_case", MEKG_TEX / "block" / "turbine_casing.png"),
        ("vd:voltaic_cbl", VOLTAIC_BLOCK / "voltaic_cable.png"),
        ("ae2:cov_cyan", AE2_TEX / "part" / "cable" / "covered" / "cyan.png"),
        ("vd:item_cable", VOLTAIC_BLOCK / "item_cable.png"),
        ("ae2:cov_gray", AE2_TEX / "part" / "cable" / "covered" / "gray.png"),
    ]
    cols = 8
    rows_n = (len(pairs) + cols - 1) // cols
    W = cols * cell_w + 24
    H = rows_n * cell_h + 24 + 260
    img = Image.new("RGBA", (W, H), (22, 25, 31, 255))
    draw = ImageDraw.Draw(img)

    for idx, (label, path) in enumerate(pairs):
        r, c = divmod(idx, cols)
        x0, y0 = 12 + c * cell_w, 12 + r * cell_h
        for cy in range(0, cell, 8):
            for cx in range(0, cell, 8):
                col = (44, 48, 56, 255) if ((cx // 8 + cy // 8) % 2 == 0) else (36, 39, 46, 255)
                draw.rectangle([x0 + 10 + cx, y0 + 6 + cy, x0 + 10 + cx + 7, y0 + 6 + cy + 7], fill=col)
        if path.exists():
            im = Image.open(path).convert("RGBA")
            if im.height > im.width and im.width in (16, 32):
                im = im.crop((0, 0, im.width, im.width))
            im_s = im.resize((cell, cell), Image.NEAREST)
            img.alpha_composite(im_s, (x0 + 10, y0 + 6))
        draw.text((x0 + 8, y0 + cell + 10), label[:16], fill=(225, 232, 242, 255))

    # Bottom row: 3x3 Reactor wall mockup + 3x3 Turbine wall mockup + 3-seg Voltaic cable mockup
    y_base = rows_n * cell_h + 28
    tile = 16 * 4  # 64px

    # Grass/sky backdrop behind mockups
    draw.rectangle([20, y_base, 20 + 3 * tile + 20 + 3 * tile + 20 + 240, y_base + 220], fill=(102, 142, 188, 255))
    draw.rectangle([20, y_base + 3 * tile, 20 + 3 * tile + 20 + 3 * tile + 20 + 240, y_base + 220], fill=(76, 120, 62, 255))

    r_wall = [
        ["reactor_casing.png", "reactor_glass.png", "reactor_casing.png"],
        ["reactor_access_port_inlet.png", "reactor_controller_on.png", "reactor_power_tap.png"],
        ["reactor_casing.png", "reactor_coolant_port_outlet.png", "reactor_casing.png"],
    ]
    for rr in range(3):
        for cc in range(3):
            im = Image.open(BLOCK_DIR / r_wall[rr][cc]).convert("RGBA").resize((tile, tile), Image.NEAREST)
            img.alpha_composite(im, (30 + cc * tile, y_base + rr * tile))
    draw.text((30, y_base + 196), "Reactor 3x3 Wall", fill=(240, 245, 250, 255))

    t_wall = [
        ["turbine_casing.png", "turbine_glass.png", "turbine_casing.png"],
        ["turbine_fluid_port_inlet.png", "turbine_controller_on.png", "turbine_power_tap.png"],
        ["turbine_casing.png", "turbine_rotor_bearing.png", "turbine_casing.png"],
    ]
    tx0 = 30 + 3 * tile + 24
    for rr in range(3):
        for cc in range(3):
            im = Image.open(BLOCK_DIR / t_wall[rr][cc]).convert("RGBA").resize((tile, tile), Image.NEAREST)
            img.alpha_composite(im, (tx0 + cc * tile, y_base + rr * tile))
    draw.text((tx0, y_base + 196), "Turbine 3x3 Wall", fill=(240, 245, 250, 255))

    cx0 = tx0 + 3 * tile + 28
    for k, cname in enumerate(["voltaic_cable.png", "item_cable.png", "fluid_cable.png"]):
        im = Image.open(VOLTAIC_BLOCK / cname).convert("RGBA")
        strip = im.crop((0, 5, 16, 11)).resize((16 * 4, 6 * 4), Image.NEAREST)
        for seg in range(3):
            img.alpha_composite(strip, (cx0 + seg * 64, y_base + 24 + k * 56))
    draw.text((cx0, y_base + 196), "Voltaic 3-Seg Cables", fill=(240, 245, 250, 255))

    img.save(out_path)
    print(f"Saved {out_path}")

if __name__ == "__main__":
    exported = []
    for (kind, name), (rows, legend) in TEXTURES.items():
        out_dir = BLOCK_DIR if kind == "block" else ITEM_DIR
        out_path = out_dir / f"{name}.png"
        craft_in_pontilha(name, rows, legend, out_path, size=16)
        exported.append((name, out_path))

    craft_in_pontilha("justreactors_logo", LOGO_ROWS, LOGO_LEGEND, LOGO_PATH, size=32)

    for cname in [
        "voltaic_cable.png",
        "voltaic_cable_glow.png",
        "item_cable.png",
        "fluid_cable.png",
        "cable_connector.png",
        "cable_connector_pull.png",
    ]:
        exported.append((f"vd:{cname[:-4]}", VOLTAIC_BLOCK / cname))

    build_contact_sheet_v3(exported, ART_DIR / "_contact_sheet_v3.png", scale=6, cols=8)
    build_comparison_v3(ART_DIR / "_comparison_v3.png")
    print("Done crafting all Just Reactors textures and verification sheets!")
