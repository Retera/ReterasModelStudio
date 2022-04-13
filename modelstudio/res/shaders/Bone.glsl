#version 330 core
//layout (lines) in;
layout (points) in;
layout (triangle_strip, max_vertices = 24) out;


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
struct cube {
    vec3 frntRghtUppp;
    vec3 frntRghtDown;
    vec3 backRghtUppp;
    vec3 backRghtDown;
    vec3 frntLeftUppp;
    vec3 frntLeftDown;
    vec3 backLeftUppp;
    vec3 backLeftDown;
};
//struct cube2 {
//    quad uppp;
//    quad down;
//    quad frnt;
//    quad back;
//    quad rght;
//    quad left;
//};

cube boneMarker = cube(
    vec3( 1, -1,  1),
    vec3( 1, -1, -1),
    vec3(-1, -1,  1),
    vec3(-1, -1, -1),
    vec3( 1,  1,  1),
    vec3( 1,  1, -1),
    vec3(-1,  1,  1),
    vec3(-1,  1, -1));

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

//vec3[4] pointsStemTop = {
//    vec3( 1, -1, 1).scale(stemTopSize),
//    vec3(-1, -1, 1).scale(stemTopSize),
//    vec3( 1,  1, 1).scale(stemTopSize),
//    vec3(-1,  1, 1).scale(stemTopSize)};
//
//vec3[4] pointsStemBot = {
//    vec3( 1, -1, -1).scale(stemBotSize),
//    vec3(-1, -1, -1).scale(stemBotSize),
//    vec3( 1,  1, -1).scale(stemBotSize),
//    vec3(-1,  1, -1).scale(stemBotSize)};

uniform mat4 u_projection;
uniform vec2 scale;

//uniform vec2 start;
//uniform vec2 end;

in VS_OUT {
    vec4 pos;
    vec4 rot;
    vec4 color;
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

//quat GetRot(vec4 pos, vec4 parent){
//    vec4 diff = vec4(parent).sub(pos);
//    if (diff.x == 0 && diff.y == 0 && diff.z == 0) {
//        diff.set(0, 0, 0.01, 0);
//    }
//    vec4 tempVec = vec4(0, 0, 1, 0).cross(diffVec).normalize();
//
//
//    difRotR.setFromAxisAngle(tempVec, (float) (diffVec.getAngleToZaxis())).normalize();
//rot90.setFromAxisAngle(tempVec, (float) (Math.PI / 2)).normalize();
//difRotR.mul(rot90).normalize();
//
//}
//
//BoneRenderThingBuf transform2(Vec3 p1, Vec3 p2, float nodeSize) {
//    diffVec.set(p2).sub(p1);
//    //		System.out.println("dist to par: " + diffVec.length());
//    if (diffVec.x == 0 && diffVec.y == 0 && diffVec.z == 0) {
//        diffVec.set(Vec3.Z_AXIS).scale(0.01f);
//    }
//    //		else {
//    //			nodeSize = diffVec.length() / 10;
//    //		}
//    tempVec.set(Vec3.Z_AXIS).cross(diffVec).normalize();
//
//    difRotR.setFromAxisAngle(tempVec, (float) (diffVec.getAngleToZaxis())).normalize();
//rot90.setFromAxisAngle(tempVec, (float) (Math.PI / 2)).normalize();
//difRotR.mul(rot90).normalize();
//
//transform1(difRotR, p1, p2, nodeSize);
////		transform(difRotR, p1, p2);
//return this;
//}
//
//BoneRenderThingBuf transform1(Quat rot, Vec3 p1, Vec3 p2, float scale) {
//    for (int i = 0; i < pointsJoint.length; i++) {
//        renderPointsJoint[i].set(pointsJoint[i]).scale(scale).transform(rot).add(p1);
//    }
//    for (int i = 0; i < pointsStemTop.length; i++) {
//        renderPointsStemTop[i].set(pointsStemTop[i]).scale(scale).transform(rot).add(p2);
//    }
//    for (int i = 0; i < pointsStemBot.length; i++) {
//        renderPointsStemBot[i].set(pointsStemBot[i]).scale(scale).transform(rot).add(p1);
//    }
//
//    for (int i = 0; i < normals.length; i++) {
//        renderNormals[i].set(normals[i]).transform(rot);
//    }
//
//    return this;
//}

//void GenerateLine(vec4 pos, vec4 parent) {
//    EmVert(vec4(pos.x, pos.y, -1.0, 1.0));    // 1:bottom-left
////    EmVert(vec4(parent.x, pos.y, -1.0, 1.0));    // 2:bottom-right
//    EmVert(vec4(parent.x, parent.y, -1.0, 1.0));    // 4:top-right
////    EmVert(vec4(pos.x, parent.y, -1.0, 1.0));    // 3:top-left
////    EmVert(vec4(pos.x, pos.y, -1.0, 1.0));    // 1:bottom-left
//
//    EndPrimitive();
//}

void DrawQuadAt(vec4 p1, quad q, vec4 rot) {
//    vec2 mag = vec2(MAGNITUDE, MAGNITUDE)*scale;

    EmVert(u_projection * (p1 + vec4(transform1(rot, q.p0), 0.0)));    // 1:bottom-left
    EmVert(u_projection * (p1 + vec4(transform1(rot, q.p1), 0.0)));    // 2:bottom-right
    EmVert(u_projection * (p1 + vec4(transform1(rot, q.p2), 0.0)));    // 3:top-left
    EmVert(u_projection * (p1 + vec4(transform1(rot, q.p3), 0.0)));    // 4:top-right

    EndPrimitive();
}

void DrawPoint(vec4 p1) {
    vec2 mag = vec2(MAGNITUDE, MAGNITUDE)*scale;

    EmVert(p1 + vec4(-mag.x, -mag.y, 0.0, 0.0));    // 1:bottom-left
    EmVert(p1 + vec4( mag.x, -mag.y, 0.0, 0.0));    // 2:bottom-right
    EmVert(p1 + vec4(-mag.x,  mag.y, 0.0, 0.0));    // 3:top-left
    EmVert(p1 + vec4( mag.x,  mag.y, 0.0, 0.0));    // 4:top-right

    EndPrimitive();
}
void DrawCube(vec4 p1, vec4 rot) {
    DrawQuadAt(p1, frontQuad, rot);
    DrawQuadAt(p1, backQuad, rot);
    DrawQuadAt(p1, topQuad, rot);
    DrawQuadAt(p1, botQuad, rot);
    DrawQuadAt(p1, rightQuad, rot);
    DrawQuadAt(p1, leftQuad, rot);
}

void main() {
    v_color = gs_in[0].color;
//    GenerateLine(gl_in[0].gl_Position, gl_in[1].gl_Position);
//    DrawPoint(u_projection * gl_Position);
//    DrawPoint(u_projection * gs_in[0].pos);
    DrawCube(gs_in[0].pos, gs_in[0].rot);
}