#version 330 core

layout (location = 0) in vec4 a_position;
layout (location = 1) in vec4 a_normal;
layout (location = 2) in vec2 a_uv;
layout (location = 3) in vec4 a_color;
layout (location = 4) in float a_scale;

out VS_OUT {
    vec4 pos;
    vec4 tail;
    float scale;
    vec2 uv;
    vec4 color;
} vs_out;

void main() {
//    gl_Position = a_position;
    vs_out.pos = a_position;
    vs_out.tail = a_normal;
    vs_out.scale = a_scale;
    vs_out.uv = a_uv;
    vs_out.color = a_color;
}