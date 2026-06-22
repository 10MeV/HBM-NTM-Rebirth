package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.LegacyFileCabinetBlock;
import com.hbm.ntm.blockentity.LegacyFileCabinetBlockEntity;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.ObjRenderContext;
import com.hbm.ntm.client.obj.ObjUtilityModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class LegacyFileCabinetRenderer implements BlockEntityRenderer<LegacyFileCabinetBlockEntity> {
    private static final double DRAWER_TRAVEL = 0.6875D;

    public LegacyFileCabinetRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.MACHINE;
    }

    @Override
    public void render(LegacyFileCabinetBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(legacyYaw(state)));
        renderModel(poseStack, buffer, state, texture(blockEntity.variant()), packedLight, packedOverlay,
                blockEntity.lowerExtent(partialTick), blockEntity.upperExtent(partialTick));
        poseStack.popPose();
    }

    public static void renderItemModel(PoseStack poseStack, MultiBufferSource buffer, BlockState state, int variant,
            int packedLight, int packedOverlay) {
        renderModel(poseStack, buffer, state, texture(variant), packedLight, packedOverlay, 0.0F, 0.0F);
    }

    private static void renderModel(PoseStack poseStack, MultiBufferSource buffer, BlockState state,
            ResourceLocation texture, int packedLight, int packedOverlay, float lower, float upper) {
        ObjRenderContext context = new ObjRenderContext(poseStack, buffer, state, packedLight, packedOverlay)
                .withRenderMode(LegacyTexturedRenderMode.CUTOUT_CULL);
        ObjUtilityModels.FILE_CABINET.renderPart("Cabinet", texture, context);

        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, DRAWER_TRAVEL * lower);
        ObjUtilityModels.FILE_CABINET.renderPart("LowerDrawer", texture, context);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, DRAWER_TRAVEL * upper);
        ObjUtilityModels.FILE_CABINET.renderPart("UpperDrawer", texture, context);
        poseStack.popPose();
    }

    private static ResourceLocation texture(int variant) {
        return variant == 1 ? ObjUtilityModels.FILE_CABINET_STEEL_TEXTURE : ObjUtilityModels.FILE_CABINET_TEXTURE;
    }

    private static float legacyYaw(BlockState state) {
        Direction facing = state.hasProperty(LegacyFileCabinetBlock.FACING)
                ? state.getValue(LegacyFileCabinetBlock.FACING)
                : Direction.NORTH;
        return switch (facing) {
            case SOUTH -> 0.0F;
            case WEST -> 270.0F;
            case EAST -> 90.0F;
            default -> 180.0F;
        };
    }
}
