#version 330 core

in vec4 v_color;

out vec4 FragColor;

void main() {
    if (v_color.a == 0){
        discard;
    }
    FragColor = v_color;
}