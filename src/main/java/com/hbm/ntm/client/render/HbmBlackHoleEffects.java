package com.hbm.ntm.client.render;

import com.hbm.ntm.HbmNtm;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@OnlyIn(Dist.CLIENT)
public final class HbmBlackHoleEffects {
    private static final ResourceLocation BLACK_HOLE = new ResourceLocation(HbmNtm.MOD_ID, "black_hole");
    private static final int NOISE_TEXTURE_SIZE = 256;
    // Legacy RenderBlackHole scales the whole model by size; the vortex swirl fades out at 6 * size.
    private static final float LEGACY_EFFECT_RADIUS_MULTIPLIER = 6.0F;
    private static final int GL_REPEAT = 10497;
    private static final int GL_LINEAR = 9729;
    private static final int GL_NEAREST = 9728;
    private static final float DEG_TO_RAD = (float) (Math.PI / 180.0D);
    private static final RenderLevelStageEvent.Stage RENDER_STAGE = RenderLevelStageEvent.Stage.AFTER_LEVEL;
    private static final List<BlackHole> ACTIVE = new ArrayList<>();
    private static final Map<Integer, TrackedBlackHole> TRACKED = new ConcurrentHashMap<>();
    private static final List<RenderJob> RENDER_JOBS = new ArrayList<>();
    private static final Matrix4f WORLD_VIEW_MATRIX = new Matrix4f();

    private static ShaderInstance blackHoleShader;
    private static TextureTarget sceneCopy;
    private static DynamicTexture noiseTexture;
    private static DynamicTexture colorRampTexture;
    private static int configuredSceneColorTextureId = -1;
    private static int configuredSceneDepthTextureId = -1;
    private static int configuredNoiseTextureId = -1;
    private static int configuredColorRampTextureId = -1;
    private static boolean sceneCopySamplingDirty = true;
    private static Uniform entityPosUniform;
    private static Uniform scaleUniform;
    private static Uniform accretionDiskRadiusScaleUniform;
    private static Uniform accretionDiskThicknessScaleUniform;
    private static Uniform accretionDiskDensityUniform;
    private static Uniform tiltAngleUniform;
    private static Uniform intensityUniform;
    private static Uniform renderQualityUniform;
    private static Uniform ditherStrengthUniform;
    private static Uniform lensBoundarySoftnessUniform;
    private static Uniform diskNoiseStrengthUniform;
    private static Uniform diskTextureStrengthUniform;
    private static Uniform accretionDiskColorUniform;
    private static Uniform accretionDiskInnerColorUniform;
    private static Uniform accretionDiskOuterColorUniform;
    private static Uniform screenSizeUniform;
    private static Uniform projectionMatrixUniform;
    private static Uniform modelViewMatrixUniform;
    private static Uniform cameraPosUniform;
    private static Uniform timeUniform;
    private static Uniform noiseTextureSizeUniform;

    public static void registerShaders(RegisterShadersEvent event) throws IOException {
        event.registerShader(new ShaderInstance(event.getResourceProvider(), BLACK_HOLE,
                DefaultVertexFormat.POSITION), shader -> {
                    blackHoleShader = shader;
                    cacheUniforms(shader);
                });
    }

    public static void spawnBlackHole(double x, double y, double z, BlackHoleSpec spec) {
        spawnBlackHole(x, y, z, spec, 0);
    }

    public static void spawnBlackHole(double x, double y, double z, BlackHoleSpec spec, int initialAge) {
        if (spec == null || spec.scale <= 0.0F || spec.intensity <= 0.0F || initialAge >= spec.lifetime) {
            return;
        }
        ACTIVE.add(new BlackHole(x, y, z, spec, Math.max(0, initialAge)));
    }

    public static void updateTrackedBlackHole(int key, double x, double y, double z, BlackHoleSpec spec, int age) {
        if (key == 0 || spec == null || spec.scale <= 0.0F || spec.intensity <= 0.0F) {
            return;
        }
        TrackedBlackHole blackHole = TRACKED.get(key);
        if (blackHole == null) {
            TRACKED.put(key, new TrackedBlackHole(x, y, z, spec, Math.max(0, age), 2));
        } else {
            blackHole.update(x, y, z, spec, Math.max(0, age), 2);
        }
    }

    public static void removeTrackedBlackHole(int key) {
        TRACKED.remove(key);
    }

