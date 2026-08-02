package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.blockentity.SatelliteLinkBlockEntity;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjMachineModels;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/** OBJ renderer matching 1.7.10 RenderSatLink's Base/Rotor/Dish call order. */
public class SatelliteLinkRenderer implements BlockEntityRenderer<SatelliteLinkBlockEntity> {
    private static final LegacyWavefrontModel MODEL = ObjMachineModels.SATLINK;
    private static final LegacyWavefrontModel.SelectionHandle BASE = MODEL.prepareRenderOnlyInCallOrder("Base");
    private static final LegacyWavefrontModel.SelectionHandle ROTOR = MODEL.prepareRenderOnlyInCallOrder("Rotor");
    private static final LegacyWavefrontModel.SelectionHandle DISH = MODEL.prepareRenderOnlyInCallOrder("Dish");

    public SatelliteLinkRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRender(SatelliteLinkBlockEntity link, Vec3 cameraPos) {
        return BlockEntityRenderer.super.shouldRender(link, cameraPos)
                && LegacyBlockEntityRenderCulling.shouldRenderMachine(link, getViewDistance());
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.machine();
    }

    @Override
    public void render(SatelliteLinkBlockEntity link, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(link, getViewDistance())) {
            return;
        }
        BlockState state = link.getBlockState();
        Direction facing = state.getValue(HorizontalMachineBlock.FACING);
        Direction clockwise = facing.getClockWise();
        int light = LegacyRenderLighting.resolveMultiblockLight(link, packedLight);
        float rotation = Mth.lerp(partialTick, link.getPreviousRotation(), link.getRotation());
        float lift = Mth.lerp(partialTick, link.getPreviousLift(), link.getLift());

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        LegacyPoseRotations.rotateYDegrees(poseStack, 180.0F);
        poseStack.translate((facing.getStepX() + clockwise.getStepX()) * 0.5D, 0.0D,
                (facing.getStepZ() + clockwise.getStepZ()) * 0.5D);
        try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(link)) {
            try (var animatedFadeScope = LegacyBlockEntityRenderCulling.animatedModelFadeScope(link)) {
                MODEL.renderOnlyInCallOrder(ObjMachineModels.SATLINK_TEXTURE, poseStack, buffer, light,
                        packedOverlay, BASE);
                poseStack.pushPose();
                LegacyPoseRotations.rotateYDegrees(poseStack, rotation);
                MODEL.renderOnlyInCallOrder(ObjMachineModels.SATLINK_TEXTURE, poseStack, buffer, light,
                        packedOverlay, ROTOR);
                poseStack.translate(0.0D, 7.375D, 0.0D);
                poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(lift));
                poseStack.translate(0.0D, -7.375D, 0.0D);
                MODEL.renderOnlyInCallOrder(ObjMachineModels.SATLINK_TEXTURE, poseStack, buffer, light,
                        packedOverlay, DISH);
                poseStack.popPose();
            }
        }
        poseStack.popPose();
    }
}
