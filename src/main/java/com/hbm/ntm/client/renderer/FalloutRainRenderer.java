package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.entity.effect.FalloutRainEntity;
import com.mojang.blaze3d.vertex.PoseStack;
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

    public FalloutRainRenderer(EntityRendererProvider.Context context) {
        super(context);
        LegacyFalloutRainRenderer.fillRainCoords(rainXCoords, rainYCoords);
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
        double camZ = camera.getPosition().z;
        double originX = Mth.lerp(partialTick, entity.xOld, entity.getX());
        double originY = Mth.lerp(partialTick, entity.yOld, entity.getY());
        double originZ = Mth.lerp(partialTick, entity.zOld, entity.getZ());

        for (int z = centerZ - range; z <= centerZ + range; z++) {
            for (int x = centerX - range; x <= centerX + range; x++) {
                int coord = LegacyFalloutRainRenderer.rainCoordIndex(x, z, centerX, centerZ);
                float rainX = LegacyFalloutRainRenderer.rainOffset(rainXCoords, coord);
                float rainZ = LegacyFalloutRainRenderer.rainOffset(rainYCoords, coord);
                int groundY = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
                LegacyFalloutRainRenderer.HeightSpan height = LegacyFalloutRainRenderer.heightSpan(centerY, range, groundY);
                if (height.minY() == height.maxY()) {
                    continue;
                }

                random.setSeed(LegacyFalloutRainRenderer.layerSeed(x, z));
                double distX = x + 0.5D - camX;
                double distZ = z + 0.5D - camZ;
                LegacyFalloutRainRenderer.ColumnStyle style =
                        LegacyFalloutRainRenderer.columnStyle(random, timer, partialTick, distX, distZ, range);
                int lightY = LegacyFalloutRainRenderer.sampleLightY(groundY, centerY);
                int light = LegacyFalloutRainRenderer.blendLegacyLight(
                        LevelRenderer.getLightColor(level, new BlockPos(x, lightY, z)));
                LegacyFalloutRainRenderer.renderColumn(TEXTURE, poseStack, buffer, x, z, height, rainX, rainZ,
                        style, light, originX, originY, originZ);
            }
        }
    }

    @Override
    public ResourceLocation getTextureLocation(FalloutRainEntity entity) {
        return TEXTURE;
    }
}
