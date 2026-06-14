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
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

@OnlyIn(Dist.CLIENT)
public class FoamParticle extends TextureSheetParticle implements HbmDeferredParticleRenderer.DeferredParticle {
    private static final AtomicInteger NEXT_VISUAL_ID = new AtomicInteger();

    private final SpriteSet sprites;
    private final int visualId;
    private final List<TrailPoint> trail = new ArrayList<>();
    private int trailLength = 15;
    private float baseScale = 1.0F;
    private float maxScale = 1.5F;
    private float buoyancy = 0.05F;
    private float jitter = 0.15F;
    private float drag = 0.96F;
    private int explosionPhase;

    private FoamParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites) {
        super(level, x, y, z);
        this.sprites = sprites;
        this.visualId = NEXT_VISUAL_ID.incrementAndGet();
        this.lifetime = 60 + level.random.nextInt(60);
        this.gravity = 0.005F + level.random.nextFloat() * 0.015F;
        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;
        this.quadSize = 0.3F + this.random.nextFloat() * 0.7F;
        this.rCol = this.gCol = this.bCol = 1.0F;
        this.alpha = 0.8F;
        this.hasPhysics = true;
        this.setSpriteFromAge(sprites);
    }

    public void setBaseScale(float baseScale) {
        this.baseScale = baseScale;
    }

    public void setMaxScale(float maxScale) {
        this.maxScale = maxScale;
    }

    public void setTrailLength(int trailLength) {
        this.trailLength = Math.max(1, trailLength);
    }

    public void setBuoyancy(float buoyancy) {
        this.buoyancy = buoyancy;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.trail.add(0, new TrailPoint(this.x, this.y, this.z));
        while (this.trail.size() > this.trailLength) {
            this.trail.remove(this.trail.size() - 1);
        }
        if (++this.age >= this.lifetime) {
            this.remove();
            return;
        }

        float progress = (float) this.age / (float) this.lifetime;
        if (progress < 0.3F) {
            this.explosionPhase = 0;
            if (progress < 0.15F) {
                this.yd += this.buoyancy * 6.0F;
            } else {
                this.yd += this.buoyancy * (1.0F - progress / 0.3F) * 2.0F;
            }
            this.quadSize = this.baseScale + (this.maxScale - this.baseScale) * (progress / 0.3F);
        } else if (progress < 0.6F) {
            this.explosionPhase = 1;
            this.yd *= 0.98D;
            this.quadSize = this.maxScale;
        } else {
            this.explosionPhase = 2;
            this.yd -= this.gravity;
            this.quadSize = this.maxScale * (1.0F - ((progress - 0.6F) / 0.4F) * 0.7F);
        }

        this.alpha = 0.8F * (1.0F - progress * progress);
        this.xd += (this.random.nextFloat() - 0.5F) * this.jitter;
        this.zd += (this.random.nextFloat() - 0.5F) * this.jitter;
        this.xd *= this.drag;
        this.yd *= this.drag;
        this.zd *= this.drag;
        this.move(this.xd, this.yd, this.zd);
        if (this.onGround) {
            this.remove();
            return;
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
        renderFoamBubbles(consumer, camera, Mth.lerp(partialTick, this.xo, this.x),
                Mth.lerp(partialTick, this.yo, this.y),
                Mth.lerp(partialTick, this.zo, this.z), this.quadSize, this.alpha);

        for (int i = 1; i < this.trail.size(); i++) {
            TrailPoint point = this.trail.get(i);
            float trailFactor = 1.0F - (float) i / (float) this.trailLength;
            renderFoamBubbles(consumer, camera, point.x, point.y, point.z,
                    this.quadSize * trailFactor,
                    this.alpha * trailFactor * 0.7F);
        }
    }

    private void renderFoamBubbles(VertexConsumer consumer, Camera camera, double worldX, double worldY, double worldZ,
            float scale, float alpha) {
        Random visualRandom = new Random(this.visualId + (long) (worldX * 100.0D) + (long) (worldY * 10.0D) + (long) worldZ);
        int bubbleCount = this.explosionPhase == 0 ? 8 : this.explosionPhase == 1 ? 6 : 4;
        float offset = this.explosionPhase == 0 ? 0.4F : this.explosionPhase == 1 ? 0.6F : 0.9F;
        Vec3 cameraPos = camera.getPosition();
        Quaternionf rotation = camera.rotation();
        int light = LightTexture.FULL_BRIGHT;

        for (int i = 0; i < bubbleCount; i++) {
            float whiteness = 0.9F + visualRandom.nextFloat() * 0.1F;
            float bubbleScale = scale * (visualRandom.nextFloat() * 0.5F + 0.75F);
            float x = (float) (worldX - cameraPos.x() + visualRandom.nextGaussian() * offset);
            float y = (float) (worldY - cameraPos.y() + visualRandom.nextGaussian() * offset * 0.7F);
            float z = (float) (worldZ - cameraPos.z() + visualRandom.nextGaussian() * offset);
            Vector3f[] corners = new Vector3f[] {
                    new Vector3f(-1.0F, -1.0F, 0.0F),
                    new Vector3f(-1.0F, 1.0F, 0.0F),
                    new Vector3f(1.0F, 1.0F, 0.0F),
                    new Vector3f(1.0F, -1.0F, 0.0F)
            };
            for (Vector3f corner : corners) {
                corner.rotate(rotation).mul(bubbleScale).add(x, y, z);
            }
            consumer.vertex(corners[0].x(), corners[0].y(), corners[0].z()).uv(getU1(), getV1()).color(whiteness, whiteness, whiteness, alpha).uv2(light).endVertex();
            consumer.vertex(corners[1].x(), corners[1].y(), corners[1].z()).uv(getU1(), getV0()).color(whiteness, whiteness, whiteness, alpha).uv2(light).endVertex();
            consumer.vertex(corners[2].x(), corners[2].y(), corners[2].z()).uv(getU0(), getV0()).color(whiteness, whiteness, whiteness, alpha).uv2(light).endVertex();
            consumer.vertex(corners[3].x(), corners[3].y(), corners[3].z()).uv(getU0(), getV1()).color(whiteness, whiteness, whiteness, alpha).uv2(light).endVertex();
        }
    }

    @Override
    public boolean shouldCull() {
        return false;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return HbmDeferredParticleRenderer.DEFERRED_RENDER_TYPE;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public FoamParticle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed) {
            return new FoamParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites);
        }
    }

    private record TrailPoint(double x, double y, double z) {
    }
}
