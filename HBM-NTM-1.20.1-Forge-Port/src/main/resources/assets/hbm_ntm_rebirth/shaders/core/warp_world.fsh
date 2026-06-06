#version 150

uniform sampler2D Sampler0;
uniform sampler2D ScreenTexture;
uniform sampler2D TargetTexture;

uniform vec4 ColorModulator;
uniform vec4 uColor;
uniform int uType;
uniform vec2 screenSize;
uniform float time;
uniform mat4 ProjMat;
uniform mat4 ModelViewMat;
uniform int useType;
uniform float intensity;

in vec4 vertexColor;
in vec2 texCoord0;
in vec3 vNormal;
in vec3 vPos;

out vec4 fragColor;

vec2 viewToScreenUV(vec3 viewPos)
{
    vec4 clip = ProjMat * vec4(viewPos, 1.0);
    vec2 ndc = clip.xy / clip.w;
    return clamp(ndc * 0.5 + 0.5, vec2(0.001), vec2(0.999));
}

vec2 projectToScreenUV(vec3 viewPos, vec3 dir, float traceLen)
{
    vec3 endPos = viewPos + dir * traceLen;
    return viewToScreenUV(endPos);
}

float schlickFresnel(float cosine, float ref_idx)
{
    float r0 = (1.0 - ref_idx) / (1.0 + ref_idx);
    r0 = r0 * r0;
    return r0 + (1.0 - r0) * pow(1.0 - cosine, 5.0);
}

vec3 safeRefract(vec3 I, vec3 N, float eta)
{
    float cosI = dot(N, I);
    float sinT2 = eta * eta * (1.0 - cosI * cosI);
    if (sinT2 > 1.0) return reflect(I, N);
    float cosT = sqrt(1.0 - sinT2);
    return eta * I + (eta * cosI - cosT) * N;
}

void shockwave(vec3 normal, vec3 viewDir, vec3 viewPos, float sphereRadius)
{
    vec2 baseUV = viewToScreenUV(viewPos);
    vec3 passthrough = texture(ScreenTexture, baseUV).rgb;

    if (intensity <= 0.0)
    {
        fragColor = vec4(passthrough, 1.0);
        return;
    }

    float vertexAlpha = vertexColor.a;
    if (vertexAlpha <= 0.002)
    {
        discard;
    }

    float cosTheta = abs(dot(normal, -viewDir));
    float edgeFactor = 1.0 - cosTheta;

    float distortionBand = smoothstep(0.40, 0.78, edgeFactor) * (1.0 - smoothstep(0.94, 1.0, edgeFactor));
    float visibleBand = smoothstep(0.58, 0.86, edgeFactor) * (1.0 - smoothstep(0.98, 1.0, edgeFactor));
    if (distortionBand <= 0.001 && visibleBand <= 0.001)
    {
        discard;
    }

    float pulse = 0.85 + 0.15 * sin(time * 28.0);
    float effectiveIntensity = intensity * vertexAlpha * vertexAlpha;
    float maxDistortion = 0.13 * effectiveIntensity * pulse;
    float strength = distortionBand * maxDistortion;

    vec4 clipNorm = ProjMat * vec4(viewPos + normal, 1.0);
    vec2 ndcNorm = clipNorm.xy / clipNorm.w;
    vec2 screenNormal = normalize((ndcNorm * 0.5 + 0.5) - baseUV);

    vec2 offset = screenNormal * strength;
    vec2 distortedUV = clamp(baseUV + offset, vec2(0.001), vec2(0.999));

    vec3 color = texture(ScreenTexture, distortedUV).rgb;

    float highlight = visibleBand * 0.08 * effectiveIntensity;
    vec3 rimColor = vec3(0.75, 0.9, 1.0) * highlight;
    color = mix(passthrough, color + rimColor, clamp(effectiveIntensity, 0.0, 1.0));

    float alpha = max(distortionBand * 0.18, visibleBand * 0.10) * clamp(effectiveIntensity, 0.0, 1.0);
    if (alpha <= 0.003)
    {
        discard;
    }
    fragColor = vec4(color, alpha);
}

