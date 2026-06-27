package com.hbm.ntm.client.particle;

import com.hbm.ntm.client.obj.LegacyTexturedQuadRenderer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
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
public class LegacyDebrisParticle extends Particle {
    private static final AtomicInteger NEXT_VISUAL_ID = new AtomicInteger();

    private final int visualId;
    private final int debrisSize;
    private final DebrisCell[] cells;
    private final float pitchStep;
    private final float yawStep;
    private float rotationPitch;
    private float prevRotationPitch;
    private float rotationYaw;
    private float prevRotationYaw;

    private LegacyDebrisParticle(ClientLevel level, double x, double y, double z,
            double motionX, double motionY, double motionZ, BlockState[] states, int debrisSize) {
        super(level, x, y, z);
        this.visualId = NEXT_VISUAL_ID.incrementAndGet();
        this.debrisSize = Math.max(1, debrisSize);
        this.cells = makeCells(states, this.debrisSize);
        RandomSource turnRandom = RandomSource.create(this.visualId);
        this.pitchStep = turnRandom.nextFloat() * 10.0F;
        this.yawStep = turnRandom.nextFloat() * 10.0F;
        this.xd = motionX * 3.0D;
        this.yd = motionY * 3.0D;
        this.zd = motionZ * 3.0D;
        this.lifetime = 100;
        this.gravity = 0.15F;
        this.hasPhysics = false;
        this.setSize(0.2F, 0.2F);
    }

    public static LegacyDebrisParticle create(ClientLevel level, double x, double y, double z,
            double motionX, double motionY, double motionZ, BlockState[] states, int debrisSize) {
        if (debrisSize <= 0 || states == null || states.length == 0) {
            return null;
        }
        return new LegacyDebrisParticle(level, x, y, z, motionX, motionY, motionZ, states, debrisSize);
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

        for (DebrisCell cell : this.cells) {
            renderBlockCube(consumer, rotation, light, x, y, z, cell);
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

    private void renderBlockCube(VertexConsumer consumer, Quaternionf rotation, int light,
            float originX, float originY, float originZ, DebrisCell cell) {
        float half = this.debrisSize * 0.5F;
        float x0 = cell.x - half;
        float y0 = cell.y - half;
        float z0 = cell.z - half;
        float x1 = x0 + 1.0F;
        float y1 = y0 + 1.0F;
        float z1 = z0 + 1.0F;
        Vector3f[] corners = new Vector3f[] {
                rotate(x0, y0, z0, rotation, originX, originY, originZ),
                rotate(x0, y0, z1, rotation, originX, originY, originZ),
                rotate(x0, y1, z0, rotation, originX, originY, originZ),
                rotate(x0, y1, z1, rotation, originX, originY, originZ),
                rotate(x1, y0, z0, rotation, originX, originY, originZ),
                rotate(x1, y0, z1, rotation, originX, originY, originZ),
                rotate(x1, y1, z0, rotation, originX, originY, originZ),
                rotate(x1, y1, z1, rotation, originX, originY, originZ)
        };
        putFace(consumer, light, cell, corners[0], corners[2], corners[3], corners[1]);
        putFace(consumer, light, cell, corners[4], corners[5], corners[7], corners[6]);
        putFace(consumer, light, cell, corners[0], corners[1], corners[5], corners[4]);
        putFace(consumer, light, cell, corners[2], corners[6], corners[7], corners[3]);
        putFace(consumer, light, cell, corners[0], corners[4], corners[6], corners[2]);
        putFace(consumer, light, cell, corners[1], corners[3], corners[7], corners[5]);
    }

    private Vector3f rotate(float x, float y, float z, Quaternionf rotation, float originX, float originY, float originZ) {
        return new Vector3f(x, y, z).rotate(rotation).add(originX, originY, originZ);
    }

    private void putFace(VertexConsumer consumer, int light, DebrisCell cell, Vector3f a, Vector3f b, Vector3f c, Vector3f d) {
        LegacyTexturedQuadRenderer.emitParticleQuadIdentity(consumer, light,
                particleVertex(cell, a, cell.u0, cell.v1),
                particleVertex(cell, b, cell.u0, cell.v0),
                particleVertex(cell, c, cell.u1, cell.v0),
                particleVertex(cell, d, cell.u1, cell.v1));
    }

    private LegacyTexturedQuadRenderer.Vertex particleVertex(DebrisCell cell, Vector3f pos, float u, float v) {
        return LegacyTexturedQuadRenderer.vertexRgbaF(pos.x(), pos.y(), pos.z(), u, v,
                cell.red, cell.green, cell.blue, this.alpha);
    }

    private static DebrisCell[] makeCells(BlockState[] states, int debrisSize) {
        java.util.ArrayList<DebrisCell> cells = new java.util.ArrayList<>();
        int size = Math.max(1, debrisSize);
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                for (int z = 0; z < size; z++) {
                    int index = (x * size + y) * size + z;
                    if (index < 0 || index >= states.length) {
                        continue;
                    }
                    BlockState state = states[index];
                    if (state == null || state.isAir()) {
                        continue;
                    }
                    TextureAtlasSprite sprite = Minecraft.getInstance().getBlockRenderer()
                            .getBlockModelShaper()
                            .getParticleIcon(state);
                    cells.add(new DebrisCell(x, y, z, sprite));
                }
            }
        }
        return cells.toArray(DebrisCell[]::new);
    }

    private static final class DebrisCell {
        private final int x;
        private final int y;
        private final int z;
        private final float u0;
        private final float u1;
        private final float v0;
        private final float v1;
        private final float red;
        private final float green;
        private final float blue;

        private DebrisCell(int x, int y, int z, TextureAtlasSprite sprite) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.u0 = sprite.getU0();
            this.u1 = sprite.getU1();
            this.v0 = sprite.getV0();
            this.v1 = sprite.getV1();
            this.red = 1.0F;
            this.green = 1.0F;
            this.blue = 1.0F;
        }
    }
}
