//#version 330 core
//
//uniform sampler2D u_textureDiffuse;
//uniform sampler2D u_textureNormal;
//uniform sampler2D u_textureORM;
//uniform sampler2D u_textureEmissive;
//uniform sampler2D u_textureTeamColor;
//uniform sampler2D u_textureReflections;
//
//uniform int u_textureUsed;
//uniform int u_alphaTest;
//uniform int u_lightingEnabled;
//uniform float u_fresnelTeamColor;
//uniform vec4 u_fresnelColor;
//uniform vec2 u_viewportSize;
//
//in vec2 v_uv;
//in vec4 v_color;
//in vec3 v_tangentLightPos;
//in vec3 v_tangentViewPos;
//in vec3 v_tangentFragPos;
//
//out vec4 FragColor;
//
//void main() {
//    vec4 color;
//    vec4 ormTexel = texture2D(u_textureORM, v_uv);
//    vec4 teamColorTexel = texture2D(u_textureTeamColor, v_uv);
//
//    if (u_textureUsed != 0) {
//        vec4 texel = texture2D(u_textureDiffuse, v_uv);
//        color = vec4(texel.rgb * ((1.0 - ormTexel.a) + (teamColorTexel.rgb * ormTexel.a)), texel.a) * v_color;
//    } else {
//        color = v_color;
//    }
//
//    if (u_alphaTest != 0 && color.a < 0.75) {
//        discard;
//    }
//
//    if (u_lightingEnabled != 0) {
//        vec2 normalXY = texture2D(u_textureNormal, v_uv).xy * 2.0 - 1.0;
//        vec3 normal = vec3(normalXY, sqrt(1.0 - dot(normalXY, normalXY)));
//        vec4 emissiveTexel = texture2D(u_textureEmissive, v_uv);
//        vec4 reflectionsTexel = clamp(0.2+2.0*texture2D(u_textureReflections, vec2(gl_FragCoord.x/u_viewportSize.x, -gl_FragCoord.y/u_viewportSize.y)), 0.0, 1.0);
//        vec3 lightDir = normalize(v_tangentViewPos);
//        float cosTheta = dot(lightDir, normal);
//        float lambertFactor = clamp(cosTheta, 0.0, 1.0);
//        vec3 diffuse = (clamp(lambertFactor * (ormTexel.r) + 0.1, 0.0, 1.0)) * color.xyz;
//        vec3 viewDir = normalize(v_tangentViewPos - v_tangentFragPos);
//        vec3 reflectDir = reflect(-lightDir, normal);
//        vec3 halfwayDir = normalize(lightDir + viewDir);
//        float spec = pow(max(dot(normal, halfwayDir), 0.0), 32.0);
//        vec3 specular = vec3(max(ormTexel.b-0.5, 0.0)) * spec /* * reflectionsTexel.xyz*/;
//        vec3 fresnelColor = vec3(u_fresnelColor.rgb * (1.0 - u_fresnelTeamColor) + teamColorTexel.rgb *  u_fresnelTeamColor) * v_color.rgb;
//        vec3 fresnel = fresnelColor*pow(1.0 - cosTheta, 1.0)*u_fresnelColor.a;
//        FragColor = vec4(emissiveTexel.xyz + specular + diffuse + fresnel, color.a);
//    } else {
//        FragColor = color;
//    }
//}
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

//void main() {
//    vec4 color;
//    vec4 ormTexel = texture2D(u_textureORM, v_uv);
//    vec4 teamColorTexel = texture2D(u_textureTeamColor, v_uv);
//    if(u_textureUsed != 0) {
//        vec4 texel = texture2D(u_textureDiffuse, v_uv);
////        color = vec4(texel.rgb * ((1.0 - ormTexel.a) + (teamColorTexel.rgb * ormTexel.a)), texel.a) * v_color;
//        color = vec4(texel.rgba);
//    } else {
//        color = v_color;
//    }
//    if(u_alphaTest != 0 && color.a < 0.75) {
//        discard;
//    }
//    if(u_lightingEnabled != 0) {
//        vec2 normalXY = texture2D(u_textureNormal, v_uv).xy * 2.0 - 1.0;
//        vec3 normal = vec3(normalXY, sqrt(1.0 - dot(normalXY,normalXY)));
//        vec4 emissiveTexel = texture2D(u_textureEmissive, v_uv);
////        vec4 reflectionsTexel = clamp(0.2+2.0*texture2D(u_textureReflections, vec2(gl_FragCoord.x/u_viewportSize.x, -gl_FragCoord.y/u_viewportSize.y)), 0.0, 1.0);
//        vec3 lightDir = normalize(v_tangentViewPos);
//        float cosTheta = dot(lightDir, normal);
//        float lambertFactor = clamp(cosTheta, 0.0, 1.0);
//        vec3 diffuse = (clamp(lambertFactor * (ormTexel.r) + 0.1, 0.0, 1.0)) * color.xyz;
//        vec3 viewDir = normalize(v_tangentViewPos - v_tangentFragPos);
//        vec3 reflectDir = reflect(-lightDir, normal);
//        vec3 halfwayDir = normalize(lightDir + viewDir);
//        float spec = pow(max(dot(normal, halfwayDir), 0.0), 32.0);
//        vec3 specular = vec3(max(ormTexel.b-0.5, 0.0)) * spec /* * reflectionsTexel.xyz*/;
//        vec3 fresnelColor = vec3(u_fresnelColor.rgb * (1.0 - u_fresnelTeamColor) + teamColorTexel.rgb *  u_fresnelTeamColor) * v_color.rgb;
////        vec3 fresnelColor = vec3(.1,.1,.1) * v_color.rgb;
////        vec3 fresnel = fresnelColor*pow(1.0 - cosTheta, 1.0)*u_fresnelColor.a;
//        vec3 fresnel = fresnelColor*pow(1.0 - lambertFactor, 1.0)*u_fresnelColor.a;
////        vec3 fresnel = fresnelColor*pow(clamp(1.0 - cosTheta, 0.0, 1.0), 1.0)*u_fresnelColor.a;
////        FragColor = vec4(emissiveTexel.xyz + specular + diffuse + fresnel, color.a);
//        FragColor = vec4(emissiveTexel.rgb + specular + diffuse + fresnel, color.a);
//    } else {
//        FragColor = color;
//    }
//}

