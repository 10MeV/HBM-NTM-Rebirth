package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.block.LegacyMachineDefinition;
import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.hbm.ntm.blockentity.RadarBlockEntity;
import com.hbm.ntm.blockentity.RadarLargeBlockEntity;
import com.hbm.ntm.client.obj.ObjModelLibrary;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class RadarRenderer<T extends RadarBlockEntity> implements BlockEntityRenderer<T> {
    public RadarRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(T blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.MACHINE;
    }

    @Override
    public void render(T radar, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (radar instanceof RadarLargeBlockEntity) {
            renderLarge(radar, partialTick, poseStack, buffer, packedLight, packedOverlay);
        } else {
            renderSmall(radar, partialTick, poseStack, buffer, packedLight, packedOverlay);
        }
    }

    private static void renderSmall(RadarBlockEntity radar, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = radar.getBlockState();
        LegacyWavefrontModel model = ObjModelLibrary.MACHINE_RADAR_LEGACY;
        int modelLight = LegacyRenderLighting.resolveBlockEntityLight(radar, packedLight);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        if (state.hasProperty(HorizontalMachineBlock.FACING)) {
            poseStack.mulPose(Axis.YP.rotationDegrees((state.getValue(HorizontalMachineBlock.FACING).toYRot() + 180.0F) % 360.0F));
        }
        model.renderPart("Base", poseStack, buffer, modelLight, packedOverlay);

        poseStack.pushPose();
        poseStack.mulPose(Axis.YN.rotationDegrees(interpolatedRotation(radar, partialTick)));
        poseStack.translate(-0.125D, 0.0D, 0.0D);
        model.renderPart("Dish", poseStack, buffer, modelLight, packedOverlay);
        poseStack.popPose();

        poseStack.popPose();
    }

    private static void renderLarge(RadarBlockEntity radar, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = radar.getBlockState();
        if (!(state.getBlock() instanceof LegacyVisibleMultiblockMachineBlock block)) {
            return;
        }

        LegacyMachineDefinition definition = block.definition();
        LegacyWavefrontModel model = ObjModelLibrary.MACHINE_RADAR_LARGE_LEGACY;
        int modelLight = LegacyRenderLighting.resolveMachineLight(radar, state, definition, packedLight);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(definition.yRotation(state)));
        Vec3 translation = definition.modelTranslation(state);
        poseStack.translate(translation.x, translation.y, translation.z);
        poseStack.mulPose(Axis.YP.rotationDegrees(definition.postModelYRotation(state)));

        model.renderPart("Radar", definition.textureLocation(), poseStack, buffer, modelLight, packedOverlay);

        poseStack.pushPose();
        poseStack.mulPose(Axis.YN.rotationDegrees(interpolatedRotation(radar, partialTick)));
        model.renderPart("Dish", definition.textureLocation(), poseStack, buffer, modelLight, packedOverlay);
        poseStack.popPose();

        poseStack.popPose();
    }

    private static float interpolatedRotation(RadarBlockEntity radar, float partialTick) {
        return Mth.lerp(partialTick, radar.getPreviousRotation(), radar.getRotation());
    }
}