void glassBall(vec3 normal, vec3 viewDir, vec3 viewPos)
{
    vec2 baseUV = viewToScreenUV(viewPos);
    vec3 passthrough = texture(ScreenTexture, baseUV).rgb;

    if (intensity <= 0.0)
    {
        fragColor = vec4(passthrough, 1.0);
        return;
    }

    float ref_idx = 1.5;
    float sphereRadius = 1.0;
    float FP = 4.0;

    float NdotV = -dot(normal, viewDir);
    NdotV = max(NdotV, 0.0);

    float eta = 1.0 / ref_idx;
    float F0 = (1.0 - eta) * (1.0 - eta) / ((1.0 + eta) * (1.0 + eta));
    float fresnelRatio = F0 + (1.0 - F0) * pow(1.0 - NdotV, FP);

    vec3 refracted1 = safeRefract(viewDir, normal, eta);

    vec3 sphereCenter = viewPos - normal * sphereRadius;
    float travelDist = sphereRadius * 2.0;
    vec3 innerHitPos = viewPos + refracted1 * travelDist;

    vec3 backNormal = -normalize(innerHitPos - sphereCenter);

    float eta_out = ref_idx;
    vec3 refracted2 = safeRefract(refracted1, backNormal, eta_out);

    float traceLen = sphereRadius * 5.0;
    vec2 refractUV = projectToScreenUV(innerHitPos, refracted2, traceLen);
    vec3 refractedColor = texture(ScreenTexture, refractUV).rgb;

    float dispersion = 0.005;

    float eta_r = 1.0 / (ref_idx - dispersion);
    vec3 refr1_r = safeRefract(viewDir, normal, eta_r);
    vec3 innerHit_r = viewPos + refr1_r * travelDist;
    vec3 backN_r = -normalize(innerHit_r - sphereCenter);
    vec3 refr2_r = safeRefract(refr1_r, backN_r, ref_idx - dispersion);
    vec2 refractUV_r = projectToScreenUV(innerHit_r, refr2_r, traceLen);

    float eta_b = 1.0 / (ref_idx + dispersion);
    vec3 refr1_b = safeRefract(viewDir, normal, eta_b);
    vec3 innerHit_b = viewPos + refr1_b * travelDist;
    vec3 backN_b = -normalize(innerHit_b - sphereCenter);
    vec3 refr2_b = safeRefract(refr1_b, backN_b, ref_idx + dispersion);
    vec2 refractUV_b = projectToScreenUV(innerHit_b, refr2_b, traceLen);

    refractedColor.r = texture(ScreenTexture, refractUV_r).r;
    refractedColor.b = texture(ScreenTexture, refractUV_b).b;

    vec3 reflectDir = reflect(viewDir, normal);
    vec2 reflectUV = projectToScreenUV(viewPos, reflectDir, sphereRadius * 8.0);
    vec3 reflectColor = texture(ScreenTexture, reflectUV).rgb;

    reflectColor = reflectColor + vec3(0.02);

    vec3 finalRGB = mix(refractedColor, reflectColor, fresnelRatio);

    float edgeGlow = pow(1.0 - NdotV, 3.0) * 0.10;
    finalRGB += vec3(edgeGlow);

    float absorption = exp(-0.02 * sphereRadius * 2.0);
    finalRGB *= absorption;

    fragColor = vec4(mix(passthrough, finalRGB, intensity), 1.0);
}

