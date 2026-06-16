package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.LegacyMachineDefinition;
import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.hbm.ntm.blockentity.TurbofanBlockEntity;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjMachineModels;
import com.hbm.ntm.client.obj.ObjModelLibrary;
import com.hbm.ntm.client.obj.ObjRenderContext;
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

    public TurbofanRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(TurbofanBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.MACHINE;
    }

    @Override
    public void render(TurbofanBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
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

        ObjRenderContext context = new ObjRenderContext(poseStack, buffer, state, modelLight, packedOverlay);
        MODEL.renderPart("Body", definition.textureLocation(), context);

        poseStack.pushPose();
        poseStack.translate(0.0D, LegacyTileRenderPlans.TURBOFAN_BLADE_PIVOT_Y, 0.0D);
        poseStack.mulPose(Axis.ZN.rotationDegrees(blockEntity.getBladeSpin(partialTick)));
        poseStack.translate(0.0D, -LegacyTileRenderPlans.TURBOFAN_BLADE_PIVOT_Y, 0.0D);
        MODEL.renderPart("Blades", definition.textureLocation(), context);
        poseStack.popPose();

        ResourceLocation afterburnerTexture = blockEntity.getAfterburner() == 0
                ? ObjMachineModels.TURBOFAN_BACK_TEXTURE
                : ObjMachineModels.TURBOFAN_AFTERBURNER_TEXTURE;
        MODEL.renderPart("Afterburner", afterburnerTexture, context);

        poseStack.popPose();
    }
}
