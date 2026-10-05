# Embedded fonts (STR-36)

The editor never uses OS fonts. These files are loaded from the classpath by
`io.stratixops.engine.imgui.glue.FontAtlas` at the DPI scale nearest the monitor
(design system section 5.4). Do not rename them: the resource paths are part of
`FontAtlas`.

| File | Family / weight | Upstream | License |
|---|---|---|---|
| `Inter-Regular.ttf` | Inter 400 | rsms/inter `v4.1` (`extras/ttf`) | OFL 1.1 — `LICENSE-Inter.txt` |
| `Inter-Medium.ttf` | Inter 500 | rsms/inter `v4.1` (`extras/ttf`) | OFL 1.1 — `LICENSE-Inter.txt` |
| `Inter-SemiBold.ttf` | Inter 600 | rsms/inter `v4.1` (`extras/ttf`) | OFL 1.1 — `LICENSE-Inter.txt` |
| `JetBrainsMono-Regular.ttf` | JetBrains Mono 400 | JetBrains/JetBrainsMono `v2.304` | OFL 1.1 — `LICENSE-JetBrainsMono.txt` |
| `lucide.ttf` | Lucide icon subset (PUA) | lucide-icons/lucide `1.52.0` | ISC — `LICENSE-Lucide.txt` |

## Lucide subset

`lucide.ttf` is the official `lucide-font-1.52.0` face subset to only the icons
named by `theme.Icon` (60 distinct glyphs), preserving the upstream Private Use
Area codepoints from `codepoints.json`. `Icon` carries those codepoints, and
`FontResourcesTest` re-derives the mapping from the shipped TTF's `cmap` so a
missing glyph fails the build.

Regenerate with `fonttools` (test-only tooling; not a build dependency):

```sh
pip install fonttools
# 1. download https://github.com/lucide-icons/lucide/releases/download/1.52.0/lucide-font-1.52.0.zip
# 2. collect the codepoints for every name in Icon.java from codepoints.json
# 3. subset:
pyftsubset lucide.ttf --unicodes=U+E061,U+E2E5,... --output-file=lucide.ttf
```

The exact byte hashes below are pinned so a changed upstream face is reviewable.

## SHA-256

```
Inter-Regular.ttf           40D692FCE188E4471E2B3CBA937BE967878F631AD3EBBBDCD587687C7EBE0C82
Inter-Medium.ttf            97AD806F526E41546D46365BB3A393145F75B7B1568913DB74549AD8B8DBA872
Inter-SemiBold.ttf          78A843FADE9D4612A5567302FB595B56976EB5FCEBF4FEA5A5912D638BAFCDE3
JetBrainsMono-Regular.ttf   A0BF60EF0F83C5ED4D7A75D45838548B1F6873372DFAC88F71804491898D138F
lucide.ttf                  4A5597AF1A76B7E0C920C10DBC36CDFFD74047B92A0A455E62404B829AF62D16
```

## DPI notes

`FontAtlas.DPI_SCALES` is `{1.0, 1.25, 1.5, 2.0}`. For a request it selects the
nearest supported scale and rounds every rasterized size to a whole pixel
(`FontAtlas.snapPixels`). ImGui's line height equals the rasterized size, so the
baseline lands on the physical pixel grid at 1.25x and 1.5x; widgets also snap
their baseline through `Ui.textBaseline`. A bitmap is never upscaled.

`FontResourcesTest` + `FontAtlasLoadTest` cover asset completeness and a real
atlas build headlessly. The visual matrix (1x/1.5x/2x, dark/light, no missing
glyphs) is a display-bound check; run `HeadlessUiTest` on a display host or the
editor sample and capture each cell.
