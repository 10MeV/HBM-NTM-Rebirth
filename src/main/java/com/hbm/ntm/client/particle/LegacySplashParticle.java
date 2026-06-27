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
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.concurrent.atomic.AtomicInteger;

@OnlyIn(Dist.CLIENT)
public class LegacySplashParticle extends TextureSheetParticle implements HbmDeferredParticleRenderer.DeferredParticle {
    private static final AtomicInteger NEXT_VISUAL_ID = new AtomicInteger();
    private static SpriteSet sharedSprites;
    private final SpriteSet sprites;
    private final boolean flipU;
    private final boolean flipV;

    private LegacySplashParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites, int color) {
        super(level, x, y, z);
        this.sprites = sprites;
        int visualId = NEXT_VISUAL_ID.incrementAndGet();
        this.flipU = visualId % 2 == 0;
        this.flipV = visualId % 4 < 2;
        if (color >= 0) {
            float shade = 1.0F - level.random.nextFloat() * 0.2F;
            this.rCol = ((color >> 16) & 255) / 255.0F * shade;
            this.gCol = ((color >> 8) & 255) / 255.0F * shade;
            this.bCol = (color & 255) / 255.0F * shade;
        } else {
            this.rCol = this.gCol = this.bCol = 1.0F - level.random.nextFloat() * 0.2F;
        }
        this.alpha = 0.5F;
        this.quadSize = 0.4F;
        this.lifetime = 200 + level.random.nextInt(50);
        this.gravity = 0.4F;
        this.hasPhysics = true;
        this.setSpriteFromAge(sprites);
    }

    public static LegacySplashParticle create(ClientLevel level, double x, double y, double z, int color) {
        return sharedSprites == null ? null : new LegacySplashParticle(level, x, y, z, sharedSprites, color);
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
        if (!this.onGround) {
            this.xd += this.random.nextGaussian() * 0.002D;
            this.zd += this.random.nextGaussian() * 0.002D;
            if (this.yd < -0.5D) {
                this.yd = -0.5D;
            }
            this.yd -= 0.04D * this.gravity;
            this.move(this.xd, this.yd, this.zd);
            this.xd *= 0.98D;
            this.yd *= 0.98D;
            this.zd *= 0.98D;
        } else {
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
        VertexConsumer consumer = buffer.getBuffer(HbmDeferredParticleRenderer.particleSheetDepthWrite());
        renderFlippedBillboard(consumer, camera, partialTick, this.flipU, this.flipV);
    }

    private void renderFlippedBillboard(VertexConsumer consumer, Camera camera, float partialTick, boolean flipU, boolean flipV) {
        Vec3 cameraPos = camera.getPosition();
        float x = (float) (Mth.lerp(partialTick, this.xo, this.x) - cameraPos.x());
        float y = (float) (Mth.lerp(partialTick, this.yo, this.y) - cameraPos.y());
        float z = (float) (Mth.lerp(partialTick, this.zo, this.z) - cameraPos.z());
        float scale = this.getQuadSize(partialTick);
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
        float minU = flipU ? this.getU1() : this.getU0();
        float maxU = flipU ? this.getU0() : this.getU1();
        float minV = flipV ? this.getV1() : this.getV0();
        float maxV = flipV ? this.getV0() : this.getV1();
        int light = this.getLightColor(partialTick);
        HbmDeferredParticleRenderer.emitParticleSheetQuad(consumer, light,
                corners[0], maxU, maxV,
                corners[1], maxU, minV,
                corners[2], minU, minV,
                corners[3], minU, maxV,
                this.rCol, this.gCol, this.bCol, this.alpha);
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
        public LegacySplashParticle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed) {
            return create(level, x, y, z, -1);
        }
    }
}
