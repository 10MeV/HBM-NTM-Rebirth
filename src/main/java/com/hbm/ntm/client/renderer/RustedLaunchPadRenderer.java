package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.RustedLaunchPadBlock;
import com.hbm.ntm.blockentity.RustedLaunchPadBlockEntity;
import com.hbm.ntm.client.obj.ObjLaunchModels;
import com.hbm.ntm.client.obj.ObjMissilePartModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;

public class RustedLaunchPadRenderer implements BlockEntityRenderer<RustedLaunchPadBlockEntity> {
    public RustedLaunchPadRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(RustedLaunchPadBlockEntity launchPad, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Direction facing = launchPad.getBlockState().hasProperty(RustedLaunchPadBlock.FACING)
                ? launchPad.getBlockState().getValue(RustedLaunchPadBlock.FACING)
                : Direction.NORTH;

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(yRotation(facing)));
        ObjLaunchModels.MISSILE_PAD.renderAll(ObjLaunchModels.MISSILE_PAD_RUSTED_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        if (launchPad.isMissileLoaded()) {
            poseStack.pushPose();
            poseStack.translate(0.0D, 1.0D, 0.0D);
            ObjMissilePartModels.MISSILE_ATLAS.renderAll(ObjMissilePartModels.MISSILE_ATLAS_DOOMSDAY_RUSTED_TEXTURE,
                    poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
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
