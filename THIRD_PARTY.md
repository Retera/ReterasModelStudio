# Third-party notices

Attribution and licence notices for code and reference material from other projects that this program is derived
from or built with. The verbatim licence files also live under `licenses/`.

## WhiteoutFlakes (BSD-3-Clause)

The PopcornFX baked-effect support under `craft3data/src/com/hiveworkshop/wc3/pkb` and the field schema
tables under `craft3data/res/pkb` were written with the WhiteoutFlakes project
(https://github.com/FernandoS27/WhiteoutFlakes, Copyright (c) 2026 Fernando Sahmkow) as the reference for the
`.pkb` container layout and the HBO class schema. Portions are AI-assisted translations of that code, which its
`LICENSE-AI.md` treats as derivative works. The BSD-3-Clause text and that notice are reproduced below and in
`licenses/WhiteoutFlakes-LICENSE.txt` and `licenses/WhiteoutFlakes-LICENSE-AI.md`. Neither the project nor its
author endorses this program.

The HD environment probe loader and split-sum BRDF table in
`craft3data/src/com/hiveworkshop/rms/editor/render3d/HDEnvironmentProbe.java` follow the same project's
`src/renderer/ibl/env_probe.cpp` and `split_sum.cpp` (the probe file layout, the engine mip count, the LUT
construction and its channel layout), under the same notices.

## Wc3Shaders (BSD-3-Clause)

The Reforged HD lighting in `craft3data/src/com/hiveworkshop/rms/editor/render3d/NGGLDP.java`
(`HDDiffuseShaderPipeline`, the `u_lightMode == 1` branch) is a GLSL port of the HD mesh pixel shader
reconstructed in FernandoS27's Wc3Shaders (https://github.com/FernandoS27/Wc3Shaders, Copyright (c) 2026
Fernando Sahmkow): the team-colour layer, the GGX/Schlick terms with the shipped clamps, the main light and
ambient/probe mix, the clustered point-light falloff and the fresnel rim. Its BSD-3-Clause text and AI notice are
reproduced below and in `licenses/Wc3Shaders-LICENSE.txt` and `licenses/Wc3Shaders-LICENSE-AI.md`. The probe pair is sampled the way the
shader does it, from the game's own `Environment/EnvironmentMap/*_IBL.dds` files.

## WarsmashModEngine

The classic (SD) per-vertex light system in the same file (`SimpleDiffuseShaderPipeline`, `modelLightFactor`)
follows Retera's WarsmashModEngine `Shaders.lightSystem` (omni falloff, directional and ambient terms, the viewer's
default sun), a project by the same author as this program.

## WhiteoutLib (BSD-3-Clause)

The Warcraft III dialect of the MDL text format written by this program (per-layer `Shader "name",`, the
`static TextureID id <= slot,` texture bindings, bare `SkinWeights` rows, the keyword order and version gates of
`Light` and `Camera`, `Glider` blocks and `SyncPoint`) follows the MDL and MDX specifications and the reference
MDL writer of FernandoS27's WhiteoutLib (https://github.com/FernandoS27/WhiteoutLib, Copyright (c) 2026 Fernando
Sahmkow, BSD-3-Clause with the same AI-derived-works notice), which were verified against the game. The
specifications are documentation; where this program's MDL reader and writer were adjusted to match them the
changes were made in this program's own code.

## Licence texts

### BSD 3-Clause License (WhiteoutFlakes, Wc3Shaders, WhiteoutLib)

```
BSD 3-Clause License

Copyright (c) 2026, Fernando Sahmkow

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

3. Neither the name of the copyright holder nor the names of its
   contributors may be used to endorse or promote products derived from
   this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
```

### AI-Derived Works Notice (WhiteoutFlakes, Wc3Shaders, WhiteoutLib)

```
AI-Derived Works Notice

For the purposes of the BSD-3-Clause License, any code generated
by automated systems, machine learning models, or AI tools that
is derived from, based on, or generated using this software as
input shall be considered a derivative work.

Redistributions of such AI-derived works must retain the original
copyright notice, license conditions, and disclaimer as required
by the BSD-3-Clause License.
```
