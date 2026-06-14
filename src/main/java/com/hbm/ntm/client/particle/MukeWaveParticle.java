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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MukeWaveParticle extends TextureSheetParticle implements HbmDeferredParticleRenderer.DeferredParticle {
    private static SpriteSet sharedSprites;

    private final SpriteSet sprites;
    private final float waveScale;

    private MukeWaveParticle(ClientLevel level, double x, double y, double z, float waveScale, int lifetime, SpriteSet sprites) {
        super(level, x, y, z);
        this.sprites = sprites;
        this.waveScale = waveScale;
        this.lifetime = Math.max(1, lifetime);
        this.hasPhysics = false;
        this.alpha = 1.0F;
        this.setSpriteFromAge(sprites);
    }

    public static MukeWaveParticle create(ClientLevel level, double x, double y, double z, float waveScale, int lifetime) {
        return sharedSprites == null ? null : new MukeWaveParticle(level, x, y, z, waveScale, lifetime, sharedSprites);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        }
    }

    @Override
    public void render(VertexConsumer consumer, Camera camera, float partialTick) {
        HbmDeferredParticleRenderer.enqueue(this, camera, this.x, this.y, this.z);
    }

    @Override
    public void renderDeferred(MultiBufferSource.BufferSource buffer, Camera camera, float partialTick) {
        float progressAge = this.age + partialTick;
        float alpha = 1.0F - progressAge / (float) this.lifetime;
        if (alpha <= 0.0F) {
            return;
        }
        VertexConsumer consumer = buffer.getBuffer(HbmDeferredParticleRenderer.particleSheetAdditiveNoDepthWrite());
        float scale = (float) (1.0D - Math.exp(progressAge * -0.125D)) * this.waveScale;
        double x = Mth.lerp(partialTick, this.xo, this.x) - camera.getPosition().x();
        double y = Mth.lerp(partialTick, this.yo, this.y) - camera.getPosition().y() - 0.25D;
        double z = Mth.lerp(partialTick, this.zo, this.z) - camera.getPosition().z();
        consumer.vertex(x - scale, y, z - scale).uv(getU1(), getV1()).color(1.0F, 1.0F, 1.0F, alpha).uv2(LightTexture.FULL_BRIGHT).endVertex();
        consumer.vertex(x - scale, y, z + scale).uv(getU1(), getV0()).color(1.0F, 1.0F, 1.0F, alpha).uv2(LightTexture.FULL_BRIGHT).endVertex();
        consumer.vertex(x + scale, y, z + scale).uv(getU0(), getV0()).color(1.0F, 1.0F, 1.0F, alpha).uv2(LightTexture.FULL_BRIGHT).endVertex();
        consumer.vertex(x + scale, y, z - scale).uv(getU0(), getV1()).color(1.0F, 1.0F, 1.0F, alpha).uv2(LightTexture.FULL_BRIGHT).endVertex();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return HbmDeferredParticleRenderer.DEFERRED_RENDER_TYPE;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
            sharedSprites = sprites;
        }

        @Override
        public MukeWaveParticle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed) {
            return new MukeWaveParticle(level, x, y, z, 45.0F, 25, sprites);
        }
    }
}
