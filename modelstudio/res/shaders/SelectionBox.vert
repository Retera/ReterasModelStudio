#version 330 core
layout (location = 0) in vec4 a_position;
//layout (location = 1) in vec4 a_normal;
layout (location = 1) in vec4 a_color;

out VS_OUT {
//    vec4 pos;
//    vec3 normal;
    vec4 color;
} vs_out;

//out vec4 v_color;
void main() {
    gl_Position = a_position;
//    vs_out.pos = a_position;
//    vs_out.normal = (a_normal).xyz;
    vs_out.color = a_color;
}