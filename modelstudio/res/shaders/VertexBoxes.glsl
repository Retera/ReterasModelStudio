#version 330 core
layout (points) in;
layout (triangle_strip , max_vertices = 4) out;

uniform mat4 u_projection;
const float MAGNITUDE = 0.4;
//uniform float vertRad;

in VS_OUT {
    vec4 pos;
    vec3 normal;
    vec4 color;
} gs_in[];


out vec4 v_color;

void EmVert(vec4 vPos) {
    gl_Position = vPos;
    EmitVertex();
}

void DrawPoint(vec4 p1) {

    EmVert(p1 + vec4(-MAGNITUDE, -MAGNITUDE, 0.0, 0.0));    // 1:bottom-left
    EmVert(p1 + vec4( MAGNITUDE, -MAGNITUDE, 0.0, 0.0));    // 2:bottom-right
    EmVert(p1 + vec4(-MAGNITUDE,  MAGNITUDE, 0.0, 0.0));    // 3:top-left
    EmVert(p1 + vec4( MAGNITUDE,  MAGNITUDE, 0.0, 0.0));    // 4:top-right

    EndPrimitive();
}

void main() {
    v_color = gs_in[0].color;
    DrawPoint(u_projection * gs_in[0].pos);
}
