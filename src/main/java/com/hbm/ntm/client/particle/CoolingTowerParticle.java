package com.hbm.ntm.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CoolingTowerParticle extends TextureSheetParticle implements HbmDeferredParticleRenderer.DeferredParticle {
    private static SpriteSet sharedSprites;
    private final SpriteSet sprites;
    private final float baseScale;
    private final float maxScale;
    private final float lift;
    private final float strafe;
    private final boolean windDir;
    private final float alphaMod;

    private CoolingTowerParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites, float lift, float baseScale,
            float maxScale, int lifetime, boolean windDir, float strafe, float alphaMod, int color) {
        super(level, x, y, z);
        this.sprites = sprites;
        this.lift = lift;
        this.baseScale = baseScale;
        this.maxScale = maxScale;
        this.lifetime = Math.max(1, lifetime);
        this.windDir = windDir;
        this.strafe = strafe;
        this.alphaMod = alphaMod;
        if (color >= 0) {
            this.rCol = ((color >> 16) & 255) / 255.0F;
            this.gCol = ((color >> 8) & 255) / 255.0F;
            this.bCol = (color & 255) / 255.0F;
        } else {
            this.rCol = this.gCol = this.bCol = 0.9F + level.random.nextFloat() * 0.05F;
        }
        this.alpha = alphaMod;
        this.quadSize = baseScale;
        this.hasPhysics = false;
        this.setSpriteFromAge(sprites);
    }

    public static CoolingTowerParticle create(ClientLevel level, double x, double y, double z, float lift, float baseScale,
            float maxScale, int lifetime, boolean windDir, float strafe, float alphaMod, int color) {
        return sharedSprites == null ? null : new CoolingTowerParticle(level, x, y, z, sharedSprites, lift, baseScale,
                maxScale, lifetime, windDir, strafe, alphaMod, color);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        float ageScale = this.age / (float) this.lifetime;
        this.alpha = this.alphaMod - ageScale * this.alphaMod;
        this.quadSize = this.baseScale + (float) Math.pow(this.maxScale * ageScale - this.baseScale, 2.0D);
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }
        if (this.lift > 0.0F && this.yd < this.lift) {
            this.yd += 0.01F;
        }
        if (this.lift < 0.0F && this.yd > this.lift) {
            this.yd -= 0.01F;
        }
        this.xd += this.random.nextGaussian() * this.strafe * ageScale;
        this.zd += this.random.nextGaussian() * this.strafe * ageScale;
        if (this.windDir) {
            this.xd += 0.02D * ageScale;
            this.zd -= 0.01D * ageScale;
        }
        this.move(this.xd, this.yd, this.zd);
        this.xd *= 0.925D;
        this.yd *= 0.925D;
        this.zd *= 0.925D;
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void render(VertexConsumer consumer, Camera camera, float partialTick) {
        HbmDeferredParticleRenderer.enqueue(this, camera, this.x, this.y, this.z);
    }

    @Override
    public void renderDeferred(MultiBufferSource.BufferSource buffer, Camera camera, float partialTick) {
        super.render(buffer.getBuffer(HbmDeferredParticleRenderer.particleSheetDepthWrite()), camera, partialTick);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return HbmDeferredParticleRenderer.DEFERRED_RENDER_TYPE;
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        public Provider(SpriteSet sprites) {
            sharedSprites = sprites;
        }

        @Override
        public CoolingTowerParticle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed) {
            return create(level, x, y, z, 0.3F, 1.0F, 1.0F, 80, true, 0.075F, 0.25F, -1);
        }
    }
}
