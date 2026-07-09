package com.hbm.ntm.client.particle;

import com.hbm.ntm.client.obj.LegacyLineRenderer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TauSparkParticle extends Particle {
    private static final int MAX_STEPS = 6;

    private static final ParticleRenderType RENDER_TYPE = new ParticleRenderType() {
        @Override
        public void begin(BufferBuilder builder, TextureManager textureManager) {
            RenderSystem.depthMask(false);
            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.lineWidth(3.0F);
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            builder.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        }

        @Override
        public void end(Tesselator tesselator) {
            tesselator.end();
            RenderSystem.lineWidth(1.0F);
            RenderSystem.enableCull();
            RenderSystem.depthMask(true);
        }

        @Override
        public String toString() {
            return "HBM_TAU_SPARK";
        }
    };

    private final double[] stepDeltas = new double[MAX_STEPS * 3];
    private final int threshold;
    private int stepStart;
    private int stepCount;

    public TauSparkParticle(ClientLevel level, double x, double y, double z, double motionX, double motionY, double motionZ, boolean small) {
        super(level, x, y, z);
        this.xd = motionX;
        this.yd = small ? -Math.abs(motionY) : motionY;
        this.zd = motionZ;
        this.threshold = small ? 3 : 4 + this.random.nextInt(3);
        this.lifetime = small ? 2 + this.random.nextInt(3) : 20 + this.random.nextInt(10);
        this.gravity = 0.5F;
        this.hasPhysics = true;
        this.pushStep(this.xd, this.yd, this.zd);
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
        this.pushStep(this.xd, this.yd, this.zd);
        this.yd -= 0.04D * this.gravity;
        double previousY = this.yd;
        this.move(this.xd, this.yd, this.zd);
        if (this.onGround) {
            this.onGround = false;
            this.yd = -previousY * 0.8D;
        }
    }

    @Override
    public void render(VertexConsumer consumer, Camera camera, float partialTick) {
        if (this.stepCount < 2) {
            return;
        }
        Vec3 cameraPos = camera.getPosition();
        double currentX = Mth.lerp(partialTick, this.xo, this.x) - cameraPos.x();
        double currentY = Mth.lerp(partialTick, this.yo, this.y) - cameraPos.y();
        double currentZ = Mth.lerp(partialTick, this.zo, this.z) - cameraPos.z();
        LegacyLineRenderer.pointPositionColorIdentity(consumer, currentX, currentY, currentZ, 0xFFFFFF, 255);
        for (int offset = this.stepCount - 1; offset >= 1; offset--) {
            int index = this.stepIndex(offset);
            int base = index * 3;
            currentX -= this.stepDeltas[base];
            currentY -= this.stepDeltas[base + 1];
            currentZ -= this.stepDeltas[base + 2];
            LegacyLineRenderer.pointPositionColorIdentity(consumer, currentX, currentY, currentZ, 0xFFFFFF, 255);
        }
    }

    private void pushStep(double x, double y, double z) {
        int index;
        if (this.stepCount < this.threshold) {
            index = this.stepIndex(this.stepCount);
            this.stepCount++;
        } else {
            index = this.stepStart;
            this.stepStart = (this.stepStart + 1) % MAX_STEPS;
        }
        int base = index * 3;
        this.stepDeltas[base] = x;
        this.stepDeltas[base + 1] = y;
        this.stepDeltas[base + 2] = z;
    }

    private int stepIndex(int offset) {
        return (this.stepStart + offset) % MAX_STEPS;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return RENDER_TYPE;
    }

    @Override
    public boolean shouldCull() {
        return false;
    }
}
