package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.LegacyMachineDefinition;
import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.hbm.ntm.blockentity.ChemicalPlantBlockEntity;
import com.hbm.ntm.client.obj.LegacyObjTransforms;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjRenderContext;
import com.hbm.ntm.client.obj.ObjMachineModels;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.recipe.GenericMachineRecipe;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class ChemicalPlantRenderer implements BlockEntityRenderer<ChemicalPlantBlockEntity> {
    private static final Map<LegacyMachineDefinition, LegacyWavefrontModel> MODELS = new IdentityHashMap<>();

    public ChemicalPlantRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(ChemicalPlantBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.MACHINE;
    }

    @Override
    public void render(ChemicalPlantBlockEntity chemicalPlant, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = chemicalPlant.getBlockState();
        if (!(state.getBlock() instanceof LegacyVisibleMultiblockMachineBlock block)) {
            return;
        }

        LegacyMachineDefinition definition = block.definition();
        int modelLight = LegacyRenderLighting.resolveMachineLight(chemicalPlant, state, definition, packedLight);
        LegacyWavefrontModel model = MODELS.computeIfAbsent(definition,
                key -> new LegacyWavefrontModel(key.modelLocation(), key.textureLocation()));
        float anim = Mth.lerp(partialTick, chemicalPlant.getPrevAnim(), chemicalPlant.getAnim());

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(definition.yRotation(state)));
        Vec3 translation = definition.modelTranslation(state);
        poseStack.translate(translation.x, translation.y, translation.z);
        poseStack.mulPose(Axis.YP.rotationDegrees(definition.postModelYRotation(state)));

        model.renderPart("Base", definition.textureLocation(), poseStack, buffer, modelLight, packedOverlay);
        if (chemicalPlant.shouldRenderFrame()) {
            model.renderPart("Frame", definition.textureLocation(), poseStack, buffer, modelLight, packedOverlay);
        }

        poseStack.pushPose();
        poseStack.translate(LegacyObjTransforms.softPeakSine(anim * 0.125F) * 0.375D, 0.0D, 0.0D);
        model.renderPart("Slider", definition.textureLocation(), poseStack, buffer, modelLight, packedOverlay);
        poseStack.popPose();

        poseStack.pushPose();
        LegacyObjTransforms.rotateAroundY(poseStack, 0.5D, 0.0D, 0.5D, (anim * 15.0F) % 360.0F);
        model.renderPart("Spinner", definition.textureLocation(), poseStack, buffer, modelLight, packedOverlay);
        poseStack.popPose();

        renderProcessingFluid(chemicalPlant, state, model, poseStack, buffer, modelLight, packedOverlay, anim);

        poseStack.popPose();
    }

    private static void renderProcessingFluid(ChemicalPlantBlockEntity chemicalPlant, BlockState state,
            LegacyWavefrontModel model, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay, float anim) {
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
        ObjRenderContext context = new ObjRenderContext(poseStack, buffer, state, packedLight, packedOverlay)
                .withRgba(plan.color().red(), plan.color().green(), plan.color().blue(),
                        Math.round((float) plan.alpha() * 255.0F))
                .withRenderMode(plan.blend().modernRenderMode())
                .withLegacyTextureMatrix(1.0F, 1.0F,
                        (float) plan.textureTranslateU(), (float) plan.textureTranslateV());
        model.renderPart("Fluid", ObjMachineModels.CHEMICAL_PLANT_FLUID_TEXTURE, context);
    }

    private static List<Integer> fluidColors(List<HbmFluidStack> stacks) {
        java.util.ArrayList<Integer> colors = new java.util.ArrayList<>();
        for (HbmFluidStack stack : stacks) {
            colors.add(stack.type().getColor());
        }
        return colors;
    }
}
