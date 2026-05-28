#version 400

uniform sampler2D MainDepthSampler;
uniform sampler2D MainColorSampler;
uniform sampler2D TextureSampler;
uniform sampler2D ColorSampler;

uniform vec2 screenSize;
uniform mat4 projectionMatrix;
uniform mat4 modelViewMatrix;
uniform vec3 cameraPos;
uniform vec3 entityPos;
uniform float time;
uniform float scale;
uniform float accretionDiskRadiusScale;
uniform float accretionDiskThicknessScale;
uniform float accretionDiskDensity;
uniform float tiltAngle;
uniform float intensity;
uniform float renderQuality;
uniform float ditherStrength;
uniform float lensBoundarySoftness;
uniform float diskNoiseStrength;
uniform float diskTextureStrength;
uniform float noiseTextureSize;
uniform vec3 accretionDiskColor;
uniform vec3 accretionDiskInnerColor;
uniform vec3 accretionDiskOuterColor;

in vec2 texCoord;
out vec4 fragColor;

const float pi = 3.14159265;
const float CORE_RADIUS = 0.3;
const float CORE_EDGE_SOFTNESS = 0.01;
const vec3 CORE_COLOR = vec3(0.0);

const int SOURCE_MAX_ITERATIONS = 200;
const int SOURCE_MIN_ITERATIONS = 60;
const int MAX_ITERATIONS = 320;
const int MIN_ITERATIONS = 30;

mat3 rotateX(float angle)
{
    float c = cos(angle);
    float s = sin(angle);
    return mat3(
    1.0, 0.0, 0.0,
    0.0,  c,  -s,
    0.0,  s,   c
    );
}

mat3 rotateZ(float angle)
{
    float c = cos(angle);
    float s = sin(angle);
    return mat3(
    c,  -s, 0.0,
    s,   c, 0.0,
    0.0, 0.0, 1.0
    );
}

mat3 rotateAxis(vec3 axis, float angle)
{
    float c = cos(angle);
    float s = sin(angle);
    float t = 1.0 - c;
    vec3 a = normalize(axis);
    return mat3(
    t*a.x*a.x + c,      t*a.x*a.y - s*a.z,  t*a.x*a.z + s*a.y,
    t*a.x*a.y + s*a.z,  t*a.y*a.y + c,       t*a.y*a.z - s*a.x,
    t*a.x*a.z - s*a.y,  t*a.y*a.z + s*a.x,  t*a.z*a.z + c
    );
}

mat3 getTiltMatrix()
{
    return rotateX(tiltAngle);
}

mat3 getInvTiltMatrix()
{
    return rotateX(-tiltAngle);
}

float saturateF(float x)
{
    return clamp(x, 0.0, 1.0);
}

vec3 saturateV(vec3 x)
{
    return clamp(x, vec3(0.0), vec3(1.0));
}

float rand(vec2 coord)
{
    return saturateF(fract(sin(dot(coord, vec2(12.9898, 78.223))) * 43758.5453));
}

float pcurve(float x, float a, float b)
{
    float k = pow(a + b, a + b) / (pow(a, a) * pow(b, b));
    return k * pow(x, a) * pow(1.0 - x, b);
}

float noise(in vec3 x)
{
    vec3 p = floor(x);
    vec3 f = fract(x);
    f = f * f * (3.0 - 2.0 * f);
    vec2 uv = (p.xy + vec2(37.0, 17.0) * p.z) + f.xy;
    vec2 rg = textureLod(TextureSampler, (uv + 0.5) / noiseTextureSize, 0.0).yx;
    return -1.0 + 2.0 * mix(rg.x, rg.y, f.z);
}

float sdTorus(vec3 p, vec2 t)
{
    vec2 q = vec2(length(p.xz) - t.x, p.y);
    return length(q) - t.y;
}

vec3 clipToView(vec2 uv, float rawDepth)
{
    vec4 clipPos = vec4(uv * 2.0 - 1.0, rawDepth * 2.0 - 1.0, 1.0);
    vec4 viewPos = inverse(projectionMatrix) * clipPos;
    return viewPos.xyz / viewPos.w;
}

