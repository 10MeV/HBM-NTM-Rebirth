package com.hbm.ntm.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.concurrent.atomic.AtomicInteger;

@OnlyIn(Dist.CLIENT)
public class AshesParticle extends TextureSheetParticle implements HbmDeferredParticleRenderer.DeferredParticle {
    private static final AtomicInteger NEXT_VISUAL_ID = new AtomicInteger();
    private static SpriteSet sharedSprites;

    private final SpriteSet sprites;
    private final float baseScale;
    private final float rollSpeed;
    private final boolean groundSmokeSeed;

    private AshesParticle(ClientLevel level, double x, double y, double z, float scale, SpriteSet sprites) {
        super(level, x, y, z);
        this.sprites = sprites;
        int visualId = NEXT_VISUAL_ID.incrementAndGet();
        this.lifetime = 1200 + random.nextInt(20);
        this.baseScale = scale * 0.9F + random.nextFloat() * 0.2F;
        float color = random.nextFloat() * 0.1F + 0.1F;
        this.rCol = color;
        this.gCol = color;
        this.bCol = color;
        this.alpha = 1.0F;
        this.quadSize = this.baseScale;
        this.gravity = 0.01F;
        this.hasPhysics = true;
        this.rollSpeed = (visualId % 2 - 0.5F) * 2.0F * Mth.DEG_TO_RAD;
        this.groundSmokeSeed = visualId % 5 == 0;
        this.setSpriteFromAge(sprites);
    }

    public static AshesParticle create(ClientLevel level, double x, double y, double z, float scale) {
        return sharedSprites == null ? null : new AshesParticle(level, x, y, z, scale, sharedSprites);
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
        this.yd -= this.gravity;
        this.oRoll = this.roll;
        if (!this.onGround) {
            this.roll += this.rollSpeed;
        }
        this.xd *= 0.95D;
        this.yd *= 0.99D;
        this.zd *= 0.95D;
        boolean wasOnGround = this.onGround;
        this.move(this.xd, this.yd, this.zd);
        if (!wasOnGround && this.onGround) {
            this.oRoll = this.roll = random.nextFloat() * Mth.TWO_PI;
        }
        if (this.groundSmokeSeed && this.onGround && random.nextInt(15) == 0) {
            this.level.addParticle(ParticleTypes.SMOKE, this.x, this.y + 0.125D, this.z, 0.0D, 0.05D, 0.0D);
        }
        float timeLeft = this.lifetime - this.age;
        this.alpha = timeLeft < 40.0F ? Math.max(timeLeft / 40.0F, 0.0F) : 1.0F;
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void render(VertexConsumer consumer, Camera camera, float partialTick) {
        HbmDeferredParticleRenderer.enqueue(this, camera, this.x, this.y, this.z);
    }

    @Override
    public void renderDeferred(MultiBufferSource.BufferSource buffer, Camera camera, float partialTick) {
        VertexConsumer consumer = buffer.getBuffer(HbmDeferredParticleRenderer.particleSheetDepthWrite());
        if (this.onGround) {
            renderGroundQuad(consumer, camera, partialTick);
            return;
        }
        HbmDeferredParticleRenderer.emitTextureSheetParticleQuad(consumer, camera, partialTick,
                this.xo, this.yo, this.zo, this.x, this.y, this.z,
                this.oRoll, this.roll, this.getQuadSize(partialTick),
                this.getU0(), this.getU1(), this.getV0(), this.getV1(),
                this.rCol, this.gCol, this.bCol, this.alpha, this.getLightColor(partialTick));
    }

    private void renderGroundQuad(VertexConsumer consumer, Camera camera, float partialTick) {
        Vec3 cameraPos = camera.getPosition();
        float x = (float) (Mth.lerp(partialTick, this.xo, this.x) - cameraPos.x());
        float y = (float) (Mth.lerp(partialTick, this.yo, this.y) - cameraPos.y()) + 0.05F;
        float z = (float) (Mth.lerp(partialTick, this.zo, this.z) - cameraPos.z());
        float angle = Mth.lerp(partialTick, this.oRoll, this.roll);
        Quaternionf rotation = new Quaternionf().rotateY(angle);
        Vector3f[] corners = new Vector3f[]{
                new Vector3f(this.baseScale, 0.0F, this.baseScale),
                new Vector3f(this.baseScale, 0.0F, -this.baseScale),
                new Vector3f(-this.baseScale, 0.0F, -this.baseScale),
                new Vector3f(-this.baseScale, 0.0F, this.baseScale)
        };
        for (Vector3f corner : corners) {
            corner.rotate(rotation).add(x, y, z);
        }
        int light = getLightColor(partialTick);
        HbmDeferredParticleRenderer.emitParticleSheetQuad(consumer, light,
                corners[0], getU1(), getV1(),
                corners[1], getU1(), getV0(),
                corners[2], getU0(), getV0(),
                corners[3], getU0(), getV1(),
                rCol, gCol, bCol, alpha);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return HbmDeferredParticleRenderer.DEFERRED_RENDER_TYPE;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
            sharedSprites = sprites;
        }

        @Override
        public AshesParticle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed) {
            return new AshesParticle(level, x, y, z, 1.0F, sprites);
        }
    }
}
