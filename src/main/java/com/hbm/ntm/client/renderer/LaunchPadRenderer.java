package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.LaunchPadBlock;
import com.hbm.ntm.blockentity.LaunchPadBlockEntity;
import com.hbm.ntm.client.obj.ObjLaunchModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

public class LaunchPadRenderer implements BlockEntityRenderer<LaunchPadBlockEntity> {
    public LaunchPadRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(LaunchPadBlockEntity launchPad, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Direction facing = launchPad.getBlockState().hasProperty(LaunchPadBlock.FACING)
                ? launchPad.getBlockState().getValue(LaunchPadBlock.FACING)
                : Direction.NORTH;

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(yRotation(facing)));
        ObjLaunchModels.MISSILE_PAD.renderAll(ObjLaunchModels.MISSILE_PAD_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);

        ItemStack missile = launchPad.getItems().getStackInSlot(LaunchPadBlockEntity.SLOT_MISSILE);
        if (!missile.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.0D, 1.0D, 0.0D);
            MissileItemRenderer.renderRawMissile(missile, poseStack, buffer, packedLight, packedOverlay);
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private static float yRotation(Direction facing) {
        return switch (facing) {
            case EAST -> 90.0F;
            case SOUTH -> 180.0F;
            case WEST -> 270.0F;
            default -> 0.0F;
        };
    }
}
