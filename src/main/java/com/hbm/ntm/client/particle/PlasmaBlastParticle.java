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
import org.joml.Quaternionf;

@OnlyIn(Dist.CLIENT)
public class PlasmaBlastParticle extends TextureSheetParticle implements HbmDeferredParticleRenderer.DeferredParticle {
    private static SpriteSet sharedSprites;
    private final SpriteSet sprites;
    private final float pitch;
    private final float yaw;
    private final float blastScale;
    private float cachedU0;
    private float cachedU1;
    private float cachedV0;
    private float cachedV1;

    private PlasmaBlastParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites,
            float red, float green, float blue, float pitch, float yaw, float scale) {
        super(level, x, y, z);
        this.sprites = sprites;
        this.pitch = pitch;
        this.yaw = yaw;
        this.blastScale = scale;
        this.rCol = red;
        this.gCol = green;
        this.bCol = blue;
        this.lifetime = 20;
        this.hasPhysics = false;
        this.setSpriteFromAge(sprites);
        this.cacheSpriteUv();
    }

    public static PlasmaBlastParticle create(ClientLevel level, double x, double y, double z,
            float red, float green, float blue, float pitch, float yaw, float scale) {
        return sharedSprites == null ? null : new PlasmaBlastParticle(level, x, y, z, sharedSprites, red, green, blue, pitch, yaw, scale);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        }
        this.setSpriteFromAge(sprites);
        this.cacheSpriteUv();
    }

    @Override
    public void render(VertexConsumer consumer, Camera camera, float partialTick) {
        HbmDeferredParticleRenderer.enqueue(this, camera, this.x, this.y, this.z);
    }

    @Override
    public void renderDeferred(MultiBufferSource.BufferSource buffer, Camera camera, float partialTick) {
        float renderAge = this.age + partialTick;
        float alpha = 1.0F - renderAge / (float) this.lifetime;
        if (alpha <= 0.0F) {
            return;
        }
        VertexConsumer consumer = HbmDeferredParticleRenderer.particleSheetAdditiveNoDepthWriteConsumer(buffer);
        float scale = (1.0F - (float) Math.exp(renderAge * -0.125D)) * this.blastScale;
        Vec3 cameraPos = camera.getPosition();
        float x = (float) (Mth.lerp(partialTick, this.xo, this.x) - cameraPos.x());
        float y = (float) (Mth.lerp(partialTick, this.yo, this.y) - cameraPos.y());
        float z = (float) (Mth.lerp(partialTick, this.zo, this.z) - cameraPos.z());
        Quaternionf rotation = HbmDeferredParticleRenderer.scratchRotation()
                .rotateY(this.yaw * Mth.DEG_TO_RAD)
                .rotateX(this.pitch * Mth.DEG_TO_RAD);
        HbmDeferredParticleRenderer.emitLocalParticleSheetQuad(consumer, LightTexture.FULL_BRIGHT, rotation, x, y, z,
                -scale, 0.0F, -scale, this.cachedU1, this.cachedV1,
                -scale, 0.0F, scale, this.cachedU1, this.cachedV0,
                scale, 0.0F, scale, this.cachedU0, this.cachedV0,
                scale, 0.0F, -scale, this.cachedU0, this.cachedV1,
                this.rCol, this.gCol, this.bCol, alpha);
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

    @Override
    public boolean shouldCull() {
        return false;
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
            sharedSprites = sprites;
        }

        @Override
        public PlasmaBlastParticle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed) {
            return new PlasmaBlastParticle(level, x, y, z, sprites, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F, 1.0F);
        }
    }
}
