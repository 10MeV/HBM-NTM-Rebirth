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
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class HadronParticle extends TextureSheetParticle implements HbmDeferredParticleRenderer.DeferredParticle {
    private static SpriteSet sharedSprites;
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
        HbmDeferredParticleRenderer.emitParticleSheetQuad(consumer, LightTexture.FULL_BRIGHT,
                corners[0], getU1(), getV1(),
                corners[1], getU1(), getV0(),
                corners[2], getU0(), getV0(),
                corners[3], getU0(), getV1(),
                1.0F, 1.0F, 1.0F, alpha);
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
        public HadronParticle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed) {
            return new HadronParticle(level, x, y, z, false, sprites);
        }
    }
}
