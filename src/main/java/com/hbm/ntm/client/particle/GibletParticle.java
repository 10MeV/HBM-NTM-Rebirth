package com.hbm.ntm.client.particle;

import com.hbm.ntm.particle.ParticleUtil;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GibletParticle extends TextureSheetParticle {
    public static final int TYPE_MEAT = ParticleUtil.GIBLET_MEAT;
    public static final int TYPE_SLIME = ParticleUtil.GIBLET_SLIME;
    public static final int TYPE_METAL = ParticleUtil.GIBLET_METAL;

    private final SpriteSet sprites;
    private final int gibType;
    private float rotationPitch;
    private float prevRotationPitch;
    private float rotationYaw;
    private float prevRotationYaw;
    private final float momentumPitch;
    private final float momentumYaw;

    private GibletParticle(ClientLevel level, double x, double y, double z, double motionX, double motionY, double motionZ,
            int gibType, SpriteSet sprites) {
        super(level, x, y, z, motionX, motionY, motionZ);
        this.sprites = sprites;
        this.gibType = gibType;
        this.xd = motionX;
        this.yd = motionY;
        this.zd = motionZ;
        this.lifetime = 140 + this.random.nextInt(20);
        this.gravity = gibType == TYPE_METAL ? 4.0F : 2.0F;
        this.quadSize = 0.1F;
        this.hasPhysics = true;
        this.momentumYaw = (float) this.random.nextGaussian() * 15.0F;
        this.momentumPitch = (float) this.random.nextGaussian() * 15.0F;
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.prevRotationPitch = this.rotationPitch;
        this.prevRotationYaw = this.rotationYaw;

        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        this.yd -= 0.04D * this.gravity;
        this.move(this.xd, this.yd, this.zd);
        if (!this.removed) {
            this.xd *= 0.98D;
            this.yd *= 0.98D;
            this.zd *= 0.98D;
        }

        if (!this.onGround) {
            this.rotationPitch += this.momentumPitch;
            this.rotationYaw += this.momentumYaw;
            if (this.gibType != ParticleUtil.GIBLET_METAL) {
                BlockState state = this.gibType == ParticleUtil.GIBLET_SLIME
                        ? Blocks.MELON.defaultBlockState()
                        : Blocks.REDSTONE_BLOCK.defaultBlockState();
                TerrainParticle trail = new TerrainParticle(this.level, this.x, this.y, this.z, 0.0D, 0.0D, 0.0D, state);
                trail.setLifetime(20 + this.random.nextInt(20));
                Minecraft.getInstance().particleEngine.add(trail);
            }
        }

        this.prevRotationPitch = unwrapPrevious(this.prevRotationPitch, this.rotationPitch);
        this.prevRotationYaw = unwrapPrevious(this.prevRotationYaw, this.rotationYaw);
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void render(VertexConsumer consumer, Camera camera, float partialTick) {
        this.oRoll = this.roll = Mth.lerp(partialTick, this.prevRotationYaw, this.rotationYaw) * Mth.DEG_TO_RAD;
        super.render(consumer, camera, partialTick);
    }

    @Override
    public int getLightColor(float partialTick) {
        return super.getLightColor(partialTick);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    private static float unwrapPrevious(float previous, float current) {
        if (Math.abs(previous - current) > 180.0F) {
            return previous < current ? previous + 360.0F : previous - 360.0F;
        }
        return previous;
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        private final int gibType;

        public Provider(SpriteSet sprites, int gibType) {
            this.sprites = sprites;
            this.gibType = gibType;
        }

        @Override
        public GibletParticle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed) {
            return new GibletParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, gibType, sprites);
        }
    }
}
