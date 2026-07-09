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
    private static SpriteSet sharedSprites;

    private final SpriteSet sprites;
    private final boolean primary;
    private float cachedU0;
    private float cachedU1;
    private float cachedV0;
    private float cachedV1;

    private LargeExplodeParticle(ClientLevel level, double x, double y, double z,
            double xSpeed, double ySpeed, double zSpeed, float scale, float red, float green, float blue,
            boolean primary, int lifetime, SpriteSet sprites) {
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
            this.lifetime = lifetime;
            this.quadSize = legacyLargeExplodeHalfExtent(scale);
            this.setPrimarySprite(0.0F);
        } else {
            this.hasPhysics = false;
            this.lifetime = lifetime;
            this.quadSize = 0.1F * scale;
            this.gravity = -0.1F;
            this.friction = 0.9F;
            this.setSecondarySprite();
        }
    }

    public static LargeExplodeParticle primary(ClientLevel level, double x, double y, double z, float size) {
        if (sharedSprites == null) {
            return null;
        }
        float color = 1.0F - level.random.nextFloat() * 0.2F;
        return new LargeExplodeParticle(level, x, y, z, 0.0D, 0.0D, 0.0D, size,
                color, 0.9F * color, 0.5F * color, true, primaryLifetime(level), sharedSprites);
    }

    public static LargeExplodeParticle largeVanilla(ClientLevel level, double x, double y, double z, float size) {
        if (sharedSprites == null) {
            return null;
        }
        return new LargeExplodeParticle(level, x, y, z, 0.0D, 0.0D, 0.0D, size,
                1.0F, 1.0F, 1.0F, true, primaryLifetime(level), sharedSprites);
    }

    public static LargeExplodeParticle explode(ClientLevel level, double x, double y, double z,
            double xSpeed, double ySpeed, double zSpeed) {
        if (sharedSprites == null) {
            return null;
        }
        float color = level.random.nextFloat() * 0.3F + 0.7F;
        float legacyScale = level.random.nextFloat() * level.random.nextFloat() * 6.0F + 1.0F;
        return new LargeExplodeParticle(level, x, y, z,
                legacyExplodeMotion(level, xSpeed), legacyExplodeMotion(level, ySpeed), legacyExplodeMotion(level, zSpeed),
                legacyScale,
                color, color, color, false, secondaryLifetime(level), sharedSprites);
    }

    public static LargeExplodeParticle secondary(ClientLevel level, double x, double y, double z, float scale) {
        if (sharedSprites == null) {
            return null;
        }
        float color = 1.0F - level.random.nextFloat() * 0.5F;
        float gray = 0.5F * color;
        float legacyScale = (level.random.nextFloat() * level.random.nextFloat() * 6.0F + 1.0F) * scale;
        return new LargeExplodeParticle(level, x, y, z,
                legacyExplodeMotion(level, 0.0D), legacyExplodeMotion(level, 0.0D), legacyExplodeMotion(level, 0.0D),
                legacyScale, gray, gray, gray, false, secondaryLifetime(level), sharedSprites);
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
        HbmDeferredParticleRenderer.emitTextureSheetParticleQuad(consumer, camera, partialTick,
                this.xo, this.yo, this.zo, this.x, this.y, this.z,
                this.oRoll, this.roll, this.getQuadSize(partialTick),
                this.cachedU0, this.cachedU1, this.cachedV0, this.cachedV1,
                this.rCol, this.gCol, this.bCol, this.alpha, this.getLightColor(partialTick));
    }

    private void setPrimarySprite(float partialTick) {
        int frame = Math.min(PRIMARY_SPRITES - 1,
                (int) (((float) this.age + partialTick) * 15.0F / (float) this.lifetime));
        this.setSprite(this.sprites.get(frame, LAST_SPRITE_INDEX));
        this.cacheSpriteUv();
    }

    private void setSecondarySprite() {
        int oldTextureIndex = Math.max(0, 7 - this.age * SECONDARY_SPRITES / this.lifetime);
        this.setSprite(this.sprites.get(SECONDARY_SPRITE_OFFSET + (7 - oldTextureIndex), LAST_SPRITE_INDEX));
        this.cacheSpriteUv();
    }

    private void cacheSpriteUv() {
        this.cachedU0 = this.getU0();
        this.cachedU1 = this.getU1();
        this.cachedV0 = this.getV0();
        this.cachedV1 = this.getV1();
    }

    private static int primaryLifetime(ClientLevel level) {
        return 6 + level.random.nextInt(4);
    }

    private static int secondaryLifetime(ClientLevel level) {
        return (int) (16.0D / (level.random.nextDouble() * 0.8D + 0.2D)) + 2;
    }

    private static double legacyExplodeMotion(ClientLevel level, double base) {
        return base + (level.random.nextDouble() * 2.0D - 1.0D) * 0.05D;
    }

    private static float legacyLargeExplodeHalfExtent(float size) {
        return 2.0F - size;
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
                    1.0F, 1.0F, 1.0F, true, primaryLifetime(level), this.sprites);
        }
    }
}