vec3 clipToWorld(vec2 uv, float rawDepth)
{
    vec4 clipPos = vec4(uv * 2.0 - 1.0, rawDepth * 2.0 - 1.0, 1.0);
    vec4 worldPos = inverse(projectionMatrix * modelViewMatrix) * clipPos;
    return worldPos.xyz / worldPos.w + cameraPos;
}

vec3 viewToWorld(vec3 viewPos)
{
    vec4 worldPos = inverse(modelViewMatrix) * vec4(viewPos, 1.0);
    return worldPos.xyz + cameraPos;
}

float worldPosToDistance(vec3 worldPos)
{
    return distance(cameraPos, worldPos);
}

float worldPosToViewDistance(vec3 worldPos)
{
    vec4 viewPos = modelViewMatrix * vec4(worldPos - cameraPos, 1.0);
    return -viewPos.z;
}

bool reconstructWorldPos(vec2 uv, out vec3 worldPos, out float linearDistance)
{
    float rawDepth = texture(MainDepthSampler, uv).r;

    if (rawDepth >= 0.9999)
    {
        worldPos = vec3(0.0);
        linearDistance = 1e10;
        return false;
    }

    worldPos = clipToWorld(uv, rawDepth);
    linearDistance = worldPosToViewDistance(worldPos);
    return true;
}

vec2 worldPosToScreenUV(vec3 worldPos)
{
    vec4 viewPos = modelViewMatrix * vec4(worldPos - cameraPos, 1.0);
    vec4 clipPos = projectionMatrix * viewPos;
    vec2 ndc = clipPos.xy / clipPos.w;
    return ndc * 0.5 + 0.5;
}

vec2 worldDirToScreenUV(vec3 origin, vec3 worldDir)
{
    return worldPosToScreenUV(origin + worldDir * 1000.0);
}

vec2 raySphereIntersect(vec3 ro, vec3 rd, vec3 center, float radius)
{
    vec3 oc = ro - center;
    float b = dot(oc, rd);
    float c = dot(oc, oc) - radius * radius;
    float h = b * b - c;

    if (h < 0.0) return vec2(-1.0);

    float sqrtH = sqrt(h);
    float t1 = -b - sqrtH;
    float t2 = -b + sqrtH;

    if (t1 < 0.0) t1 = 0.0;

    return vec2(t1, t2);
}

vec3 computeHaze(vec3 pos, float alpha, float invIterations)
{
    vec2 t = vec2(1.0, 0.01);
    float torusDist = length(sdTorus(pos + vec3(0.0, -0.05, 0.0), t));
    float bloomDisc = 1.0 / (torusDist * torusDist + 0.001);
    bloomDisc *= step(0.5, length(pos));
    return accretionDiskColor * bloomDisc * (2.9 * invIterations) * (1.0 - alpha);
}

