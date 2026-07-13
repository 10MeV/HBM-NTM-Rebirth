package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.LandmineBlock;
import com.hbm.ntm.blockentity.LandmineBlockEntity;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjBombModels;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.Vec3;

public class LandmineRenderer implements BlockEntityRenderer<LandmineBlockEntity> {
    public LandmineRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(LandmineBlockEntity mine, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!(mine.getBlockState().getBlock() instanceof LandmineBlock block)) {
            return;
        }

        int modelLight = LegacyRenderLighting.resolveBlockEntityLight(mine, packedLight);
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        LegacyPoseRotations.rotateYDegrees(poseStack, 180.0F);
        switch (block.kind()) {
            case AP -> renderApMine(mine, poseStack, buffer, modelLight, packedOverlay, ObjBombModels.MINE_AP,
                    apMineTexture(mine));
            case HE -> {
                LegacyPoseRotations.rotateYDegrees(poseStack, -90.0F);
                renderModel(ObjBombModels.MINE_MARELET, ObjBombModels.texture("mine_marelet"), poseStack, buffer,
                        modelLight, packedOverlay);
            }
            case SHRAP -> renderApMine(mine, poseStack, buffer, modelLight, packedOverlay, ObjBombModels.MINE_AP,
                    ObjBombModels.MINE_SHRAP_TEXTURE);
            case FAT -> {
                poseStack.scale(0.25F, 0.25F, 0.25F);
                renderModel(ObjBombModels.MINE_FAT, ObjBombModels.rootTexture("mine_fat"), poseStack, buffer,
                        modelLight, packedOverlay);
            }
        }
        poseStack.popPose();
    }

    private static void renderApMine(LandmineBlockEntity mine, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, LegacyWavefrontModel model, ResourceLocation texture) {
        poseStack.scale(0.375F, 0.375F, 0.375F);
        poseStack.translate(0.0D, -0.0625D * 3.5D, 0.0D);
        renderModel(model, texture, poseStack, buffer, packedLight, packedOverlay);
    }

    private static void renderModel(LegacyWavefrontModel model, ResourceLocation texture, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        model.renderAll(texture, poseStack, buffer, packedLight, packedOverlay, LegacyTexturedRenderMode.CUTOUT_NO_CULL);
    }

    private static ResourceLocation apMineTexture(LandmineBlockEntity mine) {
        Level level = mine.getLevel();
        BlockPos pos = mine.getBlockPos();
        if (level == null || level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, pos.getX(),
                pos.getZ()) > pos.getY() + 2) {
            return ObjBombModels.MINE_AP_STONE_TEXTURE;
        }
        Biome biome = level.getBiome(pos).value();
        if (biome.getPrecipitationAt(pos) == Biome.Precipitation.SNOW) {
            return ObjBombModels.MINE_AP_SNOW_TEXTURE;
        }
        Biome.ClimateSettings climate = biome.getModifiedClimateSettings();
        return climate.temperature() >= 1.5F && climate.downfall() <= 0.1F
                ? ObjBombModels.MINE_AP_DESERT_TEXTURE
                : ObjBombModels.MINE_AP_GRASS_TEXTURE;
    }

    @Override
    public boolean shouldRender(LandmineBlockEntity mine, Vec3 cameraPos) {
        return BlockEntityRenderer.super.shouldRender(mine, cameraPos);
    }
}
