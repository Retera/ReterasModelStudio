# Warcraft III 3.0.0 (Forsaken Kingdom) MDX 1800 format notes

Findings from patch 3.0.0 (build 24268, released 2026-09-12), derived from the 14,956 stock `.mdx` files on the
Blizzard CDN (product `w3`, all of `war3.w3mod`, `_hd.w3mod` and the new `_de.w3mod` layer) and from the MDL keyword
table inside `Warcraft III.exe` plus the World Editor string table. Everything below is implemented in
`craft3data` (`ModelUtils.isForsakenKingdomFormat` and friends).

## Version

* Every stock model is `FormatVersion 1800`. Two day/night-cycle environment models (`cinematics/*/environment/`)
  are 1600 and 1700 and already use the new layouts. Blizzard's own Maya exporter ("Maya Tools Version 0.30",
  September 2025) writes MDL text as `FormatVersion 1300`, which the game's pipeline compiles to 1800 MDX; a 1300
  file uses the same text features as 1800 (per-layer `Shader`, `static TextureID id <= slot`, `Tangents` and
  `SkinWeights` blocks, `BindPose`, no `VertexGroup`). Versions >= 1300 are therefore treated as the new format
  (`ModelUtils.FORMAT_VERSION_FORSAKEN_KINGDOM_MIN`). The binary layouts of 1300-1500 MDX have never been seen;
  the light fields are gated separately (ShadowCasting at 1300, the falloff triple at 1600) as a best guess.
* The exporter's MDL block order inside `Geoset` is `Vertices, Normals, Tangents, TVertices, SkinWeights, Faces,
  Groups, MaterialID, SelectionGroup, LevelOfDetail, Name`, one triangle per line under `Triangles`, and
  `Matrices{ 0 },` without a space. The classic reader accepts any block order since the 3.0 update.

## Geosets

* `SKIN`: the count field is unchanged (`vertexCount * 8`) but every element is now a `uint16`: four bone indices
  followed by four weights per vertex (16 bytes per vertex, weights still sum to 255). Bone indices can exceed 255.

## Lights (`LITE`)

After the node:

| field | type | notes |
|---|---|---|
| type | u32 | 0 omni, 1 directional, 2 ambient |
| ShadowCasting | u32 | new, "Casts Shadows" in the World Editor; bare `ShadowCasting,` flag in MDL |
| AttenuationStart, AttenuationEnd | f32 | unchanged |
| Color[3], Intensity, AmbColor[3], AmbIntensity | f32 | unchanged |
| ShadowIntensity | f32 | as in 1200 |
| ShadowCastingStart, ShadowCastingEnd | f32 | new |
| QuadraticFalloff, LinearFalloff, Damping | f32 | new (stock defaults 0.0005, 0, 0.00001) |

then the usual `KLA*` / `KLB*` tracks.

## Cameras (`CAMS`)

* The top byte of each camera's inclusive size holds a header byte, always 3 in stock data. It is masked off on
  read, preserved, and written back (3 for new cameras).
* Three depth-of-field tracks, one float per key, written before the classic tracks: `IDUF` focus distance
  (`DOFDistance` in MDL, aliases `FocusDistanceKeys`), `ELAF` focal length in mm (`FocalLength`), `PTSF` f-stop
  (`FStop`). Stock order is `IDUF, ELAF, PTSF, KCTR, KCRL, KTTR`, which the writer now follows.
* Unknown trailing camera data is skipped using the inclusive size instead of being parsed as a bogus camera.

## Materials

* Shading flag bits 0x200 and 0x400 are new. The stock data uses 0x200 only on the cinematic shadow-blocker doodads
  and 0x400 on large buildings and walls; they are mapped to the new MDL keywords `BackFacesForShadows` and
  `AmbientOcclusion` respectively. That pairing is a best guess and may need swapping once Blizzard documents it.
* Layer shader type ids other than 0/1 exist (2 on the Forsaken Paladin portrait, 24 on cinematic ice props).
  They are preserved through `Layer.getUnknownShaderTypeId()` and rendered with the nearest known shader.

## New chunk `DILG`

* Tag bytes `DILG` (a reversed `GLID`, matching the new `Glider` MDL keyword). Only nine Definitive Edition stair
  and bridge doodads carry it, always zero-filled. It is preserved verbatim (`GliderChunk`) and written after `BPOS`.

## Not seen in stock data

The exe's MDL keyword table also lists `Sounds` / `SoundEmitter` / `SoundFile` / `SoundTrack` / `SoundChannel`
(a sound emitter object) and `ComponentSkin`; no stock model uses them and no binary layout is known.

## CASC layout

* New layer `war3.w3mod\_de.w3mod` (Definitive Edition graphics) with its own `_locales`, `_tilesets` and
  `_teen` sub-mods; `_hd.w3mod` (Reforged) is still present. The 2.0 `_addons\hd2.w3addon` layers are gone.
* TVFS, BLTE, encoding and archive-index formats are unchanged.
* The data source chooser now detects 3.0 installs and offers a "Definitive Edition Graphics Mode" preset.

## Alternative parser

The `com.hiveworkshop.rms.parsers.mdlx` package is the Warsmash-derived (mdx-m3-viewer port) parser copied from
tw1lac's fork with the 3.0 changes above applied (`MdlxVersion`). "Model file parser" in the preferences, the
`rms.modelParser` system property (`classic`, `warsmash`, `fallback`) or `--parser=warsmash` on the `-convert`
command line selects it; `WarsmashParserBridge` turns its `MdlxModel` into an `EditableModel` by serialising to
binary MDX in memory and reading that with the classic chunk reader. It never opens dialogs; its messages go to
stderr via `MdlxParseLog`.
