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
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.awt.Color;
import java.util.concurrent.atomic.AtomicInteger;

@OnlyIn(Dist.CLIENT)
public class FlamethrowerParticle extends TextureSheetParticle implements HbmDeferredParticleRenderer.DeferredParticle {
    private static final AtomicInteger NEXT_VISUAL_ID = new AtomicInteger();

    public static final int META_FIRE = 0;
    public static final int META_BALEFIRE = 1;
    public static final int META_DIGAMMA = 2;
    public static final int META_OXY = 3;
    public static final int META_BLACK = 4;

    private final SpriteSet sprites;
    private final int legacyType;
    private final float baseRed;
    private final float baseGreen;
    private final float baseBlue;
    private final float rollSpeed;

    private FlamethrowerParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites, int legacyType) {
        super(level, x, y, z);
        this.sprites = sprites;
        this.legacyType = legacyType;
        this.lifetime = 20 + level.random.nextInt(10);
        this.quadSize = 0.5F;
        this.hasPhysics = false;
        this.xd = level.random.nextGaussian() * 0.02D;
        this.yd = 0.0D;
        this.zd = level.random.nextGaussian() * 0.02D;
        int visualId = NEXT_VISUAL_ID.incrementAndGet();
        this.rollSpeed = (visualId % 2 - 0.5F) * 30.0F * Mth.DEG_TO_RAD;

        float initialColor = 15.0F + level.random.nextFloat() * 25.0F;
        if (legacyType == META_BALEFIRE) {
            initialColor = 65.0F + level.random.nextFloat() * 35.0F;
        } else if (legacyType == META_DIGAMMA) {
            initialColor = -level.random.nextFloat() * 15.0F;
        }
        float hue = initialColor / 255.0F;
        Color color = Color.getHSBColor(hue, 1.0F, 1.0F);
        if (legacyType == META_OXY || legacyType == META_BLACK) {
            this.baseRed = 1.0F;
            this.baseGreen = 1.0F;
            this.baseBlue = 1.0F;
        } else {
            this.baseRed = color.getRed() / 255.0F;
            this.baseGreen = color.getGreen() / 255.0F;
            this.baseBlue = color.getBlue() / 255.0F;
        }
        this.updateVisuals(0.0F);
        this.setSpriteFromAge(sprites);
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
        this.xd *= 0.91D;
        this.yd *= 0.91D;
        this.zd *= 0.91D;
        this.yd += 0.01D;
        this.oRoll = this.roll;
        this.roll += this.rollSpeed;
        this.move(this.xd, this.yd, this.zd);
        this.updateVisuals((float) this.age / (float) this.lifetime);
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void render(VertexConsumer consumer, Camera camera, float partialTick) {
        HbmDeferredParticleRenderer.enqueue(this, camera, this.x, this.y, this.z);
    }

    @Override
    public void renderDeferred(MultiBufferSource.BufferSource buffer, Camera camera, float partialTick) {
        this.updateVisuals((this.age + partialTick) / (float) this.lifetime);
        super.render(buffer.getBuffer(HbmDeferredParticleRenderer.particleSheetDepthWrite()), camera, partialTick);
    }

    private void updateVisuals(float progress) {
        float ageScaled = Mth.clamp(progress, 0.0F, 1.0F);
        if (this.legacyType == META_OXY) {
            this.alpha = 1.0F - ageScaled;
            float add = ageScaled * 1.25F - 0.25F;
            this.rCol = clampColor(this.baseRed - add);
            this.gCol = clampColor(this.baseGreen - add * 0.75F);
            this.bCol = this.baseBlue;
        } else if (this.legacyType == META_BLACK) {
            this.alpha = 1.0F - ageScaled;
            float add = ageScaled * 2.0F - 0.25F;
            this.rCol = clampColor(this.baseRed - add * 0.75F);
            this.gCol = clampColor(this.baseGreen - add);
            this.bCol = clampColor(this.baseBlue - add * 0.5F);
        } else {
            this.alpha = (float) Math.pow(1.0F - ageScaled, 0.5D) * 0.5F;
            float add = 0.75F - ageScaled;
            this.rCol = clampColor(this.baseRed + add);
            this.gCol = clampColor(this.baseGreen + add);
            this.bCol = clampColor(this.baseBlue + add);
        }
        this.quadSize = (ageScaled * 1.25F + 0.25F) * 0.5F;
    }

    private static float clampColor(float color) {
        return Mth.clamp(color, 0.0F, 1.0F);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return HbmDeferredParticleRenderer.DEFERRED_RENDER_TYPE;
    }

    @Override
    protected int getLightColor(float partialTick) {
        return 0xF000F0;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        private final int legacyType;

        public Provider(SpriteSet sprites) {
            this(sprites, META_FIRE);
        }

        public Provider(SpriteSet sprites, int legacyType) {
            this.sprites = sprites;
            this.legacyType = legacyType;
        }

        @Override
        public FlamethrowerParticle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed) {
            return new FlamethrowerParticle(level, x, y, z, sprites, legacyType);
        }
    }
}
