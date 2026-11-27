"""Renders the mod logo: an isometric view of an assembled reactor built from the real block models and textures.

Usage: python art-src/logo/render_logo.py [--size 512] [--out path.png]

It reads the JSON block models in src/main/resources/assets/justreactors, so the logo follows texture changes:
re-run it after touching the casing, glass, fuel rod, controller or control rod art.
"""
import argparse
import json
import math
from pathlib import Path

import numpy as np
from PIL import Image, ImageFilter

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / "src" / "main" / "resources" / "assets" / "justreactors"

# ---------------------------------------------------------------- models

VANILLA = {
    "minecraft:block/block": {},
    "minecraft:block/cube": {"elements": [{"from": [0, 0, 0], "to": [16, 16, 16], "faces": {
        f: {"texture": "#" + f} for f in ("down", "up", "north", "south", "west", "east")}}]},
    "minecraft:block/cube_all": {"parent": "minecraft:block/cube", "textures": {
        f: "#all" for f in ("down", "up", "north", "south", "west", "east")}},
    "minecraft:block/cube_bottom_top": {"parent": "minecraft:block/cube", "textures": {
        "up": "#top", "down": "#bottom", "north": "#side", "south": "#side", "west": "#side", "east": "#side"}},
}


def load_model(name):
    if name in VANILLA:
        data = VANILLA[name]
    else:
        ns, path = name.split(":") if ":" in name else ("justreactors", name)
        data = json.loads((ASSETS / "models" / (path + ".json")).read_text())
    textures, elements = {}, None
    if "parent" in data:
        textures, elements = load_model(data["parent"])
        textures = dict(textures)
    textures.update(data.get("textures", {}))
    if "elements" in data:
        elements = data["elements"]
    return textures, elements


_tex_cache = {}


def texture(ref, textures):
    seen = 0
    while ref.startswith("#"):
        ref = textures[ref[1:]]
        seen += 1
        assert seen < 10, ref
    if ref not in _tex_cache:
        path = ref.split(":")[1]
        img = Image.open(ASSETS / "textures" / (path + ".png")).convert("RGBA")
        _tex_cache[ref] = np.asarray(img.crop((0, 0, 16, 16)), dtype=np.float32) / 255.0
    return ref, _tex_cache[ref]


def face_quad(face, f, t):
    """Corner (u0,v0) and the vectors to u1 and v1, in Minecraft's per-face texture convention."""
    (x0, y0, z0), (x1, y1, z1) = f, t
    if face == "north":
        return (x1, y1, z0), (x0 - x1, 0, 0), (0, y0 - y1, 0), (0, 0, -1)
    if face == "south":
        return (x0, y1, z1), (x1 - x0, 0, 0), (0, y0 - y1, 0), (0, 0, 1)
    if face == "west":
        return (x0, y1, z0), (0, 0, z1 - z0), (0, y0 - y1, 0), (-1, 0, 0)
    if face == "east":
        return (x1, y1, z1), (0, 0, z0 - z1), (0, y0 - y1, 0), (1, 0, 0)
    if face == "up":
        return (x0, y1, z0), (x1 - x0, 0, 0), (0, 0, z1 - z0), (0, 1, 0)
    if face == "down":
        return (x0, y0, z1), (x1 - x0, 0, 0), (0, 0, z0 - z1), (0, -1, 0)
    raise ValueError(face)


def default_uv(face, f, t):
    (x0, y0, z0), (x1, y1, z1) = f, t
    return {
        "north": (16 - x1, 16 - y1, 16 - x0, 16 - y0), "south": (x0, 16 - y1, x1, 16 - y0),
        "west": (z0, 16 - y1, z1, 16 - y0), "east": (16 - z1, 16 - y1, 16 - z0, 16 - y0),
        "up": (x0, z0, x1, z1), "down": (x0, 16 - z1, x1, 16 - z0),
    }[face]


