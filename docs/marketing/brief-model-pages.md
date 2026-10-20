# Brief: model page copy for tidytv.winkle.dev

You write the short human part of one web page per TV model. The page generator (tools/build-pages.py)
already prints the full app list, the sections, the source link, the steps and the buttons. You add the
intro, the "stays on" notes, quirks and a short FAQ, all in plain English for a non-technical TV owner.

## Input
`content/tv/data/<id>.json`: the list's header text (`header`), its sections with packages and notes,
`kept` (the header's "kept on purpose" items), `stock_launcher` and `count` (apps on the list).

## Output
Write `content/tv/<id>.json` (NOT inside data/), valid JSON, exactly these keys:

```json
{
  "name": "TCL 65C735",
  "brand": "TCL",
  "os": "Google TV 11",
  "summary": "...",
  "intro": ["...", "..."],
  "keep": ["...", "..."],
  "notes": ["..."],
  "faq": [{"q": "...", "a": "..."}, {"q": "...", "a": "..."}, {"q": "...", "a": "..."}]
}
```

- `name`: what an owner calls it, at most 40 characters. Brand plus model, e.g. "Sony BRAVIA 9 II", "Xiaomi Mi Box S (2nd gen)", "Chromecast with Google TV 4K". Use a sold-as name if the header gives one (e.g. "Mi TV 4C Pro"). No codenames, boards or firmware.
- `brand`: one word or a short brand: TCL, Sony, Xiaomi, Hisense, Philips, Google, NVIDIA, Amazon, onn, etc. Thomson made by TCL is "Thomson". Redmi is "Xiaomi".
- `os`: short, e.g. "Google TV 11", "Android TV 9", "Fire OS 7".
- `summary`: the meta description, 130 to 158 characters. Name the model and what the bloat is on it (ads, trackers, preloaded apps), and say it is a free phone app with a backup and one-tap undo.
- `intro`: 2 paragraphs, 40 to 80 words each. Paragraph 1: what this model ships with that people don't want, using the list's own sections (e.g. TCL ad rows, an ACR viewing tracker, retail demo mode). Paragraph 2: how it was tested (from the header: what kept working after the change, e.g. HDMI, Netflix, the mic button) and that TidyTV turns these off from a phone over Wi-Fi with a backup first and nothing deleted.
- `keep`: 3 to 7 bullets, in plain words, of what stays on and why. Take them from the header's "kept on purpose" notes, e.g. "The remote and HDMI inputs (MediaTek TV service)". Never invent.
- `notes`: 0 to 4 bullets of real quirks from the header that an owner should know, reworded plainly. Leave out anything only about the upstream shell scripts (disable.sh, TV_HOST, STOCK_LAUNCHER and similar). Empty list if there are none.
- `faq`: exactly 3 questions an owner of this model would ask, each answered in 1 to 3 sentences. Good ones: "Will the voice button still work on my <model>?", "Does casting still work?", "Can I get the old home screen back?", "Will it stop the ads on the home screen?". Answer only from the header and the facts below.

## Facts about TidyTV you may use
- A free, open-source Android phone app. No computer and no AI needed. The phone must be on the same Wi-Fi as the TV.
- On the TV you turn on developer options and USB/Network debugging once. The TV asks "Allow debugging?" once.
- It saves a backup first, turns apps off with Android's own setting (it never uninstalls), checks each one, and stops if anything looks wrong.
- Restore everything turns every app back on and puts the old home screen back, in one tap.
- Play Store, Play Services, keyboards, TV inputs, the remote and Bluetooth pairing, Settings and setup apps are always kept.
- Optional clean home screens: Simple (any Android TV) or Apple TV style (Android 8 and newer). Fire TV keeps Amazon's home screen.
- Each app has a switch in the app, so the owner decides.

## Rules
- Everything about the model comes from its data file. If the header does not say it, do not say it.
- No speed or memory numbers unless the header gives them. No "guaranteed", "100% safe", "boost".
- Plain English: short sentences, common words, no jargon (say "viewing tracker" not "ACR" unless you explain it once). No exclamation marks. No emoji.
- Do not copy the header's sentences; reword them.
- Write valid JSON (escape quotes). After writing each file, check it parses: `python3 -c "import json;json.load(open('content/tv/<id>.json'))"`.
