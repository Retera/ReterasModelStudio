# AI feature blitz roadmap

Branch: `ai-feature-blitz`, forked from `forsaken-kingdom-mdx-1800` (Warcraft III 3.0.0 MDX 1800 support and
the CASC non-UTF-8 path fix). This document is the working plan for a series of AI-assisted changes to Retera
Model Studio. Each item is meant to be one focused session and one reviewable commit or small commit series.
Read `CLAUDE.md` first; it explains the architecture and the invariants every item below must respect.

Status legend: `[ ]` not started, `[~]` in progress, `[x]` done (commit hash), `[-]` dropped (say why).

## Guiding principle

This is not a fork. Everything the maintainer's workflow relies on today keeps working, in particular:

- **File > Import** and its ability to build combined models with partially or fully linked skeletons, with one
  skeleton's keyframes copied onto another, or hybrids of both. Any data-model change is validated against that
  dialog before it lands.
- **Save-time automation** (`EditableModel.doSavePreps` -> `rebuildLists`, `updateObjectIds`, empty-geoset
  removal) is what makes Import's recycled components resolve. It stays the default. Where a change makes it
  optional, the default remains "on".
- **Data-source switching** happens constantly. Every cache is dropped in one place (see F3) and this is tested
  by actually switching SD/HD/DE sources after each feature that touches textures or game data.

