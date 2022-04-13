#version 330 core
layout (lines) in;
layout (line_strip, max_vertices = 5) out;
//layout (triangle_strip, max_vertices = 4) out;

//const float MAGNITUDE = 0.4;
const float MAGNITUDE = 2;

uniform mat4 u_projection;

//uniform vec2 start;
//uniform vec2 end;

in VS_OUT {
    vec4 color;
} gs_in[];

out vec4 v_color;

void EmVert(vec4 vPos) {
    gl_Position = vPos;
    EmitVertex();
}

void GenerateLine(vec4 start, vec4 end) {
    EmVert(vec4(start.x, start.y, -1.0, 1.0));    // 1:bottom-left
    EmVert(vec4(  end.x, start.y, -1.0, 1.0));    // 2:bottom-right
    EmVert(vec4(  end.x,   end.y, -1.0, 1.0));    // 4:top-right
    EmVert(vec4(start.x,   end.y, -1.0, 1.0));    // 3:top-left
    EmVert(vec4(start.x, start.y, -1.0, 1.0));    // 1:bottom-left

    EndPrimitive();
}

void main() {
    v_color = gs_in[0].color;
    GenerateLine(gl_in[0].gl_Position, gl_in[1].gl_Position);
}