package com.hbm.ntm.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TownAuraParticle extends TextureSheetParticle {
    private static final float LEGACY_QUAD_SIZE = 0.025F;
    private static final double LEGACY_LIFETIME_BASE = 8.0D;
    private static SpriteSet sharedSprites;
    private final SpriteSet sprites;

    private TownAuraParticle(ClientLevel level, double x, double y, double z,
            double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.sprites = sprites;
        this.setSize(0.02F, 0.02F);
        this.quadSize = LEGACY_QUAD_SIZE * (this.random.nextFloat() * 0.6F + 0.5F);
        this.xd *= 0.019999999552965164D;
        this.yd *= 0.019999999552965164D;
        this.zd *= 0.019999999552965164D;
        float color = 0.85F + random.nextFloat() * 0.15F;
        this.rCol = color;
        this.gCol = color;
        this.bCol = color;
        this.lifetime = Math.max(1, (int) (LEGACY_LIFETIME_BASE / (this.random.nextDouble() * 0.8D + 0.2D)));
        this.friction = 0.99F;
        this.gravity = 0.0F;
        this.hasPhysics = false;
        this.alpha = 1.0F;
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.removed) {
            this.setSpriteFromAge(sprites);
        }
    }

    @Override
    public void render(VertexConsumer consumer, Camera camera, float partialTick) {
        HbmDeferredParticleRenderer.emitTextureSheetParticleQuad(consumer, camera, partialTick,
                this.xo, this.yo, this.zo, this.x, this.y, this.z,
                this.oRoll, this.roll, this.getQuadSize(partialTick),
                this.getU0(), this.getU1(), this.getV0(), this.getV1(),
                this.rCol, this.gCol, this.bCol, this.alpha, this.getLightColor(partialTick));
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static SpriteSet sharedSprites() {
        return sharedSprites;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
            sharedSprites = sprites;
        }

        @Override
        public TownAuraParticle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed) {
            return new TownAuraParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites);
        }
    }
}
