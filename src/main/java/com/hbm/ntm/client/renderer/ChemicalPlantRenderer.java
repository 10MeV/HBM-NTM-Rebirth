package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.LegacyMachineDefinition;
import com.hbm.ntm.block.LegacyMachineRenderShapes;
import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.hbm.ntm.blockentity.ChemicalPlantBlockEntity;
import com.hbm.ntm.client.obj.LegacyObjTransforms;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.LegacyUvAnimation;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjMachineModels;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.recipe.GenericMachineRecipe;
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

public class ChemicalPlantRenderer implements BlockEntityRenderer<ChemicalPlantBlockEntity> {
    private static final Map<LegacyMachineDefinition, LegacyWavefrontModel> MODELS = new IdentityHashMap<>();
    private static final LegacyWavefrontModel.SelectionHandle BASE =
            ObjMachineModels.CHEMICAL_PLANT.prepareRenderOnlyInCallOrder("Base");
    private static final LegacyWavefrontModel.SelectionHandle FRAME =
            ObjMachineModels.CHEMICAL_PLANT.prepareRenderOnlyInCallOrder("Frame");
    private static final LegacyWavefrontModel.SelectionHandle BASE_FRAME =
            ObjMachineModels.CHEMICAL_PLANT.prepareRenderOnlyInCallOrder("Base", "Frame");
    private static final LegacyWavefrontModel.SelectionHandle SLIDER =
            ObjMachineModels.CHEMICAL_PLANT.prepareRenderOnlyInCallOrder("Slider");
    private static final LegacyWavefrontModel.SelectionHandle SPINNER =
            ObjMachineModels.CHEMICAL_PLANT.prepareRenderOnlyInCallOrder("Spinner");
    private static final LegacyWavefrontModel.SelectionHandle FLUID =
            ObjMachineModels.CHEMICAL_PLANT.prepareRenderOnlyInCallOrder("Fluid");

    public ChemicalPlantRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(ChemicalPlantBlockEntity blockEntity) {
        return false;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.machine();
    }

    @Override
    public boolean shouldRender(ChemicalPlantBlockEntity chemicalPlant, Vec3 cameraPos) {
        return BlockEntityRenderer.super.shouldRender(chemicalPlant, cameraPos)
                && LegacyBlockEntityRenderCulling.shouldRenderMachine(chemicalPlant, getViewDistance());
    }

    @Override
    public void render(ChemicalPlantBlockEntity chemicalPlant, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(chemicalPlant, getViewDistance())) {
            return;
        }
        BlockState state = chemicalPlant.getBlockState();
        if (!(state.getBlock() instanceof LegacyVisibleMultiblockMachineBlock block)) {
            return;
        }

        LegacyMachineDefinition definition = block.definition();
        int modelLight = LegacyRenderLighting.resolveMachineLight(chemicalPlant, state, definition, packedLight);
        LegacyWavefrontModel model = MODELS.computeIfAbsent(definition,
                key -> new LegacyWavefrontModel(key.modelLocation(), key.textureLocation()).asVBO());
        float anim = Mth.lerp(partialTick, chemicalPlant.getPrevAnim(), chemicalPlant.getAnim());

