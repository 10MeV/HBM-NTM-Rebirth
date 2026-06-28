package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.LegacyMachineDefinition;
import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.hbm.ntm.blockentity.ChungusBlockEntity;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjModelLibrary;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class ChungusRenderer implements BlockEntityRenderer<ChungusBlockEntity> {
    private static final LegacyWavefrontModel MODEL = ObjModelLibrary.MACHINE_CHUNGUS;
    private static final ResourceLocation TEXTURE = ObjModelLibrary.MACHINE_CHUNGUS_TEXTURE;
    private static final LegacyWavefrontModel.SelectionHandle BODY =
            MODEL.prepareRenderOnlyInCallOrder("Body");
    private static final LegacyWavefrontModel.SelectionHandle LEVER =
            MODEL.prepareRenderOnlyInCallOrder("Lever");
    private static final LegacyWavefrontModel.SelectionHandle BLADES =
            MODEL.prepareRenderOnlyInCallOrder("Blades");

    public ChungusRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(ChungusBlockEntity blockEntity) {
        return false;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.MACHINE;
    }

    @Override
    public void render(ChungusBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();
        if (!(state.getBlock() instanceof LegacyVisibleMultiblockMachineBlock block)) {
            return;
        }

        LegacyMachineDefinition definition = block.definition();
        int modelLight = LegacyRenderLighting.resolveMachineLight(blockEntity, state, definition, packedLight);

        try (LegacyRenderLighting.ModelViewSamplingScope ignored =
                LegacyRenderLighting.pushModelViewSampling(blockEntity, poseStack.last().pose())) {
            poseStack.pushPose();
            poseStack.translate(0.5D, 0.0D, 0.5D);
            poseStack.mulPose(Axis.YP.rotationDegrees(definition.yRotation(state)));
            Vec3 translation = definition.modelTranslation(state);
            poseStack.translate(translation.x, translation.y, translation.z);
            poseStack.mulPose(Axis.YP.rotationDegrees(definition.postModelYRotation(state)));

            renderPart(BODY, poseStack, buffer, modelLight, packedOverlay);

            poseStack.pushPose();
            poseStack.translate(0.0D, 0.0D, 4.5D);
            poseStack.mulPose(Axis.XP.rotationDegrees(blockEntity.getLeverAngle()));
            poseStack.translate(0.0D, 0.0D, -4.5D);
            renderPart(LEVER, poseStack, buffer, modelLight, packedOverlay);
            poseStack.popPose();

            poseStack.pushPose();
            poseStack.translate(0.0D, 2.5D, 0.0D);
            poseStack.mulPose(Axis.ZN.rotationDegrees(blockEntity.getRotor(partialTick)));
            poseStack.translate(0.0D, -2.5D, 0.0D);
            renderPart(BLADES, poseStack, buffer, modelLight, packedOverlay);
            poseStack.popPose();

            poseStack.popPose();
        }
    }

    private static void renderPart(LegacyWavefrontModel.SelectionHandle handle, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        MODEL.renderOnlyInCallOrder(TEXTURE, poseStack, buffer, packedLight, packedOverlay, handle);
    }
}
