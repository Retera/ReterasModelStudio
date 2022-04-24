#version 330 core

layout (location = 0) in vec4 a_position;
layout (location = 1) in vec4 a_normal;
layout (location = 2) in vec2 a_uv;
layout (location = 3) in vec4 a_color;
layout (location = 4) in vec4 a_tangent;

uniform vec3 u_lightDirection;
uniform vec3 u_viewPos;
uniform mat4 u_projection;

uniform int u_lightingEnabled;

out vec2 v_uv;
out vec4 v_color;
out vec3 v_tangentLightPos;
out vec3 v_tangentViewPos;
out vec3 v_tangentFragPos;

void main() {
    gl_Position = u_projection * a_position;
    vec4 normal = u_projection * a_normal;
    vec3 lightDirection = normalize((vec4(u_lightDirection, 1) * u_projection).xyz);
    v_uv = a_uv;
    v_color = a_color;
    if (u_lightingEnabled != 0) {
        vec3 lightFactorContribution = vec3(clamp(dot(normal.xyz, lightDirection), 0.0, 1.0));
        if (lightFactorContribution.r > 1.0 || lightFactorContribution.g > 1.0 || lightFactorContribution.b > 1.0) {
            lightFactorContribution = clamp(lightFactorContribution, 0.0, 1.0);
        }
        v_color.rgb = v_color.rgb * clamp(lightFactorContribution + vec3(0.3f, 0.3f, 0.3f), 0.0, 1.0);
    }
}