        try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(chemicalPlant);
                LegacyRenderLighting.ModelViewSamplingScope ignored =
                LegacyRenderLighting.pushModelViewSampling(chemicalPlant, poseStack.last().pose())) {
            poseStack.pushPose();
            poseStack.translate(0.5D, 0.0D, 0.5D);
            poseStack.mulPose(Axis.YP.rotationDegrees(definition.yRotation(state)));
            Vec3 translation = definition.modelTranslation(state);
            poseStack.translate(translation.x, translation.y, translation.z);
            poseStack.mulPose(Axis.YP.rotationDegrees(definition.postModelYRotation(state)));
            ResourceLocation texture = definition.textureLocation();

            renderStaticBaseFrame(model, chemicalPlant.shouldRenderFrame(), texture, poseStack, buffer,
                    modelLight, packedOverlay);

            try (var animatedFadeScope = LegacyBlockEntityRenderCulling.animatedModelFadeScope(chemicalPlant)) {
                double sliderX = LegacyObjTransforms.softPeakSine(
                        anim * LegacyTileRenderPlans.CHEMICAL_PLANT_SLIDER_ANIM_SCALE)
                        * LegacyTileRenderPlans.CHEMICAL_PLANT_SLIDER_TRAVEL_SCALE;
                renderTranslatedPart(model, "Slider", sliderX, 0.0D, 0.0D, texture,
                        poseStack, buffer, modelLight, packedOverlay);
                double spinnerAngle = anim * LegacyTileRenderPlans.CHEMICAL_PLANT_SPINNER_ROTATION_SCALE % 360.0D;
                renderRotatingYPart(model, "Spinner",
                        LegacyTileRenderPlans.CHEMICAL_PLANT_SPINNER_PIVOT_X, 0.0D,
                        LegacyTileRenderPlans.CHEMICAL_PLANT_SPINNER_PIVOT_Z, spinnerAngle,
                        texture, poseStack, buffer, modelLight, packedOverlay);
            }

            renderProcessingFluid(chemicalPlant, model, poseStack, buffer, modelLight, packedOverlay, anim);

            poseStack.popPose();
        }
    }

    private static void renderTranslatedPart(LegacyWavefrontModel model, String partName,
            double translateX, double translateY, double translateZ, ResourceLocation texture, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(translateX, translateY, translateZ);
        renderModelPart(model, partName, texture, poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
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

    private static void renderProcessingFluid(ChemicalPlantBlockEntity chemicalPlant, LegacyWavefrontModel model,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay, float anim) {
        if (!chemicalPlant.isProcessing()) {
            return;
        }
        GenericMachineRecipe recipe = chemicalPlant.getSelectedRecipeDefinition();
        if (recipe == null) {
            return;
        }
        int color = averageFluidColor(recipe.getFluidOutputs());
        if (color < 0) {
            color = averageFluidColor(recipe.getFluidInputs());
        }
        if (color < 0) {
            return;
        }
        renderModelPart(model, "Fluid", ObjMachineModels.CHEMICAL_PLANT_FLUID_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay,
                color >> 16 & 255, color >> 8 & 255, color & 255,
                Math.round((float) LegacyTileRenderPlans.CHEMICAL_PLANT_FLUID_ALPHA * 255.0F),
                LegacyTexturedRenderMode.TRANSLUCENT_NO_DEPTH_WRITE,
                LegacyWavefrontModel.legacyTextureMatrixDynamic(1.0F, 1.0F,
                        (float) LegacyUvAnimation.chemicalPlantFluidU(anim),
                        (float) LegacyUvAnimation.chemicalPlantFluidV(anim)));
    }

    private static void renderModelPart(LegacyWavefrontModel model, String partName, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        renderModelPart(model, partName, texture, poseStack, buffer, packedLight, packedOverlay,
                255, 255, 255, 255, LegacyTexturedRenderMode.CUTOUT_NO_CULL, LegacyWavefrontModel.UvTransform.DEFAULT);
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

    private static void renderModelPart(LegacyWavefrontModel model, String partName, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay, int red, int green,
            int blue, int alpha, LegacyTexturedRenderMode renderMode, LegacyWavefrontModel.UvTransform uvTransform) {
        LegacyWavefrontModel.SelectionHandle handle = handle(partName);
        if (handle != null) {
            model.renderOnlyInCallOrder(texture, poseStack, buffer, packedLight, packedOverlay,
                    red, green, blue, alpha, false, renderMode, uvTransform, handle);
            return;
        }
        model.renderPart(partName, texture, poseStack, buffer, packedLight, packedOverlay,
                red, green, blue, alpha, false, renderMode, uvTransform);
    }

    private static LegacyWavefrontModel.SelectionHandle handle(String partName) {
        if (partName == null) {
            return null;
        }
        return switch (partName) {
            case "Base" -> BASE;
            case "Frame" -> FRAME;
            case "Slider" -> SLIDER;
            case "Spinner" -> SPINNER;
            case "Fluid" -> FLUID;
            default -> null;
        };
    }

    private static int averageFluidColor(Iterable<HbmFluidStack> stacks) {
        int count = 0;
        int red = 0;
        int green = 0;
        int blue = 0;
        for (HbmFluidStack stack : stacks) {
            int color = stack.type().getColor();
            red += color >> 16 & 255;
            green += color >> 8 & 255;
            blue += color & 255;
            count++;
        }
        if (count <= 0) {
            return -1;
        }
        return (red / count) << 16 | (green / count) << 8 | blue / count;
    }
}
