"""
Textures for the Heat Exchanger and Battery multiblocks and the coolant buckets (v2 additions).
Reuses the casing/frame helpers and layouts of build_textures.py and writes PNGs directly with PIL.
Run: python art-src/textures/build_textures_v2.py
"""
import sys
from pathlib import Path

from PIL import Image

sys.path.insert(0, str(Path(__file__).parent))
import build_textures as bt  # noqa: E402  (guarded by __main__, importing only defines the textures)

BLOCK_DIR = bt.BLOCK_DIR
ITEM_DIR = bt.ITEM_DIR

# Heat Exchanger casing: warm copper-bronze plate
X_PAL = {
    "k": "#3a2e28",
    "d": "#55423a",
    "s": "#4a3a33",
    "r": "#6a5246",
    "m": "#7a5d4f",
    "n": "#88695a",
    "h": "#a3806c",
    "l": "#c29a82",
}

# Battery casing: dark slate with a green tint
B_PAL = {
    "k": "#2a3330",
    "d": "#3a4743",
    "s": "#33403c",
    "r": "#44544f",
    "m": "#4f605a",
    "n": "#596b64",
    "h": "#71857d",
    "l": "#8ea399",
}

FRAME_X = {"K": "#2e2420", "D": "#4a3a33", "M": "#6e5446", "L": "#a3806c"}
FRAME_B = {"K": "#222a27", "D": "#37433f", "M": "#566862", "L": "#8ea399"}

NEW = {}


def glass(pal, tint):
    rows, _ = bt.TEXTURES[("block", "turbine_glass")]
    return rows, {**pal, "G": tint}


def controller(pal, frame, on, accent_hi, accent_lo):
    # same LCD layout as the turbine controller, with the machine's own accent colours
    if on:
        rows = ["KDDDDDDDDK", "DPPPPPPPPD", "DPQQ15QQPD", "DPQ1QQ5QPD", "DPQ1Q25QPD",
                "DPQQQQQQPD", "DPQ3364QPD", "DPPPPPPPPD", "KMMMMMMMMK", "KKKKKKKKKK"]
        legend = {**frame, "P": "#141c22", "Q": "#1b2a30",
                  "1": accent_lo, "5": accent_hi, "2": "#f4fbff", "3": "#34c77b", "6": "#e3c542", "4": "#42e88a"}
    else:
        rows = ["KDDDDDDDDK", "DPPPPPPPPD", "DPQQ11QQPD", "DPQ1QQ1QPD", "DPQ1Q21QPD",
                "DPQQQQQQPD", "DPQ3333QPD", "DPPPPPPPPD", "KMMMMMMMMK", "KKKKKKKKKK"]
        legend = {**frame, "P": "#161a1f", "Q": "#1f252c", "1": "#3c444e", "2": "#8c3535", "3": "#333b45"}
    return bt.make_framed(pal, rows, legend)


def port(pal, frame, c_hi, c_sh, b0, b1, b2):
    return bt.make_framed(pal, [
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
    ], {**frame, "1": c_hi, "2": c_sh, "A": b0, "B": b1, "C": b2})


# --- HEAT EXCHANGER ---
NEW[("block", "heat_exchanger_casing")] = bt.make_industrial_casing(X_PAL)
NEW[("block", "heat_exchanger_glass")] = glass(X_PAL, "#ffe6d2bb")
NEW[("block", "heat_exchanger_controller_off")] = controller(X_PAL, FRAME_X, False, "#ffc08a", "#e07a35")
NEW[("block", "heat_exchanger_controller_on")] = controller(X_PAL, FRAME_X, True, "#ffc08a", "#e07a35")
NEW[("block", "heat_exchanger_fluid_port_input")] = port(X_PAL, FRAME_X, "#f0a050", "#8a4a1c", "#2a1a12", "#7a3a16", "#f07a30")
NEW[("block", "heat_exchanger_fluid_port_output_coolant")] = port(X_PAL, FRAME_X, "#6fe0ea", "#1f6a72", "#10262a", "#1f6a72", "#5fd6e0")
NEW[("block", "heat_exchanger_fluid_port_output_steam")] = port(X_PAL, FRAME_X, "#e2ecf4", "#6a7a88", "#28323c", "#6a7a88", "#d8e6f0")

# Bundle of copper pipes: hot on the left, cooled on the right
NEW[("block", "heat_exchanger_tube")] = (
    [
        "kddddddddddddddk",
        "d1a2d1a2d3b4d3bd",
        "d1a2d1a2d3b4d3bd",
        "dhhhdhhhdhhhdhhd",
        "d1a2d1a2d3b4d3bd",
        "d1a2d1a2d3b4d3bd",
        "dhhhdhhhdhhhdhhd",
        "kddddddddddddddk",
        "d1a2d1a2d3b4d3bd",
        "d1a2d1a2d3b4d3bd",
        "dhhhdhhhdhhhdhhd",
        "d1a2d1a2d3b4d3bd",
        "d1a2d1a2d3b4d3bd",
        "dhhhdhhhdhhhdhhd",
        "d1a2d1a2d3b4d3bd",
        "kddddddddddddddk",
    ],
    {**X_PAL, "1": "#e7a06a", "a": "#c46a32", "2": "#8a4520",
     "3": "#8fe4ea", "b": "#3aa9b4", "4": "#1f6a72", "h": "#a3806c"},
)