Where the twilac fork (https://github.com/tw1lac/ReterasModelStudio) solved the same problem, borrow the idea,
not the code. Appendix A lists what is worth reading there and what to avoid.

## Working agreement

- One item per commit series. Commit messages say what changed and why, and carry the
  `Co-Authored-By: Claude ...` trailer. Do not mix a cleanup with a feature in one commit.
- Verify before claiming done. For anything touching `wc3/mdl` or `wc3/mdx`, round-trip real models
  (`-convert` CLI or the harness from F1) and diff the bytes or the MDL text. For GUI features, launch
  `./gradlew :matrixeater:run` and exercise the path once, including undo and redo.
- Preserve file line endings and tab indentation. Keep `docs/mdx-1800-forsaken-kingdom.md` current when the
  1800 handling changes.
- Every edit the user can make is an `UndoAction` that fires `ModelStructureChangeListener` in both
  directions. No feature ships without undo.
- Prefer the `modeledit/newstuff` and `actions/newsys` generation of editor code. Do not extend the dead
  packages listed at the end of `CLAUDE.md`.
- Behaviour that users rely on stays unless an item explicitly changes it. When in doubt, add a preference
  rather than change a default.
- Update this file when an item finishes: flip the status, note the commit, add anything learned.

## Phase F: foundations (do these first)

- [x] **F1. Round-trip regression harness.** (`RoundTripCheck`, `./gradlew :matrixeater:roundTrip`) A headless Gradle task or `main` that reads every `.mdx` in a
  configured folder (a 3.0.0 install via CASC, or an extracted folder), writes it back through `EditableModel`,
  and reports byte-level and MDL-text differences plus load exceptions, across classic, Reforged and 1800 data.
  This is the safety net for Phase M and Phase D.
- [ ] **F2. Headless-clean data layer.** Remove the 33 `JOptionPane` uses from `wc3/mdl` and `wc3/mdx` by
  routing through a warnings collector that the GUI turns into dialogs and the CLI prints.
- [x] **F3. One place for the data-source cache drop.** (d06df33) Factor the seven-call litany duplicated in
  `MainPanel.dataSourcesChanged` and `MainFrame.main` into one method, and make it also clear
  `BLPHandler.gpuBufferCache`, which currently survives a data-source swap. (The fork got this wrong by tying
  texture cache lifetime to a size limit instead of the data source; do not copy that.)
- [x] **F4. Small known bugs.** (6afc84d) `FolderDataSource.read(String)` reads the relative path from the working
  directory. `MpqCodebase` swallows IO errors and returns null. Version string duplicated in `build.gradle` and
  `MainFrame`. One fix commit.
- [~] **F5. Linux and macOS parity.** Case-insensitive MPQ lookup done in `WarcraftInstallDetector`; registry and profile path still open. Case-insensitive `FolderDataSource` lookup, no `reg query` off Windows,
  profile path without the backslash rewrite, confirm `runtime` images launch on Linux.
- [x] **F6. Focus and hotkey contract for docked views.** (bbab0aa, `EditingHotkeys`) Today the global DEL binding on the root pane is a
  no-op outside animation mode (`MainPanel.deleteHotkeyAction`, "NOTE delete was here"), and each view that
  wants DEL (`Viewport.setupCopyPaste`, `TracksEditorPanel`, `TimeSliderPanel`) makes itself focusable,
  requests focus on click, and shadows the root ActionMap with its own "Delete", "Cut", "Copy", "Paste" entries.
  The Model tab and Outliner trees are `setFocusable(false)` so they can never receive DEL. Write this pattern
  down as a small helper (`EditingHotkeys.install(JComponent, handlers)`) so W1 and every later view use the same
  mechanism, and extend `MainPanel.focusedComponentNeedsTyping` to cover tree cell editors.

## Phase W: maintainer wishlist

The order below is the suggested build order, not priority. W1 through W4 share one tree infrastructure and
should be built together in the Model tab; W5 and W6 fill in the missing editors; W7 onward are independent.

### Model tab

Current state, verified 2026-09-18: `ModelComponentBrowserTree` rebuilds its whole `DefaultTreeModel` on every
structure event and restores selection by item identity. `ComponentsPanel` registers only seven cards (blank,
header, comment, animation, global sequence, bitmap, material). `selected(...)` for TextureAnim, Geoset,
GeosetAnim, every node type, Camera, FaceEffect and BindPose is an **empty method**, and the card is not even
switched, so the previous editor stays on screen when a bone is clicked. In the material card, the seven layer
flag checkboxes have no listeners, "Add Layer" and per-layer "Delete" have no listeners, the interpolation
combo does nothing, and the dynamic keyframe table is read-only. The tree has no popup, no key bindings, no
drag, no clipboard.

- [x] **W1. Model tab: delete, cut, copy, paste, and a right-click menu on every item.** (38f0c13) Make the tree focusable
  per F6. Popup on every item: Cut, Copy, Paste, Delete, and New with a submenu listing every component type
  (Sequence, Global Sequence, Texture, Material, Texture Anim, Geoset, Geoset Anim, Bone, Helper, Light,
  Attachment, Particle Emitter, Particle Emitter 2, Popcorn, Ribbon, Event Object, Collision Shape, Camera).
  Delete on a node reparents its children to the deleted node's parent (offer "delete subtree" as a second
  item). Copy produces a deep clone with fresh identity; because ids are recomputed on save, a pasted node only
  needs new object identity and a unique name suffix, and a pasted geoset needs its matrices re-pointed at the
  existing bones. Every operation is one `UndoAction` firing `nodesAdded/Removed`, `geosetsAdded/Removed`,
  `texturesChanged`, etc. Clipboard is in-process (a static holder of cloned components) with a text fallback of
  the MDL fragment so it can cross into a second open model.
