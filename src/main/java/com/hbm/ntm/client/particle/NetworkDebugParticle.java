package com.hbm.ntm.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class NetworkDebugParticle extends TextureSheetParticle implements HbmDeferredParticleRenderer.DeferredParticle {
    private static SpriteSet powerSprites;
    private static SpriteSet fluidSprites;
    private final SpriteSet sprites;
    private final double targetX;
    private final double targetY;
    private final double targetZ;
    private float cachedU0;
    private float cachedU1;
    private float cachedV0;
    private float cachedV1;

    private NetworkDebugParticle(ClientLevel level, double x, double y, double z, double targetX, double targetY, double targetZ,
            SpriteSet sprites, int color) {
        super(level, x, y, z);
        this.sprites = sprites;
        this.targetX = targetX;
        this.targetY = targetY;
        this.targetZ = targetZ;
        this.xd = targetX;
        this.yd = targetY;
        this.zd = targetZ;
        this.lifetime = 10;
        this.quadSize = 0.05F;
        this.hasPhysics = false;
        this.setColorFromInt(color);
        this.setSpriteFromAge(sprites);
        this.cacheSpriteUv();
    }

    public static NetworkDebugParticle power(ClientLevel level, double x, double y, double z, double targetX, double targetY, double targetZ) {
        return powerSprites == null ? null : new NetworkDebugParticle(level, x, y, z, targetX, targetY, targetZ, powerSprites, 0xFFFFFF);
    }

    public static NetworkDebugParticle fluid(ClientLevel level, double x, double y, double z, double targetX, double targetY, double targetZ, int color) {
        return fluidSprites == null ? null : new NetworkDebugParticle(level, x, y, z, targetX, targetY, targetZ, fluidSprites, color);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }
        this.x += this.targetX;
        this.y += this.targetY;
        this.z += this.targetZ;
        this.setSpriteFromAge(sprites);
        this.cacheSpriteUv();
    }

    @Override
    public void render(VertexConsumer consumer, Camera camera, float partialTick) {
        HbmDeferredParticleRenderer.enqueue(this, camera, this.x, this.y, this.z);
    }

    @Override
    public void renderDeferred(MultiBufferSource.BufferSource buffer, Camera camera, float partialTick) {
        VertexConsumer consumer = HbmDeferredParticleRenderer.particleSheetDepthWriteConsumer(buffer);
        Vec3 cameraPos = camera.getPosition();
        float x = (float) (Mth.lerp(partialTick, this.xo, this.x) - cameraPos.x());
        float y = (float) (Mth.lerp(partialTick, this.yo, this.y) - cameraPos.y());
        float z = (float) (Mth.lerp(partialTick, this.zo, this.z) - cameraPos.z());
        HbmDeferredParticleRenderer.emitCameraUnitParticleSheetQuad(consumer, camera, LightTexture.FULL_BRIGHT,
                x, y, z, this.quadSize,
                this.cachedU0, this.cachedU1, this.cachedV0, this.cachedV1,
                this.rCol, this.gCol, this.bCol, this.alpha);
    }

    private void cacheSpriteUv() {
        this.cachedU0 = this.getU0();
        this.cachedU1 = this.getU1();
        this.cachedV0 = this.getV0();
        this.cachedV1 = this.getV1();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return HbmDeferredParticleRenderer.DEFERRED_RENDER_TYPE;
    }

    private void setColorFromInt(int color) {
        this.rCol = ((color >> 16) & 255) / 255.0F;
        this.gCol = ((color >> 8) & 255) / 255.0F;
        this.bCol = (color & 255) / 255.0F;
    }

    public static final class PowerProvider implements ParticleProvider<SimpleParticleType> {
        public PowerProvider(SpriteSet sprites) {
            powerSprites = sprites;
        }

        @Override
        public NetworkDebugParticle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed) {
            return power(level, x, y, z, xSpeed, ySpeed, zSpeed);
        }
    }

    public static final class FluidProvider implements ParticleProvider<SimpleParticleType> {
        public FluidProvider(SpriteSet sprites) {
            fluidSprites = sprites;
        }

        @Override
        public NetworkDebugParticle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed) {
            return fluid(level, x, y, z, xSpeed, ySpeed, zSpeed, 0xFFFFFF);
        }
    }
}
