"""Puts real screen content onto the photoreal rooms (TV off) so before/after pairs line up exactly.

    python3 compose.py     # writes A-before.png, A-after.png, B-before.png, B-after.png, C-plate.png, tv-screens.json
"""
import json
from pathlib import Path
import numpy as np
from PIL import Image, ImageFilter, ImageChops, ImageEnhance

HERE = Path(__file__).parent


def find_screen(img: Image.Image):
    """The TV screen is the largest near-black rectangle: rows and columns that are mostly dark."""
    a = np.asarray(img.convert("L")).astype(int)
    dark = a < 24
    h, w = dark.shape
    rows = np.where(dark[:, w // 4: 3 * w // 4].mean(1) > 0.55)[0]
    # keep the longest run of dark rows
    runs, start = [], rows[0]
    for p, q in zip(rows, rows[1:]):
        if q != p + 1:
            runs.append((start, p)); start = q
    runs.append((start, rows[-1]))
    y0, y1 = max(runs, key=lambda r: r[1] - r[0])
    band = dark[y0:y1 + 1]
    cols = np.where(band.mean(0) > 0.9)[0]
    # longest unbroken run of dark columns: a dark window beside the TV is a separate run
    cruns, cs = [], cols[0]
    for p, q in zip(cols, cols[1:]):
        if q > p + 2:
            cruns.append((cs, p)); cs = q
    cruns.append((cs, cols[-1]))
    x0, x1 = max(cruns, key=lambda r: r[1] - r[0])
    inset = 6  # stay inside the bezel
    return int(x0 + inset), int(y0 + inset), int(x1 - inset), int(y1 - inset)


def on_screen(room: Image.Image, screen: Image.Image, box):
    x0, y0, x1, y1 = box
    w, h = x1 - x0, y1 - y0
    s = screen.convert("RGB").resize((w, h), Image.LANCZOS)
    # A photographed screen: a touch softer, slightly lifted blacks, a faint diagonal glare.
    s = s.filter(ImageFilter.GaussianBlur(0.6))
    s = ImageEnhance.Contrast(s).enhance(0.94)
    yy, xx = np.mgrid[0:h, 0:w]
    glare = np.clip(1 - np.abs((xx / w) * 0.9 - (yy / h) * 0.55 - 0.18) * 3.2, 0, 1) * 18
    arr = np.clip(np.asarray(s).astype(float) + glare[..., None], 0, 255).astype("uint8")
    s = Image.fromarray(arr)
    out = room.convert("RGB").copy()
    # Light spill: a soft glow of the screen's own average colour around the TV.
    avg = tuple(int(v) for v in np.asarray(s).reshape(-1, 3).mean(0))
    glow = Image.new("RGB", out.size, (0, 0, 0))
    pad = int(w * 0.12)
    glow.paste(Image.new("RGB", (w + 2 * pad, h + 2 * pad), avg), (x0 - pad, y0 - pad))
    glow = glow.filter(ImageFilter.GaussianBlur(w * 0.09))
    out = ImageChops.add(out, ImageEnhance.Brightness(glow).enhance(0.22))
    out.paste(s, (x0, y0))
    return out


def main():
    pairs = {"A": "rooms/A1-warm-cabinet.png", "B": "rooms/B1-cool-evening.png"}
    corners = {}
    for key, room_path in pairs.items():
        room = Image.open(HERE / room_path)
        box = find_screen(room)
        x0, y0, x1, y1 = box
        for state in ("before", "after"):
            img = on_screen(room, Image.open(HERE / f"screens/{key}-{state}.png"), box)
            img.save(HERE / f"{key}-{state}.png")
            corners[f"{key}-{state}"] = {"tl": [x0, y0], "tr": [x1, y0], "br": [x1, y1], "bl": [x0, y1]}
        if key == "A":
            room.convert("RGB").save(HERE / "C-plate.png")
            corners["C-plate"] = corners["A-before"]
    (HERE / "tv-screens.json").write_text(json.dumps({"size": [2560, 1440], "screens": corners}, indent=1))
    print(json.dumps(corners))


if __name__ == "__main__":
    main()
