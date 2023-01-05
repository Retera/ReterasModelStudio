#version 330 core

layout (location = 0) in vec4 a_position;
layout (location = 1) in vec4 a_normal;
layout (location = 2) in vec2 a_uv;
layout (location = 3) in vec4 a_tangent;
layout (location = 4) in vec4 a_fres_col;
layout (location = 5) in float a_selectionStatus;

uniform vec3 u_lightDirection;
uniform vec3 u_viewPos;
uniform mat4 u_projection;
uniform mat4 u_view;
uniform mat4 u_uvTransform;
uniform vec4 u_geosetColor;

uniform int u_lightingEnabled;

uniform vec4 u_vertColors[4];
// holds the vertex colors for [0:highlighted, 1:selected, 2:editable, 3:vissible (not editable)]

out vec2 v_uv;
out vec4 v_color;
out vec4 v_colorAlt;
out vec4 v_normal;
out vec3 v_lightDirection;
out vec3 v_tangentLightPos;
out vec3 v_tangentViewPos;
out vec3 v_tangentFragPos;


void main() {
    gl_Position = u_projection * u_view * a_position;
    v_normal = u_projection * u_view * a_normal;
    v_uv = (u_uvTransform * vec4(a_uv, 0.0, 1.0)).xy;


    vec4 faceNormal = u_view * vec4(a_normal.xyz, 0);
    vec4 viewPos = vec4(0,0,1,0);
    float shadowThing = (3 + dot(faceNormal.xyzw,viewPos.xyzw))/4.0;

    v_color = u_geosetColor;
    v_colorAlt = u_vertColors[int(a_selectionStatus)];
    v_colorAlt.xyz = v_colorAlt.xyz * shadowThing;

    vec3 tangent = a_tangent.xyz; // this is supposed to re-orthogonalize per https://learnopengl.com/Advanced-Lighting/Normal-Mapping although I'm undecided if wc3 needs it
    tangent = normalize(tangent - dot(tangent, a_normal.xyz) * a_normal.xyz);

    vec3 binormal = normalize(cross(a_normal.xyz, tangent) * a_tangent.w);
    mat3 mv = mat3(u_projection * u_view);
    mat3 TBN = transpose(mat3(normalize(mv*tangent), normalize(mv*binormal), normalize(mv*a_normal.xyz)));
    v_tangentLightPos = TBN * (mv * u_lightDirection).xyz;
    v_tangentViewPos = TBN * u_viewPos;
    v_tangentFragPos = TBN * (u_projection * u_view * a_position).xyz;
}