#version 330 core

layout(location = 0) in vec3 Position;
layout(location = 1) in vec3 Normal;
layout(location = 2) in vec2 UV0;
layout(location = 3) in vec4 InstModel0;
layout(location = 4) in vec4 InstModel1;
layout(location = 5) in vec4 InstModel2;
layout(location = 6) in vec4 InstModel3;
layout(location = 7) in vec4 InstLightC01;
layout(location = 8) in vec4 InstLightC23;
layout(location = 9) in vec4 InstLightC45;
layout(location = 10) in vec4 InstLightC67;
layout(location = 11) in vec4 InstColor;
layout(location = 12) in vec4 InstOverlay;
layout(location = 13) in vec3 LocalLightWeight;

uniform mat4 ProjMat;
uniform float FadeAlpha;

out vec4 vertexColor;
out float vertexDistance;
out float vFadeAlpha;

void main() {
    mat4 instanceModelView = mat4(InstModel0, InstModel1, InstModel2, InstModel3);
    vec4 viewPos = instanceModelView * vec4(Position, 1.0);
    gl_Position = ProjMat * viewPos;

    vertexColor = InstColor;
    vertexDistance = length(viewPos.xyz);
    vFadeAlpha = FadeAlpha;
}
