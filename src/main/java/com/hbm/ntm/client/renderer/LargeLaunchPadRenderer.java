package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.LargeLaunchPadBlock;
import com.hbm.ntm.blockentity.LargeLaunchPadBlockEntity;
import com.hbm.ntm.blockentity.LaunchPadBlockEntity;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.ObjLaunchModels;
import com.hbm.ntm.item.missile.MissileItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

public class LargeLaunchPadRenderer implements BlockEntityRenderer<LargeLaunchPadBlockEntity> {
    public LargeLaunchPadRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.machine();
    }

    @Override
    public void render(LargeLaunchPadBlockEntity launchPad, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(launchPad, getViewDistance())) {
            return;
        }
        Direction facing = launchPad.getBlockState().hasProperty(LargeLaunchPadBlock.FACING)
                ? launchPad.getBlockState().getValue(LargeLaunchPadBlock.FACING)
                : Direction.NORTH;
        ItemStack missile = launchPad.getItems().getStackInSlot(LaunchPadBlockEntity.SLOT_MISSILE);
        int modelLight = LegacyRenderLighting.resolveBoundsLight(launchPad,
                launchPadLightingBounds(launchPad.getBlockPos(), !missile.isEmpty()), packedLight);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(yRotation(facing)));

        try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(launchPad)) {
            renderFormFactorParts(launchPad, missile, partialTick, poseStack, buffer, modelLight, packedOverlay);
        }
        poseStack.popPose();
    }

    private static void renderFormFactorParts(LargeLaunchPadBlockEntity launchPad, ItemStack missile,
            float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ErectorParts parts = partsFor(launchPad.getFormFactor());
        if (parts == null) {
            return;
        }

        poseStack.pushPose();
        ObjLaunchModels.renderMissileErectorPart(parts.pad(), parts.texture(), poseStack, buffer, packedLight,
                packedOverlay, LegacyTexturedRenderMode.CUTOUT_CULL);
        if (!missile.isEmpty() && launchPad.isErected()) {
            ObjLaunchModels.renderMissileErectorPart(parts.rope(), parts.texture(), poseStack, buffer, packedLight,
                    packedOverlay, LegacyTexturedRenderMode.CUTOUT_CULL);
        }

        float erectorAngle = launchPad.getErector(partialTick);
        float lift = launchPad.getLift(partialTick);
        poseStack.translate(0.0D, parts.pivotY(), -parts.pivotZ());
        poseStack.mulPose(Axis.XN.rotationDegrees(erectorAngle));
        poseStack.translate(0.0D, -parts.pivotY(), parts.pivotZ());
        ObjLaunchModels.renderMissileErectorPart(parts.pivot(), parts.texture(), poseStack, buffer, packedLight,
                packedOverlay, LegacyTexturedRenderMode.CUTOUT_CULL);
        poseStack.translate(0.0D, lift, 0.0D);
        ObjLaunchModels.renderMissileErectorPart(parts.erector(), parts.texture(), poseStack, buffer, packedLight,
                packedOverlay, LegacyTexturedRenderMode.CUTOUT_CULL);
        if (!missile.isEmpty() && (launchPad.isErected() || launchPad.isReadyToLoad())) {
            poseStack.translate(0.0D, 2.0D, 0.0D);
            MissileItemRenderer.renderRawMissile(missile, poseStack, buffer, packedLight, packedOverlay);
        }
        poseStack.popPose();
    }

    private static ErectorParts partsFor(int formFactorOrdinal) {
        MissileItem.FormFactor[] values = MissileItem.FormFactor.values();
        if (formFactorOrdinal < 0 || formFactorOrdinal >= values.length) {
            return null;
        }
        MissileItem.FormFactor formFactor = values[formFactorOrdinal];
        return switch (formFactor) {
            case ABM, OTHER -> new ErectorParts("ABM_Pad", "ABM_Erector", "ABM_Pivot", "ABM_Rope",
                    1.5D, 1.25D, ObjLaunchModels.MISSILE_ERECTOR_ABM_TEXTURE);
            case MICRO -> new ErectorParts("Micro_Pad", "Micro_Erector", "Micro_Pivot", "Micro_Rope",
                    1.5D, 1.25D, ObjLaunchModels.MISSILE_ERECTOR_MICRO_TEXTURE);
            case V2 -> new ErectorParts("V2_Pad", "V2_Erector", "V2_Pivot", "V2_Rope",
                    1.75D, 1.25D, ObjLaunchModels.MISSILE_ERECTOR_V2_TEXTURE);
            case STRONG -> new ErectorParts("Strong_Pad", "Strong_Erector", "Strong_Pivot", "Strong_Rope",
                    3.0D, 1.5D, ObjLaunchModels.MISSILE_ERECTOR_STRONG_TEXTURE);
            case HUGE -> new ErectorParts("Huge_Pad", "Huge_Erector", "Huge_Pivot", "Huge_Rope",
                    3.0D, 1.5D, ObjLaunchModels.MISSILE_ERECTOR_HUGE_TEXTURE);
            case ATLAS -> new ErectorParts("Atlas_Pad", "Atlas_Erector", "Atlas_Pivot", "Atlas_Rope",
                    4.0D, 1.5D, ObjLaunchModels.MISSILE_ERECTOR_ATLAS_TEXTURE);
        };
    }

    private static AABB launchPadLightingBounds(BlockPos pos, boolean missileLoaded) {
        return new AABB(
                pos.getX() - 5,
                pos.getY(),
                pos.getZ() - 5,
                pos.getX() + 6,
                pos.getY() + (missileLoaded ? 15 : 2),
                pos.getZ() + 6);
    }

    private static float yRotation(Direction facing) {
        return switch (facing) {
            case EAST -> 90.0F;
            case SOUTH -> 180.0F;
            case WEST -> 270.0F;
            default -> 0.0F;
        };
    }

    private record ErectorParts(String pad, String erector, String pivot, String rope,
                                double pivotZ, double pivotY, ResourceLocation texture) {
    }
}