- [x] **W2. Model tab: drag to reparent in the Nodes section.** (38f0c13) `setDragEnabled(true)` plus a `TransferHandler`
  on the tree, limited to IdObject rows. Dropping onto a node sets the parent; dropping onto the "Nodes" group
  clears it. Reject drops that would create a cycle. Backed by one `SetParentAction` (does not exist yet; the
  fork's `ParentChangeAction` is the reference). Because the tree rebuilds on every structure event, keep the
  drop-target row identity, not the `TreePath`, across the rebuild.
- [x] **W3. Model tab: "Move Left" and "Move Right" on nodes.** (38f0c13) Right-click items. Move Left makes the node a
  child of its grandparent (placed after its former parent); Move Right makes it a child of the sibling above
  it. Both are thin wrappers over the W2 action. Low priority, drop if the tree rebuild makes ordering unreliable.
- [x] **W4. Model tab: "Open in Editor" and "Open in Tracks".** (38f0c13; the Outliner scrolls to and highlights the item since it does not support row selection) Right-click items that switch to the other
  InfoNode `View` (activate its tab if docked in the same `TabWindow`, via `View.restoreFocus()` /
  `DockingUtil`), then select and expand the corresponding item. For Tracks this means selecting the matching
  row in `ModelComponentAnimFlagTree` and scrolling `TracksEditorTimelinePanel` to it. Needs a small "select
  this component" API on `TracksEditorPanel` and on the Outliner; the Outliner already has `ModelViewManager`
  highlight support to reuse.
- [ ] **W5. Model tab: finish the editors for every component type.** One card per type, all edits as undo
  actions. Nodes share a base panel (name, parent chooser, pivot, billboard and inherit flags, per-type fields);
  Geoset (material chooser, selection group, LoD, extents, vertex and face counts, UV layer count); GeosetAnim
  (static alpha and color, or jump to Tracks); TextureAnim; Camera (position, target, field of view, near, far,
  1800 depth-of-field static values); FaceEffect; Sequences overview table (name, interval, tags) for W8's
  sequence manager. Fix the material card: layer flags wired, Add and Delete layer wired, layer reorder.
  Reference: the fork's `ComponentIdObjectPanel` hierarchy for field inventory; do not copy its layout.
- [ ] **W6. Static/dynamic split done right.** For Alpha, Color, TextureID, Emissive, Fresnel and similar
  properties the Model tab edits only the **static** value. If the property is animated, the card shows the
  interpolation type read-only, a per-sequence summary ("Birth: 3 keys, Stand: 1 key"), and two buttons:
  "Open in Tracks" (W4) and "Make Static" (removes the track, undoable, seeded with the value at time 0).
  "Make Dynamic" creates a one-key track and opens Tracks. The read-only keyframe `JTable` from the early draft
  goes away. Tracks (W7) is where dynamic values are understood, with the colored gradient bars between keys
  labelled by sequence name.

### Tracks tab

Current state: users can view keyframes as pills, rubber-band select, slide, and delete. No value editing, no
insertion, no copy or paste, no right-click. Rotation keys have no value rendering. `TracksEditorPanel` has its
own time scale and no connection to the shared `TimeEnvironmentImpl` or `TimeSliderPanel`, so the two selections
and the two playheads are independent.

- [x] **W7. Tracks: create and edit keyframe data.** (inspector panel with type-aware editors incl. Euler degrees and texture picker; right-click insert/copy/cut/paste/duplicate/delete/select-all/interpolation/global sequence; double-click edits; ruler in the column header drives the shared playhead; rotation keys draw an angle needle) (a) A side panel, open by default in the Tracks view, that
  shows the selected key's time, value and tangents in a type-aware editor: float spinner for alpha, color
  swatch plus RGB fields for color, texture chooser with thumbnail for TextureID, Euler-degrees plus raw
  quaternion for rotation, XYZ for translation and scaling. Edits go through `SetKeyframeAction`. (b) Right-click
  on a track row at a time: Insert Key Here (value interpolated from neighbours, or the static value if the track
  is empty), Copy, Paste, Duplicate, Delete, Set Interpolation for the track (`Linear`, `Hermite`, `Bezier`,
  `DontInterp`, with tangent generation on the way up), Convert to Global Sequence. (c) Double-click a key to
  focus the side panel. (d) Share the playhead: subscribe `TracksEditorPanel` to `TimeEnvironmentImpl`, and
  clicking on the ruler sets the shared time so the 3D preview follows. (e) Render rotation keys with a small
  axis glyph or at least the angle magnitude, so a track full of green pills becomes readable. The fork's
  `TimeLinePopup` with per-node submenus and its interpolate-on-paste behaviour are the reference for (b).

### Add menu and wizards

Current state: Add has exactly two submenus. Add > Particle mutates the model directly with no undo, and is
populated from `stock/particles/*.mdx`. Add > Animation has Rising/Falling Birth/Death (not undoable) and
Single from File/Unit/Model/Object.

- [ ] **W8. Technical "New" lives in the Model tab; friendly wizards live in Add.** New from W1 creates a bare,
  correctly defaulted component with no dialog. Add becomes a menu of wizards, each undoable: Attachment Point
  (pick an existing bone as parent, optional offset, standard name list such as `Overhead Ref`, `Weapon Ref`,
  `Hand Left Ref`); Particle (existing dialog, made undoable); Collision Shape from selection bounds; Camera from
  current perspective view; Event Object with the event browser; Sequence with duration, non-looping, rarity and
  a checkbox to copy keyframes from an existing sequence; Global Sequence. Add > Animation Single keeps working.

### Edit window

- [x] **W9. Primitives that actually draw.** (Sphere, Geosphere, Cylinder, Cone, Torus under Standard; Capsule, Tube under Extended; live segments/rings/inner-ratio options; Animation Nodes card reachable from the combo) `CreatorModelingPanel` offers Mesh Basics, Standard Primitives,
  Extended Primitives, Animation Nodes, but only Plane and Box exist, the options panel is an empty 16-row grid,
  Extended Primitives has no card, and Animation Nodes is unreachable from the combo because of a name mismatch.
  Implement Sphere, Cylinder, Cone, Torus, and Geosphere under Standard Primitives; Capsule and Tube under
  Extended; options panel with segment counts, radius and height, applied live to the drag preview. Each is a
  `Draw*Activity` producing one `Draw*Action` like the existing Box path, and each new geoset gets the default
  material and a `VertexGroup` on the selected bone or a new bone at its centre.
- [ ] **W10. One 3D-accelerated viewport with Front/Side/Top/Bottom/Perspective as presets.** Today the three
  ortho views are `Graphics2D` `Viewport`s that project two of three axes by byte index, and `PerspectiveViewport`
  is a GL canvas with no `ModelEditor`, no activity manager and no `CoordinateSystem`, so editing in 3D is
  literally absent. The activity and manipulator pipeline is `Graphics2D`-bound (`ViewportActivity.render`,
  `Graphics2DToModelElementRendererAdapter`). Plan in stages, each shippable:
  1. Introduce a camera-based `CoordinateSystem` (world to screen and screen ray to world via matrices) and make
     the ortho `Viewport` use it instead of axis bytes. Add a **Top** preset, because Bottom is what everyone
     expects Top to be. Presets in the title bar's right-click menu of each docked view.
  2. Make `ActiveViewportWatcher` and `ViewportListener` accept an interface instead of the concrete `Viewport`.
  3. Add a GL-backed implementation of the same interface (`GLViewport extends BetterAWTGLCanvas`) that owns a
     `ModelEditorViewportActivityManager`, forwards mouse events with its `CoordinateSystem`, and draws the
     manipulator overlays through a small vector-drawing interface that has both a `Graphics2D` and a GL
     implementation. Per-view toggles: orthographic or perspective, axis lock, grid planes, textured or
     wireframe, show nodes.
  4. Retire the `Graphics2D` viewports once selection, move, rotate, scale, extrude, and the creator activities
     all work in the GL one under both projections. Keep the old classes one release for fallback.
  The fork's `DisplayViewCanvas` with `CameraHandler.setOrtho` proves the one-class approach works with this
  code lineage; its shader pipeline and buffer fillers are worth reading before R4.
  **Why this is more than a feature.** The `Graphics2D` viewports repaint on Swing timers and, when all four views
  are on screen, they eat enough CPU that the GL perspective view starves and stops updating. The code contains
  a workaround where the 2D timers measure their own paint time and back off when lagging, and it is not
  reliable. Many users are on Windows 11 where Swing itself only runs acceptably with the Java2D OpenGL pipeline
  preference, so the program is already fighting itself for the GPU. Step 0 of W10 is therefore to measure the
  current paint loop and stop repainting idle 2D views (repaint on model or camera change, not on a timer),
  which relieves the starvation before the GL viewport exists; the finished W10 removes the fight entirely
  because every view shares one GL context strategy.
- [x] **W11. Outliner: visible-but-not-editable and a right-click menu.** (eye + check glyphs per row; locked nodes and cameras now have their own visible sets and draw in the "visible uneditable" color) `ModelViewManager` already has a
  separate `visibleGeosets` set, `RenderByViewModelRenderer` and `PerspectiveViewport` already draw geosets that
  are visible or editable, and every call to `makeGeosetVisible` is commented out, so the third state exists in
  the data model and is unreachable in the UI. Add the same for nodes and cameras (today the checkbox says
  "visible" but toggles editability). Render the checkbox as a three-state cycle: empty, eye (visible), check
  (visible and editable), with Shift-click applying to siblings. Right-click: Show Only This, Show All, Hide
  Selected, Lock (visible, not editable), Select in Model tab, Open in Tracks. All undoable through the existing
  `showComponent`/`hideComponent` path.

### Preview fidelity

- [ ] **W12. Particle preview that matches the game, including Popcorn (PKB).** Today `RenderParticleEmitter2`
  and `RenderRibbonEmitter` in `wc3/mdl/render3d` are a hand port of Ghostwolf's mdx-m3-viewer as it stood
  years ago, `ParticleEmitter` (model-spawning) has no preview, and `ParticleEmitterPopcorn` renders nothing.
  Users want all three drawn together in the preview. Plan:
  1. **Classic emitters.** Re-port `ParticleEmitter2` and ribbons from the newer, more faithful Java port in
     WarsmashModEngine (`com.etheller.warsmash.viewer5.handlers.mdx` package: `ParticleEmitter2Object`,
     `Particle2`, `RibbonEmitter`, `EventObjectSpn/Spl/Ubr`), which is closer to the original game, then add
     `ParticleEmitter` (spawns the referenced model per particle, with its own animation) and the event object
     splats and spawned models. Keep the software particle path but move quad building into buffers (ties to R4).
  2. **Popcorn / PKB.** Reforged popcorn emitters reference `.pkb` Blizzard particle files. FernandoS27's
     WhiteoutFlakes project has shown a fan PKB renderer is feasible. Investigate its format decoding and node
     graph evaluation, write a `pkb/` parser in `craft3data` (headless, tested with F1-style round trips on the
     stock `.pkb` files), then a `RenderPopcornEmitter` that evaluates the graph on the CPU first and on the GPU
     later. Show a placeholder glyph and the emitter's path until the parser lands so users at least see where
     the emitter sits.
  3. **A shared emitter contract** so the preview, the 2D viewports and the future GL viewport (W10) all draw
     emitters through one `RenderEmitter` interface with `update(dt)` and `fill(buffer)`.
  Treat this like Ghostwolf treated his viewer: keep the emitter code small and rewrite it cleanly rather than
  patching the current port. It is one of the few places where starting over is cheaper than carrying the stack.
- [ ] **W13. Fix the "view camera" editing mode.** The graphical camera-animation editing view (the "View"
  camera function that looks through a model `Camera` while editing its keyframes) applies wrong rotations and
  was never finished. Make its orientation match the game: position and target from the camera tracks, roll from
  `KCRL`, field of view from the camera, and verify against a stock cinematic camera model in game footage or
  the Reforged World Editor. Also let the perspective view "look through" any camera and write the current view
  back to the camera as a keyframe.

## Phase M: data model modernisation (incremental, never a rewrite)

`EditableModel` keeps both object pointers and integer ids on most components and reconciles them only in
`doPostRead` and `doSavePreps`. `rebuildLists` at save recomputes textures, materials, texture anims and global
sequences from what is referenced, so an unused `Bitmap` added in the editor disappears after save and reopen.
Import relies on that reconciliation. The goal is: **ids are a serialisation detail; editing state holds
references only; the save-time automation stays but becomes visible and optional.**

- [ ] **M1. Inventory the dual state.** List every field that is an id mirror of a pointer (`Layer.textureId`,
  `GeosetAnim.geosetId`, `IdObject.objectId/parentId`, `AnimFlag.globalSeqId`, `Matrix` bone ids, `VertexGroup`
  indices, ...) with the code paths that read the id rather than the pointer. Output: a table in this file.
- [ ] **M2. Move id materialisation to the `MdxModel(EditableModel)` and MDL `printTo` boundary.** One component
  at a time, delete the id field from the editing class, compute it in the writer from `indexOf` or a sorted node
  map, and delete the corresponding `updateIds` step. Round-trip with F1 after each component. The fork did this
  wholesale (`EditableModel.getId(Object)`, `modelIdObjects` lazy maps, `BitmapAnimFlag` holding `Bitmap`); we do
  it per component so Import can be checked at each step.
- [ ] **M3. Reconcile at event time, prune only on request.** `doSavePreps` does two different jobs and they
  need different timing. *Deriving* (ids from pointers, pivots from nodes, matrices from vertex groups, geoset
  anim to geoset links, bone geoset ids) is idempotent and safe to run whenever the structure changes, so run it
  from the `ModelStructureChangeListener` after imports, pastes, deletes and node reparenting, and again in the
  writer. *Pruning* (unused textures, materials, texture anims, global sequences, empty geosets) must not run on
  every event or adding a texture in the Model tab would delete it immediately. Pruning moves into an expanded
  **Edit > Optimize** that also merges identical texture references and identical materials, regenerates geoset
  and geoset-anim ids on bones, and reports what it changed, all as one undo action. Preferences get two
  checkboxes, "Optimize after import" and "Optimize on save", both **on** by default so the current workflow is
  unchanged; Import calls the same optimize step directly. Answer to "would this break the code": no, provided the
  derive and prune halves are separated first. The code paths that depend on save-time reconciliation are
  Import (`ImportPanel`), OBJ import (`Build`, `BuildWLists`), the Add > Particle path, and the MDL text Apply
  action; each of them already calls `doPostRead` or relies on the next save, and each can call the derive step
  explicitly instead.
- [ ] **M4. Stop mutating the live model during save.** Today saving removes empty geosets from the open model
  and reorders `idObjects`. After M2 and M3 the writer should work from the live model without mutating it, or on
  a shallow copy, so that "save then keep editing" is not a hidden edit.
- [ ] **M5. Reference integrity guard.** A debug-mode validator run after every `UndoAction` (behind a
  preference) that checks no component points at an object outside the model. Catches the class of bug Import
  and paste are prone to, before it becomes a save-time surprise.

## Phase D: model data and formats

**Round-trip findings (2026-09-18, first run of F1 over the 1.22, 2.0.x and 3.0 installs).** Fixed in the
same session: `AnimFlag.sort` was an unstable quicksort, so keys sharing a time swapped on every save;
`Layer.updateIds` remapped animated texture-id tracks through a map it was overwriting while iterating, which
collapsed a 45-frame water track into repeating triples on the first save and dropped 21 textures on the second;
`cureBoneGeoAnimIds` derived bone ownership from load-time matrices, so HD models wrote different bone GeosetIds
on the second save; fixed-width MDX names with embedded CR/LF (MalFurion's "Stand Ready") broke the MDL text
form; the "camera name longer than 20 characters" corruption dialog fired on every 3.0 camera. Still open, see
D7 and F2.

- [ ] **D1. 1800 unknowns follow-up.** Confirm or swap the `BackFacesForShadows` / `AmbientOcclusion` bit
  mapping once documented; add `Sounds`/`SoundEmitter` and `ComponentSkin` if a real model appears.
- [ ] **D2. Honest version conversion.** "Assign FormatVersion N" only sets the number. Make it drop or warn
  about fields the target cannot hold (DOF tracks, 16-bit skin indices over 255, extended light fields, Glider).
- [ ] **D3. glTF 2.0 export** (static and skinned, animations as clips, PNG textures). SD first, HD after.
- [ ] **D4. glTF import** into a new model or as an imported geoset.
- [ ] **D5. Model validator** report with one-click fixes where safe (unreferenced bones, geosets with no matrix,
  keys outside any sequence, HD weights not summing, duplicate sequence names, missing textures, bad extents).
- [ ] **D6. Keyframe simplifier v2**, tolerance-based with a preview of maximum deviation per track.
- [~] **D7. Writer fixed point.** Drive the round-trip tool to zero UNSTABLE and zero DIALOG across all three
  installs, then keep it there. Baseline after the first fixes (2026-09-18):

  | install | models | EXACT | STABLE | failures |
  |---|---|---|---|---|
  | 1.22 (all MPQs) | 3320 | 1770 | 1550 | 0 |
  | 2.0.x CASC (first 700 by path) | 700 | 411 | 289 | 0 |
  | 3.0 CASC, `_de.w3mod\units\` first 40 | 40 | 0 | 40 | 0 |

  The 3.0 Definitive Edition models are 1 to 4 MB each and the MDL text pass makes the check slow (about 4 s per
  model); run 3.0 in batches with `--filter` and `--limit`, or add a `--no-mdl` switch if a faster smoke run is
  needed. Record any new UNSTABLE or DIALOG here with the model path and the chunk from a chunk diff.

## Phase E: further editor features

- [ ] **E1. Weight painting for HD skin weights** in the GL viewport (after W10), with per-bone overlay.
- [ ] **E2. Loop cut, edge split, bridge** in the vertex and face editors.
- [ ] **E3. Snap to grid and to vertex** with grid size in the status bar.
- [ ] **E4. Better mirror** with `_L`/`_R` bone renaming and animation mirroring.
- [ ] **E5. Graph editor for keyframes** next to Tracks, tangent handles for Hermite and Bezier.
- [ ] **E6. Animation retargeting dialog** by bone-name mapping, complementing Import rather than replacing it.
- [ ] **E7. Sequence manager table** (grows out of W5's sequences overview).
- [ ] **E8. Search everywhere** across unit browser, doodad browser and the file tree, indexed once per
  data-source change.
- [ ] **E9. Thumbnails in the model browser**, rendered offscreen and cached under the profile directory.
- [ ] **E10. Screenshot and turntable GIF export** from the preview.
- [ ] **E11. Autosave and crash recovery.**
- [ ] **E12. Batch tools dialog** backed by F1/F2, replacing the `hacks/` package as the home for one-off jobs.
- [ ] **E13. Scripting API** for the Nashorn console: a documented, stable object with model, selection and
  undo helpers.
- [ ] **E14. Preferences as JSON** with one-time import of the old `user.profile`.
- [ ] **E15. Event object browser** with previews (the fork's `EventBrowser` idea).

## Phase R: rendering

- [ ] **R1. HD shading closer to the game**: ORM, emissive, team colour in HD, environment presets, and the 1800
  light falloff fields driving the preview.
- [ ] **R2. Shaders as resources** with hot reload, out of the inline strings in `NGGLDP`.
- [ ] **R3. Wireframe-over-shaded, backface and normals toggles.**
- [ ] **R4. Buffered geometry** per geoset instead of `glBegin` emulation. After W10 and R2.

## Phase C: code health

- [ ] **C1. Split `MainPanel`** (7084 lines): menu construction, `actionPerformed` dispatch, docking layout.
  Do this early so Phase W lands in small classes.
- [ ] **C2. Delete dead code** listed in `CLAUDE.md`, one commit per package.
- [ ] **C3. Vendored copies**: replace `com/etheller/collections/TreeMap` and `com/hiveworkshop/json` if nothing
  depends on local modifications.
- [ ] **C4. Logging** instead of 349 `printStackTrace` calls, with a log file under the profile directory.
- [ ] **C5. Unit tests** for pure code once F1 and F2 exist.
- [ ] **C6. TODO triage** of 314 markers.

## Suggested order

F6, F3, F4 (small, immediate). C1 partially (extract menus and dispatch) so W-items do not grow `MainPanel`.
Then W1 + W2 + W4 together (one tree, one popup, one drag handler), W5 + W6, W7, W8, W9, W11. F1 and F2 before
Phase M. W10 and W12 are the largest items; stage 1 and 2 can be done any time, stage 3 after R2. Keep interleaving
one C item per two feature items.

## Appendix A: reading list in the twilac fork

Clone with all branches; the useful work is on `timelinepanel_and_div` (2024), `better_activities` (2023) and
`shader-render` (2022), not on `master`. Paths below are under `modelstudio/src/com/hiveworkshop/rms/` and are
read with `git show <branch>:<path>`.

Worth borrowing as ideas:

- `util/TwiTextEditor/FlagPanel.java`, `EditorHelpers.java`: the static/dynamic property editor and typed
  editors (alpha, color, texture with thumbnails, rotation). Borrow the typed-editor idea for W6 and W7, not
  the per-sequence keyframe tables.
- `ui/application/edit/animation/TimeLinePopup.java`, `KeyframeTransferHelper`: keyframe clipboard with
  interpolate-on-paste and per-node submenus, for W7.
- `ui/gui/modeledit/modelviewtree/NodeThing.java` and `editor/wrapper/v2/ModelView`: two-glyph visible and
  editable toggles with Shift-click on siblings, for W11.
- `editor/actions/nodes/ParentChangeAction`, `tools/IdObjectTypeChanger`: reparent and change-node-type
  actions, for W2 and W5.
- `ui/application/edit/mesh/viewport/DisplayViewCanvas.java`, `viewer/CameraHandler.java`: one GL canvas
  with ortho as a mode, for W10. `viewer/twiTestRenderMaster/*BufferFiller` and `res/shaders/` for R2 and R4.
- `ui/application/model/nodepanels/ComponentIdObjectPanel` family: field inventory per node type, for W5.
- `editor/model/util/TempSaveModelStuff.toMdlx(model, clearUnused)` and File > "Optimize on Save": the
  explicit-cleanup shape that M3 adopts.
- `ui/application/model/nodepanels/EventBrowser`: event object browser with previews, for E15.

Do not copy:

- The data-source cache handling (`ui/preferences/dataSourceChooser/DataSourceChooserPanel` and
  `ProgramPrefWindow`): two diverging copies of the drop list, and a size-capped `BLPHandler` whose lifetime is
  unrelated to the data source.
- The import rewrites (`ui/gui/modeledit/importpanel/ImportPanelNoGui2`, `tools/twilacimport/`): they carry
  in-source `ToDo` notes about HD matrices and lost the partial-skeleton edge cases. Keep our `ImportPanel`
  and refactor it in place if needed.
- The wholesale package relayout and class renames. Our history and the user community's bug reports refer to
  the current names.

## Appendix B: external references

- Ghostwolf's mdx-m3-viewer (https://github.com/flowtsohg/mdx-m3-viewer): the origin of `RenderModel`,
  `RenderNode` and the particle code; no longer maintained.
- WarsmashModEngine (https://github.com/Retera/WarsmashModEngine): the maintainer's later, more faithful Java
  port of the same renderer for classic graphics, and the reference for W12 step 1 and for MDX/MDL parsing
  choices.
- WhiteoutFlakes (https://github.com/FernandoS27/WhiteoutFlakes): a fan renderer for Reforged PKB popcorn
  effects; the reference for W12 step 2.
- twilac's fork (https://github.com/tw1lac/ReterasModelStudio): see Appendix A.

## Log

- 2026-09-18: branch created; `CLAUDE.md` and this roadmap added on top of the 3.0.0 and CASC fixes.
- 2026-09-18: maintainer wishlist folded in as Phase W after a survey of the current Model, Tracks, Add,
  Modeling, Outliner and viewport code and of the twilac fork's branches.
- 2026-09-18: added W12 (particle and PKB preview), W13 (view camera), reworked M3 around event-time
  reconciliation and an expanded Optimize tool, added Appendix B.
- 2026-09-18: F1, F3, F4, F6 and W1 to W4 done; round-trip tool found and fixed four writer bugs (see Phase D).
- 2026-09-18: GUI verified under Xvfb (`xvfb-run`, `mate-wm`, `xdotool`, a Robot screenshot helper) against the
  3.0 install: Model tab popup, Delete, Undo, copy/paste, drag reparent and New all work with no exceptions.
  Full-install round trips: 1.22 all 3320 models clean, 2.0.x first 700 clean, 3.0 DE units first 40 clean.
- 2026-09-19: W11 (Outliner tri-state), W9 (primitives) and W7 (Tracks keyframe editor) done and verified under Xvfb.
  Note for GUI testing: the profile's "disable DirectX" preference turns on the Java2D OpenGL pipeline, which
  leaves partial black repaints on the virtual display; switching tabs forces a full repaint.
