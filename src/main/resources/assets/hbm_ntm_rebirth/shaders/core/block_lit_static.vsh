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
uniform vec3 Light0_Direction;
uniform vec3 Light1_Direction;

out vec2 texCoord;
out vec2 lightmapUV;
out vec4 vertexColor;
out vec4 overlayColor;
out float vertexDistance;
out vec3 fragNormal;
out float vFadeAlpha;

float legacyStandardLight(vec3 transformedNormal) {
    float normalLength = length(transformedNormal);
    vec3 normal = normalLength > 1.0e-5 && !isinf(normalLength)
            ? transformedNormal / normalLength : vec3(0.0);
    vec3 light0 = normalize(Light0_Direction);
    vec3 light1 = normalize(Light1_Direction);
    return min(1.0, 0.40
            + 0.60 * max(dot(normal, light0), 0.0)
            + 0.60 * max(dot(normal, light1), 0.0));
}

vec3 transformNormal(mat4 modelView, vec3 sourceNormal) {
    mat3 linear = mat3(modelView);
    float determinantValue = determinant(linear);
    if (!(abs(determinantValue) > 1.0e-8) || isinf(determinantValue)) {
        return vec3(0.0);
    }
    return transpose(inverse(linear)) * sourceNormal;
}

void main() {
    vec4 viewPos = ModelViewMat * vec4(Position, 1.0);
    vec3 eyeNormal = transformNormal(ModelViewMat, Normal);
    gl_Position = ProjMat * viewPos;

    texCoord = UV0;
    lightmapUV = (vec2(UV2) + vec2(8.0)) / 256.0;
    vertexColor = vec4(Color.rgb * legacyStandardLight(eyeNormal), Color.a);
    overlayColor = texelFetch(Sampler1, UV1, 0);
    vertexDistance = length(viewPos.xyz);
    fragNormal = eyeNormal;
    vFadeAlpha = 1.0;
}