void GasDisc(inout vec3 color, inout float alpha, vec3 pos, float lodBias)
{
    float discRadius = 3.2 * accretionDiskRadiusScale;
    float discWidth = 5.3 * accretionDiskRadiusScale;
    float discInner = discRadius - discWidth * 0.5;
    float discOuter = discRadius + discWidth * 0.5;

    float distFromCenter = length(pos.xz);
    float distFromDisc = pos.y;

    if (distFromCenter < discInner * 0.3 || distFromCenter > discOuter * 1.3) return;
    if (abs(distFromDisc) > 0.5 * accretionDiskThicknessScale) return;

    float distFromCenter3D = length(pos);

    float radialGradient = 1.0 - saturateF((distFromCenter - discInner) / discWidth * 0.8);
    float coverage = pcurve(radialGradient, 4.0, 0.9);

    float discThickness = 0.1 * radialGradient * accretionDiskThicknessScale;
    coverage *= saturateF(1.0 - abs(distFromDisc) / max(discThickness, 0.0001));

    float dustGlow = 1.0 / (pow(1.0 - radialGradient, 2.0) * 290.0 + 0.002);
    vec3 dustColor = accretionDiskColor * dustGlow * 8.2;

    coverage = saturateF(coverage * 0.9);

    float fade = pow((abs(distFromCenter - discInner) + 0.4), 4.0) * 0.04;
    float bloomFactor = 1.0 / (distFromDisc * distFromDisc * 40.0 + fade + 0.00002);
    vec3 b = accretionDiskColor * pow(bloomFactor, 1.5);

    float rg2 = pow(radialGradient, 2.0);
    float rg05 = pow(radialGradient, 0.5);
    b *= mix(accretionDiskInnerColor, accretionDiskOuterColor, vec3(rg2));
    b *= mix(accretionDiskInnerColor, accretionDiskColor, vec3(rg05));

    dustColor = mix(dustColor, b * 150.0, saturateF(1.0 - coverage));
    coverage = saturateF(coverage + bloomFactor * bloomFactor * 0.1);

    if (coverage < 0.01) return;

    float rawAngle = atan(-pos.x, -pos.z);
    float angleNorm = rawAngle / (2.0 * pi) + 0.5;

    const float SEAM_WIDTH = 0.08;

    float seamBlend = 0.0;
    if (angleNorm < SEAM_WIDTH)
    seamBlend = 1.0 - angleNorm / SEAM_WIDTH;
    else if (angleNorm > 1.0 - SEAM_WIDTH)
    seamBlend = (angleNorm - (1.0 - SEAM_WIDTH)) / SEAM_WIDTH;

    seamBlend = smoothstep(0.0, 1.0, seamBlend);

    float angleCoordA = angleNorm * 2.0 * pi * 1.5;
    float angleCoordB = (fract(angleNorm + 0.5)) * 2.0 * pi * 1.5;

    float speed = 0.06;

    vec3 rcA;
    rcA.x = distFromCenter * 1.5 + 0.55;
    rcA.y = angleCoordA;
    rcA.z = distFromDisc * 1.5;
    rcA *= 0.95;

    float noise1A = 1.0;
    vec3 rcTmpA = rcA;
    rcTmpA.y += time * speed;
    noise1A *= noise(rcTmpA * 3.0) * 0.5 + 0.5;
    rcTmpA.y = rcA.y - time * speed;
    noise1A *= noise(rcTmpA * 6.0) * 0.5 + 0.5;

    if (lodBias < 0.7)
    {
        rcTmpA.y = rcA.y + time * speed;
        noise1A *= noise(rcTmpA * 12.0) * 0.5 + 0.5;
        rcTmpA.y = rcA.y - time * speed;
        noise1A *= noise(rcTmpA * 24.0) * 0.5 + 0.5;
    }

    vec3 rcB;
    rcB.x = distFromCenter * 1.5 + 0.55;
    rcB.y = angleCoordB;
    rcB.z = distFromDisc * 1.5;
    rcB *= 0.95;

    float noise1B = 1.0;
    vec3 rcTmpB = rcB;
    rcTmpB.y += time * speed;
    noise1B *= noise(rcTmpB * 3.0) * 0.5 + 0.5;
    rcTmpB.y = rcB.y - time * speed;
    noise1B *= noise(rcTmpB * 6.0) * 0.5 + 0.5;

    if (lodBias < 0.7)
    {
        rcTmpB.y = rcB.y + time * speed;
        noise1B *= noise(rcTmpB * 12.0) * 0.5 + 0.5;
        rcTmpB.y = rcB.y - time * speed;
        noise1B *= noise(rcTmpB * 24.0) * 0.5 + 0.5;
    }

    float noise1 = mix(noise1A, noise1B, seamBlend);

    float noise2A = 2.0;
    rcTmpA = rcA + 30.0;
    noise2A *= noise(rcTmpA * 3.0) * 0.5 + 0.5;
    rcTmpA.y = rcA.y + 30.0 + time * speed;
    noise2A *= noise(rcTmpA * 6.0) * 0.5 + 0.5;
    rcTmpA.y = rcA.y + 30.0 - time * speed;
    noise2A *= noise(rcTmpA * 12.0) * 0.5 + 0.5;

    if (lodBias < 0.5)
    {
        rcTmpA.y = rcA.y + 30.0 + time * speed;
        noise2A *= noise(rcTmpA * 24.0) * 0.5 + 0.5;
        rcTmpA.y = rcA.y + 30.0 - time * speed;
        noise2A *= noise(rcTmpA * 48.0) * 0.5 + 0.5;
        rcTmpA.y = rcA.y + 30.0 + time * speed;
        noise2A *= noise(rcTmpA * 92.0) * 0.5 + 0.5;
    }

    float noise2B = 2.0;
    rcTmpB = rcB + 30.0;
    noise2B *= noise(rcTmpB * 3.0) * 0.5 + 0.5;
    rcTmpB.y = rcB.y + 30.0 + time * speed;
    noise2B *= noise(rcTmpB * 6.0) * 0.5 + 0.5;
    rcTmpB.y = rcB.y + 30.0 - time * speed;
    noise2B *= noise(rcTmpB * 12.0) * 0.5 + 0.5;

    if (lodBias < 0.5)
    {
        rcTmpB.y = rcB.y + 30.0 + time * speed;
        noise2B *= noise(rcTmpB * 24.0) * 0.5 + 0.5;
        rcTmpB.y = rcB.y + 30.0 - time * speed;
        noise2B *= noise(rcTmpB * 48.0) * 0.5 + 0.5;
        rcTmpB.y = rcB.y + 30.0 + time * speed;
        noise2B *= noise(rcTmpB * 92.0) * 0.5 + 0.5;
    }

    float noise2 = mix(noise2A, noise2B, seamBlend);

    float fineNoise = noise1 * 0.998 + 0.002;
    dustColor *= mix(1.0, fineNoise, diskNoiseStrength);

    noise2 = mix(accretionDiskDensity, 1.0, saturateF(noise2));
    coverage *= mix(1.0, noise2, diskNoiseStrength);

    float colorAngleA = angleCoordA;
    float colorAngleB = angleCoordB;

    vec2 colorUvA = vec2(colorAngleA, rcA.x) * vec2(0.15, 0.27) + vec2(time * speed * 0.5 * 0.15, 0.0);
    vec2 colorUvB = vec2(colorAngleB, rcB.x) * vec2(0.15, 0.27) + vec2(time * speed * 0.5 * 0.15, 0.0);
    vec3 colorTexA = pow(texture(ColorSampler, colorUvA).rgb, vec3(2.0)) * 4.0;
    vec3 colorTexB = pow(texture(ColorSampler, colorUvB).rgb, vec3(2.0)) * 4.0;
    vec3 colorTex = mix(colorTexA, colorTexB, seamBlend);
    dustColor *= mix(vec3(1.0), colorTex, diskTextureStrength) * accretionDiskColor;

    float invIter = 2000.0 / float(SOURCE_MAX_ITERATIONS);
    coverage = saturateF(coverage * invIter);
    dustColor = max(vec3(0.0), dustColor);
    coverage *= pcurve(radialGradient, 5.0, 0.9);

    color = (1.0 - alpha) * dustColor * coverage + color;
    alpha = (1.0 - alpha) * coverage + alpha;
}

