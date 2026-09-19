# AI feature blitz roadmap

Branch: `ai-feature-blitz`, forked from `forsaken-kingdom-mdx-1800` (Warcraft III 3.0.0 MDX 1800 support and
the CASC non-UTF-8 path fix). This document is the working plan for a series of AI-assisted changes to Retera
Model Studio. Each item is meant to be one focused session and one reviewable commit or small commit series.
Read `CLAUDE.md` first; it explains the architecture and the invariants every item below must respect.

Status legend: `[ ]` not started, `[~]` in progress, `[x]` done (commit hash), `[-]` dropped (say why).

## Working agreement

- One item per commit series. Commit messages say what changed and why, and carry the
  `Co-Authored-By: Claude ...` trailer. Do not mix a cleanup with a feature in one commit.
- Verify before claiming done. For anything touching `wc3/mdl` or `wc3/mdx`, round-trip real models
  (`-convert` CLI or the harness from item F1) and diff the bytes or the MDL text. For GUI features, launch
  `./gradlew :matrixeater:run` and exercise the path once.
- Preserve file line endings and tab indentation. Keep `docs/mdx-1800-forsaken-kingdom.md` current when the
  1800 handling changes.
- Every edit the user can make must be an `UndoAction` that fires `ModelStructureChangeListener` in both
  directions. No feature ships without undo.
- Prefer the `modeledit/newstuff` and `actions/newsys` generation of editor code. Do not extend the dead
  packages listed at the end of `CLAUDE.md`.
- Behaviour that users rely on (odd as it is) stays unless an item explicitly changes it. When in doubt, add a
  preference rather than change a default.
- Update this file when an item finishes: flip the status, note the commit, add anything learned that the next
  item needs.

## Phase F: foundations (do these first)

These make every later item cheaper and safer.

- [ ] **F1. Round-trip regression harness.** A small headless Gradle task or `main` that reads every `.mdx` in a
  configured folder (a 3.0.0 install via CASC, or an extracted folder), writes it back through `EditableModel`,
  and reports byte-level and MDL-text differences, plus load exceptions. Runs on classic, Reforged and 1800
  data. This is the safety net for everything in Phase D and most of Phase E.
- [ ] **F2. Headless-clean data layer.** Remove the 33 `JOptionPane` uses from `wc3/mdl` and `wc3/mdx` by
  routing through a `ModelLoadWarnings` collector (or exceptions with context) that the GUI turns into dialogs and
  the CLI prints. Unblocks F1, the CLI, and batch tools.
- [ ] **F3. One place for the data-source cache drop.** Factor the seven-call litany duplicated in
  `MainPanel.dataSourcesChanged` and `MainFrame.main` into a single method, and make it also clear
  `BLPHandler.gpuBufferCache`, which currently survives a data-source swap and shows stale textures.
- [ ] **F4. Small known bugs.** `FolderDataSource.read(String)` reads the relative path from the working
  directory instead of resolving against the folder. `MpqCodebase` swallows IO errors with `printStackTrace` and
  returns null. Version string is duplicated in `build.gradle` and `MainFrame`. Bundle these as one fix commit.
- [ ] **F5. Linux and macOS parity pass.** Case-insensitive lookup in `FolderDataSource`, no `reg query` on
  non-Windows, profile path handling without the backslash rewrite, and a check that `runtime` images launch on
  Linux. The 3.0.0 work is already being done from Linux, so this is real usage.

## Phase D: model data and formats

- [ ] **D1. 1800 unknowns follow-up.** Confirm or swap the `BackFacesForShadows` / `AmbientOcclusion` shading bit
  mapping once documented, and add `Sounds`/`SoundEmitter` and `ComponentSkin` support if any real model appears
  with them. Track in `docs/mdx-1800-forsaken-kingdom.md`.
- [ ] **D2. Version up/down conversion that is honest.** Today "Assign FormatVersion N" just sets the number.
  Make it a real conversion: drop or warn about fields the target cannot hold (DOF tracks, 16-bit skin indices
  over 255, extended light fields, Glider), and offer the 1800 -> 1000 -> 800 chain in one dialog.
- [ ] **D3. glTF 2.0 export (static and skinned, with animations as clips).** The most requested interchange path
  for Blender users. Textures exported as PNG alongside. Start with SD models; HD after.
- [ ] **D4. glTF / FBX-via-glTF import** into a new model or as an imported geoset, reusing the OBJ import
  settings UI where it fits.
- [ ] **D5. Model validator.** A "Check model" report: unreferenced bones, geosets with no matrix, keyframes
  outside any sequence, HD vertices whose weights do not sum, duplicate sequence names, textures missing from the
  current data sources, extents that do not match geometry. Each finding with a one-click fix where safe.
- [ ] **D6. Keyframe simplifier v2.** The existing "Simplify Keyframes (Experimental)" is per-track and lossy.
  Implement tolerance-based simplification with a preview of the maximum deviation per track.

## Phase E: editor features ("I wish I had added this")

Candidates from the codebase and from what users of the Hive thread and Discord usually ask for. The maintainer's
own wishlist goes at the top of this section; the rest are proposals, reorder freely.

### Maintainer wishlist

