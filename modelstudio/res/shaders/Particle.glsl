#version 330 core
layout (points) in;
layout (triangle_strip , max_vertices = 4) out;

uniform mat4 u_transform;
uniform mat4 u_projection;
uniform mat4 u_view;
uniform vec2 u_flipBookSize;

in VS_OUT {
    vec4 pos;
    vec4 tail;
    float scale;
    vec2 uv;
    vec4 color;
} gs_in[];

out vec4 v_color;
out vec2 v_uv;

void EmVert(vec4 vPos, vec2 uv) {
//    gl_Position = u_projection * vPos;
    gl_Position = vPos;
    v_uv = uv;
    EmitVertex();
}

void DrawPoint(vec4 p1, vec2 uv_in, float scale) {
    vec2 mag = vec2(20, 20);//*scale;
//    v_color = vec4(1, 1,1,1);
    EmVert(p1 + vec4(-mag.x, -mag.y, 0.0, 0.0), vec2((uv_in.x + 0)/u_flipBookSize.x,(uv_in.y + 0)/u_flipBookSize.y));    // 1:bottom-left
//    v_color = vec4(0, 1,0,1);
    EmVert(p1 + vec4( mag.x, -mag.y, 0.0, 0.0), vec2((uv_in.x + 1)/u_flipBookSize.x,(uv_in.y + 0)/u_flipBookSize.y));    // 2:bottom-right
//    v_color = vec4(0, 0,1,1);
    EmVert(p1 + vec4(-mag.x,  mag.y, 0.0, 0.0), vec2((uv_in.x + 0)/u_flipBookSize.x,(uv_in.y + 1)/u_flipBookSize.y));    // 3:top-left
//    v_color = vec4(1, 0,0,1);
    EmVert(p1 + vec4( mag.x,  mag.y, 0.0, 0.0), vec2((uv_in.x + 1)/u_flipBookSize.x,(uv_in.y + 1)/u_flipBookSize.y));    // 4:top-right

    EndPrimitive();
}

void DrawPointTail(vec4 p1, vec4 tail, vec2 uv_in, float scale) {
    vec4 norm = vec4(normalize(cross(vec3(1,0,0), tail.xyz)) * scale, 0);

//    vec4 norm = vec4(normalize(cross((u_transform * vec4(1,0,0,0)).xyz, tail.xyz)) * scale, 0);

//    vec2 mag = vec2(20, 20);//*scale;
//    v_color = vec4(1, 1,1,1);
    EmVert(u_projection * (p1 - norm - tail), vec2((uv_in.x + 0)/u_flipBookSize.x,(uv_in.y + 0)/u_flipBookSize.y));    // 1:bottom-left
//    v_color = vec4(0, 1,0,1);
    EmVert(u_projection * (p1 + norm - tail), vec2((uv_in.x + 1)/u_flipBookSize.x,(uv_in.y + 0)/u_flipBookSize.y));    // 2:bottom-right
//    v_color = vec4(0, 0,1,1);
    EmVert(u_projection * (p1 - norm), vec2((uv_in.x + 0)/u_flipBookSize.x,(uv_in.y + 1)/u_flipBookSize.y));    // 3:top-left
//    v_color = vec4(1, 0,0,1);
    EmVert(u_projection * (p1 + norm), vec2((uv_in.x + 1)/u_flipBookSize.x,(uv_in.y + 1)/u_flipBookSize.y));    // 4:top-right


    EndPrimitive();
}

void main() {
//    DrawPoint(u_projection * gs_Position);
    v_color = gs_in[0].color;
    v_uv = gs_in[0].uv;
//    DrawPoint(gs_in[0].pos, gs_in[0].scale);
    if(gs_in[0].tail.w == 0){
        DrawPoint(u_projection * gs_in[0].pos, gs_in[0].uv, gs_in[0].scale);
    } else {
//        DrawPoint(u_projection * gs_in[0].pos, gs_in[0].uv, gs_in[0].scale);

//        DrawPointTail(u_projection * gs_in[0].pos, u_projection * vec4(gs_in[0].norm.xyz, 0), gs_in[0].uv, gs_in[0].scale);
        DrawPointTail(gs_in[0].pos, vec4(gs_in[0].tail.xyz, 0), gs_in[0].uv, gs_in[0].scale);
    }
}