# --- BATTERY ---
NEW[("block", "battery_casing")] = bt.make_industrial_casing(B_PAL)
NEW[("block", "battery_glass")] = glass(B_PAL, "#d8ffe6bb")
NEW[("block", "battery_controller_off")] = controller(B_PAL, FRAME_B, False, "#b6f5a8", "#5fbf4f")
NEW[("block", "battery_controller_on")] = controller(B_PAL, FRAME_B, True, "#b6f5a8", "#5fbf4f")
NEW[("block", "battery_power_port_input")] = bt.make_framed(B_PAL, [
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
], {**FRAME_B, "1": "#7ad36b", "2": "#2f6a28", "C": "#1c2622", "E": "#2a8c5c", "F": "#64d99b"})
NEW[("block", "battery_power_port_output")] = bt.make_framed(B_PAL, [
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
], {**FRAME_B, "1": "#e39b4a", "2": "#7d4825", "C": "#1c2622", "E": "#b0722c", "F": "#f2c46a"})
NEW[("block", "battery_redstone_sensor_off")] = bt.make_framed(B_PAL, [
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
], {**FRAME_B, "1": "#45191d", "2": "#73272f"})
NEW[("block", "battery_redstone_sensor_on")] = bt.make_framed(B_PAL, [
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
], {**FRAME_B, "1": "#7d2020", "2": "#bf2e2e", "3": "#f05b5b", "4": "#ffb5b5"})


def cell(level):
    """Storage cell: 4 charge bars that light up from the bottom."""
    lit = {"on1": "#3f9a3a", "on2": "#7ad36b", "on3": "#c4f5b0"}
    rows = [
        "kllllllllllllllk",
        "lhhhhhhhhhhhhhhd",
        "lhkkkkkkkkkkkkhd",
        "lhk..........khd",
        "lhk.44444444.khd",
        "lhk..........khd",
        "lhk.33333333.khd",
        "lhk..........khd",
        "lhk.22222222.khd",
        "lhk..........khd",
        "lhk.11111111.khd",
        "lhk..........khd",
        "lhkkkkkkkkkkkkhd",
        "lhhhhhhhhhhhhhhd",
        "dddddddddddddddd",
        "kddddddddddddddk",
    ]
    legend = {**B_PAL, ".": "#141a18"}
    for bar in range(1, 5):
        on = bar <= level
        legend[str(bar)] = lit["on2"] if on else "#26322d"
    rows = [r.replace(".", "x") for r in rows]
    legend["x"] = "#141a18"
    # small highlight on the top-left pixel of each lit bar
    if level > 0:
        for i, r in enumerate(rows):
            for bar in range(1, level + 1):
                if str(bar) in r:
                    j = r.index(str(bar))
                    rows[i] = r[:j] + "H" + r[j + 1:]
                    r = rows[i]
        legend["H"] = lit["on3"]
    return rows, legend


for lv in range(5):
    NEW[("block", f"battery_cell_{lv}")] = cell(lv)


# --- BUCKETS ---
def bucket(c, s, w, puff):
    rows, legend = bt.TEXTURES[("item", "steam_bucket")]
    rows = list(rows)
    if not puff:
        rows[0] = "................"
    return rows, {**legend, "c": c, "s": s, "w": w}


NEW[("item", "coolant_bucket")] = bucket("#2a9aa6", "#4fd6e0", "#a8f2f6", False)
NEW[("item", "hot_coolant_bucket")] = bucket("#b54a1c", "#ef7a35", "#ffc08a", True)


def hex_rgba(value):
    v = value.lstrip("#")
    r, g, b = int(v[0:2], 16), int(v[2:4], 16), int(v[4:6], 16)
    a = int(v[6:8], 16) if len(v) == 8 else 255
    return r, g, b, a


def render(rows, legend):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y, row in enumerate(rows):
        assert len(row) == 16, row
        for x, ch in enumerate(row):
            if ch == "." and "." not in legend:
                continue
            img.putpixel((x, y), hex_rgba(legend[ch]))
    return img


if __name__ == "__main__":
    for (kind, name), (rows, legend) in NEW.items():
        out = (BLOCK_DIR if kind == "block" else ITEM_DIR) / f"{name}.png"
        render(rows, legend).save(out)
        print("wrote", out.name)
    # contact sheet for a quick look
    sheet = Image.new("RGBA", (len(NEW) * 100, 100), (40, 40, 40, 255))
    for i, ((kind, name), (rows, legend)) in enumerate(NEW.items()):
        sheet.paste(render(rows, legend).resize((96, 96), Image.NEAREST), (i * 100 + 2, 2))
    sheet.save(Path(__file__).parent / "_contact_sheet_v4.png")
