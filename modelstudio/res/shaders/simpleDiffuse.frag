#version 330 core

uniform sampler2D u_textureDiffuse;
uniform sampler2D u_textureNormal;
uniform sampler2D u_textureORM;
uniform sampler2D u_textureEmissive;
uniform sampler2D u_textureTeamColor;
uniform sampler2D u_textureReflections;

uniform int u_textureUsed;
uniform int u_alphaTest;
uniform int u_lightingEnabled;
uniform float u_fresnelTeamColor;
uniform vec4 u_fresnelColor;
uniform vec2 u_viewportSize;

in vec2 v_uv;
in vec4 v_color;
in vec3 v_tangentLightPos;
in vec3 v_tangentViewPos;
in vec3 v_tangentFragPos;

out vec4 FragColor;

void main() {
    vec4 color;
    if(u_textureUsed != 0) {
        vec4 texel = texture2D(u_textureDiffuse, v_uv);
        color = texel * v_color;
    } else {
        color = v_color;
    }
    if(u_alphaTest != 0 && color.a < 0.75) {
        discard;
    }
    FragColor = color;
}