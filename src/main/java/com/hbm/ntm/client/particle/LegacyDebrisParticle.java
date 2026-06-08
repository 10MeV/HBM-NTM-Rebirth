package com.hbm.ntm.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.concurrent.atomic.AtomicInteger;

@OnlyIn(Dist.CLIENT)
public class LegacyDebrisParticle extends TerrainParticle {
    private static final AtomicInteger NEXT_VISUAL_ID = new AtomicInteger();

    private final int visualId;
    private final int debrisSize;
    private final float bodyScale;
    private final float cubeScale;
    private final float[] cubeOffsets;
    private final float pitchStep;
    private final float yawStep;
    private float rotationPitch;
    private float prevRotationPitch;
    private float rotationYaw;
    private float prevRotationYaw;

    private LegacyDebrisParticle(ClientLevel level, double x, double y, double z,
            double motionX, double motionY, double motionZ, BlockState state, int debrisSize) {
        super(level, x, y, z, 0.0D, 0.0D, 0.0D, state);
        this.visualId = NEXT_VISUAL_ID.incrementAndGet();
        this.debrisSize = Math.max(1, debrisSize);
        this.bodyScale = Mth.clamp(this.debrisSize / 16.0F, 0.35F, 1.6F);
        int cubeCount = Mth.clamp(this.debrisSize / 3, 1, 7);
        this.cubeScale = this.bodyScale / (cubeCount <= 2 ? 1.8F : 2.6F);
        this.cubeOffsets = makeCubeOffsets(cubeCount, this.bodyScale, level.random);
        RandomSource turnRandom = RandomSource.create(this.visualId);
        this.pitchStep = turnRandom.nextFloat() * 10.0F;
        this.yawStep = turnRandom.nextFloat() * 10.0F;
        this.xd = motionX * 3.0D;
        this.yd = motionY * 3.0D;
        this.zd = motionZ * 3.0D;
        this.lifetime = 100;
        this.gravity = 0.15F;
        this.hasPhysics = false;
        this.setSize(this.bodyScale * 0.75F, this.bodyScale * 0.75F);
    }

    public static LegacyDebrisParticle create(ClientLevel level, double x, double y, double z,
            double motionX, double motionY, double motionZ, BlockState state, int debrisSize) {
        if (state == null || state.isAir()) {
            return null;
        }
        return new LegacyDebrisParticle(level, x, y, z, motionX, motionY, motionZ, state, debrisSize);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.prevRotationPitch = this.rotationPitch;
        this.prevRotationYaw = this.rotationYaw;

        if (this.age > 5) {
            this.hasPhysics = true;
        }

        this.rotationPitch += this.pitchStep;
        this.rotationYaw += this.yawStep;

        if (this.visualId % 3 == 0) {
            Particle flame = RocketFlameParticle.createLegacy((ClientLevel) this.level, this.x, this.y, this.z,
                    0.0D, 0.0D, 0.0D, Math.max(this.debrisSize, 6) / 16.0F, 50);
            if (flame != null) {
                Minecraft.getInstance().particleEngine.add(flame);
            }
        }

        this.yd -= this.gravity;
        this.move(this.xd, this.yd, this.zd);

        this.age++;
        if (this.age >= this.lifetime || this.onGround) {
            this.remove();
        }
    }

    @Override
    public void render(VertexConsumer consumer, Camera camera, float partialTick) {
        Vec3 cameraPos = camera.getPosition();
        float x = (float) (Mth.lerp(partialTick, this.xo, this.x) - cameraPos.x());
        float y = (float) (Mth.lerp(partialTick, this.yo, this.y) - cameraPos.y());
        float z = (float) (Mth.lerp(partialTick, this.zo, this.z) - cameraPos.z());
        float pitch = Mth.lerp(partialTick, this.prevRotationPitch, this.rotationPitch) * Mth.DEG_TO_RAD;
        float yaw = Mth.lerp(partialTick, this.prevRotationYaw, this.rotationYaw) * Mth.DEG_TO_RAD;
        Quaternionf rotation = new Quaternionf().rotateY(pitch).rotateZ(yaw);
        int light = this.getLightColor(partialTick);

        for (int i = 0; i < this.cubeOffsets.length; i += 3) {
            renderCube(consumer, rotation, light,
                    x + this.cubeOffsets[i],
                    y + this.cubeOffsets[i + 1],
                    z + this.cubeOffsets[i + 2],
                    this.cubeScale * (1.0F + (i % 2) * 0.1F));
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.TERRAIN_SHEET;
    }

    @Override
    public boolean shouldCull() {
        return false;
    }

    private void renderCube(VertexConsumer consumer, Quaternionf rotation, int light, float x, float y, float z, float scale) {
        float h = scale * 0.5F;
        Vector3f[] corners = new Vector3f[] {
                rotate(-h, -h, -h, rotation, x, y, z),
                rotate(-h, -h, h, rotation, x, y, z),
                rotate(-h, h, -h, rotation, x, y, z),
                rotate(-h, h, h, rotation, x, y, z),
                rotate(h, -h, -h, rotation, x, y, z),
                rotate(h, -h, h, rotation, x, y, z),
                rotate(h, h, -h, rotation, x, y, z),
                rotate(h, h, h, rotation, x, y, z)
        };
        putFace(consumer, light, corners[0], corners[2], corners[3], corners[1]);
        putFace(consumer, light, corners[4], corners[5], corners[7], corners[6]);
        putFace(consumer, light, corners[0], corners[1], corners[5], corners[4]);
        putFace(consumer, light, corners[2], corners[6], corners[7], corners[3]);
        putFace(consumer, light, corners[0], corners[4], corners[6], corners[2]);
        putFace(consumer, light, corners[1], corners[3], corners[7], corners[5]);
    }

    private Vector3f rotate(float x, float y, float z, Quaternionf rotation, float originX, float originY, float originZ) {
        return new Vector3f(x, y, z).rotate(rotation).add(originX, originY, originZ);
    }

    private void putFace(VertexConsumer consumer, int light, Vector3f a, Vector3f b, Vector3f c, Vector3f d) {
        putVertex(consumer, a, this.getU0(), this.getV1(), light);
        putVertex(consumer, b, this.getU0(), this.getV0(), light);
        putVertex(consumer, c, this.getU1(), this.getV0(), light);
        putVertex(consumer, d, this.getU1(), this.getV1(), light);
    }

    private void putVertex(VertexConsumer consumer, Vector3f pos, float u, float v, int light) {
        consumer.vertex(pos.x(), pos.y(), pos.z())
                .uv(u, v)
                .color(this.rCol, this.gCol, this.bCol, this.alpha)
                .uv2(light)
                .endVertex();
    }

    private static float[] makeCubeOffsets(int cubeCount, float bodyScale, RandomSource random) {
        float[] offsets = new float[cubeCount * 3];
        for (int i = 1; i < cubeCount; i++) {
            int base = i * 3;
            float spread = bodyScale * 0.35F;
            offsets[base] = (random.nextFloat() - 0.5F) * spread;
            offsets[base + 1] = (random.nextFloat() - 0.5F) * spread;
            offsets[base + 2] = (random.nextFloat() - 0.5F) * spread;
        }
        return offsets;
    }
}
