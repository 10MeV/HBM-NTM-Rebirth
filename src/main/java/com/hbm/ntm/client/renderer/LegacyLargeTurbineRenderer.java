package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.LegacyMachineDefinition;
import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.hbm.ntm.blockentity.LegacyLargeTurbineBlockEntity;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjModelLibrary;
import com.hbm.ntm.client.obj.ObjRenderContext;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class LegacyLargeTurbineRenderer implements BlockEntityRenderer<LegacyLargeTurbineBlockEntity> {
    private static final LegacyWavefrontModel MODEL = ObjModelLibrary.MACHINE_TURBINE_LEGACY;

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

        ObjRenderContext context = new ObjRenderContext(poseStack, buffer, state, modelLight, packedOverlay)
                .withRenderMode(LegacyMachinePartRenderContexts.renderMode(definition.renderMode()));
        MODEL.renderPart("Body", definition.textureLocation(), context);

        LegacyTileRenderPlans.RotatingModelPartPlan blades = plan.blades();
        poseStack.pushPose();
        poseStack.translate(blades.pivotX(), blades.pivotY(), blades.pivotZ());
        poseStack.mulPose(Axis.ZP.rotationDegrees((float) blades.angleDegrees()));
        poseStack.translate(-blades.pivotX(), -blades.pivotY(), -blades.pivotZ());
        MODEL.renderPart(blades.partName(),
                definition.partTextures().getOrDefault(blades.partName(), definition.textureLocation()),
                context.withPackedLight(LightTexture.FULL_BRIGHT));
        poseStack.popPose();

        poseStack.popPose();
    }
}
