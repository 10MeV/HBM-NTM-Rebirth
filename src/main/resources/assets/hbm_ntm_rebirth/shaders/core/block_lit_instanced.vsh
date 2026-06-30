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

vec2 decodePackedLight(float packedLight) {
    float value = floor(packedLight + 0.5);
    float sky = floor(value / 16.0);
    float block = value - sky * 16.0;
    return vec2(block, sky) * 16.0;
}

vec2 bilinearPackedLightUv(vec2 w, vec4 slice) {
    vec2 c00 = decodePackedLight(slice.x);
    vec2 c10 = decodePackedLight(slice.y);
    vec2 c01 = decodePackedLight(slice.z);
    vec2 c11 = decodePackedLight(slice.w);
    vec2 z0 = mix(c00, c01, w.y);
    vec2 z1 = mix(c10, c11, w.y);
    return mix(z0, z1, w.x);
}

vec2 sliceLightUv(int slice, vec2 xzWeight) {
    if (slice == 0) {
        return bilinearPackedLightUv(xzWeight, InstLightC01);
    } else if (slice == 1) {
        return bilinearPackedLightUv(xzWeight, InstLightC23);
    } else if (slice == 2) {
        return bilinearPackedLightUv(xzWeight, InstLightC45);
    }
    return bilinearPackedLightUv(xzWeight, InstLightC67);
}

vec2 slicedLightUv(vec3 w) {
    float scaledY = clamp(w.y, 0.0, 1.0) * 3.0;
    int slice0 = int(floor(scaledY));
    slice0 = clamp(slice0, 0, 3);
    int slice1 = min(slice0 + 1, 3);
    float sliceWeight = clamp(scaledY - float(slice0), 0.0, 1.0);
    vec2 xzWeight = vec2(w.x, w.z);
    return mix(sliceLightUv(slice0, xzWeight), sliceLightUv(slice1, xzWeight), sliceWeight);
}

float stableFaceShade(vec3 normal) {
    float len = length(normal);
    vec3 n = len > 1.0e-5 ? normal / len : vec3(0.0, 1.0, 0.0);
    vec3 weight = abs(n);
    float sum = max(weight.x + weight.y + weight.z, 1.0e-5);
    float yShade = n.y >= 0.0 ? 0.96 : 0.58;
    float axisShade = (weight.x * 0.76 + weight.y * yShade + weight.z * 0.86) / sum;
    vec3 keyLight = normalize(vec3(0.20, 1.00, -0.70));
    vec3 fillLight = normalize(vec3(-0.20, 1.00, 0.70));
    float fixedDiffuse = max(dot(n, keyLight), 0.0) * 0.60 + max(dot(n, fillLight), 0.0) * 0.40;
    float detailShade = 0.92 + fixedDiffuse * 0.12;
    return clamp(axisShade * detailShade, 0.52, 0.98);
}

void main() {
    mat4 instanceModelView = mat4(InstModel0, InstModel1, InstModel2, InstModel3);
    vec3 lightWeight = clamp(LocalLightWeight, vec3(0.0), vec3(1.0));
    vec2 rawLightUv = slicedLightUv(lightWeight);

    vec4 viewPos = instanceModelView * vec4(Position, 1.0);
    gl_Position = ProjMat * viewPos;

    texCoord = UV0;
    lightmapUV = (rawLightUv + vec2(8.0)) / 256.0;
    vertexColor = vec4(InstColor.rgb * stableFaceShade(Normal), InstColor.a);
    overlayColor = texelFetch(Sampler1, ivec2(InstOverlay.xy), 0);
    vertexDistance = length(viewPos.xyz);
    fragNormal = mat3(instanceModelView) * Normal;
    vFadeAlpha = FadeAlpha;
}
