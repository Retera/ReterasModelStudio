# PKB (PopcornFX baked effect) runtime port notes

Java port of the `cornflakes` runtime from WhiteoutFlakes (see `THIRD_PARTY.md` and `licenses/WhiteoutFlakes-LICENSE*`).
Packages: `com.hiveworkshop.wc3.pkb` (container parser), `pkb.vm` (bytecode decoder, register VM,
native call dispatchers, shape sampling, noise), `pkb.bind` (asset -> layer programs, samplers,
renderers, event routing), `pkb.sim` (per-particle harness, pools, effect runtime, packet extraction).
Rendering glue lives in `wc3.mdl.render3d.RenderPopcorn*`.

## Container (PkbReader)
Header 28 bytes: magic `11 0B 00`, version byte `0xCA`, major/minor/patch at 4..6, generator at 7,
revision u32 at 8, objectCount at 12, typeCount at 16, stringTableOffset at 20. Type table 8 bytes
each. Object: u32 bodySize, u8 flags, u32 typeIndex, u16 fieldCount, fields (u16 fieldId + schema
typed value; arrays u32 count prefixed; bool 1 byte; scalars 4; string = u32 index, 0xFFFFFFFF null).
Links are 1-based object indices (0 / 0xFFFFFFFF null); `PkbValue.asLink()` returns 0-based or -1.
Schema v2.5 (`res/pkb/hbo_fields_v25.txt`) vs v2.9 (`hbo_fields_v29.txt`).

## Blob layout (CCompilerBlobCache.Blob)
Field = u32 wordCount + wordCount*4 bytes. Body header 36 bytes: registerCounts[5] at 16..35.
v2.5: constStorageBytes@8, bytecodeBytes@12, constants at 36 then bytecode.
v2.9: bytecodeBytes@4, constStorageBytes@12, bytecode at 36 then constants.
Identifier: 0 Init, 3 Physics, 4 TimeFixed, 5 TimeVarying. evolve = physics ?: timeFixed ?: timeVarying.
v2.5 layer cache links: `BlobCache_IR_TimeFixed[]`, `BlobCache_IR_TimeVarying`; v2.9: `BlobCache_Backends[]`.

## Bytecode
IR opcodes 0x42..0x53, CBEM 0x69..0x7D; CBEM aliases normalised (69->LoadExternal, 6A->Store,
6C->TypeConverter, 6D->VecCtor, 6E->VecSwizzle, 77/78/79->MathFunc1/2/3, 7A->Select, 7B->FunctionCall).
Register id: bank=(v>>24), scope=normalise((v>>16)&0xFF)&3 (>=0x20 -> >>5), localIdx=v&0xFFFF.
Scopes: 0 const pool (32-byte slots, 16 used), 1 local, 2 input, 3 stream. kRegVoid 0xFFFFFFFF reads as float 0.
Banks: 00 handle, 02 bool, 04 bool3, 08 int, 09 int2, 1A ptr, 1B int2, 1C int3, 1D int4, 20..23 float1..4,
25 "intAlt" = 4-lane float family (quaternion), 26 int2. RegisterValue = 4 float lanes + componentCount + typeBank;
ints are raw bits in lanes. Truthy = int bits != 0 || float != 0.
Swizzle: operand u24, packed = ((b3&0xF0)<<4)|b2, 3 bits/lane, 0-3 lane, 4 zero, 5 one (bool one = 0xFFFFFFFF).
FunctionCall: flags u8, objSlot i16, extFunc u16, argc u8, retReg u32, args (u8 kind, u32 reg)*argc.

## Simulation
RNG: state = state*0x000D0F95 + 0x00D19EC3; unit draw = bits((raw>>9)|0x3F800000) - 1.
Particle dead iff lifeRatio >= 1.0. Tick per layer: drain pending spawns (Init per new particle,
seed = parentRng + layerSeed + 111), prepare (lifeRatio += dt*externals[self.invLife]; write self.lifeRatio),
inject scene.dt / scene.time, attribute overrides (`__a_Game.*`), run evolve on particles alive at frame
start, route SpawnEvents by eventId == globalEventSlotId to target layers. Root spawner layer = no renderers
and not a kick target; pool 1, Init once. Layer seed = base + layerIdx*0x9E3779B1.
Externals are canonicalised per layer by name (1-based) across scopes; storage size = max slot + 1.
Units: 1 corn unit = 100 game units. Emitter L2W = node world matrix (scaled by model scale), translation *0.01.

## Where the port deliberately differs from the reference
Payload-space transforms in evolve scopes (`xform_l2w_*_masked` with the payload enter bit, mask 0x13 on
every tick). The reference applies the parent particle's spawn frame (position payload + orientation payload)
in every scope. That is right for children of world-space parents, which feed an offset through the frame each
tick, but a child of a *local-space* parent (a spawner layer that never calls `xform_l2w`, so its positions are
emitter-relative) first stores `xform(0)` = the parent position at spawn and then re-transforms that stored value
every tick; applying the parent frame twice folds the value back onto itself and the game's weapon-glow trails
(`SharedFX/Hero_Glow/Weapon_Glow_*.pkb`, ~16 effects) collapse at the model origin. The reference does the
same (verified with a standalone build of cornflakes: `weapon_glow_shaft` renders at the origin, `_circle`
follows the emitter). The port keys the evolve-scope rule on the parent's space: `SpawnEvent.spawnFrameLocal`
(set by `kick` from `LayerProgram.simulatesInWorldSpace()`) makes the evolve-scope payload path apply the
emitter transform instead of the parent frame. A corpus sweep of all 2165 PKBs in 3.0 with the emitter
displaced (the `FollowScan` idea: count particles nearer the origin than the emitter) improved 48 effects and
regressed none; `-Dpkb.legacyPayloadFrame=true` restores the reference rule and `-Dpkb.traceXform=true` logs
every masked transform for diagnosis.

## Renderer input map
RenderSlot: Position, Size, Enabled, Orientation, Axis0, Axis1, Rotation, Color, TextureID, ...
From `CLayerCompileCacheRendererParticleInput` (Semantic 0 Position, 1 Size, 2 Enabled, 4/8 Axis0, 5 Axis1,
6 Rotation, 12 Orientation; AdditionalFieldName Color/TextureID/...; IndexInStorage -> `CLayerCompileCache.Fields`
name) else inferred by external name suffix `__Position` / prefix `Position_` etc.
Blend: Transparent.Type 0 Add, 1 NoAlphaAdd, 2 Blend, 3 BlendAdd; Opaque 4; AlphaKey 5.
Billboard modes: 0 ScreenAligned, 1 ViewposAligned, 2 AxisAlignedQuad, 3 AxisAlignedSpheroid, 4 AxisAlignedCapsule, 5 PlaneAligned.

## Deliberately not ported
Mesh shapes (.pkmm), texture samplers, external vector fields (.pkvf), ribbons/meshes/lights renderers,
LOD, SIMT packet backend. Those native calls return the same defaults the reference uses when the
resource is missing.
