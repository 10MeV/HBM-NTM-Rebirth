package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.LegacyMachineDefinition;
import com.hbm.ntm.block.LegacyMachineRenderShapes;
import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.hbm.ntm.blockentity.ChemicalFactoryBlockEntity;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjMachineModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.IdentityHashMap;
import java.util.Map;

public class ChemicalFactoryRenderer implements BlockEntityRenderer<ChemicalFactoryBlockEntity> {
    private static final Map<LegacyMachineDefinition, LegacyWavefrontModel> MODELS = new IdentityHashMap<>();
    private static final LegacyWavefrontModel.SelectionHandle BASE =
            ObjMachineModels.CHEMICAL_FACTORY.prepareRenderOnlyInCallOrder("Base");
    private static final LegacyWavefrontModel.SelectionHandle FRAME =
            ObjMachineModels.CHEMICAL_FACTORY.prepareRenderOnlyInCallOrder("Frame");
    private static final LegacyWavefrontModel.SelectionHandle BASE_FRAME =
            ObjMachineModels.CHEMICAL_FACTORY.prepareRenderOnlyInCallOrder("Base", "Frame");
    private static final LegacyWavefrontModel.SelectionHandle FAN_1 =
            ObjMachineModels.CHEMICAL_FACTORY.prepareRenderOnlyInCallOrder("Fan1");
    private static final LegacyWavefrontModel.SelectionHandle FAN_2 =
            ObjMachineModels.CHEMICAL_FACTORY.prepareRenderOnlyInCallOrder("Fan2");

    public ChemicalFactoryRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(ChemicalFactoryBlockEntity blockEntity) {
        return false;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.machine();
    }

    @Override
    public boolean shouldRender(ChemicalFactoryBlockEntity chemicalFactory, Vec3 cameraPos) {
        return BlockEntityRenderer.super.shouldRender(chemicalFactory, cameraPos)
                && LegacyBlockEntityRenderCulling.shouldRenderMachine(chemicalFactory, getViewDistance());
    }

    @Override
    public void render(ChemicalFactoryBlockEntity chemicalFactory, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(chemicalFactory, getViewDistance())) {
            return;
        }
        BlockState state = chemicalFactory.getBlockState();
        if (!(state.getBlock() instanceof LegacyVisibleMultiblockMachineBlock block)) {
            return;
        }

        LegacyMachineDefinition definition = block.definition();
        int modelLight = LegacyRenderLighting.resolveMachineLight(chemicalFactory, state, definition, packedLight);
        LegacyWavefrontModel model = MODELS.computeIfAbsent(definition,
                key -> new LegacyWavefrontModel(key.modelLocation(), key.textureLocation()).asVBO());
        float anim = Mth.lerp(partialTick, chemicalFactory.getPrevAnim(), chemicalFactory.getAnim());

        try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(chemicalFactory);
                LegacyRenderLighting.ModelViewSamplingScope ignored =
                LegacyRenderLighting.pushModelViewSampling(chemicalFactory, poseStack.last().pose())) {
            poseStack.pushPose();
            poseStack.translate(0.5D, 0.0D, 0.5D);
            poseStack.mulPose(Axis.YP.rotationDegrees(definition.yRotation(state)));
            Vec3 translation = definition.modelTranslation(state);
            poseStack.translate(translation.x, translation.y, translation.z);
            poseStack.mulPose(Axis.YP.rotationDegrees(definition.postModelYRotation(state)));
            ResourceLocation texture = definition.textureLocation();

            renderStaticBaseFrame(model, chemicalFactory.shouldRenderFrame(), texture, poseStack, buffer,
                    modelLight, packedOverlay);

            try (var animatedFadeScope = LegacyBlockEntityRenderCulling.animatedModelFadeScope(chemicalFactory)) {
                double fanAngle = anim * LegacyTileRenderPlans.CHEMICAL_FACTORY_FAN_ROTATION_SCALE % 360.0D;
                renderRotatingYPart(model, "Fan1", LegacyTileRenderPlans.CHEMICAL_FACTORY_FAN_PIVOT_X,
                        0.0D, 0.0D, fanAngle, texture, poseStack, buffer, modelLight, packedOverlay);
                renderRotatingYPart(model, "Fan2", -LegacyTileRenderPlans.CHEMICAL_FACTORY_FAN_PIVOT_X,
                        0.0D, 0.0D, fanAngle, texture, poseStack, buffer, modelLight, packedOverlay);
            }

            poseStack.popPose();
        }
    }

    private static void renderRotatingYPart(LegacyWavefrontModel model, String partName,
            double pivotX, double pivotY, double pivotZ, double angleDegrees, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(pivotX, pivotY, pivotZ);
        poseStack.mulPose(Axis.YP.rotationDegrees((float) angleDegrees));
        poseStack.translate(-pivotX, -pivotY, -pivotZ);
        renderModelPart(model, partName, texture, poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderModelPart(LegacyWavefrontModel model, String partName, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        LegacyWavefrontModel.SelectionHandle handle = handle(partName);
        if (handle != null) {
            model.renderOnlyInCallOrder(texture, poseStack, buffer, packedLight, packedOverlay, handle);
            return;
        }
        model.renderPart(partName, texture, poseStack, buffer, packedLight, packedOverlay);
    }

    private static void renderStaticBaseFrame(LegacyWavefrontModel model, boolean frameVisible,
            ResourceLocation texture, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay) {
        if (!LegacyMachineRenderShapes.renderChunkBakedStaticsInBer()) {
            return;
        }
        model.renderOnlyInCallOrder(texture, poseStack, buffer, packedLight, packedOverlay,
                frameVisible ? BASE_FRAME : BASE);
    }

    private static LegacyWavefrontModel.SelectionHandle handle(String partName) {
        if (partName == null) {
            return null;
        }
        return switch (partName) {
            case "Base" -> BASE;
            case "Frame" -> FRAME;
            case "Fan1" -> FAN_1;
            case "Fan2" -> FAN_2;
            default -> null;
        };
    }
}