void mirror(vec3 normal, vec3 viewDir, vec3 viewPos)
{
    vec2 baseUV = viewToScreenUV(viewPos);
    vec3 passthrough = texture(ScreenTexture, baseUV).rgb;

    if (intensity <= 0.0)
    {
        fragColor = vec4(passthrough, 1.0);
        return;
    }

    vec3 reflectDir = reflect(viewDir, normal);

    float traceDistance = 10.0;

    vec2 reflectUV = projectToScreenUV(viewPos, reflectDir, traceDistance);

    vec3 reflectColor = texture(ScreenTexture, reflectUV).rgb;

    float NdotV = abs(dot(normal, -viewDir));
    float fresnel = pow(1.0 - NdotV, 3.0);

    float edgeGlow = fresnel * 0.15;
    reflectColor += vec3(edgeGlow);

    vec3 tint = vec3(0.9, 0.95, 1.0);
    reflectColor *= tint;

    float reflectStrength = mix(0.7, 1.0, fresnel);

    vec3 finalColor = mix(passthrough, reflectColor, intensity * reflectStrength);

    fragColor = vec4(finalColor, 1.0);
}

void advancedMirror(vec3 normal, vec3 viewDir, vec3 viewPos)
{
    vec2 baseUV = viewToScreenUV(viewPos);
    vec3 passthrough = texture(ScreenTexture, baseUV).rgb;

    if (intensity <= 0.0)
    {
        fragColor = vec4(passthrough, 1.0);
        return;
    }

    float distortionAmount = 0.05 * intensity;
    vec3 distortedNormal = normal;
    distortedNormal.x += sin(time * 2.0 + viewPos.y * 3.0) * distortionAmount;
    distortedNormal.z += cos(time * 1.5 + viewPos.x * 3.0) * distortionAmount;
    distortedNormal = normalize(distortedNormal);

    vec3 reflectDir = reflect(viewDir, distortedNormal);

    float traceDistance = 10.0;

    float dispersion = 0.008 * intensity;

    vec3 reflectDir_R = reflect(viewDir, normalize(distortedNormal + vec3(dispersion, 0, 0)));
    vec2 reflectUV_R = projectToScreenUV(viewPos, reflectDir_R, traceDistance);

    vec2 reflectUV_G = projectToScreenUV(viewPos, reflectDir, traceDistance);

    vec3 reflectDir_B = reflect(viewDir, normalize(distortedNormal - vec3(dispersion, 0, 0)));
    vec2 reflectUV_B = projectToScreenUV(viewPos, reflectDir_B, traceDistance);

    vec3 reflectColor;
    reflectColor.r = texture(ScreenTexture, reflectUV_R).r;
    reflectColor.g = texture(ScreenTexture, reflectUV_G).g;
    reflectColor.b = texture(ScreenTexture, reflectUV_B).b;

    float NdotV = abs(dot(normal, -viewDir));
    float fresnel = pow(1.0 - NdotV, 2.5);

    float edgeGlow = fresnel * (0.12 + 0.08 * sin(time * 3.0));
    vec3 glowColor = vec3(0.6, 0.8, 1.0);
    reflectColor += glowColor * edgeGlow;

    float metallic = 0.85;
    vec3 metallicTint = vec3(0.95, 0.97, 1.0);
    reflectColor = mix(reflectColor, reflectColor * metallicTint, metallic);

    float reflectStrength = mix(0.75, 1.0, fresnel);

    float rainbow = sin(time * 4.0 + viewPos.x * 5.0 + viewPos.y * 3.0) * 0.5 + 0.5;
    vec3 rainbowColor = vec3(
    sin(rainbow * 6.28),
    sin(rainbow * 6.28 + 2.09),
    sin(rainbow * 6.28 + 4.18)) * 0.5 + 0.5;
    reflectColor += rainbowColor * fresnel * 0.05 * intensity;

    vec3 finalColor = mix(passthrough, reflectColor, intensity * reflectStrength);

    fragColor = vec4(finalColor, 1.0);
}

void main()
{
    vec3 normal = normalize(vNormal);
    vec3 viewDir = normalize(vPos);
    float sphereRadius = 1.0;

    if (useType == 0)
    {
        glassBall(normal, viewDir, vPos);
    }
    else if (useType == 1)
    {
        shockwave(normal, viewDir, vPos, sphereRadius);
    }
    else if (useType == 2)
    {
        mirror(normal, viewDir, vPos);
    }
    else if (useType == 3)
    {
        advancedMirror(normal, viewDir, vPos);
    }
}
