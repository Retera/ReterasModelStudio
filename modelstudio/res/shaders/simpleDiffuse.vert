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
    //    vec4 faceNormal = normalize(u_view * a_normal);
    //    vec4 faceNormal = a_normal * u_view;
    vec4 faceNormal = u_view * vec4(a_normal.xyz, 0);
    //    vec4 viewPos = normalize(vec4(0,-1,0,0)*u_view);
    //    vec4 viewPos = normalize(u_projection * u_view * vec4(1,0,0, 0));
    //    vec4 viewPos = u_projection * vec4(-1,0,0, 0);
    vec4 viewPos = vec4(0,0,1,0);
    vec4 v = vec4(0, 1, 0, 1);
    //    vec4 viewPos = (u_view * vec4(0,0,0, 0) + vec4(0,1,0, 0)) * u_view;
    vec4 viewPos1 = (vec4(0,0,0, 1) * u_view);
    vec4 viewPos2 = (vec4(1,0,0, 1) * u_view);
    //    vec4 viewPos = (vec4(1,0,0, 0) * u_view) - (vec4(0,0,0, 0) * u_view);
    vec4 normal = u_projection * u_view * a_normal;
    v_lightDirection = normalize((vec4(u_lightDirection, 1) * u_projection * u_view).xyz);
    //    v_uv = a_uv;
    v_uv = (u_uvTransform * vec4(a_uv, 0.0, 1.0)).xy;
    v_color = u_geosetColor;
    v_normal = normal;
    float shadowThing = (3 + dot(faceNormal.xyzw,viewPos.xyzw))/4.0;
    //    float shadowThing = (0 + dot(a_normal.xyz,viewPos.xyz))/1.0;
    v_color.rgb = v_color.rgb;
    if (u_lightingEnabled != 0) {
        //        vec3 lightFactorContribution = vec3(clamp(dot(normal.xyz, v_lightDirection), 0.0, 1.0));
        //        if (lightFactorContribution.r > 1.0 || lightFactorContribution.g > 1.0 || lightFactorContribution.b > 1.0) {
        //            lightFactorContribution = clamp(lightFactorContribution, 0.0, 1.0);
        //        }
        //        v_color.rgb = v_color.rgb * clamp(lightFactorContribution + vec3(0.3f, 0.3f, 0.3f), 0.0, 1.0);
        //        v_color.rgb = v_color.rgb * clamp(lightFactorContribution + vec3(0.3f, 0.3f, 0.3f), 0.0, 1.0);
    }
    vec4 vertColors[4];

    //    vertColors[0] = vec4(1,1,0,1);
    //    vertColors[1] = vec4(1,0,0,1);
    //    vertColors[2] = vec4(0,1,0,1);
    //    vertColors[3] = vec4(0,0,1,1);
//    v_colorAlt = u_vertColors[int(a_selectionStatus)] * shadowThing;
    v_colorAlt = u_vertColors[int(a_selectionStatus)];
    v_colorAlt.xyz = v_colorAlt.xyz * shadowThing;
    vec4 c = vec4(1,1,1,1)*.5 + (faceNormal*.5);
    //	vec4 c = vec4(1,1,1,1)*.5 + (viewPos*.4);
    //    v_colorAlt = vec4(c.xyz, 1);

    //    v_colorAlt = vertColors[int(a_selectionStatus)];
}