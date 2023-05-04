#version 330 core
layout (location = 0) in vec4 a_position;
layout (location = 1) in vec4 a_destination;
layout (location = 2) in vec4 a_rotation;
layout (location = 3) in vec3 a_scaling;
layout (location = 4) in float a_selectionStatus1;
layout (location = 5) in float a_selectionStatus2;
layout (location = 6) in float a_billboarded;

uniform vec4 u_vertColors[4];

out VS_OUT {
    vec4 pos;
    vec4 end;
    vec4 rot;
    vec3 scale;
    vec4 color;
    vec4 color2;
} vs_out;

//out vec4 v_color;
void main() {
    gl_Position = a_position;
    vs_out.pos = a_position;
    vs_out.end = a_destination;
    vs_out.rot = a_rotation;
    vs_out.scale = a_scaling;
    vs_out.color = u_vertColors[int(a_selectionStatus1)];
    vs_out.color2 = u_vertColors[int(a_selectionStatus2)];
}