float CoreSphere(vec3 pos)
{
    float dist = length(pos);
    float inner = CORE_RADIUS - CORE_EDGE_SOFTNESS;
    float outer = CORE_RADIUS + CORE_EDGE_SOFTNESS;
    return 1.0 - smoothstep(inner, outer, dist);
}

void WarpSpace(inout vec3 eyevec, inout vec3 raypos, float invIterations)
{
    float singularityDist2 = dot(raypos, raypos);
    float warpFactor = 1.0 / (singularityDist2 + 0.000001);
    vec3 singularityVector = -raypos * inversesqrt(singularityDist2 + 0.000001);
    eyevec = normalize(eyevec + singularityVector * warpFactor * 5.0 * invIterations * intensity);
}

float getDistanceStepMultiplier(float distToEntity)
{
    float minR = 90.0;
    float maxR = 300.0;

    if (distToEntity <= minR)
    {
        return 0.3;
    }
    else if (distToEntity >= maxR)
    {
        return 1.0;
    }
    else
    {
        float t = (distToEntity - minR) / (maxR - minR);
        return mix(0.3, 1.0, t);
    }
}

vec3 traceLensedRay(vec3 localRo, vec3 localRd, float distToEntity, out bool swallowed)
{
    mat3 tilt = getTiltMatrix();
    localRo = tilt * localRo;
    localRd = tilt * localRd;

    float effectScale = 8.0;
    vec3 eyepos = localRo * effectScale;
    vec3 eyevec = normalize(localRd);

    const float far = 15.0;

    float distFactor = saturateF((distToEntity - scale * 0.5) / (scale * 8.0));
    int iterations = int(mix(float(SOURCE_MAX_ITERATIONS) * 0.5,
                             float(SOURCE_MIN_ITERATIONS) * 0.5, distFactor) * renderQuality);
    iterations = clamp(iterations, MIN_ITERATIONS, MAX_ITERATIONS);

    float invIterations = 1.0 / float(iterations);
    float stepLen = far * invIterations;

    float stepMultiplier = getDistanceStepMultiplier(distToEntity);

    vec3 raypos = eyepos;
    swallowed = false;

    for (int i = 0; i < MAX_ITERATIONS; i++)
    {
        if (i >= iterations) break;

        WarpSpace(eyevec, raypos, invIterations);
        raypos += eyevec * stepMultiplier;

        float distSq = dot(raypos, raypos);
        float coreR = CORE_RADIUS * 0.8;
        if (distSq < coreR * coreR)
        {
            swallowed = true;
            return vec3(0.0);
        }
    }

    mat3 invTilt = getInvTiltMatrix();
    return invTilt * eyevec;
}
struct RenderResult
{
    vec3 color;
    float alpha;
    vec3 lensedDir;
    bool swallowed;
    float nearestHitViewDist;
};

