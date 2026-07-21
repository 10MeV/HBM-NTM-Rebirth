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
out float vFadeAlpha;

// Exact source constants from 1.7.10 RenderHelper#enableStandardItemLighting:
// global ambient .4; two normalized directional lights at (.2,1,-.7) and (-.2,1,.7), diffuse .6.
float legacyStandardItemLight(vec3 eyeSpaceNormal) {
    float normalLength = length(eyeSpaceNormal);
    vec3 normal = normalLength > 1.0e-5 ? eyeSpaceNormal / normalLength : vec3(0.0);
    vec3 light0 = normalize(vec3(0.20, 1.00, -0.70));
    vec3 light1 = normalize(vec3(-0.20, 1.00, 0.70));
    return min(1.0, 0.40 + 0.60 * max(dot(normal, light0), 0.0)
            + 0.60 * max(dot(normal, light1), 0.0));
}

void main() {
    vec4 viewPos = ModelViewMat * vec4(Position, 1.0);
    gl_Position = ProjMat * viewPos;

    texCoord = UV0;
    lightmapUV = (vec2(UV2) + vec2(8.0)) / 256.0;
    vertexColor = vec4(Color.rgb * legacyStandardItemLight(mat3(ModelViewMat) * Normal), Color.a);
    overlayColor = texelFetch(Sampler1, UV1, 0);
    vertexDistance = length(viewPos.xyz);
    vFadeAlpha = 1.0;
}
