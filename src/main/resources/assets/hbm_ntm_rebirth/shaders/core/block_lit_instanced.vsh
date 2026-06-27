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

uniform sampler2D Sampler1;
uniform mat4 ProjMat;
uniform float FadeAlpha;

out vec2 texCoord;
out vec2 lightmapUV;
out vec4 vertexColor;
out vec4 overlayColor;
out float vertexDistance;
out vec3 fragNormal;
out float vFadeAlpha;

vec2 trilinearLightUv(vec3 w, vec4 c01, vec4 c23, vec4 c45, vec4 c67) {
    vec2 c0 = c01.xy;
    vec2 c1 = c01.zw;
    vec2 c2 = c23.xy;
    vec2 c3 = c23.zw;
    vec2 c4 = c45.xy;
    vec2 c5 = c45.zw;
    vec2 c6 = c67.xy;
    vec2 c7 = c67.zw;

    vec2 x00 = mix(c0, c1, w.x);
    vec2 x10 = mix(c2, c3, w.x);
    vec2 x01 = mix(c4, c5, w.x);
    vec2 x11 = mix(c6, c7, w.x);
    vec2 y0 = mix(x00, x10, w.y);
    vec2 y1 = mix(x01, x11, w.y);
    return mix(y0, y1, w.z);
}

void main() {
    mat4 instanceModelView = mat4(InstModel0, InstModel1, InstModel2, InstModel3);
    vec3 lightWeight = clamp(LocalLightWeight, vec3(0.0), vec3(1.0));
    vec2 rawLightUv = trilinearLightUv(lightWeight, InstLightC01, InstLightC23, InstLightC45, InstLightC67);

    vec4 viewPos = instanceModelView * vec4(Position, 1.0);
    gl_Position = ProjMat * viewPos;

    texCoord = UV0;
    lightmapUV = (rawLightUv + vec2(8.0)) / 256.0;
    vertexColor = InstColor;
    overlayColor = texelFetch(Sampler1, ivec2(InstOverlay.xy), 0);
    vertexDistance = length(viewPos.xyz);
    fragNormal = mat3(instanceModelView) * Normal;
    vFadeAlpha = FadeAlpha;
}
