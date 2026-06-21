package com.hbm.ntm.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LegacyAuraParticle extends TextureSheetParticle {
    private static final float LEGACY_QUAD_SIZE = 0.025F;
    private static final double LEGACY_LIFETIME_BASE = 8.0D;
    private final SpriteSet sprites;

    private LegacyAuraParticle(ClientLevel level, double x, double y, double z,
            double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites,
            float red, float green, float blue) {
        super(level, x, y, z, 0.0D, 0.0D, 0.0D);
        this.sprites = sprites;
        this.setSize(0.02F, 0.02F);
        this.quadSize = LEGACY_QUAD_SIZE * (this.random.nextFloat() * 0.6F + 0.5F);
        this.xd *= 0.019999999552965164D;
        this.yd *= 0.019999999552965164D;
        this.zd *= 0.019999999552965164D;
        this.rCol = red;
        this.gCol = green;
        this.bCol = blue;
        this.lifetime = Math.max(1, (int) (LEGACY_LIFETIME_BASE / (this.random.nextDouble() * 0.8D + 0.2D)));
        this.friction = 0.99F;
        this.gravity = 0.0F;
        this.hasPhysics = false;
        this.alpha = 1.0F;
        this.setParticleSpeed(xSpeed, ySpeed, zSpeed);
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
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static LegacyAuraParticle create(ClientLevel level, double x, double y, double z,
            double xSpeed, double ySpeed, double zSpeed, float red, float green, float blue) {
        SpriteSet sprites = TownAuraParticle.sharedSprites();
        if (sprites == null) {
            return null;
        }
        return new LegacyAuraParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites, red, green, blue);
    }
}
