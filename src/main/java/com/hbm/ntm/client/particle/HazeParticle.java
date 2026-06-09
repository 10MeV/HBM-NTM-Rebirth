package com.hbm.ntm.client.particle;

import com.hbm.ntm.client.renderer.HbmClientRenderUtil;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Random;

@OnlyIn(Dist.CLIENT)
public class HazeParticle extends TextureSheetParticle {
    private static SpriteSet sharedSprites;
    private static final ParticleRenderType RENDER_TYPE = new ParticleRenderType() {
        @Override
        public void begin(BufferBuilder builder, TextureManager textureManager) {
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            RenderSystem.setShader(GameRenderer::getParticleShader);
            HbmClientRenderUtil.bindParticleAtlas();
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public void end(Tesselator tesselator) {
            tesselator.end();
            RenderSystem.defaultBlendFunc();
            RenderSystem.depthMask(true);
        }

        @Override
        public String toString() {
            return "HBM_HAZE";
        }
    };

    private final SpriteSet sprites;

    private HazeParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites) {
        super(level, x, y, z);
        this.sprites = sprites;
        this.lifetime = 600 + this.random.nextInt(100);
        this.rCol = this.gCol = this.bCol = 1.0F;
        this.quadSize = 10.0F;
        this.hasPhysics = false;
        this.setSpriteFromAge(sprites);
    }

    public static HazeParticle create(ClientLevel level, double x, double y, double z) {
        return sharedSprites == null ? null : new HazeParticle(level, x, y, z, sharedSprites);
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
        this.xd *= 0.96D;
        this.yd *= 0.96D;
        this.zd *= 0.96D;
        if (this.onGround) {
            this.xd *= 0.7D;
            this.zd *= 0.7D;
        }
        this.move(this.xd, this.yd, this.zd);
        int x = Mth.floor(this.x) + this.random.nextInt(15) - 7;
        int z = Mth.floor(this.z) + this.random.nextInt(15) - 7;
        int y = this.level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, new BlockPos(x, 0, z)).getY();
        this.level.addParticle(net.minecraft.core.particles.ParticleTypes.LAVA,
                x + this.random.nextDouble(), y + 0.1D, z + this.random.nextDouble(), 0.0D, 0.0D, 0.0D);
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void render(VertexConsumer consumer, Camera camera, float partialTick) {
        float alpha = (float) Math.sin(this.age * Math.PI / 400.0D) * 0.025F;
        if (alpha <= 0.0F) {
            return;
        }
        Vec3 cameraPos = camera.getPosition();
        Quaternionf rotation = camera.rotation();
        Random fixed = new Random(50L);
        float cumulativeX = 0.0F;
        float cumulativeY = 0.0F;
        float cumulativeZ = 0.0F;
        for (int i = 0; i < 25; i++) {
            cumulativeX += (float) (fixed.nextGaussian() * 2.5D);
            cumulativeY += (float) (fixed.nextGaussian() * 0.15D);
            cumulativeZ += (float) (fixed.nextGaussian() * 2.5D);
            float size = (float) ((fixed.nextDouble() * 0.25D + 0.75D) * this.quadSize);
            float x = (float) (Mth.lerp(partialTick, this.xo, this.x) + cumulativeX + fixed.nextGaussian() * 0.5D - cameraPos.x());
            float y = (float) (Mth.lerp(partialTick, this.yo, this.y) + cumulativeY + fixed.nextGaussian() * 0.5D - cameraPos.y());
            float z = (float) (Mth.lerp(partialTick, this.zo, this.z) + cumulativeZ + fixed.nextGaussian() * 0.5D - cameraPos.z());
            Vector3f[] corners = new Vector3f[] {
                    new Vector3f(-size, -size, 0.0F),
                    new Vector3f(-size, size, 0.0F),
                    new Vector3f(size, size, 0.0F),
                    new Vector3f(size, -size, 0.0F)
            };
            for (Vector3f corner : corners) {
                corner.rotate(rotation).add(x, y, z);
            }
            consumer.vertex(corners[0].x(), corners[0].y(), corners[0].z()).uv(getU1(), getV1()).color(this.rCol, this.gCol, this.bCol, alpha).uv2(LightTexture.FULL_BRIGHT).endVertex();
            consumer.vertex(corners[1].x(), corners[1].y(), corners[1].z()).uv(getU1(), getV0()).color(this.rCol, this.gCol, this.bCol, alpha).uv2(LightTexture.FULL_BRIGHT).endVertex();
            consumer.vertex(corners[2].x(), corners[2].y(), corners[2].z()).uv(getU0(), getV0()).color(this.rCol, this.gCol, this.bCol, alpha).uv2(LightTexture.FULL_BRIGHT).endVertex();
            consumer.vertex(corners[3].x(), corners[3].y(), corners[3].z()).uv(getU0(), getV1()).color(this.rCol, this.gCol, this.bCol, alpha).uv2(LightTexture.FULL_BRIGHT).endVertex();
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return RENDER_TYPE;
    }

    @Override
    public boolean shouldCull() {
        return false;
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        public Provider(SpriteSet sprites) {
            sharedSprites = sprites;
        }

        @Override
        public HazeParticle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed) {
            return create(level, x, y, z);
        }
    }
}
