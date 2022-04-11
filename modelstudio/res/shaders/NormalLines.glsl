#version 330 core
layout (points) in;
layout (line_strip, max_vertices = 2) out;

//const float MAGNITUDE = 0.4;
const float MAGNITUDE = 2;

uniform mat4 u_projection;

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

void GenerateLine(int index)
{
    EmVert(u_projection * gs_in[index].pos);
    EmVert(u_projection * (gs_in[index].pos + vec4(gs_in[index].normal, 0.0) * MAGNITUDE));
    EndPrimitive();
}

void main()
{
    v_color = gs_in[0].color;
    GenerateLine(0);
}