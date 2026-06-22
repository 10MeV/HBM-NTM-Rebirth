package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.LegacyMachineDefinition;
import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.hbm.ntm.blockentity.SolidifierBlockEntity;
import com.hbm.ntm.client.obj.ObjModelLibrary;
import com.hbm.ntm.client.obj.ObjRenderContext;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class SolidifierRenderer implements BlockEntityRenderer<SolidifierBlockEntity> {
    public SolidifierRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(SolidifierBlockEntity blockEntity) {
        return false;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.MACHINE;
    }

    @Override
    public void render(SolidifierBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
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

        ObjRenderContext context = new ObjRenderContext(poseStack, buffer, state, modelLight, packedOverlay);
        ObjModelLibrary.MACHINE_SOLIDIFIER.renderPart("Main", context);
        renderFluid(blockEntity, context, poseStack);
        renderTintedPart(LegacyTileRenderPlans.solidifierGlassPlan(), context);

        poseStack.popPose();
    }

    private static void renderFluid(SolidifierBlockEntity blockEntity, ObjRenderContext context, PoseStack poseStack) {
        LegacyTileRenderPlans.ScaledModelPartPlan plan = LegacyTileRenderPlans.solidifierFluidPlan(
                blockEntity.getTank().getFill(), blockEntity.getTank().getMaxFill(),
                blockEntity.getTank().getTankType().getColor());
        if (!plan.active()) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.0D, plan.pivotY(), 0.0D);
        poseStack.scale((float) plan.scaleX(), (float) plan.scaleY(), (float) plan.scaleZ());
        poseStack.translate(0.0D, -plan.pivotY(), 0.0D);
        renderScaledPart(plan, context);
        poseStack.popPose();
    }

    private static void renderScaledPart(LegacyTileRenderPlans.ScaledModelPartPlan plan, ObjRenderContext context) {
        ObjRenderContext resolved = applyColorBlend(context, plan.color(), plan.blend());
        if (plan.textured()) {
            ObjModelLibrary.MACHINE_SOLIDIFIER.renderPart(plan.partName(), resolved);
        } else {
            ObjModelLibrary.MACHINE_SOLIDIFIER.renderPartUntextured(plan.partName(), resolved);
        }
    }

    private static void renderTintedPart(LegacyTileRenderPlans.ModelPartTintPlan plan, ObjRenderContext context) {
        if (!plan.active()) {
            return;
        }
        ObjRenderContext resolved = applyColorBlend(context, plan.color(), plan.blend());
        if (plan.textured()) {
            ObjModelLibrary.MACHINE_SOLIDIFIER.renderPart(plan.partName(), resolved);
        } else {
            ObjModelLibrary.MACHINE_SOLIDIFIER.renderPartUntextured(plan.partName(), resolved);
        }
    }

    private static ObjRenderContext applyColorBlend(ObjRenderContext context, LegacyTileRenderPlans.RgbaPlan color,
            LegacyTileRenderPlans.BlendStatePlan blend) {
        ObjRenderContext resolved = context;
        if (blend != null) {
            resolved = resolved.withRenderMode(blend.modernRenderMode());
        }
        if (color != null) {
            resolved = resolved.withRgba(color.redByte(), color.greenByte(), color.blueByte(), color.alphaByte());
        }
        return resolved;
    }
}
