# Third-party notices

## WhiteoutFlakes (BSD-3-Clause)

The PopcornFX baked-effect support under `craft3data/src/com/hiveworkshop/wc3/pkb` and the field schema
tables under `craft3data/res/pkb` were written with the WhiteoutFlakes project
(https://github.com/FernandoS27/WhiteoutFlakes, Copyright (c) 2026 Fernando Sahmkow) as the reference for the
`.pkb` container layout and the HBO class schema. Portions are AI-assisted translations of that code, which its
`LICENSE-AI.md` treats as derivative works. The BSD-3-Clause text and that notice are reproduced here in
`WhiteoutFlakes-LICENSE.txt` and `WhiteoutFlakes-LICENSE-AI.md`. Neither the project nor its author endorses this
program.

## Wc3Shaders (BSD-3-Clause)

The Reforged HD lighting in `craft3data/src/com/hiveworkshop/rms/editor/render3d/NGGLDP.java`
(`HDDiffuseShaderPipeline`, the `u_lightMode == 1` branch) is a GLSL port of the HD mesh pixel shader
reconstructed in FernandoS27's Wc3Shaders (https://github.com/FernandoS27/Wc3Shaders, Copyright (c) 2026
Fernando Sahmkow): the team-colour layer, the GGX/Schlick terms with the shipped clamps, the main light and
ambient/probe mix, the clustered point-light falloff and the fresnel rim. Its BSD-3-Clause text and AI notice are
reproduced in `Wc3Shaders-LICENSE.txt` and `Wc3Shaders-LICENSE-AI.md`. The editor substitutes its environment-map
texture for the game's cube-map probe.

## WarsmashModEngine

The classic (SD) per-vertex light system in the same file (`SimpleDiffuseShaderPipeline`, `modelLightFactor`)
follows Retera's WarsmashModEngine `Shaders.lightSystem` (omni falloff, directional and ambient terms, the viewer's
default sun), a project by the same author as this program.
