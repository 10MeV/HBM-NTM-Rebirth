package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.LegacyMachineDefinition;
import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.hbm.ntm.blockentity.ChemicalPlantBlockEntity;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
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
import java.util.List;
import java.util.Map;

public class ChemicalPlantRenderer implements BlockEntityRenderer<ChemicalPlantBlockEntity> {
    private static final Map<LegacyMachineDefinition, LegacyWavefrontModel> MODELS = new IdentityHashMap<>();
    private static final LegacyWavefrontModel.SelectionHandle BASE =
            ObjMachineModels.CHEMICAL_PLANT.prepareRenderOnlyInCallOrder("Base");
    private static final LegacyWavefrontModel.SelectionHandle FRAME =
            ObjMachineModels.CHEMICAL_PLANT.prepareRenderOnlyInCallOrder("Frame");
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

            renderModelPart(model, "Base", texture, poseStack, buffer, modelLight, packedOverlay);
            if (chemicalPlant.shouldRenderFrame()) {
                renderModelPart(model, "Frame", texture, poseStack, buffer, modelLight, packedOverlay);
            }

            LegacyTileRenderPlans.ChemicalPlantMachinePlan machinePlan =
                    LegacyTileRenderPlans.chemicalPlantMachinePlan(anim);
            try (var animatedFadeScope = LegacyBlockEntityRenderCulling.animatedModelFadeScope(chemicalPlant)) {
                renderTranslatedPart(model, machinePlan.slider(), texture, poseStack, buffer, modelLight, packedOverlay);
                renderRotatingPart(model, machinePlan.spinner(), texture, poseStack, buffer, modelLight, packedOverlay);
            }

            renderProcessingFluid(chemicalPlant, model, poseStack, buffer, modelLight, packedOverlay, anim);

            poseStack.popPose();
        }
    }

    private static void renderTranslatedPart(LegacyWavefrontModel model,
            LegacyTileRenderPlans.TranslatedModelPartPlan part, ResourceLocation texture, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!part.active()) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(part.translateX(), part.translateY(), part.translateZ());
        renderModelPart(model, part.partName(), texture, poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderRotatingPart(LegacyWavefrontModel model,
            LegacyTileRenderPlans.RotatingModelPartPlan part, ResourceLocation texture, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(part.pivotX(), part.pivotY(), part.pivotZ());
        if (part.axisX() != 0.0F) {
            poseStack.mulPose(Axis.XP.rotationDegrees((float) (part.angleDegrees() * part.axisX())));
        }
        if (part.axisY() != 0.0F) {
            poseStack.mulPose(Axis.YP.rotationDegrees((float) (part.angleDegrees() * part.axisY())));
        }
        if (part.axisZ() != 0.0F) {
            poseStack.mulPose(Axis.ZP.rotationDegrees((float) (part.angleDegrees() * part.axisZ())));
        }
        poseStack.translate(-part.pivotX(), -part.pivotY(), -part.pivotZ());
        renderModelPart(model, part.partName(), texture, poseStack, buffer, packedLight, packedOverlay);
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
        LegacyTileRenderPlans.ChemicalPlantFluidPlan plan = LegacyTileRenderPlans.chemicalPlantFluidPlan(anim,
                fluidColors(recipe.getFluidOutputs()), fluidColors(recipe.getFluidInputs()));
        if (!plan.active()) {
            return;
        }
        renderModelPart(model, "Fluid", ObjMachineModels.CHEMICAL_PLANT_FLUID_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay,
                plan.color().red(), plan.color().green(), plan.color().blue(),
                Math.round((float) plan.alpha() * 255.0F), plan.blend().modernRenderMode(),
                LegacyWavefrontModel.legacyTextureMatrixDynamic(1.0F, 1.0F,
                        (float) plan.textureTranslateU(), (float) plan.textureTranslateV()));
    }

    private static void renderModelPart(LegacyWavefrontModel model, String partName, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        renderModelPart(model, partName, texture, poseStack, buffer, packedLight, packedOverlay,
                255, 255, 255, 255, LegacyTexturedRenderMode.CUTOUT_NO_CULL, LegacyWavefrontModel.UvTransform.DEFAULT);
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

    private static List<Integer> fluidColors(List<HbmFluidStack> stacks) {
        java.util.ArrayList<Integer> colors = new java.util.ArrayList<>();
        for (HbmFluidStack stack : stacks) {
            colors.add(stack.type().getColor());
        }
        return colors;
    }
}
