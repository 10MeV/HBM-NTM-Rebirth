package com.hbm.ntm.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.awt.Color;

@OnlyIn(Dist.CLIENT)
public class ExplosionSmallParticle extends TextureSheetParticle {
    private static SpriteSet sharedSprites;

    private final SpriteSet sprites;
    private final float baseScale;
    private final float hue;
    private final float rollSpeed;

    private ExplosionSmallParticle(ClientLevel level, double x, double y, double z, float scale, float speedMult, SpriteSet sprites) {
        super(level, x, y, z);
        this.sprites = sprites;
        this.lifetime = 25 + random.nextInt(10);
        this.baseScale = scale * 0.9F + random.nextFloat() * 0.2F;
        this.xd = random.nextGaussian() * speedMult;
        this.yd = 0.0D;
        this.zd = random.nextGaussian() * speedMult;
        this.gravity = random.nextFloat() * -0.01F;
        this.hasPhysics = false;
        this.hue = 20.0F + random.nextFloat() * 20.0F;
        this.rollSpeed = (random.nextBoolean() ? 1.0F : -1.0F) * 2.5F * Mth.DEG_TO_RAD;
        this.updateVisuals(0.0F);
        this.setSpriteFromAge(sprites);
    }

    public static ExplosionSmallParticle create(ClientLevel level, double x, double y, double z, float scale, float speedMult) {
        return sharedSprites == null ? null : new ExplosionSmallParticle(level, x, y, z, scale, speedMult, sharedSprites);
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
        this.yd -= this.gravity;
        float progress = (float) this.age / (float) this.lifetime;
        this.oRoll = this.roll;
        this.roll += (1.0F - progress) * rollSpeed;
        this.xd *= 0.65D;
        this.zd *= 0.65D;
        this.move(this.xd, this.yd, this.zd);
        this.updateVisuals(progress);
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void render(VertexConsumer consumer, Camera camera, float partialTick) {
        this.updateVisuals((this.age + partialTick) / (float) this.lifetime);
        super.render(consumer, camera, partialTick);
    }

    private void updateVisuals(float progress) {
        float ageScaled = Mth.clamp(progress, 0.0F, 1.0F);
        Color color = Color.getHSBColor(this.hue / 255.0F,
                Math.max(1.0F - ageScaled * 2.0F, 0.0F),
                Mth.clamp(1.25F - ageScaled * 2.0F, this.hue * 0.01F - 0.1F, 1.0F));
        this.rCol = color.getRed() / 255.0F;
        this.gCol = color.getGreen() / 255.0F;
        this.bCol = color.getBlue() / 255.0F;
        this.alpha = (float) Math.pow(1.0F - ageScaled, 0.25D) * 0.5F;
        this.quadSize = (float) (0.25D + 1.0D - Math.pow(1.0D - ageScaled, 4.0D) + ageScaled * this.lifetime * 0.02D) * this.baseScale;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
            sharedSprites = sprites;
        }

        @Override
        public ExplosionSmallParticle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed) {
            return new ExplosionSmallParticle(level, x, y, z, 2.0F, 0.5F, sprites);
        }
    }
}
