# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

Retera Model Studio: a Java 17 Swing + LWJGL 2 editor for Warcraft III models (classic MDX 800, Reforged 900-1100,
and 3.0.0 "Forsaken Kingdom" 1600-1800). It reads game assets straight out of a local Warcraft III install
(CASC for modern patches, MPQ for legacy ones) and can browse units/doodads to find models.

## Build and run

Gradle 7.3 wrapper, three modules: `craft3data` (model formats, game data, rendering, most GUI) ->
`craft3editor` (World Editor-style unit/doodad browser trees) -> `matrixeater` (the application, `MainFrame`).

```
./gradlew :matrixeater:compileJava     # compile everything (craft3data, craft3editor transitively)
./gradlew :craft3data:compileJava      # fastest check when only the data layer changed
./gradlew :matrixeater:run             # launch the editor
./gradlew :matrixeater:runtime         # release build: jlink image at matrixeater/build/image (bin/drms, drms.bat)
./gradlew :matrixeater:dist            # quick fat jar at matrixeater/build/libs/matrixeater-<version>.jar
```

Add `--offline` when there is no network; all dependencies are already in the Gradle cache plus `jars/`
(`blp-iio-plugin.jar`, `idw-gpl.jar`, pulled in by a flat fileTree dependency).

Headless conversion, useful for testing the format layer without a GUI:

```
java -jar matrixeater/build/libs/matrixeater-0.4.5.jar -convert in.mdx [out.mdl]   # mdx <-> mdl by extension
java -jar matrixeater/build/libs/matrixeater-0.4.5.jar -convert in.obj [out.mdx]   # obj import (may pop dialogs)
java -jar matrixeater/build/libs/matrixeater-0.4.5.jar -convert in.blp out.png     # image conversion
```

There is no test suite. `test` is a no-op; files named `*Test*` are ad-hoc `main()` scratch programs. Verify
format changes by round-tripping real models (see `docs/mdx-1800-forsaken-kingdom.md` for how 1800 was derived).
The version string lives in two places: `version` in the root `build.gradle` and
`MainFrame.RETERA_MODEL_STUDIO_VERSION`.

## Conventions

- Tabs for indentation. About one in nine Java files is CRLF; preserve whatever the file already uses
  (Python/sed rewrites that normalize line endings will produce 1000-line diffs).
- User settings are **Java serialization** (`SaveProfile` -> `~/.reteraStudio/user.profile`, or
  `%APPDATA%\ReteraStudio\user.profile` on Windows). New fields on `SaveProfile` or `ProgramPreferences` must be
  boxed types with null-defaulting in `ProgramPreferences.reload()`, or existing profiles break.
- Commit messages carry a `Co-Authored-By: Claude ...` trailer for AI-assisted work.

## Architecture

### Two model representations (craft3data)

- `wc3/mdx/` is the **binary chunk model**: `MdxModel` is a bag of chunk objects (`GeosetChunk`, `LightChunk`,
  `CameraChunk`, ...), each with `load/save/getSize` over `BlizzardDataInputStream/OutputStream` and one small
  class per keyframe track (`GeosetTranslation`, `CameraFocusDistance`, ...). Unknown chunk tags are skipped by size.
- `wc3/mdl/` is the **editable object model** the rest of the app uses: `EditableModel` owns lists of
  `Animation`, `Geoset`, `Material`/`Layer`, `IdObject` subclasses (`Bone`, `Helper`, `Attachment`,
  `ParticleEmitter2`, ...), `Camera`. References are object pointers, not ids. `AnimFlag` is the single
  universal keyframe track type for every animated property.
- Conversion is by constructor: `new EditableModel(MdxModel)` then `doPostRead()` on load;
  `doSavePreps()` then `new MdxModel(EditableModel, alwaysUseMinimalMatricesHD)` on save. MDL text goes through
  `MDLReader` + static `read(BufferedReader)` methods and `printTo(...)` on each class.
- **Object ids are recomputed on save** (`updateObjectIds`, fixed type order, bind pose rebuilt); `doPostRead`
  resolves ids back to objects (`updateIdObjectReferences`, `Geoset.applyMatricesToVertices`). After any bulk
  id-level mutation, re-run `doPostRead()`. Never trust a stored integer id between those passes.
