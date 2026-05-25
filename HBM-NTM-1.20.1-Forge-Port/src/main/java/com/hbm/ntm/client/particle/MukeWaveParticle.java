package com.hbm.ntm.client.particle;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MukeWaveParticle extends TextureSheetParticle {
    private static SpriteSet sharedSprites;
    private static final ParticleRenderType RENDER_TYPE = new ParticleRenderType() {
        @Override
        public void begin(BufferBuilder builder, TextureManager textureManager) {
            RenderSystem.depthMask(false);
            RenderSystem.setShader(GameRenderer::getParticleShader);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(com.mojang.blaze3d.platform.GlStateManager.SourceFactor.SRC_ALPHA,
                    com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE);
            RenderSystem.disableCull();
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public void end(Tesselator tesselator) {
            tesselator.end();
            RenderSystem.enableCull();
            RenderSystem.defaultBlendFunc();
            RenderSystem.depthMask(true);
        }

        @Override
        public String toString() {
            return "HBM_MUKE_WAVE";
        }
    };

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
        float progressAge = this.age + partialTick;
        float alpha = 1.0F - progressAge / (float) this.lifetime;
        if (alpha <= 0.0F) {
            return;
        }
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
        return RENDER_TYPE;
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
