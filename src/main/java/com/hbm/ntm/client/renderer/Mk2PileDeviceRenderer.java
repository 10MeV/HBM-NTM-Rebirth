package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.Mk2PileDeviceBlock;
import com.hbm.ntm.blockentity.Mk2PileDeviceBlockEntity;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjPileModels;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

/**
 * Exact modern rendering carrier for the legacy {@code RenderPileLoader},
 * {@code RenderPileVent}, and {@code RenderPileControl} part transforms.
 */
public final class Mk2PileDeviceRenderer implements BlockEntityRenderer<Mk2PileDeviceBlockEntity> {
    private static final LegacyWavefrontModel.SelectionHandle LOADER =
            ObjPileModels.LOADER.prepareRenderOnlyInCallOrder("Loader");
    private static final LegacyWavefrontModel.SelectionHandle LEVER =
            ObjPileModels.LOADER.prepareRenderOnlyInCallOrder("Lever");
    private static final LegacyWavefrontModel.SelectionHandle SLIDER =
            ObjPileModels.LOADER.prepareRenderOnlyInCallOrder("Slider");
    // The shipped 1.7.10 OBJ has no Rod object, but the old renderer still invokes it conditionally.
    private static final LegacyWavefrontModel.SelectionHandle LOADER_ROD =
            ObjPileModels.LOADER.prepareRenderOnlyInCallOrder("Rod");
    private static final LegacyWavefrontModel.SelectionHandle PIPE =
            ObjPileModels.VENT.prepareRenderOnlyInCallOrder("Pipe");
    private static final LegacyWavefrontModel.SelectionHandle FAN =
            ObjPileModels.VENT.prepareRenderOnlyInCallOrder("Fan");
    private static final LegacyWavefrontModel.SelectionHandle BASE =
            ObjPileModels.CONTROL.prepareRenderOnlyInCallOrder("Base");
    private static final LegacyWavefrontModel.SelectionHandle CONTROL_ROD =
            ObjPileModels.CONTROL.prepareRenderOnlyInCallOrder("Rod");

    public Mk2PileDeviceRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.machine();
    }

    @Override
    public boolean shouldRender(Mk2PileDeviceBlockEntity device, Vec3 cameraPos) {
        return BlockEntityRenderer.super.shouldRender(device, cameraPos)
                && LegacyBlockEntityRenderCulling.shouldRenderMachine(device, getViewDistance());
    }

    @Override
    public void render(Mk2PileDeviceBlockEntity device, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(device, getViewDistance())) {
            return;
        }
        if (!(device.getBlockState().getBlock() instanceof Mk2PileDeviceBlock)) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        rotateLegacyFacing(poseStack, device.getBlockState().getValue(Mk2PileDeviceBlock.FACING));
        try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(device)) {
            try (var animatedFadeScope = LegacyBlockEntityRenderCulling.animatedModelFadeScope(device)) {
                switch (device.getBlockState().getValue(Mk2PileDeviceBlock.KIND)) {
                    case LOADER -> renderLoader(device, partialTick, poseStack, buffer, packedLight, packedOverlay);
                    case VENT -> renderVent(device, partialTick, poseStack, buffer, packedLight, packedOverlay);
                    case CONTROL -> renderControl(device, partialTick, poseStack, buffer, packedLight, packedOverlay);
                }
            }
        }
        poseStack.popPose();
    }

    private static void renderLoader(Mk2PileDeviceBlockEntity device, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        double position = device.getRenderActuatorLevel(partialTick);
        render(ObjPileModels.LOADER, LOADER, ObjPileModels.LOADER_TEXTURE, poseStack, buffer, packedLight, packedOverlay);
        poseStack.pushPose();
        poseStack.translate(-0.1875D, 0.5D, 0.0D);
        LegacyPoseRotations.rotateZDegrees(poseStack, (float) (position * 90.0D));
        poseStack.translate(0.1875D, -0.5D, 0.0D);
        render(ObjPileModels.LOADER, LEVER, ObjPileModels.LOADER_TEXTURE, poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(position * -0.5D, 0.0D, 0.0D);
        render(ObjPileModels.LOADER, SLIDER, ObjPileModels.LOADER_TEXTURE, poseStack, buffer, packedLight, packedOverlay);
        if (!device.getLoaderStack().isEmpty()) {
            render(ObjPileModels.LOADER, LOADER_ROD, ObjPileModels.LOADER_TEXTURE, poseStack, buffer, packedLight, packedOverlay);
        }
        poseStack.popPose();
    }

    private static void renderVent(Mk2PileDeviceBlockEntity device, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        render(ObjPileModels.VENT, PIPE, ObjPileModels.VENT_TEXTURE, poseStack, buffer, packedLight, packedOverlay);
        poseStack.pushPose();
        LegacyPoseRotations.rotateYDegrees(poseStack, device.getRenderFanAngle(partialTick));
        render(ObjPileModels.VENT, FAN, ObjPileModels.VENT_TEXTURE, poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderControl(Mk2PileDeviceBlockEntity device, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        render(ObjPileModels.CONTROL, BASE, ObjPileModels.CONTROL_TEXTURE, poseStack, buffer, packedLight, packedOverlay);
        poseStack.pushPose();
        poseStack.translate(0.0D, device.getRenderActuatorLevel(partialTick) * 0.75D, 0.0D);
        render(ObjPileModels.CONTROL, CONTROL_ROD, ObjPileModels.CONTROL_TEXTURE, poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void render(LegacyWavefrontModel model, LegacyWavefrontModel.SelectionHandle handle,
            ResourceLocation texture, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        model.renderOnlyInCallOrder(texture, poseStack, buffer, packedLight, packedOverlay, handle);
    }

    /** Old metadata 0/1/2/3 corresponds to north/south/west/east respectively. */
    private static void rotateLegacyFacing(PoseStack poseStack, Direction facing) {
        float degrees = switch (facing) {
            case NORTH -> 90.0F;
            case SOUTH -> 270.0F;
            case WEST -> 180.0F;
            case EAST -> 0.0F;
            default -> 0.0F;
        };
        LegacyPoseRotations.rotateYDegrees(poseStack, degrees);
    }
}
