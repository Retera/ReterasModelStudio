#version 330 core
#define PI 3.1415926538
//layout (lines) in;
layout (points) in;
layout (triangle_strip, max_vertices = 48) out;


const float MAGNITUDE = 10;

const float stemTopSize = .4f;
const float stemBotSize = .15f;

struct quad {
    vec3 p0;
    vec3 p1;
    vec3 p2;
    vec3 p3;
    vec3 norm;
};

quad backQuad = quad(
    vec3( 1,  1, -1),
    vec3(-1,  1, -1),
    vec3( 1,  1,  1),
    vec3(-1,  1,  1),
    vec3( 0,  1,  0));
quad frontQuad = quad(
    vec3(-1, -1, -1),
    vec3( 1, -1, -1),
    vec3(-1, -1,  1),
    vec3( 1, -1,  1),
    vec3( 0, -1,  0));
quad topQuad = quad(
    vec3(-1, -1,  1),
    vec3( 1, -1,  1),
    vec3(-1,  1,  1),
    vec3( 1,  1,  1),
    vec3( 0,  0,  1));
quad botQuad = quad(
    vec3(-1,  1, -1),
    vec3( 1,  1, -1),
    vec3(-1, -1, -1),
    vec3( 1, -1, -1),
    vec3( 0,  0, -1));
quad leftQuad = quad(
    vec3(-1,  1, -1),
    vec3(-1, -1, -1),
    vec3(-1,  1,  1),
    vec3(-1, -1,  1),
    vec3(-1,  0,  0));
quad rightQuad = quad(
    vec3( 1, -1, -1),
    vec3( 1,  1, -1),
    vec3( 1, -1,  1),
    vec3( 1,  1,  1),
    vec3( 1,  0,  0));

uniform vec3 u_viewPos;
uniform mat4 u_projection;
uniform mat4 u_view;
uniform vec2 scale;


in VS_OUT {
    vec4 pos;
    vec4 end;
    vec4 rot;
    vec3 scale;
    vec4 color;
    vec4 color2;
} gs_in[];

out vec4 v_color;

void EmVert(vec4 vPos) {
    gl_Position = vPos;
    EmitVertex();
}

vec3 transform(vec4 quat, vec3 v) {
    float uvx = quat.y * v.z - quat.z * v.y;
    float uvy = quat.z * v.x - quat.x * v.z;
    float uvz = quat.x * v.y - quat.y * v.x;
    float uuvx = quat.y * uvz - quat.z * uvy;
    float uuvy = quat.z * uvx - quat.x * uvz;
    float uuvz = quat.x * uvy - quat.y * uvx;
    float w2 = quat.w * 2;

    float newX = v.x + (uvx * w2) + (uuvx * 2);
    float newY = v.y + (uvy * w2) + (uuvy * 2);
    float newZ = v.z + (uvz * w2) + (uuvz * 2);

    return vec3(newX, newY, newZ);
}

vec4 getFromAxisAngle(vec3 a, float angle) {
    float halfAngle = angle / 2.0;
    float sinOfHalfAngle = sin(halfAngle);
    vec4 quat = vec4(0);
    quat.x = a.x * sinOfHalfAngle;
    quat.y = a.y * sinOfHalfAngle;
    quat.z = a.z * sinOfHalfAngle;
    quat.w = cos(halfAngle);
    return quat;
}
vec4 mulQuat(vec4 a, vec4 b) {
    float newX = (b.x * a.w) + (b.w * a.x) + (b.y * a.z) - (b.z * a.y);
    float newY = (b.y * a.w) + (b.w * a.y) + (b.z * a.x) - (b.x * a.z);
    float newZ = (b.z * a.w) + (b.w * a.z) + (b.x * a.y) - (b.y * a.x);
    float newW = (b.w * a.w) - (b.x * a.x) - (b.y * a.y) - (b.z * a.z);
    return vec4(newX, newY, newZ, newW);
}

