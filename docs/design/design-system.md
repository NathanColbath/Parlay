# StratixOps Design System (SDS) — Editor, Phase 1

Owner: UX & Product Designer. Co-owner: Game & Technical Designer.
Consumers: Rendering & Graphics Coder (`engine-imgui`, STR-22), Core Engine Coder
(STR-21), Editor shell (STR-23). Implements STR-18 §4 (`spec` document) and
STR-20.

Status: **Ready for implementation.** Every value below is a contract. Any color
value, size, or token name here is authoritative; if implementation needs a value
not listed, the answer is "do not invent it — raise it against STR-20."

Normative keywords: **MUST** (contract, token maps 1:1 to code), **SHOULD**
(strong default), **MAY** (optional).

Companion files:
- `docs/design/tokens.json` — machine-readable token source of truth. The theme
  module MUST load these names/values; no hex value may appear in code that is
  not in this file.
- Icon set: Lucide (ISC license), referenced by name; see §1.7.

---

## 0. Principles (why this looks the way it does)

1. **The scene is the hero.** Chrome is low-saturation and quiet; the viewport and
   accent color carry attention. No default-ImGui grey (#808080-ish) anywhere.
2. **State is redundant.** Hover, selection, focus, error are each encoded with at
   least two cues (fill + bar/icon/weight), never hue alone.
3. **Density with air.** 22–24 px rows, 4 px rhythm, generous panel padding. Dense
   like a pro tool, never cramped.
4. **One control per intent.** Hierarchy and Project share one tree row; every
   numeric field is one property-row number widget. Learn it once, use it everywhere.
5. **Keyboard is never second-class.** Every action has a binding and a command
   palette entry; mouse is an accelerator, not a requirement.

---

## 1. Foundations

### 1.1 Color

Two layers: a **raw palette** (named by value, never used directly in widgets)
and **semantic tokens** (named by role, the only thing widgets reference). Theme
= the semantic layer's resolved values for dark or light.

#### Raw palette (dark + light share hue, differ in lightness)

| Raw | Hex | | Raw | Hex |
|---|---|---|---|---|
| neutral.950 | `#0B0E14` | | accent.400 | `#38E1C4` |
| neutral.925 | `#0E121A` | | accent.500 | `#14C8A8` |
| neutral.900 | `#11151D` | | accent.600 | `#0EA88D` |
| neutral.875 | `#161B24` | | accent.700 | `#0B8E77` |
| neutral.850 | `#1B212C` | | violet.500 | `#7C5CFF` |
| neutral.800 | `#222A36` | | green.500 | `#3FB950` |
| neutral.700 | `#2A3341` | | green.700 | `#1A7F37` |
| neutral.600 | `#46536A` | | amber.500 | `#D29922` |
| neutral.500 | `#6B7688` | | amber.700 | `#9A6700` |
| neutral.400 | `#9AA4B2` | | red.500 | `#F85149` |
| neutral.300 | `#C2C9D2` | | red.600 | `#CF222E` |
| neutral.100 | `#EFF2F5` | | blue.500 | `#58A6FF` |
| neutral.050 | `#F7F9FB` | | blue.600 | `#0969DA` |
| white | `#FFFFFF` | | focus.blue | `#6FB7FF` |

#### Semantic tokens — dark theme (default)

| Token | Value | Use |
|---|---|---|
| `bg.canvas` | `#0B0E14` | app background, scene void, letterbox bars |
| `bg.panel` | `#11151D` | docked panel body |
| `bg.toolbar` | `#161B24` | menu bar, toolbar, tab strip, table header |
| `bg.elevated` | `#1B212C` | popups, context menus, modals, input field fill |
| `bg.input` | `#1B212C` | text/number input fill |
| `bg.overlay` | `rgba(8,10,14,0.72)` | modal scrim, nav dim |
| `surface.hover` | `rgba(255,255,255,0.05)` | row/button hover |
| `surface.active` | `rgba(255,255,255,0.09)` | pressed / held |
| `surface.selected` | `rgba(20,200,168,0.18)` | selected row/field |
| `border.subtle` | `#1E2530` | separators, inner dividers |
| `border.default` | `#2A3341` | panel, popup, input outlines |
| `border.strong` | `#46536A` | scrollbar grab, table header border, input when WCAG boundary needed |
| `border.focus` | `#6FB7FF` | keyboard focus ring (2 px) |
| `text.primary` | `#EFF2F5` | default text |
| `text.secondary` | `#C2C9D2` | labels, secondary text |
| `text.muted` | `#9AA4B2` | hints, shortcut hints, metadata |
| `text.disabled` | `#6B7688` | disabled text (exempt from 4.5:1) |
| `text.onAccent` | `#04110E` | text/icon on accent fill |
| `text.inverse` | `#0B0E14` | text on light surfaces |
| `accent.default` | `#14C8A8` | primary accent, selection bar, sliders |
| `accent.hover` | `#2FD8BC` | accent hover |
| `accent.active` | `#0EA88D` | accent pressed |
| `accent.subtle` | `rgba(20,200,168,0.16)` | selected fill, chip bg, drop target |
| `accent.text` | `#38E1C4` | accent-colored text/icons on dark |
| `state.success` | `#3FB950` | success |
| `state.success.subtle` | `rgba(63,185,80,0.16)` | success bg |
| `state.warn` | `#D29922` | warning |
| `state.warn.subtle` | `rgba(210,153,34,0.16)` | warning bg |
| `state.error` | `#F85149` | error, invalid, destructive |
| `state.error.subtle` | `rgba(248,81,73,0.16)` | error bg |
| `state.info` | `#58A6FF` | info |
| `state.info.subtle` | `rgba(88,166,255,0.16)` | info bg |
| `axis.x` | `#F85149` | transform X (always labeled "X") |
| `axis.y` | `#3FB950` | transform Y (always labeled "Y") |
| `axis.z` | `#58A6FF` | transform Z (always labeled "Z") |
| `gizmo.move` | `#FFD166` | move handle |
| `gizmo.rotate` | `#4DD4E8` | rotate handle |
| `gizmo.scale` | `#C792EA` | scale handle |
| `grid.line` | `#151B24` | scene minor grid |
| `grid.line.major` | `#1E2530` | scene major grid |
| `guide` | `#7C5CFF` | guides, marquee, snapping hint |
| `placeholder.missing` | `#FF00FF` | missing-asset placeholder (ADR-0001) |
| `prefab.override` | `#58A6FF` | prefab override marker (P2) |
| `linking` | `#7C5CFF` | object picker / reference link visual (P2) |

#### Semantic tokens — light theme

| Token | Value | Use |
|---|---|---|
| `bg.canvas` | `#F7F9FB` | app background |
| `bg.panel` | `#FFFFFF` | panel body |
| `bg.toolbar` | `#F0F3F7` | menu bar, toolbar, tab strip |
| `bg.elevated` | `#FFFFFF` | popups, menus (paired with elevation e1/e2 shadow) |
| `bg.input` | `#FFFFFF` | input fill |
| `bg.overlay` | `rgba(17,21,29,0.32)` | modal scrim |
| `surface.hover` | `rgba(11,14,20,0.045)` | row/button hover |
| `surface.active` | `rgba(11,14,20,0.075)` | pressed |
| `surface.selected` | `rgba(14,168,141,0.15)` | selected |
| `border.subtle` | `#E8ECF1` | separators |
| `border.default` | `#D5DCE4` | outlines |
| `border.strong` | `#AFBAC7` | grabs/headers |
| `border.focus` | `#1E6FD9` | focus ring |
| `text.primary` | `#11151D` | default text |
| `text.secondary` | `#333D4D` | labels |
| `text.muted` | `#6B7688` | hints |
| `text.disabled` | `#9AA4B2` | disabled |
| `text.onAccent` | `#FFFFFF` | text on accent fill |
| `text.inverse` | `#EFF2F5` | text on dark surfaces |
| `accent.default` | `#0EA88D` | primary accent |
| `accent.hover` | `#12BC9E` | hover |
| `accent.active` | `#0B8E77` | pressed |
| `accent.subtle` | `rgba(14,168,141,0.14)` | selected fill |
| `accent.text` | `#0B7A66` | accent text on light |
| `state.success` | `#1A7F37` | success |
| `state.warn` | `#9A6700` | warning |
| `state.error` | `#CF222E` | error |
| `state.info` | `#0969DA` | info |
| `axis.x` | `#CF222E` | X |
| `axis.y` | `#1A7F37` | Y |
| `axis.z` | `#0969DA` | Z |
| `grid.line` | `#EEF1F5` | minor grid |
| `grid.line.major` | `#E2E7EE` | major grid |
| `placeholder.missing` | `#FF00FF` | missing asset |
| `prefab.override` | `#0969DA` | override marker |

Rules:
- Widgets MUST resolve `state.*.subtle` for fills and the solid `state.*` for
  icons/borders/text so error/info read the same in both themes.
- Missing raw light values inherit the dark row (e.g. `guide`, `gizmo.*` keep the
  same hue, darkened 10% for light backgrounds: guide `#6547E6`, move `#C99A1F`,
  rotate `#1E9DAF`, scale `#8B5BB5`).

### 1.2 Typography

Two families, embedded as a submodule/resource, no OS fonts:
- **UI:** Inter (Regular 400, Medium 500, SemiBold 600). Never Bold 700 in UI.
- **Mono:** JetBrains Mono (Regular 400, Medium 500) for console, code, numbers.

Type scale (logical px @ dpiScale 1.0):

| Token | Family/weight | Size | Line height | Letter spacing | Use |
|---|---|---|---|---|---|
| `type.xs` | Inter 400 | 11 | 16 | 0.2 | badges, captions, column meta |
| `type.sm` | Inter 400 | 12 | 18 | 0.1 | tree rows, property labels, buttons, console |
| `type.md` | Inter 400 | 13 | 20 | 0 | menu items, text inputs, toasts body, palette rows |
| `type.mdMedium` | Inter 500 | 13 | 20 | 0 | section titles, active row emphasis |
| `type.lg` | Inter 600 | 15 | 22 | 0 | panel headers, dialog titles |
| `type.xl` | Inter 600 | 18 | 26 | -0.1 | modal headers, welcome title |
| `type.2xl` | Inter 600 | 24 | 32 | -0.2 | welcome screen display |
| `type.mono` | JetBrains Mono 400 | 12 | 18 | 0 | console, GUIDs, type ids |
| `type.monoSm` | JetBrains Mono 400 | 11 | 16 | 0 | dense numeric table |

Rules:
- One `type.md` line = 20 px; a 24 px control is text + 2 px vertical padding +
  2 px borders. Do not vertically center by eyeballing: use the line-height box.
- Truncate with ellipsis, never clip mid-glyph. Tooltip carries full text (§5.2).
- Uppercase only for short category headers, tracking +0.6, `type.xs`, muted.

### 1.3 Spacing & sizing

4 px base scale:

| Token | px | Token | px |
|---|---|---|---|
| `space.0` | 0 | `space.5` | 24 |
| `space.1` | 4 | `space.6` | 32 |
| `space.2` | 8 | `space.7` | 48 |
| `space.3` | 12 | `space.8` | 64 |
| `space.4` | 16 | | |

Application rules (MUST):
- Inside a control: `space.1` vertical, `space.2` horizontal minimum.
- Between sibling controls: `space.2`.
- Between field groups: `space.3`.
- Panel padding: `space.2` horizontal, `space.1` vertical (dense), `space.3`
  around empty states and settings forms.
- Section separation: `space.5`.
- Major blocks / welcome: `space.6`–`space.8`.

Fixed sizes (logical px):

| Token | px | Token | px |
|---|---|---|---|
| `size.menuBar.h` | 28 | `size.control.sm` | 20 |
| `size.toolbar.h` | 32 | `size.control.md` | 24 |
| `size.tab.h` | 28 | `size.control.lg` | 28 |
| `size.panel.header.h` | 28 | `size.control.xl` | 32 |
| `size.statusBar.h` | 24 | `size.row.h` | 22 |
| `size.breadcrumb.h` | 24 | `size.chip.h` | 20 |
| `size.icon.sm` | 14 | `size.indent.perLevel` | 14 |
| `size.icon.md` | 16 | `size.scrollbar.w` | 10 |
| `size.icon.lg` | 20 | `size.minWindow.w` | 1024 |
| `size.colorSwatch` | 20 | `size.minWindow.h` | 640 |
| `size.thumb.sm` | 48 | `size.thumb.md` | 72 |
| `size.thumb.lg` | 96 | `size.palette.w` | 560 |

### 1.4 Radius & elevation

| Token | px | Use |
|---|---|---|
| `radius.xs` | 2 | badges, tags, keycaps |
| `radius.sm` | 4 | buttons, inputs, tree rows, chips, checkboxes |
| `radius.md` | 6 | panels (floating), popups, context menus, cards, toasts |
| `radius.lg` | 8 | modals, floating windows, command palette |
| `radius.pill` | 999 | pills, slider rail, toggle track |

Docked panel frames use `radius.xs` (2) or 0; floating windows use `radius.lg`.

Elevation (shadow color = `rgba(0,0,0,a)` dark / `rgba(16,24,40,a)` light):

| Token | Shadow | Use |
|---|---|---|
| `elev.0` | none | docked panels, menu bar, toolbar, status bar, tree rows |
| `elev.1` | `0 2px 8px a=.35` (dark) / `.12` (light) | popups, context menus, dropdowns, tooltips |
| `elev.2` | `0 8px 24px a=.45` / `.18` | floating windows, modals, command palette |
| `elev.3` | `0 12px 32px a=.50` / `.20` + 1 px `accent.default` border | drag ghost |

### 1.5 Motion

Durations (ms) and easing:

| Token | Value | Use |
|---|---|---|
| `motion.instant` | 0 | tree expand, layout reflow, camera, sliders |
| `motion.fast` | 120 | hover fades, tooltips, tab indicator, dropdown open |
| `motion.base` | 160 | popups, toasts slide, command palette open, focus ring |
| `motion.slow` | 240 | modal enter, panel maximize |

Easing: `easeOut` = `cubic-bezier(0.2, 0, 0, 1)` (default); `easeInOut` =
`cubic-bezier(0.4, 0, 0.2, 1)` (enter/exit pairs). Never `linear` except the
drag ghost (follows cursor exactly, no tween).

Allowed animated properties: opacity, 2–4 px translate (popups/palette/toast),
2 px width tab underline, panel dim/focus, progress underline. Forbidden: layout
reflow, tree expand/collapse, viewport/camera, gizmo size, scroll position.

`ProjectSettings.ui.reduceMotion` (or OS setting) MUST set all durations to
`motion.instant` and snap opacity (no cross-fade). Provide it in Project Settings.

### 1.6 Icons

- Set: **Lucide**, 24 px grid, stroke `1.5`, round caps/joins. Rasterized into
  the font atlas at each DPI size (16 default, 14 dense, 20 toolbar). Never
  scale a bitmap.
- Default color: `text.secondary`; active/selected: `text.primary` or
  `accent.text`; disabled: `text.disabled`.
- Every icon-only control MUST have a tooltip (§5.2) and a text label in the
  command palette.

Icon map (semantic → Lucide name). Missing here? Use `circle` placeholder and
file a note on STR-20; do not pick a random icon.

| Action / type | Icon | Action / type | Icon |
|---|---|---|---|
| GameObject | `box` | Add / Create | `plus` |
| Transform | `move-3d` | Create Empty Child | `corner-down-right` |
| SpriteRenderer | `image` | Search | `search` |
| Animator | `film` | Play | `play` |
| Camera | `video` | Pause | `pause` |
| AudioSource | `volume-2` | Step | `skip-forward` |
| Collider2D | `box-select` | Save | `save` |
| Behaviour / Script | `braces` | Undo / Redo | `undo-2` / `redo-2` |
| Prefab | `package` | Duplicate | `copy` |
| Scene | `layout-template` | Delete | `trash-2` |
| AssetObject | `database` | Rename | `pencil` |
| Folder | `folder` / `folder-open` | Settings | `settings` |
| Image asset | `image` | Console info | `info` |
| Audio asset | `music` | Console warn | `triangle-alert` |
| Text asset | `file-text` | Console error | `circle-x` |
| Scene asset | `layout-template` | Console success | `circle-check` |
| Material | `palette` | Visibility on/off | `eye` / `eye-off` |
| Hierarchy panel | `list-tree` | Lock on/off | `lock` / `unlock` |
| Inspector panel | `sliders-horizontal` | Move/Rotate/Scale gizmo | `move` / `rotate-cw` / `maximize` |
| Project panel | `folder-tree` | Snap | `magnet` |
| Console panel | `terminal` | Pivot | `locate-fixed` |
| Stats panel | `activity` | Zoom to fit | `scan` |
| Command palette | `command` | Refresh / Import | `refresh-cw` |
| Close | `x` | Revert to default | `rotate-ccw` |
| Chevron | `chevron-right` / `chevron-down` | Color eyedropper | `pipette` |
| Submenu | `chevron-right` | More | `ellipsis` |
| Breadcrumb separator | `chevron-right` | Object link | `link` |
| Drag handle | `grip-vertical` | Recents | `history` |

### 1.7 Focus, hover, selection (state model)

Four independent states; each has a distinct, testable visual:

| State | Dark visual | Light visual |
|---|---|---|
| hover | `surface.hover` fill | `surface.hover` fill |
| active/pressed | `surface.active` fill | `surface.active` fill |
| selected | `surface.selected` fill **+ 2 px `accent.default` left bar** + `text.primary` | same with light tokens |
| focused (keyboard) | 2 px `border.focus` ring, 1 px offset (outer `bg` gap) | same |
| drop target | `accent.subtle` fill + 1 px `accent.default` border | same |

Selected + hovered = selected fill, hover lightens by 4%. Selected + focused =
both bar and ring. Focus ring MUST be visible whenever Tab/arrow nav touched the
control and MUST NOT be suppressed.

---

## 2. Token → theme code (1:1)

The theme module MUST define a `Theme` with one field per token, grouped:
`Theme.color.*`, `Theme.typography.*`, `Theme.space.*`, `Theme.size.*`,
`Theme.radius.*`, `Theme.elev.*`, `Theme.motion.*`, `Theme.icon.*`.

Java constant naming: `SCREAMING_SNAKE` of the token path, e.g.
`bg.panel` → `BG_PANEL`, `accent.hover` → `ACCENT_HOVER`,
`state.error.subtle` → `STATE_ERROR_SUBTLE`, `type.mdMedium` → `TYPE_MD_MEDIUM`.
`tokens.json` keys MUST equal these paths exactly (see Appendix A).

### 2.1 ImGui style var mapping (docking branch, ImGui ≥ 1.91)

`ImGuiCol_*` ← semantic token:

| ImGui enum | Token | Notes |
|---|---|---|
| `Text` | `text.primary` | |
| `TextDisabled` | `text.disabled` | |
| `WindowBg` | `bg.panel` | |
| `ChildBg` | transparent | panels draw own bg |
| `PopupBg` | `bg.elevated` | + `elev.1`/`elev.2` custom shadow |
| `Border` | `border.default` | |
| `BorderShadow` | transparent | custom shadows instead |
| `FrameBg` | `bg.input` | |
| `FrameBgHovered` | `surface.hover` | |
| `FrameBgActive` | `surface.active` | |
| `TitleBg` | `bg.toolbar` | title bar hidden; panel header is custom 28 px |
| `TitleBgActive` | `bg.panel` | |
| `TitleBgCollapsed` | `bg.toolbar` | |
| `MenuBarBg` | `bg.toolbar` | |
| `ScrollbarBg` | transparent | |
| `ScrollbarGrab` | `border.strong` | |
| `ScrollbarGrabHovered` | `text.muted` | |
| `ScrollbarGrabActive` | `accent.default` | |
| `CheckMark` | `accent.text` | |
| `SliderGrab` | `accent.default` | |
| `SliderGrabActive` | `accent.hover` | |
| `Button` | `bg.elevated` | |
| `ButtonHovered` | `surface.hover` | over `bg.elevated` |
| `ButtonActive` | `surface.active` | |
| `Header` | `surface.selected` | Selectable/TreeNode selected |
| `HeaderHovered` | `surface.hover` | |
| `HeaderActive` | `surface.selected` | |
| `Separator` | `border.subtle` | |
| `SeparatorHovered` | `accent.default` | |
| `SeparatorActive` | `accent.hover` | |
| `ResizeGrip` | transparent | |
| `ResizeGripHovered` | `accent.default` @ 50% | |
| `ResizeGripActive` | `accent.default` | |
| `Tab` | `bg.panel` | |
| `TabHovered` | `surface.hover` | |
| `TabSelected` | `bg.elevated` | + 2 px `accent.default` overline |
| `TabSelectedOverline` | `accent.default` | |
| `TabDimmed` | `bg.panel` | |
| `TabDimmedSelected` | `bg.elevated` | |
| `PlotLines` | `accent.default` | |
| `PlotLinesHovered` | `accent.hover` | |
| `PlotHistogram` | `accent.default` | |
| `PlotHistogramHovered` | `accent.hover` | |
| `TableHeaderBg` | `bg.toolbar` | |
| `TableBorderStrong` | `border.strong` | |
| `TableBorderLight` | `border.subtle` | |
| `TableRowBg` | transparent | |
| `TableRowBgAlt` | `surface.hover` @ 50% | |
| `TextSelectedBg` | `surface.selected` | |
| `DragDropTarget` | `accent.default` | prefer custom overlay (§3.8) |
| `NavCursor` | `border.focus` | keyboard nav rect |
| `NavWindowingHighlight` | `text.primary` @ 70% | |
| `NavWindowingDimBg` | `bg.overlay` | |
| `ModalWindowDimBg` | `bg.overlay` | |
| `TextLink` | `accent.text` | |

`ImGuiStyleVar_*` ← size tokens:

| Style var | Value |
|---|---|
| `WindowPadding` | `(space.2, space.1)` = (8, 4) — panels use custom header/padding |
| `WindowBorderSize` | 1 |
| `WindowRounding` | 0 docked, `radius.lg` (8) floating |
| `ChildRounding` | `radius.sm` (4) |
| `ChildBorderSize` | 1 |
| `PopupRounding` | `radius.md` (6) |
| `PopupBorderSize` | 1 |
| `FramePadding` | `(space.1+1, space.1)` = (5, 4) at 13 px → 24 px control |
| `FrameRounding` | `radius.sm` (4) |
| `FrameBorderSize` | 1 |
| `ItemSpacing` | `(space.2, space.1)` = (8, 4) |
| `ItemInnerSpacing` | `(space.1, space.1)` = (4, 4) |
| `CellPadding` | `(space.1, 2)` |
| `IndentSpacing` | `size.indent.perLevel` (14) |
| `ColumnsMinSpacing` | `space.2` (8) |
| `ScrollbarSize` | `size.scrollbar.w` (10) |
| `ScrollbarRounding` | `radius.md` (6) |
| `GrabMinSize` | 10 |
| `GrabRounding` | `radius.sm` (4) |
| `TabRounding` | `radius.sm` (4) |
| `TabBorderSize` | 0 |
| `ButtonTextAlign` | `(0.5, 0.5)` |
| `SelectableTextAlign` | `(0, 0.5)` |
| `WindowMinSize` | `(240, 120)` |
| `WindowTitleAlign` | `(0, 0.5)` |
| `DisabledAlpha` | `0.45` |

Custom draw layer (not expressible as ImGui colors) MUST implement: panel header
with focus underline, 2 px selection bar, focus ring with offset, drag ghost,
drop insertion line, breadcrumb, property label column, chips, toast cards,
elevation shadows, icon atlas. All draw calls read `Theme.*`; no literals.

---

## 3. Widget kit

Shared primitives used by all widgets:

- `Row`: height `size.row.h` (22) or `size.control.md` (24); full-width;
  hover/selected from §1.7; horizontal padding `space.2`; gap `space.2`.
- `Chip`: height `size.chip.h` (20), `radius.pill` or `radius.xs`, `type.xs`,
  `space.1` horizontal padding; variants: neutral (`border.default` + `bg.panel`),
  accent (`accent.subtle` + `accent.text`), state (`state.*.subtle` + `state.*`).
- `IconButton`: 20×20 hit target (24 in toolbar), icon 16, hover `surface.hover`,
  `radius.sm`. Always has a tooltip.
- `Section`: `type.mdMedium` title + optional collapse chevron (`size.icon.sm`),
  `space.3` vertical rhythm. Section bodies indent `space.2`.
- `KeyHint`: `type.xs` `text.muted`, right-aligned; render keycaps with
  `radius.xs`, `border.default`, `space.1` padding.
- `Divider`: 1 px `border.subtle` full-bleed for panel sections; `space.1`
  inset for menu separators.

### 3.1 Tree row (Hierarchy + Project folders)

Purpose: one row component for every hierarchy/asset tree.

Anatomy (left→right): `indent` (14 × depth) · disclosure chevron (16, only if
children) · type icon (16) · label (`type.sm`, `text.primary`; `text.secondary`
when unselected? no — primary always, muted for inactive objects) · spacer · meta
chips (visibility `eye`, lock `lock`, prefab `package`; icon 14, muted).

Metrics: total height 22; row hit area full width; chevron hit 16×16; double
click toggles expand only on chevron/row when children exist.

States:
- default: transparent
- hover: `surface.hover`
- selected: `surface.selected` + 2 px `accent.default` left bar; label `text.primary`
- multi-selected (not primary): `surface.active`, no left bar
- keyboard-focused: focus ring (2 px `border.focus`, 1 px offset)
- dragging (source): row at 40% opacity
- drop-before/after (reorder): 2 px `accent.default` line above/below the row
- drop-into (reparent): `accent.subtle` fill + 1 px `accent.default` border
- invalid drop: 1 px `state.error` border, `no-drop` cursor
- disabled/inactive object (Unity "active" off): label `text.disabled`
- renamed: label replaced by inline editor

Inline rename: enter via F2 or slow double-click; editor is a frameless text
field (`type.sm`, `bg.input`, 1 px `border.focus`), text pre-selected, max width
of column; Enter/blur commits, Esc cancels, empty name reverts. Validation:
reject duplicate sibling names with `state.error` border + tooltip.

Drag/drop: 4 px threshold; auto-expand a collapsed folder hovered 500 ms;
auto-scroll within 8 px of edge at 8 px/frame; drop line follows cursor between
rows.

Keyboard: ↑/↓ move visible rows; ← collapse, or move to parent if collapsed;
→ expand, or move to first child; Home/End first/last; `Shift+↑/↓` range select;
`Ctrl+click` toggle; `Ctrl+A` select all in panel; Enter = rename; Space =
toggle active; Delete = delete; `Ctrl+D` duplicate.

### 3.2 Property row (Inspector)

Purpose: one component for every inspected field.

Layout: two columns. Label column fixed `clamp(40% , 96–200 px)`, right-padded
`space.2`; field column flexible. Row min-height 24; grows with multiline.

Anatomy: label (`type.sm`, `text.secondary`; tooltip if truncated) · field
widget · optional revert dot and override bar.

Label behaviors: hover shows tooltip (field name + tooltip text + type); dragging
the label horizontally scrubs a numeric field (Unity behavior); click selects the
row for keyboard nav.

Field widget types (the full set — implement all in `engine-imgui`):

| Type | Widget spec |
|---|---|
| `bool` | 16×16 checkbox, `radius.sm`, `accent.default` check on `text.onAccent`; clicking label toggles too |
| `int`/`float` | drag-scrub number: click = edit text (`type.mono`), drag = scrub by step; ± step buttons 16 px appear on hover; clamp via min/max; unit suffix muted |
| `slider` | slider rail 4 px `radius.pill`, grab 12 px circle; value shows in `type.mono` right |
| `vec2`/`vec3` | N sub-fields on one row, each prefixed `X`/`Y`/`Z` colored `axis.*`; label drag scrubs all; right-click → reset axis |
| `color` | 20×20 swatch `radius.sm`, checkerboard under alpha; click opens popup: SV square + hue slider + A slider + hex field + eyedropper + preset row |
| `enum` | dropdown (`size.control.md`), `chevron-down`, keyboard type-ahead |
| `string` | text field; overflow ellipsis; `space.4` char counter only when maxLength set |
| `multiline` | text area, min 3 lines, grows to 8 then scrolls |
| `assetRef` | 16 px thumbnail + filename + type icon; drop target; clear `x` on hover; click selects asset in Project |
| `objectRef` | icon + name; drag target; clear on hover; click pings/pulses target in Hierarchy |
| `list`/`array` | header with count + add/remove; reorderable rows with `grip-vertical`; foldout per element; "Add" inserts at end |
| `nestedObject` | foldout (default open) with child property rows indented `space.2` |
| `readonly` | value in `text.muted`, no frame, tooltip "computed" |

Value states:
- **Modified from default:** a 6 px `accent.default` dot at the field's right;
  field context menu adds "Revert to default", "Revert All".
- **Prefab override (P2):** 2 px `prefab.override` left bar on the row; context
  menu "Apply to Prefab", "Revert"; tooltip names the prefab.
- **Multi-object edit:** differing values show `---` centered (`text.muted`);
  editing sets all targets; no revert dot.
- **Disabled:** label `text.disabled`, field non-interactive, tooltip explains
  (e.g. "Enabled in Play mode only").
- **Invalid:** 1 px `state.error` border + error tooltip; blocks Play? no, logs.

Context menu on any field: Copy, Paste, Revert to default, and for refs
"Show in Project". `[SerializeField]` reflection (P2) maps Java types to these
widgets 1:1.

### 3.3 Searchable create menu

Triggered by: right-click in Hierarchy/Project, `+` button, or "Add Component".

Anatomy: popup `radius.md`, `elev.1`, width min 280, max height 400, padding
`space.1`; autofocused search field at top (29 px, magnifier icon, placeholder
"Search…"); scrollable result list; pinned quick actions at top when query empty.

Results: grouped by category with `type.xs` uppercase muted headers (Rendering,
Physics, Audio, UI, Scripts; Hierarchy: only "Create"). Each row: 16 px icon ·
`type.md` name · fuzzy-highlighted matched characters in `accent.text` · optional
subtitle. Right-aligned keyboard hint where relevant.

Behavior: fuzzy filter is case-insensitive, matches on name then category;
matched chars highlighted; arrow keys move selection, Enter creates and closes,
Esc closes and returns focus, Tab cycles focus, left/right collapse/expand
groups. Empty state: "No results for '<query>'". When query empty: "Create
Empty", "Create Empty Child" (context-dependent) pinned, then full grouped list,
then "Recent" (`history`) group.

### 3.4 Asset grid + breadcrumb (Project)

Breadcrumb bar (h 24): root (`folder-tree`) · segments (`type.sm`, muted) with
`chevron-right` separators; segments clickable; drag asset onto a segment moves
it (drop target = `accent.subtle` + border); overflow collapses to `…` popup;
right side: view toggle (grid/list) + search field 180 px.

Filter chips row (optional, h 24): `All · Scene · Prefab · Script · Image ·
Audio · Font`, pill chips; selected = `accent.subtle` + `accent.text`.

Grid view: tile 96×96, thumb 72 inside, `radius.sm`, 1 px `border.default`;
filename `type.sm` 2 lines ellipsis centered. Hover: tile bg `surface.hover`,
reveal checkbox + `ellipsis` button. Selected: `accent.subtle` bg + 1 px
`accent.default` border. Multi-select: `Ctrl+click`. Thumbnails cached; missing
asset = magenta `placeholder.missing` tile with `triangle-alert`; unimported =
`bg.elevated` + spinner.

List view: columns Name (flex, icon+label), Type (80), Size (80, right), Modified
(140); header h 28 `bg.toolbar`; sortable (arrow indicator `accent.text`);
resizable columns (min 60). Row height 22, same state model as tree rows.

Keyboard: arrows move, Shift+arrows extend, Enter opens, F2 rename, Delete
delete, Backspace navigate up, `Ctrl+F` focus search, `Ctrl+A` select all.

Drag: files out to Scene/Hierarchy/Inspector; `Ctrl+drag` copies, plain drag
moves within Project; drop into folder highlights it; import progress toast.

### 3.5 Command palette (`Ctrl/Cmd+Shift+P`)

Overlay: centered horizontally, top at 12% of viewport; width 560, max height
400; `radius.lg`, `elev.2`, `bg.elevated`; 1 px `border.default`. Scrim optional
(`bg.overlay` at 40%) only when not reduce-motion.

Anatomy: search field h 40 (`type.md`, autofocus, `search` icon) · divider ·
result list; each row 32 px: category icon (16) · command name (`type.md`) ·
muted subtitle/breadcrumb path · right-aligned `KeyHint`. Group headers per
category (Window, GameObject, Component, Asset, Project, Play, Preferences).
Recently used commands pinned to top under "Recent".

Behavior: fuzzy match, select first, ↑/↓ move, Enter runs + closes, Esc closes,
`Tab`/`Shift+Tab` between groups, mouse hover fine. Running a command closes the
palette and returns focus to the prior control. Disabled commands render
`text.disabled` and explain why in a tooltip. Empty state: "No commands match
'<query>'". Footer strip: "N commands" left, "↵ run · esc close" right.

### 3.6 Toasts

Placement: bottom-right, `space.4` from edges; stack max 4 visible (older beyond
4 collapse into "+N"); width 320; `radius.md`; `elev.1`; padding `space.3`; 3 px
left border in the variant color.

Anatomy: variant icon (16) · title (`type.sm` SemiBold) · body (`type.sm` muted,
optional) · action link (`accent.text`, optional) · close `x` (hover-reveal).

Variants + timing:

| Variant | Border/icon | Auto-dismiss | Motion |
|---|---|---|---|
| info | `state.info` | 4 s | slide-in 160 easeOut from right, stack reflow 120 |
| success | `state.success` | 4 s | same |
| warning | `state.warn` | 6 s | same |
| error | `state.error` | sticky until closed | same |
| progress | `accent.default` | until updated/closed | 2 px progress underline |

Behavior: hover pauses the dismiss timer; identical consecutive messages collapse
into "×N"; clicking the body focuses the related console entry/asset; keyboard
`Alt+T` focuses the newest toast, Esc dismisses; screen-reader announcements via
the accessibility hook (§5). Never block the UI; modals only for destructive
confirmations.

### 3.7 Context menus

Popup: content width min 180 / max 360; row h 24; padding `space.1`;
`radius.md`; `elev.1`; 1 px `border.default`; appears at cursor and clamps to
keep 8 px inside the window.

Row anatomy: 16 px icon · label (`type.md`) · optional `KeyHint` right ·
submenu `chevron-right`; separators are 1 px `border.subtle` with `space.1`
vertical; disabled rows `text.disabled` with reason tooltip.

Variants: checkable rows show `check` left; radio groups show `circle-dot`.
Behavior: ↑/↓, → opens submenu (120 ms delay, flips near edges), ← closes,
Enter activates, Esc closes and returns focus, type-ahead jumps to first match.

### 3.8 Drag-and-drop

- Threshold 4 px before a drag starts (prevents accidental moves).
- Ghost: snapshot of the dragged element at 50% opacity, `elev.3`, 1 px
  `accent.default` border, offset `+12,+12` from cursor; 1 px `accent.default`
  insertion line for reordering; `accent.subtle` fill + 1 px border for
  drop-into.
- Invalid target: `state.error` 1 px border + `no-drop` cursor + optional toast.
- Modifiers: plain = move/reparent; `Ctrl` = copy (`+` cursor badge);
  `Alt` = create reference/link (`link` cursor badge, violet `linking`).
- Auto-scroll during drag near viewport edges (8 px band, 8 px/frame).
- Auto-expand collapsed folder hovered 500 ms.
- Esc cancels; dropping outside any target is a no-op with a subtle snap-back.
- Snap to drop zones when within 8 px; never drop ambiguously.

---

## 4. Panel UX

### 4.1 Default docking layout

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ Menu bar  File Edit GameObject Component Window Help        (h=28)          │
├─────────────────────────────────────────────────────────────────────────────┤
│ Toolbar  [W E R] │ ◀ Play ⏸ ⏭ │ Scene | Game │ snap/pivot/2D │  project ▾  │
│                                                              (h=32)         │
├───────────────┬─────────────────────────────────────────┬───────────────────┤
│ Hierarchy     │ Tabs: Scene | Game            (h=28)     │ Inspector         │
│ ☰ + search    │ ───────────────────────────────────────  │ 🔍 + Add Comp     │
│ w=22%         │ Scene toolbar                 (h=32)     │ w=24%             │
│ v-split 60%   │  gizmo mode · pivot · snap · zoom-fit    │                   │
│               │                                          │                   │
├───────────────┤            Scene / Game viewport         │                   │
│ Project       │                                          │                   │
│ breadcrumb +  │                                          │                   │
│ grid/list     │                                          │                   │
│ v-split 40%   │                                          │                   │
├───────────────┴─────────────────────────────────────────┴───────────────────┤
│ Console | Stats                                            (h=26% of rest)  │
├─────────────────────────────────────────────────────────────────────────────┤
│ Status bar  ● Play: Stopped · 60 FPS · Win x64 · main · undo: 12 steps (h=24)│
└─────────────────────────────────────────────────────────────────────────────┘
```

Dock split ratios (of the DockSpace): left `0.22`, right `0.24`, bottom `0.26`,
left vertical Hierarchy/Project `0.60 / 0.40`. Minimum sizes: left 240, right
280, center 320, bottom 120. Window min 1024×640 logical.

- Layout is savable/loadable (`Window > Layout > Default / Save / Load`), ships
  the default above, and auto-restores per project.
- Panels are dockable, tabbable, floatable, closable; re-open from `Window` menu
  and the command palette. Floating panels get `radius.lg` + `elev.2`.
- The Scene/Game tab strip sits above the viewport; Scene and Game share the
  central dock node.
- Toolbar groups (left→right): tool select (`Q W E R`), play cluster (Play/
  Pause/Step), view tabs, scene toggles (snap, pivot, 2D/ortho, zoom-fit), then
  right-aligned project name + build target.
- Status bar: play state, FPS, active target, VCS branch, undo depth.

### 4.2 Panel chrome

- Header: h 28, left type icon (14) + title (`type.sm` SemiBold); right hover
  actions: `ellipsis` (panel menu) and `x` (close). Double-click header = float.
- Focused panel: 2 px `accent.default` underline on the header; unfocused header
  title `text.secondary`. Only one panel is "focused" at a time.
- Panel body padding: `space.2` horizontal, `space.1` vertical.
- Empty and error states fill the body; never show a blank white/grey void.

### 4.3 Empty states

Standard block: centered, max-width 320, `space.5` vertical padding; icon 32
(`text.disabled`) · title `type.mdMedium` `text.secondary` · body `type.sm`
`text.muted` · optional primary button (`accent.default` fill, `text.onAccent`)
and secondary text button.

| Panel | Title | Body | CTA |
|---|---|---|---|
| Hierarchy | No GameObjects | Create your first object to start building the scene. | Create Empty · Create Sprite |
| Inspector | Nothing selected | Select a GameObject to inspect and edit its components. | — |
| Project | This folder is empty | Drag assets here or import them. | Import Assets |
| Console | No messages | Play or run the scene to see logs here. | Clear filters |
| Scene | Scene is empty | Add a GameObject or drop a prefab into the view. | Create Empty |
| Command palette (no match) | No commands match "x" | Try a different term or browse the Window menu. | — |
| Search (no results) | No results for "x" | Check spelling or clear the filter. | Clear |

### 4.4 Selection & edit affordances

- Selection is shared across Hierarchy, Scene, Project, Inspector. Selecting in
  one highlights everywhere. Primary selection = last clicked; drives Inspector.
- Scene: selected object gets a 2 px `accent.default` outline; multi-select shows
  a 1 px dashed `guide` bounding box; primary object has solid handles, others
  outlined.
- Gizmos: constant screen size (independent of zoom); move = axis arrows (`axis.x`
  / `axis.y`) + center square; rotate = ring (`gizmo.rotate`); scale = boxes
  (`gizmo.scale`). Hover thickens/brightens the handle; active drag shows a dim
  numeric readout near the cursor. Snap step shown while `Ctrl` held.
- Edits that change data push an undo command. Undo/redo (`Ctrl+Z`/`Ctrl+Y`)
  covers every edit including multi-select bulk edits, drag-drop, and Play-mode
  changes; the History panel lists named commands.
- Copy/paste (`Ctrl+C`/`Ctrl+V`) and duplicate (`Ctrl+D`); duplicate offsets by
  1 world unit and selects the copy.

### 4.5 Shortcuts

Global (spec §10.5 transcribed; conflicts resolved — see below):

| Action | Binding |
|---|---|
| Gizmo: move / rotate / scale | `W` / `E` / `R` |
| Gizmo: pan / hand (tool) | `Q` |
| Focus selection | `F` |
| Duplicate | `Ctrl+D` |
| Save project | `Ctrl+S` |
| Undo / Redo | `Ctrl+Z` / `Ctrl+Y` |
| Play / Pause / Step | `Ctrl+P` / `Ctrl+Alt+P` / `Ctrl+Alt+Shift+P` |
| Command palette | `Ctrl+Shift+P` |
| Debug overlay | `F1` |
| Toggle colliders | `Ctrl+F2` |
| New / Open / Save As | `Ctrl+N` / `Ctrl+O` / `Ctrl+Shift+S` |
| Close panel | `Ctrl+W` |
| Panel shortcuts (focus) | `Ctrl+1..6` Hierarchy, Inspector, Scene, Game, Project, Console |
| Select all (focused list) | `Ctrl+A` |
| Delete | `Delete` |
| Rename | `F2` |
| Multi-select | `Ctrl+click`, `Shift+click` (range) |
| Marquee select (Scene) | drag LMB on empty space |
| Zoom to fit (Scene) | `F` (no selection) |
| Toggle Play focus | `Ctrl+Tab` cycle panels |
| Reduce motion | Project Settings toggle (also honors OS) |

**Conflict resolutions (documented deviations from §10.5):**
1. §10.5 assigned `Ctrl+Shift+P` to *Step* **and** the command palette. Palette
   keeps `Ctrl+Shift+P` (developer standard, primary keyboard path); Step moves to
   `Ctrl+Alt+Shift+P`; Pause to `Ctrl+Alt+P`. Play stays `Ctrl+P`.
2. §10.5 assigned `F2` to *colliders*, but `F2` is the universal rename key in
   tree rows. Rename keeps `F2`; colliders move to `Ctrl+F2`.
3. These two deviations need G&TD sign-off (see open decision OD-1).

Bindings are user-configurable; conflicts are detected and flagged in the
Keybindings settings page. All bindings are reachable from the command palette.

---

## 5. Accessibility

### 5.1 Focus order & keyboard

- Tab order follows visual reading order: menu bar → toolbar → left column
  (Hierarchy, Project) → center (Scene/Game) → right (Inspector) → bottom
  (Console/Stats) → status bar. `Shift+Tab` reverses. Within a panel, order is
  the natural list/tree/field order.
- Composite widgets (tree, list, grid, tabs, segmented) use roving focus:
  arrows move within, Tab leaves the whole widget.
- Focus ring: 2 px `border.focus` + 1 px offset, never removed; rendered on
  keyboard interaction (including programmatic focus moves). Mouse clicks MAY use
  the same ring for consistency.
- Modals trap focus; Esc closes; on close, focus returns to the invoking control.
- Every editor action is reachable by keyboard (engine AC E8). Icon-only controls
  must be focusable and activate on Enter/Space.
- Provide `Alt` access to the menu bar and underlined mnemonics; `Ctrl+Shift+P`
  always available.

### 5.2 Tooltips

- Show after 400 ms hover delay, hide after 100 ms; second tooltip in a row shows
  immediately. Keyboard focus shows the tooltip with no delay.
- Spec: `type.sm`, max-width 280, padding `space.2`/`space.1`, `radius.md`,
  `elev.1`, `bg.elevated`, 1 px `border.default`, `text.primary`.
- Optional first line = title (`type.sm` SemiBold), optional last line = shortcut
  `KeyHint`. Auto-flip near window edges.
- Required on: every icon-only control, any truncated label, disabled control
  (must state the reason), gizmo handles, and status-bar metrics.

### 5.3 Color-blind-safe states

- Never encode meaning by hue alone. Every state pairs **color + icon + label**
  (and shape/position for lists):
  - success = `circle-check` + green + label
  - warning = `triangle-alert` + amber + label
  - error = `circle-x` + red + label
  - info = `info` + blue + label
  - selection = fill + 2 px bar + weight, not color alone
  - axis = color + literal `X`/`Y`/`Z` letter
- State-critical hues are chosen CVD-safe (vermillion/amber rather than
  pure red/green pairs where feasible); verify with deuteranopia, protanopia, and
  tritanopia simulation.
- Contrast: body text ≥ 4.5:1 (target 7:1 for `type.primary`), large text ≥ 3:1,
  UI/graphic boundaries ≥ 3:1. Disabled text is exempt but never below 3:1.
- Provide `theme.colorVision = default | highContrast`; `highContrast` raises
  border and muted-text contrast and removes subtle alpha fills.

### 5.4 DPI scaling

- `dpiScale` ∈ {1.0, 1.25, 1.5, 2.0}, read from GLFW content scale per monitor;
  re-evaluate on window move and on a settings override.
- Logical px = token value; physical px = `token × dpiScale`, rounded to integers.
  All `space.*`, `size.*`, `radius.*`, and font sizes scale.
- Font atlas rasterized at each scale (1x/1.5x/2x); pick the nearest and never
  upscale bitmaps. Icons rasterized at the target size.
- 1 px borders MUST remain ≥ 1 physical pixel; at fractional scales round
  positions (not sizes) to avoid blur.
- Line height and text baseline MUST land on integer physical pixels at every
  scale (test 1.25 and 1.5 explicitly — most failure modes live there).
- Minimum window 1024×640 logical (2048×1280 at 2x). Multi-monitor mixed scales
  re-layout without restarting.
- Verification matrix: 1x/1.5x/2x × dark/light × Windows/Linux; plus
  red-green color blindness simulation.

---

## 6. Acceptance criteria for the ImGui kit (STR-22)

- AC1. `Theme` exposes every token in `tokens.json` with the documented Java
  name; no hard-coded hex/style literal outside `Theme`.
- AC2. A demo window renders every widget in §3 at 1x/1.5x/2x with no missing
  glyphs and no default ImGui grey.
- AC3. Dark and light themes both meet the §5.3 contrast targets (measure, don't
  eyeball).
- AC4. Every widget implements the full state set in §1.7 (hover/active/selected/
  focused/drop-target), verified by screenshot capture.
- AC5. `Ctrl+Shift+P` opens the command palette; §4.5 shortcuts work; conflict
  resolutions in §4.5 are implemented.
- AC6. Empty states from §4.3 render in every panel.
- AC7. Reduce-motion setting disables all non-instant transitions.
- AC8. `./gradlew check` green; headless render test renders every widget.

## 7. Open decisions

- **OD-1 (G&TD):** Confirm the two shortcut deviations in §4.5 (`Ctrl+Shift+P`
  palette over Step; `F2` rename over colliders). Recommended: as specified.
- **OD-2 (Rendering coder):** Confirm Lucide as the icon set and the atlas
  registration approach (lucide font subset vs. generated atlas). Recommended:
  font subset, subsetted to §1.6.
- **OD-3 (Lead Engineer):** Confirm `tokens.json` ships as a module resource read
  by `engine-imgui` (vs. generated Java constants). Recommended: resource +
  generated constants for compile-time safety.

---

## Appendix A — `tokens.json` (source of truth)

```json
{
  "$schema": "sds/1.0",
  "meta": { "name": "StratixOps Design System", "version": "1.0.0", "phase": 1 },
  "color": {
    "dark": {
      "bg.canvas": "#0B0E14", "bg.panel": "#11151D", "bg.toolbar": "#161B24",
      "bg.elevated": "#1B212C", "bg.input": "#1B212C", "bg.overlay": "rgba(8,10,14,0.72)",
      "surface.hover": "rgba(255,255,255,0.05)", "surface.active": "rgba(255,255,255,0.09)",
      "surface.selected": "rgba(20,200,168,0.18)",
      "border.subtle": "#1E2530", "border.default": "#2A3341", "border.strong": "#46536A",
      "border.focus": "#6FB7FF",
      "text.primary": "#EFF2F5", "text.secondary": "#C2C9D2", "text.muted": "#9AA4B2",
      "text.disabled": "#6B7688", "text.onAccent": "#04110E", "text.inverse": "#0B0E14",
      "accent.default": "#14C8A8", "accent.hover": "#2FD8BC", "accent.active": "#0EA88D",
      "accent.subtle": "rgba(20,200,168,0.16)", "accent.text": "#38E1C4",
      "state.success": "#3FB950", "state.success.subtle": "rgba(63,185,80,0.16)",
      "state.warn": "#D29922", "state.warn.subtle": "rgba(210,153,34,0.16)",
      "state.error": "#F85149", "state.error.subtle": "rgba(248,81,73,0.16)",
      "state.info": "#58A6FF", "state.info.subtle": "rgba(88,166,255,0.16)",
      "axis.x": "#F85149", "axis.y": "#3FB950", "axis.z": "#58A6FF",
      "gizmo.move": "#FFD166", "gizmo.rotate": "#4DD4E8", "gizmo.scale": "#C792EA",
      "grid.line": "#151B24", "grid.line.major": "#1E2530", "guide": "#7C5CFF",
      "placeholder.missing": "#FF00FF", "prefab.override": "#58A6FF", "linking": "#7C5CFF"
    },
    "light": {
      "bg.canvas": "#F7F9FB", "bg.panel": "#FFFFFF", "bg.toolbar": "#F0F3F7",
      "bg.elevated": "#FFFFFF", "bg.input": "#FFFFFF", "bg.overlay": "rgba(17,21,29,0.32)",
      "surface.hover": "rgba(11,14,20,0.045)", "surface.active": "rgba(11,14,20,0.075)",
      "surface.selected": "rgba(14,168,141,0.15)",
      "border.subtle": "#E8ECF1", "border.default": "#D5DCE4", "border.strong": "#AFBAC7",
      "border.focus": "#1E6FD9",
      "text.primary": "#11151D", "text.secondary": "#333D4D", "text.muted": "#6B7688",
      "text.disabled": "#9AA4B2", "text.onAccent": "#FFFFFF", "text.inverse": "#EFF2F5",
      "accent.default": "#0EA88D", "accent.hover": "#12BC9E", "accent.active": "#0B8E77",
      "accent.subtle": "rgba(14,168,141,0.14)", "accent.text": "#0B7A66",
      "state.success": "#1A7F37", "state.success.subtle": "rgba(26,127,55,0.12)",
      "state.warn": "#9A6700", "state.warn.subtle": "rgba(154,103,0,0.12)",
      "state.error": "#CF222E", "state.error.subtle": "rgba(207,34,46,0.12)",
      "state.info": "#0969DA", "state.info.subtle": "rgba(9,105,218,0.12)",
      "axis.x": "#CF222E", "axis.y": "#1A7F37", "axis.z": "#0969DA",
      "gizmo.move": "#C99A1F", "gizmo.rotate": "#1E9DAF", "gizmo.scale": "#8B5BB5",
      "grid.line": "#EEF1F5", "grid.line.major": "#E2E7EE", "guide": "#6547E6",
      "placeholder.missing": "#FF00FF", "prefab.override": "#0969DA", "linking": "#6547E6"
    }
  },
  "typography": {
    "family.ui": "Inter", "family.mono": "JetBrains Mono",
    "type.xs": { "size": 11, "lineHeight": 16, "weight": 400, "tracking": 0.2 },
    "type.sm": { "size": 12, "lineHeight": 18, "weight": 400, "tracking": 0.1 },
    "type.md": { "size": 13, "lineHeight": 20, "weight": 400, "tracking": 0 },
    "type.mdMedium": { "size": 13, "lineHeight": 20, "weight": 500, "tracking": 0 },
    "type.lg": { "size": 15, "lineHeight": 22, "weight": 600, "tracking": 0 },
    "type.xl": { "size": 18, "lineHeight": 26, "weight": 600, "tracking": -0.1 },
    "type.2xl": { "size": 24, "lineHeight": 32, "weight": 600, "tracking": -0.2 },
    "type.mono": { "size": 12, "lineHeight": 18, "weight": 400 },
    "type.monoSm": { "size": 11, "lineHeight": 16, "weight": 400 }
  },
  "space": { "space.0": 0, "space.1": 4, "space.2": 8, "space.3": 12, "space.4": 16, "space.5": 24, "space.6": 32, "space.7": 48, "space.8": 64 },
  "size": {
    "size.menuBar.h": 28, "size.toolbar.h": 32, "size.tab.h": 28, "size.panel.header.h": 28,
    "size.statusBar.h": 24, "size.breadcrumb.h": 24, "size.row.h": 22, "size.chip.h": 20,
    "size.control.sm": 20, "size.control.md": 24, "size.control.lg": 28, "size.control.xl": 32,
    "size.icon.sm": 14, "size.icon.md": 16, "size.icon.lg": 20, "size.indent.perLevel": 14,
    "size.scrollbar.w": 10, "size.colorSwatch": 20, "size.thumb.sm": 48, "size.thumb.md": 72,
    "size.thumb.lg": 96, "size.palette.w": 560, "size.minWindow.w": 1024, "size.minWindow.h": 640
  },
  "radius": { "radius.xs": 2, "radius.sm": 4, "radius.md": 6, "radius.lg": 8, "radius.pill": 999 },
  "elev": {
    "elev.0": { "shadow": "none" },
    "elev.1": { "dark": "0 2px 8px rgba(0,0,0,0.35)", "light": "0 2px 8px rgba(16,24,40,0.12)" },
    "elev.2": { "dark": "0 8px 24px rgba(0,0,0,0.45)", "light": "0 8px 24px rgba(16,24,40,0.18)" },
    "elev.3": { "dark": "0 12px 32px rgba(0,0,0,0.50)", "light": "0 12px 32px rgba(16,24,40,0.20)", "border": "accent.default" }
  },
  "motion": {
    "motion.instant": 0, "motion.fast": 120, "motion.base": 160, "motion.slow": 240,
    "easing.out": [0.2, 0, 0, 1], "easing.inOut": [0.4, 0, 0.2, 1]
  }
}
```

## Appendix B — Icon names (Lucide)

See §1.6. The full ordered list for atlas registration is the "Icon" and
"Action/type" columns of that table (deduplicated). Stroke 1.5, grid 24,
rasterized at 14/16/20 px.
