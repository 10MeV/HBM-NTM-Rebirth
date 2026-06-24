package com.hbm.ntm.client.renderer;

import com.hbm.ntm.blockentity.CargoElevatorBlockEntity;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjMachineModels;
import com.hbm.ntm.client.obj.ObjRenderContext;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class CargoElevatorRenderer implements BlockEntityRenderer<CargoElevatorBlockEntity> {
    public static final ResourceLocation TEXTURE = ObjMachineModels.ELEVATOR_TEXTURE;
    private static final LegacyWavefrontModel MODEL = ObjMachineModels.ELEVATOR_LEGACY;
    private static final LegacyWavefrontModel.SelectionHandle BASE =
            MODEL.prepareRenderOnlyInCallOrder("Base");
    private static final LegacyWavefrontModel.SelectionHandle PLATFORM =
            MODEL.prepareRenderOnlyInCallOrder("Platform");
    private static final LegacyWavefrontModel.SelectionHandle PISTON =
            MODEL.prepareRenderOnlyInCallOrder("Piston");
    private static final LegacyWavefrontModel.SelectionHandle GUIDES =
            MODEL.prepareRenderOnlyInCallOrder("Guides");

    public CargoElevatorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(CargoElevatorBlockEntity blockEntity) {
        return false;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.MACHINE;
    }

    @Override
    public void render(CargoElevatorBlockEntity elevator, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        int modelLight = LegacyRenderLighting.resolveBoundsLight(elevator, elevator.getRenderBoundingBox(),
                packedLight);
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        ObjRenderContext context = new ObjRenderContext(poseStack, buffer, elevator.getBlockState(),
                modelLight, packedOverlay).withRenderMode(LegacyTexturedRenderMode.CUTOUT_CULL);

        LegacyTileRenderPlans.CargoElevatorPlan plan = LegacyTileRenderPlans.cargoElevatorPlan(
                elevator.shouldRenderPlatform(), elevator.getPrevExtension(), elevator.getExtension(),
                partialTick, elevator.getHeight());
        if (plan.renderPlatform()) {
            renderPart(BASE, context);
            renderTranslatedParts(plan.platformParts(), poseStack, context);
        }

        renderTranslatedParts(plan.guides(), poseStack, context);
        poseStack.popPose();
    }

    private static void renderTranslatedParts(
            Iterable<LegacyTileRenderPlans.TranslatedModelPartPlan> parts, PoseStack poseStack,
            ObjRenderContext context) {
        for (LegacyTileRenderPlans.TranslatedModelPartPlan part : parts) {
            if (!part.active()) {
                continue;
            }
            poseStack.pushPose();
            poseStack.translate(part.translateX(), part.translateY(), part.translateZ());
            renderPart(handle(part), context);
            poseStack.popPose();
        }
    }

    private static LegacyWavefrontModel.SelectionHandle handle(
            LegacyTileRenderPlans.TranslatedModelPartPlan part) {
        LegacyWavefrontModel.SelectionHandle handle = handle(part.partName());
        if (handle != null) {
            return handle;
        }
        throw new IllegalArgumentException("Unexpected cargo elevator part: " + part.partName());
    }

    static void renderModelPart(String partName, ObjRenderContext context) {
        LegacyWavefrontModel.SelectionHandle handle = handle(partName);
        if (handle != null) {
            renderPart(handle, context);
            return;
        }
        MODEL.renderPart(partName, TEXTURE, context);
    }

    private static LegacyWavefrontModel.SelectionHandle handle(String partName) {
        if (partName == null) {
            return null;
        }
        return switch (partName) {
            case "Base" -> BASE;
            case "Platform" -> PLATFORM;
            case "Piston" -> PISTON;
            case "Guides" -> GUIDES;
            default -> null;
        };
    }

    private static void renderPart(LegacyWavefrontModel.SelectionHandle handle, ObjRenderContext context) {
        MODEL.renderOnlyInCallOrder(TEXTURE, context, handle);
    }
}
