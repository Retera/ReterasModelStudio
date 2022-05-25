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
in vec4 v_colorAlt;
in vec4 v_normal;
in vec3 v_lightDirection;
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
        color = v_colorAlt;
    }

//    if (u_lightingEnabled != 0) {
//        vec3 lightFactorContribution = vec3(clamp(dot(v_normal.xyz, lightDirection), 0.0, 1.0));
//        if (lightFactorContribution.r > 1.0 || lightFactorContribution.g > 1.0 || lightFactorContribution.b > 1.0) {
//            lightFactorContribution = clamp(lightFactorContribution, 0.0, 1.0);
//        }
//        color.rgb = color.rgb * clamp(lightFactorContribution + vec3(0.3f, 0.3f, 0.3f), 0.0, 1.0);
//    }
//
    if(u_textureUsed != 0 && u_alphaTest != 0 && color.a < 0.75) {
        discard;
    }
    FragColor = color;
}