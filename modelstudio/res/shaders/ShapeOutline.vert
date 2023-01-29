#version 330 core
//		POSITION + NORMAL + POSITION + POSITION + SELECTION_STATUS + SELECTION_STATUS;
layout (location = 0) in vec4 a_position;
layout (location = 1) in vec4 a_normal;
layout (location = 2) in vec4 a_vert1;
layout (location = 3) in vec4 a_vert2;
layout (location = 4) in vec2 a_shape_radius;
uniform vec4 u_vertColors[4];


out VS_OUT {
    vec4 pos;
    vec4 norm;
    vec4 color1;
    vec4 color2;
    vec4 vert1;
    vec4 vert2;
    vec2 shape_radius;
} vs_out;

void main() {
    gl_Position = a_position;
    vs_out.pos = a_position;
    vs_out.norm = a_normal;
    vs_out.color1 = u_vertColors[int(a_vert1.w)];
    vs_out.color2 = u_vertColors[int(a_vert2.w)];

    vs_out.vert1 = vec4(a_vert1.xyz, 0);
    vs_out.vert2 = vec4(a_vert2.xyz, 0);

    vs_out.shape_radius = a_shape_radius;
}