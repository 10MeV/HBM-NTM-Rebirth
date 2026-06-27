#version 330 core

in vec4 vertexColor;
in float vertexDistance;
in float vFadeAlpha;

uniform vec4 FogColor;
uniform float FogStart;
uniform float FogEnd;

out vec4 fragColor;

void main() {
    float alpha = vertexColor.a * vFadeAlpha;
    if (alpha < 0.01) {
        discard;
    }

    float fogFactor = clamp((FogEnd - vertexDistance) / (FogEnd - FogStart), 0.0, 1.0);
    fragColor = vec4(mix(FogColor.rgb, vertexColor.rgb, fogFactor), alpha);
}
