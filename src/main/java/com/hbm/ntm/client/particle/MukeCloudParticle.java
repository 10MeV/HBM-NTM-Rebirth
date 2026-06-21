package com.hbm.ntm.client.particle;

import com.hbm.ntm.HbmNtm;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class MukeCloudParticle extends Particle implements HbmDeferredParticleRenderer.DeferredParticle {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/particle/explosion.png");
    private static final ResourceLocation BALEFIRE_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/particle/explosion_bf.png");
    private static final float FRAME_SIZE = 1.0F / 5.0F;

    private final boolean balefire;
    private final float friction;
    private float quadSize;

    public MukeCloudParticle(ClientLevel level, double x, double y, double z,
            double motionX, double motionY, double motionZ, boolean balefire) {
        super(level, x, y, z);
        this.xd = motionX;
        this.yd = motionY;
        this.zd = motionZ;
        this.balefire = balefire;
        this.quadSize = 3.0F;
        this.hasPhysics = true;

        if (motionY > 0.0D) {
            this.friction = 0.9F;
            this.lifetime = motionY > 0.1D
                    ? 92 + random.nextInt(11) + (int) (motionY * 20.0D)
                    : 72 + random.nextInt(11);
        } else if (motionY == 0.0D) {
            this.friction = 0.95F;
            this.lifetime = 52 + random.nextInt(11);
        } else {
            this.friction = 0.85F;
            this.lifetime = 122 + random.nextInt(31);
            this.age = 80;
        }
    }

    public static void add(ClientLevel level, double x, double y, double z,
            double motionX, double motionY, double motionZ, boolean balefire) {
        Minecraft.getInstance().particleEngine.add(new MukeCloudParticle(level, x, y, z, motionX, motionY, motionZ, balefire));
    }

    @Override
    public void tick() {
        this.hasPhysics = this.age > 2;
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime - 2) {
            this.remove();
            return;
        }
        this.yd -= 0.04D * this.gravity;
        this.move(this.xd, this.yd, this.zd);
        this.xd *= this.friction;
        this.yd *= this.friction;
        this.zd *= this.friction;
        if (this.onGround) {
            this.xd *= 0.7D;
            this.zd *= 0.7D;
        }
    }

    @Override
    public void render(VertexConsumer consumer, Camera camera, float partialTick) {
        HbmDeferredParticleRenderer.enqueue(this, camera, this.x, this.y, this.z);
    }

    @Override
    public void renderDeferred(MultiBufferSource.BufferSource buffer, Camera camera, float partialTick) {
        VertexConsumer consumer = buffer.getBuffer(HbmDeferredParticleRenderer.texturedNoDepthWrite(
                this.balefire ? BALEFIRE_TEXTURE : TEXTURE));
        int frame = Mth.clamp((int) ((this.age + partialTick) * 25.0F / Math.max(1, this.lifetime)), 0, 24);
        float uMin = (frame % 5) * FRAME_SIZE;
        float uMax = uMin + FRAME_SIZE;
        float vMin = (frame / 5) * FRAME_SIZE;
        float vMax = vMin + FRAME_SIZE;
        Vec3 cameraPos = camera.getPosition();
        double x = Mth.lerp(partialTick, this.xo, this.x) - cameraPos.x();
        double y = Mth.lerp(partialTick, this.yo, this.y) - cameraPos.y();
        double z = Mth.lerp(partialTick, this.zo, this.z) - cameraPos.z();
        float scale = this.quadSize;
        Quaternionf rotation = camera.rotation();
        Vector3f right = new Vector3f(1.0F, 0.0F, 0.0F).rotate(rotation).mul(scale);
        Vector3f up = new Vector3f(0.0F, 1.0F, 0.0F).rotate(rotation).mul(scale);
        float centerX = (float) x;
        float centerY = (float) y;
        float centerZ = (float) z;

        // Legacy ParticleMukeCloud fixes vertical height to +/-scale; only X/Z follow the camera-facing basis.
        putVertex(consumer, centerX - right.x() - up.x(), centerY - scale,
                centerZ - right.z() - up.z(), uMax, vMax);
        putVertex(consumer, centerX - right.x() + up.x(), centerY + scale,
                centerZ - right.z() + up.z(), uMax, vMin);
        putVertex(consumer, centerX + right.x() + up.x(), centerY + scale,
                centerZ + right.z() + up.z(), uMin, vMin);
        putVertex(consumer, centerX + right.x() - up.x(), centerY - scale,
                centerZ + right.z() - up.z(), uMin, vMax);
    }

    private static void putVertex(VertexConsumer consumer, float x, float y, float z, float u, float v) {
        consumer.vertex(x, y, z)
                .uv(u, v)
                .color(1.0F, 1.0F, 1.0F, 1.0F)
                .uv2(LightTexture.FULL_BRIGHT)
                .endVertex();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return HbmDeferredParticleRenderer.DEFERRED_RENDER_TYPE;
    }

    @Override
    public boolean shouldCull() {
        return false;
    }
}
