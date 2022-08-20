#version 330 core
#define L10INV .4342944819
layout (location = 0) in vec4 a_position;
layout (location = 1) in vec4 a_normal;
layout (location = 2) in float a_selectionStatus;

uniform mat4 u_projection;
uniform mat4 u_view;
uniform vec4 u_vertColors[4];
//uniform vec4 u_color[2];

out vec4 v_color;
void main() {
    gl_Position = u_projection * u_view * a_position;
    v_color = u_vertColors[int(a_selectionStatus)];
}