RenderResult renderBlackhole3D(vec3 localRo, vec3 localRd, vec2 screenUV, float distToEntity, vec3 marchStartWorld)
{
    mat3 tilt = getTiltMatrix();
    mat3 invTilt = getInvTiltMatrix();
    localRo = tilt * localRo;
    localRd = tilt * localRd;

    float effectScale = 8.0;

    vec3 eyepos = localRo * effectScale;
    vec3 eyevec = normalize(localRd);

    const float far = 15.0;

    vec3 color = vec3(0.0);
    float alpha = 0.0;
    float traveledDistance = 0.0;

    vec3 lensEyeVec = normalize(localRd);
    vec3 lensRayPos = localRo * effectScale;
    bool swallowed = false;

    float nearestHitLocalDist = -1.0;

    float distFactor = saturateF((distToEntity - scale * 0.5) / (scale * 8.0));
    int iterations = int(mix(float(SOURCE_MAX_ITERATIONS), float(SOURCE_MIN_ITERATIONS), distFactor) * renderQuality);
    iterations = clamp(iterations, MIN_ITERATIONS, MAX_ITERATIONS);

    float lodBias = distFactor;

    float invIterations = 1.0 / float(iterations);
    float stepLen = far * invIterations;

    float dither = rand(screenUV + fract(time)) * ditherStrength;
    vec3 raypos = eyepos + eyevec * dither * stepLen;
    lensRayPos += lensEyeVec * dither * stepLen;

    for (int i = 0; i < MAX_ITERATIONS; i++)
    {
        if (i >= iterations) break;

        WarpSpace(eyevec, raypos, invIterations);
        raypos += eyevec * stepLen;
        traveledDistance += stepLen;

        WarpSpace(lensEyeVec, lensRayPos, invIterations);
        lensRayPos += lensEyeVec * stepLen;

        float lensDist2 = dot(lensRayPos, lensRayPos);
        float coreR = CORE_RADIUS * 0.8;
        if (lensDist2 < coreR * coreR)
        {
            swallowed = true;
        }

        float distSq = dot(raypos, raypos);
        float coreOuterSq = (CORE_RADIUS + CORE_EDGE_SOFTNESS);
        coreOuterSq *= coreOuterSq;

        if (distSq < coreOuterSq)
        {
            float dist = sqrt(distSq);
            float coreAlpha = 1.0 - smoothstep(CORE_RADIUS - CORE_EDGE_SOFTNESS,
                                               CORE_RADIUS + CORE_EDGE_SOFTNESS, dist);

            if (coreAlpha > 0.0)
            {
                color = mix(color, CORE_COLOR, (1.0 - alpha) * coreAlpha);
                alpha = mix(alpha, 1.0, coreAlpha);

                if (nearestHitLocalDist < 0.0)
                nearestHitLocalDist = traveledDistance;

                if (alpha > 0.99) break;
                if (coreAlpha > 0.9) continue;
            }
        }

        float distXZ = length(raypos.xz);
        if (abs(raypos.y) < 0.5 && distXZ > 0.15 && distXZ < 7.6)
        {
            float prevAlpha = alpha;
            GasDisc(color, alpha, raypos, lodBias);

            if (nearestHitLocalDist < 0.0 && alpha > 0.01 && alpha > prevAlpha + 0.001)
            nearestHitLocalDist = traveledDistance;
        }

        if (distSq < 100.0)
        color += computeHaze(raypos, alpha, invIterations);

        if (alpha > 0.99) break;
    }

    color *= 0.02;
    color = pow(color, vec3(1.5));
    color = color / (1.0 + color);
    color = pow(color, vec3(1.0 / 1.5));
    color = mix(color, color * color * (3.0 - 2.0 * color), vec3(1.0));
    color = pow(color, vec3(1.3, 1.20, 1.0));
    color = saturateV(color * 1.01);
    color = pow(color, vec3(0.7 / 2.2));

    float nearestHitViewDist = -1.0;
    if (nearestHitLocalDist > 0.0)
    {
        float worldDist = nearestHitLocalDist / effectScale * scale;
        vec3 worldLocalRd = invTilt * localRd;
        vec3 approxHitWorld = marchStartWorld + worldLocalRd * worldDist;
        vec4 hitView = modelViewMatrix * vec4(approxHitWorld - cameraPos, 1.0);
        nearestHitViewDist = -hitView.z;
    }

    lensEyeVec = invTilt * lensEyeVec;

    RenderResult result;
    result.color = color;
    result.alpha = alpha;
    result.lensedDir = lensEyeVec;
    result.swallowed = swallowed;
    result.nearestHitViewDist = nearestHitViewDist;
    return result;
}

