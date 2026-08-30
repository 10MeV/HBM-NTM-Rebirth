package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.blockentity.IndustrialGeneratorBlockEntity;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjMachineModels;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/** Exact static Body/Rotor portion of 1.7.10 RenderIGenerator. */
public class IndustrialGeneratorRenderer implements BlockEntityRenderer<IndustrialGeneratorBlockEntity> {
    private static final LegacyWavefrontModel MODEL = ObjMachineModels.IGEN;
    private static final LegacyWavefrontModel.SelectionHandle BODY = MODEL.prepareRenderOnlyInCallOrder("Body");
    private static final LegacyWavefrontModel.SelectionHandle ROTOR = MODEL.prepareRenderOnlyInCallOrder("Rotor");

    public IndustrialGeneratorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRender(IndustrialGeneratorBlockEntity generator, Vec3 cameraPos) {
        return BlockEntityRenderer.super.shouldRender(generator, cameraPos)
                && LegacyBlockEntityRenderCulling.shouldRenderMachine(generator, getViewDistance());
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.machine();
    }

    @Override
    public void render(IndustrialGeneratorBlockEntity generator, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(generator, getViewDistance())) {
            return;
        }
        BlockState state = generator.getBlockState();
        Direction facing = state.getValue(HorizontalMachineBlock.FACING);
        int light = packedLight;
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        LegacyPoseRotations.rotateYDegrees(poseStack, southZeroRotation(facing));
        poseStack.scale(1.0F / 6.0F, 1.0F / 6.0F, 1.0F / 6.0F);
        poseStack.translate(0.0D, 0.0D, -0.5D);
        try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(generator)) {
            MODEL.renderOnlyInCallOrder(ObjMachineModels.IGEN_TEXTURE, poseStack, buffer, light, packedOverlay, BODY);
            MODEL.renderOnlyInCallOrder(ObjMachineModels.IGEN_TEXTURE, poseStack, buffer, light, packedOverlay, ROTOR);
        }
        poseStack.popPose();
    }

    private static float southZeroRotation(Direction facing) {
        return switch (facing) {
            case SOUTH -> 0.0F;
            case EAST -> 90.0F;
            case NORTH -> 180.0F;
            case WEST -> 270.0F;
            default -> 0.0F;
        };
    }
}
