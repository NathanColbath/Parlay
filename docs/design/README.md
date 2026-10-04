# StratixOps Design Docs

Owner: Game & Technical Designer (*) with UX & Product Designer.
These are the gameplay/DX-facing specs for the engine MVP and the sample game.

| Doc | Contents | Primary consumers |
|---|---|---|
| [00-sample-game-design.md](00-sample-game-design.md) | Gem Dash: mechanics, tuning, levels, art direction, feel | Coders, QA, UX |
| [01-engine-features.md](01-engine-features.md) | Required engine features (P0/P1/P2) + acceptance criteria | Lead Engineer, coders, QA |
| [02-api-ergonomics.md](02-api-ergonomics.md) | Target developer API, ergonomics feedback, anti-goals | Lead Engineer, coders, UX |
| [03-onboarding-flow.md](03-onboarding-flow.md) | Getting-started flow + acceptance criteria | DX & Build Engineer, UX |
| [04-tooling-editor-ux.md](04-tooling-editor-ux.md) | Level format, debug overlay, hot-reload, editor roadmap | Coders, UX, DX |
| [05-ux-review.md](05-ux-review.md) | UX review + findings on API ergonomics, onboarding, screens | Lead Engineer, G&TD, DX, coders |

## Status

Draft for the MVP. The API contract (STR-13) is frozen; see
`docs/design/02-api-ergonomics.md` and
`docs/architecture/ADR-0001-api-contract.md`. The engine spec (STR-2) documents
module layout against that contract. Remaining scope questions are listed in
each doc.

## Key decisions (resolved in ADR-0001)

1. Logical viewport: **640x360**, integer-scaled and letterboxed into the
   physical window (default 1280x720).
2. `Vec2`/tiny math helpers ship in the engine; **collision stays sample-side**.
3. Package roots: **`io.stratixops.engine`** / **`io.stratixops.gemdash`**
   (group `io.stratixops`, root project `stratix-engine`). `com.stratixops.*`
   is dropped.
4. JDK: **17 (LTS)**.
5. Gamepad is best-effort P1; keyboard-first if it threatens the milestone.
