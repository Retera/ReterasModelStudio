#version 330 core

layout (location = 0) in vec4 a_position;
layout (location = 1) in vec4 a_normal;
layout (location = 2) in vec2 a_uv;
layout (location = 3) in vec4 a_color;
layout (location = 4) in vec4 a_tangent;
layout (location = 5) in vec4 a_fres_col;

uniform vec3 u_lightDirection;
uniform vec3 u_viewPos;
uniform mat4 u_projection;
uniform mat4 u_uvTransform;

out vec2 v_uv;
out vec4 v_color;
out vec4 v_normal;
out vec3 v_tangentLightPos;
out vec3 v_tangentViewPos;
out vec3 v_tangentFragPos;


void main() {
    gl_Position = u_projection * a_position;
    v_uv = (u_uvTransform * vec4(a_uv, 0.0, 1.0)).xy;
    v_color = a_color;
    vec3 tangent = a_tangent.xyz; // this is supposed to re-orthogonalize per https://learnopengl.com/Advanced-Lighting/Normal-Mapping although I'm undecided if wc3 needs it
    tangent = normalize(tangent - dot(tangent, a_normal.xyz) * a_normal.xyz);
    vec3 binormal = normalize(cross(a_normal.xyz, tangent) * a_tangent.w);
    mat3 mv = mat3(u_projection);
    mat3 TBN = transpose(mat3(normalize(mv*tangent), normalize(mv*binormal), normalize(mv*a_normal.xyz)));
    v_normal = u_projection * a_normal;
    v_tangentLightPos = TBN * (mv * u_lightDirection).xyz;
    v_tangentViewPos = TBN * u_viewPos;
    v_tangentFragPos = TBN * (u_projection * a_position).xyz;
}