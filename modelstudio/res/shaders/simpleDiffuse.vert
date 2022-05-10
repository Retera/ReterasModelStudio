#version 330 core

layout (location = 0) in vec4 a_position;
layout (location = 1) in vec4 a_normal;
layout (location = 2) in vec2 a_uv;
layout (location = 3) in vec4 a_tangent;
layout (location = 4) in float a_selectionStatus;

uniform vec3 u_lightDirection;
uniform vec3 u_viewPos;
uniform mat4 u_projection;
uniform mat4 u_uvTransform;
uniform vec4 u_geosetColor;

uniform int u_lightingEnabled;

uniform vec4 u_vertColors[4];

out vec2 v_uv;
out vec4 v_color;
out vec4 v_normal;
out vec3 v_lightDirection;
out vec3 v_tangentLightPos;
out vec3 v_tangentViewPos;
out vec3 v_tangentFragPos;

void main() {
    gl_Position = u_projection * a_position;
    vec4 normal = u_projection * a_normal;
    v_lightDirection = normalize((vec4(u_lightDirection, 1) * u_projection).xyz);
//    v_uv = a_uv;
    v_uv = (u_uvTransform * vec4(a_uv, 0.0, 1.0)).xy;
    v_color = u_geosetColor;
    v_normal = normal;
    if (u_lightingEnabled != 0) {
        vec3 lightFactorContribution = vec3(clamp(dot(normal.xyz, v_lightDirection), 0.0, 1.0));
        if (lightFactorContribution.r > 1.0 || lightFactorContribution.g > 1.0 || lightFactorContribution.b > 1.0) {
            lightFactorContribution = clamp(lightFactorContribution, 0.0, 1.0);
        }
        v_color.rgb = v_color.rgb * clamp(lightFactorContribution + vec3(0.3f, 0.3f, 0.3f), 0.0, 1.0);
    } else {
        v_color = u_vertColors[int(a_selectionStatus)];
    }
}