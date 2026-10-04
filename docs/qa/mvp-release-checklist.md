# MVP Release Checklist — StratixOps Engine

Owner: QA Engineer. Every box must be checked before the MVP is declared
released. Each item is executable; commands assume the repo root.

## 0. Scope confirmation

- [ ] MVP scope matches the brief: sprites, input, sound, camera, one playable
      sample, and getting-started docs. No scope creep without Chief of staff
      approval.

## 1. Source is clean and reproducible

- [ ] `git clean -xdf` then `git status --porcelain` is empty.
- [ ] Fresh clone builds: `./gradlew clean check` → `BUILD SUCCESSFUL`.

## 2. Quality gate

- [ ] CI is green on the release commit (`Build & Test` on Ubuntu + Windows).
- [ ] `./gradlew check` passes; coverage ≥ floor for every module with main
      sources (`./gradlew printQualityGate` shows the active gate).
- [ ] `./gradlew test -Pstratix.qa.gpuTests=true` passes on a machine with a
      display (or the xvfb CI job is green).
- [ ] No unexplained `@Disabled` tests: `./gradlew test --rerun-tasks` and
      inspect skips.
- [ ] No open **Blocker/Critical** bugs against MVP scope.

## 3. Core runtime verification (STR-5)

- [ ] Game loop runs at a stable frame rate for ≥ 60s of the sample.
- [ ] Input: keyboard/mouse press, release, and repeat behave per spec.
- [ ] Assets: missing/corrupt asset is handled with a clear error, no crash.
- [ ] Audio: sound plays, volume/mute works, no device => degrades gracefully.
- [ ] Unit tests for the above exist and pass.

## 4. Rendering verification (STR-6)

- [ ] Window opens at the requested size; resizes without artifacts.
- [ ] Camera pans/zooms correctly (visual check against reference screenshots).
- [ ] Sprites render with correct order and no bleeding between atlas frames.
- [ ] Animation plays at the expected frame timing.
- [ ] Frame-rate note recorded; sample meets the agreed target on the test
      machine class.

## 5. Sample game (STR-3)

- [ ] Playable end-to-end: start → play a level/round → end/restart.
- [ ] Runs from a clean clone following the getting-started guide only.

## 6. Packaging & docs (STR-7)

- [ ] Distribution artifact produced from a clean checkout and launched by a
      second person on a clean machine.
- [ ] Getting-started guide is accurate (zero → running) with no missing steps.
- [ ] `README`/docs links resolve.

## 7. Sign-off

- [ ] QA summary posted on STR-4 with command output and artifact links.
- [ ] Lead Engineer confirms the quality gate.
- [ ] Chief of staff signs off on release scope.

## Sign-off record

| Field        | Value |
| ------------ | ----- |
| Release      |       |
| Commit       |       |
| Date         |       |
| QA           |       |
| Lead Eng     |       |
