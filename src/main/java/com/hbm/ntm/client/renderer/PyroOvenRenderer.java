package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.LegacyMachineDefinition;
import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.hbm.ntm.blockentity.PyroOvenBlockEntity;
import com.hbm.ntm.client.obj.LegacyObjTransforms;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjMachineModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class PyroOvenRenderer implements BlockEntityRenderer<PyroOvenBlockEntity> {
    private static final Map<LegacyMachineDefinition, LegacyWavefrontModel> MODELS = new IdentityHashMap<>();
    private static final LegacyWavefrontModel.SelectionHandle OVEN =
            ObjMachineModels.PYROOVEN.prepareRenderOnlyInCallOrder("Oven");
    private static final LegacyWavefrontModel.SelectionHandle SLIDER =
            ObjMachineModels.PYROOVEN.prepareRenderOnlyInCallOrder("Slider");
    private static final LegacyWavefrontModel.SelectionHandle FAN =
            ObjMachineModels.PYROOVEN.prepareRenderOnlyInCallOrder("Fan");

    public PyroOvenRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(PyroOvenBlockEntity blockEntity) {
        return false;
    }

    @Override
    public boolean shouldRender(PyroOvenBlockEntity blockEntity, Vec3 cameraPos) {
        return BlockEntityRenderer.super.shouldRender(blockEntity, cameraPos)
                && LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance());
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.machine();
    }

    @Override
    public void render(PyroOvenBlockEntity pyroOven, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(pyroOven, getViewDistance())) {
            return;
        }
        BlockState state = pyroOven.getBlockState();
        if (!(state.getBlock() instanceof LegacyVisibleMultiblockMachineBlock block)) {
            return;
        }

        LegacyMachineDefinition definition = block.definition();
        int modelLight = LegacyRenderLighting.resolveMachineLight(pyroOven, state, definition, packedLight);
        LegacyWavefrontModel model = MODELS.computeIfAbsent(definition,
                key -> new LegacyWavefrontModel(key.modelLocation(), key.textureLocation()).asVBO());
        float anim = Mth.lerp(partialTick, pyroOven.getPrevAnim(), pyroOven.getAnim());

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(definition.yRotation(state)));
        Vec3 translation = definition.modelTranslation(state);
        poseStack.translate(translation.x, translation.y, translation.z);
        poseStack.mulPose(Axis.YP.rotationDegrees(definition.postModelYRotation(state)));

        try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(pyroOven)) {
            double sliderX = LegacyObjTransforms.softPeakSine(
                    anim * LegacyTileRenderPlans.PYRO_OVEN_SLIDER_ANIM_SCALE)
                    * LegacyTileRenderPlans.PYRO_OVEN_SLIDER_TRAVEL_SCALE
                    + LegacyTileRenderPlans.PYRO_OVEN_SLIDER_BASE_X;
            double fanAngle = anim * LegacyTileRenderPlans.PYRO_OVEN_FAN_ROTATION_SCALE % 360.0D;
            try (var animatedFadeScope = LegacyBlockEntityRenderCulling.animatedModelFadeScope(pyroOven)) {
                renderTranslatedPart(model, "Slider", definition.textureLocation(), sliderX, 0.0D, 0.0D,
                        poseStack, buffer, modelLight, packedOverlay);
                renderRotatingYPart(model, "Fan", definition.textureLocation(),
                        LegacyTileRenderPlans.PYRO_OVEN_FAN_PIVOT_X,
                        LegacyTileRenderPlans.PYRO_OVEN_FAN_PIVOT_Z,
                        fanAngle, poseStack, buffer, modelLight, packedOverlay);
            }
        }

        poseStack.popPose();
    }

    private static void renderTranslatedPart(LegacyWavefrontModel model, String partName, ResourceLocation texture,
            double translateX, double translateY, double translateZ,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(translateX, translateY, translateZ);
        renderModelPart(model, partName, texture, poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderRotatingYPart(LegacyWavefrontModel model, String partName, ResourceLocation texture,
            double pivotX, double pivotZ, double angleDegrees,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(pivotX, 0.0D, pivotZ);
        poseStack.mulPose(Axis.YP.rotationDegrees((float) angleDegrees));
        poseStack.translate(-pivotX, 0.0D, -pivotZ);
        renderModelPart(model, partName, texture, poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    public static void renderModelPart(LegacyWavefrontModel model, String partName, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        LegacyWavefrontModel.SelectionHandle handle = pyroOvenHandle(model, partName);
        if (handle != null) {
            model.renderOnlyInCallOrder(texture, poseStack, buffer, packedLight, packedOverlay, handle);
            return;
        }
        model.renderPart(partName, texture, poseStack, buffer, packedLight, packedOverlay);
    }

    private static LegacyWavefrontModel.SelectionHandle pyroOvenHandle(LegacyWavefrontModel model, String partName) {
        if (!isPyroOvenModel(model)) {
            return null;
        }
        return switch (partName) {
            case "Oven" -> OVEN;
            case "Slider" -> SLIDER;
            case "Fan" -> FAN;
            default -> null;
        };
    }

    private static boolean isPyroOvenModel(LegacyWavefrontModel model) {
        return model == ObjMachineModels.PYROOVEN
                || model.modelLocation().equals(ObjMachineModels.PYROOVEN.modelLocation());
    }
}
