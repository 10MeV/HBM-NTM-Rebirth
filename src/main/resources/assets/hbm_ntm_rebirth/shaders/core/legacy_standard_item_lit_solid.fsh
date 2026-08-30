#version 330 core

#moj_import <fog.glsl>

in vec2 texCoord;
in vec2 lightmapUV;
in vec4 vertexColor;
in vec4 overlayColor;
in float vertexDistance;
in float vFadeAlpha;

uniform sampler2D Sampler0;
uniform sampler2D Sampler2;
uniform vec4 FogColor;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 ColorModulator;

out vec4 fragColor;

void main() {
    vec4 baseColor = texture(Sampler0, texCoord) * vertexColor * ColorModulator;
    vec3 lightColor = texture(Sampler2, lightmapUV).rgb;
    baseColor.rgb = mix(overlayColor.rgb, baseColor.rgb, overlayColor.a);
    baseColor.rgb *= lightColor;
    baseColor.a *= vFadeAlpha;
    fragColor = linear_fog(baseColor, vertexDistance, FogStart, FogEnd, FogColor);
}