- [ ] (fill in)

### Modelling

- [ ] **E1. Undoable material and layer editor rewrite.** The current texture and material dialogs mutate the
  model directly in places. Make every field an `UndoAction` and support HD layer slots explicitly.
- [ ] **E2. Weight painting for HD skin weights** in the perspective viewport, with a per-bone influence
  overlay and normalisation.
- [ ] **E3. Loop cut / edge split / bridge** in the vertex and face editors.
- [ ] **E4. Snap to grid and snap to vertex** with a visible grid size in the status bar.
- [ ] **E5. Better mirror.** Mirror geometry across a plane with automatic bone renaming (`_L` <-> `_R`) and
  animation mirroring.

### Animation

- [ ] **E6. Graph editor for keyframes.** A curve view per track next to `TimeSliderPanel`, with tangent handles
  for Hermite and Bezier keys.
- [ ] **E7. Animation retargeting.** Transfer animations between models with different bone names via a mapping
  dialog, replacing the Oinkerwinkle-style transfer for the common case.
- [ ] **E8. Sequence manager.** A table for add, rename, reorder, set interval, `NonLooping`, `Rarity`,
  `MoveSpeed`, with drag-to-retime and gap detection.
- [ ] **E9. Onion skinning** in the 2D viewports and optional ghost in 3D.

### Viewing and browsing

- [ ] **E10. Search everywhere.** One search box over the unit browser, doodad browser, and the CASC/MPQ file
  tree with fuzzy matching. `enumerateFiles` output is already available; index it once per data-source change.
- [ ] **E11. Thumbnail grid in the model browser** rendered offscreen with `AnimatedPerspectiveViewport`, cached
  on disk under the profile directory.
- [ ] **E12. Reference image and grid planes** in the perspective view.
- [ ] **E13. Screenshot and turntable GIF export** from the animation preview.

### Workflow

- [ ] **E14. Autosave and crash recovery** (timed `.mdx` snapshots under the profile directory; offer to restore
  on next start).
- [ ] **E15. Batch tools dialog** backed by F1/F2: convert a folder, assign version, flush unused textures,
  recalculate extents, with a log panel. Replace the `hacks/` package as the place one-off jobs live.
- [ ] **E16. Scripting API.** Turn the Nashorn console into something usable: a documented `rms` object with
  stable model, selection and undo helpers, a snippets folder, and run-on-open scripts.
- [ ] **E17. Preferences as JSON.** Migrate `SaveProfile` and `ProgramPreferences` off Java serialization with a
  one-time import of the old `user.profile`. Removes the boxed-field dance and makes settings diffable.

## Phase R: rendering

- [ ] **R1. HD shading closer to the game.** ORM and emissive handling, team colour in HD, environment light
  presets, and the 1800 light falloff fields (`QuadraticFalloff`, `LinearFalloff`, `Damping`) driving the preview.
- [ ] **R2. Shaders as resources.** Move the inline GLSL strings out of `NGGLDP` into resource files with a hot
  reload action, so R1 iterations do not need a rebuild.
- [ ] **R3. Wireframe-over-shaded, backface and normals display toggles** in the perspective view.
- [ ] **R4. Modernise the GL path** away from `glBegin` emulation towards buffered geometry per geoset. Big and
  risky; only after R2 and with F1 as a smoke test for load paths.

## Phase C: code health

Do these opportunistically when an item above touches the area, or as dedicated sessions once Phase F is done.

- [ ] **C1. Split `MainPanel`** (7084 lines) by extracting menu construction, the `actionPerformed` dispatch,
  and the docking layout into separate classes. Mechanical, but it is the file every feature touches.
- [ ] **C2. Delete dead code** listed in `CLAUDE.md` (`hacks/`, `matrixeaterhayate` except `TextureManager`,
  `ysera`, `colorizer`, `blpconv`, `stuff`, oobjloader demos, standalone `craft3editor` frames). One commit per
  package so any of them can be reverted.
- [ ] **C3. Vendored copies.** `com/etheller/collections/TreeMap` and `com/hiveworkshop/json` are vendored
  copies of standard code. Replace with the JDK `TreeMap` and the `org.json` artifact if nothing depends on the
  local modifications.
- [ ] **C4. Logging.** Replace the 349 `printStackTrace` calls with `java.util.logging` and a log file under the
  profile directory, surfaced in the About or Help menu.
- [ ] **C5. Unit tests for pure code.** Once F1 and F2 exist, add JUnit to `craft3data` for `AnimFlag`
  interpolation, `ModelUtils` version predicates, MDL tokenizer edge cases, and CASC path decoding.
- [ ] **C6. TODO triage.** 314 `TODO`/`FIXME`/`HACK` markers. Convert the real ones into items here and delete
  the stale ones.

## Suggested order

F1, F2, F3, F4 (one or two sessions). Then interleave: one Phase E or D item, one Phase C item, so cleanup keeps
pace with features. R2 before any other rendering work. C1 early enough that later E items land in small classes
instead of adding to `MainPanel`.

## Log

- 2026-09-18: branch created; `CLAUDE.md` and this roadmap added on top of the 3.0.0 and CASC fixes.
