package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.LegacyMachineDefinition;
import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.hbm.ntm.blockentity.ChemicalFactoryBlockEntity;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.IdentityHashMap;
import java.util.Map;

public class ChemicalFactoryRenderer implements BlockEntityRenderer<ChemicalFactoryBlockEntity> {
    private static final Map<LegacyMachineDefinition, LegacyWavefrontModel> MODELS = new IdentityHashMap<>();

    public ChemicalFactoryRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(ChemicalFactoryBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.MACHINE;
    }

    @Override
    public void render(ChemicalFactoryBlockEntity chemicalFactory, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = chemicalFactory.getBlockState();
        if (!(state.getBlock() instanceof LegacyVisibleMultiblockMachineBlock block)) {
            return;
        }

        LegacyMachineDefinition definition = block.definition();
        int modelLight = LegacyRenderLighting.resolveMachineLight(chemicalFactory, state, definition, packedLight);
        LegacyWavefrontModel model = MODELS.computeIfAbsent(definition,
                key -> new LegacyWavefrontModel(key.modelLocation(), key.textureLocation()));
        float anim = Mth.lerp(partialTick, chemicalFactory.getPrevAnim(), chemicalFactory.getAnim());

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(definition.yRotation(state)));
        Vec3 translation = definition.modelTranslation(state);
        poseStack.translate(translation.x, translation.y, translation.z);

        model.renderPart("Base", definition.textureLocation(), poseStack, buffer, modelLight, packedOverlay);
        if (chemicalFactory.shouldRenderFrame()) {
            model.renderPart("Frame", definition.textureLocation(), poseStack, buffer, modelLight, packedOverlay);
        }

        poseStack.pushPose();
        rotateFanAround(poseStack, 1.0D, (-anim * 45.0F) % 360.0F);
        model.renderPart("Fan1", definition.textureLocation(), poseStack, buffer, modelLight, packedOverlay);
        poseStack.popPose();

        poseStack.pushPose();
        rotateFanAround(poseStack, -1.0D, (-anim * 45.0F) % 360.0F);
        model.renderPart("Fan2", definition.textureLocation(), poseStack, buffer, modelLight, packedOverlay);
        poseStack.popPose();

        poseStack.popPose();
    }

    private static void rotateFanAround(PoseStack poseStack, double x, float degrees) {
        poseStack.translate(x, 0.0D, 0.0D);
        poseStack.mulPose(Axis.YP.rotationDegrees(degrees));
        poseStack.translate(-x, 0.0D, 0.0D);
    }
}
