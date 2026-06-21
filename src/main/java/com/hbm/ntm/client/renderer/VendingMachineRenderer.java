package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.block.VendingMachineBlock;
import com.hbm.ntm.blockentity.VendingMachineBlockEntity;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjMachineModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class VendingMachineRenderer implements BlockEntityRenderer<VendingMachineBlockEntity> {
    public static final ResourceLocation TEXTURE = ObjMachineModels.VENDING_MACHINE_TEXTURE;
    public static final LegacyWavefrontModel MODEL = ObjMachineModels.VENDING_MACHINE;

    public VendingMachineRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(VendingMachineBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.MACHINE;
    }

    @Override
    public void render(VendingMachineBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        render(blockEntity.getBlockState(), poseStack, buffer,
                LegacyRenderLighting.resolveMultiblockLight(blockEntity, packedLight), packedOverlay);
    }

    public static void render(BlockState state, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay) {
        Direction facing = state.hasProperty(HorizontalMachineBlock.FACING)
                ? state.getValue(HorizontalMachineBlock.FACING)
                : Direction.SOUTH;
        int variant = state.hasProperty(VendingMachineBlock.VARIANT) ? state.getValue(VendingMachineBlock.VARIANT) : 0;

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(legacyYaw(facing)));
        MODEL.renderPart(variant == 0 ? "Soda" : "Obamna", TEXTURE, poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static float legacyYaw(Direction facing) {
        return switch (facing) {
            case NORTH -> 90.0F;
            case WEST -> 180.0F;
            case SOUTH -> 270.0F;
            default -> 0.0F;
        };
    }
}
