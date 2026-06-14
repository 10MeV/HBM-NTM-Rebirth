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

@OnlyIn(Dist.CLIENT)
public class NetworkDebugParticle extends TextureSheetParticle implements HbmDeferredParticleRenderer.DeferredParticle {
    private static SpriteSet powerSprites;
    private static SpriteSet fluidSprites;
    private final SpriteSet sprites;
    private final double targetX;
    private final double targetY;
    private final double targetZ;

    private NetworkDebugParticle(ClientLevel level, double x, double y, double z, double targetX, double targetY, double targetZ,
            SpriteSet sprites, int color) {
        super(level, x, y, z);
        this.sprites = sprites;
        this.targetX = targetX;
        this.targetY = targetY;
        this.targetZ = targetZ;
        this.xd = targetX;
        this.yd = targetY;
        this.zd = targetZ;
        this.lifetime = 10;
        this.quadSize = 0.05F;
        this.hasPhysics = false;
        this.setColorFromInt(color);
        this.setSpriteFromAge(sprites);
    }

    public static NetworkDebugParticle power(ClientLevel level, double x, double y, double z, double targetX, double targetY, double targetZ) {
        return powerSprites == null ? null : new NetworkDebugParticle(level, x, y, z, targetX, targetY, targetZ, powerSprites, 0xFFFFFF);
    }

    public static NetworkDebugParticle fluid(ClientLevel level, double x, double y, double z, double targetX, double targetY, double targetZ, int color) {
        return fluidSprites == null ? null : new NetworkDebugParticle(level, x, y, z, targetX, targetY, targetZ, fluidSprites, color);
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
        this.x += this.targetX;
        this.y += this.targetY;
        this.z += this.targetZ;
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void render(VertexConsumer consumer, Camera camera, float partialTick) {
        HbmDeferredParticleRenderer.enqueue(this, camera, this.x, this.y, this.z);
    }

    @Override
    public void renderDeferred(MultiBufferSource.BufferSource buffer, Camera camera, float partialTick) {
        VertexConsumer consumer = buffer.getBuffer(HbmDeferredParticleRenderer.particleSheetDepthWrite());
        Vec3 cameraPos = camera.getPosition();
        float x = (float) (Mth.lerp(partialTick, this.xo, this.x) - cameraPos.x());
        float y = (float) (Mth.lerp(partialTick, this.yo, this.y) - cameraPos.y());
        float z = (float) (Mth.lerp(partialTick, this.zo, this.z) - cameraPos.z());
        Quaternionf rotation = camera.rotation();
        Vector3f[] corners = new Vector3f[] {
                new Vector3f(-1.0F, -1.0F, 0.0F),
                new Vector3f(-1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, -1.0F, 0.0F)
        };
        for (Vector3f corner : corners) {
            corner.rotate(rotation).mul(this.quadSize).add(x, y, z);
        }
        consumer.vertex(corners[0].x(), corners[0].y(), corners[0].z()).uv(getU1(), getV1()).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(LightTexture.FULL_BRIGHT).endVertex();
        consumer.vertex(corners[1].x(), corners[1].y(), corners[1].z()).uv(getU1(), getV0()).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(LightTexture.FULL_BRIGHT).endVertex();
        consumer.vertex(corners[2].x(), corners[2].y(), corners[2].z()).uv(getU0(), getV0()).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(LightTexture.FULL_BRIGHT).endVertex();
        consumer.vertex(corners[3].x(), corners[3].y(), corners[3].z()).uv(getU0(), getV1()).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(LightTexture.FULL_BRIGHT).endVertex();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return HbmDeferredParticleRenderer.DEFERRED_RENDER_TYPE;
    }

    private void setColorFromInt(int color) {
        this.rCol = ((color >> 16) & 255) / 255.0F;
        this.gCol = ((color >> 8) & 255) / 255.0F;
        this.bCol = (color & 255) / 255.0F;
    }

    public static final class PowerProvider implements ParticleProvider<SimpleParticleType> {
        public PowerProvider(SpriteSet sprites) {
            powerSprites = sprites;
        }

        @Override
        public NetworkDebugParticle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed) {
            return power(level, x, y, z, xSpeed, ySpeed, zSpeed);
        }
    }

    public static final class FluidProvider implements ParticleProvider<SimpleParticleType> {
        public FluidProvider(SpriteSet sprites) {
            fluidSprites = sprites;
        }

        @Override
        public NetworkDebugParticle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed) {
            return fluid(level, x, y, z, xSpeed, ySpeed, zSpeed, 0xFFFFFF);
        }
    }
}