    public static boolean isRenderStage(RenderLevelStageEvent.Stage stage) {
        return stage == RENDER_STAGE;
    }

    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            clearAll();
            return;
        }

        for (int i = ACTIVE.size() - 1; i >= 0; i--) {
            BlackHole blackHole = ACTIVE.get(i);
            if (blackHole.age++ >= blackHole.lifetime) {
                ACTIVE.remove(i);
            }
        }

        java.util.Iterator<Map.Entry<Integer, TrackedBlackHole>> trackedIterator = TRACKED.entrySet().iterator();
        while (trackedIterator.hasNext()) {
            Map.Entry<Integer, TrackedBlackHole> entry = trackedIterator.next();
            TrackedBlackHole blackHole = entry.getValue();
            if (blackHole.ttl-- <= 0) {
                trackedIterator.remove();
            }
        }
    }

    public static void render(RenderLevelStageEvent event) {
        if ((ACTIVE.isEmpty() && TRACKED.isEmpty()) || blackHoleShader == null
                || event.getStage() != RENDER_STAGE) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }

        RenderTarget mainTarget = minecraft.getMainRenderTarget();
        ensureSceneCopy(mainTarget);
        ensureDefaultTextures();

        Camera camera = event.getCamera();
        Vec3 cameraPos = camera.getPosition();
        float partialTick = event.getPartialTick();
        float time = minecraft.level.getGameTime() + partialTick;

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();

        int jobCount = collectRenderJobs(cameraPos, partialTick);
        sortRenderJobsFarToNear(jobCount);
        for (int i = 0; i < jobCount; i++) {
            RenderJob job = RENDER_JOBS.get(i);
            beginBlackHolePass(mainTarget, event, camera, cameraPos, time);
            BlackHoleSpec spec = job.spec;
            setUniform(entityPosUniform, (float) job.x, (float) job.y, (float) job.z);
            setUniform(scaleUniform, spec.scale);
            setUniform(accretionDiskRadiusScaleUniform, spec.accretionDiskRadiusScale);
            setUniform(accretionDiskThicknessScaleUniform, spec.accretionDiskThicknessScale);
            setUniform(accretionDiskDensityUniform, spec.accretionDiskDensity);
            setUniform(tiltAngleUniform, spec.tiltAngle);
            setUniform(intensityUniform, Mth.clamp(spec.intensity * job.alpha, 0.0F, 8.0F));
            setUniform(renderQualityUniform, spec.renderQuality);
            setUniform(ditherStrengthUniform, spec.ditherStrength);
            setUniform(lensBoundarySoftnessUniform, spec.lensBoundarySoftness);
            setUniform(diskNoiseStrengthUniform, spec.diskNoiseStrength);
            setUniform(diskTextureStrengthUniform, spec.diskTextureStrength);
            setUniform(accretionDiskColorUniform, spec.diskColorR, spec.diskColorG, spec.diskColorB);
            setUniform(accretionDiskInnerColorUniform, spec.diskInnerColorR, spec.diskInnerColorG, spec.diskInnerColorB);
            setUniform(accretionDiskOuterColorUniform, spec.diskOuterColorR, spec.diskOuterColorG, spec.diskOuterColorB);
            drawFullscreenQuad();
        }

        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        mainTarget.bindWrite(false);
    }

    private static int collectRenderJobs(Vec3 cameraPos, float partialTick) {
        int count = 0;
        for (int i = 0; i < ACTIVE.size(); i++) {
            BlackHole blackHole = ACTIVE.get(i);
            float age = blackHole.age + partialTick;
            float alpha = blackHole.alpha(age);
            if (alpha > 0.0F) {
                addRenderJob(count++, blackHole.x, blackHole.y, blackHole.z, blackHole.spec, alpha, cameraPos);
            }
        }
        for (TrackedBlackHole blackHole : TRACKED.values()) {
            float age = blackHole.age + partialTick;
            float alpha = blackHole.alpha(age);
            if (alpha > 0.0F) {
                addRenderJob(count++, blackHole.x, blackHole.y, blackHole.z, blackHole.spec, alpha, cameraPos);
            }
        }
        return count;
    }

    private static void addRenderJob(int index, double x, double y, double z, BlackHoleSpec spec, float alpha,
            Vec3 cameraPos) {
        while (RENDER_JOBS.size() <= index) {
            RENDER_JOBS.add(new RenderJob());
        }
        RENDER_JOBS.get(index).set(x, y, z, spec, alpha, cameraPos);
    }

    private static void sortRenderJobsFarToNear(int count) {
        for (int i = 1; i < count; i++) {
            RenderJob job = RENDER_JOBS.get(i);
            int j = i - 1;
            while (j >= 0 && RENDER_JOBS.get(j).distanceToCameraSqr < job.distanceToCameraSqr) {
                RENDER_JOBS.set(j + 1, RENDER_JOBS.get(j));
                j--;
            }
            RENDER_JOBS.set(j + 1, job);
        }
    }

    private static void beginBlackHolePass(RenderTarget mainTarget, RenderLevelStageEvent event, Camera camera,
            Vec3 cameraPos, float time) {
        copyMainTarget(mainTarget);
        ensureSceneCopySampling();
        ensureEffectTextureSampling();
        RenderSystem.setShader(() -> blackHoleShader);
        blackHoleShader.setSampler("MainColorSampler", sceneCopy);
        blackHoleShader.setSampler("MainDepthSampler", Integer.valueOf(sceneCopy.getDepthTextureId()));
        blackHoleShader.setSampler("TextureSampler", noiseTexture);
        blackHoleShader.setSampler("ColorSampler", colorRampTexture);
        setUniform(screenSizeUniform, (float) mainTarget.viewWidth, (float) mainTarget.viewHeight);
        setUniform(projectionMatrixUniform, event.getProjectionMatrix());
        setUniform(modelViewMatrixUniform, worldViewMatrix(camera));
        setUniform(cameraPosUniform, (float) cameraPos.x, (float) cameraPos.y, (float) cameraPos.z);
        setUniform(timeUniform, time);
        setUniform(noiseTextureSizeUniform, (float) NOISE_TEXTURE_SIZE);
    }

    public static void clearAll() {
        ACTIVE.clear();
        TRACKED.clear();
    }

    private static void ensureSceneCopy(RenderTarget mainTarget) {
        if (sceneCopy == null) {
            sceneCopy = new TextureTarget(mainTarget.width, mainTarget.height, true, Minecraft.ON_OSX);
            sceneCopySamplingDirty = true;
        } else if (sceneCopy.width != mainTarget.width || sceneCopy.height != mainTarget.height) {
            sceneCopy.resize(mainTarget.width, mainTarget.height, Minecraft.ON_OSX);
            sceneCopySamplingDirty = true;
        }
    }

    private static void copyMainTarget(RenderTarget mainTarget) {
        GlStateManager._glBindFramebuffer(GlConst.GL_READ_FRAMEBUFFER, mainTarget.frameBufferId);
        GlStateManager._glBindFramebuffer(GlConst.GL_DRAW_FRAMEBUFFER, sceneCopy.frameBufferId);
        GlStateManager._glBlitFrameBuffer(0, 0, mainTarget.width, mainTarget.height,
                0, 0, sceneCopy.width, sceneCopy.height,
                GlConst.GL_COLOR_BUFFER_BIT | GlConst.GL_DEPTH_BUFFER_BIT, GlConst.GL_NEAREST);
        GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, mainTarget.frameBufferId);
        RenderSystem.viewport(0, 0, mainTarget.viewWidth, mainTarget.viewHeight);
    }

    private static void ensureDefaultTextures() {
        if (noiseTexture == null) {
            NativeImage noise = new NativeImage(NOISE_TEXTURE_SIZE, NOISE_TEXTURE_SIZE, false);
            Random random = new Random(0x48424D4C);
            for (int y = 0; y < NOISE_TEXTURE_SIZE; y++) {
                for (int x = 0; x < NOISE_TEXTURE_SIZE; x++) {
                    int value = random.nextInt(256);
                    noise.setPixelRGBA(x, y, rgba(value, value, value, 255));
                }
            }
            noiseTexture = new DynamicTexture(noise);
            noiseTexture.upload();
            configureTiledBilinear(noiseTexture);
            configuredNoiseTextureId = noiseTexture.getId();
        }
        if (colorRampTexture == null) {
            colorRampTexture = createColorRampTexture(1.0F, 1.0F, 1.0F,
                    1.7F, 0.5F, 0.1F,
                    0.5F, 0.6F, 1.0F);
            configuredColorRampTextureId = colorRampTexture.getId();
        }
    }

    private static void ensureEffectTextureSampling() {
        configuredNoiseTextureId = ensureTiledBilinear(noiseTexture, configuredNoiseTextureId);
        configuredColorRampTextureId = ensureTiledBilinear(colorRampTexture, configuredColorRampTextureId);
    }

    private static int ensureTiledBilinear(DynamicTexture texture, int configuredTextureId) {
        if (texture == null) {
            return -1;
        }
        int textureId = texture.getId();
        if (textureId > 0 && textureId != configuredTextureId) {
            configureTiledBilinear(texture);
            return textureId;
        }
        return configuredTextureId;
    }

    private static DynamicTexture createColorRampTexture(float cr, float cg, float cb,
            float ir, float ig, float ib, float or, float og, float ob) {
        NativeImage image = new NativeImage(256, 256, false);
        for (int y = 0; y < 256; y++) {
            float radial = y / 255.0F;
            float r = Mth.lerp(radial, ir, or) * cr;
            float g = Mth.lerp(radial, ig, og) * cg;
            float b = Mth.lerp(radial, ib, ob) * cb;
            int packed = rgba(toByte(r), toByte(g), toByte(b), 255);
            for (int x = 0; x < 256; x++) {
                image.setPixelRGBA(x, y, packed);
            }
        }
        DynamicTexture texture = new DynamicTexture(image);
        texture.upload();
        configureTiledBilinear(texture);
        return texture;
    }

    private static void configureTiledBilinear(DynamicTexture texture) {
        texture.bind();
        RenderSystem.texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        RenderSystem.texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        RenderSystem.texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_WRAP_S, GL_REPEAT);
        RenderSystem.texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_WRAP_T, GL_REPEAT);
    }

    private static void ensureSceneCopySampling() {
        int colorTextureId = sceneCopy.getColorTextureId();
        int depthTextureId = sceneCopy.getDepthTextureId();
        if (!sceneCopySamplingDirty
                && colorTextureId == configuredSceneColorTextureId
                && depthTextureId == configuredSceneDepthTextureId) {
            return;
        }
        configureClampedTexture(colorTextureId, GL_LINEAR);
        configureClampedTexture(depthTextureId, GL_NEAREST);
        configuredSceneColorTextureId = colorTextureId;
        configuredSceneDepthTextureId = depthTextureId;
        sceneCopySamplingDirty = false;
    }

    private static void configureClampedTexture(int textureId, int filter) {
        if (textureId <= 0) {
            return;
        }
        GlStateManager._bindTexture(textureId);
        RenderSystem.texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MIN_FILTER, filter);
        RenderSystem.texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MAG_FILTER, filter);
        RenderSystem.texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_WRAP_S, GlConst.GL_CLAMP_TO_EDGE);
        RenderSystem.texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_WRAP_T, GlConst.GL_CLAMP_TO_EDGE);
    }

    private static Matrix4f worldViewMatrix(Camera camera) {
        return WORLD_VIEW_MATRIX.identity()
                .rotateX(camera.getXRot() * DEG_TO_RAD)
                .rotateY((camera.getYRot() + 180.0F) * DEG_TO_RAD);
    }

    private static int toByte(float value) {
        return Mth.clamp(Math.round(value * 255.0F), 0, 255);
    }

    private static int rgba(int r, int g, int b, int a) {
        return (a & 255) << 24 | (b & 255) << 16 | (g & 255) << 8 | (r & 255);
    }

    private static void drawFullscreenQuad() {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        builder.vertex(-1.0D, -1.0D, 0.0D).endVertex();
        builder.vertex(1.0D, -1.0D, 0.0D).endVertex();
        builder.vertex(1.0D, 1.0D, 0.0D).endVertex();
        builder.vertex(-1.0D, 1.0D, 0.0D).endVertex();
        BufferUploader.drawWithShader(builder.end());
    }

    private static void cacheUniforms(ShaderInstance shader) {
        entityPosUniform = shader.getUniform("entityPos");
        scaleUniform = shader.getUniform("scale");
        accretionDiskRadiusScaleUniform = shader.getUniform("accretionDiskRadiusScale");
        accretionDiskThicknessScaleUniform = shader.getUniform("accretionDiskThicknessScale");
        accretionDiskDensityUniform = shader.getUniform("accretionDiskDensity");
        tiltAngleUniform = shader.getUniform("tiltAngle");
        intensityUniform = shader.getUniform("intensity");
        renderQualityUniform = shader.getUniform("renderQuality");
        ditherStrengthUniform = shader.getUniform("ditherStrength");
        lensBoundarySoftnessUniform = shader.getUniform("lensBoundarySoftness");
        diskNoiseStrengthUniform = shader.getUniform("diskNoiseStrength");
        diskTextureStrengthUniform = shader.getUniform("diskTextureStrength");
        accretionDiskColorUniform = shader.getUniform("accretionDiskColor");
        accretionDiskInnerColorUniform = shader.getUniform("accretionDiskInnerColor");
        accretionDiskOuterColorUniform = shader.getUniform("accretionDiskOuterColor");
        screenSizeUniform = shader.getUniform("screenSize");
        projectionMatrixUniform = shader.getUniform("projectionMatrix");
        modelViewMatrixUniform = shader.getUniform("modelViewMatrix");
        cameraPosUniform = shader.getUniform("cameraPos");
        timeUniform = shader.getUniform("time");
        noiseTextureSizeUniform = shader.getUniform("noiseTextureSize");
    }

    private static void setUniform(Uniform uniform, float value) {
        if (uniform != null) {
            uniform.set(value);
        }
    }

    private static void setUniform(Uniform uniform, float x, float y) {
        if (uniform != null) {
            uniform.set(x, y);
        }
    }

    private static void setUniform(Uniform uniform, float x, float y, float z) {
        if (uniform != null) {
            uniform.set(x, y, z);
        }
    }

    private static void setUniform(Uniform uniform, Matrix4f matrix) {
        if (uniform != null) {
            uniform.set(matrix);
        }
    }

    public static final class BlackHoleSpec {
        private final float scale;
        private final float eventHorizonRadius;
        private final int lifetime;
        private final float fadeInTicks;
        private final float fadeOutStartTick;
        private final float accretionDiskRadiusScale;
        private final float accretionDiskThicknessScale;
        private final float accretionDiskDensity;
        private final float tiltAngle;
        private final float intensity;
        private final float renderQuality;
        private final float ditherStrength;
        private final float lensBoundarySoftness;
        private final float diskNoiseStrength;
        private final float diskTextureStrength;
        private final float diskColorR;
        private final float diskColorG;
        private final float diskColorB;
        private final float diskInnerColorR;
        private final float diskInnerColorG;
        private final float diskInnerColorB;
        private final float diskOuterColorR;
        private final float diskOuterColorG;
        private final float diskOuterColorB;

        private BlackHoleSpec(float scale, float eventHorizonRadius, int lifetime, float fadeInTicks, float fadeOutStartTick,
                float accretionDiskRadiusScale, float accretionDiskThicknessScale,
                float accretionDiskDensity, float tiltAngle, float intensity, float renderQuality, float ditherStrength,
                float lensBoundarySoftness, float diskNoiseStrength, float diskTextureStrength,
                float diskColorR, float diskColorG, float diskColorB,
                float diskInnerColorR, float diskInnerColorG, float diskInnerColorB,
                float diskOuterColorR, float diskOuterColorG, float diskOuterColorB) {
            this.scale = Math.max(0.01F, scale);
            this.eventHorizonRadius = Mth.clamp(eventHorizonRadius, 0.01F, this.scale);
            this.lifetime = Math.max(1, lifetime);
            this.fadeInTicks = Mth.clamp(fadeInTicks, 0.0F, this.lifetime - 1.0F);
            this.fadeOutStartTick = Mth.clamp(fadeOutStartTick, 0.0F, this.lifetime - 1.0F);
            this.accretionDiskRadiusScale = Mth.clamp(accretionDiskRadiusScale, 0.25F, 3.0F);
            this.accretionDiskThicknessScale = Mth.clamp(accretionDiskThicknessScale, 0.25F, 3.0F);
            this.accretionDiskDensity = Mth.clamp(accretionDiskDensity, 0.0F, 1.0F);
            this.tiltAngle = tiltAngle;
            this.intensity = Math.max(0.0F, intensity);
            this.renderQuality = Mth.clamp(renderQuality, 0.35F, 1.6F);
            this.ditherStrength = Mth.clamp(ditherStrength, 0.0F, 3.0F);
            this.lensBoundarySoftness = Mth.clamp(lensBoundarySoftness, 0.02F, 0.6F);
            this.diskNoiseStrength = Mth.clamp(diskNoiseStrength, 0.0F, 1.0F);
            this.diskTextureStrength = Mth.clamp(diskTextureStrength, 0.0F, 1.0F);
            this.diskColorR = diskColorR;
            this.diskColorG = diskColorG;
            this.diskColorB = diskColorB;
            this.diskInnerColorR = diskInnerColorR;
            this.diskInnerColorG = diskInnerColorG;
            this.diskInnerColorB = diskInnerColorB;
            this.diskOuterColorR = diskOuterColorR;
            this.diskOuterColorG = diskOuterColorG;
            this.diskOuterColorB = diskOuterColorB;
        }

        public static BlackHoleSpec of(float eventHorizonRadius, int lifetime) {
            float safeEventHorizonRadius = Math.max(0.01F, eventHorizonRadius);
            return new BlackHoleSpec(safeEventHorizonRadius * LEGACY_EFFECT_RADIUS_MULTIPLIER,
                    safeEventHorizonRadius, lifetime,
                    10.0F, lifetime * 0.75F,
                    1.0F, 1.0F,
                    0.01F, 0.4363F, 1.0F, RenderPrecision.NATIVE.quality, RenderPrecision.NATIVE.ditherStrength,
                    0.6F, 1.0F, 0.35F,
                    1.0F, 1.0F, 1.0F,
                    1.7F, 0.5F, 0.1F,
                    0.5F, 0.6F, 1.0F);
        }

        public static BlackHoleSpec ofConfigured(float eventHorizonRadius, int lifetime,
                float fadeInTicks, float fadeOutStartTick,
                float accretionDiskDensity, float tiltAngle, float intensity,
                float renderQuality, float ditherStrength, float lensBoundarySoftness,
                float diskNoiseStrength, float diskTextureStrength,
                float diskColorR, float diskColorG, float diskColorB,
                float diskInnerColorR, float diskInnerColorG, float diskInnerColorB,
                float diskOuterColorR, float diskOuterColorG, float diskOuterColorB) {
            float safeEventHorizonRadius = Math.max(0.01F, eventHorizonRadius);
            return new BlackHoleSpec(safeEventHorizonRadius * LEGACY_EFFECT_RADIUS_MULTIPLIER,
                    safeEventHorizonRadius, lifetime,
                    fadeInTicks, fadeOutStartTick,
                    1.0F, 1.0F,
                    accretionDiskDensity, tiltAngle, intensity, renderQuality, ditherStrength,
                    lensBoundarySoftness, diskNoiseStrength, diskTextureStrength,
                    diskColorR, diskColorG, diskColorB,
                    diskInnerColorR, diskInnerColorG, diskInnerColorB,
                    diskOuterColorR, diskOuterColorG, diskOuterColorB);
        }

        public BlackHoleSpec withFade(float fadeInTicks, float fadeOutStartTick) {
            return new BlackHoleSpec(scale, eventHorizonRadius, lifetime, fadeInTicks, fadeOutStartTick,
                    accretionDiskRadiusScale, accretionDiskThicknessScale,
                    accretionDiskDensity, tiltAngle, intensity, renderQuality, ditherStrength,
                    lensBoundarySoftness, diskNoiseStrength, diskTextureStrength,
                    diskColorR, diskColorG, diskColorB,
                    diskInnerColorR, diskInnerColorG, diskInnerColorB,
                    diskOuterColorR, diskOuterColorG, diskOuterColorB);
        }

        public BlackHoleSpec withEventHorizonRadius(float eventHorizonRadius) {
            float safeEventHorizonRadius = Math.max(0.01F, eventHorizonRadius);
            return new BlackHoleSpec(safeEventHorizonRadius * LEGACY_EFFECT_RADIUS_MULTIPLIER,
                    safeEventHorizonRadius, lifetime, fadeInTicks, fadeOutStartTick,
                    accretionDiskRadiusScale, accretionDiskThicknessScale,
                    accretionDiskDensity, tiltAngle, intensity, renderQuality, ditherStrength,
                    lensBoundarySoftness, diskNoiseStrength, diskTextureStrength,
                    diskColorR, diskColorG, diskColorB,
                    diskInnerColorR, diskInnerColorG, diskInnerColorB,
                    diskOuterColorR, diskOuterColorG, diskOuterColorB);
        }

        public BlackHoleSpec withAccretionDiskScale(float radiusScale, float thicknessScale) {
            return new BlackHoleSpec(scale, eventHorizonRadius, lifetime, fadeInTicks, fadeOutStartTick,
                    radiusScale, thicknessScale,
                    accretionDiskDensity, tiltAngle, intensity, renderQuality, ditherStrength,
                    lensBoundarySoftness, diskNoiseStrength, diskTextureStrength,
                    diskColorR, diskColorG, diskColorB,
                    diskInnerColorR, diskInnerColorG, diskInnerColorB,
                    diskOuterColorR, diskOuterColorG, diskOuterColorB);
        }

        public BlackHoleSpec withAccretionDiskDensity(float accretionDiskDensity) {
            return new BlackHoleSpec(scale, eventHorizonRadius, lifetime, fadeInTicks, fadeOutStartTick,
                    accretionDiskRadiusScale, accretionDiskThicknessScale,
                    accretionDiskDensity, tiltAngle, intensity, renderQuality, ditherStrength,
                    lensBoundarySoftness, diskNoiseStrength, diskTextureStrength,
                    diskColorR, diskColorG, diskColorB,
                    diskInnerColorR, diskInnerColorG, diskInnerColorB,
                    diskOuterColorR, diskOuterColorG, diskOuterColorB);
        }

        public BlackHoleSpec withTiltAngle(float tiltAngle) {
            return new BlackHoleSpec(scale, eventHorizonRadius, lifetime, fadeInTicks, fadeOutStartTick,
                    accretionDiskRadiusScale, accretionDiskThicknessScale,
                    accretionDiskDensity, tiltAngle, intensity, renderQuality, ditherStrength,
                    lensBoundarySoftness, diskNoiseStrength, diskTextureStrength,
                    diskColorR, diskColorG, diskColorB,
                    diskInnerColorR, diskInnerColorG, diskInnerColorB,
                    diskOuterColorR, diskOuterColorG, diskOuterColorB);
        }

        public BlackHoleSpec withIntensity(float intensity) {
            return new BlackHoleSpec(scale, eventHorizonRadius, lifetime, fadeInTicks, fadeOutStartTick,
                    accretionDiskRadiusScale, accretionDiskThicknessScale,
                    accretionDiskDensity, tiltAngle, intensity, renderQuality, ditherStrength,
                    lensBoundarySoftness, diskNoiseStrength, diskTextureStrength,
                    diskColorR, diskColorG, diskColorB,
                    diskInnerColorR, diskInnerColorG, diskInnerColorB,
                    diskOuterColorR, diskOuterColorG, diskOuterColorB);
        }

        public BlackHoleSpec withRenderPrecision(RenderPrecision precision) {
            RenderPrecision safePrecision = precision == null ? RenderPrecision.HIGH : precision;
            return withRenderQuality(safePrecision.quality, safePrecision.ditherStrength);
        }

        public BlackHoleSpec withRenderQuality(float renderQuality, float ditherStrength) {
            return new BlackHoleSpec(scale, eventHorizonRadius, lifetime, fadeInTicks, fadeOutStartTick,
                    accretionDiskRadiusScale, accretionDiskThicknessScale,
                    accretionDiskDensity, tiltAngle, intensity, renderQuality, ditherStrength,
                    lensBoundarySoftness, diskNoiseStrength, diskTextureStrength,
                    diskColorR, diskColorG, diskColorB,
                    diskInnerColorR, diskInnerColorG, diskInnerColorB,
                    diskOuterColorR, diskOuterColorG, diskOuterColorB);
        }

        public BlackHoleSpec withLensBoundarySoftness(float lensBoundarySoftness) {
            return new BlackHoleSpec(scale, eventHorizonRadius, lifetime, fadeInTicks, fadeOutStartTick,
                    accretionDiskRadiusScale, accretionDiskThicknessScale,
                    accretionDiskDensity, tiltAngle, intensity, renderQuality, ditherStrength,
                    lensBoundarySoftness, diskNoiseStrength, diskTextureStrength,
                    diskColorR, diskColorG, diskColorB,
                    diskInnerColorR, diskInnerColorG, diskInnerColorB,
                    diskOuterColorR, diskOuterColorG, diskOuterColorB);
        }

        public BlackHoleSpec withDiskDetail(float diskNoiseStrength, float diskTextureStrength) {
            return new BlackHoleSpec(scale, eventHorizonRadius, lifetime, fadeInTicks, fadeOutStartTick,
                    accretionDiskRadiusScale, accretionDiskThicknessScale,
                    accretionDiskDensity, tiltAngle, intensity, renderQuality, ditherStrength,
                    lensBoundarySoftness, diskNoiseStrength, diskTextureStrength,
                    diskColorR, diskColorG, diskColorB,
                    diskInnerColorR, diskInnerColorG, diskInnerColorB,
                    diskOuterColorR, diskOuterColorG, diskOuterColorB);
        }

        public BlackHoleSpec withDiskColor(float r, float g, float b) {
            return new BlackHoleSpec(scale, eventHorizonRadius, lifetime, fadeInTicks, fadeOutStartTick,
                    accretionDiskRadiusScale, accretionDiskThicknessScale,
                    accretionDiskDensity, tiltAngle, intensity, renderQuality, ditherStrength,
                    lensBoundarySoftness, diskNoiseStrength, diskTextureStrength,
                    r, g, b,
                    diskInnerColorR, diskInnerColorG, diskInnerColorB,
                    diskOuterColorR, diskOuterColorG, diskOuterColorB);
        }

        public BlackHoleSpec withDiskRamp(float innerR, float innerG, float innerB,
                float outerR, float outerG, float outerB) {
            return new BlackHoleSpec(scale, eventHorizonRadius, lifetime, fadeInTicks, fadeOutStartTick,
                    accretionDiskRadiusScale, accretionDiskThicknessScale,
                    accretionDiskDensity, tiltAngle, intensity, renderQuality, ditherStrength,
                    lensBoundarySoftness, diskNoiseStrength, diskTextureStrength,
                    diskColorR, diskColorG, diskColorB,
                    innerR, innerG, innerB,
                    outerR, outerG, outerB);
        }

        public int lifetime() {
            return lifetime;
        }
    }

    public enum RenderPrecision {
        NATIVE(1.0F, 2.0F),
        LOW(0.5F, 2.4F),
        MEDIUM(0.75F, 2.2F),
        HIGH(1.15F, 1.35F),
        ULTRA(1.45F, 0.8F);

        private final float quality;
        private final float ditherStrength;

        RenderPrecision(float quality, float ditherStrength) {
            this.quality = quality;
            this.ditherStrength = ditherStrength;
        }
    }

    private static final class BlackHole {
        private final double x;
        private final double y;
        private final double z;
        private final BlackHoleSpec spec;
        private final int lifetime;
        private int age;

        private BlackHole(double x, double y, double z, BlackHoleSpec spec, int initialAge) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.spec = spec;
            this.lifetime = spec.lifetime;
            this.age = initialAge;
        }

        private float alpha(float progressAge) {
            float fadeIn = spec.fadeInTicks <= 0.0F ? 1.0F : Mth.clamp(progressAge / spec.fadeInTicks, 0.0F, 1.0F);
            if (progressAge <= spec.fadeOutStartTick) {
                return fadeIn;
            }
            float fadeOut = 1.0F - Mth.clamp((progressAge - spec.fadeOutStartTick)
                    / Math.max(1.0F, spec.lifetime - spec.fadeOutStartTick), 0.0F, 1.0F);
            return fadeIn * fadeOut * fadeOut;
        }
    }

    private static final class TrackedBlackHole {
        private double x;
        private double y;
        private double z;
        private BlackHoleSpec spec;
        private int age;
        private int ttl;

        private TrackedBlackHole(double x, double y, double z, BlackHoleSpec spec, int age, int ttl) {
            update(x, y, z, spec, age, ttl);
        }

        private void update(double x, double y, double z, BlackHoleSpec spec, int age, int ttl) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.spec = spec;
            this.age = age;
            this.ttl = ttl;
        }

        private float alpha(float progressAge) {
            float fadeIn = spec.fadeInTicks <= 0.0F ? 1.0F : Mth.clamp(progressAge / spec.fadeInTicks, 0.0F, 1.0F);
            if (progressAge <= spec.fadeOutStartTick) {
                return fadeIn;
            }
            float fadeOut = 1.0F - Mth.clamp((progressAge - spec.fadeOutStartTick)
                    / Math.max(1.0F, spec.lifetime - spec.fadeOutStartTick), 0.0F, 1.0F);
            return fadeIn * fadeOut * fadeOut;
        }
    }

    private static final class RenderJob {
        private double x;
        private double y;
        private double z;
        private BlackHoleSpec spec;
        private float alpha;
        private double distanceToCameraSqr;

        private void set(double x, double y, double z, BlackHoleSpec spec, float alpha, Vec3 cameraPos) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.spec = spec;
            this.alpha = alpha;
            double dx = x - cameraPos.x;
            double dy = y - cameraPos.y;
            double dz = z - cameraPos.z;
            this.distanceToCameraSqr = dx * dx + dy * dy + dz * dz;
        }
    }

    private HbmBlackHoleEffects() {
    }
}
