package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.LegacyMachineDefinition;
import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.hbm.ntm.blockentity.SolidifierBlockEntity;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjModelLibrary;
import com.hbm.ntm.client.render.LegacyMachineEffectPresenter;
import com.hbm.ntm.client.render.LegacyMachineEffectPresenter.PresentStage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class SolidifierRenderer implements BlockEntityRenderer<SolidifierBlockEntity> {
    private static final LegacyWavefrontModel MODEL = ObjModelLibrary.MACHINE_SOLIDIFIER;
    private static final LegacyWavefrontModel.SelectionHandle FLUID =
            MODEL.prepareRenderOnlyInCallOrder("Fluid");
    private static final LegacyWavefrontModel.SelectionHandle GLASS =
            MODEL.prepareRenderOnlyInCallOrder("Glass");

    public SolidifierRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(SolidifierBlockEntity blockEntity) {
        return false;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.machine();
    }

    @Override
    public void render(SolidifierBlockEntity blockEntity, float partialTick, PoseStack poseStack,
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

        LegacyTileRenderPlans.ScaledModelPartPlan fluidPlan = LegacyTileRenderPlans.solidifierFluidPlan(
                blockEntity.getTank().getFill(), blockEntity.getTank().getMaxFill(),
                blockEntity.getTank().getTankType().getColor());
        LegacyTileRenderPlans.ModelPartTintPlan glassPlan = LegacyTileRenderPlans.solidifierGlassPlan();
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
            renderModelPart(FLUID, poseStack, buffer, packedLight, packedOverlay, plan.color(), plan.blend());
        } else {
            renderModelPartUntextured(FLUID, poseStack, buffer, plan.color(), plan.blend());
        }
    }

    private static void renderTintedPart(LegacyTileRenderPlans.ModelPartTintPlan plan, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!plan.active()) {
            return;
        }
        if (plan.textured()) {
            renderModelPart(GLASS, poseStack, buffer, packedLight, packedOverlay, plan.color(), plan.blend());
        } else {
            renderModelPartUntextured(GLASS, poseStack, buffer, plan.color(), plan.blend());
        }
    }

    private static void renderModelPart(LegacyWavefrontModel.SelectionHandle handle, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay, LegacyTileRenderPlans.RgbaPlan color,
            LegacyTileRenderPlans.BlendStatePlan blend) {
        MODEL.renderOnlyInCallOrder(MODEL.textureLocation(), poseStack, buffer, packedLight, packedOverlay,
                red(color), green(color), blue(color), alpha(color), false, renderMode(blend),
                LegacyWavefrontModel.UvTransform.DEFAULT, handle);
    }

    private static void renderModelPartUntextured(LegacyWavefrontModel.SelectionHandle handle, PoseStack poseStack,
            MultiBufferSource buffer, LegacyTileRenderPlans.RgbaPlan color, LegacyTileRenderPlans.BlendStatePlan blend) {
        MODEL.renderOnlyUntextured(poseStack, buffer, red(color), green(color), blue(color), alpha(color),
                renderMode(blend), handle);
    }

    private static int red(LegacyTileRenderPlans.RgbaPlan color) {
        return color == null ? 255 : color.redByte();
    }

    private static int green(LegacyTileRenderPlans.RgbaPlan color) {
        return color == null ? 255 : color.greenByte();
    }

    private static int blue(LegacyTileRenderPlans.RgbaPlan color) {
        return color == null ? 255 : color.blueByte();
    }

    private static int alpha(LegacyTileRenderPlans.RgbaPlan color) {
        return color == null ? 255 : color.alphaByte();
    }

    private static LegacyTexturedRenderMode renderMode(LegacyTileRenderPlans.BlendStatePlan blend) {
        return blend == null ? LegacyTexturedRenderMode.CUTOUT_NO_CULL : blend.modernRenderMode();
    }
}
