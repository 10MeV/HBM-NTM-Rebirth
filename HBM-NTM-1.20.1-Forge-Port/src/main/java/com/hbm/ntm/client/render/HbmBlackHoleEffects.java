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
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
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
import org.joml.Vector3f;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
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
    private static final RenderLevelStageEvent.Stage RENDER_STAGE = RenderLevelStageEvent.Stage.AFTER_LEVEL;
    private static final List<BlackHole> ACTIVE = new ArrayList<>();
    private static final Map<Integer, TrackedBlackHole> TRACKED = new ConcurrentHashMap<>();

    private static ShaderInstance blackHoleShader;
    private static TextureTarget sceneCopy;
    private static DynamicTexture noiseTexture;
    private static DynamicTexture colorRampTexture;

    public static void registerShaders(RegisterShadersEvent event) throws IOException {
        event.registerShader(new ShaderInstance(event.getResourceProvider(), BLACK_HOLE,
                DefaultVertexFormat.POSITION), shader -> blackHoleShader = shader);
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
        TRACKED.put(key, new TrackedBlackHole(x, y, z, spec, Math.max(0, age), 2));
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

        Iterator<BlackHole> iterator = ACTIVE.iterator();
        while (iterator.hasNext()) {
            BlackHole blackHole = iterator.next();
            if (blackHole.age++ >= blackHole.lifetime) {
                iterator.remove();
            }
        }

        Iterator<Map.Entry<Integer, TrackedBlackHole>> trackedIterator = TRACKED.entrySet().iterator();
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

        for (BlackHole blackHole : ACTIVE) {
            float age = blackHole.age + partialTick;
            float alpha = blackHole.alpha(age);
            if (alpha <= 0.0F) {
                continue;
            }

            beginBlackHolePass(mainTarget, event, camera, cameraPos, time);
            BlackHoleSpec spec = blackHole.spec;
            setUniform("entityPos", (float) blackHole.x, (float) blackHole.y, (float) blackHole.z);
            setUniform("scale", spec.scale);
            setUniform("accretionDiskRadiusScale", spec.accretionDiskRadiusScale);
            setUniform("accretionDiskThicknessScale", spec.accretionDiskThicknessScale);
            setUniform("accretionDiskDensity", spec.accretionDiskDensity);
            setUniform("tiltAngle", spec.tiltAngle);
            setUniform("intensity", Mth.clamp(spec.intensity * alpha, 0.0F, 8.0F));
            setUniform("renderQuality", spec.renderQuality);
            setUniform("ditherStrength", spec.ditherStrength);
            setUniform("lensBoundarySoftness", spec.lensBoundarySoftness);
            setUniform("diskNoiseStrength", spec.diskNoiseStrength);
            setUniform("diskTextureStrength", spec.diskTextureStrength);
            setUniform("accretionDiskColor", spec.diskColor);
            setUniform("accretionDiskInnerColor", spec.diskInnerColor);
            setUniform("accretionDiskOuterColor", spec.diskOuterColor);
            drawFullscreenQuad();
        }

        for (TrackedBlackHole blackHole : TRACKED.values()) {
            float age = blackHole.age + partialTick;
            float alpha = blackHole.alpha(age);
            if (alpha <= 0.0F) {
                continue;
            }

            beginBlackHolePass(mainTarget, event, camera, cameraPos, time);
            BlackHoleSpec spec = blackHole.spec;
            setUniform("entityPos", (float) blackHole.x, (float) blackHole.y, (float) blackHole.z);
            setUniform("scale", spec.scale);
            setUniform("accretionDiskRadiusScale", spec.accretionDiskRadiusScale);
            setUniform("accretionDiskThicknessScale", spec.accretionDiskThicknessScale);
            setUniform("accretionDiskDensity", spec.accretionDiskDensity);
            setUniform("tiltAngle", spec.tiltAngle);
            setUniform("intensity", Mth.clamp(spec.intensity * alpha, 0.0F, 8.0F));
            setUniform("renderQuality", spec.renderQuality);
            setUniform("ditherStrength", spec.ditherStrength);
            setUniform("lensBoundarySoftness", spec.lensBoundarySoftness);
            setUniform("diskNoiseStrength", spec.diskNoiseStrength);
            setUniform("diskTextureStrength", spec.diskTextureStrength);
            setUniform("accretionDiskColor", spec.diskColor);
            setUniform("accretionDiskInnerColor", spec.diskInnerColor);
            setUniform("accretionDiskOuterColor", spec.diskOuterColor);
            drawFullscreenQuad();
        }

        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        mainTarget.bindWrite(false);
    }

    private static void beginBlackHolePass(RenderTarget mainTarget, RenderLevelStageEvent event, Camera camera,
            Vec3 cameraPos, float time) {
        copyMainTarget(mainTarget);
        RenderSystem.setShader(() -> blackHoleShader);
        blackHoleShader.setSampler("MainColorSampler", sceneCopy);
        blackHoleShader.setSampler("MainDepthSampler", Integer.valueOf(sceneCopy.getDepthTextureId()));
        blackHoleShader.setSampler("TextureSampler", noiseTexture);
        blackHoleShader.setSampler("ColorSampler", colorRampTexture);
        setUniform("screenSize", (float) mainTarget.viewWidth, (float) mainTarget.viewHeight);
        setUniform("projectionMatrix", event.getProjectionMatrix());
        setUniform("modelViewMatrix", createWorldViewMatrix(camera));
        setUniform("cameraPos", (float) cameraPos.x, (float) cameraPos.y, (float) cameraPos.z);
        setUniform("time", time);
        setUniform("noiseTextureSize", (float) NOISE_TEXTURE_SIZE);
    }

    public static void clearAll() {
        ACTIVE.clear();
        TRACKED.clear();
    }

    private static void ensureSceneCopy(RenderTarget mainTarget) {
        if (sceneCopy == null) {
            sceneCopy = new TextureTarget(mainTarget.width, mainTarget.height, true, Minecraft.ON_OSX);
        } else if (sceneCopy.width != mainTarget.width || sceneCopy.height != mainTarget.height) {
            sceneCopy.resize(mainTarget.width, mainTarget.height, Minecraft.ON_OSX);
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
            noiseTexture.setFilter(true, false);
            noiseTexture.upload();
            configureRepeat(noiseTexture);
        }
        if (colorRampTexture == null) {
            colorRampTexture = createColorRampTexture(1.0F, 1.0F, 1.0F,
                    1.7F, 0.5F, 0.1F,
                    0.5F, 0.6F, 1.0F);
        }
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
        texture.setFilter(true, false);
        texture.upload();
        configureRepeat(texture);
        return texture;
    }

    private static void configureRepeat(DynamicTexture texture) {
        texture.bind();
        RenderSystem.texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_WRAP_S, GL_REPEAT);
        RenderSystem.texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_WRAP_T, GL_REPEAT);
    }

    private static Matrix4f createWorldViewMatrix(Camera camera) {
        PoseStack poseStack = new PoseStack();
        poseStack.mulPose(Axis.XP.rotationDegrees(camera.getXRot()));
        poseStack.mulPose(Axis.YP.rotationDegrees(camera.getYRot() + 180.0F));
        return new Matrix4f(poseStack.last().pose());
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

    private static void setUniform(String name, float value) {
        Uniform uniform = blackHoleShader.getUniform(name);
        if (uniform != null) {
            uniform.set(value);
        }
    }

    private static void setUniform(String name, float x, float y) {
        Uniform uniform = blackHoleShader.getUniform(name);
        if (uniform != null) {
            uniform.set(x, y);
        }
    }

    private static void setUniform(String name, float x, float y, float z) {
        Uniform uniform = blackHoleShader.getUniform(name);
        if (uniform != null) {
            uniform.set(x, y, z);
        }
    }

    private static void setUniform(String name, Matrix4f matrix) {
        Uniform uniform = blackHoleShader.getUniform(name);
        if (uniform != null) {
            uniform.set(matrix);
        }
    }

    private static void setUniform(String name, Vector3f value) {
        setUniform(name, value.x(), value.y(), value.z());
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
        private final Vector3f diskColor;
        private final Vector3f diskInnerColor;
        private final Vector3f diskOuterColor;

        private BlackHoleSpec(float scale, float eventHorizonRadius, int lifetime, float fadeInTicks, float fadeOutStartTick,
                float accretionDiskRadiusScale, float accretionDiskThicknessScale,
                float accretionDiskDensity, float tiltAngle, float intensity, float renderQuality, float ditherStrength,
                float lensBoundarySoftness, float diskNoiseStrength, float diskTextureStrength,
                Vector3f diskColor, Vector3f diskInnerColor, Vector3f diskOuterColor) {
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
            this.diskColor = new Vector3f(diskColor);
            this.diskInnerColor = new Vector3f(diskInnerColor);
            this.diskOuterColor = new Vector3f(diskOuterColor);
        }

        public static BlackHoleSpec of(float eventHorizonRadius, int lifetime) {
            float safeEventHorizonRadius = Math.max(0.01F, eventHorizonRadius);
            return new BlackHoleSpec(safeEventHorizonRadius * LEGACY_EFFECT_RADIUS_MULTIPLIER,
                    safeEventHorizonRadius, lifetime,
                    10.0F, lifetime * 0.75F,
                    1.0F, 1.0F,
                    0.01F, 0.4363F, 1.0F, RenderPrecision.NATIVE.quality, RenderPrecision.NATIVE.ditherStrength,
                    0.6F, 1.0F, 0.35F,
                    new Vector3f(1.0F, 1.0F, 1.0F),
                    new Vector3f(1.7F, 0.5F, 0.1F),
                    new Vector3f(0.5F, 0.6F, 1.0F));
        }

        public BlackHoleSpec withFade(float fadeInTicks, float fadeOutStartTick) {
            return new BlackHoleSpec(scale, eventHorizonRadius, lifetime, fadeInTicks, fadeOutStartTick,
                    accretionDiskRadiusScale, accretionDiskThicknessScale,
                    accretionDiskDensity, tiltAngle, intensity, renderQuality, ditherStrength,
                    lensBoundarySoftness, diskNoiseStrength, diskTextureStrength,
                    diskColor, diskInnerColor, diskOuterColor);
        }

        public BlackHoleSpec withEventHorizonRadius(float eventHorizonRadius) {
            float safeEventHorizonRadius = Math.max(0.01F, eventHorizonRadius);
            return new BlackHoleSpec(safeEventHorizonRadius * LEGACY_EFFECT_RADIUS_MULTIPLIER,
                    safeEventHorizonRadius, lifetime, fadeInTicks, fadeOutStartTick,
                    accretionDiskRadiusScale, accretionDiskThicknessScale,
                    accretionDiskDensity, tiltAngle, intensity, renderQuality, ditherStrength,
                    lensBoundarySoftness, diskNoiseStrength, diskTextureStrength,
                    diskColor, diskInnerColor, diskOuterColor);
        }

        public BlackHoleSpec withAccretionDiskScale(float radiusScale, float thicknessScale) {
            return new BlackHoleSpec(scale, eventHorizonRadius, lifetime, fadeInTicks, fadeOutStartTick,
                    radiusScale, thicknessScale,
                    accretionDiskDensity, tiltAngle, intensity, renderQuality, ditherStrength,
                    lensBoundarySoftness, diskNoiseStrength, diskTextureStrength,
                    diskColor, diskInnerColor, diskOuterColor);
        }

        public BlackHoleSpec withAccretionDiskDensity(float accretionDiskDensity) {
            return new BlackHoleSpec(scale, eventHorizonRadius, lifetime, fadeInTicks, fadeOutStartTick,
                    accretionDiskRadiusScale, accretionDiskThicknessScale,
                    accretionDiskDensity, tiltAngle, intensity, renderQuality, ditherStrength,
                    lensBoundarySoftness, diskNoiseStrength, diskTextureStrength,
                    diskColor, diskInnerColor, diskOuterColor);
        }

        public BlackHoleSpec withTiltAngle(float tiltAngle) {
            return new BlackHoleSpec(scale, eventHorizonRadius, lifetime, fadeInTicks, fadeOutStartTick,
                    accretionDiskRadiusScale, accretionDiskThicknessScale,
                    accretionDiskDensity, tiltAngle, intensity, renderQuality, ditherStrength,
                    lensBoundarySoftness, diskNoiseStrength, diskTextureStrength,
                    diskColor, diskInnerColor, diskOuterColor);
        }

        public BlackHoleSpec withIntensity(float intensity) {
            return new BlackHoleSpec(scale, eventHorizonRadius, lifetime, fadeInTicks, fadeOutStartTick,
                    accretionDiskRadiusScale, accretionDiskThicknessScale,
                    accretionDiskDensity, tiltAngle, intensity, renderQuality, ditherStrength,
                    lensBoundarySoftness, diskNoiseStrength, diskTextureStrength,
                    diskColor, diskInnerColor, diskOuterColor);
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
                    diskColor, diskInnerColor, diskOuterColor);
        }

        public BlackHoleSpec withLensBoundarySoftness(float lensBoundarySoftness) {
            return new BlackHoleSpec(scale, eventHorizonRadius, lifetime, fadeInTicks, fadeOutStartTick,
                    accretionDiskRadiusScale, accretionDiskThicknessScale,
                    accretionDiskDensity, tiltAngle, intensity, renderQuality, ditherStrength,
                    lensBoundarySoftness, diskNoiseStrength, diskTextureStrength,
                    diskColor, diskInnerColor, diskOuterColor);
        }

        public BlackHoleSpec withDiskDetail(float diskNoiseStrength, float diskTextureStrength) {
            return new BlackHoleSpec(scale, eventHorizonRadius, lifetime, fadeInTicks, fadeOutStartTick,
                    accretionDiskRadiusScale, accretionDiskThicknessScale,
                    accretionDiskDensity, tiltAngle, intensity, renderQuality, ditherStrength,
                    lensBoundarySoftness, diskNoiseStrength, diskTextureStrength,
                    diskColor, diskInnerColor, diskOuterColor);
        }

        public BlackHoleSpec withDiskColor(float r, float g, float b) {
            return new BlackHoleSpec(scale, eventHorizonRadius, lifetime, fadeInTicks, fadeOutStartTick,
                    accretionDiskRadiusScale, accretionDiskThicknessScale,
                    accretionDiskDensity, tiltAngle, intensity, renderQuality, ditherStrength,
                    lensBoundarySoftness, diskNoiseStrength, diskTextureStrength,
                    new Vector3f(r, g, b), diskInnerColor, diskOuterColor);
        }

        public BlackHoleSpec withDiskRamp(float innerR, float innerG, float innerB,
                float outerR, float outerG, float outerB) {
            return new BlackHoleSpec(scale, eventHorizonRadius, lifetime, fadeInTicks, fadeOutStartTick,
                    accretionDiskRadiusScale, accretionDiskThicknessScale,
                    accretionDiskDensity, tiltAngle, intensity, renderQuality, ditherStrength,
                    lensBoundarySoftness, diskNoiseStrength, diskTextureStrength, diskColor,
                    new Vector3f(innerR, innerG, innerB), new Vector3f(outerR, outerG, outerB));
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
        private final double x;
        private final double y;
        private final double z;
        private final BlackHoleSpec spec;
        private final int age;
        private int ttl;

        private TrackedBlackHole(double x, double y, double z, BlackHoleSpec spec, int age, int ttl) {
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

    private HbmBlackHoleEffects() {
    }
}