- **Format version branching lives in `wc3/util/ModelUtils`** (`isForsakenKingdomFormat`, `isSkin16BitSupported`,
  `isExtendedLightSupported`, `isCameraDepthOfFieldSupported`, `isTangentAndSkinSupported`, ...). Add a
  predicate there rather than comparing `formatVersion` inline.
- Adding a node type, track, or field touches about six places: the `mdx` chunk class, the `EditableModel(MdxModel)`
  step, the `MdxModel(EditableModel)` step, the MDL `read`/`printTo` pair, an `AnimFlag` constructor, and
  `sortIdObjects`/`getAllAnimFlags`. Grep for an existing sibling (e.g. `ShadowCastingStart`) to find them all.
- SD skinning is `Geoset.matrix` + `VertexGroup`; HD is `GeosetVertex.skinBoneIndexes` + weights + tangents,
  with `MatrixGeneratorStrategy` choosing how HD bone lists are emitted. Layers hold an
  `EnumMap<ShaderTextureTypeHD, ...>`; non-diffuse animated texture ids are smuggled as `AnimFlag`s named
  `NormalTextureID` etc.
- The data layer is **not headless-clean**: `JOptionPane` dialogs are raised from inside `EditableModel`,
  `Geoset` and `Matrix` on bad data. Keep that in mind for CLI/convert paths and avoid adding more.
- Other formats: OBJ import via `matrixeater/src/com/owens/oobjloader` + `com/matrixeater/imp/MDLBuilder`.
  `mdl/MDXHandler` (external `converter.exe`) and the `mdlx/` package are dead.

### Editor (craft3data `gui/` + matrixeater `MainPanel`)

- `MainFrame.main` -> `MainPanel` (7000+ lines, the god class: every menu item field, `createMenuBar()`, one
  giant `actionPerformed` dispatch, InfoNode `RootWindow` docking layout, toolbar, shared
  `ModelStructureChangeListener` impl, list of open `ModelPanel`s). The four viewport `View`s are shared
  singletons whose contents are swapped in `setCurrentModel()`.
- `gui/modeledit/ModelPanel` is the per-open-model aggregate: `EditableModel`, `ModelViewManager`
  (visibility/editability + `ModelView` read-only interface), `UndoManagerImpl`, `RenderModel`,
  `ModelEditorManager`, the three 2D `DisplayPanel`s and one `PerspDisplayPanel`, plus trees.
- `gui/modeledit/newstuff/ModelEditorManager` rebuilds a fresh `ModelEditor` + `SelectionManager` whenever the
  selection type changes (vertex/face/group/cluster/animate/T-pose), so never cache a `ModelEditor`. Two
  generations of editor code coexist; prefer `modeledit/newstuff/*` and `actions/newsys/*` over legacy `modeledit/*`.
- **Every edit is an `UndoAction`** (`undo()`, `redo()`, `actionName()`; ~95 classes under
  `modeledit/actions/**` and `modeledit/newstuff/actions/**`). A new editing feature: return an `UndoAction` from a
  `ModelEditor` method (add to the interface, `AbstractModelEditor`, and the `ModelEditorNotifier` fan-out if it is
  a new operation), push it via `UndoManager.pushAction`, and fire the `ModelStructureChangeListener`
  (`geosetsAdded`, `nodesRemoved`, `keyframeAdded`, `texturesChanged`, ...) from **both** do and undo paths.
  MainPanel's listener implementation is what repopulates trees and calls `RenderModel.refreshFromEditor`;
  skipping it leaves ghost geometry in the 3D view. Mouse drags reach actions via
  `ModelEditorViewportActivityManager` -> `Manipulator`, whose `finish()` returns the pushed action.
- Animation editing state is one `TimeEnvironmentImpl` on MainPanel shared by every `RenderModel`;
  `setStaticViewMode(true)` is T-pose. `TimeSliderPanel` is the keyframe bar; `MainPanel.refreshAnimationModeState()`
  is the mode switch.

### Rendering

