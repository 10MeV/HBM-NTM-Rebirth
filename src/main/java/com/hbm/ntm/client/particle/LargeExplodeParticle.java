package com.hbm.ntm.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.NoRenderParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LargeExplodeParticle extends TextureSheetParticle {
    private static final int PRIMARY_SPRITES = 16;
    private static final int SECONDARY_SPRITE_OFFSET = 16;
    private static final int SECONDARY_SPRITES = 8;
    private static final int LAST_SPRITE_INDEX = PRIMARY_SPRITES + SECONDARY_SPRITES - 1;
    private static final int HUGE_SEED_LIFETIME = 8;
    private static final float LEGACY_MUZZLE_PRIMARY_SCALE = 0.5F;
    private static SpriteSet sharedSprites;

    private final SpriteSet sprites;
    private final boolean primary;

    private LargeExplodeParticle(ClientLevel level, double x, double y, double z,
            double xSpeed, double ySpeed, double zSpeed, float scale, float red, float green, float blue,
            boolean primary, SpriteSet sprites) {
        super(level, x, y, z);
        this.sprites = sprites;
        this.primary = primary;
        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;
        this.rCol = red;
        this.gCol = green;
        this.bCol = blue;
        if (primary) {
            this.hasPhysics = false;
            this.lifetime = 6 + this.random.nextInt(4);
            this.quadSize = LEGACY_MUZZLE_PRIMARY_SCALE * scale;
            this.setPrimarySprite(0.0F);
        } else {
            this.gravity = -0.1F;
            this.friction = 0.9F;
            this.xd += (this.random.nextDouble() * 2.0D - 1.0D) * 0.05D;
            this.yd += (this.random.nextDouble() * 2.0D - 1.0D) * 0.05D;
            this.zd += (this.random.nextDouble() * 2.0D - 1.0D) * 0.05D;
            this.quadSize = 0.1F * scale * (this.random.nextFloat() * this.random.nextFloat() * 6.0F + 1.0F);
            this.lifetime = (int) (16.0D / (this.random.nextDouble() * 0.8D + 0.2D)) + 2;
            this.setSecondarySprite();
        }
    }

    public static LargeExplodeParticle primary(ClientLevel level, double x, double y, double z, float size) {
        if (sharedSprites == null) {
            return null;
        }
        float color = 1.0F - level.random.nextFloat() * 0.2F;
        return new LargeExplodeParticle(level, x, y, z, 0.0D, 0.0D, 0.0D, size,
                color, 0.9F * color, 0.5F * color, true, sharedSprites);
    }

    public static LargeExplodeParticle largeVanilla(ClientLevel level, double x, double y, double z, float size) {
        if (sharedSprites == null) {
            return null;
        }
        return new LargeExplodeParticle(level, x, y, z, 0.0D, 0.0D, 0.0D, size,
                1.0F, 1.0F, 1.0F, true, sharedSprites);
    }

    public static LargeExplodeParticle explode(ClientLevel level, double x, double y, double z,
            double xSpeed, double ySpeed, double zSpeed) {
        if (sharedSprites == null) {
            return null;
        }
        float color = level.random.nextFloat() * 0.3F + 0.7F;
        return new LargeExplodeParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, 1.0F,
                color, color, color, false, sharedSprites);
    }

    public static LargeExplodeParticle secondary(ClientLevel level, double x, double y, double z, float scale) {
        if (sharedSprites == null) {
            return null;
        }
        float color = 1.0F - level.random.nextFloat() * 0.5F;
        float gray = 0.5F * color;
        return new LargeExplodeParticle(level, x, y, z, 0.0D, 0.0D, 0.0D, scale,
                gray, gray, gray, false, sharedSprites);
    }

    public static Particle hugeExplosionSeed(ClientLevel level, double x, double y, double z) {
        if (sharedSprites == null) {
            return null;
        }
        return new HugeExplosionSeed(level, x, y, z);
    }

    @Override
    public int getLightColor(float partialTick) {
        return this.primary ? LightTexture.FULL_BRIGHT : super.getLightColor(partialTick);
    }

    @Override
    public float getQuadSize(float partialTick) {
        if (!this.primary) {
            return super.getQuadSize(partialTick);
        }
        return this.quadSize;
    }

    @Override
    public void tick() {
        if (this.primary) {
            this.xo = this.x;
            this.yo = this.y;
            this.zo = this.z;
            if (++this.age >= this.lifetime) {
                this.remove();
            } else {
                this.setPrimarySprite(0.0F);
            }
            return;
        }
        super.tick();
        if (!this.removed) {
            this.setSecondarySprite();
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return this.primary ? ParticleRenderType.PARTICLE_SHEET_LIT : ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public void render(VertexConsumer consumer, Camera camera, float partialTick) {
        if (this.primary) {
            this.setPrimarySprite(partialTick);
        }
        super.render(consumer, camera, partialTick);
    }

    private void setPrimarySprite(float partialTick) {
        int frame = Math.min(PRIMARY_SPRITES - 1,
                (int) (((float) this.age + partialTick) * 15.0F / (float) this.lifetime));
        this.setSprite(this.sprites.get(frame, LAST_SPRITE_INDEX));
    }

    private void setSecondarySprite() {
        int oldTextureIndex = Math.max(0, 7 - this.age * SECONDARY_SPRITES / this.lifetime);
        this.setSprite(this.sprites.get(SECONDARY_SPRITE_OFFSET + (7 - oldTextureIndex), LAST_SPRITE_INDEX));
    }

    private static class HugeExplosionSeed extends NoRenderParticle {
        private int timeSinceStart;

        private HugeExplosionSeed(ClientLevel level, double x, double y, double z) {
            super(level, x, y, z, 0.0D, 0.0D, 0.0D);
            this.lifetime = HUGE_SEED_LIFETIME;
        }

        @Override
        public void tick() {
            for (int i = 0; i < 6; i++) {
                Particle particle = largeVanilla(this.level,
                        this.x + (this.random.nextDouble() - this.random.nextDouble()) * 4.0D,
                        this.y + (this.random.nextDouble() - this.random.nextDouble()) * 4.0D,
                        this.z + (this.random.nextDouble() - this.random.nextDouble()) * 4.0D,
                        (float) this.timeSinceStart / (float) HUGE_SEED_LIFETIME);
                if (particle != null) {
                    Minecraft.getInstance().particleEngine.add(particle);
                }
            }
            this.timeSinceStart++;
            if (this.timeSinceStart == HUGE_SEED_LIFETIME) {
                this.remove();
            }
        }
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
            sharedSprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed) {
            return new LargeExplodeParticle(level, x, y, z, 0.0D, 0.0D, 0.0D, (float) xSpeed,
                    1.0F, 1.0F, 1.0F, true, this.sprites);
        }
    }
}
