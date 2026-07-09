package com.hbm.ntm.client.particle;

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
        Quaternionf rotation = HbmDeferredParticleRenderer.scratchRotation().rotateY(pitch).rotateZ(yaw);
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
        putFace(consumer, rotation, light, cell, originX, originY, originZ,
                x0, y0, z0, x0, y1, z0, x0, y1, z1, x0, y0, z1);
        putFace(consumer, rotation, light, cell, originX, originY, originZ,
                x1, y0, z0, x1, y0, z1, x1, y1, z1, x1, y1, z0);
        putFace(consumer, rotation, light, cell, originX, originY, originZ,
                x0, y0, z0, x0, y0, z1, x1, y0, z1, x1, y0, z0);
        putFace(consumer, rotation, light, cell, originX, originY, originZ,
                x0, y1, z0, x1, y1, z0, x1, y1, z1, x0, y1, z1);
        putFace(consumer, rotation, light, cell, originX, originY, originZ,
                x0, y0, z0, x1, y0, z0, x1, y1, z0, x0, y1, z0);
        putFace(consumer, rotation, light, cell, originX, originY, originZ,
                x0, y0, z1, x0, y1, z1, x1, y1, z1, x1, y0, z1);
    }

    private void putFace(VertexConsumer consumer, Quaternionf rotation, int light, DebrisCell cell,
            float originX, float originY, float originZ,
            float ax, float ay, float az, float bx, float by, float bz,
            float cx, float cy, float cz, float dx, float dy, float dz) {
        HbmDeferredParticleRenderer.emitLocalParticleSheetQuad(consumer, light, rotation, originX, originY, originZ,
                ax, ay, az, cell.u0, cell.v1,
                bx, by, bz, cell.u0, cell.v0,
                cx, cy, cz, cell.u1, cell.v0,
                dx, dy, dz, cell.u1, cell.v1,
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