- 2D orthographic viewports are plain `Graphics2D` (`Viewport`, `ViewportModelRenderer`). Warcraft III is Z-up;
  viewports address axes as bytes 0=X, 1=Y, 2=Z (Front = Y,Z; Bottom = Y,X; Side = X,Z) and
  `BasicCoordinateSystem` flips screen Y.
- 3D uses LWJGL 2 `AWTGLCanvas` via `gui/lwjgl/BetterAWTGLCanvas`: `PerspectiveViewport` (edit view) and
  `AnimatedPerspectiveViewport` (preview), each repainted by a 16 ms Swing timer **on the EDT**. All GL work must
  happen there.
- `rms/editor/render3d/NGGLDP` is the GL abstraction: a `glBegin`-style `Pipeline` with fixed-function, SD shader
  and HD (PBR-ish) shader implementations, switched per layer by `LayerShader`. **Shaders are inline Java string
  constants in NGGLDP**; there are no shader resource files. `NGGLDP.pipeline` is a global static.
- Animation evaluation is `wc3/mdl/render3d/RenderModel` / `RenderNode` (ported from Ghostwolf's mdx-m3-viewer),
  including particle and ribbon emitters. `RenderNode` uses static scratch vectors; single-threaded only.

### Game data (craft3data `gui/datachooser`, `mpq`, `blizzard/casc`, `units`)

- `DataSource` is the asset interface; `DataSourceDescriptor` is its serializable recipe stored in the profile.
  `MpqCodebase.get()` is the app-wide compound source (jar resources first as lowest priority, then the profile's
  descriptors; **lookups iterate backwards so the last source wins**). `EditableModel.setFileRef` layers the
  model's own folder on top via `CompoundDataSource`.
- Modern installs: `CascDataSource` wraps the JCASC port (`blizzard/casc/io/WarcraftIIICASC`, TVFS in `casc/vfs`).
  It tries each prefix (`war3.w3mod`, `war3.w3mod\_hd.w3mod`, `war3.w3mod\_de.w3mod`, `_locales\...`) in order.
  Which prefixes exist per patch, locale detection, and the SD/HD/DE mode buttons live in
  `DataSourceChooserPanel`. Legacy installs become `MpqDataSource`s; extracted folders become `FolderDataSource`s.
- Non-UTF-8 TVFS names exist in 3.0 data; `VirtualFileSystem.convertPathFragments` falls back per fragment to
  Windows-1252 and lookups retry with that encoding.
- Textures: `BLPHandler.get().getTexture(dataSource, path)` with fallback chain `.blp` -> `.dds` -> `.tga` and
  `filealiases.json` aliases (applied before the extension fallback). BLP and DDS decode through ImageIO SPIs
  from `jars/blp-iio-plugin.jar` and `net.nikr:dds`. Caching is skipped for `FolderDataSource` so edited files
  reload live.
- Unit/doodad data: `StandardObjectData` parses SLK + profile txt into `DataTable`s (statically cached);
  `MutableObjectData` overlays w3u/w3d/w3a changesets from a mounted map. The browser trees come from
  `craft3editor` (`UnitEditorTree`, `BetterUnitEditorModelSelector`); the rest of `jworldedit` is a dormant
  World Editor clone.
- **Changing data sources requires the full cache-drop litany**: `MpqCodebase.refresh`,
  `UnitOptionPanel.dropRaceCache`, `DataTable.dropCache`, `ModelOptionPanel.dropCache`, `WEString.dropCache`,
  `Resources.dropCache`, `BLPHandler.dropCache`. It is duplicated in `MainPanel.dataSourcesChanged` and
  `MainFrame.main`; keep both in sync or factor it out.
- Path handling differs per source: CASC lowercases and maps `/` to `\`; `FolderDataSource` is case-sensitive on
  Linux; the install path default comes from the Windows registry via `reg query` and is Windows-only.

### Dead or standalone code (do not extend)

`matrixeater/src/com/matrixeater/hacks/**` (one-off scratch mains with hardcoded paths), `com/matrixeaterhayate`
(except `TextureManager`), `ysera/`, `com/matrixeater/colorizer`, `com/matrixeater/blpconv`, `stuff/`,
`com/owens/oobjloader/{lwjgl,test}`, and the standalone frames in `craft3editor` (`JWMEFrame`, `OSWE`, `TestMain*`).
