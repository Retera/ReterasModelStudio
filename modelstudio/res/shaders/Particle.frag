#version 330 core

uniform sampler2D u_texture;

uniform int u_alphaTest;

in vec2 v_uv;
in vec4 v_color;

out vec4 FragColor;

void main() {
    vec4 texel = texture2D(u_texture, v_uv);
    vec4 color = texel * v_color;

//    if(u_alphaTest != 0 && color.a < 0.75) {
//        discard;
//    }
//    if(texel.a < 0.01) {
//        discard;
//    }
    FragColor = color;
//    FragColor = vec4(1,1,1,1);
}