vec3 getSpecular(vec4 ormTexel, vec3 normal, vec3 lightDir){
    vec3 viewDir = normalize(v_tangentViewPos - v_tangentFragPos);
    vec3 reflectDir = reflect(-lightDir, normal);
    vec3 halfwayDir = normalize(lightDir + viewDir);
    float spec = pow(max(dot(normal, halfwayDir), 0.0), 32.0);
//    vec3 specular = vec3(max(ormTexel.b-0.5, 0.0)) * spec /* * reflectionsTexel.xyz*/;
    vec3 specular = vec3(max(ormTexel.b, 0.0)) * spec /* * reflectionsTexel.xyz*/;
    return specular;
}

float ShadowCalculation(vec4 ormTexel, vec3 fragPosLightSpace){
//    vec3 projCoords = fragPosLightSpace.xyz / fragPosLightSpace.w;
    vec3 projCoords = fragPosLightSpace.xyz;
    projCoords = projCoords * 0.5 + 0.5;
    float closestDepth = ormTexel.r;
    float currentDepth = projCoords.z;
    float shadow = currentDepth > closestDepth  ? 1.0 : 0.0;

    return shadow;
}
void main() {
    vec4 color;
    vec4 ormTexel = texture2D(u_textureORM, v_uv);
    vec4 teamColorTexel = texture2D(u_textureTeamColor, v_uv);
    if(u_textureUsed != 0) {
        vec4 texel = texture2D(u_textureDiffuse, v_uv);
//        color = vec4(texel.rgb * ((1.0 - ormTexel.a) + (teamColorTexel.rgb * ormTexel.a)), texel.a) * v_color;
        color = vec4(texel.rgba);
    } else {
        color = v_color;
//        color = vec4(1,0,1,1);
    }
    if(u_alphaTest != 0 && color.a < 0.75) {
        discard;
    }
    if(u_lightingEnabled != 0) {
        vec2 normalXY = texture2D(u_textureNormal, v_uv).xy * 2.0 - 1.0;
        vec3 normal = vec3(normalXY, sqrt(1.0 - dot(normalXY,normalXY)));
        vec4 emissiveTexel = texture2D(u_textureEmissive, v_uv);
//        vec4 reflectionsTexel = clamp(0.2+2.0*texture2D(u_textureReflections, vec2(gl_FragCoord.x/u_viewportSize.x, -gl_FragCoord.y/u_viewportSize.y)), 0.0, 1.0);
        vec3 lightDir = normalize(v_tangentViewPos);
        float cosTheta = (dot(lightDir, normal), 1.0);
        float lambertFactor = clamp(cosTheta, 0.0, 1.0);
        vec3 diffuse = (clamp(lambertFactor * (ormTexel.r) + 0.1, 0.0, 1.0)) * color.xyz;
//        vec3 diffuse = cosTheta * color.rgb * ((ormTexel.r));

//        vec3 viewDir = normalize(v_tangentViewPos - v_tangentFragPos);
//        vec3 reflectDir = reflect(-lightDir, normal);
//        vec3 halfwayDir = normalize(lightDir + viewDir);
//        float spec = pow(max(dot(normal, halfwayDir), 0.0), 32.0);
        vec3 specular = getSpecular(ormTexel, normal, lightDir);
        vec3 fresnelColor = vec3(u_fresnelColor.rgb * (1.0 - u_fresnelTeamColor) + teamColorTexel.rgb *  u_fresnelTeamColor) * v_color.rgb;
//        vec3 fresnelColor = vec3(.1,.1,.1) * v_color.rgb;
//        vec3 fresnel = fresnelColor*pow(1.0 - cosTheta, 1.0)*u_fresnelColor.a;
        vec3 fresnel = fresnelColor*pow(1.0 - lambertFactor, 1.0)*u_fresnelColor.a;
//        vec3 fresnel = fresnelColor*pow(clamp(1.0 - cosTheta, 0.0, 1.0), 1.0)*u_fresnelColor.a;
//        FragColor = vec4(emissiveTexel.xyz + specular + diffuse + fresnel, color.a);

//        float shadow = ShadowCalculation(ormTexel, lightDir);
        FragColor = vec4(emissiveTexel.rgb + specular + diffuse + fresnel, color.a);
//        FragColor = vec4(ormTexel.rgb, 1);
    } else {
        FragColor = color;
    }

}