def rot_point(p, rx, ry):
    x, y, z = p
    for _ in range(rx // 90):  # x=90: north goes down
        y, z = z, 16 - y
    for _ in range(ry // 90):  # y=90: north goes east
        x, z = 16 - z, x
    return x, y, z


def rot_vec(v, rx, ry):
    x, y, z = v
    for _ in range(rx // 90):
        y, z = z, -y
    for _ in range(ry // 90):
        x, z = -z, x
    return x, y, z


# ---------------------------------------------------------------- scene

N = 5  # outer size of the reactor (N x N x N)
INNER = range(1, N - 1)
RODS = {(x, z) for x in (1, 2, 3) for z in (1, 2, 3)}  # interior (x, z) columns holding fuel rods


def casing_part(x, y, z):
    on_x, on_z = x in (0, N - 1), z in (0, N - 1)
    if y == 0 and (on_x or on_z):
        return "base"
    if y == N - 1 and (on_x or on_z):
        return "top_edge"
    if on_x and on_z:
        return "pillar"
    return "panel"


def build_scene():
    blocks = {}
    for x in range(N):
        for y in range(N):
            for z in range(N):
                shell = x in (0, N - 1) or y in (0, N - 1) or z in (0, N - 1)
                if shell:
                    blocks[(x, y, z)] = ("casing", casing_part(x, y, z))
                elif (x, z) in RODS:
                    blocks[(x, y, z)] = ("fuel_rod", None)
    # glass windows: whole east face, upper two rows of the south face
    for y in INNER:
        for z in INNER:
            blocks[(N - 1, y, z)] = ("glass", None)
    for x in INNER:
        for y in (2, 3):
            blocks[(x, y, N - 1)] = ("glass", None)
    blocks[(2, 1, N - 1)] = ("controller", "south")
    for (x, z) in RODS:
        blocks[(x, N - 1, z)] = ("control_rod", None)
    return blocks


def models_for(pos, kind, extra, blocks):
    """(model name, rx, ry) list for one block, resolving the glass multipart and facings."""
    if kind == "casing":
        return [("block/reactor_casing" + ("" if extra == "none" else "_" + extra), 0, 0)]
    if kind == "fuel_rod":
        return [("block/reactor_fuel_rod", 0, 0)]
    if kind == "control_rod":
        return [("block/reactor_control_rod_0", 0, 0)]
    if kind == "power_tap":
        return [("block/reactor_power_tap", 0, 0)]
    if kind == "coolant_inlet":
        return [("block/reactor_coolant_port_inlet", 0, 0)]
    if kind == "controller":
        return [("block/reactor_controller_on", 0, {"north": 0, "east": 90, "south": 180, "west": 270}[extra])]
    if kind == "glass":
        state = json.loads((ASSETS / "blockstates" / "reactor_glass.json").read_text())
        x, y, z = pos
        nb = {"north": (x, y, z - 1), "south": (x, y, z + 1), "west": (x - 1, y, z), "east": (x + 1, y, z),
              "up": (x, y + 1, z), "down": (x, y - 1, z)}
        props = {d: "true" if blocks.get(p, ("",))[0] == "glass" else "false" for d, p in nb.items()}
        out = []
        for part in state["multipart"]:
            if all(props[k] == v for k, v in part["when"].items()):
                a = part["apply"]
                out.append((a["model"], a.get("x", 0), a.get("y", 0)))
        return out
    raise ValueError(kind)


# ---------------------------------------------------------------- rasterizer

C30, S30 = math.cos(math.radians(30)), 0.5


def project(p):
    x, y, z = p
    return np.array([(x - z) * C30, (x + z) * S30 - y])


def depth(p):
    x, y, z = p
    return x + y + z


SHADE = {(0, 1, 0): 1.0, (0, -1, 0): 0.5, (0, 0, 1): 0.82, (0, 0, -1): 0.82, (1, 0, 0): 0.62, (-1, 0, 0): 0.62}


def collect_quads(blocks):
    quads = []
    for pos, (kind, extra) in blocks.items():
        for model, rx, ry in models_for(pos, kind, extra, blocks):
            textures, elements = load_model(model if ":" in model else "justreactors:" + model)
            translucent = "glass" in model
            for el in elements or []:
                for face, spec in el["faces"].items():
                    ref, tex = texture(spec["texture"], textures)
                    o, du, dv, n = face_quad(face, el["from"], el["to"])
                    uv = spec.get("uv") or default_uv(face, el["from"], el["to"])
                    o = rot_point(o, rx, ry)
                    du, dv, n = rot_vec(du, rx, ry), rot_vec(dv, rx, ry), rot_vec(n, rx, ry)
                    base = np.array(pos, dtype=float) * 16
                    quads.append({
                        "o": base + np.array(o, float), "du": np.array(du, float), "dv": np.array(dv, float),
                        "n": tuple(int(round(c)) for c in n), "uv": uv, "tex": tex, "ref": ref,
                        "translucent": translucent, "emissive": ref.endswith("reactor_fuel_rod")
                        or ref.endswith("controller_on"),
                    })
    return quads


def light(q, wp):
    """Shade factor: face direction, darker inside the vessel, green glow near the fuel rods."""
    s = SHADE[q["n"]]
    inside = np.all((wp > 16 + 0.01) & (wp < (N - 1) * 16 - 0.01), axis=-1)
    glow = np.zeros(wp.shape[:-1])
    for (rx, rz) in RODS:
        d = np.hypot(wp[..., 0] - (rx * 16 + 8), wp[..., 2] - (rz * 16 + 8))
        glow = np.maximum(glow, np.exp(-d / 18.0))
    dark = np.where(inside, 0.45, 1.0)
    return s * dark, np.where(inside, glow, 0.0)


GLASS_BOOST = 2.6
BLOOM = 0.9
GLOW_RGB = np.array([0.55, 1.0, 0.25])


def render(blocks, scale, pad):
    quads = collect_quads(blocks)
    corners = [project(np.array(p) * 16.0) for p in
               [(x, y, z) for x in (0, N) for y in (0, N) for z in (0, N)]]
    mn = np.min(corners, axis=0)
    mx = np.max(corners, axis=0)
    w, h = ((mx - mn) * scale).astype(int) + 2 * pad
    side = max(w, h)
    off = np.array([(side - (mx - mn)[0] * scale) / 2, (side - (mx - mn)[1] * scale) / 2]) - mn * scale

    color = np.zeros((side, side, 3))
    alpha = np.zeros((side, side))
    zbuf = np.full((side, side), -np.inf)
    emis = np.zeros((side, side))

    def raster(q, blend):
        o, du, dv = q["o"], q["du"], q["dv"]
        p0 = project(o) * scale + off
        a = project(du) * scale
        b = project(dv) * scale
        det = a[0] * b[1] - a[1] * b[0]
        if det <= 1e-9:  # back face or edge-on
            return
        pts = np.array([p0, p0 + a, p0 + b, p0 + a + b])
        x0, y0 = np.floor(pts.min(0)).astype(int)
        x1, y1 = np.ceil(pts.max(0)).astype(int)
        x0, y0 = max(x0, 0), max(y0, 0)
        x1, y1 = min(x1, side - 1), min(y1, side - 1)
        if x1 < x0 or y1 < y0:
            return
        ys, xs = np.mgrid[y0:y1 + 1, x0:x1 + 1]
        px, py = xs + 0.5 - p0[0], ys + 0.5 - p0[1]
        s = (px * b[1] - py * b[0]) / det
        t = (a[0] * py - a[1] * px) / det
        m = (s >= 0) & (s < 1) & (t >= 0) & (t < 1)
        if not m.any():
            return
        s, t, ys, xs = s[m], t[m], ys[m], xs[m]
        u0, v0, u1, v1 = q["uv"]
        tu = np.clip(np.floor(u0 + s * (u1 - u0)), 0, 15).astype(int)
        tv = np.clip(np.floor(v0 + t * (v1 - v0)), 0, 15).astype(int)
        texel = q["tex"][tv, tu]
        d = depth(o) + s * depth(du) + t * depth(dv)
        wp = o + s[:, None] * du + t[:, None] * dv
        keep = (texel[:, 3] > 0.02) if blend else (texel[:, 3] >= 0.5)
        keep &= d > zbuf[ys, xs] + 1e-6
        if not keep.any():
            return
        ys, xs, texel, d, wp = ys[keep], xs[keep], texel[keep], d[keep], wp[keep]
        shade, glow = light(q, wp)
        if q["emissive"]:
            shade = np.ones_like(shade)
        rgb = texel[:, :3] * shade[:, None]
        if not q["emissive"]:
            rgb = rgb + GLOW_RGB * (glow * 0.55)[:, None]
        else:
            rgb = rgb * 1.08
        if blend:
            # the in-game pane is almost invisible (alpha ~0.1); boost it so the windows read at icon size
            al = np.clip(texel[:, 3:4] * GLASS_BOOST, 0, 0.9)
            color[ys, xs] = color[ys, xs] * (1 - al) + rgb * al
            alpha[ys, xs] = np.maximum(alpha[ys, xs], texel[:, 3])
        else:
            color[ys, xs] = rgb
            alpha[ys, xs] = 1.0
            zbuf[ys, xs] = d
            emis[ys, xs] = 1.0 if q["emissive"] and "fuel_rod" in q["ref"] else 0.0

    for q in quads:
        if not q["translucent"]:
            raster(q, False)
    for q in sorted((q for q in quads if q["translucent"]), key=lambda q: depth(q["o"] + (q["du"] + q["dv"]) / 2)):
        raster(q, True)
    # bloom: blur the visible fuel rod pixels and add them back, so the core glows through the windows
    src = np.clip(color * emis[..., None], 0, 1)
    radius = float(side) * 0.012
    blurred = np.dstack([np.asarray(Image.fromarray((src[..., c] * 255).astype(np.uint8)).filter(
        ImageFilter.GaussianBlur(radius)), dtype=np.float64) / 255 for c in range(3)])
    color = color + blurred * BLOOM * (alpha > 0)[..., None]
    rgba = np.dstack([np.clip(color, 0, 1), alpha])
    return Image.fromarray((rgba * 255).round().astype(np.uint8), "RGBA")


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--size", type=int, default=512)
    ap.add_argument("--out", default=str(ROOT / "src" / "main" / "resources" / "justreactors_logo.png"))
    args = ap.parse_args()
    ss = 4  # supersampling for clean silhouette edges
    target = args.size * ss
    span = 2 * N * 16 * C30  # projected width of the cube
    pad = int(target * 0.04)
    img = render(build_scene(), (target - 2 * pad) / span, pad)
    img = img.resize((args.size, args.size), Image.BOX)
    img.save(args.out)
    print("wrote", args.out, img.size)


if __name__ == "__main__":
    main()
