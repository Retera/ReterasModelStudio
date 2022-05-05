#version 330 core
layout (triangles) in;
layout (line_strip, max_vertices = 20) out;
//layout (triangle_strip, max_vertices = 4) out;

//const float MAGNITUDE = 0.4;
const float MAGNITUDE = 2;
const float majorDist = 100;
const float div = 10;
const float divs = 2;

//uniform float majorDist;
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


void GenerateLine(vec3 start, vec3 end, vec4 color) {
    v_color = color;
    EmVert(u_projection * vec4(start, 1));
    EmVert(u_projection * vec4(end, 1));

    EndPrimitive();
}

//void GenerateLines(vec4 start, vec4 endLine, vec4 endSpread, vec4 color) {
//
//    vec3 line = (vec4(endLine)-start).xyz;
//    vec3 dirLine = normalize(line);
//    vec3 dirSpread = normalize((vec4(endSpread)-start).xyz);
//
//    vec3 linePoint = vec3(start);
//
//    vec4 color2 = vec4(color);
//    for(int major = 0; major<5; major++){
//        color2.a = color.a;
//        linePoint = start.xyz + vec3(dirSpread)*(majorDist*major);
////        GenerateLine(linePoint, linePoint+line, color2);
//        GenerateLine(linePoint, linePoint+line, vec4(1.0,0.0,0.0,1.0));
//        for(int minor = 1; minor<div; minor++){
//            color2.a = color.a/2.0;
//            linePoint = start.xyz + vec3(dirSpread)*(majorDist*major + majorDist/div*minor);
////            GenerateLine(linePoint, linePoint+line, color2);
//            GenerateLine(linePoint, linePoint+line, vec4(0.5,1.0,0.0,0.5));
//        }
//    }
//
//
////    EmVert(vec4(start.x, start.y, -1.0, 1.0));    // 1:bottom-left
////    EmVert(vec4(  end.x, start.y, -1.0, 1.0));    // 2:bottom-right
////    EmVert(vec4(  end.x,   end.y, -1.0, 1.0));    // 4:top-right
////    EmVert(vec4(start.x,   end.y, -1.0, 1.0));    // 3:top-left
////    EmVert(vec4(start.x, start.y, -1.0, 1.0));    // 1:bottom-left
////
////    EndPrimitive();
//}

void GenerateLines2(vec4 start, vec4 endLine, vec4 dirSpread, vec4 color) {

    float divAdj = 10;
    vec4 color2 = vec4(color);
    color2.a = color.a/2.0;

    int line = 1;

    vec3 linePointStart = vec3(start);
    vec3 linePointEnd = vec3(endLine);
    vec3 lineSpacing = (dirSpread / divAdj).xyz;
//    GenerateLine(linePointStart, linePointEnd, vec4(1.0,0.0,0.0,1.0));

    vec4 v1 = vec4(linePointStart, 1) * u_projection;
    vec4 v2 = vec4(linePointStart + lineSpacing, 1) * u_projection;
    vec3 spredNorm = normalize((v1-v2).xyz);
//    vec3 spredNorm = (v1-v2).xyz;

    float dZ = dot(spredNorm, vec3(0.0,0.0,1.0));
    float dY = dot(spredNorm, vec3(0.0,1.0,0.0));
    float dX = dot(spredNorm, vec3(1.0,0.0,0.0));

//    vec4 col = vec4(0.0,0.0,0.0,0.5);
    vec4 col = vec4(0.0,0.0,0.0,1.0);
//    if(dX>0.5){
//        col.x = 1.0;
//    }
//    if(dY>0.5){
//        col.y = 1.0;
//    }
//    if(dZ>0.5){
//        col.z = 1.0;
//    }
    col.x = abs(dX);
    col.y = abs(dY);
    col.z = abs(dZ);

//    vec4 col;
//    if(dX<0.5){
//        vec4 col = vec4(0.5,1.0,0.0,0.5);
//    }
//    if(dY<0.5){
//        vec4 col = vec4(0.5,1.0,0.0,0.5);
//    }
//    if(dZ<0.5){
//        vec4 col = vec4(0.5,1.0,0.0,0.5);
//    }



    GenerateLine(linePointStart, linePointEnd, col);


//    if(length(dirSpread.xyz)>0 && dZ<0.5){
//        for(int i = 1; i < 10; i++){
//            linePointStart += lineSpacing;
//            linePointEnd += lineSpacing;
//            GenerateLine(linePointStart, linePointEnd, col);
//        }
//    }
}

void main() {
//    v_color = gs_in[0].color;
//    GenerateLines(gl_in[0].gl_Position, gl_in[1].gl_Position, gl_in[2].gl_Position, gs_in[0].color);
    GenerateLines2(gl_in[0].gl_Position, gl_in[1].gl_Position, gl_in[2].gl_Position, gs_in[0].color);
}