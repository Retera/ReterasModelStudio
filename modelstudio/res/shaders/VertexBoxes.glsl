#version 330 core
layout (points) in;
layout (triangle_strip , max_vertices = 4) out;

uniform mat4 u_projection;
uniform vec2 scale;
const float MAGNITUDE = 2;
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
    vec2 mag = vec2(MAGNITUDE, MAGNITUDE)*scale;

    EmVert(p1 + vec4(-mag.x, -mag.y, 0.0, 0.0));    // 1:bottom-left
    EmVert(p1 + vec4( mag.x, -mag.y, 0.0, 0.0));    // 2:bottom-right
    EmVert(p1 + vec4(-mag.x,  mag.y, 0.0, 0.0));    // 3:top-left
    EmVert(p1 + vec4( mag.x,  mag.y, 0.0, 0.0));    // 4:top-right

    EndPrimitive();
}

void main() {
    v_color = gs_in[0].color;
    DrawPoint(u_projection * gs_in[0].pos);
}
