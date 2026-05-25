package com.hbm.ntm.client.particle;

import com.mojang.blaze3d.platform.GlStateManager;
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
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class HadronParticle extends TextureSheetParticle {
    private static SpriteSet sharedSprites;
    private static final ParticleRenderType RENDER_TYPE = new ParticleRenderType() {
        @Override
        public void begin(BufferBuilder builder, TextureManager textureManager) {
            RenderSystem.depthMask(false);
            RenderSystem.setShader(GameRenderer::getParticleShader);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public void end(Tesselator tesselator) {
            tesselator.end();
            RenderSystem.defaultBlendFunc();
            RenderSystem.depthMask(true);
        }

        @Override
        public String toString() {
            return "HBM_HADRON";
        }
    };

    private final SpriteSet sprites;
    private final float baseScale;

    private HadronParticle(ClientLevel level, double x, double y, double z, boolean small, SpriteSet sprites) {
        super(level, x, y, z);
        this.sprites = sprites;
        this.lifetime = small ? 5 : 10;
        this.baseScale = small ? 0.5F : 1.0F;
        this.hasPhysics = false;
        this.setSpriteFromAge(sprites);
    }

    public static HadronParticle create(ClientLevel level, double x, double y, double z, boolean small) {
        return sharedSprites == null ? null : new HadronParticle(level, x, y, z, small, sharedSprites);
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
    }

    @Override
    public void render(VertexConsumer consumer, Camera camera, float partialTick) {
        float progressAge = this.age + partialTick;
        float alpha = 1.0F - progressAge / (float) this.lifetime;
        if (alpha <= 0.0F) {
            return;
        }
        float scale = progressAge * 0.15F * this.baseScale;
        Vec3 cameraPos = camera.getPosition();
        float x = (float) (Mth.lerp(partialTick, this.xo, this.x) - cameraPos.x());
        float y = (float) (Mth.lerp(partialTick, this.yo, this.y) - cameraPos.y());
        float z = (float) (Mth.lerp(partialTick, this.zo, this.z) - cameraPos.z());
        Quaternionf rotation = camera.rotation();
        Vector3f[] corners = new Vector3f[] {
                new Vector3f(-1.0F, -1.0F, 0.0F),
                new Vector3f(-1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, -1.0F, 0.0F)
        };
        for (Vector3f corner : corners) {
            corner.rotate(rotation).mul(scale).add(x, y, z);
        }
        consumer.vertex(corners[0].x(), corners[0].y(), corners[0].z()).uv(getU1(), getV1()).color(1.0F, 1.0F, 1.0F, alpha).uv2(LightTexture.FULL_BRIGHT).endVertex();
        consumer.vertex(corners[1].x(), corners[1].y(), corners[1].z()).uv(getU1(), getV0()).color(1.0F, 1.0F, 1.0F, alpha).uv2(LightTexture.FULL_BRIGHT).endVertex();
        consumer.vertex(corners[2].x(), corners[2].y(), corners[2].z()).uv(getU0(), getV0()).color(1.0F, 1.0F, 1.0F, alpha).uv2(LightTexture.FULL_BRIGHT).endVertex();
        consumer.vertex(corners[3].x(), corners[3].y(), corners[3].z()).uv(getU0(), getV1()).color(1.0F, 1.0F, 1.0F, alpha).uv2(LightTexture.FULL_BRIGHT).endVertex();
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
        public HadronParticle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed) {
            return new HadronParticle(level, x, y, z, false, sprites);
        }
    }
}
