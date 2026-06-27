package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.GeigerBlock;
import com.hbm.ntm.blockentity.GeigerBlockEntity;
import com.hbm.ntm.client.obj.ObjUtilityModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.core.Direction;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class GeigerRenderer implements BlockEntityRenderer<GeigerBlockEntity> {
    public GeigerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(GeigerBlockEntity geiger, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Direction facing = geiger.getBlockState().getValue(GeigerBlock.FACING);
        int modelLight = LegacyRenderLighting.resolveBlockEntityLight(geiger, packedLight);
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation(facing)));
        ObjUtilityModels.GEIGER_COUNTER.renderAll(ObjUtilityModels.GEIGER_TEXTURE,
                poseStack, buffer, modelLight, packedOverlay);
        poseStack.popPose();
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.MACHINE;
    }

    private static float rotation(Direction facing) {
        return switch (facing) {
            case WEST -> 90.0F;
            case SOUTH -> 180.0F;
            case EAST -> 270.0F;
            default -> 0.0F;
        };
    }
}
