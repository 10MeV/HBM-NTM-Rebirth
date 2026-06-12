package com.hbm.ntm.client.render;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.config.HbmClientConfig;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public final class HbmRenderEffects {
    private static final ResourceLocation WARP_WORLD = new ResourceLocation(HbmNtm.MOD_ID, "warp_world");
    private static final int SPHERE_RINGS = 18;
    private static final int SPHERE_SEGMENTS = 48;
    private static final float OUTER_EDGE_OFFSET = 1.0F;
    private static final float MUKE_WAVE_RENDER_Y_OFFSET = -0.25F;
    private static final float MUKE_WAVE_FADE_START_FRACTION = 0.97F;
    private static final float MUKE_WAVE_VISIBLE_END_FRACTION = 0.999F;
    private static final float TOREX_SHOCK_RADIUS_PER_TICK = 2.25F;
    private static final int TOREX_SHOCK_SPAWN_TICKS = 150;
    private static final int TOREX_SHOCK_POST_FLASH_VISIBLE_TICKS = 120;
    private static final int NUCLEAR_FLASH_COVER_TICKS = 100;
    private static final int MIN_POST_FLASH_WARP_TICKS = 80;

    private static final List<Shockwave> ACTIVE = new ArrayList<>();
    private static ShaderInstance warpWorldShader;
    private static TextureTarget sceneCopy;
    private static long lastDebugLogMillis;

    public static void registerShaders(RegisterShadersEvent event) throws IOException {
        event.registerShader(new ShaderInstance(event.getResourceProvider(), WARP_WORLD,
                DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL), shader -> warpWorldShader = shader);
    }

    public static void spawnNuclearWarpShockwave(double x, double y, double z, float waveScale, int lifetime) {
        if (!enabled()) {
            return;
        }
        spawnWarpWorldShockwave(x, y, z, WarpWorldShockwaveSpec.nuclear(waveScale, lifetime)
                .withMeshSegments(configuredSegments()));
    }

    public static void spawnTorexWarpShockwave(double x, double y, double z) {
        spawnTorexWarpShockwave(x, y, z, 0);
    }

    public static void spawnTorexWarpShockwave(double x, double y, double z, int initialAge) {
        if (!enabled()) {
            return;
        }
        spawnWarpWorldShockwave(x, y, z, WarpWorldShockwaveSpec.torex()
                .withMeshSegments(configuredSegments()), initialAge);
    }

    public static void spawnWarpWorldShockwave(double x, double y, double z, WarpWorldShockwaveSpec spec) {
        spawnWarpWorldShockwave(x, y, z, spec, 0);
    }

    public static void spawnWarpWorldShockwave(double x, double y, double z, WarpWorldShockwaveSpec spec, int initialAge) {
        if (spec == null || spec.intensity <= 0.0F || spec.radiusScale <= 0.0F) {
            return;
        }
        if (initialAge >= spec.lifetime) {
            return;
        }
        ACTIVE.add(new Shockwave(x, y, z, spec, Math.max(0, initialAge)));
        debugLog("spawn warp_world shockwave active=" + ACTIVE.size()
                + " pos=" + x + "," + y + "," + z
                + " mode=" + spec.radiusMode
                + " radiusScale=" + spec.radiusScale
                + " lifetime=" + spec.lifetime
                + " initialAge=" + initialAge);
    }

    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            clearAll();
            return;
        }

        Iterator<Shockwave> iterator = ACTIVE.iterator();
        while (iterator.hasNext()) {
            Shockwave shockwave = iterator.next();
            if (shockwave.age++ >= shockwave.lifetime) {
                iterator.remove();
            }
        }
    }

    public static void render(RenderLevelStageEvent event) {
        if (ACTIVE.isEmpty() || warpWorldShader == null || event.getStage() != RenderLevelStageEvent.Stage.AFTER_WEATHER) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }

        RenderTarget mainTarget = minecraft.getMainRenderTarget();
        ensureSceneCopy(mainTarget);
        copyMainTarget(mainTarget);

        Camera camera = event.getCamera();
        Vec3 cameraPos = camera.getPosition();
        float partialTick = event.getPartialTick();
        float time = minecraft.level.getGameTime() + partialTick;
        debugLog("render stage=" + event.getStage() + " active=" + ACTIVE.size()
                + " shader=" + (warpWorldShader != null)
                + " target=" + mainTarget.width + "x" + mainTarget.height);

        RenderSystem.backupProjectionMatrix();
        RenderSystem.setProjectionMatrix(event.getProjectionMatrix(), VertexSorting.DISTANCE_TO_ORIGIN);
        PoseStack modelView = RenderSystem.getModelViewStack();
        modelView.pushPose();
        modelView.setIdentity();
        modelView.mulPose(Axis.XP.rotationDegrees(camera.getXRot()));
        modelView.mulPose(Axis.YP.rotationDegrees(camera.getYRot() + 180.0F));
        RenderSystem.applyModelViewMatrix();

        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableCull();

        RenderSystem.setShader(() -> warpWorldShader);
        warpWorldShader.setSampler("ScreenTexture", sceneCopy);
        setUniform("useType", 1);
        setUniform("time", time);

        boolean debugWireframe = debugWireframe();
        for (Shockwave shockwave : ACTIVE) {
            float progressAge = shockwave.age + partialTick;
            float alpha = shockwave.alpha(progressAge);
            if (alpha <= 0.0F) {
                continue;
            }
            float radius = shockwave.radius(progressAge);
            setUniform("intensity", Mth.clamp(alpha * shockwave.spec.intensity, 0.0F, 8.0F));

            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder builder = tesselator.getBuilder();
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);
            int segments = shockwave.spec.meshSegments;
            int rings = Math.max(6, segments / 3);
            float centerX = (float) (shockwave.x - cameraPos.x);
            float centerY = (float) (shockwave.y + shockwave.spec.yOffset - cameraPos.y);
            float centerZ = (float) (shockwave.z - cameraPos.z);
            buildSphere(builder,
                    centerX,
                    centerY,
                    centerZ,
                    radius,
                    Mth.clamp(alpha, 0.0F, 1.0F),
                    rings,
                    segments);
            BufferUploader.drawWithShader(builder.end());

            if (debugWireframe) {
                renderDebugWireframe(tesselator, builder, centerX, centerY, centerZ, radius, rings, segments, alpha);
                RenderSystem.setShader(() -> warpWorldShader);
            }
        }

        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        modelView.popPose();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.restoreProjectionMatrix();
        mainTarget.bindWrite(false);
    }

    public static void clearAll() {
        ACTIVE.clear();
    }

    private static void ensureSceneCopy(RenderTarget mainTarget) {
        if (sceneCopy == null) {
            sceneCopy = new TextureTarget(mainTarget.width, mainTarget.height, false, Minecraft.ON_OSX);
        } else if (sceneCopy.width != mainTarget.width || sceneCopy.height != mainTarget.height) {
            sceneCopy.resize(mainTarget.width, mainTarget.height, Minecraft.ON_OSX);
        }
    }

    private static void copyMainTarget(RenderTarget mainTarget) {
        GlStateManager._glBindFramebuffer(GlConst.GL_READ_FRAMEBUFFER, mainTarget.frameBufferId);
        GlStateManager._glBindFramebuffer(GlConst.GL_DRAW_FRAMEBUFFER, sceneCopy.frameBufferId);
        GlStateManager._glBlitFrameBuffer(0, 0, mainTarget.width, mainTarget.height,
                0, 0, sceneCopy.width, sceneCopy.height,
                GlConst.GL_COLOR_BUFFER_BIT, GlConst.GL_NEAREST);
        GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, mainTarget.frameBufferId);
        RenderSystem.viewport(0, 0, mainTarget.viewWidth, mainTarget.viewHeight);
    }

    private static void buildSphere(BufferBuilder builder, float centerX, float centerY, float centerZ,
            float radius, float alpha, int rings, int segments) {
        for (int ring = 0; ring < rings; ring++) {
            float v0 = ring / (float) rings;
            float v1 = (ring + 1) / (float) rings;
            float theta0 = (float) (Math.PI * v0);
            float theta1 = (float) (Math.PI * v1);

            for (int segment = 0; segment < segments; segment++) {
                float u0 = segment / (float) segments;
                float u1 = (segment + 1) / (float) segments;
                float phi0 = (float) (Math.PI * 2.0D * u0);
                float phi1 = (float) (Math.PI * 2.0D * u1);

                putSphereVertex(builder, centerX, centerY, centerZ, radius, theta0, phi0, u0, v0, alpha);
                putSphereVertex(builder, centerX, centerY, centerZ, radius, theta1, phi0, u0, v1, alpha);
                putSphereVertex(builder, centerX, centerY, centerZ, radius, theta1, phi1, u1, v1, alpha);
                putSphereVertex(builder, centerX, centerY, centerZ, radius, theta0, phi1, u1, v0, alpha);
            }
        }
    }

    private static void putSphereVertex(BufferBuilder builder, float centerX, float centerY, float centerZ,
            float radius, float theta, float phi, float u, float v, float alpha) {
        float sinTheta = Mth.sin(theta);
        float nx = sinTheta * Mth.cos(phi);
        float ny = Mth.cos(theta);
        float nz = sinTheta * Mth.sin(phi);
        builder.vertex(centerX + nx * radius, centerY + ny * radius, centerZ + nz * radius)
                .uv(u, v)
                .color(255, 255, 255, Mth.clamp((int) (alpha * 255.0F), 0, 255))
                .normal(nx, ny, nz)
                .endVertex();
    }

    private static void renderDebugWireframe(Tesselator tesselator, BufferBuilder builder, float centerX, float centerY,
            float centerZ, float radius, int rings, int segments, float alpha) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        builder.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR);
        int colorAlpha = Mth.clamp((int) (alpha * 180.0F), 32, 180);

        for (int ring = 1; ring < rings; ring++) {
            float theta = (float) (Math.PI * ring / (float) rings);
            for (int segment = 0; segment < segments; segment++) {
                float phi0 = (float) (Math.PI * 2.0D * segment / (float) segments);
                float phi1 = (float) (Math.PI * 2.0D * (segment + 1) / (float) segments);
                putDebugLineVertex(builder, centerX, centerY, centerZ, radius, theta, phi0, colorAlpha);
                putDebugLineVertex(builder, centerX, centerY, centerZ, radius, theta, phi1, colorAlpha);
            }
        }

        for (int segment = 0; segment < segments; segment += Math.max(1, segments / 12)) {
            float phi = (float) (Math.PI * 2.0D * segment / (float) segments);
            for (int ring = 0; ring < rings; ring++) {
                float theta0 = (float) (Math.PI * ring / (float) rings);
                float theta1 = (float) (Math.PI * (ring + 1) / (float) rings);
                putDebugLineVertex(builder, centerX, centerY, centerZ, radius, theta0, phi, colorAlpha);
                putDebugLineVertex(builder, centerX, centerY, centerZ, radius, theta1, phi, colorAlpha);
            }
        }

        tesselator.end();
    }

    private static void putDebugLineVertex(BufferBuilder builder, float centerX, float centerY, float centerZ,
            float radius, float theta, float phi, int alpha) {
        float sinTheta = Mth.sin(theta);
        float nx = sinTheta * Mth.cos(phi);
        float ny = Mth.cos(theta);
        float nz = sinTheta * Mth.sin(phi);
        builder.vertex(centerX + nx * radius, centerY + ny * radius, centerZ + nz * radius)
                .color(90, 220, 255, alpha)
                .endVertex();
    }

    private static void setUniform(String name, int value) {
        Uniform uniform = warpWorldShader.getUniform(name);
        if (uniform != null) {
            uniform.set(value);
        }
    }

    private static void setUniform(String name, float value) {
        Uniform uniform = warpWorldShader.getUniform(name);
        if (uniform != null) {
            uniform.set(value);
        }
    }

    private static void setUniform(String name, float x, float y) {
        Uniform uniform = warpWorldShader.getUniform(name);
        if (uniform != null) {
            uniform.set(x, y);
        }
    }

    private static void setUniform(String name, float x, float y, float z, float w) {
        Uniform uniform = warpWorldShader.getUniform(name);
        if (uniform != null) {
            uniform.set(x, y, z, w);
        }
    }

    private static boolean enabled() {
        return HbmClientConfig.nukeWarpShockwaveEnabled();
    }

    private static float configuredIntensity() {
        return HbmClientConfig.nukeWarpShockwaveIntensity();
    }

    private static int configuredSegments() {
        return HbmClientConfig.nukeWarpShockwaveMeshSegments();
    }

    private static boolean debugWireframe() {
        return HbmClientConfig.debugNukeWarpShockwaveWireframe();
    }

    private static void debugLog(String message) {
        if (!debugWireframe()) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now - lastDebugLogMillis >= 1_000L) {
            HbmNtm.LOGGER.info("HBM render effects: {}", message);
            lastDebugLogMillis = now;
        }
    }

    public static final class WarpWorldShockwaveSpec {
        private final RadiusMode radiusMode;
        private final float radiusScale;
        private final int lifetime;
        private final float fadeStartTick;
        private final float outerEdgeOffset;
        private final float yOffset;
        private final float intensity;
        private final int meshSegments;

        private WarpWorldShockwaveSpec(RadiusMode radiusMode, float radiusScale, int lifetime, float fadeStartTick,
                float outerEdgeOffset, float yOffset, float intensity, int meshSegments) {
            this.radiusMode = radiusMode;
            this.radiusScale = radiusScale;
            this.lifetime = Math.max(1, lifetime);
            this.fadeStartTick = Mth.clamp(fadeStartTick, 0.0F, this.lifetime - 1.0F);
            this.outerEdgeOffset = outerEdgeOffset;
            this.yOffset = yOffset;
            this.intensity = Math.max(0.0F, intensity);
            this.meshSegments = Mth.clamp(meshSegments, 12, 96);
        }

        public static WarpWorldShockwaveSpec of(float waveScale, int lifetime) {
            return of(waveScale, lifetime, Math.max(1, lifetime) * 0.72F);
        }

        public static WarpWorldShockwaveSpec of(float waveScale, int lifetime, float fadeStartTick) {
            return new WarpWorldShockwaveSpec(RadiusMode.MUKE_EXPONENTIAL, waveScale, lifetime, fadeStartTick,
                    OUTER_EDGE_OFFSET, 0.0F, 1.0F, SPHERE_SEGMENTS);
        }

        public static WarpWorldShockwaveSpec nuclear(float waveScale, int lifetime) {
            int visibleLifetime = Math.max(Math.max(1, lifetime), nuclearVisibleLifetime(waveScale));
            float fadeStart = Math.max(NUCLEAR_FLASH_COVER_TICKS,
                    ticksToReachRadiusFraction(MUKE_WAVE_FADE_START_FRACTION, waveScale));
            return of(waveScale, visibleLifetime, fadeStart)
                    .withYOffset(MUKE_WAVE_RENDER_Y_OFFSET)
                    .withIntensity(configuredIntensity());
        }

        public static WarpWorldShockwaveSpec linear(float radiusPerTick, int lifetime, float fadeStartTick) {
            return new WarpWorldShockwaveSpec(RadiusMode.LINEAR, radiusPerTick, lifetime, fadeStartTick,
                    OUTER_EDGE_OFFSET, 0.0F, 1.0F, SPHERE_SEGMENTS);
        }

        public static WarpWorldShockwaveSpec torex() {
            int lifetime = NUCLEAR_FLASH_COVER_TICKS + TOREX_SHOCK_POST_FLASH_VISIBLE_TICKS;
            return linear(TOREX_SHOCK_RADIUS_PER_TICK, lifetime, NUCLEAR_FLASH_COVER_TICKS)
                    .withIntensity(configuredIntensity());
        }

        public WarpWorldShockwaveSpec withOuterEdgeOffset(float outerEdgeOffset) {
            return new WarpWorldShockwaveSpec(radiusMode, radiusScale, lifetime, fadeStartTick, outerEdgeOffset, yOffset, intensity, meshSegments);
        }

        public WarpWorldShockwaveSpec withYOffset(float yOffset) {
            return new WarpWorldShockwaveSpec(radiusMode, radiusScale, lifetime, fadeStartTick, outerEdgeOffset, yOffset, intensity, meshSegments);
        }

        public WarpWorldShockwaveSpec withIntensity(float intensity) {
            return new WarpWorldShockwaveSpec(radiusMode, radiusScale, lifetime, fadeStartTick, outerEdgeOffset, yOffset, intensity, meshSegments);
        }

        public WarpWorldShockwaveSpec withMeshSegments(int meshSegments) {
            return new WarpWorldShockwaveSpec(radiusMode, radiusScale, lifetime, fadeStartTick, outerEdgeOffset, yOffset, intensity, meshSegments);
        }

        public WarpWorldShockwaveSpec withFadeStartTick(float fadeStartTick) {
            return new WarpWorldShockwaveSpec(radiusMode, radiusScale, lifetime, fadeStartTick, outerEdgeOffset, yOffset, intensity, meshSegments);
        }

        public int lifetime() {
            return lifetime;
        }

        public float fadeStartTick() {
            return fadeStartTick;
        }

        public float outerEdgeOffset() {
            return outerEdgeOffset;
        }

        public float yOffset() {
            return yOffset;
        }

        public float intensity() {
            return intensity;
        }

        public int meshSegments() {
            return meshSegments;
        }
    }

    private enum RadiusMode {
        MUKE_EXPONENTIAL,
        LINEAR
    }

    private static final class Shockwave {
        private final double x;
        private final double y;
        private final double z;
        private final WarpWorldShockwaveSpec spec;
        private final int lifetime;
        private int age;

        private Shockwave(double x, double y, double z, WarpWorldShockwaveSpec spec, int initialAge) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.spec = spec;
            this.lifetime = spec.lifetime;
            this.age = initialAge;
        }

        private float radius(float progressAge) {
            if (this.spec.radiusMode == RadiusMode.LINEAR) {
                return progressAge * this.spec.radiusScale + this.spec.outerEdgeOffset;
            }
            return (float) (1.0D - Math.exp(progressAge * -0.125D)) * this.spec.radiusScale + this.spec.outerEdgeOffset;
        }

        private float alpha(float progressAge) {
            float fadeStart = this.spec.fadeStartTick;
            if (progressAge <= fadeStart) {
                return 1.0F;
            }
            float fadeProgress = Mth.clamp((progressAge - fadeStart) / Math.max(1.0F, this.spec.lifetime - fadeStart), 0.0F, 1.0F);
            float easeOut = 1.0F - fadeProgress;
            return easeOut * easeOut * easeOut;
        }
    }

    private static int nuclearVisibleLifetime(float waveScale) {
        int ticksToReachVisibleEnd = Mth.ceil(ticksToReachRadiusFraction(MUKE_WAVE_VISIBLE_END_FRACTION, waveScale));
        return NUCLEAR_FLASH_COVER_TICKS + Math.max(MIN_POST_FLASH_WARP_TICKS, ticksToReachVisibleEnd);
    }

    private static float ticksToReachRadiusFraction(float fraction, float waveScale) {
        double clamped = Mth.clamp(fraction, 0.0F, 0.9999F);
        return (float) (-Math.log(Math.max(1.0E-4D, 1.0D - clamped)) / 0.125D * Math.max(0.01F, waveScale) / 45.0F);
    }

    private HbmRenderEffects() {
    }
}
