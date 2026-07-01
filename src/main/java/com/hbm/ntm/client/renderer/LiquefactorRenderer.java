package com.hbm.ntm.client.renderer;

import com.hbm.ntm.blockentity.LiquefactorBlockEntity;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjModelLibrary;
import com.hbm.ntm.client.render.LegacyMachineEffectPresenter;
import com.hbm.ntm.client.render.LegacyMachineEffectPresenter.PresentStage;
import com.hbm.ntm.block.LegacyMachineDefinition;
import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class LiquefactorRenderer implements BlockEntityRenderer<LiquefactorBlockEntity> {
    private static final LegacyWavefrontModel.SelectionHandle FLUID =
            ObjModelLibrary.MACHINE_LIQUEFACTOR.prepareRenderOnlyInCallOrder("Fluid");
    private static final LegacyWavefrontModel.SelectionHandle GLASS =
            ObjModelLibrary.MACHINE_LIQUEFACTOR.prepareRenderOnlyInCallOrder("Glass");

    public LiquefactorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(LiquefactorBlockEntity blockEntity) {
        return false;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.machine();
    }

    @Override
    public void render(LiquefactorBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance())) {
            return;
        }
        BlockState state = blockEntity.getBlockState();
        if (!(state.getBlock() instanceof LegacyVisibleMultiblockMachineBlock block)) {
            return;
        }

        LegacyMachineDefinition definition = block.definition();
        int modelLight = LegacyRenderLighting.resolveMachineLight(blockEntity, state, definition, packedLight);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(definition.yRotation(state)));
        Vec3 translation = definition.modelTranslation(state);
        poseStack.translate(translation.x, translation.y, translation.z);
        poseStack.mulPose(Axis.YP.rotationDegrees(definition.postModelYRotation(state)));

        LegacyTileRenderPlans.ScaledModelPartPlan fluidPlan = LegacyTileRenderPlans.liquefactorFluidPlan(
                blockEntity.getTank().getFill(), blockEntity.getTank().getMaxFill(),
                blockEntity.getTank().getTankType().getColor());
        LegacyTileRenderPlans.ModelPartTintPlan glassPlan = LegacyTileRenderPlans.liquefactorGlassPlan();
        try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(blockEntity)) {
            LegacyMachineEffectPresenter.enqueue(PresentStage.AFTER_BLOCK_ENTITIES, poseStack, queuedPose -> {
                renderFluid(fluidPlan, queuedPose, buffer, modelLight, packedOverlay);
                renderTintedPart(glassPlan, queuedPose, buffer, modelLight, packedOverlay);
            });
        }

        poseStack.popPose();
    }

    private static void renderFluid(LegacyTileRenderPlans.ScaledModelPartPlan plan, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!plan.active()) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.0D, plan.pivotY(), 0.0D);
        poseStack.scale((float) plan.scaleX(), (float) plan.scaleY(), (float) plan.scaleZ());
        poseStack.translate(0.0D, -plan.pivotY(), 0.0D);
        renderScaledPart(plan, poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderScaledPart(LegacyTileRenderPlans.ScaledModelPartPlan plan, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (plan.textured()) {
            renderModelPart(plan.partName(), poseStack, buffer, packedLight, packedOverlay,
                    plan.color(), plan.blend());
        } else {
            renderModelPartUntextured(plan.partName(), poseStack, buffer, plan.color(), plan.blend());
        }
    }

    private static void renderTintedPart(LegacyTileRenderPlans.ModelPartTintPlan plan, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!plan.active()) {
            return;
        }
        if (plan.textured()) {
            renderModelPart(plan.partName(), poseStack, buffer, packedLight, packedOverlay,
                    plan.color(), plan.blend());
        } else {
            renderModelPartUntextured(plan.partName(), poseStack, buffer, plan.color(), plan.blend());
        }
    }

    private static void renderModelPart(String partName, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        renderModelPart(partName, poseStack, buffer, packedLight, packedOverlay, null, null);
    }

    private static void renderModelPart(String partName, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, LegacyTileRenderPlans.RgbaPlan color,
            LegacyTileRenderPlans.BlendStatePlan blend) {
        LegacyWavefrontModel.SelectionHandle handle = handle(partName);
        int red = color == null ? 255 : color.redByte();
        int green = color == null ? 255 : color.greenByte();
        int blue = color == null ? 255 : color.blueByte();
        int alpha = color == null ? 255 : color.alphaByte();
        LegacyTexturedRenderMode renderMode = blend == null
                ? LegacyTexturedRenderMode.CUTOUT_NO_CULL
                : blend.modernRenderMode();
        if (handle != null) {
            ObjModelLibrary.MACHINE_LIQUEFACTOR.renderOnlyInCallOrder(
                    ObjModelLibrary.MACHINE_LIQUEFACTOR.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay, red, green, blue, alpha, false, renderMode,
                    LegacyWavefrontModel.UvTransform.DEFAULT, handle);
            return;
        }
        ObjModelLibrary.MACHINE_LIQUEFACTOR.renderPart(partName,
                ObjModelLibrary.MACHINE_LIQUEFACTOR.textureLocation(), poseStack, buffer, packedLight, packedOverlay,
                red, green, blue, alpha, false, renderMode, LegacyWavefrontModel.UvTransform.DEFAULT);
    }

    private static void renderModelPartUntextured(String partName, PoseStack poseStack, MultiBufferSource buffer,
            LegacyTileRenderPlans.RgbaPlan color, LegacyTileRenderPlans.BlendStatePlan blend) {
        LegacyWavefrontModel.SelectionHandle handle = handle(partName);
        int red = color == null ? 255 : color.redByte();
        int green = color == null ? 255 : color.greenByte();
        int blue = color == null ? 255 : color.blueByte();
        int alpha = color == null ? 255 : color.alphaByte();
        LegacyTexturedRenderMode renderMode = blend == null
                ? LegacyTexturedRenderMode.CUTOUT_NO_CULL
                : blend.modernRenderMode();
        if (handle != null) {
            ObjModelLibrary.MACHINE_LIQUEFACTOR.renderOnlyUntextured(poseStack, buffer,
                    red, green, blue, alpha, renderMode, handle);
            return;
        }
        ObjModelLibrary.MACHINE_LIQUEFACTOR.renderPartUntextured(partName, poseStack, buffer,
                red, green, blue, alpha, renderMode);
    }

    private static LegacyWavefrontModel.SelectionHandle handle(String partName) {
        if (partName == null) {
            return null;
        }
        return switch (partName) {
            case "Fluid" -> FLUID;
            case "Glass" -> GLASS;
            default -> null;
        };
    }

}
