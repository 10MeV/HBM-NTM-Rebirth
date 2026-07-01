package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.LegacyMachineDefinition;
import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.hbm.ntm.blockentity.TurbofanBlockEntity;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.ObjMachineModels;
import com.hbm.ntm.client.obj.ObjModelLibrary;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class TurbofanRenderer implements BlockEntityRenderer<TurbofanBlockEntity> {
    private static final LegacyWavefrontModel MODEL = ObjModelLibrary.MACHINE_TURBOFAN;
    private static final LegacyWavefrontModel.SelectionHandle BODY =
            MODEL.prepareRenderOnlyInCallOrder("Body");
    private static final LegacyWavefrontModel.SelectionHandle BLADES =
            MODEL.prepareRenderOnlyInCallOrder("Blades");
    private static final LegacyWavefrontModel.SelectionHandle AFTERBURNER =
            MODEL.prepareRenderOnlyInCallOrder("Afterburner");

    public TurbofanRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(TurbofanBlockEntity blockEntity) {
        return false;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.machine();
    }

    @Override
    public void render(TurbofanBlockEntity blockEntity, float partialTick, PoseStack poseStack,
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

        try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(blockEntity)) {
            try (var animatedFadeScope = LegacyBlockEntityRenderCulling.animatedModelFadeScope(blockEntity)) {
                poseStack.pushPose();
                poseStack.translate(0.0D, LegacyTileRenderPlans.TURBOFAN_BLADE_PIVOT_Y, 0.0D);
                poseStack.mulPose(Axis.ZN.rotationDegrees(blockEntity.getBladeSpin(partialTick)));
                poseStack.translate(0.0D, -LegacyTileRenderPlans.TURBOFAN_BLADE_PIVOT_Y, 0.0D);
                renderPart(BLADES, definition.textureLocation(), poseStack, buffer, modelLight, packedOverlay);
                poseStack.popPose();
            }

            ResourceLocation afterburnerTexture = blockEntity.getAfterburner() == 0
                    ? ObjMachineModels.TURBOFAN_BACK_TEXTURE
                    : ObjMachineModels.TURBOFAN_AFTERBURNER_TEXTURE;
            renderPart(AFTERBURNER, afterburnerTexture, poseStack, buffer, modelLight, packedOverlay);
        }

        poseStack.popPose();
    }

    private static void renderPart(LegacyWavefrontModel.SelectionHandle handle, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        MODEL.renderOnlyInCallOrder(texture, poseStack, buffer, packedLight, packedOverlay, handle,
                LegacyTexturedRenderMode.CUTOUT_CULL);
    }

    static void renderModelPart(LegacyWavefrontModel model, String partName, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode) {
        LegacyWavefrontModel.SelectionHandle handle = sameModel(model) ? handle(partName) : null;
        if (handle != null) {
            MODEL.renderOnlyInCallOrder(texture, poseStack, buffer, packedLight, packedOverlay, handle, renderMode);
            return;
        }
        model.renderPart(partName, texture, poseStack, buffer, packedLight, packedOverlay);
    }

    private static boolean sameModel(LegacyWavefrontModel model) {
        return model == MODEL || model.modelLocation().equals(MODEL.modelLocation());
    }

    private static LegacyWavefrontModel.SelectionHandle handle(String partName) {
        if (partName == null) {
            return null;
        }
        return switch (partName) {
            case "Body" -> BODY;
            case "Blades" -> BLADES;
            case "Afterburner" -> AFTERBURNER;
            default -> null;
        };
    }
}
