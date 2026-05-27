package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.entity.effect.FalloutRainEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.Random;

public class FalloutRainRenderer extends EntityRenderer<FalloutRainEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HbmNtm.MOD_ID, "textures/entity/fallout.png");
    private final Random random = new Random();
    private final float[] rainXCoords = new float[1024];
    private final float[] rainYCoords = new float[1024];

    public FalloutRainRenderer(EntityRendererProvider.Context context) {
        super(context);
        for (int i = 0; i < 32; i++) {
            for (int j = 0; j < 32; j++) {
                float x = j - 16.0F;
                float z = i - 16.0F;
                float length = Mth.sqrt(x * x + z * z);
                if (length == 0.0F) {
                    rainXCoords[i << 5 | j] = 0.0F;
                    rainYCoords[i << 5 | j] = 0.0F;
                } else {
                    rainXCoords[i << 5 | j] = -z / length;
                    rainYCoords[i << 5 | j] = x / length;
                }
            }
        }
    }

    @Override
    public void render(FalloutRainEntity entity, float yaw, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        Minecraft minecraft = Minecraft.getInstance();
        Camera camera = minecraft.gameRenderer.getMainCamera();
        if (camera.getPosition().distanceTo(entity.position()) > entity.getScale()) {
            return;
        }

        Entity view = camera.getEntity();
        Level level = entity.level();
        int centerX = Mth.floor(camera.getPosition().x);
        int centerY = Mth.floor(camera.getPosition().y);
        int centerZ = Mth.floor(camera.getPosition().z);
        GraphicsStatus graphics = minecraft.options.graphicsMode().get();
        int range = graphics == GraphicsStatus.FANCY || graphics == GraphicsStatus.FABULOUS ? 10 : 5;
        int timer = view == null ? entity.tickCount : view.tickCount;
        double camX = camera.getPosition().x;
        double camY = camera.getPosition().y;
        double camZ = camera.getPosition().z;
        double originX = Mth.lerp(partialTick, entity.xOld, entity.getX());
        double originY = Mth.lerp(partialTick, entity.yOld, entity.getY());
        double originZ = Mth.lerp(partialTick, entity.zOld, entity.getZ());

        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
        Matrix4f pose = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();

        for (int z = centerZ - range; z <= centerZ + range; z++) {
            for (int x = centerX - range; x <= centerX + range; x++) {
                int coord = (z - centerZ + 16) * 32 + x - centerX + 16;
                float rainX = rainXCoords[coord] * 0.5F;
                float rainZ = rainYCoords[coord] * 0.5F;
                int groundY = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
                int minY = Math.max(centerY - range, groundY);
                int maxY = Math.max(centerY + range, groundY);
                if (minY == maxY) {
                    continue;
                }

                random.setSeed(x * x * 3121 + x * 45_238_971 ^ z * z * 418_711 + z * 13_761);
                float fallLoop = ((timer & 511) + partialTick) / 512.0F;
                float fallVariation = 0.4F + random.nextFloat() * 0.2F;
                float swayVariation = random.nextFloat();
                double distX = x + 0.5D - camX;
                double distZ = z + 0.5D - camZ;
                float distanceMod = Mth.sqrt((float) (distX * distX + distZ * distZ)) / range;
                float alpha = (1.0F - distanceMod * distanceMod) * 0.3F + 0.5F;

                putVertex(consumer, pose, normal, (float) (x - rainX + 0.5D - originX), (float) (minY - originY),
                        (float) (z - rainZ + 0.5D - originZ), fallVariation,
                        minY / 4.0F + fallLoop + swayVariation, alpha);
                putVertex(consumer, pose, normal, (float) (x + rainX + 0.5D - originX), (float) (minY - originY),
                        (float) (z + rainZ + 0.5D - originZ), 1.0F + fallVariation,
                        minY / 4.0F + fallLoop + swayVariation, alpha);
                putVertex(consumer, pose, normal, (float) (x + rainX + 0.5D - originX), (float) (maxY - originY),
                        (float) (z + rainZ + 0.5D - originZ), 1.0F + fallVariation,
                        maxY / 4.0F + fallLoop + swayVariation, alpha);
                putVertex(consumer, pose, normal, (float) (x - rainX + 0.5D - originX), (float) (maxY - originY),
                        (float) (z - rainZ + 0.5D - originZ), fallVariation,
                        maxY / 4.0F + fallLoop + swayVariation, alpha);
            }
        }
    }

    private static void putVertex(VertexConsumer consumer, Matrix4f pose, Matrix3f normal, float x, float y,
            float z, float u, float v, float alpha) {
        consumer.vertex(pose, x, y, z)
                .color(1.0F, 1.0F, 1.0F, alpha)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT)
                .normal(normal, 0.0F, 1.0F, 0.0F)
                .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(FalloutRainEntity entity) {
        return TEXTURE;
    }
}
