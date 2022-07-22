#version 330 core
#define L10INV .4342944819
layout (location = 0) in vec4 a_position;
layout (location = 1) in vec4 a_dist;
layout (location = 2) in vec4 a_color;

uniform mat4 u_projection;
uniform mat4 u_view;
//uniform vec4 u_color[2];

//uniform vec3 u_dist;
float log10(float x){
    return L10INV * log(x);
}

out vec4 v_color;
void main() {
    ////    gl_Position = u_projection * u_view * (a_position + vec4(u_dist*gl_InstanceID, 0));
    ////    vec4 norm = u_projection * u_view * vec4(normalize(cross(a_dist.xyz, normalize(a_position.xyz))),0);
    //    vec4 norm = vec4(normalize(cross(a_dist.xyz, normalize(a_position.xyz))),0);
    //    vec4 distProj = u_projection * u_view * vec4(a_dist.xyz, 1);
    //    vec3 distProj2 = normalize((u_projection * u_view * vec4(a_dist.xyz, 0)).xyz);
    //    float dotX = dot(normalize((u_projection * u_view * vec4(a_dist.xyz, 0)).xyz), normalize(vec4(1,0,0,1).xyz));
    //    float dotY = dot(normalize((u_projection * u_view * vec4(a_dist.xyz, 0)).xyz), normalize(vec4(0,1,0,1).xyz));
    //    float dotZ = dot(normalize((u_projection * u_view * vec4(a_dist.xyz, 0)).xyz), normalize(vec4(0,0,1,1).xyz));
    ////    float n_dotZ = dot(normalize((u_projection * u_view * vec4(norm.xyz, 0)).xyz), normalize(vec4(0,0,1,0).xyz));
    ////    float n_dotZ = dot(normalize((u_projection * u_view * vec4(norm.xyz, 1)).xyz), normalize(vec4(0,0,1,1).xyz));
    ////    float n_dotZ = dot(normalize((u_projection * u_view * vec4(norm.xyz, 0)).xyz), vec3(0,0,1));
    //    float n_dotZ = dot(u_projection * u_view * vec4(norm.xyz, 0), vec4(0,0,1,1));
    ////    float dotX = dot(u_projection * u_view * vec4(a_dist), vec4(1,0,0,0));
    ////    float dotY = dot(u_projection * u_view * vec4(a_dist), vec4(0,1,0,0));
    ////    float dotZ = dot(normalize((u_projection * u_view * vec4(a_dist.xyz, 1)).xyz), normalize((u_projection * vec4(0,0,1,1)).xyz));
    ////    float dotZ = dot(normalize((u_projection * u_view * vec4(a_dist.xyz, 0)).xyz), normalize(vec4(0,0,1,1).xyz));
    //    float leng = length(u_projection * u_view * vec4(a_dist));
    ////    float oneLen = length(u_projection * u_view * vec4(1, 0, 0, 0));
    //    float oneLen = length(u_projection * u_view * vec4(1, 1, 1, 0));
    ////    float lenAdj = 1/length(u_projection * u_view * vec4(1, 1, 1, 0));
    //    float p1 = length(u_projection*vec4(1,0,0,0));
    ////    float lenAdj = 1/length(u_view * vec4(1, 1, 1, 0)*22);
    //    float lenAdj = 1/length(u_projection*vec4(1,0,0,0));
    ////    float vissible = length((u_projection * u_view * vec4(a_dist.xyz, 1)).xyz);
    //    int pot = int(log10(lenAdj));
    //    float adj = pow(10, pot);


    //    gl_Position = u_projection * u_view * (a_position + vec4(a_dist*gl_InstanceID*adj));
    gl_Position = u_projection * u_view * (a_position + vec4(a_dist*gl_InstanceID));
    //    float distAdj = lenght(u_projection * u_view * vec4(dist, 0))/10;
    //    v_color = u_color[0];
    v_color = a_color;
    //    v_color.y = oneLen;
    //    v_color.y = p1;


    ////    if(dotZ <=-.99 || .99 <= dotZ){
    ////    if(dotZ ==-1 || 1 == dotZ){
    ////    if((distProj2.x == 0 || distProj2 == 0) && (distProj2.z == -1 || distProj2.z == 1)){
    ////    if((dotX.x == 0 || dotY == 0) && dotZ != 0){
    ////    if((dotX.x == 0 || dotY == 0) && dotZ != 0){
    //    if(n_dotZ == 0 && (distProj2.x == 0 || distProj2.y == 0)){
    ////        v_color = vec4(1,1,0,1);
    //        v_color = vec4(norm.xyz,1);
    //    } else {
    //
    //        v_color.a = v_color.a *(int((100-gl_InstanceID%100)/100)*0.3 + int((10-gl_InstanceID%10)/10)*0.3 + .3);
    //    }
    v_color.a = v_color.a *(int((100-gl_InstanceID%100)/100)*0.3 + int((10-gl_InstanceID%10)/10)*0.3 + .3);

}