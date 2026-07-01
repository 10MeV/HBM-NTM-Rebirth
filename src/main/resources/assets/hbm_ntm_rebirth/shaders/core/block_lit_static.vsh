#version 330 core

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV1;
in ivec2 UV2;
in vec3 Normal;

uniform sampler2D Sampler1;
uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec2 texCoord;
out vec2 lightmapUV;
out vec4 vertexColor;
out vec4 overlayColor;
out float vertexDistance;
out vec3 fragNormal;
out float vFadeAlpha;

float stableFaceShade(vec3 normal) {
    float len = length(normal);
    vec3 n = len > 1.0e-5 ? normal / len : vec3(0.0, 1.0, 0.0);
    vec3 weight = abs(n);
    float sum = max(weight.x + weight.y + weight.z, 1.0e-5);
    float yShade = n.y >= 0.0 ? 0.98 : 0.54;
    float axisShade = (weight.x * 0.72 + weight.y * yShade + weight.z * 0.82) / sum;
    vec3 keyLight = normalize(vec3(0.20, 1.00, -0.70));
    vec3 fillLight = normalize(vec3(-0.20, 1.00, 0.70));
    float fixedDiffuse = max(dot(n, keyLight), 0.0) * 0.60 + max(dot(n, fillLight), 0.0) * 0.40;
    float detailShade = 0.84 + fixedDiffuse * 0.20;
    return clamp(axisShade * detailShade, 0.50, 1.00);
}

void main() {
    vec4 viewPos = ModelViewMat * vec4(Position, 1.0);
    gl_Position = ProjMat * viewPos;

    texCoord = UV0;
    lightmapUV = (vec2(UV2) + vec2(8.0)) / 256.0;
    vertexColor = vec4(Color.rgb * stableFaceShade(Normal), Color.a);
    overlayColor = texelFetch(Sampler1, UV1, 0);
    vertexDistance = length(viewPos.xyz);
    fragNormal = mat3(ModelViewMat) * Normal;
    vFadeAlpha = 1.0;
}
