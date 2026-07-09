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
    private static final LegacyWavefrontModel MODEL = ObjModelLibrary.MACHINE_LIQUEFACTOR;
    private static final LegacyWavefrontModel.SelectionHandle FLUID =
            MODEL.prepareRenderOnlyInCallOrder("Fluid");
    private static final LegacyWavefrontModel.SelectionHandle GLASS =
            MODEL.prepareRenderOnlyInCallOrder("Glass");
    private static final int GLASS_RED = 191;
    private static final int GLASS_GREEN = 255;
    private static final int GLASS_BLUE = 255;
    private static final int GLASS_ALPHA = 38;

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
    public boolean shouldRender(LiquefactorBlockEntity blockEntity, Vec3 cameraPos) {
        return BlockEntityRenderer.super.shouldRender(blockEntity, cameraPos)
                && LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance());
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

        double fluidHeight = fluidHeight(blockEntity.getTank().getFill(), blockEntity.getTank().getMaxFill());
        int fluidColor = blockEntity.getTank().getTankType().getColor();
        try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(blockEntity)) {
            enqueueFluid(fluidHeight, fluidColor, poseStack, buffer);
            enqueueGlass(poseStack, buffer, modelLight, packedOverlay);
        }

        poseStack.popPose();
    }

    private static void enqueueFluid(double height, int fluidColor, PoseStack poseStack, MultiBufferSource buffer) {
        if (height <= 0.0D) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.0D, LegacyTileRenderPlans.LIQUEFACTOR_FLUID_PIVOT_Y, 0.0D);
        poseStack.scale(1.0F, (float) height, 1.0F);
        poseStack.translate(0.0D, -LegacyTileRenderPlans.LIQUEFACTOR_FLUID_PIVOT_Y, 0.0D);
        LegacyMachineEffectPresenter.enqueueUntexturedObjPartGroup(PresentStage.AFTER_BLOCK_ENTITIES,
                poseStack, buffer, parts -> parts.add(MODEL, FLUID, red(fluidColor), green(fluidColor),
                        blue(fluidColor), 255, LegacyTexturedRenderMode.CUTOUT_NO_CULL));
        poseStack.popPose();
    }

    private static void enqueueGlass(PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        LegacyMachineEffectPresenter.enqueueTexturedObjPartGroup(PresentStage.AFTER_BLOCK_ENTITIES,
                poseStack, buffer, parts -> parts.add(MODEL, GLASS, MODEL.textureLocation(), packedLight,
                        packedOverlay, GLASS_RED, GLASS_GREEN, GLASS_BLUE, GLASS_ALPHA, false,
                        LegacyTexturedRenderMode.TRANSLUCENT_NO_DEPTH_WRITE,
                        LegacyWavefrontModel.UvTransform.DEFAULT));
    }

    private static double fluidHeight(int fill, int maxFill) {
        if (maxFill <= 0) {
            return 0.0D;
        }
        return Math.max(0.0D, Math.min(1.0D, (double) Math.max(0, fill) / (double) maxFill));
    }

    private static int red(int color) {
        return color >> 16 & 255;
    }

    private static int green(int color) {
        return color >> 8 & 255;
    }

    private static int blue(int color) {
        return color & 255;
    }
}
