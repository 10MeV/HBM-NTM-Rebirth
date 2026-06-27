package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.LegacyMachineDefinition;
import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.hbm.ntm.blockentity.LegacyLargeTurbineBlockEntity;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjModelLibrary;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class LegacyLargeTurbineRenderer implements BlockEntityRenderer<LegacyLargeTurbineBlockEntity> {
    private static final LegacyWavefrontModel MODEL = ObjModelLibrary.MACHINE_TURBINE_LEGACY;
    private static final LegacyWavefrontModel.SelectionHandle BODY =
            MODEL.prepareRenderOnlyInCallOrder("Body");
    private static final LegacyWavefrontModel.SelectionHandle BLADES =
            MODEL.prepareRenderOnlyInCallOrder("Blades");

    public LegacyLargeTurbineRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(LegacyLargeTurbineBlockEntity blockEntity) {
        return false;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.MACHINE;
    }

    @Override
    public void render(LegacyLargeTurbineBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();
        if (!(state.getBlock() instanceof LegacyVisibleMultiblockMachineBlock block)) {
            return;
        }

        LegacyMachineDefinition definition = block.definition();
        int modelLight = LegacyRenderLighting.resolveMachineLight(blockEntity, state, definition, packedLight);
        LegacyTileRenderPlans.BigTurbinePlan plan = LegacyTileRenderPlans.bigTurbinePlan(
                blockEntity.getLastRotor(), blockEntity.getRotor(), partialTick);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(definition.yRotation(state)));
        poseStack.mulPose(Axis.YP.rotationDegrees((float) plan.baseRotationY()));
        Vec3 translation = definition.modelTranslation(state);
        poseStack.translate(translation.x, translation.y, translation.z + plan.translateZ());

        LegacyTexturedRenderMode renderMode = LegacyMachinePartRenderContexts.renderMode(definition.renderMode());
        MODEL.renderOnlyInCallOrder(definition.textureLocation(), poseStack, buffer, modelLight, packedOverlay, BODY,
                renderMode);

        LegacyTileRenderPlans.RotatingModelPartPlan blades = plan.blades();
        poseStack.pushPose();
        poseStack.translate(blades.pivotX(), blades.pivotY(), blades.pivotZ());
        poseStack.mulPose(Axis.ZP.rotationDegrees((float) blades.angleDegrees()));
        poseStack.translate(-blades.pivotX(), -blades.pivotY(), -blades.pivotZ());
        MODEL.renderOnlyInCallOrder(
                definition.partTextures().getOrDefault(blades.partName(), definition.textureLocation()),
                poseStack, buffer, LightTexture.FULL_BRIGHT, packedOverlay, BLADES, renderMode);
        poseStack.popPose();

        poseStack.popPose();
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
            default -> null;
        };
    }
}
