#version 330 core

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

out vec4 fragColor;

void main() {
    vec4 baseColor = texture(Sampler0, texCoord) * vertexColor;
    vec3 lightColor = texture(Sampler2, lightmapUV).rgb;
    baseColor.rgb = mix(overlayColor.rgb, baseColor.rgb, overlayColor.a);
    vec3 litColor = baseColor.rgb * lightColor * 0.8;

    float alpha = baseColor.a * vFadeAlpha;
    if (alpha < 0.01) {
        discard;
    }

    float fogFactor = clamp((FogEnd - vertexDistance) / (FogEnd - FogStart), 0.0, 1.0);
    fragColor = vec4(mix(FogColor.rgb, litColor, fogFactor), alpha);
}