vec4 getRot(vec4 pos, vec4 parent){
    vec4 diff = vec4(parent) - pos;
    if (diff.x == 0 && diff.y == 0 && diff.z == 0) {
        diff.xyzw = vec4(0, 0, 0.01, 0);
    }
    vec3 tempVec = normalize(cross(vec3(0, 0, 1), diff.xyz));

    float angleToZ = acos(diff.z / length(diff)) - PI / 2.0;;
    vec4 difRotR = normalize(getFromAxisAngle(tempVec, angleToZ));
    vec4 rot90 = normalize(getFromAxisAngle(tempVec, PI / 2.0));
    return normalize(mulQuat(difRotR,rot90));

}

void DrawQuadAt(vec4 p1, quad q, vec4 rot, vec3 scale, vec4 color) {
    mat4 ugg = u_projection * u_view;
    vec4 quadNormal = u_view * vec4(transform(rot, q.norm.xyz), 0);
    float shadowThing = (3 + dot(quadNormal,vec4(0,0,1,0)))/4.0;

    v_color.xyz = color.xyz * shadowThing;

    EmVert(ugg * (p1 + vec4(transform(rot, q.p0*scale), 0.0)));    // 1:bottom-left
    EmVert(ugg * (p1 + vec4(transform(rot, q.p1*scale), 0.0)));    // 2:bottom-right
    EmVert(ugg * (p1 + vec4(transform(rot, q.p2*scale), 0.0)));    // 3:top-left
    EmVert(ugg * (p1 + vec4(transform(rot, q.p3*scale), 0.0)));    // 4:top-right

    EndPrimitive();
}

void DrawLongQuadBetween(vec4 p1, vec4 p2, quad q, vec4 rot, vec3 scale1, vec3 scale2, vec4 color) {
    mat4 ugg = u_projection * u_view;
    vec4 quadNormal = u_view * vec4(transform(rot, q.norm.xyz), 0);
    float shadowThing = (3 + dot(quadNormal,vec4(0,0,1,0)))/4.0;

    v_color.xyz = color.xyz * shadowThing;

    EmVert(ugg * (p2 + vec4(transform(rot, q.p0*scale2), 0.0)));    // 1:bottom-left
    EmVert(ugg * (p2 + vec4(transform(rot, q.p1*scale2), 0.0)));    // 2:bottom-right
    EmVert(ugg * (p1 + vec4(transform(rot, q.p2*scale1), 0.0)));    // 3:top-left
    EmVert(ugg * (p1 + vec4(transform(rot, q.p3*scale1), 0.0)));    // 4:top-right

    EndPrimitive();
}

void DrawCube(vec4 p1, vec4 rot, vec3 scale, vec4 color) {
    DrawQuadAt(p1, frontQuad, rot, scale, color);
    DrawQuadAt(p1, backQuad, rot, scale, color);
    DrawQuadAt(p1, topQuad, rot, scale, color);
    DrawQuadAt(p1, botQuad, rot, scale, color);
    DrawQuadAt(p1, rightQuad, rot, scale, color);
    DrawQuadAt(p1, leftQuad, rot, scale, color);
}

void DrawStick(vec4 p1, vec4 p2, vec4 rot1, vec3 scale, vec4 color) {
    vec4 rot = getRot(p1, p2);
    vec3 scale1 = scale * .2;
    vec3 scale2 = scale * .4;
    DrawQuadAt(p1, topQuad, rot, scale1, color);
    DrawQuadAt(p2, botQuad, rot, scale2, color);
    DrawLongQuadBetween(p1, p2, frontQuad, rot, scale1, scale2, color);
    DrawLongQuadBetween(p1, p2, backQuad, rot, scale1, scale2, color);
    DrawLongQuadBetween(p1, p2, rightQuad, rot, scale1, scale2, color);
    DrawLongQuadBetween(p1, p2, leftQuad, rot, scale1, scale2, color);
}

void main() {
    v_color = gs_in[0].color;
    DrawCube(gs_in[0].pos, gs_in[0].rot, gs_in[0].scale, gs_in[0].color);

    v_color = gs_in[0].color2;
    DrawStick(gs_in[0].pos, gs_in[0].end, gs_in[0].rot, gs_in[0].scale, gs_in[0].color2);
}