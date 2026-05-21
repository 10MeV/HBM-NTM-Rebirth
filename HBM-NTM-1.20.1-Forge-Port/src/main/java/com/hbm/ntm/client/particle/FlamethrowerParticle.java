package com.hbm.ntm.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.awt.Color;

@OnlyIn(Dist.CLIENT)
public class FlamethrowerParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    private FlamethrowerParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.sprites = sprites;
        this.lifetime = 20 + level.random.nextInt(10);
        this.quadSize = 0.5F;
        this.friction = 0.91F;
        this.hasPhysics = false;
        this.yd += 0.01D;
        float hue = (15.0F + level.random.nextFloat() * 25.0F) / 255.0F;
        Color color = Color.getHSBColor(hue, 1.0F, 1.0F);
        this.rCol = color.getRed() / 255.0F;
        this.gCol = color.getGreen() / 255.0F;
        this.bCol = color.getBlue() / 255.0F;
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.removed) {
            this.yd += 0.01D;
            float progress = (float) this.age / (float) this.lifetime;
            float add = 0.75F - progress;
            this.alpha = (float) Math.pow(1.0F - progress, 0.5D) * 0.5F;
            this.rCol = Math.min(1.0F, this.rCol + add);
            this.gCol = Math.min(1.0F, this.gCol + add);
            this.bCol = Math.min(1.0F, this.bCol + add);
            this.quadSize = 0.25F + progress * 0.65F;
            this.setSpriteFromAge(sprites);
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public FlamethrowerParticle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed) {
            return new FlamethrowerParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites);
        }
    }
}
