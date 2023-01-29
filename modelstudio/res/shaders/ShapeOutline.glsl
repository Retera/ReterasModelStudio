#version 330 core
#define PI 3.1415926538
layout (points) in;
layout (line_strip, max_vertices = 64) out;


uniform vec3 u_viewPos;
uniform mat4 u_projection;
uniform mat4 u_view;
uniform vec2 scale;
uniform float u_size;
uniform int show_node_dir;

in VS_OUT {
    vec4 pos;
    vec4 norm;
    vec4 color1;
    vec4 color2;
    vec4 vert1;
    vec4 vert2;
    vec2 shape_radius;
} gs_in[];


out vec4 v_color;

void EmVert(vec4 vPos) {
    gl_Position = vPos;
    EmitVertex();
}
vec4 transform(vec4 quat, vec4 v) {
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

    return vec4(newX, newY, newZ, 0);
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

void DrawSphereAt(vec4 pos, vec4 vert1, vec4 vert2, float radius){
    vec4 dir1 = vec4(radius, 0, 0, 0);
    vec4 dir2 = vec4(0, radius, 0, 0);
    vec4 emPos;
    float step = PI/16.0;
    for (float i = 0; i < 2*PI; i += step) {
        emPos = (u_view * pos + dir1*cos(i) + dir2*sin(i));
        EmVert(u_projection * emPos);
    }
    emPos = u_view * pos + dir1*cos(0) + dir2*sin(0);
    EmVert(u_projection * emPos);
    EndPrimitive();

}

void DrawCylinderAt(vec4 pos, vec4 vert1, vec4 col1, vec4 vert2, vec4 col2, float radius){
    vec4 dir1 = vec4(radius, 0, 0, 0);
    vec4 dir2 = vec4(0, radius, 0, 0);


    vec4 rot = getRot(vert1, vert2);
    float step = PI/4.0;
    float step05 = PI/8.0;
    vec4 emPos;
    mat4 viewProj = u_projection * u_view;
    for (float i = 0; i < 2*PI; i += step) {
        vec4 circ1 = transform(rot, dir1*cos(i) + dir2*sin(i));
        vec4 circ05 = transform(rot, dir1*cos(i+step05) + dir2*sin(i+step05));
        vec4 circ2 = transform(rot, dir1*cos(i+step) + dir2*sin(i+step));

        v_color = col1;
        EmVert(viewProj * (pos + vert1 + circ1));
        EmVert(viewProj * (pos + vert1 + circ05));
        EmVert(viewProj * (pos + vert1 + circ2));

        v_color = col2;
        EmVert(viewProj * (pos + vert2 + circ2));
        EmVert(viewProj * (pos + vert2 + circ05));
        EmVert(viewProj * (pos + vert2 + circ1));

        EndPrimitive();
    }

}
void DrawBoxAt(vec4 pos, vec4 vert1, vec4 vert2){

    vec4 emPos;

    vec4 v1 = vert1;
    vec4 v2 = vert2;

    mat4 viewProj = u_projection * u_view;

    EmVert(viewProj * (pos + vec4(v1.x, v1.y, v1.z, 0)));
    EmVert(viewProj * (pos + vec4(v1.x, v2.y, v1.z, 0)));
    EmVert(viewProj * (pos + vec4(v1.x, v2.y, v2.z, 0)));
    EmVert(viewProj * (pos + vec4(v1.x, v1.y, v2.z, 0)));
    EndPrimitive();

    EmVert(viewProj * (pos + vec4(v2.x, v1.y, v2.z, 0)));
    EmVert(viewProj * (pos + vec4(v1.x, v1.y, v2.z, 0)));
    EmVert(viewProj * (pos + vec4(v1.x, v1.y, v1.z, 0)));
    EmVert(viewProj * (pos + vec4(v2.x, v1.y, v1.z, 0)));
    EndPrimitive();

    EmVert(viewProj * (pos + vec4(v2.x, v2.y, v2.z, 0)));
    EmVert(viewProj * (pos + vec4(v2.x, v1.y, v2.z, 0)));
    EmVert(viewProj * (pos + vec4(v2.x, v1.y, v1.z, 0)));
    EmVert(viewProj * (pos + vec4(v2.x, v2.y, v1.z, 0)));
    EndPrimitive();


    EmVert(viewProj * (pos + vec4(v1.x, v2.y, v2.z, 0)));
    EmVert(viewProj * (pos + vec4(v2.x, v2.y, v2.z, 0)));
    EmVert(viewProj * (pos + vec4(v2.x, v2.y, v1.z, 0)));
    EmVert(viewProj * (pos + vec4(v1.x, v2.y, v1.z, 0)));
    EndPrimitive();

}
void DrawBoxAtCol(vec4 pos, vec4 vert1, vec4 vert2){

    vec4 emPos;

    vec4 v1 = vert1;
    vec4 v2 = vert2;

    mat4 viewProj = u_projection * u_view;

    v_color = vec4(1,0,0,1);
    EmVert(viewProj * (pos + vec4(v1.x, v1.y, v1.z, 0)));
    //        v_color = vec4(0,0,0,1);
    v_color = vec4(1,0,0,1);
    EmVert(viewProj * (pos + vec4(v1.x, v2.y, v1.z, 0)));
    //        v_color = vec4(0,0,0,1);
    v_color = vec4(1,.5,0,1);
    EmVert(viewProj * (pos + vec4(v1.x, v2.y, v2.z, 0)));
    //        v_color = vec4(0,0,0,1);
    v_color = vec4(1,.5,0,1);
    EmVert(viewProj * (pos + vec4(v1.x, v1.y, v2.z, 0)));
    EndPrimitive();

    v_color = vec4(0,0,0,1);
    v_color = vec4(1,.5,0,1);
    EmVert(viewProj * (pos + vec4(v2.x, v1.y, v2.z, 0)));
    //        v_color = vec4(0,0,0,1);
    v_color = vec4(1,0,0,1);
    EmVert(viewProj * (pos + vec4(v1.x, v1.y, v2.z, 0)));
    v_color = vec4(1,0,0,1);
    EmVert(viewProj * (pos + vec4(v1.x, v1.y, v1.z, 0)));
    //        v_color = vec4(0,0,0,1);
    v_color = vec4(1,0,0,1);
    EmVert(viewProj * (pos + vec4(v2.x, v1.y, v1.z, 0)));
    EndPrimitive();

    v_color = vec4(0,1,0,1);
    EmVert(viewProj * (pos + vec4(v2.x, v2.y, v2.z, 0)));
    //        v_color = vec4(0,0,0,1);
    v_color = vec4(0,1,0,1);
    EmVert(viewProj * (pos + vec4(v2.x, v1.y, v2.z, 0)));
    //        v_color = vec4(0,0,0,1);
    v_color = vec4(1,.7,0,1);
    EmVert(viewProj * (pos + vec4(v2.x, v1.y, v1.z, 0)));
    v_color = vec4(0,0,0,1);
    v_color = vec4(1,.7,0,1);
    EmVert(viewProj * (pos + vec4(v2.x, v2.y, v1.z, 0)));
    EndPrimitive();


    //        v_color = vec4(0,0,0,1);
    v_color = vec4(0,1,0,1);
    EmVert(viewProj * (pos + vec4(v1.x, v2.y, v2.z, 0)));
    v_color = vec4(0,1,0,1);
    EmVert(viewProj * (pos + vec4(v2.x, v2.y, v2.z, 0)));
    //        v_color = vec4(0,0,0,1);
    v_color = vec4(0,1,0,1);
    EmVert(viewProj * (pos + vec4(v2.x, v2.y, v1.z, 0)));
    v_color = vec4(0,0,0,1);
    v_color = vec4(1,.5,0,1);
    EmVert(viewProj * (pos + vec4(v1.x, v2.y, v1.z, 0)));
    EndPrimitive();

}
void DrawBoxAtCol2(vec4 pos, vec4 vert1, vec4 col1, vec4 vert2, vec4 col2){

    vec4 emPos;

    vec4 v1 = vert1;
    vec4 v2 = vert2;

    mat4 viewProj = u_projection * u_view;

    v_color = col1;
    EmVert(viewProj * (pos + vec4(v1.x, v1.y, v1.z, 0)));
//    v_color = col1;
    EmVert(viewProj * (pos + vec4(v1.x, v2.y, v1.z, 0)));
    v_color = col2;
    EmVert(viewProj * (pos + vec4(v1.x, v2.y, v2.z, 0)));
    v_color = col1;
    EmVert(viewProj * (pos + vec4(v1.x, v1.y, v2.z, 0)));
    EndPrimitive();

    v_color = col2;
    EmVert(viewProj * (pos + vec4(v2.x, v1.y, v2.z, 0)));
    v_color = col1;
    EmVert(viewProj * (pos + vec4(v1.x, v1.y, v2.z, 0)));
//    v_color = col1;
    EmVert(viewProj * (pos + vec4(v1.x, v1.y, v1.z, 0)));
//    v_color = col1;
    EmVert(viewProj * (pos + vec4(v2.x, v1.y, v1.z, 0)));
    EndPrimitive();

    v_color = col2;
    EmVert(viewProj * (pos + vec4(v2.x, v2.y, v2.z, 0)));
//    v_color = col2;
    EmVert(viewProj * (pos + vec4(v2.x, v1.y, v2.z, 0)));
    v_color = col1;
    EmVert(viewProj * (pos + vec4(v2.x, v1.y, v1.z, 0)));
    v_color = col2;
    EmVert(viewProj * (pos + vec4(v2.x, v2.y, v1.z, 0)));
    EndPrimitive();


    v_color = col2;
    EmVert(viewProj * (pos + vec4(v1.x, v2.y, v2.z, 0)));
//    v_color = col2;
    EmVert(viewProj * (pos + vec4(v2.x, v2.y, v2.z, 0)));
//    v_color = col2;
    EmVert(viewProj * (pos + vec4(v2.x, v2.y, v1.z, 0)));
    v_color = col1;
    EmVert(viewProj * (pos + vec4(v1.x, v2.y, v1.z, 0)));
    EndPrimitive();

}
void DrawPlaneAt(vec4 pos, vec4 vert1, vec4 vert2){

    vec4 emPos;

    vec4 v1 = vert1;
    vec4 v2 = vert2;

    mat4 viewProj = u_projection * u_view;

    // Line to both
    v_color = vec4(1, 0, 0, 1);
    EmVert(viewProj * (pos));
    EmVert(viewProj * (pos + v1));
    EndPrimitive();

    EmVert(viewProj * (pos));
    EmVert(viewProj * (pos + v2));
    EndPrimitive();

    //    // v1 norm, v2 size
    //    v_color = vec4(0, 1, 0, 1);
    //    vec4 rot = getRot(v1, vec4(cross(normalize(v1.xyz), normalize(v2.xyz)), 0));
    //
    //    vec4 dirX = transform(rot, vec4(v2.x, 0, 0, 0));
    //    vec4 dirY = transform(rot, vec4(0, v2.y, 0, 0));
    //    vec4 dirZ = transform(rot, vec4(0, 0, v2.z, 0));
    //    vec4 dir = transform(rot, v2);
    //
    //    EmVert(viewProj * (pos + transform(rot, vec4( dir.x,  dir.y, dir.z, 0))));
    //    EmVert(viewProj * (pos + transform(rot, vec4( dir.x, -dir.y, dir.z, 0))));
    //    EmVert(viewProj * (pos + transform(rot, vec4(-dir.x, -dir.y, dir.z, 0))));
    //    EmVert(viewProj * (pos + transform(rot, vec4(-dir.x,  dir.y, dir.z, 0))));
    //    EmVert(viewProj * (pos + transform(rot, vec4( dir.x, dir.y, dir.z, 0))));
    //    EndPrimitive();
    //
    //    // v1 norm, v2 size alt
    //    v_color = vec4(1, 1, 0, 1);
    //    vec4 p1 = v2;
    //    vec4 p2 = -v2;
    //    vec4 p3 = -v2;
    //    vec4 p4 = -v2;
    //    EmVert(viewProj * (pos + transform(rot, p1)));
    //    EmVert(viewProj * (pos + transform(rot, p2)));
    //    EmVert(viewProj * (pos + transform(rot, p3)));
    //    EmVert(viewProj * (pos + transform(rot, p4)));
    //    EmVert(viewProj * (pos + transform(rot, p1)));
    //    EndPrimitive();
    //
    //
    //    // v1 -> v2 -> -v2 -> -v1
    //    v_color = vec4(0, 0, 1, 1);
    //    EmVert(viewProj * (pos + v1));
    //    EmVert(viewProj * (pos - v2));
    //    EmVert(viewProj * (pos - v1));
    //    EmVert(viewProj * (pos + v2));
    //    EmVert(viewProj * (pos + v1));
    //    EndPrimitive();

    //
    //    EmVert(viewProj * (pos + vec4(v2.x, v2.y, v2.z, 0)));
    //    EmVert(viewProj * (pos + vec4(v2.x, v1.y, v2.z, 0)));
    //    EmVert(viewProj * (pos + vec4(v2.x, v1.y, v1.z, 0)));
    //    EmVert(viewProj * (pos + vec4(v2.x, v2.y, v1.z, 0)));
    //    EndPrimitive();
    //
    //
    //    EmVert(viewProj * (pos + vec4(v1.x, v2.y, v2.z, 0)));
    //    EmVert(viewProj * (pos + vec4(v2.x, v2.y, v2.z, 0)));
    //    EmVert(viewProj * (pos + vec4(v2.x, v2.y, v1.z, 0)));
    //    EmVert(viewProj * (pos + vec4(v1.x, v2.y, v1.z, 0)));
    //    EndPrimitive();
}



void main() {
    v_color = gs_in[0].color1;
    //    vec3 scale = gs_in[0].scale*u_size;
    vec3 scale = vec3(1, 1, 1);

    float shape = gs_in[0].shape_radius.x;
    float radius = gs_in[0].shape_radius.y;

    //    v_color = vec4(col_shape/100.0, col_shape/1000, col_rad/10, 1);
    if (int(shape) == 0){
//        DrawBoxAt(gs_in[0].pos, gs_in[0].vert1, gs_in[0].vert2);
        DrawBoxAtCol2(gs_in[0].pos, gs_in[0].vert1, gs_in[0].color1, gs_in[0].vert2, gs_in[0].color2);
    } else if (int(shape) == 1){
        DrawPlaneAt(gs_in[0].pos, gs_in[0].vert1, gs_in[0].vert2);
    } else if (int(shape) == 2){
        DrawSphereAt(gs_in[0].pos, gs_in[0].vert1, gs_in[0].vert2, radius);
    } else if (int(shape) == 3){
        DrawCylinderAt(gs_in[0].pos, gs_in[0].vert1, gs_in[0].color1, gs_in[0].vert2, gs_in[0].color2, radius);
    }
}



//void main() {
////    v_color = gs_in[0].color;
//
////    vec3 scale = gs_in[0].scale*u_size;
//
////    float col_shape = gs_in[0].shape;
////    float col_rad = gs_in[0].radius;
//
//    float col_shape = gs_in[0].shape_radius.x;
//    float col_rad = gs_in[0].shape_radius.y;
//
////    if (col_shape < .5){
////        v_color = vec4(1, col_shape, col_rad/100, 1);
////        DrawSphereAt(10, gs_in[0].pos);
//////    } else if (col_shape < 1.1){
//////        v_color = vec4(1, 1, 0, 1);
//////        DrawSphereAt(30, gs_in[0].pos);
//////    } else if (col_shape < 2.1){
//////        v_color = vec4(0, 1, 0, 1);
//////        DrawSphere();
//////    } else if (col_shape < 3.1){
//////        v_color = vec4(1, 0, 1, 1);
//////        DrawSphereAt(50, gs_in[0].pos);
//////    } else if (col_shape < 4.1){
////////        v_color = vec4(col_shape/3.0, 0.0, 0.0, 1.0);
//////        v_color = vec4(0, 1, 1, 1);
//////        DrawSphereAt(70, gs_in[0].pos);
//////    } else if (col_shape < 5.1){
////////        v_color = vec4(col_shape/3.0, 0.0, 0.0, 1.0);
//////        v_color = vec4(0, .5, 1, 1);
//////        DrawSphereAt(70, gs_in[0].pos);
//////    } else if (col_shape < 6.1){
////////        v_color = vec4(col_shape/3.0, 0.0, 0.0, 1.0);
//////        v_color = vec4(0, 0, 1, 1);
//////        DrawSphereAt(70, gs_in[0].pos);
////    } else {
//////        v_color = vec4(col_shape/3.0, 0.0, 0.0, 1.0);
////        v_color = vec4(col_shape/10.0, col_shape/100, 0, 1);
//////        DrawSphereAt(col_shape, gs_in[0].pos);
////        DrawSphereAt(50, gs_in[0].pos);
////    }
//
//
//
//    v_color = vec4(col_shape/10.0, col_shape/1.0, col_rad/1, 1);
//
//    DrawSphereAt(50, vec4(0,0,0,1));
////    //        DrawSphereAt(col_shape, gs_in[0].pos);
//////    DrawSphereAt(50, gs_in[0].pos);
////
////    if (int(col_shape) == 0){
////        DrawBox();
//////    } else if (int(col_shape) == 1){
//////        DrawBox();
////    } else if (int(col_shape) == 2){
////        DrawSphere();
////    } else if (int(col_shape) == 3){
////        DrawCylinder();
////    }
////
////    if (col_shape < 0.1){
////        DrawBox();
//////    } else if (col_shape < 1.1){
//////        DrawBox();
////    } else if (col_shape < 2.1){
////        DrawSphere();
////    } else if (col_shape < 3.1){
////        DrawCylinder();
////    } else {
////        DrawSphereAt(col_shape, gs_in[0].pos);
////    }
//
//
//
////    if (gs_in[0].shape == 0){
////        DrawBox();
//////    } else if (gs_in[0].shape == 1){
//////        DrawBox();
////    } else if (gs_in[0].shape == 2){
////        DrawSphere();
////    } else if (gs_in[0].shape == 3){
////        DrawCylinder();
////    }
//}
