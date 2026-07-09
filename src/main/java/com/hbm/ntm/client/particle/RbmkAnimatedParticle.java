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

@OnlyIn(Dist.CLIENT)
public class RbmkAnimatedParticle extends TextureSheetParticle implements HbmDeferredParticleRenderer.DeferredParticle {
    private static SpriteSet flameSprites;
    private static SpriteSet steamSprites;
    private static SpriteSet mushSprites;

    private final SpriteSet sprites;
    private final Mode mode;
    private final float baseScale;
    private float cachedU0;
    private float cachedU1;
    private float cachedV0;
    private float cachedV1;

    private RbmkAnimatedParticle(ClientLevel level, double x, double y, double z, Mode mode, float scale, int lifetime, SpriteSet sprites) {
        super(level, x, y, z);
        this.sprites = sprites;
        this.mode = mode;
        this.lifetime = Math.max(1, lifetime);
        this.baseScale = scale;
        this.quadSize = scale;
        this.hasPhysics = false;
        this.alpha = mode == Mode.STEAM ? 0.25F : 1.0F;
        this.setSpriteFromAge(sprites);
        this.cacheSpriteUv();
    }

    public static RbmkAnimatedParticle flame(ClientLevel level, double x, double y, double z, int lifetime) {
        if (flameSprites == null) {
            return null;
        }
        return new RbmkAnimatedParticle(level, x, y, z, Mode.FLAME, level.random.nextFloat() + 1.0F, lifetime, flameSprites);
    }

    public static RbmkAnimatedParticle steam(ClientLevel level, double x, double y, double z) {
        if (steamSprites == null) {
            return null;
        }
        return new RbmkAnimatedParticle(level, x, y, z, Mode.STEAM, 4.0F, 10, steamSprites);
    }

    public static RbmkAnimatedParticle mush(ClientLevel level, double x, double y, double z, float scale) {
        if (mushSprites == null) {
            return null;
        }
        return new RbmkAnimatedParticle(level, x, y, z, Mode.MUSH, scale, 50, mushSprites);
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
        this.setSpriteFromAge(sprites);
        this.cacheSpriteUv();
    }

    @Override
    public void render(VertexConsumer consumer, Camera camera, float partialTick) {
        HbmDeferredParticleRenderer.enqueue(this, camera, this.x, this.y, this.z);
    }

    @Override
    public void renderDeferred(MultiBufferSource.BufferSource buffer, Camera camera, float partialTick) {
        float renderAge = Math.min(this.age + partialTick, this.lifetime);
        float alpha = switch (mode) {
            case FLAME -> flameAlpha(renderAge) * 0.5F;
            case STEAM -> 0.25F;
            case MUSH -> 1.0F;
        };
        if (alpha <= 0.0F) {
            return;
        }
        VertexConsumer consumer = HbmDeferredParticleRenderer.particleSheetAdditiveNoDepthWriteConsumer(buffer);
        Vec3 cameraPos = camera.getPosition();
        float x = (float) (Mth.lerp(partialTick, this.xo, this.x) - cameraPos.x());
        float y = (float) (Mth.lerp(partialTick, this.yo, this.y) - cameraPos.y());
        float z = (float) (Mth.lerp(partialTick, this.zo, this.z) - cameraPos.z());
        float width = mode == Mode.STEAM ? this.baseScale * 0.25F : this.baseScale;
        float height = mode == Mode.FLAME ? this.baseScale * 2.0F : mode == Mode.STEAM ? this.baseScale : this.baseScale;
        float offsetX = mode == Mode.STEAM ? -0.9375F : mode == Mode.FLAME ? -1.0F : 0.0F;
        float offsetY = mode == Mode.STEAM ? -0.25F : mode == Mode.MUSH ? this.baseScale : 0.0F;
        Quaternionf rotation = HbmDeferredParticleRenderer.scratchRotation().rotateY(-camera.getYRot() * Mth.DEG_TO_RAD);
        float u0 = this.cachedU0;
        float u1 = this.cachedU1;
        float v0 = this.cachedV0;
        float v1 = this.cachedV1;
        if (mode == Mode.FLAME) {
            int frame = (((int) renderAge * 5) % 14) % 5;
            float widthStep = (u1 - u0) / 14.0F;
            u1 = u0 + widthStep * (frame + 1);
            u0 += widthStep * frame;
        } else if (mode == Mode.STEAM) {
            int frame = ((int) (renderAge / (float) this.lifetime * 20.0F) % 20 + 19) % 20;
            float widthStep = (u1 - u0) / 20.0F;
            u1 = u0 + widthStep * (frame + 1);
            u0 += widthStep * frame;
        } else {
            int frame = Mth.clamp((int) (renderAge / (float) this.lifetime * 30.0F), 0, 29);
            float heightStep = (v1 - v0) / 30.0F;
            v1 = v0 + heightStep * (frame + 1);
            v0 += heightStep * frame;
        }
        HbmDeferredParticleRenderer.emitLocalParticleSheetQuad(consumer, LightTexture.FULL_BRIGHT, rotation, x, y, z,
                -width + offsetX, -height + offsetY, 0.0F, u1, v1,
                -width + offsetX, height + offsetY, 0.0F, u1, v0,
                width + offsetX, height + offsetY, 0.0F, u0, v0,
                width + offsetX, -height + offsetY, 0.0F, u0, v1,
                1.0F, 1.0F, 1.0F, alpha);
    }

    private void cacheSpriteUv() {
        this.cachedU0 = this.getU0();
        this.cachedU1 = this.getU1();
        this.cachedV0 = this.getV0();
        this.cachedV1 = this.getV1();
    }

    private float flameAlpha(float renderAge) {
        float alpha = 1.0F;
        if (renderAge < 20.0F) {
            alpha = renderAge / 20.0F;
        }
        if (renderAge > this.lifetime - 20.0F) {
            alpha = (this.lifetime - renderAge) / 20.0F;
        }
        return Math.max(0.0F, alpha);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return HbmDeferredParticleRenderer.DEFERRED_RENDER_TYPE;
    }

    private enum Mode {
        FLAME,
        STEAM,
        MUSH
    }

    public static final class FlameProvider implements ParticleProvider<SimpleParticleType> {
        public FlameProvider(SpriteSet sprites) {
            flameSprites = sprites;
        }

        @Override
        public RbmkAnimatedParticle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed) {
            return flame(level, x, y, z, 40);
        }
    }

    public static final class SteamProvider implements ParticleProvider<SimpleParticleType> {
        public SteamProvider(SpriteSet sprites) {
            steamSprites = sprites;
        }

        @Override
        public RbmkAnimatedParticle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed) {
            return steam(level, x, y, z);
        }
    }

    public static final class MushProvider implements ParticleProvider<SimpleParticleType> {
        public MushProvider(SpriteSet sprites) {
            mushSprites = sprites;
        }

        @Override
        public RbmkAnimatedParticle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed) {
            return mush(level, x, y, z, 1.0F);
        }
    }
}
