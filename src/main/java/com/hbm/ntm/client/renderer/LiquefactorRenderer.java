package com.hbm.ntm.client.renderer;

import com.hbm.ntm.blockentity.LiquefactorBlockEntity;
import com.hbm.ntm.client.obj.ObjModelLibrary;
import com.hbm.ntm.client.obj.ObjRenderContext;
import com.hbm.ntm.block.LegacyMachineDefinition;
import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

public class LiquefactorRenderer implements BlockEntityRenderer<LiquefactorBlockEntity> {
    public LiquefactorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(LiquefactorBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.MACHINE;
    }

    @Override
    public void render(LiquefactorBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();
        int modelLight = packedLight;
        if (state.getBlock() instanceof LegacyVisibleMultiblockMachineBlock block) {
            LegacyMachineDefinition definition = block.definition();
            modelLight = LegacyRenderLighting.resolveMachineLight(blockEntity, state, definition, packedLight);
        }
        float rotation = state.hasProperty(com.hbm.ntm.block.HorizontalMachineBlock.FACING)
                ? 270.0F - state.getValue(com.hbm.ntm.block.HorizontalMachineBlock.FACING).toYRot()
                : 180.0F;

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));

        ObjRenderContext context = new ObjRenderContext(poseStack, buffer, state, modelLight, packedOverlay);
        ObjModelLibrary.MACHINE_LIQUEFACTOR.renderPart("Main", context);
        renderFluid(blockEntity, context, poseStack);
        renderTintedPart(LegacyTileRenderPlans.liquefactorGlassPlan(), context);

        poseStack.popPose();
    }

    private static void renderFluid(LiquefactorBlockEntity blockEntity, ObjRenderContext context, PoseStack poseStack) {
        LegacyTileRenderPlans.ScaledModelPartPlan plan = LegacyTileRenderPlans.liquefactorFluidPlan(
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
            ObjModelLibrary.MACHINE_LIQUEFACTOR.renderPart(plan.partName(), resolved);
        } else {
            ObjModelLibrary.MACHINE_LIQUEFACTOR.renderPartUntextured(plan.partName(), resolved);
        }
    }

    private static void renderTintedPart(LegacyTileRenderPlans.ModelPartTintPlan plan, ObjRenderContext context) {
        if (!plan.active()) {
            return;
        }
        ObjRenderContext resolved = applyColorBlend(context, plan.color(), plan.blend());
        if (plan.textured()) {
            ObjModelLibrary.MACHINE_LIQUEFACTOR.renderPart(plan.partName(), resolved);
        } else {
            ObjModelLibrary.MACHINE_LIQUEFACTOR.renderPartUntextured(plan.partName(), resolved);
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
