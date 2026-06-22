package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.BombMultiBlock;
import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.blockentity.BombMultiBlockEntity;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.ObjNukeModels;
import com.hbm.ntm.client.obj.ObjRenderContext;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class BombMultiRenderer implements BlockEntityRenderer<BombMultiBlockEntity> {
    public BombMultiRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(BombMultiBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();
        if (!(state.getBlock() instanceof BombMultiBlock)) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(legacyYaw(state)));
        renderModel(poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    public static void renderModel(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ObjRenderContext context = new ObjRenderContext(poseStack, buffer, null, packedLight, packedOverlay)
                .withRenderMode(LegacyTexturedRenderMode.CUTOUT_NO_CULL);
        ObjNukeModels.BOMB_MULTI_LEGACY.renderAll(ObjNukeModels.texture("bomb_multi_legacy"), context);
    }

    public static void applyLegacyItemCommon(PoseStack poseStack) {
        poseStack.translate(0.75D, 0.0D, 0.0D);
        poseStack.scale(3.0F, 3.0F, 3.0F);
        poseStack.translate(0.0D, 0.5D, 0.0D);
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
    }

    private static float legacyYaw(BlockState state) {
        Direction facing = state.hasProperty(HorizontalMachineBlock.FACING)
                ? state.getValue(HorizontalMachineBlock.FACING)
                : Direction.SOUTH;
        return switch (facing) {
            case NORTH -> 90.0F;
            case EAST -> 180.0F;
            case SOUTH -> 270.0F;
            case WEST -> 0.0F;
            default -> 270.0F;
        };
    }

    @Override
    public boolean shouldRenderOffScreen(BombMultiBlockEntity blockEntity) {
        return false;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.MACHINE;
    }
}
