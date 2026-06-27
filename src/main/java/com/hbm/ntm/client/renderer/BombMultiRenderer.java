package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.BombMultiBlock;
import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.blockentity.BombMultiBlockEntity;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.ObjNukeModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class BombMultiRenderer implements BlockEntityRenderer<BombMultiBlockEntity> {
    private static final ResourceLocation BOMB_MULTI_TEXTURE = ObjNukeModels.texture("bomb_multi_legacy");

    public BombMultiRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(BombMultiBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();
        if (!(state.getBlock() instanceof BombMultiBlock)) {
            return;
        }
        int modelLight = LegacyRenderLighting.resolveBlockEntityLight(blockEntity, packedLight);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(legacyYaw(state)));
        renderModel(poseStack, buffer, modelLight, packedOverlay);
        poseStack.popPose();
    }

    public static void renderModel(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ObjNukeModels.BOMB_MULTI_LEGACY.renderAll(BOMB_MULTI_TEXTURE, poseStack, buffer, packedLight, packedOverlay,
                LegacyTexturedRenderMode.CUTOUT_NO_CULL);
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