void main()
{
    // Source-style lens has no visible shell; keep the old softness API linked without affecting valid values.
    if (lensBoundarySoftness < 0.0)
    {
        discard;
    }

    float sceneRawDepth = texture(MainDepthSampler, texCoord).r;
    vec3 sceneColor = texture(MainColorSampler, texCoord).rgb;

    vec3 sceneWorldPos;
    float sceneViewDist;
    bool hasSceneGeometry = reconstructWorldPos(texCoord, sceneWorldPos, sceneViewDist);

    if (!hasSceneGeometry) sceneViewDist = 1e10;

    vec3 rayNear = clipToWorld(texCoord, 0.0);
    vec3 rayFar = clipToWorld(texCoord, 1.0);
    vec3 rayOrigin = rayNear;
    vec3 rayDir = normalize(rayFar - rayNear);

    float distToEntity = distance(cameraPos, entityPos);

    vec2 sphereT = raySphereIntersect(rayOrigin, rayDir, entityPos, scale);

    vec4 entityView = modelViewMatrix * vec4(entityPos - cameraPos, 1.0);
    float entityViewDist = -entityView.z;

    bool inRenderSphere = (sphereT.y >= 0.0);

    vec3 lensLocalRo = (rayOrigin - entityPos) / scale;
    vec3 lensLocalRd = rayDir;
    bool raySwallowed = false;
    vec3 lensedDir = traceLensedRay(lensLocalRo, lensLocalRd, distToEntity, raySwallowed);

    vec3 lensedSceneColor = sceneColor;
    if (!raySwallowed && length(lensedDir) > 0.001)
    {
        vec2 lensedUV = worldDirToScreenUV(rayOrigin, lensedDir);
        lensedUV = clamp(lensedUV, vec2(0.001), vec2(0.999));
        lensedSceneColor = texture(MainColorSampler, lensedUV).rgb;
    }
    else
    {
        lensedSceneColor = vec3(0.0);
    }

    if (inRenderSphere)
    {
        float sphereEnterViewDist = 1e10;
        if (sphereT.x >= 0.0)
        {
            vec3 sphereEnterWorld = rayOrigin + rayDir * sphereT.x;
            vec4 sphereEnterView = modelViewMatrix * vec4(sphereEnterWorld - cameraPos, 1.0);
            sphereEnterViewDist = -sphereEnterView.z;
        }
        else
        {
            sphereEnterViewDist = 0.0;
        }

        if (hasSceneGeometry && sceneViewDist < sphereEnterViewDist - 0.1)
        {
            fragColor = vec4(lensedSceneColor, 1.0);
            gl_FragDepth = sceneRawDepth;
            return;
        }

        vec3 marchStart;
        if (sphereT.x <= 0.0) marchStart = rayOrigin;
        else marchStart = rayOrigin + rayDir * sphereT.x;

        vec3 localRo = (marchStart - entityPos) / scale;
        vec3 localRd = rayDir;

        RenderResult result = renderBlackhole3D(localRo, localRd, texCoord, distToEntity, marchStart);

        if (result.alpha > 0.001)
        {
            float resultAlpha = result.alpha;
            if (resultAlpha <= 0.001)
            {
                fragColor = vec4(lensedSceneColor, 1.0);
                gl_FragDepth = hasSceneGeometry ? sceneRawDepth : 0.9999;
                return;
            }

            bool hitOccluded = false;
            if (hasSceneGeometry && result.nearestHitViewDist > 0.0)
            {
                if (sceneViewDist < result.nearestHitViewDist - 0.5)
                {
                    hitOccluded = true;
                }
            }

            if (hitOccluded)
            {
                fragColor = vec4(lensedSceneColor, 1.0);
                gl_FragDepth = sceneRawDepth;
                return;
            }

            vec3 bgColor = lensedSceneColor;

            vec3 finalColor = mix(bgColor, result.color, resultAlpha);

            if (result.nearestHitViewDist > 0.0)
            {
                vec4 hitClip = projectionMatrix * vec4(0.0, 0.0, -result.nearestHitViewDist, 1.0);
                float ndcDepth = hitClip.z / hitClip.w;
                gl_FragDepth = ndcDepth * 0.5 + 0.5;
            }
            else
            {
                vec4 entityClip = projectionMatrix * entityView;
                float ndcDepth = entityClip.z / entityClip.w;
                gl_FragDepth = ndcDepth * 0.5 + 0.5;
            }

            gl_FragDepth = clamp(gl_FragDepth, 0.0, 0.9999);

            fragColor = vec4(finalColor, 1.0);
        }
        else
        {
            if (raySwallowed)
            {
                fragColor = vec4(0.0, 0.0, 0.0, 1.0);
                vec4 entityClip = projectionMatrix * entityView;
                gl_FragDepth = clamp(entityClip.z / entityClip.w * 0.5 + 0.5, 0.0, 0.9999);
            }
            else
            {
                fragColor = vec4(lensedSceneColor, 1.0);
                gl_FragDepth = hasSceneGeometry ? sceneRawDepth : 0.9999;
            }
        }
    }
    else
    {
        fragColor = vec4(lensedSceneColor, 1.0);
        gl_FragDepth = hasSceneGeometry ? sceneRawDepth : 0.9999;
    }
}
