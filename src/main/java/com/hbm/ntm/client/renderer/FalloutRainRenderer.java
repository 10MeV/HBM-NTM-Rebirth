package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.obj.LegacyTexturedQuadRenderer;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.entity.effect.FalloutRainEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.Random;

public class FalloutRainRenderer extends EntityRenderer<FalloutRainEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HbmNtm.MOD_ID, "textures/entity/fallout.png");
    private final Random random = new Random();
    private final float[] rainXCoords = new float[1024];
    private final float[] rainYCoords = new float[1024];
    private final BlockPos.MutableBlockPos lightPos = new BlockPos.MutableBlockPos();

    public FalloutRainRenderer(EntityRendererProvider.Context context) {
        super(context);
        LegacyFalloutRainRenderer.fillRainCoords(rainXCoords, rainYCoords);
    }

    @Override
    public void render(FalloutRainEntity entity, float yaw, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        Minecraft minecraft = Minecraft.getInstance();
        Camera camera = minecraft.gameRenderer.getMainCamera();
        var cameraPos = camera.getPosition();
        double cameraX = cameraPos.x;
        double cameraY = cameraPos.y;
        double cameraZ = cameraPos.z;
        double scale = entity.getScale();
        double deltaX = cameraX - entity.getX();
        double deltaY = cameraY - entity.getY();
        double deltaZ = cameraZ - entity.getZ();
        if (scale < 0.0D || deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ > scale * scale) {
            return;
        }

        Entity view = camera.getEntity();
        Level level = entity.level();
        int centerX = Mth.floor(cameraX);
        int centerY = Mth.floor(cameraY);
        int centerZ = Mth.floor(cameraZ);
        GraphicsStatus graphics = minecraft.options.graphicsMode().get();
        int range = graphics == GraphicsStatus.FANCY || graphics == GraphicsStatus.FABULOUS ? 10 : 5;
        int timer = view == null ? entity.tickCount : view.tickCount;
        double originX = Mth.lerp(partialTick, entity.xOld, entity.getX());
        double originY = Mth.lerp(partialTick, entity.yOld, entity.getY());
        double originZ = Mth.lerp(partialTick, entity.zOld, entity.getZ());
        float swayLoop = LegacyFalloutRainRenderer.swayLoop(timer, partialTick);
        VertexConsumer falloutConsumer = LegacyTexturedQuadRenderer.vertexAlphaConsumer(TEXTURE, buffer,
                LegacyTexturedRenderMode.TRANSLUCENT);
        PoseStack.Pose falloutPose = poseStack.last();

        for (int z = centerZ - range; z <= centerZ + range; z++) {
            for (int x = centerX - range; x <= centerX + range; x++) {
                int coord = LegacyFalloutRainRenderer.rainCoordIndex(x, z, centerX, centerZ);
                float rainX = LegacyFalloutRainRenderer.rainOffset(rainXCoords, coord);
                float rainZ = LegacyFalloutRainRenderer.rainOffset(rainYCoords, coord);
                int groundY = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
                int minY = LegacyFalloutRainRenderer.minHeight(centerY, range, groundY);
                int maxY = LegacyFalloutRainRenderer.maxHeight(centerY, range, groundY);
                if (minY == maxY) {
                    continue;
                }

                random.setSeed(LegacyFalloutRainRenderer.layerSeed(x, z));
                double distX = x + 0.5D - cameraX;
                double distZ = z + 0.5D - cameraZ;
                float fallVariation = LegacyFalloutRainRenderer.fallVariation(random);
                float swayVariation = LegacyFalloutRainRenderer.swayVariation(random);
                float alpha = LegacyFalloutRainRenderer.alpha(distX, distZ, range);
                int lightY = LegacyFalloutRainRenderer.sampleLightY(groundY, centerY);
                int light = LegacyFalloutRainRenderer.blendLegacyLight(
                        LevelRenderer.getLightColor(level, lightPos.set(x, lightY, z)));
                LegacyFalloutRainRenderer.renderColumn(falloutConsumer, falloutPose, x, z, minY, maxY, rainX, rainZ,
                        fallVariation, swayVariation, swayLoop, alpha, light, originX, originY, originZ);
            }
        }
    }

    @Override
    public ResourceLocation getTextureLocation(FalloutRainEntity entity) {
        return TEXTURE;
    }
}
